package com.acme.leavemgmt.servlet;

import com.acme.leavemgmt.dao.RequestDAO;
import com.acme.leavemgmt.model.Request;
import com.acme.leavemgmt.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
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

        HttpSession s = req.getSession(false);
        User me = (s != null) ? (User) s.getAttribute("currentUser") : null;

        if (me == null) {
            resp.sendRedirect(req.getContextPath() + "/login?next=/request/approvals");
            return;
        }

        // Cho phép: ADMIN, SYS_ADMIN, DIV_LEADER, TEAM_LEAD, HR_ADMIN, MANAGER/LEADER
        String role = "";
        if (me.getRole() != null) role = me.getRole();
        else if (me.getRoleCode() != null) role = me.getRoleCode();
        role = role == null ? "" : role.trim().toUpperCase();

        boolean allowed =
                (has(me, "ADMIN") || has(me, "SYS_ADMIN") ||
                 has(me, "DIV_LEADER") || has(me, "TEAM_LEAD") ||
                 has(me, "HR_ADMIN") || has(me, "MANAGER") || has(me, "LEADER"));

        if (!allowed) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            List<Request> items = requestDAO.findPendingForApprover(me);
            if (items == null) items = Collections.emptyList();

            // Tương thích cả JSP cũ & mới
            req.setAttribute("items", items);
            req.setAttribute("pending", items);

            req.getRequestDispatcher("/WEB-INF/views/request/approvals.jsp")
               .forward(req, resp);

        } catch (SQLException ex) {
            log.log(Level.SEVERE, "Load approvals failed", ex);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "DB error");
        }
    }

    /** Helper: ưu tiên isAdmin()/isLeader() nếu model có, fallback so sánh chuỗi role. */
    private boolean has(User me, String need) {
        String n = need == null ? "" : need.trim().toUpperCase();
        if ("ADMIN".equals(n) && safeBool(() -> me.isAdmin())) return true;
        if (("LEADER".equals(n) || "DIV_LEADER".equals(n) || "TEAM_LEAD".equals(n))
                && safeBool(() -> me.isLeader())) return true;

        String r = (me.getRole() != null ? me.getRole() :
                   (me.getRoleCode() != null ? me.getRoleCode() : ""));
        r = r == null ? "" : r.trim().toUpperCase();
        return r.equals(n);
    }

    private boolean safeBool(Check c) {
        try { return c.get(); } catch (Throwable t) { return false; }
    }
    @FunctionalInterface private interface Check { boolean get(); }
}
