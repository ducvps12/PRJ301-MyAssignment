package com.acme.leavemgmt.servlet;

import com.acme.leavemgmt.dao.RequestDAO;
import com.acme.leavemgmt.model.Request;
import com.acme.leavemgmt.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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
            // quay lại sau khi login
            String back = req.getContextPath() + "/login?next=" + req.getContextPath() + "/request/approvals";
            resp.sendRedirect(back);
            return;
        }

        if (!isAllowed(me)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        List<Request> items;
        try {
            items = requestDAO.findPendingForApprover(me);
            if (items == null) items = Collections.emptyList();
        } catch (SQLException ex) {
            // Không văng 500 nữa – log và render trang với list rỗng
            log.log(Level.SEVERE, "Load approvals failed (DB error)", ex);
            items = Collections.emptyList();
            req.setAttribute("dbError", "DB_ERROR");
        } catch (Exception ex) {
            // Bất kỳ lỗi runtime nào khác
            log.log(Level.SEVERE, "Load approvals failed (unexpected)", ex);
            items = Collections.emptyList();
            req.setAttribute("dbError", "UNEXPECTED");
        }

        // Tên attribute tương thích cả view cũ & mới
        req.setAttribute("items", items);
        req.setAttribute("pending", items);

        // Nếu bạn đặt JSP ở chỗ khác, đổi path này cho khớp
        req.getRequestDispatcher("/WEB-INF/views/request/approvals.jsp").forward(req, resp);
    }

    /* ===== Helpers ===== */

    private boolean isAllowed(User me) {
        // Ưu tiên boolean helper nếu Model đã có
        try { if (me.isAdmin())  return true; } catch (Throwable ignored) {}
        try { if (me.isLeader()) return true; } catch (Throwable ignored) {}

        String role = normalize(roleOf(me));
        // Cho phép: ADMIN, SYS_ADMIN, DIV_LEADER, TEAM_LEAD, HR_ADMIN, MANAGER, LEADER
        return "ADMIN".equals(role)
                || "SYS_ADMIN".equals(role)
                || "DIV_LEADER".equals(role)
                || "TEAM_LEAD".equals(role)
                || "HR_ADMIN".equals(role)
                || "MANAGER".equals(role)
                || "LEADER".equals(role);
    }

    private String roleOf(User u) {
        if (u == null) return "";
        if (u.getRole() != null && !u.getRole().isEmpty()) return u.getRole();
        if (u.getRoleCode() != null && !u.getRoleCode().isEmpty()) return u.getRoleCode();
        return "";
    }

    private String normalize(String s) {
        return s == null ? "" : s.trim().toUpperCase(Locale.ROOT);
    }
}
