package com.acme.leavemgmt.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * CSRF helper – Session token + form/header verification.
 *
 * - Lưu token trong session (attr: "csrf").
 * - Client gửi lại qua input name "_csrf" hoặc header "X-CSRF-Token".
 * - Bỏ qua kiểm tra với GET/HEAD/OPTIONS.
 * - Nên gọi rotate() khi login/logout để thay token.
 */
public final class Csrf {
    private Csrf() {}

    public static final String ATTR   = "csrf";
    public static final String PARAM  = "_csrf";
    public static final String HEADER = "X-CSRF-Token";

    private static final SecureRandom RNG = new SecureRandom();

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
     * - Với các method khác: so sánh token session với form param "_csrf" hoặc header "X-CSRF-Token".
     */
    public static boolean valid(HttpServletRequest req) {
        String m = req.getMethod();
        if (isSafeMethod(m)) return true;

        HttpSession session = req.getSession(false);
        String expected = getToken(session);
        if (expected == null || expected.isEmpty()) return false;

        String provided = firstNonEmpty(
                req.getParameter(PARAM),
                req.getHeader(HEADER)
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

    private static String firstNonEmpty(String a, String b) {
        if (a != null && !a.isEmpty()) return a;
        if (b != null && !b.isEmpty()) return b;
        return null;
    }

    /** So sánh hằng thời gian để hạn chế timing attacks. */
    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        int len = Math.max(a.length(), b.length());
        int diff = a.length() ^ b.length();
        for (int i = 0; i < len; i++) {
            char ca = i < a.length() ? a.charAt(i) : 0;
            char cb = i < b.length() ? b.charAt(i) : 0;
            diff |= (ca ^ cb);
        }
        return diff == 0;
    }
}
