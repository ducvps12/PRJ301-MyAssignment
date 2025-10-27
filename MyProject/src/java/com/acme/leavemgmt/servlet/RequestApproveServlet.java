package com.acme.leavemgmt.servlet;

import com.acme.leavemgmt.dao.ActivityDAO;
import com.acme.leavemgmt.dao.RequestDAO;
import com.acme.leavemgmt.model.Request;
import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

@WebServlet(name = "RequestApproveServlet",
        urlPatterns = {"/request/approve", "/request/approve/*"})
public class RequestApproveServlet extends HttpServlet {

    private final RequestDAO requestDAO = new RequestDAO();
    private final ActivityDAO activityDAO = new ActivityDAO();

    private boolean hasApproveRole(User u) {
        if (u == null) return false;
        String role = u.getRole();
        return "ADMIN".equals(role) || "DIV_LEAD".equals(role) || "LEAD".equals(role);
    }

    /** true nếu đã redirect về login. */
    private boolean requireLogin(User u, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (u != null) return false;
        String nextRaw = req.getRequestURI() + (req.getQueryString() != null ? ("?" + req.getQueryString()) : "");
        String next = URLEncoder.encode(nextRaw, StandardCharsets.UTF_8);
        resp.sendRedirect(resp.encodeRedirectURL(req.getContextPath() + "/login?next=" + next));
        return true;
    }

    /** Đọc id từ ?id= hoặc /approve/{id}; null nếu không hợp lệ. */
    private Integer readId(HttpServletRequest req) {
        String idRaw = req.getParameter("id");
        if (idRaw == null) {
            String pi = req.getPathInfo();              // ví dụ "/123"
            if (pi != null && pi.length() > 1) idRaw = pi.substring(1);
        }
        if (idRaw == null || !idRaw.matches("\\d+")) return null;
        try { return Integer.valueOf(idRaw); } catch (Exception ignore) { return null; }
    }

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession s = req.getSession(false);
        User me = (s != null) ? (User) s.getAttribute("currentUser") : null;

        if (requireLogin(me, req, resp)) return;
        if (!hasApproveRole(me)) { resp.sendError(HttpServletResponse.SC_FORBIDDEN); return; }

        Integer id = readId(req);
        if (id == null) {
            resp.sendRedirect(resp.encodeRedirectURL(req.getContextPath()+"/request/list?err=missing_id"));
            return;
        }

        try {
            Request r = requestDAO.findById(id);
            if (r == null) { resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Request not found"); return; }
            if (!requestDAO.isAllowedToApprove(me, r)) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Out of your approval scope"); return;
            }

            req.setAttribute("requestItem", r);
            req.getRequestDispatcher("/WEB-INF/views/request/approve.jsp").forward(req, resp);

        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        HttpSession s = req.getSession(false);
        User me = (s != null) ? (User) s.getAttribute("currentUser") : null;

        if (requireLogin(me, req, resp)) return;
        if (!hasApproveRole(me)) { resp.sendError(HttpServletResponse.SC_FORBIDDEN); return; }

        String action = req.getParameter("action"); // approve | reject
        String note   = req.getParameter("note");
        Integer id    = readId(req);
        if (id == null) {
            resp.sendRedirect(resp.encodeRedirectURL(req.getContextPath()+"/request/list?err=missing_id"));
            return;
        }

        String newStatus = "approve".equalsIgnoreCase(action) ? "APPROVED"
                         : "reject".equalsIgnoreCase(action)  ? "REJECTED" : null;
        if (newStatus == null) {
            resp.sendRedirect(resp.encodeRedirectURL(req.getContextPath()+"/request/approve/"+id+"?err=invalid_action"));
            return;
        }

        try {
            Request r = requestDAO.findById(id);
            if (r == null) { resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Request not found"); return; }
            if (!requestDAO.isAllowedToApprove(me, r)) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Out of your approval scope"); return;
            }

            boolean ok = requestDAO.updateStatusIfPending(id, newStatus, me.getId(), note);

            String ip = WebUtil.clientIp(req);
            String ua = WebUtil.userAgent(req);
            String actionName = "REJECTED".equals(newStatus) ? "REJECT_REQUEST" : "APPROVE_REQUEST";
            activityDAO.log(me.getId(), actionName, "REQUEST", id,
                    ok ? ("Processed " + newStatus) : "Skip: not PENDING", ip, ua);

            String qs = ok ? "msg=processed" : "msg=not_pending";
            resp.sendRedirect(resp.encodeRedirectURL(req.getContextPath() + "/request/list?" + qs));

        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
