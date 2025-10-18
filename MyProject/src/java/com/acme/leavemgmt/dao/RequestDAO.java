package com.acme.leavemgmt.dao;

import com.acme.leavemgmt.model.Request;
import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.sql.Date;


public class RequestDAO {

    // =====================================================
    // =============== USER FUNCTIONS ======================
    // =====================================================

    /** Đăng nhập: tương thích cả schema mới (role_id/department_id) và schema cũ (role/department) */
    public User findByUsernameAndPassword(String username, String password) throws SQLException {
        final String sqlNew = """
            SELECT u.user_id        AS uid,
                   u.username,
                   u.full_name,
                   ISNULL(r.role_code, 'EMPLOYEE') AS role_code,
                   ISNULL(d.dept_name,  N'')       AS dept_name
            FROM [dbo].[Users] u
            LEFT JOIN [dbo].[Roles]       r ON r.role_id = u.role_id
            LEFT JOIN [dbo].[Departments] d ON d.department_id = u.department_id
            WHERE u.username = ? AND u.[password] = ? AND (u.is_active = 1 OR u.is_active IS NULL)
        """;

        final String sqlOld = """
            SELECT id               AS uid,
                   username,
                   full_name,
                   role             AS role_code,
                   department       AS dept_name
            FROM [dbo].[Users]
            WHERE username = ? AND [password] = ?
        """;

        try (Connection cn = DBConnection.getConnection()) {
            try (PreparedStatement ps = cn.prepareStatement(sqlNew)) {
                ps.setString(1, username);
                ps.setString(2, password);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new User(
                                rs.getInt("uid"),
                                rs.getString("username"),
                                rs.getString("full_name"),
                                rs.getString("role_code"),
                                rs.getString("dept_name")
                        );
                    }
                }
            } catch (SQLException ignore) {
                // fallback sang schema cũ
            }

            try (PreparedStatement ps = cn.prepareStatement(sqlOld)) {
                ps.setString(1, username);
                ps.setString(2, password);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new User(
                                rs.getInt("uid"),
                                rs.getString("username"),
                                rs.getString("full_name"),
                                rs.getString("role_code"),
                                rs.getString("dept_name")
                        );
                    }
                }
            }
        }
        return null;
    }

    /** Lấy danh sách user theo tên phòng ban (dept_name hoặc Users.department) */
    public List<User> listUsersByDepartment(String departmentName) throws SQLException {
        final String sqlNew = """
            SELECT u.user_id AS user_id,
                   u.username,
                   u.full_name,
                   ISNULL(r.role_code,'EMPLOYEE') AS role_code,
                   d.dept_name
            FROM [dbo].[Users] u
            JOIN [dbo].[Departments] d ON d.department_id = u.department_id
            LEFT JOIN [dbo].[Roles] r   ON r.role_id = u.role_id
            WHERE d.dept_name = ?
        """;

        final String sqlOld = """
            SELECT u.id       AS user_id,
                   u.username,
                   u.full_name,
                   u.role      AS role_code,
                   u.department AS dept_name
            FROM [dbo].[Users] u
            WHERE u.department = ?
        """;

        List<User> list = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection()) {
            try (PreparedStatement ps = cn.prepareStatement(sqlNew)) {
                ps.setString(1, departmentName);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(new User(
                                rs.getInt("user_id"),
                                rs.getString("username"),
                                rs.getString("full_name"),
                                rs.getString("role_code"),
                                rs.getString("dept_name")
                        ));
                    }
                }
            } catch (SQLException e) {
                try (PreparedStatement ps = cn.prepareStatement(sqlOld)) {
                    ps.setString(1, departmentName);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            list.add(new User(
                                    rs.getInt("user_id"),
                                    rs.getString("username"),
                                    rs.getString("full_name"),
                                    rs.getString("role_code"),
                                    rs.getString("dept_name")
                            ));
                        }
                    }
                }
            }
        }
        return list;
    }

    // =====================================================
    // =============== REQUEST FUNCTIONS ===================
    // =====================================================

    /** Tạo đơn – hỗ trợ V2 (employee_id) / V1 (user_id) / V0 (created_by) */
    /** Tạo đơn – hỗ trợ V2 (employee_id) / V1 (user_id) / V0 (created_by) */
