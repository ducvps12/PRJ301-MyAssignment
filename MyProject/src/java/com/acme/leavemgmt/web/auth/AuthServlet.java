package com.acme.leavemgmt.web.auth;

import com.acme.leavemgmt.dao.RequestDAO;
import com.acme.leavemgmt.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

public class AuthServlet extends HttpServlet {
    // Đưa login.jsp vào /WEB-INF/views/auth/login.jsp cho rõ ràng
    private static final String VIEW_LOGIN = "/WEB-INF/views/auth/login.jsp";
    private final RequestDAO dao = new RequestDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Nếu đã đăng nhập, đi thẳng về trang chính (tránh nhìn thấy form login nữa)
        HttpSession s = req.getSession(false);
        if (s != null && s.getAttribute("userId") != null) {
            resp.sendRedirect(req.getContextPath() + "/request/list");
            return;
        }

        // Logout: /login?logout=1
        if ("1".equals(req.getParameter("logout"))) {
            if (s != null) s.invalidate();
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // CSRF token cho form
        ensureCsrfToken(req.getSession(true));

        // Nếu có param "next" thì giữ lại để sau đăng nhập chuyển tiếp
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

        // Kiểm tra CSRF token
        if (!validateCsrf(req)) {
            req.setAttribute("error", "Phiên không hợp lệ. Vui lòng thử lại.");
            req.getRequestDispatcher(VIEW_LOGIN).forward(req, resp);
            return;
        }

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
            // TODO: Sau này đổi sang kiểm tra theo mật khẩu đã hash (BCrypt, Argon2)
            User u = dao.findByUsernameAndPassword(username, password);

            if (u != null) {
                // Ngăn session fixation
                HttpSession old = req.getSession(false);
                if (old != null) old.invalidate();
                HttpSession s = req.getSession(true);
                try { req.changeSessionId(); } catch (Throwable ignore) {}

                // Lưu thông tin tối thiểu cần thiết
                s.setAttribute("userId", u.getId());
                s.setAttribute("fullName", u.getFullName());
                s.setAttribute("role", u.getRole());
                s.setAttribute("department", u.getDepartment());

                // Điều hướng sau đăng nhập (ưu tiên 'next' nếu có)
                String target = (next != null && !next.isBlank())
                                ? next
                                : (req.getContextPath() + "/request/list");
                resp.sendRedirect(target);
            } else {
                // Sai TK/MK → trả lại form với thông báo lỗi, giữ lại username
                req.setAttribute("error", "Sai tài khoản hoặc mật khẩu.");
                req.setAttribute("username", username);
                req.setAttribute("next", next);
                req.getRequestDispatcher(VIEW_LOGIN).forward(req, resp);
            }
        } catch (SQLException e) {
            throw new ServletException("Database error during login", e);
        }
    }

    // ===== Helpers =====
    private static String trim(String s) {
        return s == null ? "" : s.trim();
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
}
