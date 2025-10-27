package com.acme.leavemgmt.servlet.admin;

import com.acme.leavemgmt.dao.StatsDAO;
import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.Csrf;
import com.acme.leavemgmt.util.AuditLog;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

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
            String next = URLEncoder.encode(req.getRequestURI()
                    + (req.getQueryString() != null ? "?" + req.getQueryString() : ""), StandardCharsets.UTF_8.name());
            resp.sendRedirect(req.getContextPath() + "/login?next=" + next);
            return;
        }

        boolean isAdmin   = me.isAdmin();
        boolean isLeader  = me.isLeader();
        if (!isAdmin && !isLeader) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // 2) Chọn phòng ban: Admin có thể xem dept bất kỳ qua ?dept=QA|IT|SALE
        String dept = req.getParameter("dept");
        if (!isAdmin || dept == null || dept.isBlank()) {
            dept = safe(me.getDepartment()); // leader luôn khoá theo phòng của họ
        }

        // 3) Khoảng ngày (tuỳ chọn) – mặc định hôm nay
        LocalDate from = parseDate(req.getParameter("from"), LocalDate.now());
        LocalDate to   = parseDate(req.getParameter("to"),   LocalDate.now());
        if (to.isBefore(from)) { LocalDate t = from; from = to; to = t; }

        // 4) Lấy dữ liệu
        //   Yêu cầu: StatsDAO tự handle SQLException và trả về list/map rỗng khi lỗi.
        var stats    = statsDAO.getDivisionStats(dept, from, to);
        var pending  = statsDAO.getDivisionPendingRequests(dept);     // trạng thái PENDING
        var todayOff = statsDAO.getDivisionTodayOff(dept, LocalDate.now());

        // 5) CSRF token cho form Approve/Reject
        String csrf = Csrf.ensureToken(req.getSession());
        req.setAttribute("csrf", csrf);

        // 6) Set attribute cho view
        req.setAttribute("dept", dept);
        req.setAttribute("canSwitchDept", isAdmin);
        req.setAttribute("from", from);
        req.setAttribute("to", to);
        req.setAttribute("stats", stats);
        req.setAttribute("pending", pending);
        req.setAttribute("todayOff", todayOff);

        // 7) No-store + audit nhẹ
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);
        try { AuditLog.log(req, "DIV_DASHBOARD_VIEW", "USER", me.getId(), "dept="+dept+", from="+from+", to="+to); } catch (Throwable ignore) {}

        req.getRequestDispatcher("/WEB-INF/views/admin/div_dashboard.jsp").forward(req, resp);
    }

    /* ===== helpers ===== */
    private static String safe(String s){ return s == null ? "" : s.trim(); }
    private static LocalDate parseDate(String s, LocalDate d){
        try { return (s == null || s.isBlank()) ? d : LocalDate.parse(s); }
        catch (Exception e){ return d; }
    }
}
