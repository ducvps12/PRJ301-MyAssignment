package com.acme.leavemgmt.servlet;

import com.acme.leavemgmt.dao.UserDAO;           // dùng DAO đăng nhập chuẩn
import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.AuditLog;
import com.acme.leavemgmt.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO(); // nếu bạn muốn giữ RequestDAO, đổi lại cho khớp dự án

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Trang login – nếu bạn dùng index.jsp làm trang login thì giữ như cũ
        req.getRequestDispatcher("/WEB-INF/views/index.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String username = trim(req.getParameter("username"));
        String password = trim(req.getParameter("password"));

        if (username.isEmpty() || password.isEmpty()) {
            req.setAttribute("error", "Vui lòng nhập đầy đủ tài khoản và mật khẩu.");
            req.getRequestDispatcher("/WEB-INF/views/index.jsp").forward(req, resp);
            return;
        }

        try {
            // Đăng nhập (trả u nếu đúng & status=1, null nếu sai)
            User u = userDAO.findByUsernameAndPassword(username, password);

            if (u == null) {
                // Log thất bại
                AuditLog.log(req, "LOGIN_FAIL", "USER", null, "Sai tài khoản/mật khẩu");
                req.setAttribute("error", "Sai tài khoản hoặc mật khẩu.");
                req.getRequestDispatcher("/WEB-INF/views/index.jsp").forward(req, resp);
                return;
            }

            // Reset session để tránh session fixation
            HttpSession old = req.getSession(false);
            if (old != null) old.invalidate();

            HttpSession session = req.getSession(true);
            session.setAttribute("currentUser", u);      // key chuẩn dùng toàn hệ thống
            // (tuỳ chọn) alias cho view cũ
            session.setAttribute("fullName", u.getFullName());
            session.setAttribute("role", u.getRole());
            session.setAttribute("department", u.getDepartment());

            // Log thành công
            AuditLog.log(req, "LOGIN", "USER", u.getId(), "Đăng nhập thành công");

            // Điều hướng
            String ctx = req.getContextPath();
            String dest = u.canAccessAdminDashboard() ? "/admin" : "/request/list";
            resp.sendRedirect(ctx + dest);
            return;

        } catch (SQLException e) {
            throw new ServletException("Database error during login", e);
        }
    }

    private static String trim(String s) { return s == null ? "" : s.trim(); }
}
