package com.acme.leavemgmt.servlet.account;

import com.acme.leavemgmt.dao.UserDAO;
import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.Csrf;
import com.acme.leavemgmt.util.MailSender;
import com.acme.leavemgmt.util.Passwords;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.security.SecureRandom;

@WebServlet("/forgot")
public class ForgotPasswordServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Csrf.addToken(req);
        req.getRequestDispatcher("/WEB-INF/views/account/forgot.jsp").forward(req, resp);
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            if (!Csrf.validate(req)) { resp.sendError(400, "CSRF invalid"); return; }

            String email = req.getParameter("email");
            if (email == null || email.trim().isEmpty()) {
                req.setAttribute("error", "Vui lòng nhập email.");
                req.getRequestDispatcher("/WEB-INF/views/account/forgot.jsp").forward(req, resp);
                return;
            }

            User user = userDAO.findByEmail(email.trim().toLowerCase());
            // Không tiết lộ sự tồn tại của user
            if (user == null) {
                req.setAttribute("message", "Nếu email tồn tại, mật khẩu mới đã được gửi. Vui lòng kiểm tra email.");
                req.getRequestDispatcher("/WEB-INF/views/account/forgot.jsp").forward(req, resp);
                return;
            }

            // Tạo mật khẩu tạm và hash bằng util của dự án
            String newPassword = generateSecurePassword(12);
            String hashed = Passwords.hash(newPassword);

            boolean ok = userDAO.updatePassword(user.getId(), hashed);
            if (!ok) {
                req.setAttribute("error", "Không thể cập nhật mật khẩu, hãy thử lại sau.");
                req.getRequestDispatcher("/WEB-INF/views/account/forgot.jsp").forward(req, resp);
                return;
            }

            String subject = "LeaveMgmt - Mật khẩu tạm thời";
            String body = String.format(
                    "Xin chào %s,\n\n" +
                    "Bạn (hoặc ai đó) đã yêu cầu đặt lại mật khẩu. Mật khẩu tạm thời của bạn là:\n\n%s\n\n" +
                    "Vui lòng đăng nhập và đổi mật khẩu ngay lập tức tại mục Tài khoản → Đổi mật khẩu.\n\n" +
                    "Nếu bạn không yêu cầu thao tác này, hãy liên hệ quản trị hệ thống.\n\nTrân trọng,\nLeaveMgmt",
                    user.getFullName() == null ? user.getUsername() : user.getFullName(),
                    newPassword
            );
            MailSender.send(user.getEmail(), subject, body);

            req.setAttribute("message", "Nếu email tồn tại, mật khẩu mới đã được gửi. Vui lòng kiểm tra email.");
            req.getRequestDispatcher("/WEB-INF/views/account/forgot.jsp").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(500, e.getMessage());
        }
    }

    private String generateSecurePassword(int length) {
        final String allowed = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789@#&*!";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) sb.append(allowed.charAt(rnd.nextInt(allowed.length())));
        return sb.toString();
    }
}
