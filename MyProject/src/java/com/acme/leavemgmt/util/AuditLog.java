package com.acme.leavemgmt.util;

import com.acme.leavemgmt.dao.ActivityDAO;
import com.acme.leavemgmt.model.Activity;
import com.acme.leavemgmt.model.User;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Ghi log hoạt động của user vào bảng User_Activity
 * Dùng chung cho toàn hệ thống.
 */
public final class AuditLog {

    // Dùng 1 instance DAO dùng lại
    private static final ActivityDAO dao = new ActivityDAO();

    private AuditLog() {
    }

    /* =========================================================
     * 1. HÀM CHÍNH – đã ổn từ đầu của bạn
     * ========================================================= */
    /**
     * Ghi log với đầy đủ thông tin từ request.
     *
     * @param req        HttpServletRequest – để lấy currentUser, IP, UA
     * @param action     hành động, ví dụ: "LOGIN", "LOGIN_FAIL", "ADMIN_USERS_VIEW"
     * @param entityType loại đối tượng tác động, ví dụ: "USER", "REQUEST", "SETTING"
     * @param entityId   id của đối tượng (có thể null)
     * @param note       ghi chú thêm (có thể null)
     */
    public static void log(HttpServletRequest req,
                           String action,
                           String entityType,
                           Integer entityId,
                           String note) {
        try {
            User cu = (User) req.getSession().getAttribute("currentUser");

            Activity a = new Activity();
            a.setUserId(cu != null ? cu.getId() : null);
            a.setAction(safe(action, 50));
            a.setEntityType(safe(entityType, 30));
            a.setEntityId(entityId);
            a.setNote(safe(note, 1000));
            a.setIpAddr(getIp(req));
            a.setUserAgent(getUA(req));

            dao.insert(a);
        } catch (Exception ignore) {
            // tuyệt đối không để crash luồng chính vì lỗi ghi log
        }
    }

    /* =========================================================
     * 2. OVERLOAD 1 – NetBeans sinh ra, bạn bảo "hoàn thiện" cái này
     *    public static void log(HttpServletRequest req,
     *        String login_fail, String user, Object object, String sai_tài_khoảnmật_khẩu)
     *    => Mình hiểu bạn muốn 1 hàm "log login fail" linh hoạt.
     *    => Mình sẽ convert nó thành gọi hàm chính ở trên.
     * ========================================================= */
    public static void log(HttpServletRequest req,
                           String action,
                           String entityType,
                           Object target,
                           String note) {
        // target có thể là id (Integer) hoặc username (String)
        Integer entityId = null;
        String finalNote = note;

        if (target != null) {
            if (target instanceof Integer) {
                entityId = (Integer) target;
            } else {
                // target là text (username, email, …) -> append vào note
                String extra = target.toString();
                if (finalNote == null || finalNote.isBlank()) {
                    finalNote = extra;
                } else {
                    finalNote = finalNote + " | " + extra;
                }
            }
        }

        log(req, action, entityType, entityId, finalNote);
    }

    /* =========================================================
     * 3. OVERLOAD 2 – dùng khi KHÔNG có HttpServletRequest
     *    public static void log(int id, String admin_user_detail_view, String string,
     *                           String remoteAddr, String header)
     *    => thường dùng ở chỗ service / scheduler / filter đặc biệt
     * ========================================================= */
    public static void log(int userId,
                           String action,
                           String entityType,
                           String ip,
                           String userAgent) {
        try {
            Activity a = new Activity();
            a.setUserId(userId);
            a.setAction(safe(action, 50));
            a.setEntityType(safe(entityType, 30));
            a.setEntityId(null);
            a.setNote(null);
            a.setIpAddr(safe(ip, 45));
            a.setUserAgent(safe(userAgent, 255));

            dao.insert(a);
        } catch (Exception ignore) {
        }
    }

    /* =========================================================
     * 4. OVERLOAD 3 – đơn giản nhất: chỉ có userId + action + entityType
     *    public static void log(int id, String admin_user_resetpw, String string)
     *    => Ví dụ: AuditLog.log(adminId, "ADMIN_USER_RESETPW", "USER");
     * ========================================================= */
    public static void log(int userId,
                           String action,
                           String entityType) {
        // gọi overload phía trên với IP/UA = null
        log(userId, action, entityType, null, null);
    }

    /* =========================================================
     * 5. Helper lấy IP & User-Agent
     * ========================================================= */
    private static String getIp(HttpServletRequest r) {
        if (r == null) return null;
        String xff = r.getHeader("X-Forwarded-For");
        return (xff != null && !xff.isBlank())
                ? xff.split(",")[0].trim()
                : r.getRemoteAddr();
    }

    private static String getUA(HttpServletRequest r) {
        if (r == null) return null;
        String ua = r.getHeader("User-Agent");
        return safe(ua, 255);
    }

    private static String safe(String s, int max) {
        if (s == null) return null;
        if (s.length() <= max) return s;
        return s.substring(0, max);
    }

    /* =========================================================
     * 6. Bộ constant tên hành động – để code servlet gọn
     * ========================================================= */
    public static final class Event {
        private Event() {}

        // login
        public static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";
        public static final String LOGIN_FAIL = "LOGIN_FAIL";
        public static final String LOGOUT = "LOGOUT";

        // admin – user
        public static final String ADMIN_USERS_VIEW = "ADMIN_USERS_VIEW";
        public static final String ADMIN_USER_DETAIL_VIEW = "ADMIN_USER_DETAIL_VIEW";
        public static final String ADMIN_USER_CREATE = "ADMIN_USER_CREATE";
        public static final String ADMIN_USER_UPDATE = "ADMIN_USER_UPDATE";
        public static final String ADMIN_USER_RESETPW = "ADMIN_USER_RESETPW";
        public static final String ADMIN_USER_DEACTIVATE = "ADMIN_USER_DEACTIVATE";
        public static final String ADMIN_USER_ACTIVATE = "ADMIN_USER_ACTIVATE";

        // setting
        public static final String ADMIN_SETTING_CREATE = "ADMIN_SETTING_CREATE";
        public static final String ADMIN_SETTING_UPDATE = "ADMIN_SETTING_UPDATE";

        // request (đơn nghỉ)
        public static final String REQUEST_CREATE = "REQUEST_CREATE";
        public static final String REQUEST_APPROVE = "REQUEST_APPROVE";
        public static final String REQUEST_REJECT = "REQUEST_REJECT";
        public static final String REQUEST_UPDATE = "REQUEST_UPDATE";
        public static final String REQUEST_VIEW = "REQUEST_VIEW";
    }
}
