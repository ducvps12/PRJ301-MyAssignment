package com.acme.leavemgmt.web;

import com.acme.leavemgmt.dao.ActivityDAO;
import com.acme.leavemgmt.model.Activity;
import com.acme.leavemgmt.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.time.LocalDate;

public class ActivityServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final ActivityDAO dao = new ActivityDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        User me = session == null ? null : (User) session.getAttribute("currentUser");
        String path = req.getServletPath();

        // /activity: user xem lịch sử của chính mình
        if ("/activity".equals(path)) {
            if (me == null) {
                resp.sendRedirect(req.getContextPath() + "/login");
                return;
            }
            handleList(req, resp, me.getId(), false);
            return;
        }

        // /admin/activity: chỉ ADMIN
        if (me == null || !"ADMIN".equalsIgnoreCase(me.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        Integer userId = safeIntObj(req.getParameter("userId"));
        handleList(req, resp, userId, true);
    }

    /** Render chung + hỗ trợ export CSV. */
    private void handleList(HttpServletRequest req, HttpServletResponse resp,
                            Integer userId, boolean isAdmin)
            throws ServletException, IOException {

        int page = safeInt(req.getParameter("page"), 1);
        int size = safeInt(req.getParameter("size"), 20);

        String action = trim(req.getParameter("action"));
        String q      = trim(req.getParameter("q"));
        LocalDate from = parseDate(req.getParameter("from"));
        LocalDate to   = parseDate(req.getParameter("to"));

        // Export CSV
        if ("csv".equalsIgnoreCase(req.getParameter("export"))) {
            resp.setContentType("text/csv; charset=UTF-8");
            resp.setHeader("Content-Disposition", "attachment; filename=activity-log.csv");
            dao.exportCsv(userId, action, q, from, to, resp.getWriter());
            return;
        }

        ActivityDAO.Page<Activity> result =
                dao.search(userId, action, q, from, to, page, size);

        req.setAttribute("result", result);  // tên mới
        req.setAttribute("pg", result);      // alias cho JSP cũ
        req.setAttribute("userFilter", userId);
        req.setAttribute("scope", isAdmin ? "admin" : "me");

        req.getRequestDispatcher("/WEB-INF/views/activity.jsp").forward(req, resp);
    }

    /* ---------------- helpers ---------------- */
    private static String trim(String s){ return (s == null) ? null : s.trim(); }

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
