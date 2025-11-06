package com.acme.leavemgmt.dao;

import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.DBConnection;
import com.acme.leavemgmt.util.Passwords;

import java.sql.*;
import java.time.LocalDate;

public class UserDAO {

    private static final String T_USERS = "[dbo].[Users]"; // đổi nếu schema khác

    // ==========================
    // Authentication / Login
    // ==========================

    /**
     * Đăng nhập an toàn:
     *  - Lấy user theo username & status=1
     *  - Nếu password trong DB ở dạng PBKDF2 -> verify bằng Passwords.verify
     *  - Nếu legacy (plain) -> so sánh trực tiếp; đúng thì migrate sang PBKDF2
     */
    public User findByUsernameAndPassword(String username, String passwordPlain) throws SQLException {
        final String sql = """
            SELECT id, username, [password], full_name, role, department, status,
                   email, phone, address, birthday, bio, avatar_url,
                   created_at, updated_at, department_id, role_id
            FROM """ + T_USERS + """
            WHERE username=? AND status=1
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                String stored = rs.getString("password");
                boolean ok;

                if (stored != null && stored.startsWith("PBKDF2$")) {
                    ok = Passwords.verify(passwordPlain, stored);
                } else {
                    ok = passwordPlain != null && passwordPlain.equals(stored);
                    if (ok) {
                        // migrate ngay sang PBKDF2
                        String newHash = Passwords.hash(passwordPlain);
                        try (PreparedStatement up = con.prepareStatement(
                                "UPDATE " + T_USERS + " SET [password]=?, updated_at=SYSDATETIME() WHERE id=?")) {
                            up.setString(1, newHash);
                            up.setInt(2, rs.getInt("id"));
                            up.executeUpdate();
                        }
                    }
                }

                if (!ok) return null;
                return mapRow(rs);
            }
        }
    }

    // ==========================
    // Forgot password / helpers
    // ==========================

    /** Tìm theo email (case-insensitive). */
    public User findByEmail(String email) throws SQLException {
        final String sql = """
            SELECT id, username, [password], full_name, role, department, status,
                   email, phone, address, birthday, bio, avatar_url,
                   created_at, updated_at, department_id, role_id
            FROM """ + T_USERS + """
            WHERE LOWER(email) = LOWER(?)
        """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /** Cập nhật mật khẩu đã hash sẵn (phục vụ flow forgot). */
    public boolean updatePasswordHashed(int userId, String hashedPassword) throws SQLException {
        final String sql = "UPDATE " + T_USERS + " SET [password]=?, updated_at=SYSUTCDATETIME() WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setInt(2, userId);
            return ps.executeUpdate() == 1;
        }
    }

    /** Đổi mật khẩu khi biết mật khẩu hiện tại (dùng cho ChangePasswordServlet). */
    public boolean updatePasswordIfMatches(int userId, String currentPlain, String newPlain) throws SQLException {
        final String sqlGet = "SELECT [password] FROM " + T_USERS + " WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sqlGet)) {

            ps.setInt(1, userId);
            String stored;
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                stored = rs.getString(1);
            }

            boolean ok;
            if (stored != null && stored.startsWith("PBKDF2$")) {
                ok = Passwords.verify(currentPlain, stored);
            } else {
                ok = currentPlain != null && currentPlain.equals(stored);
            }
            if (!ok) return false;

            String newHash = Passwords.hash(newPlain);
            try (PreparedStatement up = con.prepareStatement(
                    "UPDATE " + T_USERS + " SET [password]=?, updated_at=SYSDATETIME() WHERE id=?")) {
                up.setString(1, newHash);
                up.setInt(2, userId);
                return up.executeUpdate() > 0;
            }
        }
    }

    /** Admin đặt mật khẩu mới cho user – luôn lưu PBKDF2 (nhận plain). */
    public boolean adminSetPasswordPlain(int id, String newPasswordPlain) throws SQLException {
        final String sql = "UPDATE " + T_USERS + " SET [password]=?, updated_at=SYSDATETIME() WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, Passwords.hash(newPasswordPlain));
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ==========================
    // Queries / Profile
    // ==========================

    public User findById(int id) throws SQLException {
        final String sql = """
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

    /** Cập nhật nhanh các trường cơ bản (giữ tương thích). */
    public boolean updateBasic(User u) throws SQLException {
        final String sql = """
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

    /** Cập nhật đầy đủ hồ sơ (khớp UI/Servlet). */
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
        final String sql = """
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
            setNullable(ps, 7,  birthday);
            setNullable(ps, 8,  bio);
            setNullable(ps, 9,  avatarUrl);
            ps.setInt(10, id);

            return ps.executeUpdate() > 0;
        }
    }

