package com.acme.leavemgmt.servlet;

import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.AuditLog;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet(name = "LogoutServlet", urlPatterns = {"/logout"})
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Lấy current user (nếu có) để ghi log trước khi huỷ session
        HttpSession session = req.getSession(false);
        User cu = null;
        if (session != null) {
            Object u = session.getAttribute("currentUser");
            if (u instanceof User) {
                cu = (User) u;
            }
        }

        // Ghi log logout
        try {
            AuditLog.log(req, "LOGOUT", "USER", (cu != null ? cu.getId() : null), "User logout");
        } catch (Throwable ignore) {
            // tránh làm hỏng luồng logout vì lỗi ghi log
        }

        // Huỷ session
        if (session != null) {
            session.invalidate();
        }

        // Xoá các cookie phiên/ghi nhớ (nếu có)
        String ctx = req.getContextPath();
        String path = (ctx == null || ctx.isEmpty()) ? "/" : ctx;

        // JSESSIONID
        Cookie jsid = new Cookie("JSESSIONID", "");
        jsid.setMaxAge(0);
        jsid.setHttpOnly(true);
        jsid.setPath(path);
        resp.addCookie(jsid);

        // Nếu bạn có cookie "REMEMBER_ME" hoặc "AUTH_TOKEN", xoá tương tự:
        for (String name : new String[]{"REMEMBER_ME", "AUTH_TOKEN"}) {
            Cookie c = new Cookie(name, "");
            c.setMaxAge(0);
            c.setHttpOnly(true);
            c.setPath(path);
            resp.addCookie(c);
        }

        // Chặn cache để không quay lại trang cũ sau khi logout
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);

        // Điều hướng về trang đăng nhập
        resp.sendRedirect(path + "/login");
    }

    // Hỗ trợ POST (nếu form submit hoặc AJAX gọi POST)
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }
}
