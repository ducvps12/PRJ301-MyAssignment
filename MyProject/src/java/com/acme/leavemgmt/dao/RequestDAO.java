package com.acme.leavemgmt.dao;

import com.acme.leavemgmt.model.Request;
import com.acme.leavemgmt.model.RequestHistory;
import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.DBConnection;
import static com.acme.leavemgmt.util.DBConnection.getConnection;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class RequestDAO {

    // =====================================================
    // =============== USER FUNCTIONS ======================
    // =====================================================
    /**
     * Đăng nhập: tương thích cả schema mới (role_id/department_id) và schema cũ
     * (role/department)
     */
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

    /**
     * Lấy danh sách user theo tên phòng ban (dept_name hoặc Users.department)
     */
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
    /**
     * Tạo đơn – hỗ trợ V2 (employee_id) / V1 (user_id) / V0 (created_by)
     */
    /**
     * Chọn người duyệt đầu tiên cho user: ưu tiên TEAM_LEAD cùng phòng, không
     * có thì DIV_LEADER. Fallback admin id=1.
     */
    public int findManagerFor(User u) throws SQLException {
        if (u == null || u.getDepartment() == null) {
            return 1;
        }
        try (Connection cn = getConnection()) { // hoặc DBConnection.getConnection()
            // 1) Team Lead
            String sqlTL = "SELECT TOP 1 id FROM dbo.Users WHERE department = ? AND role = 'TEAM_LEAD' ORDER BY id";
            try (PreparedStatement ps = cn.prepareStatement(sqlTL)) {
                ps.setString(1, u.getDepartment());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
            // 2) Division Leader
            String sqlDL = "SELECT TOP 1 id FROM dbo.Users WHERE department = ? AND role = 'DIV_LEADER' ORDER BY id";
            try (PreparedStatement ps = cn.prepareStatement(sqlDL)) {
                ps.setString(1, u.getDepartment());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        }
        return 1; // fallback Admin (đổi nếu admin của bạn không phải id=1)
    }
public int createRequest(Request r) throws SQLException {
    // ===== SQL variants =====
    final String INS_V2_T_ID = """
        INSERT INTO dbo.Requests
          (employee_id, approver_id, from_date, to_date, reason, title, status)
        OUTPUT INSERTED.id
        VALUES (?, ?, ?, ?, ?, ?, N'PENDING')
    """;
    final String INS_V2_T_REQID =
        INS_V2_T_ID.replace("OUTPUT INSERTED.id", "OUTPUT INSERTED.request_id");

    final String INS_V2_ID = """
        INSERT INTO dbo.Requests
          (employee_id, approver_id, from_date, to_date, reason, status)
        OUTPUT INSERTED.id
        VALUES (?, ?, ?, ?, ?, N'PENDING')
    """;
    final String INS_V2_REQID =
        INS_V2_ID.replace("OUTPUT INSERTED.id", "OUTPUT INSERTED.request_id");

    final String INS_V1_T = """
        INSERT INTO dbo.Requests
          (user_id, type, status, reason, title, start_date, end_date, created_at)
        OUTPUT INSERTED.id
        VALUES (?, ?, ?, ?, ?, ?, ?, SYSDATETIME())
    """;
    final String INS_V1 = """
        INSERT INTO dbo.Requests
          (user_id, type, status, reason, start_date, end_date, created_at)
        OUTPUT INSERTED.id
        VALUES (?, ?, ?, ?, ?, ?, SYSDATETIME())
    """;

    final String INS_V0_T = """
        INSERT INTO dbo.Requests
          (created_by, processed_by, start_date, end_date, reason, title, status)
        OUTPUT INSERTED.id
        VALUES (?, ?, ?, ?, ?, ?, N'PENDING')
    """;
    final String INS_V0 = """
        INSERT INTO dbo.Requests
          (created_by, processed_by, start_date, end_date, reason, status)
        OUTPUT INSERTED.id
        VALUES (?, ?, ?, ?, ?, N'PENDING')
    """;

    try (Connection cn = DBConnection.getConnection()) {
        cn.setAutoCommit(false);
        Integer newId = null;

        // ===== V2 (ưu tiên): có title =====
        try {
            newId = insertV2(cn, INS_V2_T_ID, r, true);
        } catch (SQLException ex1) {
            try {
                newId = insertV2(cn, INS_V2_T_REQID, r, true);
            } catch (SQLException ignore) { /* thử bản không title */ }
        }

        // ===== V2 fallback: không title =====
        if (newId == null) {
            try {
                newId = insertV2(cn, INS_V2_ID, r, false);
            } catch (SQLException ex2) {
                try {
                    newId = insertV2(cn, INS_V2_REQID, r, false);
                } catch (SQLException ignore) { /* rơi xuống V1 */ }
            }
        }

        // ===== V1 =====
        if (newId == null) {
            final String type = (r.getType() == null || r.getType().isBlank()) ? "ANNUAL" : r.getType();

            // có title
            try (PreparedStatement ps = cn.prepareStatement(INS_V1_T)) {
                ps.setInt(1, orCreatedBy(r));
                ps.setString(2, type);
                ps.setString(3, "PENDING");
                if (r.getReason()!=null) ps.setNString(4, r.getReason()); else ps.setNull(4, java.sql.Types.NVARCHAR);
                if (r.getTitle()!=null)  ps.setNString(5, r.getTitle());  else ps.setNull(5, java.sql.Types.NVARCHAR);
                setLocalDateOrNull(ps, 6, r.getStartDate());
                setLocalDateOrNull(ps, 7, r.getEndDate());
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) newId = rs.getInt(1); }
            } catch (SQLException noTitleCol) {
                // không title
                try (PreparedStatement ps = cn.prepareStatement(INS_V1)) {
                    ps.setInt(1, orCreatedBy(r));
                    ps.setString(2, type);
                    ps.setString(3, "PENDING");
                    if (r.getReason()!=null) ps.setNString(4, r.getReason()); else ps.setNull(4, java.sql.Types.NVARCHAR);
                    setLocalDateOrNull(ps, 5, r.getStartDate());
                    setLocalDateOrNull(ps, 6, r.getEndDate());
                    try (ResultSet rs = ps.executeQuery()) { if (rs.next()) newId = rs.getInt(1); }
                }
            }
        }

        // ===== V0 =====
        if (newId == null) {
            // có title
            try (PreparedStatement ps = cn.prepareStatement(INS_V0_T)) {
                ps.setInt(1, orCreatedBy(r));
                setIntOrNull(ps, 2, r.getProcessedBy());
                setLocalDateOrNull(ps, 3, r.getStartDate());
                setLocalDateOrNull(ps, 4, r.getEndDate());
                if (r.getReason()!=null) ps.setNString(5, r.getReason()); else ps.setNull(5, java.sql.Types.NVARCHAR);
                if (r.getTitle()!=null)  ps.setNString(6, r.getTitle());  else ps.setNull(6, java.sql.Types.NVARCHAR);
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) newId = rs.getInt(1); }
            } catch (SQLException noTitleColV0) {
                // không title
                try (PreparedStatement ps = cn.prepareStatement(INS_V0)) {
                    ps.setInt(1, orCreatedBy(r));
                    setIntOrNull(ps, 2, r.getProcessedBy());
                    setLocalDateOrNull(ps, 3, r.getStartDate());
                    setLocalDateOrNull(ps, 4, r.getEndDate());
                    if (r.getReason()!=null) ps.setNString(5, r.getReason()); else ps.setNull(5, java.sql.Types.NVARCHAR);
                    try (ResultSet rs = ps.executeQuery()) { if (rs.next()) newId = rs.getInt(1); }
                }
            }
        }

        if (newId == null) {
            cn.rollback();
            throw new SQLException("Không tạo được request mới (không xác định schema).");
        }

        String creatorName = (r.getCreatedByName() != null ? r.getCreatedByName() : "SYSTEM");
        insertHistory(cn, newId, orCreatedBy(r), creatorName, "CREATED", null);

        cn.commit();
        return newId;
    }
}


/* Overload hỗ trợ V2 có/không title */
private Integer insertV2(Connection cn, String sql, Request r, boolean withTitle) throws SQLException {
    try (PreparedStatement ps = cn.prepareStatement(sql)) {
        int i = 1;
        ps.setInt(i++, orCreatedBy(r));                // employee_id
        setIntOrNull(ps, i++, r.getApproverId());      // approver_id
        setLocalDateOrNull(ps, i++, r.getStartDate()); // from_date
        setLocalDateOrNull(ps, i++, r.getEndDate());   // to_date
        if (r.getReason()!=null) ps.setNString(i++, r.getReason());
        else ps.setNull(i++, java.sql.Types.NVARCHAR);
        if (withTitle) {
            if (r.getTitle()!=null) ps.setNString(i++, r.getTitle());
            else ps.setNull(i++, java.sql.Types.NVARCHAR);
        }
        try (ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : null;
        }
    }
}