    // ==========================
    // OAuth-lite helper
    // ==========================

    /**
     * Tạo user từ OAuth (Google...) mà KHÔNG phụ thuộc cột auth_provider.
     * - Nếu email tồn tại: cập nhật full_name, avatar_url (không đụng password)
     * - Nếu chưa: insert user mới (status=1, role mặc định 'STAFF' nếu null)
     */
    public User createFromOAuth(User u) throws SQLException {
        if (u == null || u.getEmail() == null) throw new IllegalArgumentException("email required");

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                Integer existedId = null;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT id FROM " + T_USERS + " WHERE email=?")) {
                    ps.setString(1, u.getEmail());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) existedId = rs.getInt(1);
                    }
                }

                if (existedId != null) {
                    try (PreparedStatement up = con.prepareStatement("""
                        UPDATE """ + T_USERS + """
                        SET full_name = COALESCE(?, full_name),
                            avatar_url = COALESCE(?, avatar_url),
                            updated_at = SYSDATETIME()
                        WHERE id=?
                    """)) {
                        setNullable(up, 1, u.getFullName());
                        setNullable(up, 2, u.getAvatarUrl());
                        up.setInt(3, existedId);
                        up.executeUpdate();
                    }
                    con.commit();
                    return findById(existedId);
                } else {
                    String username = u.getUsername();
                    if (username == null || username.isBlank()) {
                        String em = u.getEmail();
                        int at = em.indexOf('@');
                        username = (at > 0 ? em.substring(0, at) : em);
                    }
                    String role = (u.getRole() == null || u.getRole().isBlank()) ? "STAFF" : u.getRole();

                    String sqlIns = """
                        INSERT INTO """ + T_USERS + """
                        (username, [password], full_name, role, department, status,
                         email, phone, address, birthday, bio, avatar_url,
                         created_at, updated_at, department_id, role_id)
                        VALUES (?,?,?,?,?,1, ?,?,?,?, ?,?, SYSDATETIME(), SYSDATETIME(), ?,?)
                    """;
                    try (PreparedStatement ins = con.prepareStatement(sqlIns, Statement.RETURN_GENERATED_KEYS)) {
                        int i = 1;
                        ins.setString(i++, username);
                        ins.setNull(i++, Types.NVARCHAR); // không thiết lập password khi OAuth
                        setNullable(ins, i++, u.getFullName());
                        setNullable(ins, i++, role);
                        setNullable(ins, i++, u.getDepartment());
                        setNullable(ins, i++, u.getEmail());
                        setNullable(ins, i++, u.getPhone());
                        setNullable(ins, i++, u.getAddress());
                        setNullable(ins, i++, u.getBirthday());
                        setNullable(ins, i++, u.getBio());
                        setNullable(ins, i++, u.getAvatarUrl());
                        setNullableInt(ins, i++, u.getDepartmentId());
                        setNullableInt(ins, i++, u.getRoleId());

                        ins.executeUpdate();
                        try (ResultSet gk = ins.getGeneratedKeys()) {
                            if (gk.next()) {
                                int newId = gk.getInt(1);
                                con.commit();
                                return findById(newId);
                            }
                        }
                    }
                    con.commit();
                    return findByEmail(u.getEmail());
                }
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    // ==========================
    // Mapping & helpers
    // ==========================

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        // KHÔNG set password vào model
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

    private static void setNullableInt(PreparedStatement ps, int idx, Integer v) throws SQLException {
        if (v == null) ps.setNull(idx, Types.INTEGER);
        else ps.setInt(idx, v);
    }

    public boolean updatePassword(int id, String hashed) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    /** Đọc INT an toàn (kể cả cột không tồn tại hoặc NULL) */
    private interface IntSetter { void set(int x); }
    private static void safeGetInt(ResultSet rs, String col, IntSetter setter) {
        try {
            int v = rs.getInt(col);
            if (!rs.wasNull()) setter.set(v);
        } catch (SQLException ignore) { /* cột không tồn tại */ }
    }
}
