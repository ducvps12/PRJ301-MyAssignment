package com.acme.leavemgmt.servlet.auth;

import com.acme.leavemgmt.dao.UserDAO;
import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.Csrf;
import com.acme.leavemgmt.util.Passwords;
import com.acme.leavemgmt.util.ResponseUtils;
import com.acme.leavemgmt.util.MailSender;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Objects;

@WebServlet(urlPatterns = {"/forgot"})
public class ForgotPasswordServlet extends HttpServlet {

    // ==== Session keys ====
    private static final String S_KEY_EMAIL          = "FP_EMAIL";
    private static final String S_KEY_OTP            = "FP_OTP";
    private static final String S_KEY_OTP_EXPIRE     = "FP_OTP_EXPIRE";
    private static final String S_KEY_OTP_VERIFIED   = "FP_VERIFIED";   // boolean
    private static final String S_KEY_LAST_REQ_MS    = "FP_LAST_REQ";   // cooldown

    // ==== Config ====
    private static final long   OTP_TTL_MS   = 5 * 60 * 1000; // 5 phút
    private static final long   COOLDOWN_MS = 60 * 1000;      // 60s giữa 2 lần gửi
    private static final int    OTP_LEN      = 6;

    private final UserDAO userDAO = new UserDAO();
    private final SecureRandom rnd = new SecureRandom();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Csrf.protect(req); // đẩy csrfParam/csrfToken xuống view
        req.getRequestDispatcher("/WEB-INF/views/auth/forgot.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!Csrf.isTokenValid(req)) {
            resp.sendError(400, "CSRF invalid");
            return;
        }

