package com.acme.leavemgmt.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet(name = "LogoutServlet", urlPatterns = {"/logout"})
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Hủy session hiện tại (nếu có)
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // Xóa cache trình duyệt để tránh quay lại trang cũ sau logout
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);

        // Điều hướng về trang đăng nhập
        resp.sendRedirect(req.getContextPath() + "/login");
    }

    // Nếu form POST logout, vẫn xử lý tương tự
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }
}
