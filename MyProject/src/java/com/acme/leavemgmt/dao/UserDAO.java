package com.acme.leavemgmt.dao;

import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;

public class UserDAO {

    private static final String T_USERS = "[dbo].[Users]"; // đổi nếu schema khác

    /** Đăng nhập: đúng user/pass và còn active (status=1) */
    public User findByUsernameAndPassword(String username, String password) throws SQLException {
        String sql = """
            SELECT id, username, full_name, role, department, status,
                   email, phone, address, birthday, bio, avatar_url,
                   created_at, updated_at, department_id, role_id
            FROM """ + T_USERS + """
            WHERE username=? AND password=? AND status=1
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /** Tìm theo ID */
    public User findById(int id) throws SQLException {
        String sql = """
            SELECT id, username, full_name, role, department, status,
                   email, phone, address, birthday, bio, avatar_url,
                   created_at, updated_at, department_id, role_id
            FROM """ + T_USERS + " WHERE id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /** (Giữ để tương thích) Cập nhật vài trường cơ bản */
    public boolean updateBasic(User u) throws SQLException {
        String sql = """
            UPDATE """ + T_USERS + """
            SET full_name=?, email=?, phone=?, department=?, role=?, updated_at=SYSDATETIME()
            WHERE id=?
        """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            setNullable(ps, 1, u.getFullName());
            setNullable(ps, 2, u.getEmail());
            setNullable(ps, 3, u.getPhone());
            setNullable(ps, 4, u.getDepartment());
            setNullable(ps, 5, u.getRole());
            ps.setInt(6, u.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /** Cập nhật đầy đủ hồ sơ (khớp UI/Servlet) */
    public boolean updateProfile(int id,
                                 String fullName,
                                 String department,
                                 String role,
                                 String email,
                                 String phone,
                                 String address,
                                 LocalDate birthday,
                                 String bio,
                                 String avatarUrl) throws SQLException {
        String sql = """
            UPDATE """ + T_USERS + """
            SET full_name=?,
                department=?,
                role=?,
                email=?,
                phone=?,
                address=?,
                birthday=?,
                bio=?,
                avatar_url=?,
                updated_at=SYSDATETIME()
            WHERE id=?
        """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            setNullable(ps, 1,  fullName);
            setNullable(ps, 2,  department);
            setNullable(ps, 3,  role);
            setNullable(ps, 4,  email);
            setNullable(ps, 5,  phone);
            setNullable(ps, 6,  address);
            setNullable(ps, 7,  birthday);   // LocalDate -> DATE
            setNullable(ps, 8,  bio);
            setNullable(ps, 9,  avatarUrl);
            ps.setInt(10, id);

            return ps.executeUpdate() > 0;
        }
    }

    /** (Tùy chọn) Đổi mật khẩu – nếu bạn cần */
    public boolean updatePassword(int id, String newPasswordPlain) throws SQLException {
        String sql = "UPDATE " + T_USERS + " SET password=?, updated_at=SYSDATETIME() WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newPasswordPlain); // thực tế nên lưu hash
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ===== Helper mapping & null-setters =====

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setFullName(rs.getString("full_name"));
        u.setRole(rs.getString("role"));
        u.setDepartment(rs.getString("department"));
        u.setStatus(rs.getInt("status"));

        u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone"));
        u.setAddress(rs.getString("address"));
        Date bd = rs.getDate("birthday");
        if (bd != null) u.setBirthday(((java.sql.Date) bd).toLocalDate());
        u.setBio(rs.getString("bio"));
        u.setAvatarUrl(rs.getString("avatar_url"));

        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) u.setCreatedAt(new java.util.Date(ct.getTime()));
        // updated_at nếu cần thì thêm field trong model

        // Optional (có thể NULL/không tồn tại)
        safeGetInt(rs, "department_id", u::setDepartmentId);
        safeGetInt(rs, "role_id",       u::setRoleId);

        return u;
    }

    private static void setNullable(PreparedStatement ps, int idx, String v) throws SQLException {
        if (v == null || v.isBlank()) ps.setNull(idx, Types.NVARCHAR);
        else ps.setString(idx, v);
    }

    private static void setNullable(PreparedStatement ps, int idx, LocalDate v) throws SQLException {
        if (v == null) ps.setNull(idx, Types.DATE);
        else ps.setDate(idx, java.sql.Date.valueOf(v));
    }

    /** Đọc cột INT an toàn (kể cả không tồn tại hoặc NULL) */
    private interface IntSetter { void set(int x); }
    private static void safeGetInt(ResultSet rs, String col, IntSetter setter) {
        try {
            int v = rs.getInt(col);
            if (!rs.wasNull()) setter.set(v);
        } catch (SQLException ignore) { /* cột không tồn tại */ }
    }

public User findByEmail(String email) throws SQLException { /* SELECT * FROM Users WHERE email=? */
        return null;
    }

public void createFromOAuth(User u) throws SQLException {
  // INSERT INTO Users(email, full_name, avatar_url, status, role, created_at, auth_provider)
}



}
