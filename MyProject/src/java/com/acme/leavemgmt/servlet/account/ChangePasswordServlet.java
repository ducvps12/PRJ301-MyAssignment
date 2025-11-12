package com.acme.leavemgmt.servlet.account;

import com.acme.leavemgmt.dao.UserDAO;
import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.Csrf;
import com.acme.leavemgmt.util.WebUtil;
import com.acme.leavemgmt.util.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@WebServlet(urlPatterns = {"/account/change-password"})
public class ChangePasswordServlet extends HttpServlet {

    // chính sách độ mạnh cơ bản: >=8 ký tự, có chữ & số
    private static final Pattern BASIC_POLICY =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");

    // chống spam đổi liên tục: tối đa 1 lần / 10 giây
    private static final long COOLDOWN_MS = TimeUnit.SECONDS.toMillis(10);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User me = (User) req.getSession().getAttribute("currentUser");
        if (me == null) {
            resp.sendRedirect(req.getContextPath() + "/login?next=" + req.getRequestURI());
            return;
        }
        // đồng bộ với util Csrf (trước bạn dùng Csrf.protect)
        Csrf.protect(req);
        req.getRequestDispatcher("/WEB-INF/views/account/change_password.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession(false);
        User me = (session == null) ? null : (User) session.getAttribute("currentUser");
        if (me == null) {
            resp.sendRedirect(req.getContextPath() + "/login?next=" + req.getRequestURI());
            return;
        }

        // CSRF (đồng bộ với util Csrf đang dùng ở servlet khác)
        if (!Csrf.isTokenValid(req)) {
            resp.sendError(403, "Bad CSRF token");
            return;
        }

        // cooldown chống spam
        Long last = (Long) session.getAttribute("cp_last");
        long now = System.currentTimeMillis();
        if (last != null && (now - last) < COOLDOWN_MS) {
            reRender(req, resp, "Bạn thao tác quá nhanh, vui lòng thử lại sau vài giây.");
            return;
        }
        session.setAttribute("cp_last", now);

        String current = trim(req.getParameter("current"));
        String pass1   = trim(req.getParameter("pass1"));
        String pass2   = trim(req.getParameter("pass2"));

        // validate
        if (isBlank(current) || isBlank(pass1) || isBlank(pass2)) {
            reRender(req, resp, "Vui lòng nhập đầy đủ thông tin.");
            return;
        }
        if (!pass1.equals(pass2)) {
            reRender(req, resp, "Mật khẩu nhập lại không khớp.");
            return;
        }
        if (!BASIC_POLICY.matcher(pass1).matches()) {
            reRender(req, resp, "Mật khẩu mới tối thiểu 8 ký tự và phải có cả chữ và số.");
            return;
        }
        // không cho đặt trùng mật khẩu hiện tại
        if (pass1.equals(current)) {
            reRender(req, resp, "Mật khẩu mới không được trùng với mật khẩu hiện tại.");
            return;
        }
        // chặn vài mật khẩu quá yếu phổ biến
        String low = pass1.toLowerCase();
        if (low.equals("12345678") || low.equals("password") || low.equals("11111111")) {
            reRender(req, resp, "Mật khẩu quá yếu, vui lòng chọn mật khẩu khác.");
            return;
        }

        try (UserDAO userDAO = new UserDAO(DBConnection.getConnection())) {
            boolean ok = userDAO.updatePasswordIfMatches(me.getId(), current, pass1);

            // log activity nhẹ (nếu bạn có ActivityDAO thì gọi ở đây)
            String ip = WebUtil.clientIp(req);
            String ua = WebUtil.userAgent(req);
            // activityDAO.log(me.getId(), "CHANGE_PASSWORD", "USER", me.getId(), ok ? "OK" : "FAIL", ip, ua);

            if (!ok) {
                reRender(req, resp, "Mật khẩu hiện tại không đúng.");
                return;
            }

            // huỷ phiên hiện tại để bắt đăng nhập lại
            session.invalidate();

            // Hỗ trợ yêu cầu JSON/XHR
            if (wantsJson(req)) {
                resp.setContentType("application/json; charset=UTF-8");
                resp.getWriter().write("{\"ok\":true,\"message\":\"changed_password\"}");
                return;
            }

            resp.sendRedirect(req.getContextPath() + "/login?msg=changed_password");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void reRender(HttpServletRequest req, HttpServletResponse resp, String err)
            throws ServletException, IOException {
        req.setAttribute("err", err);
        // đồng bộ kiểu CSRF với các trang khác
        Csrf.protect(req);
        req.getRequestDispatcher("/WEB-INF/views/account/change_password.jsp").forward(req, resp);
    }

    private static boolean wantsJson(HttpServletRequest r) {
        String acc = r.getHeader("Accept");
        String xhr = r.getHeader("X-Requested-With");
        return "XMLHttpRequest".equalsIgnoreCase(xhr)
                || (acc != null && acc.toLowerCase().contains("application/json"));
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
    private static String trim(String s) { return s == null ? "" : s.trim(); }
}
