package com.acme.leavemgmt.dao;

import com.acme.leavemgmt.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StatsDAO {

    public static class Stats {
        public int pendingCount;
        public int approvedThisMonth;
        public int approvalNumerator;
        public int approvalDenominator;
    }

    /** Thống kê theo phòng ban (Division Dashboard) */
    public Stats getDivisionStats(String department) {
        Stats s = new Stats();
        if (department == null || department.isBlank()) return s;

        String sqlPending =
            "SELECT COUNT(*) " +
            "FROM dbo.Requests r " +
            "JOIN dbo.Users u ON u.id = r.employee_id " +
            "WHERE u.department = ? AND r.status = 'PENDING'";

        String sqlApprovedThisMonth =
            "SELECT COUNT(*) " +
            "FROM dbo.Requests r " +
            "JOIN dbo.Users u ON u.id = r.employee_id " +
            "WHERE u.department = ? AND r.status = 'APPROVED' " +
            "AND MONTH(r.approved_at) = MONTH(GETDATE()) " +
            "AND YEAR(r.approved_at) = YEAR(GETDATE())";

        String sqlApprovedTotal =
            "SELECT COUNT(*) " +
            "FROM dbo.Requests r " +
            "JOIN dbo.Users u ON u.id = r.employee_id " +
            "WHERE u.department = ? AND r.status = 'APPROVED'";

        String sqlProcessedTotal =
            "SELECT COUNT(*) " +
            "FROM dbo.Requests r " +
            "JOIN dbo.Users u ON u.id = r.employee_id " +
            "WHERE u.department = ? AND r.status IN ('APPROVED','REJECTED')";

        try (Connection cn = DBConnection.getConnection()) {
            s.pendingCount        = scalarInt(cn, sqlPending, department);
            s.approvedThisMonth   = scalarInt(cn, sqlApprovedThisMonth, department);
            s.approvalNumerator   = scalarInt(cn, sqlApprovedTotal, department);
            s.approvalDenominator = scalarInt(cn, sqlProcessedTotal, department);
        } catch (SQLException ex) {
            System.err.println("[StatsDAO] getDivisionStats error: " + ex.getMessage());
        }
        return s;
    }

    // helper nhỏ để đọc COUNT(*)
    private int scalarInt(Connection cn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++)
                ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
