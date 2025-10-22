package com.acme.leavemgmt.servlet;

import com.acme.leavemgmt.dao.ActivityDAO;
import com.acme.leavemgmt.dao.RequestDAO;
import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.WebUtil;
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
            User u = dao.findByUsernameAndPassword(username, password); // trả về null nếu sai
            if (u == null) {
                req.setAttribute("error", "Sai tài khoản hoặc mật khẩu.");
                req.getRequestDispatcher("/WEB-INF/views/index.jsp").forward(req, resp);
                return;
            }

            // --- Ngăn session fixation & set đúng session keys ---
            HttpSession old = req.getSession(false);
            if (old != null) {
                old.invalidate();
            }
            HttpSession session = req.getSession(true); // tạo session mới
            session.setAttribute("currentUser", u);     // <<< quan trọng: key chuẩn dùng bởi RoleFilter/JSP

            // log
            new ActivityDAO().log(
                    u.getId(),
                    "LOGIN", "USER", u.getId(),
                    "Đăng nhập thành công",
                    WebUtil.clientIp(req), WebUtil.userAgent(req)
            );

            // (tuỳ bạn, có thể set thêm các alias nếu view cũ đang dùng)
            session.setAttribute("fullName", u.getFullName());
            session.setAttribute("role", u.getRole());
            session.setAttribute("department", u.getDepartment());

            // --- Điều hướng sau login ---
            String ctx = req.getContextPath();
            String dest = (u.canAccessAdminDashboard()) ? "/admin" : "/request/list";
            resp.sendRedirect(ctx + dest);

        } catch (SQLException e) {
            throw new ServletException("Database error during login", e);
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
