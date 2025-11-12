// com.acme.leavemgmt.servlet.account.ForgotPasswordServlet
package com.acme.leavemgmt.servlet.account;

import com.acme.leavemgmt.dao.UserDAO;
import com.acme.leavemgmt.dao.PasswordResetDAO;
import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.Csrf;
import com.acme.leavemgmt.util.Randoms;
import com.acme.leavemgmt.util.MailSender;
import com.acme.leavemgmt.util.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.Instant;

@WebServlet(urlPatterns = {"/forgot"})
public class ForgotPasswordServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    // đẩy csrfParam/csrfToken xuống view
    Csrf.protect(req);
    req.getRequestDispatcher("/WEB-INF/views/auth/forgot.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    if (!Csrf.isTokenValid(req)) { resp.sendError(400, "CSRF invalid"); return; }

    String email = req.getParameter("email");
    String ip    = req.getRemoteAddr();

    // Luôn hiển thị cùng một thông điệp để tránh dò email
    String generic = "Nếu email tồn tại, chúng tôi đã gửi mã xác minh. Vui lòng kiểm tra hộp thư.";
    req.setAttribute("message", generic);

    if (email == null || email.isBlank()) {
      doGet(req, resp); return;
    }
    email = email.trim().toLowerCase();

    try (Connection cn = DBConnection.getConnection();
         UserDAO userDAO = new UserDAO(cn)) {

      // NOTE: nếu PasswordResetDAO của bạn có constructor nhận Connection,
      // hãy dùng: new PasswordResetDAO(cn). Nếu không có, giữ mặc định.
      PasswordResetDAO prDAO = new PasswordResetDAO();

      User u = userDAO.findByEmail(email);
      if (u == null) { doGet(req, resp); return; } // vẫn trả UI generic

      // rate limit: tối đa 3 yêu cầu trong 15 phút
      if (prDAO.countRecentRequests(email) >= 3) {
        doGet(req, resp); return; // UI generic
      }

      String otp = Randoms.otp6();
      Timestamp exp = Timestamp.from(Instant.now().plusSeconds(10 * 60));

      prDAO.insert(u.getUserId(), email, otp, exp, ip);

      // mask email cho subtitle trong template nếu cần
      String masked = email.replaceAll("(^.).*(@.*$)", "$1***$2");
      try { MailSender.sendOtp(email, masked, otp); } catch (Exception ignore) {}

    } catch (Exception e) {
      // có thể ghi log, nhưng UI vẫn để generic để tránh lộ thông tin
      // log.error("Forgot password error", e);
    }

    doGet(req, resp);
  }
}
