package com.acme.leavemgmt.dao;

import com.acme.leavemgmt.model.Request;
import com.acme.leavemgmt.model.RequestHistory;
import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.DBConnection;
import static com.acme.leavemgmt.util.DBConnection.getConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.sql.Date;
import java.time.LocalDateTime;

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
WHERE r.user_id = ?            ORDER BY lr.request_id DESC
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

    /**
     * Lấy chi tiết đơn theo id (V2 → V1 → V0)
     */
    /**
     * Lấy chi tiết đơn theo id (V2 → V1 → V0)
     */
    public Request findById(int id) throws SQLException {
        final String sqlV2 = """
        SELECT
            lr.request_id   AS id,
            CAST(NULL AS NVARCHAR(255))  AS title,
            lr.reason,
            lr.from_date    AS start_date,
            lr.to_date      AS end_date,
            lr.status,
            lr.manager_note,
            u_emp.full_name AS created_name,
            lr.employee_id  AS created_by,
            u_app.full_name AS processed_name,
            lr.approver_id  AS processed_by,
            COALESCE(lt.name, lt.code, lr.type) AS leaveTypeName
        FROM [dbo].[Requests] lr
        JOIN [dbo].[Users] u_emp      ON u_emp.user_id = lr.employee_id
        LEFT JOIN [dbo].[Users] u_app ON u_app.user_id = lr.approver_id
        LEFT JOIN [dbo].[LeaveTypes] lt
               ON (lt.id = lr.leave_type_id OR lt.code = lr.type)
        WHERE lr.request_id = ?
    """;

        final String sqlV1 = """
        SELECT
            r.id            AS id,
            CAST(NULL AS NVARCHAR(255))  AS title,
            r.reason,
            r.start_date    AS start_date,
            r.end_date      AS end_date,
            r.status,
            CAST(NULL AS NVARCHAR(400))  AS manager_note,
            u.full_name     AS created_name,
            r.user_id       AS created_by,
            CAST(NULL AS NVARCHAR(200))  AS processed_name,
            CAST(NULL AS INT)            AS processed_by,
            COALESCE(lt.name, lt.code, r.type) AS leaveTypeName
        FROM [dbo].[Requests] r
        JOIN [dbo].[Users] u ON u.id = r.user_id
        LEFT JOIN [dbo].[LeaveTypes] lt
               ON (lt.id = r.leave_type_id OR lt.code = r.type)
        WHERE r.id = ?
    """;

        final String sqlV0 = """
        SELECT
            r.id            AS id,
            CAST(NULL AS NVARCHAR(255))  AS title,   -- bỏ r.title để tránh lỗi
            r.reason,
            r.start_date    AS start_date,
            r.end_date      AS end_date,
            r.status,
            r.manager_note,
            u_emp.full_name AS created_name,
            r.created_by    AS created_by,
            u_app.full_name AS processed_name,
            r.processed_by  AS processed_by,
            COALESCE(lt.name, lt.code, r.type) AS leaveTypeName
        FROM [dbo].[Requests] r
        JOIN [dbo].[Users] u_emp      ON u_emp.id = r.created_by
        LEFT JOIN [dbo].[Users] u_app ON u_app.id = r.processed_by
        LEFT JOIN [dbo].[LeaveTypes] lt
               ON (lt.id = r.leave_type_id OR lt.code = r.type)
        WHERE r.id = ?
    """;

        try (Connection cn = DBConnection.getConnection()) {
            // V2
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
            } catch (SQLException ignoreV2) {
                /* fallback */ }

            // V1
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
            } catch (SQLException ignoreV1) {
                /* fallback */ }

            // V0
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

    private static String safeGetString(ResultSet rs, String col) {
        try {
            return rs.getString(col);
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * Map các cột chung từ ResultSet → Request (không mở thêm kết nối)
     */
    private Request mapRowBasic(ResultSet rs) throws SQLException {
        Request r = new Request();
        r.setId(rs.getInt("id"));

        // cột 'title' có thể NULL (V2/V1), an toàn hóa:
        try {
            String title = rs.getString("title");
            r.setTitle(title);
        } catch (SQLException ignore) {
            r.setTitle(null);
        }

        r.setReason(rs.getString("reason"));

        Date sd = rs.getDate("start_date");
        Date ed = rs.getDate("end_date");
        r.setStartDate(sd != null ? sd.toLocalDate() : null);
        r.setEndDate(ed != null ? ed.toLocalDate() : null);

        r.setStatus(rs.getString("status"));
        r.setManagerNote(rs.getString("manager_note"));

        r.setCreatedBy(rs.getInt("created_by"));
        r.setCreatedByName(rs.getString("created_name"));

        int pb = rs.getInt("processed_by");
        r.setProcessedBy(rs.wasNull() ? null : pb);
        r.setProcessedByName(rs.getString("processed_name"));

        // Nếu bạn có trường đính kèm:
        try {
            r.setAttachmentName(rs.getString("attachment_name"));
        } catch (SQLException ignoreAttachment) {
            /* không có cột này thì bỏ qua */ }

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
        final String sql = """
          UPDATE Requests
          SET status = 'CANCELLED',
              processed_by = ?, 
              manager_note = ?
          WHERE id = ? AND created_by = ? AND status = 'INPROGRESS'
        """;
        // Fallback cho schema V2 (tên cột khác)
        final String sqlV2 = """
          UPDATE Requests
          SET status = 'CANCELLED',
              approver_id = ?, 
              manager_note = ?
          WHERE request_id = ? AND employee_id = ? AND status = 'INPROGRESS'
        """;

        try (Connection cn = DBConnection.getConnection()) {
            cn.setAutoCommit(false);
            int rows;
            // thử cho V0/V1 trước
            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setString(2, note);
                ps.setInt(3, requestId);
                ps.setInt(4, userId);
                rows = ps.executeUpdate();
            } catch (SQLException tryV2) {
                // nếu lỗi cột, thử V2
                try (PreparedStatement ps2 = cn.prepareStatement(sqlV2)) {
                    ps2.setInt(1, userId);
                    ps2.setString(2, note);
                    ps2.setInt(3, requestId);
                    ps2.setInt(4, userId);
                    rows = ps2.executeUpdate();
                }
            }

            if (rows > 0) {
                insertHistory(cn, requestId, userId, userName, "CANCELLED", note);
                cn.commit();
                return true;
            }
            cn.rollback();
            return false;
        }
    }

    // =======================
    // ===== TẠO MỚI (optional)
    // =======================
    /**
     * Tạo đơn mới (tối giản) – trả về id mới. Tùy DB đặt tên cột tương ứng. Nhớ
     * thêm lịch sử CREATED.
     */
   public int createRequest(Request r) throws SQLException {
        final String sql = """
            INSERT INTO dbo.Requests
                (user_id, type, status, reason, start_date, end_date, created_by, created_at)
            OUTPUT INSERTED.id
            VALUES (?, ?, N'PENDING', ?, ?, ?, ?, SYSDATETIME())
        """;

        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            // 1) user_id: người gửi đơn
            ps.setInt(1, r.getUserId());

            // 2) type: nếu form bạn chưa có thì gắn tạm ANNUAL
            String type = (r.getType() == null || r.getType().isBlank()) ? "ANNUAL" : r.getType();
            ps.setString(2, type);

            // 3) reason
            ps.setString(3, r.getReason());

            // 4) start_date
            LocalDate s = r.getStartDate();
            if (s != null) {
                ps.setDate(4, Date.valueOf(s));
            } else {
                ps.setNull(4, Types.DATE);
            }

            // 5) end_date
            LocalDate e = r.getEndDate();
            if (e != null) {
                ps.setDate(5, Date.valueOf(e));
            } else {
                ps.setNull(5, Types.DATE);
            }

            // 6) created_by
            ps.setInt(6, r.getCreatedBy());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int newId = rs.getInt(1);
                    // nếu bạn muốn ghi history thì gọi thêm ở đây
                    insertHistory(cn, newId, r.getCreatedBy(), "CREATED", null);
                    return newId;
                }
            }
        }

        throw new SQLException("Không tạo được request mới.");
    }

    /**
     * Ghi lịch sử request (nếu bạn có bảng history).
     * Nếu bạn CHƯA có bảng này thì bỏ toàn bộ hàm + chỗ gọi đi là được.
     */
    private void insertHistory(Connection cn, int requestId, int actorId,
                               String action, String note) throws SQLException {
        // ví dụ bảng:
        // CREATE TABLE RequestHistory(id INT IDENTITY PK, request_id INT, actor_id INT, action NVARCHAR(50), note NVARCHAR(255), created_at DATETIME2)
        final String sql = """
            INSERT INTO dbo.RequestHistory(request_id, actor_id, action, note, created_at)
            VALUES (?, ?, ?, ?, SYSDATETIME())
        """;
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ps.setInt(2, actorId);
            ps.setString(3, action);
            ps.setString(4, note);
            ps.executeUpdate();
        } catch (SQLException ex) {
            // nếu bạn chưa tạo bảng này thì tạm thời không cần fail
            // có thể log ra:
            // System.err.println("History skipped: " + ex.getMessage());
        }
    }


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
    /**
     * Helper chuyển ResultSet → Request (title không có trong schema → để null)
     */
    private Request mapRow(ResultSet rs) throws SQLException {
        Request r = new Request();
        r.setId(rs.getInt("id"));
        r.setTitle(null);
        r.setReason(rs.getString("reason"));
        r.setStartDate(rs.getDate("start_date").toLocalDate());
        r.setEndDate(rs.getDate("end_date").toLocalDate());
        r.setStatus(rs.getString("status"));
        try {
            r.setManagerNote(rs.getString("manager_note"));
        } catch (SQLException ignore) {
            r.setManagerNote(null);
        }

        Object created = null, processed = null;
        try {
            created = rs.getObject("created_by");
        } catch (SQLException ignore) {
        }
        try {
            processed = rs.getObject("processed_by");
        } catch (SQLException ignore) {
        }

        r.setCreatedBy(created == null ? 0 : ((Number) created).intValue());
        r.setProcessedBy(processed == null ? null : ((Number) processed).intValue());

        return r;
    }
