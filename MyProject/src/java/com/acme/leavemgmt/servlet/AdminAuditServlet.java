package com.acme.leavemgmt.servlet;

import com.acme.leavemgmt.dao.ActivityDAO;
import com.acme.leavemgmt.model.Activity;
import com.acme.leavemgmt.dao.admin.AdminAuditDAO;
import com.acme.leavemgmt.dao.admin.AdminAuditDAO.Page;
import com.acme.leavemgmt.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.time.LocalDate;

public class AdminAuditServlet extends HttpServlet {
    private final AdminAuditDAO dao = new AdminAuditDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        User me = (session == null) ? null : (User) session.getAttribute("currentUser");

        // Gộp route thực tế: servletPath + pathInfo
        String sp    = req.getServletPath(); // "/activity" hoặc "/admin"
        String pi    = req.getPathInfo();    // null hoặc "/activity"
        String route = (sp == null ? "" : sp) + (pi == null ? "" : pi); // "/activity" | "/admin/activity"

        // /activity : người dùng xem của chính mình
        if ("/activity".equals(route)) {
            if (me == null) { resp.sendRedirect(req.getContextPath()+"/login"); return; }
            handleList(req, resp, me.getId());
            return;
        }

        // /admin/activity : admin xem, có thể lọc theo userId
        if ("/admin/activity".equals(route)) {
            if (me == null || !"ADMIN".equalsIgnoreCase(me.getRole())) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            Integer userId = safeIntObj(req.getParameter("userId")); // null => tất cả
            handleList(req, resp, userId);
            return;
        }

        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    /** Render chung cho 2 đường dẫn */
    private void handleList(HttpServletRequest req, HttpServletResponse resp, Integer userId)
            throws ServletException, IOException {

        int page = safeInt(req.getParameter("page"), 1);
        int size = safeInt(req.getParameter("size"), 20);

        String action = trim(req.getParameter("action"));
        String q      = trim(req.getParameter("q"));
        LocalDate from = parseDate(req.getParameter("from"));
        LocalDate to   = parseDate(req.getParameter("to"));

        try {
            Page<Activity> result =
                    dao.search(userId, action, q, from, to, page, size);

            req.setAttribute("pg", result);
            req.setAttribute("result", result);
            req.setAttribute("userFilter", userId);
            req.setAttribute("scope", (userId == null ? "admin" : "me"));

            req.getRequestDispatcher("/WEB-INF/views/admin/audit.jsp").forward(req, resp);
        } catch (RuntimeException e) {
            throw new ServletException(e);
        }
    }

    /* ============== helpers ============== */
    private static String trim(String s){ return s==null?null:s.trim(); }
    private static int safeInt(String s, int def) {
        if (s == null || s.isBlank()) return def;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return def; }
    }
    private static Integer safeIntObj(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Integer.valueOf(s); } catch (NumberFormatException e) { return null; }
    }
    private static LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        try { return LocalDate.parse(s); } catch (Exception ignore) { return null; }
    }
}
