package com.acme.leavemgmt.dao;

import com.acme.leavemgmt.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/**
 * DAO thống kê cho Division Dashboard (SQL Server).
 * Bảng giả định:
 *   - Users(u): id, department, status (1/0), full_name, ...
 *   - Requests(r): id, employee_id, status('PENDING'|'APPROVED'|'REJECTED'),
 *                  created_at, approved_at, from_date, to_date, type, reason
 *
 * Lưu ý: luôn nuốt SQLException, log nhẹ và trả dữ liệu rỗng/an toàn.
 */
public class StatsDAO {

    /* ================== DTO nhỏ gọn ================== */
    public static class Stats {
        public int headcount;             // tổng nhân sự active của phòng
        public int pendingCount;          // đơn chờ duyệt (theo phòng)
        public int approvedThisMonth;     // số đơn đã duyệt trong tháng hiện tại
        public int approvalNumerator;     // tổng đơn APPROVED (từ trước tới nay/trong khoảng)
        public int approvalDenominator;   // tổng đơn đã xử lý (APPROVED+REJECTED)
        public LocalDate fromDate;        // mốc from (nếu có)
        public LocalDate toDate;          // mốc to (nếu có)

        public double approvalRate() {
            return (approvalDenominator == 0) ? 0.0
                    : (approvalNumerator * 1.0) / approvalDenominator;
        }
    }

    /* ================== API cũ – giữ nguyên chữ ký ================== */

    /** Thống kê theo phòng ban (mặc định trong tháng hiện tại/hôm nay). */
    public Stats getDivisionStats(String department) {
        LocalDate today = LocalDate.now();
        return getDivisionStats(department, today.withDayOfMonth(1), today);
    }

    /* ================== API mở rộng cho dashboard ================== */

    /** Thống kê theo phòng ban trong khoảng [from..to] (tính theo created/approved). */
    public Stats getDivisionStats(String department, LocalDate from, LocalDate to) {
        Stats s = new Stats();
        s.fromDate = from;
        s.toDate = to;
        if (isBlank(department)) return s;

        String sqlHeadcount =
            "SELECT COUNT(*) FROM dbo.Users WHERE department = ? AND status = 1";

        String sqlPending =
            "SELECT COUNT(*) " +
            "FROM dbo.Requests r JOIN dbo.Users u ON u.id = r.employee_id " +
            "WHERE u.department = ? AND r.status = 'PENDING'";

        // đã duyệt trong tháng hiện tại (giữ nguyên ý nghĩa field cũ)
        String sqlApprovedThisMonth =
            "SELECT COUNT(*) " +
            "FROM dbo.Requests r JOIN dbo.Users u ON u.id = r.employee_id " +
            "WHERE u.department = ? AND r.status = 'APPROVED' " +
            "  AND MONTH(r.approved_at) = MONTH(GETDATE()) " +
            "  AND YEAR(r.approved_at)  = YEAR(GETDATE())";

        // approved (giới hạn trong khoảng from..to nếu có)
        String sqlApprovedInRange =
            "SELECT COUNT(*) " +
            "FROM dbo.Requests r JOIN dbo.Users u ON u.id = r.employee_id " +
            "WHERE u.department = ? AND r.status = 'APPROVED' " +
            "  AND CAST(r.approved_at AS date) BETWEEN ? AND ?";

        // processed (APPROVED/REJECTED) trong khoảng
        String sqlProcessedInRange =
            "SELECT COUNT(*) " +
            "FROM dbo.Requests r JOIN dbo.Users u ON u.id = r.employee_id " +
            "WHERE u.department = ? AND r.status IN ('APPROVED','REJECTED') " +
            "  AND CAST(r.created_at  AS date) BETWEEN ? AND ?";

        try (Connection cn = DBConnection.getConnection()) {
            s.headcount            = scalarInt(cn, sqlHeadcount, department);
            s.pendingCount         = scalarInt(cn, sqlPending, department);
            s.approvedThisMonth    = scalarInt(cn, sqlApprovedThisMonth, department);
            s.approvalNumerator    = scalarInt(cn, sqlApprovedInRange, department, toSqlDate(from), toSqlDate(to));
            s.approvalDenominator  = scalarInt(cn, sqlProcessedInRange, department, toSqlDate(from), toSqlDate(to));
        } catch (SQLException ex) {
            System.err.println("[StatsDAO] getDivisionStats(range) error: " + ex.getMessage());
        }
        return s;
    }

    /** Danh sách đơn PENDING của phòng (mới nhất trước). */
    public List<Map<String, Object>> getDivisionPendingRequests(String department) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (isBlank(department)) return list;

        String sql =
            "SELECT r.id, u.full_name, r.type, r.from_date, r.to_date, r.reason, r.created_at " +
            "FROM dbo.Requests r JOIN dbo.Users u ON u.id = r.employee_id " +
            "WHERE u.department = ? AND r.status = 'PENDING' " +
            "ORDER BY r.created_at DESC";

        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, department);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id",        rs.getInt("id"));
                    m.put("fullName",  rs.getString("full_name"));
                    m.put("type",      rs.getString("type"));
                    m.put("from",      rs.getDate("from_date"));
                    m.put("to",        rs.getDate("to_date"));
                    m.put("reason",    rs.getString("reason"));
                    m.put("createdAt", rs.getTimestamp("created_at"));
                    list.add(m);
                }
            }
        } catch (SQLException ex) {
            System.err.println("[StatsDAO] getDivisionPendingRequests error: " + ex.getMessage());
        }
        return list;
    }

    /** Danh sách người đang nghỉ trong ngày 'day' của phòng. */
    public List<Map<String, Object>> getDivisionTodayOff(String department, LocalDate day) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (isBlank(department)) return list;
        if (day == null) day = LocalDate.now();

        String sql =
            "SELECT u.full_name, r.type, r.from_date, r.to_date " +
            "FROM dbo.Requests r JOIN dbo.Users u ON u.id = r.employee_id " +
            "WHERE u.department = ? AND r.status = 'APPROVED' " +
            "  AND ? BETWEEN r.from_date AND r.to_date " +
            "ORDER BY u.full_name ASC";

        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, department);
            ps.setDate(2, toSqlDate(day));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("fullName", rs.getString("full_name"));
                    m.put("type",     rs.getString("type"));
                    m.put("from",     rs.getDate("from_date"));
                    m.put("to",       rs.getDate("to_date"));
                    list.add(m);
                }
            }
        } catch (SQLException ex) {
            System.err.println("[StatsDAO] getDivisionTodayOff error: " + ex.getMessage());
        }
        return list;
    }

    /* ================== helpers ================== */

    // helper nhỏ để đọc COUNT(*)
    private int scalarInt(Connection cn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        }
    }

    private static java.sql.Date toSqlDate(LocalDate d) {
        return (d == null) ? null : java.sql.Date.valueOf(d);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
