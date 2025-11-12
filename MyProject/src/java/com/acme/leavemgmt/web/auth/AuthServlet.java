package com.acme.leavemgmt.web.auth;

import com.acme.leavemgmt.dao.UserDAO;
import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

@WebServlet(urlPatterns = {"/login"})
public class AuthServlet extends HttpServlet {

    private static final String VIEW_LOGIN = "/WEB-INF/views/auth/login.jsp";

    // ====== GET: hiển thị form, xử lý logout ======
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession s = req.getSession(false);

        // /login?logout=1
        if ("1".equals(req.getParameter("logout"))) {
            if (s != null) s.invalidate();
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Đã đăng nhập -> điều hướng theo role
        if (s != null && s.getAttribute("currentUser") != null) {
            User cu = (User) s.getAttribute("currentUser");
            String fallback = req.getContextPath()
                    + (isOffboardingRole(cu != null ? cu.getRole() : null) ? "/user/home" : "/portal");
            resp.sendRedirect(fallback);
            return;
        }

        ensureCsrfToken(req.getSession(true));

        String next = req.getParameter("next");
        if (next != null && !next.isBlank()) req.setAttribute("next", next);

        req.getRequestDispatcher(VIEW_LOGIN).forward(req, resp);
    }

    // ====== POST: xử lý đăng nhập ======
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // CSRF
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

        // Mở DAO theo request (ưu tiên JNDI, fallback DBConnection)
        try (UserDAO dao = openUserDao()) {
            User u = dao.findByUsernameAndPassword(username, password);

            if (isLoginAllowed(u)) {
                HttpSession old = req.getSession(false);
                if (old != null) old.invalidate();
                HttpSession s = req.getSession(true);
                try { req.changeSessionId(); } catch (Throwable ignore) {}

                s.setAttribute("currentUser", u);
                s.setAttribute("userId", u.getId());
                s.setAttribute("fullName", u.getFullName());
                s.setAttribute("role", u.getRole());
                s.setAttribute("department", u.getDepartment());

                String fallback = req.getContextPath()
                        + (isOffboardingRole(u.getRole()) ? "/user/home" : "/portal");
                String target = safeNext(req, next, fallback);
                resp.sendRedirect(target);
                return;
            }

            req.setAttribute("error", "Sai tài khoản hoặc mật khẩu.");
            req.setAttribute("username", username);
            req.setAttribute("next", next);
            req.getRequestDispatcher(VIEW_LOGIN).forward(req, resp);

        } catch (SQLException e) {
            throw new ServletException("Database error during login", e);
        }
    }

    // ====== Helpers ======
    /** Mở UserDAO theo request: ưu tiên DataSource JNDI, nếu không có thì dùng DBConnection. */
    private UserDAO openUserDao() throws SQLException {
        DataSource ds = jndiDataSourceOrNull("java:comp/env/jdbc/LeaveMgmtDS");
        if (ds != null) return new UserDAO(ds); // DAO tự mở & sẽ tự close() nhờ try-with-resources
        Connection cn = DBConnection.getConnection();
        return new UserDAO(cn); // vẫn tự close() ở try-with-resources (UserDAO implements AutoCloseable)
    }

    private DataSource jndiDataSourceOrNull(String jndiName) {
        try {
            InitialContext ctx = new InitialContext();
            return (DataSource) ctx.lookup(jndiName);
        } catch (NamingException ignore) {
            return null;
        }
    }

    private static String trim(String s) { return (s == null) ? "" : s.trim(); }

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
        if (!u.isActive()) return false;
        String role = u.getRole();
        return !("SUSPENDED".equals(role) || "TERMINATED".equals(role));
    }

    /** Nhóm role “hạn chế” → rơi về /user/home thay vì /portal. */
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

    /** Chỉ cho phép redirect nội bộ; có fallback. */
    private String safeNext(HttpServletRequest req, String next, String fallback) {
        String fb = (fallback == null || fallback.isBlank())
                ? (req.getContextPath() + "/portal")
                : fallback;

        if (next == null || next.isBlank()) return fb;

        try {
            URI u = new URI(next);
            if (u.isAbsolute()) return fb; // chặn open redirect
        } catch (URISyntaxException e) {
            return fb;
        }

        if (next.startsWith(req.getContextPath()) || next.startsWith("/")) {
            return next.startsWith("/") ? (req.getContextPath() + next)
                                        : (req.getContextPath() + "/" + next);
        }
        return fb;
    }

    // Không cần init()/destroy() vì DAO được mở/đóng theo từng request.
}