        final String action = safe(req.getParameter("action")); // request | verify | reset
        switch (action) {
            case "request" -> handleRequestOtp(req, resp);
            case "verify"  -> handleVerifyOtp(req, resp);
            case "reset"   -> handleResetPassword(req, resp);
            default        -> {
                req.setAttribute("error", "Yêu cầu không hợp lệ.");
                doGet(req, resp);
            }
        }
    }

    /* ========== 1) GỬI OTP ========== */
    private void handleRequestOtp(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // honeypot chống bot
        if (!isEmpty(req.getParameter("website")) || !isEmpty(req.getParameter("homepage"))) {
            resp.sendError(400); return;
        }

        final String email = safe(req.getParameter("email"));
        if (email.isBlank()) {
            req.setAttribute("error", "Vui lòng nhập email.");
            doGet(req, resp); return;
        }

        try {
            User u = userDAO.findByEmail(email);
            if (u == null) {
                req.setAttribute("error", "Email không tồn tại trong hệ thống.");
                doGet(req, resp); return;
            }

            HttpSession s = req.getSession(true);

            // cooldown 60s
            Long last = (Long) s.getAttribute(S_KEY_LAST_REQ_MS);
            long now = System.currentTimeMillis();
            if (last != null && now - last < COOLDOWN_MS) {
                long remain = (COOLDOWN_MS - (now - last)) / 1000;
                req.setAttribute("error",
                        "Bạn vừa yêu cầu mã gần đây. Vui lòng thử lại sau " + remain + " giây.");
                doGet(req, resp); return;
            }

            String otp = randomDigits(OTP_LEN);
            long expire = now + OTP_TTL_MS;

            // lưu session
            s.setAttribute(S_KEY_EMAIL, email);
            s.setAttribute(S_KEY_OTP, otp);
            s.setAttribute(S_KEY_OTP_EXPIRE, expire);
            s.setAttribute(S_KEY_OTP_VERIFIED, Boolean.FALSE);
            s.setAttribute(S_KEY_LAST_REQ_MS, now);

            // gửi email
            String subject = "[LeaveMgmt] Mã xác minh đặt lại mật khẩu";
            String body = """
                    Xin chào %s,

                    Mã xác minh (OTP) của bạn là: %s
                    Mã sẽ hết hạn sau 5 phút.

                    Nếu bạn không yêu cầu, vui lòng bỏ qua email này.
                    """.formatted(Objects.toString(u.getFullName(), "bạn"), otp);

            MailSender.send(email, subject, body);

            req.setAttribute("message", "Đã gửi mã OTP tới email. Vui lòng kiểm tra hộp thư.");
            // focus OTP input
            req.setAttribute("otpFocus", "1");
            doGet(req, resp);
        } catch (Exception ex) {
            req.setAttribute("error", "Không thể gửi mã. Vui lòng thử lại sau.");
            doGet(req, resp);
        }
    }

    /* ========== 2) XÁC MINH OTP ========== */
    private void handleVerifyOtp(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        final String otp = safe(req.getParameter("otp"));
        HttpSession s = req.getSession(false);

        if (s == null || s.getAttribute(S_KEY_EMAIL) == null) {
            req.setAttribute("error", "Phiên xác minh đã hết hoặc chưa yêu cầu mã.");
            doGet(req, resp); return;
        }

        String sessOtp = (String) s.getAttribute(S_KEY_OTP);
        Long expire = (Long) s.getAttribute(S_KEY_OTP_EXPIRE);
        if (sessOtp == null || expire == null || Instant.now().toEpochMilli() > expire) {
            req.setAttribute("error", "Mã OTP đã hết hạn. Vui lòng gửi lại mã.");
            doGet(req, resp); return;
        }

        if (!otp.equals(sessOtp)) {
            req.setAttribute("error", "Mã OTP không chính xác.");
            req.setAttribute("otpFocus", "1");
            doGet(req, resp); return;
        }

        s.setAttribute(S_KEY_OTP_VERIFIED, Boolean.TRUE);
        req.setAttribute("message", "Xác minh thành công. Hãy nhập mật khẩu mới.");
        // chuyển focus về ô mật khẩu
        req.setAttribute("otpFocus", "0");
        doGet(req, resp);
    }

    /* ========== 3) ĐẶT LẠI MẬT KHẨU ========== */
    private void handleResetPassword(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute(S_KEY_EMAIL) == null) {
            req.setAttribute("error", "Phiên đặt lại không hợp lệ.");
            doGet(req, resp); return;
        }
        boolean verified = Boolean.TRUE.equals(s.getAttribute(S_KEY_OTP_VERIFIED));
        if (!verified) {
            req.setAttribute("error", "Bạn cần xác minh OTP trước khi đặt lại mật khẩu.");
            doGet(req, resp); return;
        }

        String newPwd = safe(req.getParameter("password"));
        String rePwd  = safe(req.getParameter("repassword"));
        if (newPwd.length() < 6) {
            req.setAttribute("error", "Mật khẩu tối thiểu 6 ký tự.");
            doGet(req, resp); return;
        }
        if (!newPwd.equals(rePwd)) {
            req.setAttribute("error", "Xác nhận mật khẩu không khớp.");
            doGet(req, resp); return;
        }

        String email = (String) s.getAttribute(S_KEY_EMAIL);
        try {
            String hash = Passwords.hash(newPwd); // PBKDF2/SHA256 tuỳ util của bạn
            boolean ok = userDAO.updatePasswordByEmail(email, hash);
            if (!ok) {
                req.setAttribute("error", "Không thể cập nhật mật khẩu.");
                doGet(req, resp); return;
            }

            // xoá dữ liệu phiên quên mật khẩu
            s.removeAttribute(S_KEY_EMAIL);
            s.removeAttribute(S_KEY_OTP);
            s.removeAttribute(S_KEY_OTP_EXPIRE);
            s.removeAttribute(S_KEY_OTP_VERIFIED);

            // chuyển về trang đăng nhập
            ResponseUtils.redirectWithMessage(req, resp,
                    req.getContextPath() + "/login",
                    "Đặt lại mật khẩu thành công. Vui lòng đăng nhập.");
        } catch (Exception e) {
            req.setAttribute("error", "Có lỗi khi đặt lại mật khẩu.");
            doGet(req, resp);
        }
    }

    // ===== Helpers =====
    private String randomDigits(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(rnd.nextInt(10));
        return sb.toString();
    }
    private static boolean isEmpty(String s) { return s == null || s.isBlank(); }
    private static String safe(String s) { return s == null ? "" : s.trim(); }
}
