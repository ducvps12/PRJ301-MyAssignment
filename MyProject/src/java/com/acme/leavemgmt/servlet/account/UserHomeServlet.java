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
 * UserHomeServlet – Trang Home tiện ích sau đăng nhập cho người dùng.
 * Nạp các số liệu: balance phép, counts theo trạng thái, đơn gần đây,
 * inbox duyệt (nếu có quyền), thông báo, và sparkline series.
 *
 * URL: /user/home
 *
 * LƯU Ý: Nếu web.xml có metadata-complete="true" thì bắt buộc mapping servlet này trong web.xml.
 */
@WebServlet("/user/home")
public class UserHomeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // ======= Gợi ý Service/DAO (nếu dự án bạn đã có thì thay thế ở dưới) =======
    // private final RequestService       requestService  = new RequestService();
    // private final NotificationService  notiService     = new NotificationService();
    // private final ApprovalService      approvalService = new ApprovalService();
    // private final LeaveBalanceService  balanceService  = new LeaveBalanceService();

    // Nhóm role có quyền duyệt
    private static final Set<String> APPROVER_ROLES = new HashSet<>(Arrays.asList(
            "TEAM_LEAD", "DIV_LEADER", "HR_ADMIN", "MANAGER"
    ));

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
        // Cho JSP truy cập nhanh (nhiều file dùng ${currentUser})
        req.setAttribute("currentUser", current);

        // 2) Thời điểm hiện tại (cho UI)
        req.setAttribute("now", new Date());

        // 3) Balance phép năm (AL) – thay bằng gọi service thực của bạn
        Map<String, Double> myBalances = new HashMap<>();
        try {
            // Double al = balanceService.getAnnualLeaveRemaining(current.getId());
            // myBalances.put("AL", al != null ? al : 0d);
            myBalances.put("AL", 8.5d); // DEMO fallback
        } catch (Exception ex) {
            myBalances.put("AL", 0d);
        }
        req.setAttribute("myBalances", myBalances);

        // 4) Đếm đơn theo trạng thái – thay bằng DAO thực tế
        int myPendingCount = 0, myApprovedCount = 0, myRejectedCount = 0;
        try {
            // myPendingCount  = requestService.countByUserAndStatus(current.getId(), "pending");
            // myApprovedCount = requestService.countByUserAndStatus(current.getId(), "approved");
            // myRejectedCount = requestService.countByUserAndStatus(current.getId(), "rejected");
            myPendingCount = 2; myApprovedCount = 12; myRejectedCount = 1; // DEMO
        } catch (Exception ignore) { /* giữ mặc định 0 nếu lỗi */ }

        req.setAttribute("myPendingCount",  myPendingCount);
        req.setAttribute("myApprovedCount", myApprovedCount);
        req.setAttribute("myRejectedCount", myRejectedCount);

        // 5) Đơn gần đây của chính user – thay bằng DAO thực tế
        List<Map<String, Object>> recentRequests = new ArrayList<>();
        try {
            // List<Request> list = requestService.listRecentByUser(current.getId(), 10);
            // req.setAttribute("recentRequests", list);
            recentRequests.add(makeReq(1012, "Annual", daysAgo(2), daysAgo(1), "PENDING"));
            recentRequests.add(makeReq(1007, "Sick",   daysAgo(10), daysAgo(9), "APPROVED"));
            recentRequests.add(makeReq(1003, "WFH",    daysAgo(15), daysAgo(15), "REJECTED"));
        } catch (Exception ignore) { }
        req.setAttribute("recentRequests", recentRequests);

        // 6) Inbox duyệt nếu có quyền
        boolean canApprove = hasApproveRole(current);
        req.setAttribute("canApprove", canApprove);

        List<Map<String, Object>> approveInbox = new ArrayList<>();
        if (canApprove) {
            try {
                // approveInbox = approvalService.listInbox(current.getId(), 10);
                approveInbox.add(makeInbox(2011, "Nguyễn Văn A", daysAgo(1), daysAgo(1)));
                approveInbox.add(makeInbox(2008, "Trần Thị B",   daysAgo(3), daysAgo(2)));
            } catch (Exception ignore) { }
        }
        req.setAttribute("approveInbox", approveInbox);

        // 7) Thông báo gần đây – thay bằng DAO thực tế
        List<Map<String, Object>> notifications = new ArrayList<>();
        try {
            // notifications = notiService.listForUser(current.getId(), 10);
            notifications.add(makeNoti("Cập nhật chính sách", "Tăng hạn mức WFH 2 ngày/tháng.", null));
            notifications.add(makeNoti("Bảo trì hệ thống", "Gián đoạn 15 phút lúc 22:00 đêm nay.", null));
        } catch (Exception ignore) { }
        req.setAttribute("notifications", notifications);

        // 8) Dữ liệu sparkline cho chart nhỏ – mảng 12 điểm
        double[] balanceSeries = new double[]{8,7.5,7,7,6.5,6,5,4.5,4,3.5,3,2.5};
        req.setAttribute("serverBalanceSeries", toJsArray(balanceSeries));

        // 9) Forward sang JSP
        req.getRequestDispatcher("/WEB-INF/views/user/home.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Home không có form post riêng; đưa về GET cho đơn giản.
        doGet(req, resp);
    }

    /* =================== Helpers & DEMO makers =================== */

    private static boolean hasApproveRole(User u) {
        if (u == null || u.getRole() == null) return false;
        String r = u.getRole().trim().toUpperCase(Locale.ROOT);
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

    /** Xuất mảng double thành JS array string để in vào JSP nếu cần. */
    private static String toJsArray(double[] arr) {
        if (arr == null || arr.length == 0) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(arr[i]);
        }
        return sb.append(']').toString();
    }
}