/* ---------------- helpers ---------------- */

private static Integer orCreatedBy(Request r) {
    // bảo đảm có id người tạo; nếu userId null thì lấy createdBy
    return (r.getUserId() != null) ? r.getUserId() : r.getCreatedBy();
}

private static void setLocalDateOrNull(PreparedStatement ps, int idx, java.time.LocalDate d) throws SQLException {
    if (d == null) ps.setNull(idx, java.sql.Types.DATE);
    else ps.setDate(idx, java.sql.Date.valueOf(d));
}

private static void setIntOrNull(PreparedStatement ps, int idx, Integer v) throws SQLException {
    if (v == null) ps.setNull(idx, java.sql.Types.INTEGER);
    else ps.setInt(idx, v);
}

private static Integer insertV2(Connection cn, String sql, Request r) throws SQLException {
    try (PreparedStatement ps = cn.prepareStatement(sql)) {
        ps.setInt(1, orCreatedBy(r));               // employee_id
        setIntOrNull(ps, 2, r.getProcessedBy());    // approver_id
        setLocalDateOrNull(ps, 3, r.getStartDate());// from_date
        setLocalDateOrNull(ps, 4, r.getEndDate());  // to_date
        ps.setString(5, r.getReason());             // reason
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return null;
    }
}


    /**
     * Danh sách đơn của tôi – ưu tiên V2, rồi V1, rồi V0. Alias created_by cho
     * đồng nhất.
     */
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
                    while (rs.next()) {
                        list.add(mapRow(rs));
                    }
                    if (!list.isEmpty()) {
                        return list;
                    }
                }
            } catch (SQLException ignoreV2) {
            }

            try (PreparedStatement ps = cn.prepareStatement(sqlV1)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapRow(rs));
                    }
                    if (!list.isEmpty()) {
                        return list;
                    }
                }
            } catch (SQLException ignoreV1) {
            }

            try (PreparedStatement ps = cn.prepareStatement(sqlV0)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapRow(rs));
                    }
                }
            }
        }
        return list;
    }

    /**
     * Danh sách đơn theo phòng ban (V2 → V1 → V0)
     */
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
                    while (rs.next()) {
                        list.add(mapRow(rs));
                    }
                    if (!list.isEmpty()) {
                        return list;
                    }
                }
            } catch (SQLException ignoreV2) {
            }

            try (PreparedStatement ps = cn.prepareStatement(sqlV1)) {
                ps.setString(1, departmentName);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapRow(rs));
                    }
                    if (!list.isEmpty()) {
                        return list;
                    }
                }
            } catch (SQLException ignoreV1) {
            }

            try (PreparedStatement ps = cn.prepareStatement(sqlV0)) {
                ps.setString(1, departmentName);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapRow(rs));
                    }
                }
            }
        }
        return list;
    }

  /** Lấy chi tiết đơn theo id (thử V2 có title → V2 fallback → V1 có title → V1 fallback → V0 có title → V0 fallback) */
public Request findById(int id) throws SQLException {
   // V2 có title
final String sqlV2_T = """
  SELECT
    lr.request_id AS id,
    lr.title,
    lr.reason,
    lr.from_date  AS start_date,
    lr.to_date    AS end_date,
    lr.status,
    lr.manager_note,
    u_emp.full_name AS created_name,
    lr.employee_id  AS created_by,
    u_app.full_name AS processed_name,
    lr.approver_id  AS processed_by,
    COALESCE(lt.name, lt.code, lr.type) AS leaveTypeName
  FROM dbo.Requests lr
  JOIN dbo.Users u_emp      ON u_emp.id = lr.employee_id
  LEFT JOIN dbo.Users u_app ON u_app.id = lr.approver_id
  LEFT JOIN dbo.LeaveTypes lt ON (lt.id = lr.leave_type_id OR lt.code = lr.type)
  WHERE lr.request_id = ?
""";

// V2 fallback (schema chưa có title)
final String sqlV2 = """
  SELECT
    lr.request_id AS id,
    CAST(NULL AS NVARCHAR(255)) AS title,
    lr.reason,
    lr.from_date  AS start_date,
    lr.to_date    AS end_date,
    lr.status,
    lr.manager_note,
    u_emp.full_name AS created_name,
    lr.employee_id  AS created_by,
    u_app.full_name AS processed_name,
    lr.approver_id  AS processed_by,
    COALESCE(lt.name, lt.code, lr.type) AS leaveTypeName
  FROM dbo.Requests lr
  JOIN dbo.Users u_emp      ON u_emp.id = lr.employee_id
  LEFT JOIN dbo.Users u_app ON u_app.id = lr.approver_id
  LEFT JOIN dbo.LeaveTypes lt ON (lt.id = lr.leave_type_id OR lt.code = lr.type)
  WHERE lr.request_id = ?
""";

// V1 có title
final String sqlV1_T = """
  SELECT
    r.id AS id,
    r.title,
    r.reason,
    r.start_date AS start_date,
    r.end_date   AS end_date,
    r.status,
    CAST(NULL AS NVARCHAR(400)) AS manager_note,
    u.full_name  AS created_name,
    r.user_id    AS created_by,
    CAST(NULL AS NVARCHAR(200)) AS processed_name,
    CAST(NULL AS INT) AS processed_by,
    COALESCE(lt.name, lt.code, r.type) AS leaveTypeName
  FROM dbo.Requests r
  JOIN dbo.Users u ON u.id = r.user_id
  LEFT JOIN dbo.LeaveTypes lt ON (lt.id = r.leave_type_id OR lt.code = r.type)
  WHERE r.id = ?
""";

// V1 fallback
final String sqlV1 = """
  SELECT
    r.id AS id,
    CAST(NULL AS NVARCHAR(255)) AS title,
    r.reason,
    r.start_date AS start_date,
    r.end_date   AS end_date,
    r.status,
    CAST(NULL AS NVARCHAR(400)) AS manager_note,
    u.full_name  AS created_name,
    r.user_id    AS created_by,
    CAST(NULL AS NVARCHAR(200)) AS processed_name,
    CAST(NULL AS INT) AS processed_by,
    COALESCE(lt.name, lt.code, r.type) AS leaveTypeName
  FROM dbo.Requests r
  JOIN dbo.Users u ON u.id = r.user_id
  LEFT JOIN dbo.LeaveTypes lt ON (lt.id = r.leave_type_id OR lt.code = r.type)
  WHERE r.id = ?
""";

// V0 có title
final String sqlV0_T = """
  SELECT
    r.id AS id,
    r.title,
    r.reason,
    r.start_date AS start_date,
    r.end_date   AS end_date,
    r.status,
    r.manager_note,
    u_emp.full_name AS created_name,
    r.created_by    AS created_by,
    u_app.full_name AS processed_name,
    r.processed_by  AS processed_by,
    COALESCE(lt.name, lt.code, r.type) AS leaveTypeName
  FROM dbo.Requests r
  JOIN dbo.Users u_emp      ON u_emp.id = r.created_by
  LEFT JOIN dbo.Users u_app ON u_app.id = r.processed_by
  LEFT JOIN dbo.LeaveTypes lt ON (lt.id = r.leave_type_id OR lt.code = r.type)
  WHERE r.id = ?
""";

// V0 fallback
final String sqlV0 = """
  SELECT
    r.id AS id,
    CAST(NULL AS NVARCHAR(255)) AS title,
    r.reason,
    r.start_date AS start_date,
    r.end_date   AS end_date,
    r.status,
    r.manager_note,
    u_emp.full_name AS created_name,
    r.created_by    AS created_by,
    u_app.full_name AS processed_name,
    r.processed_by  AS processed_by,
    COALESCE(lt.name, lt.code, r.type) AS leaveTypeName
  FROM dbo.Requests r
  JOIN dbo.Users u_emp      ON u_emp.id = r.created_by
  LEFT JOIN dbo.Users u_app ON u_app.id = r.processed_by
  LEFT JOIN dbo.LeaveTypes lt ON (lt.id = r.leave_type_id OR lt.code = r.type)
  WHERE r.id = ?
""";


    try (Connection cn = DBConnection.getConnection()) {
        // V2 có title
        try (PreparedStatement ps = cn.prepareStatement(sqlV2_T)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Request r = mapRowBasic(rs);
                    r.setLeaveTypeName(safeGetString(rs, "leaveTypeName"));
                    r.setHistory(listHistory(cn, id));
                    return r;
                }
            }
        } catch (SQLException ignore) {}

        // V2 fallback
        try (PreparedStatement ps = cn.prepareStatement(sqlV2)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Request r = mapRowBasic(rs);
                    r.setLeaveTypeName(safeGetString(rs, "leaveTypeName"));
                    r.setHistory(listHistory(cn, id));
                    return r;
                }
            }
        } catch (SQLException ignore) {}

        // V1 có title
        try (PreparedStatement ps = cn.prepareStatement(sqlV1_T)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Request r = mapRowBasic(rs);
                    r.setLeaveTypeName(safeGetString(rs, "leaveTypeName"));
                    r.setHistory(listHistory(cn, id));
                    return r;
                }
            }
        } catch (SQLException ignore) {}

        // V1 fallback
        try (PreparedStatement ps = cn.prepareStatement(sqlV1)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Request r = mapRowBasic(rs);
                    r.setLeaveTypeName(safeGetString(rs, "leaveTypeName"));
                    r.setHistory(listHistory(cn, id));
                    return r;
                }
            }
        } catch (SQLException ignore) {}

        // V0 có title
        try (PreparedStatement ps = cn.prepareStatement(sqlV0_T)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Request r = mapRowBasic(rs);
                    r.setLeaveTypeName(safeGetString(rs, "leaveTypeName"));
                    r.setHistory(listHistory(cn, id));
                    return r;
                }
            }
        } catch (SQLException ignore) {}

        // V0 fallback
        try (PreparedStatement ps = cn.prepareStatement(sqlV0)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Request r = mapRowBasic(rs);
                    r.setLeaveTypeName(safeGetString(rs, "leaveTypeName"));
                    r.setHistory(listHistory(cn, id));
                    return r;
                }
            }
        }
    }
    return null;
}
// ===== Helpers dùng chung trong RequestDAO =====

