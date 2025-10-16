package com.acme.leavemgmt.dao;

import com.acme.leavemgmt.model.Request;
import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/**
 * RequestDAO - Data Access Layer cho các nghiệp vụ User & Request
 * ----------------------------------------------
 * Các nhóm chức năng chính:
 *  - USER: đăng nhập, lọc theo phòng ban
 *  - REQUEST: tạo đơn, xem đơn, duyệt đơn, lấy danh sách nghỉ phép đã duyệt
 */
public class RequestDAO {

    // =====================================================
    // =============== USER FUNCTIONS =======================
    // =====================================================

    /** Đăng nhập và trả về thông tin User nếu hợp lệ */
    public User findByUsernameAndPassword(String username, String password) throws SQLException {
        String sql = """
            SELECT id, username, full_name, role, department
            FROM Users
            WHERE username = ? AND password = ?
        """;

        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("full_name"),
                        rs.getString("role"),
                        rs.getString("department")
                    );
                }
            }
        }
        return null;
    }

    /** Lấy danh sách user cùng phòng ban */
    public List<User> listUsersByDepartment(String department) throws SQLException {
        String sql = """
            SELECT id, username, full_name, role, department
            FROM Users
            WHERE department = ?
        """;

        List<User> list = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, department);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User u = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("full_name"),
                        rs.getString("role"),
                        rs.getString("department")
                    );
                    list.add(u);
                }
            }
        }
        return list;
    }

    // =====================================================
    // =============== REQUEST FUNCTIONS ===================
    // =====================================================

    /** Tạo đơn nghỉ phép mới */
    public void createRequest(Request r) throws SQLException {
        String sql = """
            INSERT INTO Requests(title, reason, start_date, end_date, status, created_by)
            VALUES(?,?,?,?, 'INPROGRESS', ?)
        """;

        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, r.getTitle());
            ps.setString(2, r.getReason());
            ps.setDate(3, java.sql.Date.valueOf(r.getStartDate()));
            ps.setDate(4, java.sql.Date.valueOf(r.getEndDate()));
            ps.setInt(5, r.getCreatedBy());

            ps.executeUpdate();
        }
    }

    /** Danh sách đơn nghỉ của chính user */
    public List<Request> listMyRequests(int userId) throws SQLException {
        String sql = """
            SELECT r.id, r.title, r.reason, r.start_date, r.end_date, r.status,
                   u1.full_name AS created_name,
                   u2.full_name AS processed_name, r.processed_by, r.manager_note
            FROM Requests r
            JOIN Users u1 ON u1.id = r.created_by
            LEFT JOIN Users u2 ON u2.id = r.processed_by
            WHERE r.created_by = ?
            ORDER BY r.id DESC
        """;

        List<Request> list = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Danh sách đơn của cấp dưới (theo phòng ban) */
    public List<Request> listSubordinateRequests(String department) throws SQLException {
        String sql = """
            SELECT r.id, r.title, r.reason, r.start_date, r.end_date, r.status,
                   u1.full_name AS created_name, u1.id AS created_by,
                   u2.full_name AS processed_name, r.processed_by, r.manager_note
            FROM Requests r
            JOIN Users u1 ON u1.id = r.created_by
            LEFT JOIN Users u2 ON u2.id = r.processed_by
            WHERE u1.department = ?
            ORDER BY r.id DESC
        """;

        List<Request> list = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, department);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Request r = mapRow(rs);
                    r.setCreatedBy(rs.getInt("created_by"));
                    list.add(r);
                }
            }
        }
        return list;
    }

    /** Lấy chi tiết 1 đơn theo id */
    public Request findById(int id) throws SQLException {
        String sql = """
            SELECT r.id, r.title, r.reason, r.start_date, r.end_date, r.status,
                   u1.full_name AS created_name, u1.id AS created_by,
                   u2.full_name AS processed_name, r.processed_by, r.manager_note
            FROM Requests r
            JOIN Users u1 ON u1.id = r.created_by
            LEFT JOIN Users u2 ON u2.id = r.processed_by
            WHERE r.id = ?
        """;

        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Request r = mapRow(rs);
                    r.setCreatedBy(rs.getInt("created_by"));
                    return r;
                }
            }
        }
        return null;
    }

    /** Quản lý duyệt hoặc từ chối đơn */
    public void processRequest(int id, int managerId, String status, String note) throws SQLException {
        String sql = "UPDATE Requests SET status = ?, processed_by = ?, manager_note = ? WHERE id = ?";

        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, status); // APPROVED / REJECTED
            ps.setInt(2, managerId);
            ps.setString(3, note);
            ps.setInt(4, id);

            ps.executeUpdate();
        }
    }

    /** Dành cho agenda: trả về Map<userId, Set<LocalDate>> chứa các ngày nghỉ đã duyệt */
    public Map<Integer, Set<LocalDate>> getApprovedAbsences(String department,
                                                            LocalDate from,
                                                            LocalDate to) throws SQLException {
        final String sql = """
            SELECT r.start_date, r.end_date, r.created_by AS uid
            FROM Requests r
            JOIN Users u ON u.id = r.created_by
            WHERE r.status = N'APPROVED'
              AND u.department = ?
              AND r.end_date >= ? AND r.start_date <= ?
        """;

        Map<Integer, Set<LocalDate>> map = new HashMap<>();

        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, department);
            ps.setDate(2, java.sql.Date.valueOf(from));
            ps.setDate(3, java.sql.Date.valueOf(to));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate start = rs.getDate("start_date").toLocalDate();
                    LocalDate end = rs.getDate("end_date").toLocalDate();
                    int uid = rs.getInt("uid");

                    Set<LocalDate> days = map.computeIfAbsent(uid, k -> new HashSet<>());
                    for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
                        if (!d.isBefore(from) && !d.isAfter(to)) {
                            days.add(d);
                        }
                    }
                }
            }
        }
        return map;
    }

    // =====================================================
    // =============== MAPPING HELPER =======================
    // =====================================================

    /** Helper chuyển ResultSet → Request object */
    private Request mapRow(ResultSet rs) throws SQLException {
        Request r = new Request();
        r.setId(rs.getInt("id"));
        r.setTitle(rs.getString("title"));
        r.setReason(rs.getString("reason"));
        r.setStartDate(rs.getDate("start_date").toLocalDate());
        r.setEndDate(rs.getDate("end_date").toLocalDate());
        r.setStatus(rs.getString("status"));
        r.setManagerNote(rs.getString("manager_note"));
        r.setCreatedBy(rs.getInt("created_by"));

        Object p = rs.getObject("processed_by");
        r.setProcessedBy(p == null ? null : ((Number) p).intValue());

        return r;
    }
}
