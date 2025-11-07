package com.acme.leavemgmt.web.auth;

import com.acme.leavemgmt.dao.UserDAO;
import com.acme.leavemgmt.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.UUID;

// Nếu đã cấu hình trong web.xml thì giữ nguyên; nếu chưa có thì mở dòng dưới:
// @WebServlet(urlPatterns = {"/login"})
public class AuthServlet extends HttpServlet {

    private static final String VIEW_LOGIN = "/WEB-INF/views/auth/login.jsp";
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession s = req.getSession(false);

        // Logout: /login?logout=1
        if ("1".equals(req.getParameter("logout"))) {
            if (s != null) s.invalidate();
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Đã đăng nhập -> điều hướng theo role
        if (s != null && s.getAttribute("currentUser") != null) {
            User cu = (User) s.getAttribute("currentUser");
            String fallback = req.getContextPath()
                    + (isOffboardingRole(cu != null ? cu.getRole() : null)
                       ? "/user/home"
                       : "/request/list");
            resp.sendRedirect(fallback);
            return;
        }

        // CSRF token cho form
        ensureCsrfToken(req.getSession(true));

        // Giữ lại tham số next nếu có
        String next = req.getParameter("next");
        if (next != null && !next.isBlank()) {
            req.setAttribute("next", next);
        }

        req.getRequestDispatcher(VIEW_LOGIN).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // 1) CSRF
        if (!validateCsrf(req)) {
            req.setAttribute("error", "Phiên không hợp lệ. Vui lòng thử lại.");
            req.getRequestDispatcher(VIEW_LOGIN).forward(req, resp);
            return;
        }

        // 2) Lấy input
        String username = trim(req.getParameter("username"));
        String password = trim(req.getParameter("password"));
        String next     = trim(req.getParameter("next"));

        if (username.isEmpty() || password.isEmpty()) {
            req.setAttribute("error", "Vui lòng nhập đầy đủ tài khoản và mật khẩu.");
            req.setAttribute("username", username);
            req.setAttribute("next", next);
            req.getRequestDispatcher(VIEW_LOGIN).forward(req, resp);
            return;
        }

        try {
            // 3) Xác thực (TODO: chuyển sang BCrypt/Argon2)
            User u = userDAO.findByUsernameAndPassword(username, password);

            if (isLoginAllowed(u)) {
                // 4) Chống session fixation
                HttpSession old = req.getSession(false);
                if (old != null) old.invalidate();
                HttpSession s = req.getSession(true);
                try { req.changeSessionId(); } catch (Throwable ignore) {}

                // 5) Lưu user vào session (đúng key mà RoleFilter dùng)
                s.setAttribute("currentUser", u);
                s.setAttribute("userId", u.getId());
                s.setAttribute("fullName", u.getFullName());
                s.setAttribute("role", u.getRole());
                s.setAttribute("department", u.getDepartment());

                // 6) Điều hướng an toàn theo role (tôn trọng 'next' nếu hợp lệ)
                String fallback = req.getContextPath()
                        + (isOffboardingRole(u.getRole()) ? "/user/home" : "/request/list");
                String target = safeNext(req, next, fallback);
                resp.sendRedirect(target);
                return;
            }

            // Sai TK/MK hoặc không được phép đăng nhập
            req.setAttribute("error", "Sai tài khoản hoặc mật khẩu.");
            req.setAttribute("username", username);
            req.setAttribute("next", next);
            req.getRequestDispatcher(VIEW_LOGIN).forward(req, resp);

        } catch (SQLException e) {
            throw new ServletException("Database error during login", e);
        }
    }

    // ===== Helpers =====

    private static String trim(String s) {
        return (s == null) ? "" : s.trim();
    }

    private void ensureCsrfToken(HttpSession session) {
        if (session.getAttribute("_csrf") == null) {
            session.setAttribute("_csrf", UUID.randomUUID().toString());
        }
    }

    private boolean validateCsrf(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) return false;
        Object token = s.getAttribute("_csrf");
        String formToken = req.getParameter("_csrf");
        return token != null && token.equals(formToken);
    }

    /** Cho phép đăng nhập khi user tồn tại, active và không thuộc nhóm bị khóa hẳn. */
    private boolean isLoginAllowed(User u) {
        if (u == null) return false;
        // isActive() của bạn có thể đã cover status; ta vẫn chặn thêm các role "khóa hẳn"
        if (!u.isActive()) return false;
        String role = u.getRole();
        // SUSPENDED & TERMINATED: không cho login
        if ("SUSPENDED".equals(role) || "TERMINATED".equals(role)) return false;
        return true;
    }

    /** Nhóm role “hạn chế” → rơi về /user/home thay vì /request/list */
    private boolean isOffboardingRole(String role) {
        if (role == null) return false;
        switch (role) {
            case "OFFBOARDING":
            case "UNDER_REVIEW":
            case "PROBATION":
                return true;
            default:
                return false;
        }
    }

    /** Chỉ cho phép redirect về URL nội bộ; với fallback tùy role. */
    private String safeNext(HttpServletRequest req, String next, String fallback) {
        String fb = (fallback == null || fallback.isBlank())
                ? (req.getContextPath() + "/request/list")
                : fallback;

        if (next == null || next.isBlank()) return fb;

        // Không cho absolute URL (tránh open redirect)
        try {
            URI u = new URI(next);
            if (u.isAbsolute()) return fb;
        } catch (URISyntaxException e) {
            return fb;
        }

        // Chỉ chấp nhận đường dẫn nội bộ
        if (next.startsWith(req.getContextPath()) || next.startsWith("/")) {
            return next.startsWith("/")
                    ? (req.getContextPath() + next)
                    : (req.getContextPath() + "/" + next);
        }
        return fb;
    }

    /** Bản giữ tương thích nếu nơi khác còn gọi. */
    private String safeNext(HttpServletRequest req, String next) {
        return safeNext(req, next, req.getContextPath() + "/request/list");
    }
}
