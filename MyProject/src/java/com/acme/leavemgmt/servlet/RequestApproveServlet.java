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
import java.sql.SQLException;

@WebServlet(name = "RequestApproveServlet", urlPatterns = {"/request/approve"})
public class RequestApproveServlet extends HttpServlet {
    private final RequestDAO requestDAO = new RequestDAO();
    private final ActivityDAO activityDAO = new ActivityDAO();

    // --- Helpers ---
    private boolean hasApproveRole(User u) {
        if (u == null) return false;
        String role = u.getRole();
        return "ADMIN".equals(role) || "DIV_LEADER".equals(role) || "TEAM_LEAD".equals(role);
    }

    private void requireLogin(User u, HttpServletResponse resp, HttpServletRequest req) throws IOException {
        if (u == null) {
            resp.sendRedirect(req.getContextPath() + "/login?next=" + req.getRequestURI() + (req.getQueryString() != null ? ("?" + req.getQueryString()) : ""));
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession s = req.getSession(false);
        User me = (s != null) ? (User) s.getAttribute("currentUser") : null;

        // Chưa đăng nhập -> chuyển về login
        if (me == null) {
            requireLogin(null, resp, req);
            return;
        }

        // Sai quyền
        if (!hasApproveRole(me)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Lấy id
        String idRaw = req.getParameter("id");
        int id;
        try {
            id = Integer.parseInt(idRaw);
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request id");
            return;
        }

        try {
            Request r = requestDAO.findById(id);
            if (r == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Request not found");
                return;
            }

            // Phạm vi duyệt: ADMIN duyệt tất; DIV_LEADER duyệt cùng division; TEAM_LEAD duyệt team mình
            // -> uỷ thác cho DAO check (tùy bạn hiện thực)
            if (!requestDAO.isAllowedToApprove(me, r)) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Out of your approval scope");
                return;
            }

            req.setAttribute("reqItem", r);
            req.getRequestDispatcher("/WEB-INF/views/request/approve.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        HttpSession s = req.getSession(false);
        User me = (s != null) ? (User) s.getAttribute("currentUser") : null;
        if (me == null) {
            requireLogin(null, resp, req);
            return;
        }
        if (!hasApproveRole(me)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Params
        String action = req.getParameter("action"); // approve | reject
        String note   = req.getParameter("note");
        String idRaw  = req.getParameter("id");

        int id;
        try {
            id = Integer.parseInt(idRaw);
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request id");
            return;
        }

        String newStatus;
        if ("approve".equalsIgnoreCase(action)) {
            newStatus = "APPROVED";
        } else if ("reject".equalsIgnoreCase(action)) {
            newStatus = "REJECTED";
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
            return;
        }

        try {
            Request r = requestDAO.findById(id);
            if (r == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Request not found");
                return;
            }

            if (!requestDAO.isAllowedToApprove(me, r)) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Out of your approval scope");
                return;
            }

            // Chỉ xử lý khi đang PENDING (tránh duyệt trùng)
            boolean ok = requestDAO.updateStatusIfPending(
                    id,
                    newStatus,
                    me.getId(),
                    note
            );

            // Ghi activity (dù ok hay không, nhưng nội dung khác nhau)
            String ip = WebUtil.clientIp(req);
            String ua = WebUtil.userAgent(req);
            String actionName = "APPROVE_REQUEST";
            if ("REJECTED".equals(newStatus)) actionName = "REJECT_REQUEST";

            activityDAO.log(
                    me.getId(),
                    actionName,
                    "REQUEST",
                    id,
                    (ok ? ("Processed " + newStatus) : ("Skip: not PENDING")),
                    ip,
                    ua
            );

            String qs = ok ? "msg=processed" : "msg=not_pending";
            resp.sendRedirect(req.getContextPath() + "/request/list?" + qs);

        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
