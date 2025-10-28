package com.acme.leavemgmt.dao;

import com.acme.leavemgmt.util.DBConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/**
 * DAO thống kê cho Division Dashboard (SQL Server).
 * Users(u): id, department, status(1/0), full_name, ...
 * Requests(r): id, employee_id, status('PENDING'|'APPROVED'|'REJECTED'),
 *              created_at, approved_at, from_date, to_date, type, reason
 */
public class StatsDAO {

    /* ================== DTO (JavaBean cho JSP EL) ================== */
    public static class Stats {
        private int headcount;            // tổng nhân sự active của phòng
        private int pendingCount;         // đơn chờ duyệt
        private int approvedThisMonth;    // số đơn đã duyệt trong tháng hiện tại
        private int approvalNumerator;    // số đơn APPROVED trong khoảng
        private int approvalDenominator;  // số đơn đã xử lý (APPROVED+REJECTED) trong khoảng
        private LocalDate fromDate;       // mốc from
        private LocalDate toDate;         // mốc to

        // === Getters (JSP EL cần các hàm này) ===
        public int getHeadcount() { return headcount; }
        public int getPendingCount() { return pendingCount; }
        public int getApprovedThisMonth() { return approvedThisMonth; }
        public int getApprovalNumerator() { return approvalNumerator; }
        public int getApprovalDenominator() { return approvalDenominator; }
        public LocalDate getFromDate() { return fromDate; }
        public LocalDate getToDate() { return toDate; }

        // EL: ${stats.approvalRate}
        public double getApprovalRate() {
            return approvalDenominator == 0 ? 0.0
                    : (approvalNumerator * 1.0) / approvalDenominator;
        }

        // === Setters (dùng nội bộ DAO) ===
        public void setHeadcount(int v) { this.headcount = v; }
        public void setPendingCount(int v) { this.pendingCount = v; }
        public void setApprovedThisMonth(int v) { this.approvedThisMonth = v; }
        public void setApprovalNumerator(int v) { this.approvalNumerator = v; }
        public void setApprovalDenominator(int v) { this.approvalDenominator = v; }
        public void setFromDate(LocalDate v) { this.fromDate = v; }
        public void setToDate(LocalDate v) { this.toDate = v; }
    }

    /* ================== API cũ – giữ nguyên chữ ký ================== */
    /** Thống kê theo phòng ban (mặc định trong tháng hiện tại). */
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
        if (isBlank(department)) return s;

        String sqlHeadcount =
            "SELECT COUNT(*) FROM dbo.Users WHERE department = ? AND status = 1";

        String sqlPending =
            "SELECT COUNT(*) " +
            "FROM dbo.Requests r JOIN dbo.Users u ON u.id = r.employee_id " +
            "WHERE u.department = ? AND r.status = 'PENDING'";

        // đã duyệt trong tháng hiện tại
        String sqlApprovedThisMonth =
            "SELECT COUNT(*) " +
            "FROM dbo.Requests r JOIN dbo.Users u ON u.id = r.employee_id " +
            "WHERE u.department = ? AND r.status = 'APPROVED' " +
            "  AND MONTH(r.approved_at) = MONTH(GETDATE()) " +
            "  AND YEAR(r.approved_at)  = YEAR(GETDATE())";

        // approved (trong khoảng approved_at)
        String sqlApprovedInRange =
            "SELECT COUNT(*) " +
            "FROM dbo.Requests r JOIN dbo.Users u ON u.id = r.employee_id " +
            "WHERE u.department = ? AND r.status = 'APPROVED' " +
            "  AND CAST(r.approved_at AS date) BETWEEN ? AND ?";

        // processed (APPROVED/REJECTED) trong khoảng (theo created_at)
        String sqlProcessedInRange =
            "SELECT COUNT(*) " +
            "FROM dbo.Requests r JOIN dbo.Users u ON u.id = r.employee_id " +
            "WHERE u.department = ? AND r.status IN ('APPROVED','REJECTED') " +
            "  AND CAST(r.created_at  AS date) BETWEEN ? AND ?";

        try (Connection cn = DBConnection.getConnection()) {
            s.setHeadcount(           scalarInt(cn, sqlHeadcount, department));
            s.setPendingCount(        scalarInt(cn, sqlPending, department));
            s.setApprovedThisMonth(   scalarInt(cn, sqlApprovedThisMonth, department));
            s.setApprovalNumerator(   scalarInt(cn, sqlApprovedInRange, department, toSqlDate(from), toSqlDate(to)));
            s.setApprovalDenominator( scalarInt(cn, sqlProcessedInRange, department, toSqlDate(from), toSqlDate(to)));
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
