package com.acme.leavemgmt.servlet;

import com.acme.leavemgmt.dao.ActivityDAO;
import com.acme.leavemgmt.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "ActivityServlet", urlPatterns = {"/activity", "/admin/activity"})
public class ActivityServlet extends HttpServlet {

    private final ActivityDAO dao = new ActivityDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        // ✅ đúng tên attribute
        User me = session == null ? null : (User) session.getAttribute("currentUser");
        String path = req.getServletPath();

        // ========== /activity : user tự xem lịch sử ==========
        if ("/activity".equals(path)) {
            if (me == null) {
                resp.sendRedirect(req.getContextPath() + "/login");
                return;
            }
            handleUser(me.getId(), req, resp);
            return;
        }

        // ========== /admin/activity : chỉ admin ==========
        if (me == null || !"ADMIN".equalsIgnoreCase(me.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        Integer userId = null;
        String uid = req.getParameter("userId");
        if (uid != null && !uid.isBlank()) {
            try {
                userId = Integer.valueOf(uid);
            } catch (NumberFormatException ignored) {}
        }

        handleAdmin(userId, req, resp);
    }

    // ================== USER ==================
    private void handleUser(int userId, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // ❗ không được parse thẳng
        int page = safeInt(req.getParameter("page"), 1);
        int size = safeInt(req.getParameter("size"), 20);

        try {
            var pg = dao.pageByUser(userId, page, size);
            req.setAttribute("pg", pg);
            req.setAttribute("scope", "me");
            req.getRequestDispatcher("/WEB-INF/views/activity.jsp")
                    .forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    // ================== ADMIN ==================
    private void handleAdmin(Integer userId, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        int page = safeInt(req.getParameter("page"), 1);
        int size = safeInt(req.getParameter("size"), 20);

        try {
            // dùng lại hàm DAO sẵn có
            var pg = dao.pageByUser(userId, page, size);

            req.setAttribute("pg", pg);
            req.setAttribute("scope", "admin");
            req.setAttribute("userFilter", userId);
            req.getRequestDispatcher("/WEB-INF/views/activity.jsp")
                    .forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    // ================== helper ==================
    private int safeInt(String s, int def) {
        if (s == null || s.isBlank()) return def;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
