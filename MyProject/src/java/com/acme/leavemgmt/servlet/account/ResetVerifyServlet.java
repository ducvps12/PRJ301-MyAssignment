// com.acme.leavemgmt.servlet.account.ResetVerifyServlet
package com.acme.leavemgmt.servlet.account;


import com.acme.leavemgmt.dao.UserDAO;
import com.acme.leavemgmt.dao.PasswordResetDAO;
import com.acme.leavemgmt.dao.PasswordResetDAO.ResetRow;
import com.acme.leavemgmt.util.Csrf;
import com.acme.leavemgmt.util.Randoms;
import com.acme.leavemgmt.util.MailSender;
import com.acme.leavemgmt.util.Passwords; // your existing hashing helper (or replace with your own)


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;


@WebServlet(urlPatterns = {"/forgot/verify"})
public class ResetVerifyServlet extends HttpServlet {
private final UserDAO userDAO = new UserDAO();
private final PasswordResetDAO prDAO = new PasswordResetDAO();


@Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
if (!Csrf.isTokenValid(req)) { resp.sendError(400, "CSRF invalid"); return; }
String email = req.getParameter("email");
String otp = req.getParameter("otp");


String generic = "Nếu mã hợp lệ, mật khẩu mới đã được gửi email. Vui lòng kiểm tra hộp thư.";
req.setAttribute("message", generic);


if (email == null || otp == null) { forward(req, resp); return; }
email = email.trim().toLowerCase();
otp = otp.trim();


try {
ResetRow r = prDAO.findActiveByEmail(email);
if (r == null) { forward(req, resp); return; }


if (r.attempts >= 5) { forward(req, resp); return; }
if (!r.otp.equals(otp)) {
prDAO.incrementAttempts(r.id);
forward(req, resp); return;
}


// Correct OTP → generate temp password and set
String tempPwd = Randoms.tempPassword(10);
String hashed;
try { hashed = Passwords.hash(tempPwd); } // your helper
catch (Throwable t) { hashed = tempPwd; } // fallback (plain) if helper not present


userDAO.updatePassword(r.userId, hashed);
prDAO.markUsed(r.id);


try { MailSender.sendNewPassword(email, tempPwd); } catch (Exception ignore) {}
} catch (Exception e) {
// log if desired
}


forward(req, resp);
}


private void forward(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
Csrf.protect(req);
req.getRequestDispatcher("/WEB-INF/views/auth/forgot.jsp").forward(req, resp);
}
}