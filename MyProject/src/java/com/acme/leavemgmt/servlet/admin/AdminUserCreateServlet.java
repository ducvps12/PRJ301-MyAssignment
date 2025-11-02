package com.acme.leavemgmt.servlet.admin;

import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.AuditLog;
import com.acme.leavemgmt.util.Csrf;
import com.acme.leavemgmt.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "AdminUserCreateServlet", urlPatterns = {"/admin/users/create"})
public class AdminUserCreateServlet extends HttpServlet {

    // những role được phép tạo user
    private static final String[] ALLOWED_ROLES = {
            "ADMIN",
            "HR",          // nếu DB của bạn dùng HR_ADMIN thì đổi dòng này
            "DIV_LEADER"
    };

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession ses = req.getSession(false);
        User me = (ses == null) ? null : (User) ses.getAttribute("currentUser");
        if (me == null || !canCreate(me)) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        req.setAttribute("csrf", Csrf.ensureToken(req.getSession()));
        req.setAttribute("mode", "create");
        req.getRequestDispatcher("/WEB-INF/views/admin/user_form.jsp")
                .forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1) check login + quyền
        HttpSession ses = req.getSession(false);
        User me = (ses == null) ? null : (User) ses.getAttribute("currentUser");
        if (me == null || !canCreate(me)) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // 2) check CSRF
        if (!Csrf.verifyToken(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF token invalid");
            return;
        }

        // 3) lấy data từ form
        String fullName   = n(req.getParameter("full_name"));
        String email      = n(req.getParameter("email"));
        String username   = n(req.getParameter("username"));
        String password   = n(req.getParameter("password"));
        String role       = n(req.getParameter("role"));
        String department = n(req.getParameter("department"));
        String statusStr  = n(req.getParameter("status"));
        if (statusStr.isEmpty()) {
            statusStr = "ACTIVE";
        }
        int status = mapStatusToTinyInt(statusStr);   // <-- quan trọng

        // 4) validate
        List<String> errs = new ArrayList<>();
        if (fullName.isEmpty())   errs.add("Họ tên không được để trống");
        if (username.isEmpty())   errs.add("Username không được để trống");
        if (password.isEmpty())   errs.add("Mật khẩu không được để trống");
        if (role.isEmpty())       errs.add("Role không được để trống");
        if (department.isEmpty()) errs.add("Phòng ban không được để trống");

        try (Connection cn = DBConnection.getConnection()) {

            // check username trùng
            try (PreparedStatement ps = cn.prepareStatement(
                    "SELECT 1 FROM dbo.Users WHERE username = ?"
            )) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        errs.add("Username đã tồn tại");
                    }
                }
            }

            // check email trùng
            if (!email.isEmpty()) {
                try (PreparedStatement ps = cn.prepareStatement(
                        "SELECT 1 FROM dbo.Users WHERE email = ?"
                )) {
                    ps.setString(1, email);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            errs.add("Email đã tồn tại");
                        }
                    }
                }
            }

            // nếu có lỗi -> trả lại form
            if (!errs.isEmpty()) {
                pushBackForm(req, errs, fullName, email, username, role, department, statusStr);
                req.getRequestDispatcher("/WEB-INF/views/admin/user_form.jsp")
                        .forward(req, resp);
                return;
            }

            // 6) insert
            // CHÚ Ý: trong DB gốc của bạn cột status là tinyint,
            // và bạn đã ALTER thêm cột created_by
            String sql = """
                    INSERT INTO dbo.Users
                        (full_name, email, username, password, role, department, status, created_at, created_by)
                    VALUES (?,?,?,?,?,?,?,?,?)
                    """;

            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setString(1, fullName);
                ps.setString(2, email.isEmpty() ? null : email);
                ps.setString(3, username);
                ps.setString(4, encodePassword(password));
                ps.setString(5, role);
                ps.setString(6, department);
                ps.setInt(7, status); // <-- giờ là int, không còn lỗi convert
                ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
                ps.setInt(9, me.getId());
                ps.executeUpdate();
            }

            // 7) audit
            try {
                AuditLog.log(
                        req,
                        "ADMIN_USERS_CREATE",
                        "USER",
                        me.getId(),
                        "username=" + username
                );
            } catch (Throwable ignored) {
            }

        } catch (SQLException e) {
            throw new ServletException(e);
        }

        // 8) redirect
        resp.sendRedirect(req.getContextPath() + "/admin/users?msg=created");
    }

    /* ========== helper ========== */

    // cho ADMIN, HR, DIV_LEADER được tạo
    private static boolean canCreate(User u) {
        if (u == null) return false;
        String r = u.getRole();
        if (r == null) return false;
        for (String ok : ALLOWED_ROLES) {
            if (r.equalsIgnoreCase(ok)) return true;
        }
        return false;
    }

    private static String n(String s) {
        return s == null ? "" : s.trim();
    }

    // map chuỗi từ form -> tinyint trong DB
    private static int mapStatusToTinyInt(String s) {
        if (s == null) return 1;
        s = s.trim().toUpperCase();
        return switch (s) {
            case "INACTIVE", "0" -> 0;
            case "SUSPENDED" -> 2;
            case "TERMINATED" -> 3;
            default -> 1; // ACTIVE
        };
    }

    // chỗ này bạn thay bằng hàm băm thực tế
    private static String encodePassword(String raw) {
        return raw; // TODO: BCrypt / SHA-256 ...
    }

    // đổ lại form khi có lỗi
    private static void pushBackForm(HttpServletRequest req,
                                     List<String> errs,
                                     String fullName,
                                     String email,
                                     String username,
                                     String role,
                                     String dept,
                                     String status) {

        req.setAttribute("errs", errs);
        req.setAttribute("mode", "create");
        req.setAttribute("csrf", Csrf.ensureToken(req.getSession()));

        req.setAttribute("f_full_name", fullName);
        req.setAttribute("f_email", email);
        req.setAttribute("f_username", username);
        req.setAttribute("f_role", role);
        req.setAttribute("f_department", dept);
        req.setAttribute("f_status", status);
    }
}
