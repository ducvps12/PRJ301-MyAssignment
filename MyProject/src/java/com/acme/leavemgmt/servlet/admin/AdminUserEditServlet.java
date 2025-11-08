package com.acme.leavemgmt.servlet.admin;

import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.AuditLog;
import com.acme.leavemgmt.util.Csrf;
import com.acme.leavemgmt.util.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@WebServlet(name = "AdminUserEditServlet", urlPatterns = {"/admin/users/edit"})
public class AdminUserEditServlet extends HttpServlet {

    /* ===== Roles allowed to edit users ===== */
    private static final Set<String> ALLOWED = new HashSet<>(Arrays.asList(
        "ADMIN", "HR", "DIV_LEADER"
    ));

    private static boolean isAllowed(User cur) {
        if (cur == null) return false;

        // Prefer text role; fallback to code/id if your model populates those.
        String r = null;
        if (cur.getRole() != null && !cur.getRole().isBlank()) {
            r = cur.getRole();
        } else if (cur.getRoleCode() != null && !cur.getRoleCode().isBlank()) {
            r = cur.getRoleCode();
        }
        if (r != null && ALLOWED.contains(r.trim().toUpperCase())) return true;

        Integer rid = cur.getRoleId();            // 1=ADMIN, 2=HR, 3=DIV_LEADER (your convention)
        return rid != null && (rid == 1 || rid == 2 || rid == 3);
    }

    /* ===== Small helpers ===== */
    private static String p(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v == null ? null : v.trim();
    }

    private static int normalizeStatus(String statusRaw) {
        if (statusRaw == null) return 1;
        String s = statusRaw.trim().toUpperCase();
        if ("0".equals(s) || "INACTIVE".equals(s) || "FALSE".equals(s) || "OFF".equals(s)) return 0;
        return 1;
    }

    private static void noStore(HttpServletResponse w) {
        w.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        w.setHeader("Pragma", "no-cache");
        w.setDateHeader("Expires", 0);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1) Must login
        HttpSession ses = req.getSession(false);
        User current = (ses != null) ? (User) ses.getAttribute("currentUser") : null;
        if (current == null) {
            String back = URLEncoder.encode(
                    req.getRequestURI() + (req.getQueryString() != null ? "?" + req.getQueryString() : ""),
                    StandardCharsets.UTF_8
            );
            resp.sendRedirect(req.getContextPath() + "/login?redirect=" + back);
            return;
        }

        // 2) Permission
        if (!isAllowed(current)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền sửa người dùng");
            return;
        }

