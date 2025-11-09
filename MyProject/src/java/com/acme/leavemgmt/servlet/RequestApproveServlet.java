package com.acme.leavemgmt.servlet;

import com.acme.leavemgmt.dao.ActivityDAO;
import com.acme.leavemgmt.dao.RequestDAO;
import com.acme.leavemgmt.model.Request;
import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.WebUtil;
import com.acme.leavemgmt.util.Csrf;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet(name = "RequestApproveServlet",
        urlPatterns = {"/request/approve", "/request/approve/*"})
public class RequestApproveServlet extends HttpServlet {

    // ===== Status (đồng bộ DB) =====
    private static final String ST_PENDING  = "PENDING";
    private static final String ST_APPROVED = "APPROVED";
    private static final String ST_REJECTED = "REJECTED";

    private final RequestDAO requestDAO = new RequestDAO();
    private final ActivityDAO activityDAO = new ActivityDAO();

    /* ===== Helpers ===== */

    // Ưu tiên flag trong model; fallback theo role text khi thiếu method
    private boolean hasApproveRole(User u) {
        if (u == null) return false;
        try { if (u.isAdmin())  return true; } catch (Throwable ignore) {}
        try { if (u.isLeader()) return true; } catch (Throwable ignore) {}
        try { if (u.canApproveRequests()) return true; } catch (Throwable ignore) {}

        String r = (u.getRoleCode()!=null ? u.getRoleCode() : u.getRole());
        r = r == null ? "" : r.trim().toUpperCase();
        return r.equals("ADMIN") || r.equals("SYS_ADMIN") || r.equals("HR_ADMIN")
            || r.equals("DIV_LEADER") || r.equals("TEAM_LEAD")
            || r.equals("MANAGER") || r.equals("LEADER");
    }

    private static int actorId(User u){
        try { if (u.getUserId() > 0) return u.getUserId(); } catch (Throwable ignore) {}
        try { if (u.getId()     > 0) return u.getId();     } catch (Throwable ignore) {}
        return 0;
    }

    /** true nếu đã redirect về login */
    private boolean requireLogin(User u, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (u != null) return false;
        final String nextRaw = req.getRequestURI() + (req.getQueryString() != null ? ("?" + req.getQueryString()) : "");
        final String next = URLEncoder.encode(nextRaw, StandardCharsets.UTF_8);
        resp.sendRedirect(resp.encodeRedirectURL(req.getContextPath() + "/login?next=" + next));
        return true;
    }

    /** Lấy 1 id từ ?id= hoặc /approve/{id} */
    private Integer readId(HttpServletRequest req) {
        String idRaw = req.getParameter("id");
        if (idRaw == null) {
            final String pi = req.getPathInfo(); // "/123"
            if (pi != null && pi.length() > 1) idRaw = pi.substring(1);
        }
        if (idRaw == null || !idRaw.matches("\\d+")) return null;
        try { return Integer.valueOf(idRaw); } catch (Exception ignore) { return null; }
    }

    /** Lấy danh sách ids (ids[] hoặc rơi về id đơn lẻ) */
    private List<Integer> readIds(HttpServletRequest req) {
        List<Integer> ids = new ArrayList<>();
        Integer single = readId(req);
        if (single != null) { ids.add(single); return ids; }

        String[] many = req.getParameterValues("ids");
        if (many != null) {
            for (String x : many) {
                if (x != null && x.matches("\\d+")) ids.add(Integer.parseInt(x));
            }
        }
        return ids;
    }

    private static void json(HttpServletResponse resp, int status, String body) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json; charset=UTF-8");
        try (PrintWriter w = resp.getWriter()) { w.write(body); }
    }

    /* ====== GET: mở trang detail (nếu bạn cần) ====== */
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession s = req.getSession(false);
        User me = (s != null) ? (User) s.getAttribute("currentUser") : null;

        if (requireLogin(me, req, resp)) return;
        if (!hasApproveRole(me)) { resp.sendError(HttpServletResponse.SC_FORBIDDEN); return; }

        Integer id = readId(req);
        if (id == null) {
            resp.sendRedirect(resp.encodeRedirectURL(req.getContextPath()+"/request/approvals?err=missing_id"));
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

    /* ====== POST: approve / reject (single hoặc bulk) ====== */
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        HttpSession s = req.getSession(false);
        User me = (s != null) ? (User) s.getAttribute("currentUser") : null;

        if (requireLogin(me, req, resp)) return;
        if (!hasApproveRole(me)) { json(resp, 403, "{\"success\":false,\"message\":\"FORBIDDEN\"}"); return; }

        // CSRF
        if (!Csrf.isTokenValid(req)) {
            json(resp, 400, "{\"success\":false,\"message\":\"CSRF_INVALID\"}");
            return;
        }

        final String action = Optional.ofNullable(req.getParameter("action")).orElse("").trim().toLowerCase();
        final String note   = Optional.ofNullable(req.getParameter("note")).orElse("");
        final String target = "approve".equals(action) ? ST_APPROVED : "reject".equals(action) ? ST_REJECTED : null;

        if (target == null) {
            json(resp, 400, "{\"success\":false,\"message\":\"BAD_ACTION\"}");
            return;
        }

        List<Integer> ids = readIds(req);
        if (ids.isEmpty()) {
            json(resp, 400, "{\"success\":false,\"message\":\"NO_IDS\"}");
            return;
        }

        int ok = 0, fail = 0;
        List<Integer> failed = new ArrayList<>();
        int actor = actorId(me);
        String ip = WebUtil.clientIp(req);
        String ua = WebUtil.userAgent(req);
        String actName = ST_REJECTED.equals(target) ? "REJECT_REQUEST" : "APPROVE_REQUEST";

        for (Integer id : ids) {
            try {
                Request r = requestDAO.findById(id);
                if (r == null || !requestDAO.isAllowedToApprove(me, r)) {
                    fail++; failed.add(id);
                    continue;
                }
                boolean done = requestDAO.updateStatusIfPending(id, target, actor, note);
                if (done) ok++; else { fail++; failed.add(id); }

                // log từng đơn
                activityDAO.log(actor, actName, "REQUEST", id,
                        done ? ("Processed " + target) : ("Skip: not " + ST_PENDING),
                        ip, ua);

            } catch (SQLException ex) {
                fail++; failed.add(id);
            }
        }

        boolean successAll = ok > 0 && fail == 0;
        String failedStr = failed.stream().map(String::valueOf).collect(Collectors.joining(","));
        String body = String.format(Locale.US,
                "{\"success\":%s,\"ok\":%d,\"fail\":%d,\"status\":\"%s\",\"failed\":\"%s\"}",
                successAll ? "true" : "false", ok, fail, target, failedStr);

        json(resp, 200, body);
    }
}
