// com.acme.leavemgmt.servlet.account.ForgotPasswordServlet
package com.acme.leavemgmt.servlet.account;


import com.acme.leavemgmt.dao.UserDAO;
import com.acme.leavemgmt.dao.PasswordResetDAO;
import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.Csrf;
import com.acme.leavemgmt.util.Randoms;
import com.acme.leavemgmt.util.MailSender;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;


@WebServlet(urlPatterns = {"/forgot"})
public class ForgotPasswordServlet extends HttpServlet {
private final UserDAO userDAO = new UserDAO();
private final PasswordResetDAO prDAO = new PasswordResetDAO();


@Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
Csrf.protect(req); // put csrfParam/csrfToken to request
req.getRequestDispatcher("/WEB-INF/views/auth/forgot.jsp").forward(req, resp);
}


@Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
if (!Csrf.isTokenValid(req)) { resp.sendError(400, "CSRF invalid"); return; }
String email = req.getParameter("email");
String ip = req.getRemoteAddr();


// Always show the same message to avoid enumeration
String generic = "Nếu email tồn tại, chúng tôi đã gửi mã xác minh. Vui lòng kiểm tra hộp thư.";
req.setAttribute("message", generic);


if (email == null || email.isBlank()) {
doGet(req, resp); return;
}
email = email.trim().toLowerCase();


try {
User u = userDAO.findByEmail(email);
if (u == null) { doGet(req, resp); return; } // same generic UI


// simple rate limit: max 3 requests / 15 minutes
if (prDAO.countRecentRequests(email) >= 3) {
doGet(req, resp); return; // generic
}


String otp = Randoms.otp6();
Timestamp exp = Timestamp.from(Instant.now().plusSeconds(10 * 60));
prDAO.insert(u.getUserId(), email, otp, exp, ip);


// mask email for template subtitle
String masked = email.replaceAll("(^.).*(@.*$)", "$1***$2");
try { MailSender.sendOtp(email, masked, otp); } catch (Exception ignore) {}
} catch (Exception e) {
// log if you have a logger; keep UI generic
}


doGet(req, resp);
}
}