        // 3) Parse id
        String sid = p(req, "id");
        if (sid == null || sid.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu id");
            return;
        }
        int id;
        try {
            id = Integer.parseInt(sid);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "id không hợp lệ");
            return;
        }

        // 4) Load user
        User u;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT id, username, full_name, email, role, department, status, last_login, last_ip " +
                 "FROM Users WHERE id = ?")) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy user");
                    return;
                }
                u = new User();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                u.setFullName(rs.getString("full_name"));
                u.setEmail(rs.getString("email"));
                u.setRole(rs.getString("role"));
                u.setDepartment(rs.getString("department"));
                u.setStatus(rs.getInt("status"));
                Timestamp ts = rs.getTimestamp("last_login");
                if (ts != null) u.setLastLogin(new Date(ts.getTime()));
                u.setLastIp(rs.getString("last_ip"));
            }

        } catch (SQLException e) {
            throw new ServletException("DB error load user", e);
        }

        // 5) Push to view
        req.setAttribute("u", u);
        req.setAttribute("roles", new String[]{
            "ADMIN", "DIV_LEADER", "TEAM_LEAD", "QA_LEAD", "STAFF", "INTERN", "PROBATION"
        });
        req.setAttribute("departments", new String[]{
            "HR", "IT", "FINANCE", "MARKETING", "SALES", "OPERATION", "BOARD"
        });
        req.setAttribute("statuses", new String[]{"ACTIVE", "INACTIVE"});

        // CSRF for the form
        Csrf.protect(req);

        noStore(resp);
        req.getRequestDispatcher("/WEB-INF/views/admin/user_edit.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        noStore(resp);

        HttpSession ses = req.getSession(false);
        User current = (ses != null) ? (User) ses.getAttribute("currentUser") : null;
        if (current == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        if (!isAllowed(current)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền");
            return;
        }

        // CSRF (accept both header & parameter as implemented in Csrf.verify)
        if (!Csrf.verify(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF token không hợp lệ");
            return;
        }

        final int id;
        try {
            id = Integer.parseInt(p(req, "id"));
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "id không hợp lệ");
            return;
        }

        final String action = (p(req, "action") == null || p(req, "action").isBlank())
            ? "update" : p(req, "action");

        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                if ("update".equalsIgnoreCase(action)) {

                    final String fullName = p(req, "fullName");
                    final String email    = p(req, "email");
                    final String role     = p(req, "role");
                    final String dept     = p(req, "department");
                    final int st          = normalizeStatus(p(req, "status"));

                    if (fullName == null || fullName.isBlank())
                        throw new ServletException("Họ tên bắt buộc");
                    if (email == null || !email.contains("@"))
                        throw new ServletException("Email không hợp lệ");

                    // Optional rule: do not allow an admin to demote themselves out of ADMIN.
                    int curId = current.getId();
if (curId == id) {
    String curRole = current.getRole() != null ? current.getRole() : current.getRoleCode();
    if (curRole != null && curRole.equalsIgnoreCase("ADMIN") && !"ADMIN".equalsIgnoreCase(role)) {
        throw new ServletException("Không thể tự hạ vai trò của chính bạn khỏi ADMIN.");
    }
}


                    try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE Users SET full_name=?, email=?, role=?, department=?, status=? WHERE id=?")) {
                        ps.setString(1, fullName);
                        ps.setString(2, email);
                        ps.setString(3, role);
                        ps.setString(4, dept);
                        ps.setInt(5, st);
                        ps.setInt(6, id);
                        int n = ps.executeUpdate();
                        if (n != 1) throw new ServletException("Update không thành công (n=" + n + ")");
                    }

                    AuditLog.log(current.getId(), "ADMIN_USER_UPDATE",
                            "Cập nhật user id=" + id + " (role=" + role + ", dept=" + dept + ", status=" + st + ")");

                } else if ("toggle".equalsIgnoreCase(action)) {

                    int cur;
                    try (PreparedStatement psSel = c.prepareStatement("SELECT status FROM Users WHERE id=?")) {
                        psSel.setInt(1, id);
                        try (ResultSet rs = psSel.executeQuery()) {
                            if (!rs.next()) throw new ServletException("Không tìm thấy user");
                            cur = rs.getInt(1);
                        }
                    }
                    int next = (cur == 1) ? 0 : 1;

                    try (PreparedStatement psUp = c.prepareStatement("UPDATE Users SET status=? WHERE id=?")) {
                        psUp.setInt(1, next);
                        psUp.setInt(2, id);
                        psUp.executeUpdate();
                    }

                    AuditLog.log(current.getId(), "ADMIN_USER_TOGGLE",
                            "Đổi trạng thái user id=" + id + " từ " + cur + " -> " + next);

                } else if ("resetpw".equalsIgnoreCase(action)) {

                    try (PreparedStatement ps = c.prepareStatement("UPDATE Users SET password=? WHERE id=?")) {
                        // TODO: Replace by PBKDF2/BCrypt hash
                        ps.setString(1, "123456");
                        ps.setInt(2, id);
                        ps.executeUpdate();
                    }

                    AuditLog.log(current.getId(), "ADMIN_USER_RESETPW",
                            "Reset mật khẩu user id=" + id);

                } else {
                    throw new ServletException("Action không hỗ trợ: " + action);
                }

                c.commit();
            } catch (Exception ex) {
                c.rollback();
                if (ex instanceof ServletException) throw (ServletException) ex;
                throw new ServletException(ex);
            } finally {
                c.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ServletException("DB error update user", e);
        }

        // Back to list with a flash flag, or stay on edit page if you prefer:
        // resp.sendRedirect(req.getContextPath() + "/admin/users/edit?id=" + id + "&updated=1");
        resp.sendRedirect(req.getContextPath() + "/admin/users?updated=1");
    }
}
