package com.acme.leavemgmt.util;

import com.acme.leavemgmt.dao.SysSettingDAO;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

public final class MailSender {
    private static final SysSettingDAO SS = new SysSettingDAO();
    private MailSender(){}

    public static void send(String to, String subject, String body) {
        try {
            if (!SS.getBool("mail_enabled", false)) return;

            String host = SS.get("mail_host");
            int    port = SS.getInt("mail_port", 587);
            String user = SS.get("mail_username");
            String pass = SS.get("mail_password");
            boolean tls = SS.getBool("mail_tls", port != 465);
            String from = SS.get("mail_from") != null ? SS.get("mail_from") : user;
            String name = SS.get("mail_from_name") != null ? SS.get("mail_from_name") : "LeaveMgmt";

            if (host == null || user == null || pass == null)
                throw new RuntimeException("Missing SMTP settings in Sys_Settings");

            Properties props = new Properties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", String.valueOf(port));
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", String.valueOf(tls));

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, pass);
                }
            });

            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from, name, StandardCharsets.UTF_8.name()));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
            msg.setSubject(subject, StandardCharsets.UTF_8.name());
            msg.setText(body, StandardCharsets.UTF_8.name());

            Transport.send(msg);
        } catch (MessagingException e) {
            throw new RuntimeException("Send mail failed (SMTP): " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Send mail failed: " + e.getMessage(), e);
        }
    }
}
