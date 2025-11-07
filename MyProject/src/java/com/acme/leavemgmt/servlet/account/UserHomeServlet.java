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
 * Nạp các số liệu cá nhân: balance phép, counts, đơn gần đây,
 * inbox duyệt (nếu có quyền), thông báo, sparkline series, và
 * xuất các cờ capability theo role/trạng thái để JSP ẩn/khóa UI.
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

        // 3) SUY LUẬN TRẠNG THÁI & QUYỀN từ role (không phụ thuộc field lạ)
        String role = safeUpper(current.getRole());
        String accState = deriveAccountState(role, current); // "", "LIMITED", hoặc 1 trong RESTRICTED_STATES

        boolean canApprove = APPROVER_ROLES.contains(role);
        boolean isRestricted = RESTRICTED_STATES.contains(accState); // tạm ngưng/khóa/offboarding/under_review
        boolean isLimited    = "LIMITED".equals(accState) || LIMITED_ROLES.contains(role);

        // Capability cho JSP
        boolean canCreateRequest   = !isRestricted;                // restricted thì không được tạo
        boolean canViewMyRequests  = !"TERMINATED".equals(accState); // terminated thì không cho xem gì
        boolean canUseAgenda       = ! "TERMINATED".equals(accState);
        boolean canReceiveNoti     = ! "TERMINATED".equals(accState);

        req.setAttribute("accState", accState);
        req.setAttribute("canApprove", canApprove);
        req.setAttribute("cap_canCreate",  canCreateRequest);
        req.setAttribute("cap_canViewMy",  canViewMyRequests);
        req.setAttribute("cap_canAgenda",  canUseAgenda);
        req.setAttribute("cap_canNoti",    canReceiveNoti);

        // 4) Thông tin liên hệ (từ web.xml <context-param> nếu có, hoặc fallback)
     String contactHR = getServletContext().getInitParameter("hr.email");
if (contactHR == null || contactHR.trim().isEmpty()) {
    contactHR = "hradmin@company.com";
}
String contactMgr = currentEmailOfManager(current);
if (contactMgr == null || contactMgr.trim().isEmpty()) {
    contactMgr = "manager@company.com";
}
req.setAttribute("contactHR", contactHR);
req.setAttribute("contactMgr", contactMgr);


        // 5) Balance phép năm (AL) – thay bằng gọi service thực của bạn
        Map<String, Double> myBalances = new HashMap<>();
        try {
            // Double al = balanceService.getAnnualLeaveRemaining(current.getId());
            // myBalances.put("AL", al != null ? al : 0d);
            myBalances.put("AL", 8.5d); // DEMO fallback
        } catch (Exception ex) {
            myBalances.put("AL", 0d);
        }
        req.setAttribute("myBalances", myBalances);

        // 6) Đếm đơn theo trạng thái – thay bằng DAO thực tế
        int myPendingCount = 0, myApprovedCount = 0, myRejectedCount = 0;
        try {
            // myPendingCount  = requestService.countByUserAndStatus(current.getId(), "pending");
            // myApprovedCount = requestService.countByUserAndStatus(current.getId(), "approved");
            // myRejectedCount = requestService.countByUserAndStatus(current.getId(), "rejected");
            myPendingCount = 2; myApprovedCount = 12; myRejectedCount = 1; // DEMO
        } catch (Exception ignore) { }
        req.setAttribute("myPendingCount",  myPendingCount);
        req.setAttribute("myApprovedCount", myApprovedCount);
        req.setAttribute("myRejectedCount", myRejectedCount);

        // 7) Đơn gần đây của chính user – thay bằng DAO thực tế
        List<Map<String, Object>> recentRequests = new ArrayList<>();
        if (canViewMyRequests) {
            try {
                // List<Request> list = requestService.listRecentByUser(current.getId(), 10);
                // req.setAttribute("recentRequests", list);
                recentRequests.add(makeReq(1012, "Annual", daysAgo(2), daysAgo(1), "PENDING"));
                recentRequests.add(makeReq(1007, "Sick",   daysAgo(10), daysAgo(9), "APPROVED"));
                recentRequests.add(makeReq(1003, "WFH",    daysAgo(15), daysAgo(15), "REJECTED"));
            } catch (Exception ignore) { }
        }
        req.setAttribute("recentRequests", recentRequests);

        // 8) Inbox duyệt nếu có quyền
        List<Map<String, Object>> approveInbox = new ArrayList<>();
        if (canApprove && !isRestricted) {
            try {
                // approveInbox = approvalService.listInbox(current.getId(), 10);
                approveInbox.add(makeInbox(2011, "Nguyễn Văn A", daysAgo(1), daysAgo(1)));
                approveInbox.add(makeInbox(2008, "Trần Thị B",   daysAgo(3), daysAgo(2)));
            } catch (Exception ignore) { }
        }
        req.setAttribute("approveInbox", approveInbox);

        // 9) Thông báo gần đây – thay bằng DAO thực tế
        List<Map<String, Object>> notifications = new ArrayList<>();
        if (canReceiveNoti) {
            try {
                // notifications = notiService.listForUser(current.getId(), 10);
                notifications.add(makeNoti("Cập nhật chính sách", "Tăng hạn mức WFH 2 ngày/tháng.", null));
                notifications.add(makeNoti("Bảo trì hệ thống", "Gián đoạn 15 phút lúc 22:00 đêm nay.", null));
            } catch (Exception ignore) { }
        }
        req.setAttribute("notifications", notifications);

        // 10) Dữ liệu sparkline cho chart nhỏ – mảng 12 điểm
        double[] balanceSeries = new double[]{8,7.5,7,7,6.5,6,5,4.5,4,3.5,3,2.5};
        req.setAttribute("serverBalanceSeries", toJsArray(balanceSeries));

        // 11) Forward sang JSP user
        req.getRequestDispatcher("/WEB-INF/views/user/home.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Home không có form post; đưa về GET cho đơn giản.
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

        // Nếu model User có field status (1=active), ta bảo vệ thêm:
        try {
            Integer st = (Integer) User.class.getMethod("getStatus").invoke(u);
            if (st != null && st != 1) return "SUSPENDED";
        } catch (Exception ignore) { /* không có getter hoặc khác kiểu -> bỏ qua */ }

        return "";
    }

    private static boolean hasApproveRole(User u) {
        String r = safeUpper(u == null ? null : u.getRole());
        return APPROVER_ROLES.contains(r);
    }

    private static Date daysAgo(int d) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, -d);
        return c.getTime();
    }

    private static Map<String, Object> makeReq(int id, String type, Date from, Date to, String status) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("type", type);
        m.put("startDate", from);
        m.put("endDate", to);
        m.put("status", status);
        return m;
    }

    private static Map<String, Object> makeInbox(int id, String userName, Date from, Date to) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("userName", userName);
        m.put("startDate", from);
        m.put("endDate", to);
        return m;
    }

    private static Map<String, Object> makeNoti(String title, String body, String linkUrl) {
        Map<String, Object> m = new HashMap<>();
        m.put("title", title);
        m.put("body", body);
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
