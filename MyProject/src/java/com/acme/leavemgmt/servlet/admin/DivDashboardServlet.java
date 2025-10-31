package com.acme.leavemgmt.servlet.admin;

import com.acme.leavemgmt.dao.StatsDAO;
import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.AuditLog;
import com.acme.leavemgmt.util.Csrf;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@WebServlet(name = "DivDashboardServlet", urlPatterns = {"/admin/div"})
public class DivDashboardServlet extends HttpServlet {

    private final StatsDAO statsDAO = new StatsDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1) Auth
        HttpSession session = req.getSession(false);
        User me = (session != null) ? (User) session.getAttribute("currentUser") : null;
        if (me == null) {
            String next = URLEncoder.encode(
                    req.getRequestURI() + (req.getQueryString() != null ? "?" + req.getQueryString() : ""),
                    StandardCharsets.UTF_8.name()
            );
            resp.sendRedirect(req.getContextPath() + "/login?next=" + next);
            return;
        }

        // 2) Check quyền (admin hoặc leader)
        boolean isAdmin  = safeBoolean(me.isAdmin());
        boolean isLeader = safeBoolean(me.isLeader());
        if (!isAdmin && !isLeader) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // 3) Xác định phòng ban được xem
        //    - Admin: được chọn qua ?dept=...
        //    - Leader: ép theo department của user
        String deptParam = trimOrNull(req.getParameter("dept"));
        String dept;
        if (isAdmin && deptParam != null && !deptParam.isBlank()) {
            dept = deptParam;
        } else {
            // phòng của chính user
            dept = trimOrEmpty(me.getDepartment());
        }

        // 4) Khoảng ngày
        LocalDate today = LocalDate.now();
        LocalDate from = parseOrDefault(req.getParameter("from"), today);
        LocalDate to   = parseOrDefault(req.getParameter("to"),   today);
        if (to.isBefore(from)) {
            LocalDate tmp = from;
            from = to;
            to   = tmp;
        }

        // 5) Gọi DAO lấy dữ liệu
        // LƯU Ý: Ở đây mình gọi 3 hàm. Bạn phải đảm bảo trong StatsDAO có đủ 3 hàm này
        // và chúng JOIN được sang bảng Users để lọc theo department.
        var stats    = statsDAO.getDivisionStats(dept, from, to);
        var pending  = statsDAO.getDivisionPendingRequests(dept);     // danh sách đơn PENDING
        var todayOff = statsDAO.getDivisionTodayOff(dept, today);     // ai đang nghỉ hôm nay

        // 6) CSRF
        String csrf = Csrf.ensureToken(req.getSession());

        // 7) Đẩy sang JSP
        req.setAttribute("csrf", csrf);
        req.setAttribute("dept", dept);
        req.setAttribute("canSwitchDept", isAdmin);
        req.setAttribute("from", from.toString());
        req.setAttribute("to", to.toString());
        req.setAttribute("stats", stats);
        req.setAttribute("pending", pending);
        req.setAttribute("todayOff", todayOff);

        // 8) Không cache + audit
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);
        try {
            AuditLog.log(req, "DIV_DASHBOARD_VIEW", "USER", me.getId(),
                    "dept=" + dept + ", from=" + from + ", to=" + to);
        } catch (Throwable ignore) {}

        // 9) Forward
        req.getRequestDispatcher("/WEB-INF/views/admin/div_dashboard.jsp")
           .forward(req, resp);
    }

    /* ===== Helpers ===== */

    private static String trimOrNull(String s) {
        return (s == null) ? null : s.trim();
    }

    private static String trimOrEmpty(String s) {
        return (s == null) ? "" : s.trim();
    }

    private static boolean safeBoolean(Boolean b) {
        return b != null && b;
    }

    private static LocalDate parseOrDefault(String s, LocalDate dflt) {
        try {
            if (s == null || s.isBlank()) return dflt;
            return LocalDate.parse(s.trim());
        } catch (Exception e) {
            return dflt;
        }
    }
}
