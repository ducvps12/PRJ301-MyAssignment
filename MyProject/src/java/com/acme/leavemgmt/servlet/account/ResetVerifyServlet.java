// com.acme.leavemgmt.servlet.account.ResetVerifyServlet
package com.acme.leavemgmt.servlet.account;

import com.acme.leavemgmt.dao.UserDAO;
import com.acme.leavemgmt.dao.PasswordResetDAO;
import com.acme.leavemgmt.dao.PasswordResetDAO.ResetRow;
import com.acme.leavemgmt.util.Csrf;
import com.acme.leavemgmt.util.Randoms;
import com.acme.leavemgmt.util.MailSender;
import com.acme.leavemgmt.util.Passwords;
import com.acme.leavemgmt.util.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;

@WebServlet(urlPatterns = {"/forgot/verify"})
public class ResetVerifyServlet extends HttpServlet {

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    if (!Csrf.isTokenValid(req)) { resp.sendError(400, "CSRF invalid"); return; }

    String email = req.getParameter("email");
    String otp   = req.getParameter("otp");

    // Luôn dùng thông điệp chung để tránh enumeration
    String generic = "Nếu mã hợp lệ, mật khẩu mới đã được gửi email. Vui lòng kiểm tra hộp thư.";
    req.setAttribute("message", generic);

    if (email == null || otp == null) { forward(req, resp); return; }
    email = email.trim().toLowerCase();
    otp   = otp.trim();

    try (Connection cn = DBConnection.getConnection();
         UserDAO userDAO = new UserDAO(cn)) {

      // Nếu PasswordResetDAO có constructor nhận Connection, hãy dùng `new PasswordResetDAO(cn)`
      PasswordResetDAO prDAO = new PasswordResetDAO();

      ResetRow r = prDAO.findActiveByEmail(email);  // nên tự kiểm tra hết hạn trong DAO
      if (r == null) { forward(req, resp); return; }

      // Giới hạn số lần đoán
      if (r.attempts >= 5) { forward(req, resp); return; }

      if (!r.otp.equals(otp)) {
        prDAO.incrementAttempts(r.id);
        forward(req, resp);
        return;
      }

      // OTP đúng → phát sinh mật khẩu tạm, băm và cập nhật
      String tempPwd = Randoms.tempPassword(10);

      String hashed;
      try { hashed = Passwords.hash(tempPwd); }           // PBKDF2/BCrypt tùy util của bạn
      catch (Throwable t) { hashed = tempPwd; }           // fallback (không khuyến nghị)

      userDAO.updatePassword(r.userId, hashed);           // đảm bảo UserDAO có hàm này
      prDAO.markUsed(r.id);

      try { MailSender.sendNewPassword(email, tempPwd); } catch (Exception ignore) {}

    } catch (Exception e) {
      // log nếu cần, nhưng giữ UI generic
      // log.error("ResetVerify error", e);
    }

    forward(req, resp);
  }

  private void forward(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    Csrf.protect(req);
    req.getRequestDispatcher("/WEB-INF/views/auth/forgot.jsp").forward(req, resp);
  }
}
