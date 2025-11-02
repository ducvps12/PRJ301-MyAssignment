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
import java.util.Date;

@WebServlet(name = "AdminUserEditServlet", urlPatterns = {"/admin/users/edit"})
public class AdminUserEditServlet extends HttpServlet {

    private static final String[] ALLOWED_ROLES = {
            "ADMIN", "HR", "DIV_LEADER"
    };

    private boolean isAllowed(User current) {
        if (current == null) return false;
        String r = current.getRole();
        if (r == null) return false;
        for (String ok : ALLOWED_ROLES) {
            if (ok.equalsIgnoreCase(r)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1. check login
        HttpSession ses = req.getSession(false);
        User current = (ses != null) ? (User) ses.getAttribute("currentUser") : null;
        if (current == null) {
            String back = URLEncoder.encode(
                    req.getRequestURI() +
                            (req.getQueryString() != null ? "?" + req.getQueryString() : ""),
                    StandardCharsets.UTF_8
            );
            resp.sendRedirect(req.getContextPath() + "/login?redirect=" + back);
            return;
        }

        // 2. check quyền
        if (!isAllowed(current)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền sửa người dùng");
            return;
        }

        // 3. id
        String sid = req.getParameter("id");
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

        // 4. load user
        User u = null;
        try (Connection c = DBConnection.getConnection()) {

            PreparedStatement ps = c.prepareStatement(
                    // viết SQL rõ ràng, KHÔNG có dấu --
                    "SELECT id, username, full_name, email, role, department, status, " +
                            "last_login, last_ip " +
                            "FROM Users WHERE id = ?"
            );
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                u = new User();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                u.setFullName(rs.getString("full_name"));
                u.setEmail(rs.getString("email"));
                u.setRole(rs.getString("role"));
                u.setDepartment(rs.getString("department"));

                // status trong DB là int 0/1
                int st = rs.getInt("status");
                u.setStatus(st); // <-- model của bạn nhận int

                // last_login
                Timestamp ts = rs.getTimestamp("last_login");
                if (ts != null) {
                    u.setLastLogin(new Date(ts.getTime()));
                }

                // last_ip
                String lastIp = rs.getString("last_ip");
                if (lastIp != null) {
                    u.setLastIp(lastIp);
                }

            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy user");
                return;
            }

        } catch (SQLException e) {
            throw new ServletException(e);
        }

        // 5. đẩy sang JSP
        req.setAttribute("u", u);
        req.setAttribute("roles", new String[]{
                "ADMIN", "DIV_LEADER", "TEAM_LEAD", "QA_LEAD", "STAFF", "INTERN", "PROBATION"
        });
        req.setAttribute("departments", new String[]{
                "HR", "IT", "FINANCE", "MARKETING", "SALES", "OPERATION", "BOARD"
        });
        req.setAttribute("statuses", new String[]{
                "ACTIVE", "INACTIVE"
        });

        // CSRF
        Csrf.protect(req);

        req.getRequestDispatcher("/WEB-INF/views/admin/user_edit.jsp")
                .forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

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

        if (!Csrf.verify(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF token không hợp lệ");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(req.getParameter("id"));
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "id không hợp lệ");
            return;
        }

        String action = req.getParameter("action");
        if (action == null || action.isBlank()) {
            action = "update";
        }

        try (Connection c = DBConnection.getConnection()) {

            if ("update".equalsIgnoreCase(action)) {
                String fullName = req.getParameter("fullName");
                String email = req.getParameter("email");
                String role = req.getParameter("role");
                String dept = req.getParameter("department");
                String status = req.getParameter("status"); // ACTIVE / INACTIVE / 1 / 0

                int st = 1;
                if (status != null) {
                    if ("0".equals(status) || "INACTIVE".equalsIgnoreCase(status)) {
                        st = 0;
                    }
                }

                PreparedStatement ps = c.prepareStatement(
                        "UPDATE Users SET full_name = ?, email = ?, role = ?, department = ?, status = ? " +
                                "WHERE id = ?"
                );
                ps.setString(1, fullName);
                ps.setString(2, email);
                ps.setString(3, role);
                ps.setString(4, dept);
                ps.setInt(5, st);
                ps.setInt(6, id);
                ps.executeUpdate();

                AuditLog.log(current.getId(), "ADMIN_USER_UPDATE",
                        "Cập nhật user id=" + id + " -> " + fullName);

            } else if ("toggle".equalsIgnoreCase(action)) {
                // bật/tắt user
                PreparedStatement psSel = c.prepareStatement("SELECT status FROM Users WHERE id=?");
                psSel.setInt(1, id);
                ResultSet rs = psSel.executeQuery();
                if (!rs.next()) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy user");
                    return;
                }
                int cur = rs.getInt("status");
                int next = (cur == 1) ? 0 : 1;

                PreparedStatement psUp = c.prepareStatement("UPDATE Users SET status=? WHERE id=?");
                psUp.setInt(1, next);
                psUp.setInt(2, id);
                psUp.executeUpdate();

                AuditLog.log(current.getId(), "ADMIN_USER_TOGGLE",
                        "Đổi trạng thái user id=" + id + " từ " + cur + " -> " + next);

            } else if ("resetpw".equalsIgnoreCase(action)) {
                // reset mật khẩu
                PreparedStatement ps = c.prepareStatement(
                        "UPDATE Users SET password = ? WHERE id = ?"
                );
                ps.setString(1, "123456"); // TODO: đổi thành hash
                ps.setInt(2, id);
                ps.executeUpdate();

                AuditLog.log(current.getId(), "ADMIN_USER_RESETPW",
                        "Reset mật khẩu user id=" + id);
            }

        } catch (SQLException e) {
            throw new ServletException(e);
        }

        resp.sendRedirect(req.getContextPath() + "/admin/users?updated=1");
    }
}
