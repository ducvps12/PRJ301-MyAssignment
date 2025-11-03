package com.acme.leavemgmt.web.auth;

import com.acme.leavemgmt.dao.UserDAO;
import com.acme.leavemgmt.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.UUID;

// @WebServlet(urlPatterns = {"/login"})  // dùng annotation nếu không khai báo trong web.xml
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

        // Đã đăng nhập -> về list
        if (s != null && s.getAttribute("currentUser") != null) {
            resp.sendRedirect(req.getContextPath() + "/request/list");
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
            // 3) Xác thực (ghi chú: sau này chuyển sang BCrypt/Argon2)
            User u = userDAO.findByUsernameAndPassword(username, password);

if (u != null && u.isActive()) {
                // 4) Chống session fixation
                HttpSession old = req.getSession(false);
                if (old != null) old.invalidate();
                HttpSession s = req.getSession(true);
                try { req.changeSessionId(); } catch (Throwable ignore) {}

                // 5) Lưu user vào session (đúng key mà RoleFilter dùng)
                s.setAttribute("currentUser", u);
                // (tuỳ chọn: lưu thêm hay không)
                s.setAttribute("userId", u.getId());
                s.setAttribute("fullName", u.getFullName());
                s.setAttribute("role", u.getRole());
                s.setAttribute("department", u.getDepartment());

                // 6) Điều hướng an toàn
                String target = safeNext(req, next);
                resp.sendRedirect(target);
                return;
            }

            // Sai TK/MK
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

    /** Chỉ cho phép redirect về URL nội bộ trong cùng context; mặc định /request/list */
    private String safeNext(HttpServletRequest req, String next) {
        String fallback = req.getContextPath() + "/request/list";
        if (next == null || next.isBlank()) return fallback;

        // Không cho absolute URL (tránh open redirect)
        try {
            URI u = new URI(next);
            if (u.isAbsolute()) return fallback;
        } catch (URISyntaxException e) {
            return fallback;
        }

        // Chỉ cho phép đường dẫn bắt đầu bằng contextPath hoặc là path tương đối
        if (next.startsWith(req.getContextPath()) || next.startsWith("/")) {
            return next.startsWith("/") ? (req.getContextPath() + next) : (req.getContextPath() + "/" + next);
        }
        return fallback;
    }
}
