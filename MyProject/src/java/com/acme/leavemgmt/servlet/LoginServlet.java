package com.acme.leavemgmt.servlet;

import com.acme.leavemgmt.dao.RequestDAO;
import com.acme.leavemgmt.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    private final RequestDAO dao = new RequestDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Hiển thị form đăng nhập
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
            User u = dao.findByUsernameAndPassword(username, password);
            if (u != null) {
                // Ngăn session fixation
                HttpSession old = req.getSession(false);
                if (old != null) old.invalidate();
                HttpSession s = req.getSession(true);
                try { s = req.changeSessionId() != null ? req.getSession(false) : s; } catch (Throwable ignore) {}

                s.setAttribute("userId", u.getId());
                s.setAttribute("fullName", u.getFullName());
                s.setAttribute("role", u.getRole());
                s.setAttribute("department", u.getDepartment());

                // Điều hướng tới danh sách đơn của tôi
                resp.sendRedirect(req.getContextPath() + "/request/list");
            } else {
                req.setAttribute("error", "Sai tài khoản hoặc mật khẩu.");
                req.getRequestDispatcher("/WEB-INF/views/index.jsp").forward(req, resp);
            }
        } catch (SQLException e) {
            throw new ServletException("Database error during login", e);
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
