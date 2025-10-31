package com.acme.leavemgmt.dao;

import com.acme.leavemgmt.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/**
 * DAO thống kê cho Division Dashboard (SQL Server).
 *
 * Bảng gốc tham chiếu theo ảnh bạn chụp:
 *   - dbo.Requests: id, user_id, type, status, reason,
 *                   start_date, end_date,
 *                   created_at, updated_at, created_by, processed_by, manager_note, leave_type_id
 *   - dbo.Users   : id, department, status, full_name, ...
 *
 * Chú ý: nếu tên bảng/ cột của bạn khác thì sửa lại đúng tên ở dưới.
 */
public class StatsDAO {

    /* ================== DTO (JavaBean cho JSP EL) ================== */
    public static class Stats {
        private int headcount;            // tổng nhân sự active của phòng
        private int pendingCount;         // đơn chờ duyệt
        private int approvedThisMonth;    // số đơn đã duyệt trong tháng hiện tại
        private int approvalNumerator;    // số đơn APPROVED trong khoảng
        private int approvalDenominator;  // số đơn đã xử lý (APPROVED+REJECTED) trong khoảng
        private LocalDate fromDate;
        private LocalDate toDate;

        public int getHeadcount() { return headcount; }
        public int getPendingCount() { return pendingCount; }
        public int getApprovedThisMonth() { return approvedThisMonth; }
        public int getApprovalNumerator() { return approvalNumerator; }
        public int getApprovalDenominator() { return approvalDenominator; }
        public LocalDate getFromDate() { return fromDate; }
        public LocalDate getToDate() { return toDate; }

        // EL: ${stats.approvalRate}
        public double getApprovalRate() {
            return approvalDenominator == 0
                    ? 0.0
                    : (approvalNumerator * 1.0) / approvalDenominator;
        }

        public void setHeadcount(int v) { this.headcount = v; }
        public void setPendingCount(int v) { this.pendingCount = v; }
        public void setApprovedThisMonth(int v) { this.approvedThisMonth = v; }
        public void setApprovalNumerator(int v) { this.approvalNumerator = v; }
        public void setApprovalDenominator(int v) { this.approvalDenominator = v; }
        public void setFromDate(LocalDate v) { this.fromDate = v; }
        public void setToDate(LocalDate v) { this.toDate = v; }
    }

    /* ================== API cũ – giữ nguyên chữ ký ================== */
    /** Thống kê theo phòng ban (trong tháng hiện tại). */
    public Stats getDivisionStats(String department) {
        LocalDate today = LocalDate.now();
        return getDivisionStats(department, today.withDayOfMonth(1), today);
    }

