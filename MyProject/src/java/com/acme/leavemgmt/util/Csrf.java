package com.acme.leavemgmt.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * CSRF helper – session token + form/header verification.
 *
 * - Lưu token trong session (attr: "csrf").
 * - Client gửi lại qua input name "_csrf" hoặc header "X-CSRF-Token"
 *   (cũng chấp nhận "csrf_token" / "csrf" và header "X-CSRF").
 * - GET/HEAD/OPTIONS -> bỏ qua (always valid).
 */
public final class Csrf {

    // Tên attr / param / header chuẩn
    public static final String ATTR        = "csrf";
    public static final String PARAM       = "_csrf";          // tên input mặc định
    public static final String ALT_PARAM   = "csrf_token";     // tên input phổ biến
    public static final String ALT_PARAM_2 = "csrf";           // tên input legacy
    public static final String HEADER      = "X-CSRF-Token";
    public static final String ALT_HEADER  = "X-CSRF";

    private static final SecureRandom RNG = new SecureRandom();

    /* ---------- Aliases & helpers public API ---------- */

    /** Alias: kiểm tra hợp lệ token cho request hiện tại (true/false). */
    public static boolean isTokenValid(HttpServletRequest req) { return valid(req); }

    /** Đảm bảo có token trong session và đẩy xuống request để JSP render. */
    public static void addToken(HttpServletRequest req) {
        String t = ensureToken(req.getSession());
        // expose theo nhiều key để các form cũ/mới đều dùng được
        req.setAttribute("csrf_token", t);
        req.setAttribute("_csrf", t);
        req.setAttribute(ATTR, t);
    }

    /** Lấy/khởi tạo token hiện tại gắn với session của request. */
    static String token(HttpServletRequest req) { return ensureToken(req.getSession()); }

    /** Gọi ở doGet để chắc chắn view luôn có token render. */
    public static void protect(HttpServletRequest req) { addToken(req); }

    /** Trả về true/false khi kiểm tra token từ param/header. */
    public static boolean verify(HttpServletRequest req) { return valid(req); }

    /** Alias cũ, tương đương verify(req). */
    public static boolean verifyToken(HttpServletRequest req) { return valid(req); }

    /* ========= Core logic ========= */

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

    /** Xoay token mới (gọi sau login/logout/đổi quyền). */
    public static void rotate(HttpSession s) {
        if (s != null) s.setAttribute(ATTR, newToken());
    }

    /**
     * Kiểm tra CSRF cho request.
     * - Với GET/HEAD/OPTIONS: luôn true.
     * - Với method khác: so sánh token session với form param hoặc header.
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
                req.getParameter(ALT_PARAM_2),    // csrf (legacy)
                req.getHeader(HEADER),            // X-CSRF-Token
                req.getHeader(ALT_HEADER)         // X-CSRF
        );
        if (provided == null) return false;

        return constantTimeEquals(expected, provided);
    }

    /* ===== Implement các method còn thiếu (compat) ===== */

    /** Compat: alias cho valid(req). */
    public static boolean validate(HttpServletRequest req) {
        return valid(req);
    }

    /**
     * Kiểm tra với token truyền vào sẵn:
     * - Nếu method safe -> return (không ném).
     * - Nếu token null/rỗng -> fallback đọc từ param/header.
     * - Nếu sai -> ném SecurityException (để servlet/bộ lọc bắt và trả 403).
     */
    public static void verify(HttpServletRequest req, String token) {
        if (isSafeMethod(req.getMethod())) return; // không yêu cầu CSRF cho GET/HEAD/OPTIONS

        HttpSession session = req.getSession(false);
        String expected = getToken(session);
        if (expected == null || expected.isEmpty()) {
            throw new SecurityException("Missing CSRF session token");
        }

        String provided = (token != null && !token.isEmpty()) ? token : firstNonEmpty(
                req.getParameter(PARAM),
                req.getParameter(ALT_PARAM),
                req.getParameter(ALT_PARAM_2),
                req.getHeader(HEADER),
                req.getHeader(ALT_HEADER)
        );

        if (provided == null || !constantTimeEquals(expected, provided)) {
            throw new SecurityException("Invalid CSRF token");
        }
    }

    /* ===== internal helpers ===== */

    private static boolean isSafeMethod(String m) {
        return "GET".equalsIgnoreCase(m)
            || "HEAD".equalsIgnoreCase(m)
            || "OPTIONS".equalsIgnoreCase(m);
    }

    private static String firstNonEmpty(String... arr) {
        if (arr == null) return null;
        for (String s : arr) if (s != null && !s.isEmpty()) return s;
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

    private Csrf() {}
}
