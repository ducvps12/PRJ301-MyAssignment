package com.acme.leavemgmt.servlet.account;

import com.acme.leavemgmt.model.User;
// import com.acme.leavemgmt.model.Request;
// import com.acme.leavemgmt.model.Notification;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.*;

/**
 * UserHomeServlet – Trang Home sau đăng nhập (phiên người dùng).
 * Nạp số liệu cá nhân + capability theo role/trạng thái và xuất ra cho JSP.
 *
 * URL: /user/home
 * LƯU Ý: Nếu web.xml có metadata-complete="true" thì cần mapping trong web.xml.
 */
@WebServlet("/user/home")
public class UserHomeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // ======= Gợi ý Service/DAO (thay bằng service thực tế của bạn) =======
    // private final RequestService       requestService  = new RequestService();
    // private final NotificationService  notiService     = new NotificationService();
    // private final ApprovalService      approvalService = new ApprovalService();
    // private final LeaveBalanceService  balanceService  = new LeaveBalanceService();
    // private final AttendanceService    attendanceService = new AttendanceService();

    /** Nhóm role có quyền duyệt. */
    private static final Set<String> APPROVER_ROLES = setOf(
            "TEAM_LEAD", "DIV_LEADER", "HR_ADMIN", "MANAGER", "DEPT_MANAGER"
    );

    /** Nhóm role/trạng thái bị hạn chế mạnh (read-only hoặc khóa). */
    private static final Set<String> RESTRICTED_STATES = setOf(
            "SUSPENDED", "UNDER_REVIEW", "OFFBOARDING", "TERMINATED"
    );

    /** Nhóm role hạn chế nhẹ (vẫn cho tạo đơn cá nhân). */
    private static final Set<String> LIMITED_ROLES = setOf(
            "INTERN", "PROBATION"
    );

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Không cache để KPI/Inbox luôn mới
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        resp.setHeader("Pragma", "no-cache");

        // 1) Kiểm tra đăng nhập
        User current = (User) req.getSession().getAttribute("currentUser");
        if (current == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        req.setAttribute("currentUser", current);

        // 2) Thời điểm hiện tại (cho UI)
        req.setAttribute("now", new Date());

        // 3) SUY LUẬN TRẠNG THÁI & QUYỀN từ role
        String role = safeUpper(current.getRole());
        String accState = deriveAccountState(role, current); // "", "LIMITED", hoặc giá trị trong RESTRICTED_STATES

        boolean canApprove   = APPROVER_ROLES.contains(role);
        boolean isRestricted = RESTRICTED_STATES.contains(accState);
        boolean isLimited    = "LIMITED".equals(accState) || LIMITED_ROLES.contains(role);

        // Capability cho JSP (portal có thể ẩn/khóa tùy biến)
        boolean canCreateRequest  = !isRestricted;
        boolean canViewMyRequests = !"TERMINATED".equals(accState);
        boolean canUseAgenda      = !"TERMINATED".equals(accState);
        boolean canReceiveNoti    = !"TERMINATED".equals(accState);

        req.setAttribute("accState", accState);
        req.setAttribute("canApprove", canApprove);
        req.setAttribute("cap_canCreate",  canCreateRequest);
        req.setAttribute("cap_canViewMy",  canViewMyRequests);
        req.setAttribute("cap_canAgenda",  canUseAgenda);
        req.setAttribute("cap_canNoti",    canReceiveNoti);

        // 4) Thông tin liên hệ (init-param hoặc fallback)
        String contactHR = getServletContext().getInitParameter("hr.email");
        if (contactHR == null || contactHR.trim().isEmpty()) contactHR = "hradmin@company.com";
        String contactMgr = currentEmailOfManager(current);
        if (contactMgr == null || contactMgr.trim().isEmpty()) contactMgr = "manager@company.com";
        req.setAttribute("contactHR", contactHR);
        req.setAttribute("contactMgr", contactMgr);

        // 5) KPI cho khung trên cùng của Portal Home (khớp JSP: requestScope.kpi)
        Map<String, Object> kpi = new LinkedHashMap<>();
        try {
            // Double al  = balanceService.getAnnualLeaveRemaining(current.getId());
            // int pending = requestService.countByUserAndStatus(current.getId(), "PENDING");
            // int lateM   = attendanceService.countLateInMonth(current.getId(), YearMonth.now());
            // double net  = payrollService.estimateNet(current.getId(), YearMonth.now());
            kpi.put("AL", 8.0);          // phép năm còn
            kpi.put("pending", 2);       // đơn đang chờ
            kpi.put("late", 1);          // đi muộn (tháng)
            kpi.put("net", 15_000_000d); // ước tính net (double)
        } catch (Exception ignore) {
            kpi.put("AL", 0d); kpi.put("pending", 0); kpi.put("late", 0); kpi.put("net", 0d);
        }
        req.setAttribute("kpi", kpi);

        // 6) Hoạt động gần đây (khớp JSP: recentActivities  gồm {type,title,time(Date)})
        List<Map<String, Object>> recentActivities = new ArrayList<>();
        if (canViewMyRequests) {
            recentActivities.add(act("REQUEST", "Đơn nghỉ #1029 - PENDING", daysAgo(0)));
        }
        // có thể bơm thêm: "CHECKIN", "CHECKOUT", "PAYROLL", ...
        req.setAttribute("recentActivities", recentActivities);

        // 7) Trạng thái chấm công hôm nay + quyền bấm (khớp JSP: todaySummary, clock)
        // TODO: thay bằng attendanceService lấy thật theo user & ngày hiện tại
        String todaySummary = "08:32 → — (muộn 2')";
        Map<String, Object> clock = new HashMap<>();
        clock.put("inAllowed",  Boolean.TRUE);   // cho bấm check-in?
        clock.put("outAllowed", Boolean.FALSE);  // cho bấm check-out?
        req.setAttribute("todaySummary", todaySummary);
        req.setAttribute("clock", clock);

        // 8) CSRF token (Portal Home đang post /attendance/clock cần biến 'csrf')
        String csrf = (String) req.getSession().getAttribute("csrf_token");
        if (csrf == null) {
            csrf = UUID.randomUUID().toString();
            req.getSession().setAttribute("csrf_token", csrf);
        }
        req.setAttribute("csrf", csrf);

        // 9) (Tuỳ chọn) dữ liệu khác nếu bạn muốn dùng ở nơi khác trong UI
        //    – giữ lại mấy biến cũ để không ảnh hưởng các trang bạn đã code
        req.setAttribute("myBalances", Map.of("AL", ((Number)kpi.get("AL")).doubleValue()));
        req.setAttribute("myPendingCount",  ((Number)kpi.get("pending")).intValue());
        req.setAttribute("myApprovedCount", 12);  // DEMO
        req.setAttribute("myRejectedCount", 1);   // DEMO
        req.setAttribute("approveInbox",   canApprove && !isRestricted ? demoInbox() : List.of());
        req.setAttribute("notifications",  canReceiveNoti ? demoNoti() : List.of());
        req.setAttribute("serverBalanceSeries", toJsArray(new double[]{8,7.5,7,7,6.5,6,5,4.5,4,3.5,3,2.5}));

        // 10) Forward sang JSP
        //    Nếu bạn dùng Portal riêng: "/WEB-INF/views/portal/home.jsp"
        //    Ở đây để đúng tên file bạn đã show screenshot:
        req.getRequestDispatcher("/WEB-INF/views/portal/home.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Home không có form post; đưa về GET
        doGet(req, resp);
    }

    /* =================== Helpers & DEMO makers =================== */

    private static String safeUpper(String s) {
        return s == null ? "" : s.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * Suy luận trạng thái truy cập:
     * - Nếu role là 1 trong RESTRICTED_STATES -> trả chính code đó.
     * - Nếu role là INTERN/PROBATION -> "LIMITED".
     * - Nếu User.status != 1 (nếu bạn dùng) -> "SUSPENDED".
     * - Mặc định: "" (bình thường).
     */
    private static String deriveAccountState(String role, User u) {
        if (RESTRICTED_STATES.contains(role)) return role;
        if (LIMITED_ROLES.contains(role))     return "LIMITED";
        // Nếu model User có field status (1=active), bảo vệ thêm:
        try {
            Integer st = (Integer) User.class.getMethod("getStatus").invoke(u);
            if (st != null && st != 1) return "SUSPENDED";
        } catch (Exception ignore) { /* không có getter hoặc khác kiểu -> bỏ qua */ }
        return "";
    }

    private static Date daysAgo(int d) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, -d);
        return c.getTime();
    }

    private static Map<String, Object> act(String type, String title, Date time) {
        Map<String, Object> m = new HashMap<>();
        m.put("type",  type);
        m.put("title", title);
        m.put("time",  time);   // CHÚ Ý: để đúng kiểu Date cho fmt:formatDate bên JSP
        return m;
    }

    private static List<Map<String, Object>> demoInbox() {
        List<Map<String, Object>> l = new ArrayList<>();
        l.add(inbox(2011, "Nguyễn Văn A", daysAgo(1), daysAgo(1)));
        l.add(inbox(2008, "Trần Thị B",   daysAgo(3), daysAgo(2)));
        return l;
    }

    private static Map<String, Object> inbox(int id, String userName, Date from, Date to) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("userName", userName);
        m.put("startDate", from);
        m.put("endDate", to);
        return m;
    }

    private static List<Map<String, Object>> demoNoti() {
        List<Map<String, Object>> l = new ArrayList<>();
        l.add(noti("Cập nhật chính sách", "Tăng hạn mức WFH 2 ngày/tháng.", null));
        l.add(noti("Bảo trì hệ thống", "Gián đoạn 15 phút lúc 22:00 đêm nay.", null));
        return l;
    }

    private static Map<String, Object> noti(String title, String body, String linkUrl) {
        Map<String, Object> m = new HashMap<>();
        m.put("title", title);
        m.put("body",  body);
        m.put("linkUrl", linkUrl);
        m.put("createdAt", new Date());
        return m;
    }

    /** Xuất mảng double thành string JS array để in vào JSP. */
    private static String toJsArray(double[] arr) {
        if (arr == null || arr.length == 0) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(arr[i]);
        }
        return sb.append(']').toString();
    }

    /** Email quản lý trực tiếp (nếu bạn có quan hệ trong DB thì thay logic này). */
    private static String currentEmailOfManager(User u) {
        // TODO: thay bằng truy vấn thật, ví dụ userService.findManagerEmail(u.getId())
        return null; // để JSP fallback sang manager@company.com
    }

    /* small util */
    private static Set<String> setOf(String... s) {
        return new HashSet<>(Arrays.asList(s));
    }
}
