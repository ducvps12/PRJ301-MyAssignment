package com.acme.leavemgmt.servlet;

import com.acme.leavemgmt.dao.RequestDAO;
import com.acme.leavemgmt.model.Request;
import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.Csrf;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Phê duyệt đơn nghỉ: /request/approvals */
@WebServlet(name = "ApprovalsServlet",
        urlPatterns = {"/request/approvals", "/request/approvals/"})
public class ApprovalsServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(ApprovalsServlet.class.getName());
    private final RequestDAO requestDAO = new RequestDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");

        HttpSession s = req.getSession(false);
        User me = (s != null) ? (User) s.getAttribute("currentUser") : null;

        if (me == null) {
            String back = req.getContextPath() + "/login?next=" + req.getContextPath() + "/request/approvals";
            resp.sendRedirect(back);
            return;
        }
        if (!isAllowed(me)) { resp.sendError(HttpServletResponse.SC_FORBIDDEN); return; }

        List<Request> items;
        try {
            items = requestDAO.findPendingForApprover(me);
            if (items == null) items = Collections.emptyList();
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "Load approvals failed (DB)", ex);
            items = Collections.emptyList();
            req.setAttribute("dbError", "DB_ERROR");
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Load approvals failed (Unexpected)", ex);
            items = Collections.emptyList();
            req.setAttribute("dbError", "UNEXPECTED");
        }

        req.setAttribute("items", items);
        req.setAttribute("pending", items);

        // flash từ session sang request để show 1 lần
        if (s != null && s.getAttribute("flash") != null) {
            req.setAttribute("flash", s.getAttribute("flash"));
            s.removeAttribute("flash");
        }

        Csrf.protect(req); // sinh csrfParam/csrfToken
        req.getRequestDispatcher("/WEB-INF/views/request/approvals.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        req.setCharacterEncoding("UTF-8");

        HttpSession s = req.getSession(false);
        User me = (s != null) ? (User) s.getAttribute("currentUser") : null;

        if (me == null) { resp.sendError(401); return; }
        if (!isAllowed(me)) { resp.sendError(403); return; }
        if (!Csrf.valid(req)) { resp.sendError(400, "Invalid CSRF"); return; }

        String action = norm(req.getParameter("action")); // APPROVE | REJECT | CANCEL
        String note   = safe(req.getParameter("note"));
        String idStr  = req.getParameter("id");
        String[] ids  = req.getParameterValues("ids");

        int ok = 0, fail = 0;

        try {
            if (idStr != null && !idStr.isBlank()) {
                if (applyOne(pint(idStr), action, me.getUserId(), note)) ok++; else fail++;
            } else if (ids != null && ids.length > 0) {
                for (String sId : ids) if (applyOne(pint(sId), action, me.getUserId(), note)) ok++; else fail++;
            } else {
                if (s != null) s.setAttribute("flash", "Chưa chọn bản ghi hoặc hành động.");
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Approve/Reject error", ex);
            fail++;
            if (s != null) s.setAttribute("flash", "Có lỗi khi xử lý: " + ex.getMessage());
        }

        if (s != null && (ok > 0 || fail > 0)) {
            s.setAttribute("flash", String.format("Thành công: %d | Thất bại: %d", ok, fail));
        }
        resp.sendRedirect(req.getContextPath() + "/request/approvals");
    }

    private boolean applyOne(int id, String action, int actorId, String note) throws SQLException {
        if (id <= 0) return false;
        switch (action) {
            case "APPROVE": case "APPROVED":
                return requestDAO.updateStatusIfPending(id, "APPROVED", actorId, note);
            case "REJECT": case "REJECTED":
                return requestDAO.updateStatusIfPending(id, "REJECTED", actorId, note);
            case "CANCEL": case "CANCELLED":
                return requestDAO.updateStatusIfPending(id, "CANCELLED", actorId, note);
            default: return false;
        }
    }

    private boolean isAllowed(User me) {
        try { if (me.isAdmin())  return true; } catch (Throwable ignored) {}
        try { if (me.isLeader()) return true; } catch (Throwable ignored) {}
        String r = norm(roleOf(me));
        return r.equals("ADMIN") || r.equals("SYS_ADMIN") || r.equals("DIV_LEADER")
            || r.equals("TEAM_LEAD") || r.equals("HR_ADMIN") || r.equals("MANAGER") || r.equals("LEADER");
    }
    private String roleOf(User u){ if(u==null)return ""; if(nz(u.getRole())) return u.getRole(); if(nz(u.getRoleCode())) return u.getRoleCode(); return ""; }
    private boolean nz(String s){ return s!=null && !s.isEmpty(); }
    private String norm(String s){ return s==null?"":s.trim().toUpperCase(Locale.ROOT); }
    private int pint(String s){ try { return Integer.parseInt(s.trim()); } catch(Exception e){ return -1; } }
    private String safe(String s){ return (s==null||s.trim().isEmpty())?null:s.trim(); }
}