/** Lấy NVARCHAR an toàn (trả null nếu cột không tồn tại hoặc lỗi) */
private static String safeGetString(ResultSet rs, String col) {
    try {
        // Ưu tiên getNString để giữ Unicode (tiếng Việt)
        return rs.getNString(col);
    } catch (SQLException e) {
        try {
            return rs.getString(col);
        } catch (SQLException ex) {
            return null;
        }
    }
}

/** Map các cột cơ bản của Request từ 1 dòng ResultSet (đa schema V2/V1/V0) */
private Request mapRowBasic(ResultSet rs) throws SQLException {
    Request r = new Request();

    r.setId(rs.getInt("id"));

    // NVARCHAR
    r.setTitle(safeGetString(rs, "title"));
    r.setReason(safeGetString(rs, "reason"));
    r.setManagerNote(safeGetString(rs, "manager_note"));

    // DATE
    java.sql.Date sd = rs.getDate("start_date");
    java.sql.Date ed = rs.getDate("end_date");
    r.setStartDate(sd != null ? sd.toLocalDate() : null);
    r.setEndDate(ed != null ? ed.toLocalDate() : null);

    // STATUS
    r.setStatus(safeGetString(rs, "status"));

    // Người tạo / người xử lý (có schema không có processed_by)
    Object cbObj = null;
    try { cbObj = rs.getObject("created_by"); } catch (SQLException ignore) {}
    if (cbObj != null) r.setCreatedBy(((Number) cbObj).intValue());

    r.setCreatedByName(safeGetString(rs, "created_name"));
    r.setProcessedByName(safeGetString(rs, "processed_name"));

    Object pbObj = null;
    try { pbObj = rs.getObject("processed_by"); } catch (SQLException ignore) {}
    r.setProcessedBy(pbObj == null ? null : ((Number) pbObj).intValue());

    // leaveTypeName được set ở ngoài findById() (như bạn đang làm), hoặc có thể:
    // r.setLeaveTypeName(safeGetString(rs, "leaveTypeName"));

    // Nếu có cột đính kèm thì lấy thêm (không bắt buộc)
    try { r.setAttachmentName(safeGetString(rs, "attachment_name")); } catch (Exception ignore) {}

    return r;
}

    // =======================
    // ===== LỊCH SỬ (History)
    // =======================
    /**
     * Thêm lịch sử (mở kết nối mới)
     */
    public void insertHistory(int requestId, int actedBy, String actedByName,
            String action, String note) throws SQLException {
        try (Connection cn = DBConnection.getConnection()) {
            insertHistory(cn, requestId, actedBy, actedByName, action, note);
        }
    }

    /**
     * Thêm lịch sử (tái sử dụng connection có sẵn)
     */
    private void insertHistory(Connection cn, int requestId, int actedBy, String actedByName,
            String action, String note) throws SQLException {
        final String sql = """
          INSERT INTO Request_History(request_id, action, note, acted_by, acted_by_name)
          VALUES (?,?,?,?,?)
        """;
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ps.setString(2, action);
            ps.setString(3, note);
            ps.setInt(4, actedBy);
            ps.setString(5, actedByName);
            ps.executeUpdate();
        }
    }

    /**
     * Danh sách lịch sử (mở kết nối mới)
     */
    public List<RequestHistory> listHistory(int requestId) throws SQLException {
        try (Connection cn = DBConnection.getConnection()) {
            return listHistory(cn, requestId);
        }
    }

    /**
     * Danh sách lịch sử (tái sử dụng connection)
     */
    private List<RequestHistory> listHistory(Connection cn, int requestId) throws SQLException {
        final String sql = """
          SELECT id, request_id, action, note, acted_by, acted_by_name, acted_at
          FROM Request_History
          WHERE request_id = ?
          ORDER BY acted_at DESC, id DESC
        """;
        List<RequestHistory> list = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RequestHistory h = new RequestHistory();
                    h.setId(rs.getInt("id"));
                    h.setRequestId(rs.getInt("request_id"));
                    h.setAction(rs.getString("action"));
                    h.setNote(rs.getString("note"));
                    h.setActedBy(rs.getInt("acted_by"));
                    h.setActedByName(rs.getString("acted_by_name"));
                    Timestamp ts = rs.getTimestamp("acted_at");
                    h.setActedAt(ts != null ? ts.toLocalDateTime() : null);
                    list.add(h);
                }
            }
        }
        return list;
    }

    // =======================
    // ===== HỦY YÊU CẦU
    // =======================
    /**
     * Hủy yêu cầu: chỉ cho phép khi là chủ đơn và trạng thái còn INPROGRESS.
     * Ghi manager_note (lý do hủy) và người xử lý = chính người hủy.
     */
   public boolean cancelRequest(int requestId, int userId, String userName, String note) throws SQLException {
    // Ghi chú:
    // - Requests:       dùng [id], [processed_by], [manager_note], [updated_at]
    // - KHÔNG dùng:     request_id, cancelled_at (không tồn tại trong schema hiện tại)
    // - Trạng thái cho phép hủy: PENDING, INPROGRESS (tuỳ bạn)

    final String SQL_UPDATE_REQUEST =
        "UPDATE dbo.[Requests] " +
        "SET [status] = 'CANCELLED', " +
        "    [processed_by] = ?, " +
        "    [manager_note] = CASE " +
        "        WHEN NULLIF(LTRIM(RTRIM(?)),'') IS NULL THEN [manager_note] " +
        "        WHEN [manager_note] IS NULL OR [manager_note] = '' THEN CONCAT('[User cancel] ', ?) " +
        "        ELSE CONCAT([manager_note], CHAR(10), '[User cancel] ', ?) " +
        "    END, " +
        "    [updated_at] = SYSDATETIME() " +
        "WHERE [id] = ? " +
        "  AND ([user_id] = ? OR [created_by] = ?) " +
        "  AND UPPER([status]) IN ('PENDING','INPROGRESS')";

    // (Tuỳ chọn) Nếu có bảng con RequestApprovals với FK request_id:
    final String SQL_CANCEL_APPROVALS =
        "UPDATE dbo.[RequestApprovals] " +
        "SET [status] = 'CANCELLED', [note] = ?, [updated_at] = SYSDATETIME() " +
        "WHERE [request_id] = ? AND UPPER([status]) = 'PENDING'";

    try (Connection cn = DBConnection.getConnection()) {
        cn.setAutoCommit(false);
        int rowsReq;

        String clean = (note == null) ? null : note.trim();

        // 1) Hủy ở bảng Requests
        try (PreparedStatement ps = cn.prepareStatement(SQL_UPDATE_REQUEST)) {
            int i = 1;
            ps.setInt(i++, userId);     // processed_by
            ps.setString(i++, clean);   // CASE check null/empty
            ps.setString(i++, clean);   // CONCAT when empty
            ps.setString(i++, clean);   // CONCAT when append
            ps.setInt(i++, requestId);  // WHERE id = ?
            ps.setInt(i++, userId);     // AND user_id = ?
            ps.setInt(i++, userId);     // OR  created_by = ?
            rowsReq = ps.executeUpdate();
        }

        // 2) (Tuỳ chọn) Hủy các pending approval con – bỏ qua lỗi nếu bảng không tồn tại
        if (rowsReq > 0) {
            try (PreparedStatement ps2 = cn.prepareStatement(SQL_CANCEL_APPROVALS)) {
                ps2.setString(1, clean == null ? "" : clean);
                ps2.setInt(2, requestId);
                try { ps2.executeUpdate(); } catch (SQLException ignoreIfNoChild) { /* bảng con có thể chưa tạo */ }
            } catch (SQLException ignoreIfNoChild) { /* không có bảng con */ }

            insertHistory(cn, requestId, userId, userName, "CANCELLED", clean);
            cn.commit();
            return true;
        } else {
            cn.rollback();
            return false;
        }
    }
}

    // =======================
    // ===== TẠO MỚI (optional)
    // =======================
    /**
     * Tạo đơn mới (tối giản) – trả về id mới. Tùy DB đặt tên cột tương ứng. Nhớ
     * thêm lịch sử CREATED.
     */
   
    // ====================================
    // ===== DUYỆT / TỪ CHỐI (optional)
    // ====================================
    public boolean approve(int requestId, int managerId, String managerName, String note) throws SQLException {
        final String sqlV0 = """
          UPDATE Requests SET status='APPROVED', processed_by=?, manager_note=?
          WHERE id=? AND status='INPROGRESS'
        """;
        final String sqlV2 = """
          UPDATE Requests SET status='APPROVED', approver_id=?, manager_note=?
          WHERE request_id=? AND status='INPROGRESS'
        """;
        return updateDecisionWithHistory(requestId, managerId, managerName, note, "APPROVED", sqlV0, sqlV2);
    }

    public boolean reject(int requestId, int managerId, String managerName, String note) throws SQLException {
        final String sqlV0 = """
          UPDATE Requests SET status='REJECTED', processed_by=?, manager_note=?
          WHERE id=? AND status='INPROGRESS'
        """;
        final String sqlV2 = """
          UPDATE Requests SET status='REJECTED', approver_id=?, manager_note=?
          WHERE request_id=? AND status='INPROGRESS'
        """;
        return updateDecisionWithHistory(requestId, managerId, managerName, note, "REJECTED", sqlV0, sqlV2);
    }

    public int duplicate(int id, int newOwnerId) throws SQLException {
        final String sql = """
            INSERT INTO Requests(title,reason,start_date,end_date,status,created_by)
            SELECT title, reason, start_date, end_date, 'PENDING', ?
              FROM Requests WHERE id=?
            """;
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, newOwnerId);
            ps.setInt(2, id);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public int bulkUpdate(List<Integer> ids, String action, int managerId, String note) throws SQLException {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        String newStatus = switch (action) {
            case "approve" ->
                "APPROVED";
            case "reject" ->
                "REJECTED";
            case "cancel" ->
                "CANCELLED";
            default ->
                null;
        };
        if (newStatus == null) {
            return 0;
        }
        String in = "?,".repeat(ids.size());
        in = in.substring(0, in.length() - 1);
        String sql = "UPDATE Requests SET status=?, processed_by=?, manager_note=? WHERE status='PENDING' AND id IN (" + in + ")";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            int idx = 1;
            ps.setString(idx++, newStatus);
            ps.setInt(idx++, managerId);
            ps.setString(idx++, note);
            for (Integer id : ids) {
                ps.setInt(idx++, id);
            }
            return ps.executeUpdate();
        }
    }