    /* ================== API mở rộng cho dashboard ================== */
    /** Thống kê theo phòng ban trong khoảng [from..to]. */
    public Stats getDivisionStats(String department, LocalDate from, LocalDate to) {
        Stats s = new Stats();
        s.setFromDate(from);
        s.setToDate(to);

        // SQL phải dùng đúng tên cột/bảng của bạn
        String sqlHeadcount =
                "SELECT COUNT(*) FROM dbo.Users WHERE status = 1"
                        + (isBlank(department) ? "" : " AND department = ?");

        String sqlPending =
                "SELECT COUNT(*) " +
                "FROM dbo.Requests r " +
                "JOIN dbo.Users u ON u.id = r.user_id " +   // CHỖ NÀY: user_id
                "WHERE r.status = 'PENDING' " +
                (isBlank(department) ? "" : "AND u.department = ?");

        String sqlApprovedThisMonth =
                "SELECT COUNT(*) " +
                "FROM dbo.Requests r " +
                "JOIN dbo.Users u ON u.id = r.user_id " +
                "WHERE r.status = 'APPROVED' " +
                (isBlank(department) ? "" : "AND u.department = ? ") +
                "AND MONTH(r.updated_at) = MONTH(GETDATE()) " +   // hoặc approved_at nếu bạn có
                "AND YEAR(r.updated_at)  = YEAR(GETDATE())";

        String sqlApprovedInRange =
                "SELECT COUNT(*) " +
                "FROM dbo.Requests r " +
                "JOIN dbo.Users u ON u.id = r.user_id " +
                "WHERE r.status = 'APPROVED' " +
                (isBlank(department) ? "" : "AND u.department = ? ") +
                "AND CAST(r.updated_at AS date) BETWEEN ? AND ?";

        String sqlProcessedInRange =
                "SELECT COUNT(*) " +
                "FROM dbo.Requests r " +
                "JOIN dbo.Users u ON u.id = r.user_id " +
                "WHERE r.status IN ('APPROVED','REJECTED') " +
                (isBlank(department) ? "" : "AND u.department = ? ") +
                "AND CAST(r.created_at AS date) BETWEEN ? AND ?";

        try (Connection cn = DBConnection.getConnection()) {

            // 1) headcount
            if (isBlank(department)) {
                s.setHeadcount(scalarInt(cn, sqlHeadcount));
            } else {
                s.setHeadcount(scalarInt(cn, sqlHeadcount, department));
            }

            // 2) pending
            if (isBlank(department)) {
                s.setPendingCount(scalarInt(cn, sqlPending));
            } else {
                s.setPendingCount(scalarInt(cn, sqlPending, department));
            }

            // 3) approved this month
            if (isBlank(department)) {
                s.setApprovedThisMonth(scalarInt(cn, sqlApprovedThisMonth));
            } else {
                s.setApprovedThisMonth(scalarInt(cn, sqlApprovedThisMonth, department));
            }

            // 4) approved in range
            if (isBlank(department)) {
                s.setApprovalNumerator(scalarInt(cn, sqlApprovedInRange,
                        toSqlDate(from), toSqlDate(to)));
            } else {
                s.setApprovalNumerator(scalarInt(cn, sqlApprovedInRange,
                        department, toSqlDate(from), toSqlDate(to)));
            }

            // 5) processed in range
            if (isBlank(department)) {
                s.setApprovalDenominator(scalarInt(cn, sqlProcessedInRange,
                        toSqlDate(from), toSqlDate(to)));
            } else {
                s.setApprovalDenominator(scalarInt(cn, sqlProcessedInRange,
                        department, toSqlDate(from), toSqlDate(to)));
            }

        } catch (SQLException ex) {
            System.err.println("[StatsDAO] getDivisionStats error: " + ex.getMessage());
        }

        return s;
    }

    /** Danh sách đơn PENDING của phòng (mới nhất trước). */
    public List<Map<String, Object>> getDivisionPendingRequests(String department) {
        List<Map<String, Object>> list = new ArrayList<>();

        String sql =
                "SELECT r.id, u.full_name, r.type, r.start_date, r.end_date, r.reason, r.created_at " +
                "FROM dbo.Requests r " +
                "JOIN dbo.Users u ON u.id = r.user_id " +
                "WHERE r.status = 'PENDING' " +
                (isBlank(department) ? "" : "AND u.department = ? ") +
                "ORDER BY r.created_at DESC";

        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            if (isBlank(department)) {
                // không set param
            } else {
                ps.setString(1, department);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id",       rs.getInt("id"));
                    m.put("fullName", rs.getString("full_name"));
                    m.put("type",     rs.getString("type"));
                    m.put("from",     rs.getDate("start_date"));
                    m.put("to",       rs.getDate("end_date"));
                    m.put("reason",   rs.getString("reason"));
                    m.put("createdAt",rs.getTimestamp("created_at"));
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
        if (day == null) day = LocalDate.now();

        String sql =
                "SELECT u.full_name, r.type, r.start_date, r.end_date " +
                "FROM dbo.Requests r " +
                "JOIN dbo.Users u ON u.id = r.user_id " +
                "WHERE r.status = 'APPROVED' " +
                "  AND ? BETWEEN r.start_date AND r.end_date " +
                (isBlank(department) ? "" : "AND u.department = ? ") +
                "ORDER BY u.full_name ASC";

        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setDate(1, toSqlDate(day));
            if (!isBlank(department)) {
                ps.setString(2, department);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("fullName", rs.getString("full_name"));
                    m.put("type",     rs.getString("type"));
                    m.put("from",     rs.getDate("start_date"));
                    m.put("to",       rs.getDate("end_date"));
                    list.add(m);
                }
            }

        } catch (SQLException ex) {
            System.err.println("[StatsDAO] getDivisionTodayOff error: " + ex.getMessage());
        }

        return list;
    }

    /* ================== helpers ================== */

    private int scalarInt(Connection cn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private static java.sql.Date toSqlDate(LocalDate d) {
        return (d == null) ? null : java.sql.Date.valueOf(d);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
