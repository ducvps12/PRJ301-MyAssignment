// src/main/java/com/acme/leavemgmt/util/MailSender.java
package com.acme.leavemgmt.util;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Properties;

public final class MailSender {
    private MailSender(){}

    /* ===== Helpers ===== */
    private static boolean bool(String key, boolean defVal){ return AppConfig.getBool(key, defVal); }
    private static int intval(String key, int defVal){ return AppConfig.getInt(key, defVal); }

    private static void guardConfig() throws MessagingException {
        if (!AppConfig.isMailEnabled()) throw new MessagingException("Mail disabled by config");
        if (isBlank(AppConfig.get("smtp.host", null))) throw new MessagingException("smtp.host is empty");
        if (isBlank(AppConfig.get("smtp.user", null))) throw new MessagingException("smtp.user is empty");
        if (isBlank(AppConfig.get("smtp.pass", null))) throw new MessagingException("smtp.pass is empty");
    }

    private static Properties smtpProps(){
        String host = AppConfig.get("smtp.host", "smtp.gmail.com");
        int port    = intval("smtp.port", 587);
        boolean ssl = port == 465 || bool("smtp.ssl", false);
        boolean starttls = !ssl && bool("smtp.starttls", true);

        Properties p = new Properties();
        p.put("mail.smtp.host", host);
        p.put("mail.smtp.port", String.valueOf(port));
        p.put("mail.smtp.auth", "true");
        p.put("mail.smtp.starttls.enable", String.valueOf(starttls));
        p.put("mail.smtp.starttls.required", String.valueOf(bool("smtp.starttls.required", false)));
        p.put("mail.smtp.ssl.enable", String.valueOf(ssl));
        // Gmail-friendly
        p.put("mail.smtp.ssl.checkserveridentity", "true");
        p.put("mail.smtp.ssl.trust", host);
        // timeouts
        p.put("mail.smtp.connectiontimeout", "10000");
        p.put("mail.smtp.timeout",           "10000");
        p.put("mail.smtp.writetimeout",      "10000");
        return p;
    }

    private static Session session(){
        Properties p = smtpProps();
        Session s = Session.getInstance(p, new Authenticator() {
            @Override protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        AppConfig.get("smtp.user", ""),
                        AppConfig.get("smtp.pass", "")
                );
            }
        });
        if (AppConfig.getBool("mail.debug", false)) s.setDebug(true);
        return s;
    }

    private static InternetAddress fromAddress() throws UnsupportedEncodingException, AddressException {
        String from     = AppConfig.get("mail.from", AppConfig.get("smtp.user", ""));
        String fromName = AppConfig.get("mail.fromName", "LeaveMgmt");
        InternetAddress ia = new InternetAddress(from);
        if (!isBlank(fromName)) ia.setPersonal(fromName, StandardCharsets.UTF_8.name());
        return ia;
    }

    /* ===== Public API ===== */

    /** Gửi HTML đơn giản. */
    public static void send(String to, String subject, String html) throws MessagingException {
        guardConfig();
        if (isBlank(to)) throw new MessagingException("Recipient is empty");
        try {
            Message m = new MimeMessage(session());
            m.setFrom(fromAddress());
            m.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
            m.setSubject(subject != null ? subject : "");
            m.setSentDate(new Date());
            m.setContent(wrapHtml(html), "text/html; charset=UTF-8");
            Transport.send(m);
        } catch (UnsupportedEncodingException e) {
            throw new MessagingException("Invalid From/Personal", e);
        }
    }

    /** Mẫu gửi OTP. */
    public static void sendOtp(String to, String masked, String otp) throws MessagingException {
        String body = """
            <p>Xin chào,</p>
            <p>Mã xác minh cho tài khoản <b>%s</b> là:</p>
            <h2 style="letter-spacing:2px;margin:12px 0">%s</h2>
            <p>Mã có hiệu lực trong 10 phút. Nếu không phải bạn, hãy bỏ qua email này.</p>
            <p>— LeaveMgmt</p>
        """.formatted(escape(masked), escape(otp));
        send(to, "Mã xác minh đặt lại mật khẩu", body);
    }

    /** Mẫu gửi mật khẩu tạm. */
    public static void sendNewPassword(String to, String tempPwd) throws MessagingException {
        String body = """
            <p>Mật khẩu tạm thời mới của bạn là:</p>
            <h2 style="margin:12px 0">%s</h2>
            <p>Đăng nhập rồi vào <b>Tài khoản → Đổi mật khẩu</b> để thay ngay.</p>
            <p>— LeaveMgmt</p>
        """.formatted(escape(tempPwd));
        send(to, "Mật khẩu tạm thời mới", body);
    }

    /* ===== Small utils ===== */
    private static String wrapHtml(String inner){
        String brand = AppConfig.get("brand.name", AppConfig.get("mail.fromName", "LeaveMgmt"));
        return """
          <!doctype html><html><head><meta charset="utf-8"/>
          <meta name="viewport" content="width=device-width, initial-scale=1"/>
          <title>%s</title></head>
          <body style="background:#f6f7fb;margin:0;padding:24px;font-family:system-ui,-apple-system,Segoe UI,Roboto,Arial,sans-serif;color:#0f172a">
            <div style="max-width:640px;margin:0 auto;background:#fff;border:1px solid #e5e7eb;border-radius:12px;overflow:hidden">
              <div style="padding:14px 18px;background:#111827;color:#fff;font-weight:600">%s</div>
              <div style="padding:18px">%s</div>
              <div style="padding:14px 18px;color:#6b7280;font-size:12px;border-top:1px solid #eef2f7">
                Email này được gửi tự động từ hệ thống %s. Vui lòng không trả lời.
              </div>
            </div>
          </body></html>
        """.formatted(escape(brand), escape(brand), inner == null ? "" : inner, escape(brand));
    }
    private static String escape(String s){
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")
                .replace("\"","&quot;").replace("'","&#39;");
    }
    private static boolean isBlank(String s){ return s == null || s.trim().isEmpty(); }
}