public int duplicateFrom(int srcId, int newUserId, String newUserName,
                         boolean copyAttachments,
                         String overrideReason,
                         LocalDate overrideStart,
                         LocalDate overrideEnd,
                         Integer overrideLeaveTypeId) throws SQLException {

    String sqlInsert = """
        INSERT INTO Requests (user_id, type, status, reason, start_date, end_date,
                              leave_type_id, created_at, updated_at, processed_by,
                              manager_note, approved_by, approved_at, approve_note)
        SELECT ?, r.type, 'pending',
               COALESCE(?, r.reason),
               COALESCE(?, r.start_date),
               COALESCE(?, r.end_date),
               COALESCE(?, r.leave_type_id),
               sysdatetime(), NULL, NULL, NULL, NULL, NULL, NULL
        FROM Requests r WHERE r.id = ?
        """;

    try (Connection c = DBConnection.getConnection();
         PreparedStatement ps = c.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {

        ps.setInt(1, newUserId);
        ps.setString(2, overrideReason);
        if (overrideStart != null) ps.setDate(3, Date.valueOf(overrideStart)); else ps.setNull(3, Types.DATE);
        if (overrideEnd   != null) ps.setDate(4, Date.valueOf(overrideEnd));   else ps.setNull(4, Types.DATE);
        if (overrideLeaveTypeId != null) ps.setInt(5, overrideLeaveTypeId);   else ps.setNull(5, Types.INTEGER);
        ps.setInt(6, srcId);

        int n = ps.executeUpdate();
        if (n == 0) throw new SQLException("Duplicate failed");

        int newId;
        try (ResultSet rs = ps.getGeneratedKeys()) {
            rs.next();
            newId = rs.getInt(1);
        }

        // (Optional) copy attachments: giữ file_name, file_path… Nếu không muốn copy path, thay bằng NULL.
        if (copyAttachments) {
            String sqlAtt = """
              INSERT INTO Request_Attachments (request_id, file_name, file_path, uploaded_by, uploaded_at)
              SELECT ?, file_name, file_path, ?, uploaded_at
              FROM Request_Attachments WHERE request_id = ?
            """;
            try (PreparedStatement pa = c.prepareStatement(sqlAtt)) {
                pa.setInt(1, newId);
                pa.setInt(2, newUserId);
                pa.setInt(3, srcId);
                pa.executeUpdate();
            }
        }

        // Ghi lịch sử
        String note = "Duplicated from #" + srcId;
        String sqlHis = """
            INSERT INTO Request_History (request_id, action, note, acted_by, acted_by_name, acted_at)
            VALUES (?, 'CREATED', ?, ?, ?, sysdatetime())
        """;
        try (PreparedStatement ph = c.prepareStatement(sqlHis)) {
            ph.setInt(1, newId);
            ph.setString(2, note);
            ph.setInt(3, newUserId);
            ph.setString(4, newUserName != null ? newUserName : ("User#" + newUserId));
            ph.executeUpdate();
        }

        return newId;
    }
}







    // ====== LIST + COUNT with filters ======
    public List<Request> findPage(Integer userId, boolean onlyMine, boolean teamOfManager,
            LocalDate from, LocalDate to, String status, String keyword,
            String sort, int page, int pageSize) throws SQLException {
        StringBuilder sql = new StringBuilder("""
            SELECT r.id, r.title, r.reason, r.start_date, r.end_date, r.status,
                   r.created_by, u1.full_name AS created_by_name,
                   r.processed_by, u2.full_name AS processed_by_name,
                   r.manager_note
              FROM Requests r
              JOIN Users u1 ON u1.id = r.created_by
              LEFT JOIN Users u2 ON u2.id = r.processed_by
             WHERE 1=1
            """);
        List<Object> params = new ArrayList<>();

        // scope
        if (onlyMine && userId != null) {
            sql.append(" AND r.created_by = ? ");
            params.add(userId);
        } else if (teamOfManager && userId != null) {
            // Giả sử Users.department: lấy tất cả user cùng department của manager trừ manager
            sql.append("""
                AND u1.department IN (
                    SELECT u.department FROM Users u WHERE u.id = ?
                )
                """);
            params.add(userId);
        }

        // filters
        if (from != null) {
            sql.append(" AND r.start_date >= ? ");
            params.add(from);
        }
        if (to != null) {
            sql.append(" AND r.end_date   <= ? ");
            params.add(to);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND r.status = ? ");
            params.add(status.toUpperCase());
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (r.title LIKE ? OR r.reason LIKE ? OR u1.full_name LIKE ?) ");
            String k = "%" + keyword.trim() + "%";
            params.add(k);
            params.add(k);
            params.add(k);
        }

        // sort
        String order = switch (sort == null ? "" : sort) {
            case "created_asc" ->
                " r.id ASC ";
            case "created_desc" ->
                " r.id DESC ";
            case "from_asc" ->
                " r.start_date ASC, r.id DESC ";
            case "from_desc" ->
                " r.start_date DESC, r.id DESC ";
            default ->
                " r.id DESC ";
        };
        sql.append(" ORDER BY ").append(order);
        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY ");
        int offset = Math.max(0, (page - 1) * pageSize);

        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int i = 1;
            for (Object p : params) {
                ps.setObject(i++, p);
            }
            ps.setInt(i++, offset);
            ps.setInt(i, pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                List<Request> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(map(rs));
                }
                return out;
            }
        }
    }

    public int count(Integer userId, boolean onlyMine, boolean teamOfManager,
            LocalDate from, LocalDate to, String status, String keyword) throws SQLException {
        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(1)
              FROM Requests r
              JOIN Users u1 ON u1.id = r.created_by
             WHERE 1=1
            """);
        List<Object> params = new ArrayList<>();

        if (onlyMine && userId != null) {
            sql.append(" AND r.created_by = ? ");
            params.add(userId);
        } else if (teamOfManager && userId != null) {
            sql.append("""
                AND u1.department IN (
                    SELECT u.department FROM Users u WHERE u.id = ?
                )
                """);
            params.add(userId);
        }
        if (from != null) {
            sql.append(" AND r.start_date >= ? ");
            params.add(from);
        }
        if (to != null) {
            sql.append(" AND r.end_date   <= ? ");
            params.add(to);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND r.status = ? ");
            params.add(status.toUpperCase());
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (r.title LIKE ? OR r.reason LIKE ? OR u1.full_name LIKE ?) ");
            String k = "%" + keyword.trim() + "%";
            params.add(k);
            params.add(k);
            params.add(k);
        }

        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int i = 1;
            for (Object p : params) {
                ps.setObject(i++, p);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public Stats statsForManager(int managerId) throws SQLException {
        Stats s = new Stats();
        try (Connection c = DBConnection.getConnection()) {

            try (PreparedStatement ps = c.prepareStatement("""
                SELECT COUNT(*) FROM Requests r
                JOIN Users u ON u.id=r.created_by
                WHERE r.status='PENDING' AND u.department IN (SELECT department FROM Users WHERE id=?)
            """)) {
                ps.setInt(1, managerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        s.pendingCount = rs.getInt(1);
                    }
                }
            }

            try (PreparedStatement ps = c.prepareStatement("""
                SELECT COUNT(*) FROM Requests r
                JOIN Users u ON u.id=r.created_by
                WHERE r.status='APPROVED'
                  AND MONTH(r.start_date) = MONTH(GETDATE())
                  AND YEAR(r.start_date)  = YEAR(GETDATE())
                  AND u.department IN (SELECT department FROM Users WHERE id=?)
            """)) {
                ps.setInt(1, managerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        s.approvedThisMonth = rs.getInt(1);
                    }
                }
            }

            try (PreparedStatement ps = c.prepareStatement("""
                SELECT SUM(CASE WHEN r.status='APPROVED' THEN 1 ELSE 0 END) AS ok,
                       COUNT(*) AS allcnt
                FROM Requests r
                JOIN Users u ON u.id=r.created_by
                WHERE u.department IN (SELECT department FROM Users WHERE id=?)
            """)) {
                ps.setInt(1, managerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        s.approvalNumerator = rs.getInt("ok");
                        s.approvalDenominator = rs.getInt("allcnt");
                    }
                }
            }
        }
        return s;
    }

    private Request map(ResultSet rs) throws SQLException {
        Request r = new Request();
        r.setId(rs.getInt("id"));
        r.setTitle(rs.getString("title"));
        r.setReason(rs.getString("reason"));
        Object sd = rs.getObject("start_date");
        Object ed = rs.getObject("end_date");
        if (sd != null) {
            r.setStartDate(((java.sql.Date) sd).toLocalDate());
        }
        if (ed != null) {
            r.setEndDate(((java.sql.Date) ed).toLocalDate());
        }
        r.setStatus(rs.getString("status"));
        r.setCreatedBy(rs.getInt("created_by"));
        r.setCreatedByName(rs.getString("created_by_name"));
        int pb = rs.getInt("processed_by");
        r.setProcessedBy(rs.wasNull() ? null : pb);
        r.setProcessedByName(rs.getString("processed_by_name"));
        r.setManagerNote(rs.getString("manager_note"));
        return r;
    }

    private boolean updateDecisionWithHistory(int requestId, int uid, String uname, String note,
            String action, String sqlV0, String sqlV2) throws SQLException {
        try (Connection cn = DBConnection.getConnection()) {
            cn.setAutoCommit(false);
            int rows;
            try (PreparedStatement ps = cn.prepareStatement(sqlV0)) {
                ps.setInt(1, uid);
                ps.setString(2, note);
                ps.setInt(3, requestId);
                rows = ps.executeUpdate();
            } catch (SQLException tryV2) {
                try (PreparedStatement ps2 = cn.prepareStatement(sqlV2)) {
                    ps2.setInt(1, uid);
                    ps2.setString(2, note);
                    ps2.setInt(3, requestId);
                    rows = ps2.executeUpdate();
                }
            }
            if (rows > 0) {
                insertHistory(cn, requestId, uid, uname, action, note);
                cn.commit();
                return true;
            }
            cn.rollback();
            return false;
        }
    }

    // =======================
    // ===== LIST cơ bản (optional, phục vụ list.jsp có phân quyền)
    // =======================
    /**
     * Liệt kê đơn theo người tạo (cho nhân viên)
     */
    public List<Request> listByCreator(int userId) throws SQLException {
        final String sql = """
          SELECT id, title, reason, start_date, end_date, status, manager_note,
                 created_by, NULL AS processed_by,
                 CAST(NULL AS NVARCHAR(255)) AS created_name,
                 CAST(NULL AS NVARCHAR(255)) AS processed_name
          FROM Requests
          WHERE created_by = ?
          ORDER BY id DESC
        """;
        List<Request> out = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(mapRowBasic(rs));
                }
            }
        }
        return out;
    }

    /**
     * Liệt kê toàn bộ (cho manager)
     */
    public List<Request> listAll() throws SQLException {
        final String sql = """
          SELECT id, title, reason, start_date, end_date, status, manager_note,
                 created_by, processed_by,
                 CAST(NULL AS NVARCHAR(255)) AS created_name,
                 CAST(NULL AS NVARCHAR(255)) AS processed_name
          FROM Requests
          ORDER BY id DESC
        """;
        List<Request> out = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(mapRowBasic(rs));
            }
        }
        return out;
    }

    /**
     * Duyệt/từ chối – nếu schema không có approver/manager_note thì chỉ update
     * status
     */
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

    /**
     * Map<userId, Set<LocalDate>> các ngày Approved trong phòng ban/khoảng ngày
     */
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
                    if (!map.isEmpty()) {
                        return map;
                    }
                }
            } catch (SQLException ignoreV2) {
            }

            try (PreparedStatement ps = cn.prepareStatement(sqlV1)) {
                ps.setString(1, departmentName);
                ps.setDate(2, Date.valueOf(from));
                ps.setDate(3, Date.valueOf(to));
                try (ResultSet rs = ps.executeQuery()) {
                    accumulateDays(map, rs, from, to);
                    if (!map.isEmpty()) {
                        return map;
                    }
                }
            } catch (SQLException ignoreV1) {
            }

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
            LocalDate end = rs.getDate("to_date").toLocalDate();
            int uid = rs.getInt("uid");
            Set<LocalDate> days = map.computeIfAbsent(uid, k -> new HashSet<>());
            for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
                if (!d.isBefore(from) && !d.isAfter(to)) {
                    days.add(d);
                }
            }
        }
    }

   // =====================================================
// =============== MAPPING HELPER ======================
// =====================================================
/** ResultSet -> Request (không đòi hỏi cột 'title') */
private Request mapRow(ResultSet rs) throws SQLException {
    Request r = new Request();
    r.setId(rs.getInt("id"));
    r.setUserId(rs.getInt("user_id"));
    r.setType(safeGet(rs, "type"));
    r.setStatus(safeGet(rs, "status"));
    r.setReason(safeGet(rs, "reason"));

    java.sql.Date sd = rs.getDate("start_date");
    java.sql.Date ed = rs.getDate("end_date");
    r.setFrom(sd == null ? null : sd.toLocalDate());
    r.setTo(ed == null ? null : ed.toLocalDate());

    r.setManagerNote(safeGet(rs, "manager_note"));
    r.setProcessedBy((Integer)(rs.getObject("processed_by")==null? null : rs.getInt("processed_by")));
    r.setApprovedBy((Integer)(rs.getObject("approved_by")==null ? null : rs.getInt("approved_by")));

    try {
        java.sql.Timestamp ct = rs.getTimestamp("created_at");
        java.sql.Timestamp ap = rs.getTimestamp("approved_at");
        r.setCreatedAt(ct == null ? null : ct.toLocalDateTime());
        r.setApprovedAt(ap == null ? null : ap.toLocalDateTime());
    } catch (SQLException ignore) {}

    // nếu SELECT không cấp full_name thì tự fallback
    String name = safeGet(rs, "full_name");
    if (name == null || name.isBlank()) name = "User #" + r.getUserId();
    r.setFullName(name);

    r.setTitle(null);
    return r;
}

public int create(Request r) throws SQLException {
    final String sql =
        "INSERT INTO dbo.Requests " +
        "(user_id, type, status, reason, start_date, end_date, leave_type_id) " +
        "VALUES (?, ?, 'PENDING', ?, ?, ?, ?)";

    try (Connection cn = DBConnection.getConnection();
         PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

        ps.setInt(1, r.getUserId());
        ps.setString(2, r.getType());
        ps.setString(3, r.getReason());

        // from (java.util.Date -> java.sql.Date)
        if (r.getFrom() != null) {
            ps.setDate(4, new java.sql.Date(r.getFrom().getTime()));
        } else {
            ps.setNull(4, java.sql.Types.DATE);
        }

        // to (java.util.Date -> java.sql.Date)
        if (r.getTo() != null) {
            ps.setDate(5, new java.sql.Date(r.getTo().getTime()));
        } else {
            ps.setNull(5, java.sql.Types.DATE);
        }

        if (r.getLeaveTypeId() != null) ps.setInt(6, r.getLeaveTypeId());
        else ps.setNull(6, java.sql.Types.INTEGER);

        ps.executeUpdate();
        try (ResultSet rs = ps.getGeneratedKeys()) {
            return rs.next() ? rs.getInt(1) : -1;
        }
    }
}


// Trong RequestDAO
public boolean updateStatusIfPending(int id, String newStatus, int actorId, String note) throws SQLException {
    try (Connection cn = getConnection()) {
        String ns = (newStatus == null ? "" : newStatus.trim().toUpperCase());
        switch (ns) {
            case "APPROVED": {
                String sql = """
                    UPDATE dbo.Requests
                       SET status='APPROVED',
                           approved_by = ?,
                           approved_at = SYSDATETIME(),
                           approve_note = ?,
                           updated_at = SYSDATETIME()
                     WHERE id = ? AND status = 'PENDING'
                """;
                try (PreparedStatement ps = cn.prepareStatement(sql)) {
                    ps.setInt(1, actorId);
                    ps.setString(2, safeNote(note));
                    ps.setInt(3, id);
                    return ps.executeUpdate() > 0;
                }
            }
            case "REJECTED":
            case "CANCELLED": {
                String sql = """
                    UPDATE dbo.Requests
                       SET status = ?,
                           processed_by = ?,
                           manager_note = CASE
                               WHEN ? IS NULL OR LTRIM(RTRIM(?))='' THEN manager_note
                               WHEN manager_note IS NULL OR manager_note='' THEN ?
                               ELSE CONCAT(manager_note, CHAR(10), ?)
                           END,
                           updated_at = SYSDATETIME()
                     WHERE id = ? AND status IN ('PENDING','INPROGRESS')
                """;
                try (PreparedStatement ps = cn.prepareStatement(sql)) {
                    String n = safeNote(note);
                    ps.setString(1, ns);
                    ps.setInt(2, actorId);
                    ps.setString(3, n);
                    ps.setString(4, n);
                    ps.setString(5, n);
                    ps.setString(6, n);
                    ps.setInt(7, id);
                    return ps.executeUpdate() > 0;
                }
            }
            default:
                return false;
        }
    }
}


public boolean isAllowedToApprove(User me, Request reqObj) throws SQLException {
    if (me == null || reqObj == null) return false;

    final String role = me.getRole() == null ? "" : me.getRole().trim().toUpperCase();
    final int requestId = reqObj.getId();

    if ("ADMIN".equals(role) || "SYS_ADMIN".equals(role) || "HR_ADMIN".equals(role)) return true;

    try (Connection cn = getConnection()) {
        ensureMappings(cn);

        // Base join: Users (requester) <-> Requests (owner of request)
        final String BASE_JOIN = """
            FROM dbo.Requests r
            JOIN dbo.Users    u ON u.%s = r.%s
           WHERE r.id = ?
        """.formatted(USERS_PK, REQ_REQUESTER_COL);

        switch (role) {
            case "TEAM_LEAD": {
                // Approve nếu me là manager trực tiếp của requester
                final String sql = "SELECT 1 " + BASE_JOIN + " AND u." + USERS_MANAGER_COL + " = ?";
                try (PreparedStatement ps = cn.prepareStatement(sql)) {
                    ps.setInt(1, requestId);
                    ps.setInt(2, me.getUserId()); // hoặc me.getId() tùy User model
                    try (ResultSet rs = ps.executeQuery()) {
                        return rs.next();
                    }
                }
            }
            case "DIV_LEADER": {
                // Approve nếu cùng phòng/ban
                final String sql = "SELECT 1 " + BASE_JOIN + " AND u." + USERS_DEPTID_COL + " = ?";
                try (PreparedStatement ps = cn.prepareStatement(sql)) {
                    ps.setInt(1, requestId);
                    ps.setInt(2, me.getDepartmentId());
                    try (ResultSet rs = ps.executeQuery()) {
                        return rs.next();
                    }
                }
            }
            default:
                return false;
        }
    }
}

// ==== Column detector (cache) ====
private static volatile String USERS_PK = null;        // user_id | id
private static volatile String REQ_REQUESTER_COL = null; // employee_id | created_by | user_id
private static volatile String USERS_MANAGER_COL = "manager_id"; // nếu schema khác có thể detect tương tự
private static volatile String USERS_DEPTID_COL  = "department_id"; // detect tương tự nếu cần

private static String detectColumn(Connection cn, String schema, String table, String... candidates) throws SQLException {
    final String sql = """
        SELECT c.name
          FROM sys.columns c
         WHERE c.object_id = OBJECT_ID(?)
           AND c.name IN (%s)
    """.formatted(String.join(",", java.util.Collections.nCopies(candidates.length, "?")));
    try (PreparedStatement ps = cn.prepareStatement(sql)) {
        ps.setString(1, schema + "." + table);
        for (int i = 0; i < candidates.length; i++) ps.setString(i + 2, candidates[i]);
        try (ResultSet rs = ps.executeQuery()) {
            java.util.Set<String> exists = new java.util.HashSet<>();
            while (rs.next()) exists.add(rs.getString(1));
            for (String cand : candidates) if (exists.contains(cand)) return cand;
        }
    }
    throw new SQLException("None of the candidate columns exist on " + schema + "." + table + " → " + java.util.Arrays.toString(candidates));
}

private static void ensureMappings(Connection cn) throws SQLException {
    if (USERS_PK == null || REQ_REQUESTER_COL == null) {
        synchronized (RequestDAO.class) {
            if (USERS_PK == null)
                USERS_PK = detectColumn(cn, "dbo", "Users", "user_id", "id");
            if (REQ_REQUESTER_COL == null)
                REQ_REQUESTER_COL = detectColumn(cn, "dbo", "Requests", "employee_id", "created_by", "user_id");
            // Nếu cần, cũng detect manager/dept id tương tự:
            // USERS_MANAGER_COL = detectColumn(cn, "dbo", "Users", "manager_id", "leader_id");
            // USERS_DEPTID_COL  = detectColumn(cn, "dbo", "Users", "department_id", "dept_id");
        }
    }
}



public int countHeadcountActiveByDept(String dept) throws SQLException {
    final String sql = """
        SELECT COUNT(*) FROM dbo.Users
         WHERE status = 1 AND department = ?
    """;
    try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
        ps.setString(1, dept);
        try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
    }
}

public int countPendingByDept(String dept, LocalDate from, LocalDate to) throws SQLException {
    StringBuilder sb = new StringBuilder("""
        SELECT COUNT(*)
          FROM dbo.Requests r
          JOIN dbo.Users u ON u.user_id = r.user_id
         WHERE r.status = 'PENDING'
           AND u.department = ?
    """);
    if (from != null) sb.append(" AND r.start_date >= ? ");
    if (to   != null) sb.append(" AND r.end_date   <= ? ");

    try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sb.toString())) {
        int i=1; ps.setString(i++, dept);
        if (from != null) ps.setDate(i++, Date.valueOf(from));
        if (to   != null) ps.setDate(i++, Date.valueOf(to));
        try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
    }
}

public int countApprovedThisMonthByDept(String dept) throws SQLException {
    final String sql = """
        SELECT COUNT(*)
          FROM dbo.Requests r
          JOIN dbo.Users u ON u.user_id = r.user_id
         WHERE r.status = 'APPROVED'
           AND u.department = ?
           AND YEAR(r.approved_at) = YEAR(GETDATE())
           AND MONTH(r.approved_at) = MONTH(GETDATE())
    """;
    try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
        ps.setString(1, dept);
        try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
    }
}

public int countApprovalNumerator(String dept) throws SQLException {
    final String sql = """
        SELECT COUNT(*)
          FROM dbo.Requests r
          JOIN dbo.Users u ON u.user_id = r.user_id
         WHERE r.status = 'APPROVED' AND u.department = ?
    """;
    try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
        ps.setString(1, dept);
        try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
    }
}

public int countApprovalDenominator(String dept) throws SQLException {
    final String sql = """
        SELECT COUNT(*)
          FROM dbo.Requests r
          JOIN dbo.Users u ON u.user_id = r.user_id
         WHERE r.status IN ('APPROVED','REJECTED') AND u.department = ?
    """;
    try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
        ps.setString(1, dept);
        try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
    }
}


public List<Request> findPendingList(String dept, LocalDate from, LocalDate to) throws SQLException {
   StringBuilder sb = new StringBuilder("""
       SELECT r.id, r.user_id AS created_by, u.full_name AS createdByName,
              r.type, r.reason, r.start_date, r.end_date, r.status, u.department AS dept
         FROM dbo.Requests r
         JOIN dbo.Users u ON u.user_id = r.user_id
        WHERE r.status = 'PENDING' AND u.department = ?
   """);
   if (from != null) sb.append(" AND r.start_date >= ? ");
   if (to   != null) sb.append(" AND r.end_date   <= ? ");
   sb.append(" ORDER BY r.created_at DESC ");

   try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sb.toString())) {
       int i=1; ps.setString(i++, dept);
       if (from != null) ps.setDate(i++, Date.valueOf(from));
       if (to   != null) ps.setDate(i++, Date.valueOf(to));
       try (ResultSet rs = ps.executeQuery()) {
           List<Request> list = new ArrayList<>();
           while (rs.next()) {
               Request r = new Request();
               r.setId(rs.getInt("id"));
               r.setCreatedBy(rs.getInt("created_by"));
               r.setCreatedByName(rs.getString("createdByName"));
               r.setType(rs.getString("type"));
               r.setReason(rs.getString("reason"));
               java.sql.Date sd = rs.getDate("start_date"), ed = rs.getDate("end_date");
               r.setFrom(sd==null?null:sd.toLocalDate());
               r.setTo(ed==null?null:ed.toLocalDate());
               r.setStatus(rs.getString("status"));
               r.setDepartment(rs.getString("dept"));
               list.add(r);
           }
           return list;
       }
   }
}

public List<Request> findTodayOff(String dept, LocalDate today) throws SQLException {
  final String sql = """
      SELECT r.id, r.user_id AS created_by, u.full_name AS createdByName,
             r.type, r.start_date, r.end_date, u.department AS dept
        FROM dbo.Requests r
        JOIN dbo.Users u ON u.user_id = r.user_id
       WHERE r.status='APPROVED'
         AND u.department = ?
         AND ? BETWEEN r.start_date AND r.end_date
       ORDER BY u.full_name
  """;
  try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
      ps.setString(1, dept);
      ps.setDate(2, Date.valueOf(today));
      try (ResultSet rs = ps.executeQuery()) {
          List<Request> list = new ArrayList<>();
          while (rs.next()) {
              Request r = new Request();
              r.setId(rs.getInt("id"));
              r.setCreatedBy(rs.getInt("created_by"));
              r.setCreatedByName(rs.getString("createdByName"));
              r.setType(rs.getString("type"));
              java.sql.Date sd = rs.getDate("start_date"), ed = rs.getDate("end_date");
              r.setFrom(sd==null?null:sd.toLocalDate());
              r.setTo(ed==null?null:ed.toLocalDate());
              r.setDepartment(rs.getString("dept"));
              r.setStatus("APPROVED");
              list.add(r);
          }
          return list;
      }
  }
}

    private boolean hasRole(int userId, String... roleCodes) throws SQLException {
        String in = String.join(",", java.util.Collections.nCopies(roleCodes.length, "?"));
        String sql = "SELECT 1 FROM UserRoles ur "
                + "JOIN Roles r ON r.id = ur.role_id "
                + "WHERE ur.user_id=? AND r.code IN (" + in + ")";
        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            for (int i = 0; i < roleCodes.length; i++) {
                ps.setString(2 + i, roleCodes[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

 public boolean canProcessRequest(int requestId, User me) throws SQLException {
    // ADMIN thì pass nhanh
    if (hasRole(me.getUserId(), "ADMIN", "SYS_ADMIN", "HR_ADMIN")) return true;

    String sql = """
        SELECT 1
          FROM Requests r
          JOIN Users u ON u.user_id = r.user_id
         WHERE r.id = ?
           AND (
                 u.manager_id = ?                                      -- quản lý trực tiếp
              OR (u.department = ? AND EXISTS (
                    SELECT 1 FROM UserRoles ur
                    JOIN Roles ro ON ur.role_id = ro.id
                   WHERE ur.user_id = ? AND ro.code IN ('DIV_LEADER','TEAM_LEAD','LEADER')
                 ))
           )
    """;
    try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
        ps.setInt(1, requestId);
        ps.setInt(2, me.getUserId());
        ps.setString(3, me.getDepartment());
        ps.setInt(4, me.getUserId());
        try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
    }
}

public boolean processDecision(int requestId, int approverId, boolean approve, String note) throws SQLException {
    return updateStatusIfPending(requestId, approve ? "APPROVED" : "REJECTED", approverId, note);
}



public List<Request> dedupe(List<Request> raw) {
    Map<Integer, Request> map = new LinkedHashMap<>();
    for (Request r : raw) {
        map.putIfAbsent(r.getId(), r);
    }
    return new ArrayList<>(map.values());
}
// chạy query và map
private List<Request> runAndMap(String sql, Object... params) throws SQLException {
    try (Connection cn = DBConnection.getConnection();
         PreparedStatement ps = cn.prepareStatement(sql)) {
        for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
        List<Request> out = new java.util.ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) { while (rs.next()) out.add(mapRow(rs)); }
        return out;
    }
}

public List<Request> findPendingForApprover(User me) throws SQLException {
    String role = (me.getRoleCode()!=null ? me.getRoleCode() : me.getRole());
    role = role==null ? "" : role.trim().toUpperCase();
    final int meId = (me.getUserId()!=0 ? me.getUserId() : me.getId());
    final String myDept = (me.getDepartment()==null ? "" : me.getDepartment().trim());

    // 3 biến thể BASE
    final String BASE1 = """
        SELECT r.id, r.user_id, r.type, r.status, r.reason,
               r.start_date, r.end_date,
               r.processed_by, r.approved_by, r.approved_at,
               r.manager_note, r.created_at,
               u.full_name AS full_name
          FROM dbo.Requests r
          JOIN dbo.Users u ON u.user_id = r.user_id
         WHERE UPPER(r.status)='PENDING'
    """;
    final String BASE2 = BASE1.replace("u.user_id = r.user_id","u.id = r.user_id");
    final String BASE3 = """
        SELECT r.id, r.user_id, r.type, r.status, r.reason,
               r.start_date, r.end_date,
               r.processed_by, r.approved_by, r.approved_at,
               r.manager_note, r.created_at,
               CAST(NULL AS nvarchar(255)) AS full_name
          FROM dbo.Requests r
         WHERE UPPER(r.status)='PENDING'
    """;

    // ADMIN/SYS_ADMIN/HR_ADMIN
    if ("ADMIN".equals(role) || "SYS_ADMIN".equals(role) || "HR_ADMIN".equals(role)) {
        try { return runAndMap(BASE1 + " ORDER BY r.created_at DESC"); }
        catch (SQLException e1) {
            try { return runAndMap(BASE2 + " ORDER BY r.created_at DESC"); }
            catch (SQLException e2) { return runAndMap(BASE3 + " ORDER BY r.created_at DESC"); }
        }
    }

    // LEADER/MANAGER/DIV_LEADER/TEAM_LEAD
    if ("DIV_LEADER".equals(role) || "TEAM_LEAD".equals(role)
        || "MANAGER".equals(role) || "LEADER".equals(role)) {

        String TAIL = " AND (u.department = ? OR r.processed_by = ? OR r.approved_by = ?) " +
                      " ORDER BY r.created_at DESC";
        try { return runAndMap(BASE1 + TAIL, myDept, meId, meId); }
        catch (SQLException e1) {
            try { return runAndMap(BASE2 + TAIL, myDept, meId, meId); }
            catch (SQLException e2) {
                // cuối cùng: bỏ JOIN, ít nhất vẫn xem được đơn được assign cho mình
                return runAndMap(BASE3 + " AND (r.processed_by=? OR r.approved_by=?) ORDER BY r.created_at DESC",
                                 meId, meId);
            }
        }
    }

    // User thường
    return runAndMap(BASE3 + " AND (r.processed_by=? OR r.approved_by=?) ORDER BY r.created_at DESC",
                     meId, meId);
}




private List<Request> query(String sql, Object... params) throws SQLException {
    try (Connection cn = DBConnection.getConnection();
         PreparedStatement ps = cn.prepareStatement(sql)) {

        for (int i = 0; i < params.length; i++) {
            Object p = params[i];
            int idx = i + 1;

            if (p == null) {
                // đoán kiểu hay dùng: ID là INT/BIGINT, mặc định INT
                ps.setNull(idx, java.sql.Types.INTEGER);
            } else if (p instanceof Integer)   ps.setInt(idx, (Integer) p);
            else if (p instanceof Long)        ps.setLong(idx, (Long) p);
            else if (p instanceof String)      ps.setString(idx, (String) p);
            else if (p instanceof java.time.LocalDate)     ps.setDate(idx, java.sql.Date.valueOf((java.time.LocalDate)p));
            else if (p instanceof java.time.LocalDateTime) ps.setTimestamp(idx, java.sql.Timestamp.valueOf((java.time.LocalDateTime)p));
            else if (p instanceof java.util.Date)          ps.setTimestamp(idx, new java.sql.Timestamp(((java.util.Date)p).getTime()));
            else ps.setObject(idx, p);
        }

        List<Request> list = new java.util.ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRequest(rs));
        }
        return list;
    }
}
private Request mapRequest(ResultSet rs) throws SQLException {
    Request r = new Request();
    r.setId(rs.getInt("id"));
    r.setUserId(rs.getInt("user_id"));
    r.setType(rs.getString("type"));
    r.setReason(rs.getString("reason"));
    r.setStatus(rs.getString("status"));

    // nếu cột là start_date / end_date:
    java.sql.Date sd = rs.getDate("start_date");
    java.sql.Date ed = rs.getDate("end_date");
    r.setFrom(sd == null ? null : sd.toLocalDate());
    r.setTo(ed == null ? null : ed.toLocalDate());

    java.sql.Timestamp ct = rs.getTimestamp("created_at");
    r.setCreatedAt(ct == null ? null : ct.toLocalDateTime());

    java.sql.Timestamp ap = rs.getTimestamp("approved_at");
    r.setApprovedAt(ap == null ? null : ap.toLocalDateTime());

    r.setProcessedBy((Integer) (rs.getObject("processed_by") == null ? null : rs.getInt("processed_by")));
    r.setApprovedBy((Integer) (rs.getObject("approved_by")  == null ? null : rs.getInt("approved_by")));

    // để JSP hiển thị tên:
    r.setFullName(rs.getString("full_name")); // từ SELECT u.full_name AS full_name

    return r;
}

    private static String safeGet(ResultSet rs, String col) {
        try { String v = rs.getString(col); return rs.wasNull() ? null : v; }
        catch (SQLException e) { return null; }
    }
    private static Long safeGetLong(ResultSet rs, String col) {
        try { long v = rs.getLong(col); return rs.wasNull() ? null : v; }
        catch (SQLException e) { return null; }
    }

  public int countByFilter(int meId,
                         String myDept,
                         String myRole,
                         LocalDate fromDate,
                         LocalDate toDate,
                         String status,
                         String mine,
                         String keyword) throws SQLException {

    // chuẩn hóa
    String role = (myRole == null) ? "" : myRole.toUpperCase();
    boolean onlyMine = "MINE".equalsIgnoreCase(mine) || "1".equals(mine) || "true".equalsIgnoreCase(mine);

    StringBuilder sql = new StringBuilder();
    sql.append("SELECT COUNT(*) ")
       .append("FROM dbo.Requests r ")
       // bảng thật của bạn: Users.id <-> Requests.user_id
       .append("JOIN dbo.Users u ON u.id = r.user_id ")
       .append("WHERE 1=1 ");

    // danh sách tham số
    java.util.List<Object> params = new java.util.ArrayList<>();

    // ====== QUYỀN XEM ======
    if (!"ADMIN".equals(role)) {
        if (onlyMine) {
            // xem đơn của chính mình
            sql.append(" AND r.user_id = ? ");
            params.add(meId);
        } else {
            // không tick "mine" -> cho xem đơn trong phòng ban mình
            if (myDept != null && !myDept.isBlank()) {
                sql.append(" AND u.department = ? ");
                params.add(myDept);
            } else {
                // fallback: ít nhất vẫn chỉ xem đơn của mình
                sql.append(" AND r.user_id = ? ");
                params.add(meId);
            }
        }
    }
    // ADMIN thì không thêm điều kiện gì

    // ====== Lọc status ======
    if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
        sql.append(" AND r.status = ? ");
        params.add(status.toUpperCase());
    }

    // ====== Lọc theo ngày ======
    // bảng thật của bạn: start_date / end_date
    if (fromDate != null) {
        sql.append(" AND r.start_date >= ? ");
        params.add(java.sql.Date.valueOf(fromDate));
    }
    if (toDate != null) {
        sql.append(" AND r.end_date <= ? ");
        params.add(java.sql.Date.valueOf(toDate));
    }

    // ====== Keyword ======
    if (keyword != null && !keyword.isBlank()) {
        sql.append(" AND (r.reason LIKE ? OR u.full_name LIKE ? OR u.username LIKE ?) ");
        String kw = "%" + keyword.trim() + "%";
        params.add(kw);
        params.add(kw);
        params.add(kw);
    }

    try (Connection cn = DBConnection.getConnection();
         PreparedStatement ps = cn.prepareStatement(sql.toString())) {

        // set tham số
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
    }

    return 0;
}

  // Tùy DB: approve_note nvarchar(500), manager_note nvarchar(1000)
private static final int DEFAULT_NOTE_MAX = 1000;

/** Chuẩn hoá ghi chú: trim, bỏ control char (trừ \n \t), chuẩn hóa \n, cắt độ dài, rỗng -> null */
private String safeNote(String note) {
    return safeNote(note, DEFAULT_NOTE_MAX);
}

/** Bản có tham số độ dài tối đa */
private String safeNote(String note, int maxLen) {
    if (note == null) return null;

    // Chuẩn hóa xuống dòng về \n, giữ tab
    String n = note.replace("\r\n", "\n")
                   .replace('\r', '\n');

    // Loại control chars (giữ lại \n và \t)
    n = n.replaceAll("[\\p{Cntrl}&&[^\\n\\t]]", "");

    // Trim 2 đầu
    n = n.trim();
    if (n.isEmpty()) return null;

    // Cắt độ dài an toàn
    if (maxLen > 0 && n.length() > maxLen) {
        n = n.substring(0, maxLen);
    }
    return n;
}

/** Tạo bản sao đơn: copy type, reason, dates, title, leave_type_id...
 *  user_id/new created_by = newUserId, status = PENDING, các trường duyệt reset về NULL.
 *  Trả về id mới.
 */
/** Nhân bản 1 đơn: copy các trường chính, reset trạng thái về PENDING,
 *  gán user hiện tại làm created_by; KHÔNG copy file đính kèm/ghi chú duyệt.
 *  Trả về id mới.
 */
/** Duplicate 1 đơn: copy fields chính, reset status về PENDING.
 *  KHÔNG đụng vào created_by (vì có thể là computed).
 *  Trả về id mới.
 */
public int duplicateRequest(int sourceId, int newUserId) throws SQLException {
    final String sql = """
        INSERT INTO dbo.Requests
            (user_id, type, status, reason, start_date, end_date,
             created_at, updated_at, processed_by, manager_note,
             leave_type_id, approved_by, approved_at, approve_note, title)
        SELECT
             ?,            -- user_id mới (người thao tác)
             r.type,
             'PENDING',
             r.reason,
             r.start_date, r.end_date,
             SYSDATETIME(),
             SYSDATETIME(),
             NULL,               -- processed_by
             NULL,               -- manager_note
             r.leave_type_id,
             NULL, NULL, NULL,   -- approved_* reset
             r.title
        FROM dbo.Requests r
        WHERE r.id = ?
        """;

    try (var con = getConnection();
         var ps = con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
        ps.setInt(1, newUserId);
        ps.setInt(2, sourceId);
        int n = ps.executeUpdate();
        if (n == 0) throw new SQLException("Duplicate failed, source not found: " + sourceId);
        try (var rs = ps.getGeneratedKeys()) {
            if (rs.next()) return rs.getInt(1);
        }
    }

    // Fallback khi driver không trả generated keys
    try (var con = getConnection();
         var ps = con.prepareStatement("SELECT CAST(SCOPE_IDENTITY() AS INT)")) {
        try (var rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
    }
    throw new SQLException("Cannot retrieve new id after duplicate");
}


}