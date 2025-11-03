package com.acme.leavemgmt.dao;

import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HrDAO {

    public int countEmployees() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Users WHERE deleted_at IS NULL";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next(); return rs.getInt(1);
        }
    }

    public int countInterns() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Users WHERE role='INTERN' AND deleted_at IS NULL";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next(); return rs.getInt(1);
        }
    }

    public int countContractEndingInDays(int days) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Users WHERE contract_end IS NOT NULL " +
                "AND DATEDIFF(DAY, GETDATE(), contract_end) BETWEEN 0 AND ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, days);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
        }
    }

    public int countOnLeaveToday() throws SQLException {
        String sql = """
            SELECT COUNT(DISTINCT r.user_id)
            FROM Requests r
            WHERE r.status='APPROVED'
              AND CAST(GETDATE() AS DATE) BETWEEN r.from_date AND r.to_date
        """;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next(); return rs.getInt(1);
        }
    }

    public List<OnLeaveRow> listOnLeaveToday(int limit) throws SQLException {
        String sql = """
            SELECT TOP (?) u.id, u.fullname, d.name AS division_name, r.from_date, r.to_date
            FROM Requests r
            JOIN Users u ON u.id=r.user_id
            LEFT JOIN Divisions d ON d.id=u.division_id
            WHERE r.status='APPROVED'
              AND CAST(GETDATE() AS DATE) BETWEEN r.from_date AND r.to_date
            ORDER BY u.fullname
        """;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                List<OnLeaveRow> list = new ArrayList<>();
                while (rs.next()) {
                    OnLeaveRow row = new OnLeaveRow();
                    row.userId = rs.getInt("id");
                    row.fullname = rs.getString("fullname");
                    row.divisionName = rs.getString("division_name");
                    row.from = rs.getDate("from_date").toLocalDate();
                    row.to = rs.getDate("to_date").toLocalDate();
                    list.add(row);
                }
                return list;
            }
        }
    }

    public List<User> searchUsers(String q, String role, Integer divisionId, int page, int pageSize) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("""
            SELECT id, username, fullname, email, role, division_id, status, job_title, join_date, contract_end
            FROM Users WHERE deleted_at IS NULL
        """);
        List<Object> args = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            sb.append(" AND (fullname LIKE ? OR username LIKE ? OR email LIKE ?) ");
            args.add("%"+q+"%"); args.add("%"+q+"%"); args.add("%"+q+"%");
        }
        if (role != null && !role.isBlank()) {
            sb.append(" AND role = ? "); args.add(role);
        }
        if (divisionId != null) {
            sb.append(" AND division_id = ? "); args.add(divisionId);
        }
        sb.append(" ORDER BY fullname OFFSET ? ROWS FETCH NEXT ? ROWS ONLY ");
        args.add((page-1)*pageSize); args.add(pageSize);

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sb.toString())) {
            for (int i=0;i<args.size();i++) ps.setObject(i+1, args.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                List<User> list = new ArrayList<>();
                while (rs.next()) {
                    User u = new User();
                    u.setId(rs.getInt("id"));
                    u.setUsername(rs.getString("username"));
                    u.setFullname(rs.getString("fullname"));
                    u.setEmail(rs.getString("email"));
                    u.setRole(rs.getString("role"));
                    u.setDivisionId((Integer) rs.getObject("division_id"));
                    u.setStatus(rs.getString("status"));
                    u.setJobTitle(rs.getString("job_title"));
                    Date jd = rs.getDate("join_date"); if (jd!=null) u.setJoinDate(jd.toLocalDate());
                    Date ce = rs.getDate("contract_end"); if (ce!=null) u.setContractEnd(ce.toLocalDate());
                    list.add(u);
                }
                return list;
            }
        }
    }

    public User findUser(int id) throws SQLException {
        String sql = """
            SELECT id, username, fullname, email, role, division_id, status, job_title, join_date, contract_end, salary
            FROM Users WHERE id=?
        """;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                u.setFullname(rs.getString("fullname"));
                u.setEmail(rs.getString("email"));
                u.setRole(rs.getString("role"));
                u.setDivisionId((Integer) rs.getObject("division_id"));
                u.setStatus(rs.getString("status"));
                u.setJobTitle(rs.getString("job_title"));
                Date jd = rs.getDate("join_date"); if (jd!=null) u.setJoinDate(jd.toLocalDate());
                Date ce = rs.getDate("contract_end"); if (ce!=null) u.setContractEnd(ce.toLocalDate());
                u.setSalary(rs.getBigDecimal("salary"));
                return u;
            }
        }
    }

    public void updateUser(User u) throws SQLException {
        String sql = """
            UPDATE Users SET fullname=?, email=?, role=?, division_id=?,
                status=?, job_title=?, join_date=?, contract_end=?, salary=?
            WHERE id=?
        """;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getFullname());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getRole());
            if (u.getDivisionId() == null) ps.setNull(4, Types.INTEGER); else ps.setInt(4, u.getDivisionId());
            ps.setString(5, u.getStatus());
            ps.setString(6, u.getJobTitle());
            if (u.getJoinDate()==null) ps.setNull(7, Types.DATE); else ps.setDate(7, Date.valueOf(u.getJoinDate()));
            if (u.getContractEnd()==null) ps.setNull(8, Types.DATE); else ps.setDate(8, Date.valueOf(u.getContractEnd()));
            ps.setBigDecimal(9, u.getSalary());
            ps.setInt(10, u.getId());
            ps.executeUpdate();
        }
    }

    /* DTO nhỏ cho bảng "Nghỉ hôm nay" */
    public static class OnLeaveRow {
        public int userId;
        public String fullname;
        public String divisionName;
        public LocalDate from;
        public LocalDate to;
    }
}
