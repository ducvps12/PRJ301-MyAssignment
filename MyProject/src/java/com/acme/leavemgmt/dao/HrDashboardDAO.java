package com.acme.leavemgmt.dao;

import com.acme.leavemgmt.dto.DashboardKpis;
import com.acme.leavemgmt.dto.TodayLeave;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HrDashboardDAO {

    private Connection getConn() throws SQLException {
        // TODO: thay bằng cách lấy connection của bạn (DataSource/JNDI hoặc DriverManager)
        // Ví dụ DriverManager:
        // return DriverManager.getConnection(DB_URL, USER, PASS);
        throw new UnsupportedOperationException("Implement getConn()");
    }

    public DashboardKpis getKpis() throws SQLException {
        String totalEmpSql =
            "SELECT COUNT(*) FROM Users WHERE status = 'ACTIVE'";

        String onLeaveTodaySql =
            "SELECT COUNT(*) " +
            "FROM Requests r " +
            "WHERE r.status = 'APPROVED' " +
            "  AND CAST(GETDATE() AS date) BETWEEN CAST(r.start_date AS date) AND CAST(r.end_date AS date)";

        String internsSql =
            "SELECT COUNT(*) FROM Users " +
            "WHERE status = 'ACTIVE' AND (employment_type = 'INTERN' OR employment_type = 'INTERN_STUDENT')";

        String contractEndingSoonSql =
            "SELECT COUNT(*) FROM Users " +
            "WHERE status = 'ACTIVE' " +
            "  AND contract_end IS NOT NULL " +
            "  AND CAST(contract_end AS date) BETWEEN CAST(GETDATE() AS date) AND DATEADD(day, 30, CAST(GETDATE() AS date))";

        DashboardKpis k = new DashboardKpis();

        try (Connection cn = getConn()) {
            try (PreparedStatement ps = cn.prepareStatement(totalEmpSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) k.setTotalEmployees(rs.getInt(1));
            }

            try (PreparedStatement ps = cn.prepareStatement(onLeaveTodaySql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) k.setOnLeaveToday(rs.getInt(1));
            }

            try (PreparedStatement ps = cn.prepareStatement(internsSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) k.setInterns(rs.getInt(1));
            }

            try (PreparedStatement ps = cn.prepareStatement(contractEndingSoonSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) k.setContractEndingSoon(rs.getInt(1));
            }
        }
        return k;
    }

    public List<TodayLeave> listTodayLeaves() throws SQLException {
        String sql =
            "SELECT u.full_name AS fullName, d.name AS divisionName, r.start_date AS startDate, r.end_date AS endDate " +
            "FROM Requests r " +
            "JOIN Users u ON u.id = r.user_id " +
            "LEFT JOIN Divisions d ON d.id = u.division_id " +
            "WHERE r.status = 'APPROVED' " +
            "  AND CAST(GETDATE() AS date) BETWEEN CAST(r.start_date AS date) AND CAST(r.end_date AS date) " +
            "ORDER BY u.full_name";

        List<TodayLeave> list = new ArrayList<>();
        try (Connection cn = getConn();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                TodayLeave tl = new TodayLeave(
                    rs.getString("fullName"),
                    rs.getString("divisionName"),
                    rs.getTimestamp("startDate"),
                    rs.getTimestamp("endDate")
                );
                list.add(tl);
            }
        }
        return list;
    }
}
