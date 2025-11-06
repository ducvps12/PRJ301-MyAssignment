package com.acme.leavemgmt.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * CSRF helper – session token + form/header verification.
 *
 * - Lưu token trong session (attr: "csrf").
 * - Client gửi lại qua input name "_csrf" hoặc header "X-CSRF-Token" (cũng chấp nhận "csrf_token").
 * - GET/HEAD/OPTIONS -> bỏ qua.
 */
public final class Csrf {

    // tên attr / param / header chuẩn
    public static final String ATTR   = "csrf";
    public static final String PARAM  = "_csrf";          // tên input mặc định
    public static final String ALT_PARAM = "csrf_token";  // tên input mà nhiều form đang dùng
    public static final String HEADER = "X-CSRF-Token";

    private static final SecureRandom RNG = new SecureRandom();

    /* ===================== Bổ sung triển khai 3 method còn thiếu ===================== */

    /** Alias: kiểm tra hợp lệ của token cho request hiện tại. */
    public static boolean isTokenValid(HttpServletRequest req) {
        return valid(req);
    }

    /** Đảm bảo có token trong session và đẩy xuống request để JSP render. */
    public static void addToken(HttpServletRequest req) {
        String t = ensureToken(req.getSession());
        // đặt nhiều key để các form cũ/mới đều tận dụng được
        req.setAttribute("csrf_token", t);
        req.setAttribute("_csrf", t);
        // tuỳ chọn: nếu bạn muốn cũng expose theo tên ATTR
        req.setAttribute(ATTR, t);
    }

    /** Lấy/khởi tạo token hiện tại gắn với session của request. */
    static String token(HttpServletRequest req) {
        return ensureToken(req.getSession());
    }

    /* =======================================================================
       API public để servlet gọi
       ======================================================================= */

    /** Gọi ở doGet: tương tự addToken, giữ lại để tương thích. */
    public static void protect(HttpServletRequest req) {
        HttpSession ses = req.getSession();
        String token = ensureToken(ses);
        req.setAttribute("csrf_token", token);
        req.setAttribute("_csrf", token);
    }

    /** Gọi ở doPost: kiểm tra token gửi lên. */
    public static boolean verify(HttpServletRequest req) {
        return valid(req);
    }

    /** Alias cho verify(...) để tương thích code cũ. */
    public static boolean verifyToken(HttpServletRequest req) {
        return valid(req);
    }

    /* =======================================================================
       Core logic
       ======================================================================= */

    /** Tạo token mới (32 bytes -> ~43 ký tự Base64 URL-safe, không padding). */
    private static String newToken() {
        byte[] b = new byte[32];
        RNG.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    /** Đảm bảo session có token, trả về token hiện tại. */
    public static String ensureToken(HttpSession s) {
        Object t = (s != null) ? s.getAttribute(ATTR) : null;
        if (!(t instanceof String)) {
            String nt = newToken();
            if (s != null) s.setAttribute(ATTR, nt);
            return nt;
        }
        return (String) t;
    }

    /** Lấy token hiện có (có thể null). */
    public static String getToken(HttpSession s) {
        return (s == null) ? null : (String) s.getAttribute(ATTR);
    }

    /** Xoay token mới (khuyến nghị gọi sau login/logout/đổi quyền). */
    public static void rotate(HttpSession s) {
        if (s != null) s.setAttribute(ATTR, newToken());
    }

    /**
     * Kiểm tra CSRF cho request.
     * - Với GET/HEAD/OPTIONS: luôn true.
     * - Với method khác: so sánh token session với form param "_csrf" hoặc "csrf_token"
     *   hoặc header "X-CSRF-Token".
     */
    public static boolean valid(HttpServletRequest req) {
        String m = req.getMethod();
        if (isSafeMethod(m)) return true;

        HttpSession session = req.getSession(false);
        String expected = getToken(session);
        if (expected == null || expected.isEmpty()) return false;

        String provided = firstNonEmpty(
                req.getParameter(PARAM),          // _csrf
                req.getParameter(ALT_PARAM),      // csrf_token
                req.getHeader(HEADER)             // X-CSRF-Token
        );
        if (provided == null) return false;

        return constantTimeEquals(expected, provided);
    }

    /* ===== helpers ===== */

    private static boolean isSafeMethod(String m) {
        return "GET".equalsIgnoreCase(m)
                || "HEAD".equalsIgnoreCase(m)
                || "OPTIONS".equalsIgnoreCase(m);
    }

    private static String firstNonEmpty(String... arr) {
        if (arr == null) return null;
        for (String s : arr) {
            if (s != null && !s.isEmpty()) return s;
        }
        return null;
    }

    /** So sánh hằng thời gian để hạn chế timing attacks. */
    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        int len = Math.max(a.length(), b.length());
        int diff = a.length() ^ b.length();
        for (int i = 0; i < len; i++) {
            char ca = (i < a.length()) ? a.charAt(i) : 0;
            char cb = (i < b.length()) ? b.charAt(i) : 0;
            diff |= (ca ^ cb);
        }
        return diff == 0;
    }

    public static boolean validate(HttpServletRequest req) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private Csrf() {}
}