// Trong RequestDAO

    public boolean updateStatusIfPending(int id, String newStatus, int approverId, String note) throws SQLException {
        String sql
                = "UPDATE dbo.Requests "
                + "SET status = ?, approved_by = ?, approve_note = ?, approved_at = GETDATE() "
                + "WHERE id = ? AND status = 'PENDING'";

        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, approverId);
            ps.setString(3, note);
            ps.setInt(4, id);
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

   public boolean isAllowedToApprove(User me, Request reqObj) throws SQLException {
    if (me == null || reqObj == null) {
        return false;
    }

    String role = me.getRole() == null ? "" : me.getRole().trim().toUpperCase();

    // 1️⃣ ADMIN: duyệt tất cả
    if ("ADMIN".equals(role)) {
        return true;
    }

    // 2️⃣ TEAM_LEAD: duyệt nếu mình là manager trực tiếp của người tạo đơn
    if ("TEAM_LEAD".equals(role)) {
        String sql = """
            SELECT 1
            FROM dbo.Requests r
            JOIN dbo.Users u ON u.id = r.user_id   -- user_id là người gửi đơn
            WHERE r.id = ? AND u.manager_id = ?    -- manager_id nằm ở bảng Users
        """;
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, reqObj.getId());
            ps.setInt(2, me.getId());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // 3️⃣ DIV_LEADER: duyệt nếu nhân viên cùng department
    if ("DIV_LEADER".equals(role)) {
        String sql = """
            SELECT 1
            FROM dbo.Requests r
            JOIN dbo.Users u ON u.id = r.user_id
            WHERE r.id = ? AND u.department = ?
        """;
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, reqObj.getId());
            ps.setString(2, me.getDepartment());   // ví dụ 'IT', 'HR', 'SALES'
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // 4️⃣ Mặc định không được duyệt
    return false;
}


    // ĐẾM nhân sự active theo phòng ban (Users.status = 1)
    public int countHeadcountActiveByDept(String dept) throws SQLException {
        final String sql = """
            SELECT COUNT(*)
              FROM dbo.Users
             WHERE status = 1
               AND department = ?
        """;
        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, dept);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    // ĐẾM đơn PENDING theo phòng ban + optional khoảng ngày
    public int countPendingByDept(String dept, LocalDate from, LocalDate to) throws SQLException {
        StringBuilder sb = new StringBuilder("""
            SELECT COUNT(*)
              FROM dbo.Requests r
              LEFT JOIN dbo.Users u ON u.id = r.created_by
             WHERE r.status = 'pending'
               AND COALESCE(r.department, u.department) = ?
        """);
        if (from != null) {
            sb.append(" AND r.start_date >= ? ");
        }
        if (to != null) {
            sb.append(" AND r.end_date   <= ? ");
        }

        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sb.toString())) {
            int i = 1;
            ps.setString(i++, dept);
            if (from != null) {
                ps.setDate(i++, Date.valueOf(from));
            }
            if (to != null) {
                ps.setDate(i++, Date.valueOf(to));
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    // ĐẾM đơn APPROVED trong tháng hiện tại theo phòng
    public int countApprovedThisMonthByDept(String dept) throws SQLException {
        final String sql = """
            SELECT COUNT(*)
              FROM dbo.Requests r
              LEFT JOIN dbo.Users u ON u.id = r.created_by
             WHERE r.status = 'approved'
               AND COALESCE(r.department, u.department) = ?
               AND YEAR(r.approved_at) = YEAR(GETDATE())
               AND MONTH(r.approved_at) = MONTH(GETDATE())
        """;
        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, dept);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    // Tử số: tổng APPROVED theo phòng
    public int countApprovalNumerator(String dept) throws SQLException {
        final String sql = """
            SELECT COUNT(*)
              FROM dbo.Requests r
              LEFT JOIN dbo.Users u ON u.id = r.created_by
             WHERE r.status = 'approved'
               AND COALESCE(r.department, u.department) = ?
        """;
        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, dept);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    // Mẫu số: tổng (APPROVED + REJECTED) theo phòng
    public int countApprovalDenominator(String dept) throws SQLException {
        final String sql = """
            SELECT COUNT(*)
              FROM dbo.Requests r
              LEFT JOIN dbo.Users u ON u.id = r.created_by
             WHERE r.status IN ('approved','rejected')
               AND COALESCE(r.department, u.department) = ?
        """;
        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, dept);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    // Danh sách đơn PENDING (dùng cho bảng “Đơn chờ duyệt”)
    public List<Request> findPendingList(String dept, LocalDate from, LocalDate to) throws SQLException {
        StringBuilder sb = new StringBuilder("""
            SELECT r.id,
                   r.created_by,
                   u.full_name      AS createdByName,
                   r.type,
                   r.reason,
                   r.start_date,
                   r.end_date,
                   r.status,
                   COALESCE(r.department, u.department) AS dept
              FROM dbo.Requests r
              LEFT JOIN dbo.Users u ON u.id = r.created_by
             WHERE r.status = 'pending'
               AND COALESCE(r.department, u.department) = ?
        """);
        if (from != null) {
            sb.append(" AND r.start_date >= ? ");
        }
        if (to != null) {
            sb.append(" AND r.end_date   <= ? ");
        }
        sb.append(" ORDER BY r.created_at DESC ");

        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sb.toString())) {
            int i = 1;
            ps.setString(i++, dept);
            if (from != null) {
                ps.setDate(i++, Date.valueOf(from));
            }
            if (to != null) {
                ps.setDate(i++, Date.valueOf(to));
            }

            try (ResultSet rs = ps.executeQuery()) {
                List<Request> list = new ArrayList<>();
                while (rs.next()) {
                    Request r = new Request();
                    r.setId(rs.getInt("id"));
                    r.setCreatedBy(rs.getInt("created_by"));
                    r.setCreatedByName(rs.getString("createdByName"));
                    r.setType(rs.getString("type"));
                    r.setReason(rs.getString("reason"));
                    Date sd = rs.getDate("start_date");
                    Date ed = rs.getDate("end_date");
                    r.setStartDate(sd != null ? sd.toLocalDate() : null);
                    r.setEndDate(ed != null ? ed.toLocalDate() : null);
                    r.setStatus(rs.getString("status"));
                    r.setDepartment(rs.getString("dept"));
                    list.add(r);
                }
                return list;
            }
        }
    }

    // Danh sách “Đang nghỉ hôm nay” (đã APPROVED & ngày hôm nay nằm trong khoảng)
    public List<Request> findTodayOff(String dept, LocalDate today) throws SQLException {
        final String sql = """
            SELECT r.id,
                   r.created_by,
                   u.full_name      AS createdByName,
                   r.type,
                   r.start_date,
                   r.end_date,
                   COALESCE(r.department, u.department) AS dept
              FROM dbo.Requests r
              LEFT JOIN dbo.Users u ON u.id = r.created_by
             WHERE r.status = 'approved'
               AND COALESCE(r.department, u.department) = ?
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
                    Date sd = rs.getDate("start_date");
                    Date ed = rs.getDate("end_date");
                    r.setStartDate(sd != null ? sd.toLocalDate() : null);
                    r.setEndDate(ed != null ? ed.toLocalDate() : null);
                    r.setDepartment(rs.getString("dept"));
                    r.setStatus("approved");
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

    // Kiểm tra: me có quyền xử lý đơn này không (admin/div leader/manager trực tiếp)
    public boolean canProcessRequest(int requestId, User me) throws SQLException {
        try (Connection cn = DBConnection.getConnection()) {
            // Ví dụ logic:
            // - ADMIN: role_code='ADMIN' -> pass
            // - Leader/Manager: là quản lý trực tiếp của requester HOẶC cùng phòng và có role LEAD
            String sql
                    = "SELECT 1 "
                    + "FROM Requests r "
                    + "JOIN Users u ON u.id = r.created_by "
                    + "WHERE r.id=? AND ("
                    + "  ? IN (SELECT ur.user_id FROM UserRoles ur JOIN Roles ro ON ur.role_id=ro.id WHERE ro.code IN ('ADMIN')) "
                    + "  OR u.manager_id = ? "
                    + "  OR (u.dept_id = (SELECT dept_id FROM Users WHERE id=?) AND "
                    + "      EXISTS (SELECT 1 FROM UserRoles ur JOIN Roles ro ON ur.role_id=ro.id WHERE ur.user_id=? AND ro.code IN ('DIV_LEAD','LEAD')) )"
                    + ")";
            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setInt(1, requestId);
                ps.setInt(2, me.getId());
                ps.setInt(3, me.getId());
                ps.setInt(4, me.getId());
                ps.setInt(5, me.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        }
    }

    // Duyệt/Từ chối
    public boolean processDecision(int requestId, int approverId, boolean approve, String note) throws SQLException {
        String sql
                = "UPDATE Requests "
                + "SET status=?, processed_by=?, processed_at=GETDATE(), decision_note=? "
                + "WHERE id=? AND status='Inprogress'";
        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, approve ? "Approved" : "Rejected");
            ps.setInt(2, approverId);
            ps.setString(3, note);
            ps.setInt(4, requestId);
            return ps.executeUpdate() == 1;
        }
    }

    // === API chính bạn cần ===
    public List<Request> findPendingForApprover(User me) throws SQLException {
        // Phân nhánh theo role
        String role = (me.getRole() == null ? "" : me.getRole().toUpperCase());

        if ("ADMIN".equals(role)) {
            // ADMIN thấy tất cả pending
            String sql = ""
                    + "SELECT r.* , u.department AS requester_department "
                    + "FROM requests r "
                    + "JOIN users u ON u.id = r.requester_id "
                    + "WHERE r.status = 'PENDING' "
                    + "ORDER BY r.created_at DESC";
            return query(sql /* no extra params */);
        }

        if ("MANAGER".equals(role)) {
            // MANAGER: cùng phòng hoặc thuộc cây cấp dưới (đệ quy qua manager_id)
            String sql = ""
                    + "WITH RECURSIVE sub AS ( "
                    + "  SELECT id FROM users WHERE manager_id = ? "
                    + "  UNION ALL "
                    + "  SELECT u.id FROM users u JOIN sub s ON u.manager_id = s.id "
                    + ") "
                    + "SELECT r.* , u.department AS requester_department "
                    + "FROM requests r "
                    + "JOIN users u ON u.id = r.requester_id "
                    + "WHERE r.status = 'PENDING' "
                    + "  AND (u.department = ? OR u.id IN (SELECT id FROM sub)) "
                    + "ORDER BY r.created_at DESC";
            return query(sql, me.getId(), me.getDepartment());
        }

        // APPROVER/USER: chỉ thấy những request được assign cho mình
        String sql = ""
                + "SELECT r.* , u.department AS requester_department "
                + "FROM requests r "
                + "JOIN users u ON u.id = r.requester_id "
                + "WHERE r.status = 'PENDING' "
                + "  AND r.approver_id = ? "
                + "ORDER BY r.created_at DESC";
        return query(sql, me.getId());
    }

    // === Helper query chung (varargs) ===
    private List<Request> query(String sql, Object... params) throws SQLException {
        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {

            // bind params
            for (int i = 0; i < params.length; i++) {
                Object p = params[i];
                if (p instanceof Integer) {
                    ps.setInt(i + 1, (Integer) p);
                } else if (p instanceof Long) {
                    ps.setLong(i + 1, (Long) p);
                } else if (p instanceof String) {
                    ps.setString(i + 1, (String) p);
                } else if (p instanceof java.util.Date) {
                    ps.setTimestamp(i + 1, new Timestamp(((java.util.Date) p).getTime()));
                } else if (p instanceof LocalDate) {
                    ps.setDate(i + 1, Date.valueOf((LocalDate) p));
                } else if (p instanceof LocalDateTime) {
                    ps.setTimestamp(i + 1, Timestamp.valueOf((LocalDateTime) p));
                } else if (p == null) {
                    ps.setObject(i + 1, null);
                } else {
                    ps.setObject(i + 1, p);
                }
            }

            List<Request> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRequest(rs));
                }
            }
            return list;
        }
    }

    // === Mapper: chỉnh theo schema của bạn nếu khác ===
    private Request mapRequest(ResultSet rs) throws SQLException {
        Request r = new Request();
        r.setId((int) rs.getLong("id"));
        r.setRequesterId(rs.getLong("requester_id"));
        r.setApproverId(safeGetLong(rs, "approver_id"));
        r.setType(safeGet(rs, "type"));
        r.setReason(safeGet(rs, "reason"));
        r.setStatus(safeGet(rs, "status"));

        // Ngày từ/đến có thể là DATE hoặc TIMESTAMP
        r.setFrom(rs.getObject("from_date") != null
                ? rs.getDate("from_date").toLocalDate() : null);
        r.setTo(rs.getObject("to_date") != null
                ? rs.getDate("to_date").toLocalDate() : null);

        r.setCreatedAt(rs.getObject("created_at") != null
                ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        r.setApprovedAt(rs.getObject("approved_at") != null
                ? rs.getTimestamp("approved_at").toLocalDateTime() : null);

        // optional: department của requester (do JOIN users u)
        r.setRequesterDepartment(safeGet(rs, "requester_department"));

        return r;
    }

    private static String safeGet(ResultSet rs, String col) {
        try {
            String v = rs.getString(col);
            return rs.wasNull() ? null : v;
        } catch (SQLException e) {
            return null;
        }
    }

    private static Long safeGetLong(ResultSet rs, String col) {
        try {
            long v = rs.getLong(col);
            return rs.wasNull() ? null : v;
        } catch (SQLException e) {
            return null;
        }
    }

}
