package com.acme.leavemgmt.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * CSRF helper – session token + form/header verification.
 *
 * Cách dùng:
 *  - Ở doGet:  Csrf.protect(req);  // đẩy token xuống request để view render
 *    JSP: <c:set var="csrfParam" value="${requestScope.csrfParam}" />
 *         <c:set var="csrfToken" value="${requestScope.csrfToken}" />
 *         <input type="hidden" name="${csrfParam}" value="${csrfToken}"/>
 *
 *  - Ở doPost: if (!Csrf.isTokenValid(req)) { resp.sendError(400, "CSRF invalid"); return; }
 *
 * Lưu ý:
 *  - Với form multipart, bạn cần parser (Apache FileUpload/Servlet 3.0 parts)
 *    để req.getParameter(...) thấy được field token.
 */
public final class Csrf {

    /* ====== Tên attr / param / header ====== */
    public static final String SESSION_ATTR = "csrf";          // token lưu trong session
    public static final String PARAM        = "_csrf";         // tên input mặc định
    public static final String ALT_PARAM    = "csrf_token";    // alias phổ biến
    public static final String ALT_PARAM_2  = "csrf";          // alias legacy
    public static final String HEADER       = "X-CSRF-Token";
    public static final String ALT_HEADER   = "X-CSRF";

    // Thuận tiện cho view (request attributes)
    public static final String REQ_ATTR_PARAM       = "csrfParam";    // => PARAM
    public static final String REQ_ATTR_VALUE       = "csrfToken";    // => token string
    public static final String REQ_ATTR_OBJ         = "csrfTokenObj"; // => Token object
    public static final String REQ_ATTR_BACKCOMP_1  = "_csrf";        // back-compat
    public static final String REQ_ATTR_BACKCOMP_2  = "csrf_token";
    public static final String REQ_ATTR_BACKCOMP_3  = "csrf";

    private static final SecureRandom RNG = new SecureRandom();

    /** Alias ngắn gọn để kiểm tra token; tương đương {@link #valid(HttpServletRequest)}. */
    public static boolean validate(HttpServletRequest req) {
        return valid(req);
    }

    /** Struct mang xuống view (param + value). */
    public static final class Token {
        private final String param;
        private final String value;
        public Token(String param, String value) { this.param = param; this.value = value; }
        public String getParam() { return param; }
        public String getValue() { return value; }
    }

    /* ================== Public API ================== */

    /** Đảm bảo có token trong session và expose ra request để view render. */
    public static void protect(HttpServletRequest req) { addToken(req); }

    /** Alias: kiểm tra hợp lệ token cho request hiện tại (true/false). */
    public static boolean isTokenValid(HttpServletRequest req) { return valid(req); }

    /** Alias: như trên. */
    public static boolean verify(HttpServletRequest req) { return valid(req); }

    /** Alias cũ. */
    public static boolean verifyToken(HttpServletRequest req) { return valid(req); }

    /** Thực sự đẩy token ra request (và tạo nếu chưa có trong session). */
    public static void addToken(HttpServletRequest req) {
        // BẮT BUỘC tạo/duy trì session khi render form
        HttpSession s = req.getSession(true);
        String t = ensureToken(s);

        // Đưa xuống view – vừa chuẩn vừa back-compat
        req.setAttribute(REQ_ATTR_PARAM, PARAM);
        req.setAttribute(REQ_ATTR_VALUE, t);
        req.setAttribute(REQ_ATTR_OBJ, new Token(PARAM, t));

        // Back-compat aliases cho các view cũ
        req.setAttribute(REQ_ATTR_BACKCOMP_1, t);
        req.setAttribute(REQ_ATTR_BACKCOMP_2, t);
        req.setAttribute(REQ_ATTR_BACKCOMP_3, t);
    }

    /** Lấy/khởi tạo token hiện tại gắn với session của request. */
    static String token(HttpServletRequest req) { return ensureToken(req.getSession(true)); }

    /** Soát vé CSRF (ném SecurityException nếu sai) cho các phương thức không an toàn. */
    public static void verify(HttpServletRequest req, String token) {
        if (isSafeMethod(req.getMethod())) return;

        HttpSession session = req.getSession(false);
        String expected = getToken(session);
        if (isEmpty(expected)) throw new SecurityException("Missing CSRF session token");

        String provided = !isEmpty(token) ? token : firstNonEmpty(
                req.getParameter(PARAM),
                req.getParameter(ALT_PARAM),
                req.getParameter(ALT_PARAM_2),
                req.getHeader(HEADER),
                req.getHeader(ALT_HEADER)
        );
        if (isEmpty(provided) || !constantTimeEquals(expected, provided)) {
            throw new SecurityException("Invalid CSRF token");
        }
    }

    /** (Tiện ích) Ghi luôn thẻ input ẩn vào output stream. */
    public static void writeHiddenInput(HttpServletRequest req, PrintWriter out) {
        String t = token(req);
        out.print("<input type=\"hidden\" name=\"" + PARAM + "\" value=\"" + escapeHtml(t) + "\"/>");
    }

    /* ================== Core logic ================== */

    /** true = hợp lệ; GET/HEAD/OPTIONS luôn hợp lệ. */
    public static boolean valid(HttpServletRequest req) {
        String m = req.getMethod();
        if (isSafeMethod(m)) return true;

        HttpSession session = req.getSession(false);
        String expected = getToken(session);
        if (isEmpty(expected)) return false;

        String provided = firstNonEmpty(
                req.getParameter(PARAM),
                req.getParameter(ALT_PARAM),
                req.getParameter(ALT_PARAM_2),
                req.getHeader(HEADER),
                req.getHeader(ALT_HEADER)
        );
        return !isEmpty(provided) && constantTimeEquals(expected, provided);
    }

    /** Đảm bảo session có token, trả về token hiện tại. */
    public static String ensureToken(HttpSession s) {
        if (s == null) return newToken(); // không set được – caller nên gọi getSession(true)
        Object t = s.getAttribute(SESSION_ATTR);
        if (!(t instanceof String) || isEmpty((String) t)) {
            String nt = newToken();
            s.setAttribute(SESSION_ATTR, nt);
            return nt;
        }
        return (String) t;
    }

    /** Lấy token hiện có (có thể null). */
    public static String getToken(HttpSession s) {
        return (s == null) ? null : (String) s.getAttribute(SESSION_ATTR);
    }

    /** Xoay token mới (gọi sau login/logout/đổi quyền). */
    public static void rotate(HttpSession s) {
        if (s != null) s.setAttribute(SESSION_ATTR, newToken());
    }

    /* ================== Helpers ================== */

    private static String newToken() {
        byte[] b = new byte[32];  // 256-bit
        RNG.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    private static boolean isSafeMethod(String m) {
        return "GET".equalsIgnoreCase(m)
            || "HEAD".equalsIgnoreCase(m)
            || "OPTIONS".equalsIgnoreCase(m);
    }

    private static String firstNonEmpty(String... arr) {
        if (arr == null) return null;
        for (String s : arr) if (!isEmpty(s)) return s;
        return null;
    }

    private static boolean isEmpty(String s) { return s == null || s.isEmpty(); }

    /** So sánh hằng-thời-gian để hạn chế timing attacks. */
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

    /** Rất tối thiểu – đủ cho hidden input. */
    private static String escapeHtml(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&': sb.append("&amp;"); break;
                case '<': sb.append("&lt;");  break;
                case '>': sb.append("&gt;");  break;
                case '"': sb.append("&quot;");break;
                case '\'':sb.append("&#x27;");break;
                default:  sb.append(c);
            }
        }
        return sb.toString();
    }

    private Csrf() {}
}