public void createRequest(Request r) throws SQLException {
    final String sqlV2 = """
        INSERT INTO [dbo].[Requests]
            (employee_id, approver_id, from_date, to_date, reason, status)
        VALUES
            (           ?,          ?,         ?,       ?,      ?, N'Inprogress')
    """;
    final String sqlV1 = """
        INSERT INTO [dbo].[Requests]
            (user_id, type, status, reason, start_date, end_date, created_at)
        VALUES
            (      ?,    ?,      ?,      ?,          ?,        ?, SYSDATETIME())
    """;
    final String sqlV0 = """
        INSERT INTO [dbo].[Requests]
            (created_by, processed_by, start_date, end_date, reason, status)
        VALUES
            (         ?,           ?,         ?,       ?,      ?, N'Inprogress')
    """;

    try (Connection cn = DBConnection.getConnection()) {
        // Thử schema V2 (employee_id/approver_id/from_date/to_date)
        try (PreparedStatement ps = cn.prepareStatement(sqlV2)) {
            ps.setInt(1, r.getCreatedBy());                           // employee_id
            if (r.getProcessedBy() == null) ps.setNull(2, Types.INTEGER);
            else ps.setInt(2, r.getProcessedBy());                    // approver_id
            ps.setDate(3, Date.valueOf(r.getStartDate()));            // from_date
            ps.setDate(4, Date.valueOf(r.getEndDate()));              // to_date
            ps.setString(5, r.getReason());                           // reason
            ps.executeUpdate();
            return;
        } catch (SQLException ignoreV2) {
            // Thử schema V1 (user_id/type/status/start_date/end_date)
            try (PreparedStatement ps = cn.prepareStatement(sqlV1)) {
                // Vì Request model không có type/status -> dùng default
                final String defaultType = "ANNUAL";
                final String defaultStatus = "PENDING";
                ps.setInt(1, r.getCreatedBy());                       // user_id
                ps.setString(2, defaultType);                         // type
                ps.setString(3, defaultStatus);                       // status
                ps.setString(4, r.getReason());                       // reason
                ps.setDate(5, Date.valueOf(r.getStartDate()));        // start_date
                ps.setDate(6, Date.valueOf(r.getEndDate()));          // end_date
                ps.executeUpdate();
                return;
            } catch (SQLException ignoreV1) {
                // Fallback schema V0 (created_by/processed_by/...)
                try (PreparedStatement ps = cn.prepareStatement(sqlV0)) {
                    ps.setInt(1, r.getCreatedBy());                   // created_by
                    if (r.getProcessedBy() == null) ps.setNull(2, Types.INTEGER);
                    else ps.setInt(2, r.getProcessedBy());            // processed_by
                    ps.setDate(3, Date.valueOf(r.getStartDate()));    // start_date
                    ps.setDate(4, Date.valueOf(r.getEndDate()));      // end_date
                    ps.setString(5, r.getReason());                   // reason
                    ps.executeUpdate();
                }
            }
        }
    }
}

    /** Danh sách đơn của tôi – ưu tiên V2, rồi V1, rồi V0. Alias created_by cho đồng nhất. */
    public List<Request> listMyRequests(int userId) throws SQLException {
        final String sqlV2 = """
            SELECT
                lr.request_id   AS id,
                lr.reason,
                lr.from_date    AS start_date,
                lr.to_date      AS end_date,
                lr.status,
                lr.manager_note,
                u_emp.full_name AS created_name,
                lr.employee_id  AS created_by,
                u_app.full_name AS processed_name,
                lr.approver_id  AS processed_by
            FROM [dbo].[Requests] lr
            JOIN [dbo].[Users] u_emp  ON u_emp.user_id = lr.employee_id
            LEFT JOIN [dbo].[Users] u_app ON u_app.user_id = lr.approver_id
            WHERE lr.employee_id = ?
            ORDER BY lr.request_id DESC
        """;
        final String sqlV1 = """
            SELECT
                r.id            AS id,
                r.reason,
                r.start_date    AS start_date,
                r.end_date      AS end_date,
                r.status,
                CAST(NULL AS NVARCHAR(400)) AS manager_note,
                u.full_name     AS created_name,
                r.user_id       AS created_by,
                CAST(NULL AS NVARCHAR(200)) AS processed_name,
                CAST(NULL AS INT)           AS processed_by
            FROM [dbo].[Requests] r
            JOIN [dbo].[Users] u ON u.id = r.user_id
            WHERE r.user_id = ?
            ORDER BY r.id DESC
        """;
        final String sqlV0 = """
            SELECT
                r.id           AS id,
                r.reason,
                r.start_date   AS start_date,
                r.end_date     AS end_date,
                r.status,
                r.manager_note,
                u_emp.full_name AS created_name,
                r.created_by    AS created_by,
                u_app.full_name AS processed_name,
                r.processed_by  AS processed_by
            FROM [dbo].[Requests] r
            JOIN [dbo].[Users] u_emp  ON u_emp.id = r.created_by
            LEFT JOIN [dbo].[Users] u_app ON u_app.id = r.processed_by
            WHERE r.created_by = ?
            ORDER BY r.id DESC
        """;

        List<Request> list = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection()) {
            try (PreparedStatement ps = cn.prepareStatement(sqlV2)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapRow(rs));
                    if (!list.isEmpty()) return list;
                }
            } catch (SQLException ignoreV2) {}

            try (PreparedStatement ps = cn.prepareStatement(sqlV1)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapRow(rs));
                    if (!list.isEmpty()) return list;
                }
            } catch (SQLException ignoreV1) {}

            try (PreparedStatement ps = cn.prepareStatement(sqlV0)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /** Danh sách đơn theo phòng ban (V2 → V1 → V0) */
    public List<Request> listSubordinateRequests(String departmentName) throws SQLException {
        final String sqlV2 = """
            SELECT
                lr.request_id   AS id,
                lr.reason,
                lr.from_date    AS start_date,
                lr.to_date      AS end_date,
                lr.status,
                lr.manager_note,
                u_emp.full_name AS created_name,
                lr.employee_id  AS created_by,
                u_app.full_name AS processed_name,
                lr.approver_id  AS processed_by
            FROM [dbo].[Requests] lr
            JOIN [dbo].[Users] u_emp       ON u_emp.user_id = lr.employee_id
            JOIN [dbo].[Departments] d     ON d.department_id = u_emp.department_id
            LEFT JOIN [dbo].[Users] u_app  ON u_app.user_id = lr.approver_id
            WHERE d.dept_name = ?
            ORDER BY lr.request_id DESC
        """;
        final String sqlV1 = """
            SELECT
                r.id            AS id,
                r.reason,
                r.start_date    AS start_date,
                r.end_date      AS end_date,
                r.status,
                CAST(NULL AS NVARCHAR(400)) AS manager_note,
                u.full_name     AS created_name,
                r.user_id       AS created_by,
                CAST(NULL AS NVARCHAR(200)) AS processed_name,
                CAST(NULL AS INT)           AS processed_by
            FROM [dbo].[Requests] r
            JOIN [dbo].[Users] u ON u.id = r.user_id
            WHERE u.department = ?
            ORDER BY r.id DESC
        """;
        final String sqlV0 = """
            SELECT
                r.id            AS id,
                r.reason,
                r.start_date    AS start_date,
                r.end_date      AS end_date,
                r.status,
                r.manager_note,
                u_emp.full_name AS created_name,
                r.created_by    AS created_by,
                u_app.full_name AS processed_name,
                r.processed_by  AS processed_by
            FROM [dbo].[Requests] r
            JOIN [dbo].[Users] u_emp      ON u_emp.id = r.created_by
            LEFT JOIN [dbo].[Users] u_app ON u_app.id = r.processed_by
            WHERE u_emp.department = ?
            ORDER BY r.id DESC
        """;

        List<Request> list = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection()) {
            try (PreparedStatement ps = cn.prepareStatement(sqlV2)) {
                ps.setString(1, departmentName);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapRow(rs));
                    if (!list.isEmpty()) return list;
                }
            } catch (SQLException ignoreV2) {}

            try (PreparedStatement ps = cn.prepareStatement(sqlV1)) {
                ps.setString(1, departmentName);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapRow(rs));
                    if (!list.isEmpty()) return list;
                }
            } catch (SQLException ignoreV1) {}

            try (PreparedStatement ps = cn.prepareStatement(sqlV0)) {
                ps.setString(1, departmentName);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /** Lấy chi tiết đơn theo id (V2 → V1 → V0) */
    public Request findById(int id) throws SQLException {
        final String sqlV2 = """
            SELECT
                lr.request_id   AS id,
                lr.reason,
                lr.from_date    AS start_date,
                lr.to_date      AS end_date,
                lr.status,
                lr.manager_note,
                u_emp.full_name AS created_name,
                lr.employee_id  AS created_by,
                u_app.full_name AS processed_name,
                lr.approver_id  AS processed_by
            FROM [dbo].[Requests] lr
            JOIN [dbo].[Users] u_emp      ON u_emp.user_id = lr.employee_id
            LEFT JOIN [dbo].[Users] u_app ON u_app.user_id = lr.approver_id
            WHERE lr.request_id = ?
        """;
        final String sqlV1 = """
            SELECT
                r.id            AS id,
                r.reason,
                r.start_date    AS start_date,
                r.end_date      AS end_date,
                r.status,
                CAST(NULL AS NVARCHAR(400)) AS manager_note,
                u.full_name     AS created_name,
                r.user_id       AS created_by,
                CAST(NULL AS NVARCHAR(200)) AS processed_name,
                CAST(NULL AS INT)           AS processed_by
            FROM [dbo].[Requests] r
            JOIN [dbo].[Users] u ON u.id = r.user_id
            WHERE r.id = ?
        """;
        final String sqlV0 = """
            SELECT
                r.id           AS id,
                r.reason,
                r.start_date   AS start_date,
                r.end_date     AS end_date,
                r.status,
                r.manager_note,
                u_emp.full_name AS created_name,
                r.created_by    AS created_by,
                u_app.full_name AS processed_name,
                r.processed_by  AS processed_by
            FROM [dbo].[Requests] r
            JOIN [dbo].[Users] u_emp      ON u_emp.id = r.created_by
            LEFT JOIN [dbo].[Users] u_app ON u_app.id = r.processed_by
            WHERE r.id = ?
        """;

        try (Connection cn = DBConnection.getConnection()) {
            try (PreparedStatement ps = cn.prepareStatement(sqlV2)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapRow(rs);
                }
            } catch (SQLException ignoreV2) {}

            try (PreparedStatement ps = cn.prepareStatement(sqlV1)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapRow(rs);
                }
            } catch (SQLException ignoreV1) {}

            try (PreparedStatement ps = cn.prepareStatement(sqlV0)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapRow(rs);
                }
            }
        }
        return null;
    }

    /** Duyệt/từ chối – nếu schema không có approver/manager_note thì chỉ update status */
    public void processRequest(int id, int managerId, String status, String note) throws SQLException {
        final String sqlV2 = """
            UPDATE [dbo].[Requests]
               SET status = ?, approver_id = ?, manager_note = ?, reviewed_at = SYSUTCDATETIME()
             WHERE request_id = ?
        """;
        final String sqlV1 = """
            UPDATE [dbo].[Requests]
               SET status = ?
             WHERE id = ?
        """;
        final String sqlV0 = """
            UPDATE [dbo].[Requests]
               SET status = ?, processed_by = ?, manager_note = ?, reviewed_at = GETUTCDATE()
             WHERE id = ?
        """;

        try (Connection cn = DBConnection.getConnection()) {
            try (PreparedStatement ps = cn.prepareStatement(sqlV2)) {
                ps.setString(1, status);
                ps.setInt(2, managerId);
                ps.setString(3, note);
                ps.setInt(4, id);
                ps.executeUpdate();
                return;
            } catch (SQLException ignoreV2) {
                try (PreparedStatement ps = cn.prepareStatement(sqlV1)) {
                    ps.setString(1, status);
                    ps.setInt(2, id);
                    ps.executeUpdate();
                    return;
                } catch (SQLException ignoreV1) {
                    try (PreparedStatement ps = cn.prepareStatement(sqlV0)) {
                        ps.setString(1, status);
                        ps.setInt(2, managerId);
                        ps.setString(3, note);
                        ps.setInt(4, id);
                        ps.executeUpdate();
                    }
                }
            }
        }
    }

    /** Map<userId, Set<LocalDate>> các ngày Approved trong phòng ban/khoảng ngày */
    public Map<Integer, Set<LocalDate>> getApprovedAbsences(String departmentName,
                                                            LocalDate from,
                                                            LocalDate to) throws SQLException {
        final String sqlV2 = """
            SELECT lr.from_date AS from_date, lr.to_date AS to_date, lr.employee_id AS uid
            FROM [dbo].[Requests] lr
            JOIN [dbo].[Users] u  ON u.user_id = lr.employee_id
            JOIN [dbo].[Departments] d ON d.department_id = u.department_id
            WHERE lr.status = N'Approved'
              AND d.dept_name = ?
              AND lr.to_date   >= ?
              AND lr.from_date <= ?
        """;
        final String sqlV1 = """
            SELECT r.start_date AS from_date, r.end_date AS to_date, r.user_id AS uid
            FROM [dbo].[Requests] r
            JOIN [dbo].[Users] u ON u.id = r.user_id
            WHERE r.status = N'Approved'
              AND u.department = ?
              AND r.end_date   >= ?
              AND r.start_date <= ?
        """;
        final String sqlV0 = """
            SELECT r.start_date AS from_date, r.end_date AS to_date, r.created_by AS uid
            FROM [dbo].[Requests] r
            JOIN [dbo].[Users] u ON u.id = r.created_by
            WHERE r.status = N'Approved'
              AND u.department = ?
              AND r.end_date   >= ?
              AND r.start_date <= ?
        """;

        Map<Integer, Set<LocalDate>> map = new HashMap<>();
        try (Connection cn = DBConnection.getConnection()) {
            try (PreparedStatement ps = cn.prepareStatement(sqlV2)) {
                ps.setString(1, departmentName);
                ps.setDate(2, Date.valueOf(from));
                ps.setDate(3, Date.valueOf(to));
                try (ResultSet rs = ps.executeQuery()) {
                    accumulateDays(map, rs, from, to);
                    if (!map.isEmpty()) return map;
                }
            } catch (SQLException ignoreV2) {}

            try (PreparedStatement ps = cn.prepareStatement(sqlV1)) {
                ps.setString(1, departmentName);
                ps.setDate(2, Date.valueOf(from));
                ps.setDate(3, Date.valueOf(to));
                try (ResultSet rs = ps.executeQuery()) {
                    accumulateDays(map, rs, from, to);
                    if (!map.isEmpty()) return map;
                }
            } catch (SQLException ignoreV1) {}

            try (PreparedStatement ps = cn.prepareStatement(sqlV0)) {
                ps.setString(1, departmentName);
                ps.setDate(2, Date.valueOf(from));
                ps.setDate(3, Date.valueOf(to));
                try (ResultSet rs = ps.executeQuery()) {
                    accumulateDays(map, rs, from, to);
                }
            }
        }
        return map;
    }

    private void accumulateDays(Map<Integer, Set<LocalDate>> map, ResultSet rs, LocalDate from, LocalDate to) throws SQLException {
        while (rs.next()) {
            LocalDate start = rs.getDate("from_date").toLocalDate();
            LocalDate end   = rs.getDate("to_date").toLocalDate();
            int uid        = rs.getInt("uid");
            Set<LocalDate> days = map.computeIfAbsent(uid, k -> new HashSet<>());
            for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
                if (!d.isBefore(from) && !d.isAfter(to)) days.add(d);
            }
        }
    }

    // =====================================================
    // =============== MAPPING HELPER ======================
    // =====================================================

    /** Helper chuyển ResultSet → Request (title không có trong schema → để null) */
    private Request mapRow(ResultSet rs) throws SQLException {
        Request r = new Request();
        r.setId(rs.getInt("id"));
        r.setTitle(null);
        r.setReason(rs.getString("reason"));
        r.setStartDate(rs.getDate("start_date").toLocalDate());
        r.setEndDate(rs.getDate("end_date").toLocalDate());
        r.setStatus(rs.getString("status"));
        try { r.setManagerNote(rs.getString("manager_note")); } catch (SQLException ignore) { r.setManagerNote(null); }

        Object created = null, processed = null;
        try { created = rs.getObject("created_by"); } catch (SQLException ignore) {}
        try { processed = rs.getObject("processed_by"); } catch (SQLException ignore) {}

        r.setCreatedBy(created == null ? 0 : ((Number) created).intValue());
        r.setProcessedBy(processed == null ? null : ((Number) processed).intValue());

        return r;
    }
}
