package com.acme.leavemgmt.dao;

import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.DBConnection;
import com.acme.leavemgmt.util.Passwords;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

/**
 * UserDAO – làm việc với bảng Users (SQL Server).
 * Chỉnh T_USERS nếu schema khác dbo.
 */
public class UserDAO {

    private static final String T_USERS = "[dbo].[Users]";

    private final DataSource ds; // có thể null -> fallback DBConnection

    // ==========================
    // Constructors
    // ==========================
    public UserDAO(DataSource ds) { this.ds = ds; }
    public UserDAO() { this(null); }

    // ==========================
    // Authentication / Login
    // ==========================

    /**
     * Đăng nhập:
     *  - Lấy user theo username & status=1
     *  - Nếu password dạng PBKDF2 -> verify
     *  - Nếu legacy (plain) -> so sánh trực tiếp; đúng thì migrate sang PBKDF2
     */
    public User findByUsernameAndPassword(String username, String passwordPlain) throws SQLException {
        final String sql = """
            SELECT u.id, u.username, u.[password], u.full_name, u.role, u.department, u.status,
                   u.email, u.phone, u.address, u.birthday, u.bio, u.avatar_url,
                   u.created_at, u.updated_at,
                   u.department_id, u.role_id, u.division_id, u.manager_id
            FROM """ + T_USERS + """
            u WHERE u.username=? AND u.status=1
        """;

        try (Connection con = getConn();
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

    public User findByEmail(String email) throws SQLException {
        final String sql = """
            SELECT u.id, u.username, u.[password], u.full_name, u.role, u.department, u.status,
                   u.email, u.phone, u.address, u.birthday, u.bio, u.avatar_url,
                   u.created_at, u.updated_at,
                   u.department_id, u.role_id, u.division_id, u.manager_id
            FROM """ + T_USERS + """
            u WHERE LOWER(u.email)=LOWER(?)
        """;
        try (Connection con = getConn();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public boolean updatePasswordHashed(int userId, String hashedPassword) throws SQLException {
        final String sql = "UPDATE " + T_USERS + " SET [password]=?, updated_at=SYSUTCDATETIME() WHERE id=?";
        try (Connection con = getConn();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setInt(2, userId);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean updatePasswordIfMatches(int userId, String currentPlain, String newPlain) throws SQLException {
        final String sqlGet = "SELECT [password] FROM " + T_USERS + " WHERE id=?";
        try (Connection con = getConn();
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

    public boolean adminSetPasswordPlain(int id, String newPasswordPlain) throws SQLException {
        final String sql = "UPDATE " + T_USERS + " SET [password]=?, updated_at=SYSDATETIME() WHERE id=?";
        try (Connection con = getConn();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, Passwords.hash(newPasswordPlain));
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updatePassword(int id, String hashed) {
        final String sql = "UPDATE " + T_USERS + " SET [password] = ?, updated_at = SYSDATETIME() WHERE id = ?";
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, hashed);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("updatePassword failed", e);
        }
    }

    // ==========================
    // Queries / Profile
    // ==========================

    /** Lấy user + tên phòng ban/khối/role/manager (JOIN). */
    /** Lấy user + label phòng ban/khối/role/manager (JOIN). */
public User findById(int id) throws SQLException {
final String sql = """
    SELECT 
        u.id, u.username, u.full_name, u.role, u.department, u.status,
        u.email, u.phone, u.address, u.birthday, u.bio, u.avatar_url,
        u.created_at, u.updated_at,
        u.department_id, u.role_id, u.division_id, u.manager_id,
        d.name  AS department_name, d.code AS department_code,
        dv.name AS division_name,   dv.code AS division_code,
        r.name  AS role_name,       r.code AS role_code,
        m.full_name AS manager_name
    FROM dbo.Users u
    LEFT JOIN dbo.Departments d ON u.department = d.code   -- dùng name/code, cùng NVARCHAR
    LEFT JOIN dbo.Roles       r ON u.role       = r.code   -- dùng name/code, cùng NVARCHAR
    LEFT JOIN dbo.Divisions  dv ON u.division_id = dv.id   -- INT ↔ INT
    LEFT JOIN dbo.Users       m ON u.manager_id  = m.id    -- INT ↔ INT
    WHERE u.id = ?
""";



    try (Connection con = getConn();
         PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, id);
        try (ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) return null;
            User u = mapRow(rs);
            // gán các label nếu model có setter tương ứng (reflection an toàn)
            safeSetStrReflect(u, rs, "department_name", "setDepartmentName");
            safeSetStrReflect(u, rs, "division_name",   "setDivisionName");
            safeSetStrReflect(u, rs, "role_name",       "setRoleName");
            safeSetStrReflect(u, rs, "manager_name",    "setManagerName");
            return u;
        }
    }
}







private static void safeSetIntReflect(Object bean, ResultSet rs, String col, String setterName) {
    try {
        int v = rs.getInt(col);
        if (rs.wasNull()) return;
        try { bean.getClass().getMethod(setterName, int.class).invoke(bean, v); }
        catch (NoSuchMethodException ignore) {}
    } catch (Exception ignore) {}
}
private static void safeSetStrReflect(Object bean, ResultSet rs, String col, String setterName) {
    try {
        String v = rs.getString(col);
        if (v == null) return;
        try { bean.getClass().getMethod(setterName, String.class).invoke(bean, v); }
        catch (NoSuchMethodException ignore) {}
    } catch (Exception ignore) {}
}


    /** Cập nhật nhanh vài trường cơ bản (giữ tương thích). */
    public boolean updateBasic(User u) throws SQLException {
        final String sql = """
            UPDATE """ + T_USERS + """
            SET full_name=?, email=?, phone=?, department=?, role=?, updated_at=SYSDATETIME()
            WHERE id=?
        """;
        try (Connection con = getConn();
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

    /** Ghi đè trực tiếp (legacy text). */
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
        try (Connection con = getConn();
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

    /** COALESCE: tham số null -> giữ nguyên. Khớp UI hiện tại. */
    public boolean updateProfile(
            int id,
            String fullName,          // null = giữ nguyên
            String email,
            String phone,
            String address,
            LocalDate birthday,
            String bio,
            String avatarUrl,
            Long divisionId,
            Long departmentId,
            Long managerId,
            Long roleIdToSave
    ) {
        final String sql = """
            UPDATE """ + T_USERS + """
            SET
                full_name     = COALESCE(?, full_name),
                email         = COALESCE(?, email),
                phone         = COALESCE(?, phone),
                address       = COALESCE(?, address),
                birthday      = COALESCE(?, birthday),
                bio           = COALESCE(?, bio),
                avatar_url    = COALESCE(?, avatar_url),
                division_id   = COALESCE(?, division_id),
                department_id = COALESCE(?, department_id),
                manager_id    = COALESCE(?, manager_id),
                role_id       = COALESCE(?, role_id),
                updated_at    = SYSDATETIME()
            WHERE id = ?
        """;
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            int i = 1;
            setNullable(ps, i++, fullName);
            setNullable(ps, i++, email);
            setNullable(ps, i++, phone);
            setNullable(ps, i++, address);

            // birthday
            if (birthday == null) ps.setNull(i++, Types.DATE);
            else ps.setDate(i++, Date.valueOf(birthday));

            setNullable(ps, i++, bio);
            setNullable(ps, i++, avatarUrl);

            // FK INT (nullable)
            setNullableInt(ps, i++, divisionId);
            setNullableInt(ps, i++, departmentId);
            setNullableInt(ps, i++, managerId);
            setNullableInt(ps, i++, roleIdToSave);

            ps.setInt(i, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("updateProfile (COALESCE) failed", e);
        }
    }

    // ==========================
    // OAuth-lite helper
    // ==========================

    public User createFromOAuth(User u) throws SQLException {
        if (u == null || u.getEmail() == null) throw new IllegalArgumentException("email required");

        try (Connection con = getConn()) {
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
                        ins.setNull(i++, Types.NVARCHAR);
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
    // Lists / helpers
    // ==========================

    /** Danh sách user có vai trò quản lý (fill select Manager). */
    public List<User> listManagers() throws SQLException {
        final String sql = """
            SELECT id, username,
                   COALESCE(full_name, username) AS full_name,
                   role, role_id, division_id
            FROM """ + T_USERS + """
            WHERE (UPPER(role) IN ('ADMIN','DIV_LEADER','TEAM_LEAD','QA_LEAD','LEADER','MANAGER')
                   OR role_id IN (1,2,3,4))
              AND status = 1
            ORDER BY full_name, username
        """;

        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<User> out = new ArrayList<>();
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                u.setFullName(rs.getString("full_name"));
                u.setRole(rs.getString("role"));
                safeGetInt(rs, "role_id", u::setRoleId);
                safeGetInt(rs, "division_id", u::setDivisionId);
                out.add(u);
            }
            return out;
        }
    }

    // ==========================
    // Mapping & low-level helpers
    // ==========================

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setFullName(rs.getString("full_name"));
        u.setRole(rs.getString("role"));
        u.setDepartment(rs.getString("department"));
        safeGetInt(rs, "status", u::setStatus);

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
        safeGetInt(rs, "division_id",   u::setDivisionId);
        safeGetInt(rs, "manager_id",    u::setManagerId);

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

    /** Cho tham số Long nhưng map về SQL INT (FK). */
    private static void setNullableInt(PreparedStatement ps, int idx, Long v) throws SQLException {
        if (v == null) ps.setNull(idx, Types.INTEGER);
        else ps.setInt(idx, v.intValue());
    }
    private static void setNullableInt(PreparedStatement ps, int idx, Integer v) throws SQLException {
        if (v == null) ps.setNull(idx, Types.INTEGER);
        else ps.setInt(idx, v);
    }

    private interface IntSetter { void set(int x); }
    private static void safeGetInt(ResultSet rs, String col, IntSetter setter) {
        try {
            int v = rs.getInt(col);
            if (!rs.wasNull()) setter.set(v);
        } catch (SQLException ignore) { /* cột không tồn tại -> bỏ qua */ }
    }
    private static void safeSetStr(java.util.function.Consumer<String> setter, ResultSet rs, String col) {
        try {
            String v = rs.getString(col);
            if (v != null) setter.accept(v);
        } catch (SQLException ignore) { }
    }

    /** Lấy Connection: ưu tiên DataSource; fallback DBConnection. */
    private Connection getConn() {
        try {
            if (this.ds != null) return this.ds.getConnection();
            return DBConnection.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Cannot obtain DB connection", e);
        }
    }
}
