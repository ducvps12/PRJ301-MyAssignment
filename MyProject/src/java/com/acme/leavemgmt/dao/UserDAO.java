package com.acme.leavemgmt.dao;

import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.DBConnection;

import java.sql.*;

public class UserDAO {

    public User findById(int id) throws SQLException {
        String sql = "SELECT * FROM Users WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = new User();
                    u.setId(rs.getInt("id"));
                    u.setUsername(rs.getString("username"));
                    u.setFullName(rs.getString("full_name"));
                    u.setEmail(rs.getString("email"));
                    u.setPhone(rs.getString("phone"));
                    u.setDepartmentId(rs.getInt("department_id"));
                    u.setRoleId(rs.getInt("role_id"));
                    return u;
                }
            }
        }
        return null;
    }

    public void updateBasic(User u) throws SQLException {
        String sql = "UPDATE Users SET full_name=?, email=?, phone=?, department_id=?, role_id=? WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getFullName());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getPhone());
            ps.setInt(4, u.getDepartmentId());
            ps.setInt(5, u.getRoleId());
            ps.setInt(6, u.getId());
            ps.executeUpdate();
        }
    }
}
