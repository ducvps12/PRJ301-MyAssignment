package com.acme.leavemgmt.servlet.admin;

import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.AuditLog;
import com.acme.leavemgmt.util.Csrf;
import com.acme.leavemgmt.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "AdminUserCreateServlet", urlPatterns = {"/admin/users/create"})
public class AdminUserCreateServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1) check login + quyền
        HttpSession ses = req.getSession(false);
        User me = (ses == null) ? null : (User) ses.getAttribute("currentUser");
        if (me == null || !isAdmin(me)) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // 2) CSRF cho form
        String csrf = Csrf.ensureToken(req.getSession());
        req.setAttribute("csrf", csrf);

        // 3) mode = create
        req.setAttribute("mode", "create");

        // 4) forward ra form
        req.getRequestDispatcher("/WEB-INF/views/admin/user_form.jsp")
                .forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1) check login
        HttpSession ses = req.getSession(false);
        User me = (ses == null) ? null : (User) ses.getAttribute("currentUser");
        if (me == null || !isAdmin(me)) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // 2) check CSRF
        if (!Csrf.verifyToken(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF token invalid");
            return;
        }

        // 3) lấy data từ form
        String fullName  = n(req.getParameter("full_name"));
        String email     = n(req.getParameter("email"));
        String username  = n(req.getParameter("username"));
        String password  = n(req.getParameter("password"));
        String role      = n(req.getParameter("role"));
        String department= n(req.getParameter("department"));
        String status    = n(req.getParameter("status"));

        // 4) validate
        List<String> errs = new ArrayList<>();
        if (fullName.isEmpty())  errs.add("Họ tên không được để trống");
        if (username.isEmpty())  errs.add("Username không được để trống");
        if (password.isEmpty())  errs.add("Mật khẩu không được để trống");
        if (role.isEmpty())      errs.add("Role không được để trống");
        if (department.isEmpty())errs.add("Phòng ban không được để trống");
        if (status.isEmpty())    status = "ACTIVE";   // default

        // 5) check trùng username / email
        try (Connection cn = DBConnection.getConnection()) {
            // check username
            try (PreparedStatement ps = cn.prepareStatement(
                    "SELECT 1 FROM dbo.Users WHERE username = ?")) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        errs.add("Username đã tồn tại");
                    }
                }
            }
            // check email (cho phép rỗng, chỉ check khi có nhập)
            if (!email.isEmpty()) {
                try (PreparedStatement ps = cn.prepareStatement(
                        "SELECT 1 FROM dbo.Users WHERE email = ?")) {
                    ps.setString(1, email);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            errs.add("Email đã tồn tại");
                        }
                    }
                }
            }

            if (!errs.isEmpty()) {
                // đưa lại dữ liệu cũ + lỗi
                req.setAttribute("errs", errs);
                req.setAttribute("mode", "create");
                req.setAttribute("csrf", Csrf.ensureToken(req.getSession()));
                // giữ lại giá trị
                req.setAttribute("f_full_name", fullName);
                req.setAttribute("f_email", email);
                req.setAttribute("f_username", username);
                req.setAttribute("f_role", role);
                req.setAttribute("f_department", department);
                req.setAttribute("f_status", status);
                req.getRequestDispatcher("/WEB-INF/views/admin/user_form.jsp")
                        .forward(req, resp);
                return;
            }

            // 6) insert
            String sql = """
                    INSERT INTO dbo.Users
                        (full_name, email, username, password, role, department, status, created_at, created_by)
                    VALUES (?,?,?,?,?,?,?,SYSDATETIME(),?)
                    """;
            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setString(1, fullName);
                ps.setString(2, email.isEmpty() ? null : email);
                ps.setString(3, username);
                ps.setString(4, encodePassword(password));
                ps.setString(5, role);
                ps.setString(6, department);
                ps.setString(7, status);
                ps.setInt(8, me.getId());
                ps.executeUpdate();
            }

            // 7) audit
            try {
                AuditLog.log(req,
                        "ADMIN_USERS_CREATE",
                        "USER",
                        me.getId(),
                        "username=" + username);
            } catch (Throwable ignored) {}

        } catch (SQLException e) {
            throw new ServletException(e);
        }

        // 8) về lại list
        resp.sendRedirect(req.getContextPath() + "/admin/users?msg=created");
    }

    /* ===== helper ===== */
    private static boolean isAdmin(User u) {
        if (u == null) return false;
        String r = u.getRole();
        return r != null && (r.equalsIgnoreCase("ADMIN") || r.equalsIgnoreCase("HR"));
    }

    private static String n(String s) {
        return s == null ? "" : s.trim();
    }

    // nếu hệ thống của em đã có mã hoá rồi thì thay đoạn này bằng hàm đó
    private static String encodePassword(String raw) {
        // TODO: thay bằng bcrypt / sha-256 nếu có
        return raw;
    }
}
