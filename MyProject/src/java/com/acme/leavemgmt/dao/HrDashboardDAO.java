package com.acme.leavemgmt.dao;

import com.acme.leavemgmt.dto.DashboardKpis;
import com.acme.leavemgmt.dto.TodayLeave;
import com.acme.leavemgmt.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class HrDashboardDAO {
    private static final Logger LOG = Logger.getLogger(HrDashboardDAO.class.getName());

    private Connection getConn() throws SQLException {
        // Ưu tiên util có sẵn
        return DBConnection.getConnection();

        // Hoặc DriverManager (nếu bạn không dùng DBConnection):
        // String url = "jdbc:sqlserver://localhost:1433;databaseName=LeaveMgmt;encrypt=false";
        // String user = "sa";
        // String pass = "yourStrong(!)Password";
        // return DriverManager.getConnection(url, user, pass);
    }

    public DashboardKpis getKpis() throws SQLException {
        final String totalEmpSql =
            "SELECT COUNT(*) FROM dbo.Users WHERE status = 'ACTIVE'";

        final String onLeaveTodaySql =
            "SELECT COUNT(*) " +
            "FROM dbo.Requests r " +
            "WHERE r.status = 'APPROVED' " +
            "  AND CAST(GETDATE() AS date) BETWEEN CAST(r.start_date AS date) AND CAST(r.end_date AS date)";

        final String internsSql =
            "SELECT COUNT(*) FROM dbo.Users " +
            "WHERE status = 'ACTIVE' AND (employment_type IN ('INTERN','INTERN_STUDENT'))";

        // Lưu ý: cột contract_end có thể CHƯA tồn tại -> try/catch an toàn
        final String contractEndingSoonSql =
            "SELECT COUNT(*) FROM dbo.Users " +
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

            // KPI hợp đồng sắp hết hạn – không để trang vỡ nếu thiếu cột
            try (PreparedStatement ps = cn.prepareStatement(contractEndingSoonSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) k.setContractEndingSoon(rs.getInt(1));
            } catch (SQLException e) {
                // Trường hợp thiếu cột 'contract_end' hoặc tên khác -> đặt 0 và log cảnh báo
                LOG.warning("contractEndingSoon KPI skipped: " + e.getMessage());
                k.setContractEndingSoon(0);
            }
        }
        return k;
    }

    public List<TodayLeave> listTodayLeaves() throws SQLException {
        final String sql =
            "SELECT u.full_name AS fullName, d.name AS divisionName, " +
            "       r.start_date AS startDate, r.end_date AS endDate " +
            "FROM dbo.Requests r " +
            "JOIN dbo.Users u ON u.id = r.user_id " +
            "LEFT JOIN dbo.Divisions d ON d.id = u.division_id " +
            "WHERE r.status = 'APPROVED' " +
            "  AND CAST(GETDATE() AS date) BETWEEN CAST(r.start_date AS date) AND CAST(r.end_date AS date) " +
            "ORDER BY u.full_name";

        List<TodayLeave> list = new ArrayList<>();
        try (Connection cn = getConn();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // Lấy java.sql.Timestamp -> dùng được với fmt:formatDate (java.util.Date)
                Timestamp sd = rs.getTimestamp("startDate");
                Timestamp ed = rs.getTimestamp("endDate");

                TodayLeave tl = new TodayLeave(
                    rs.getString("fullName"),
                    rs.getString("divisionName"),
                    sd, ed
                );
                list.add(tl);
            }
        }
        return list;
    }
}
