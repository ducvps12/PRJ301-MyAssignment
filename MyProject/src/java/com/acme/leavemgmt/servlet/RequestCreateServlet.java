package com.acme.leavemgmt.servlet;

import com.acme.leavemgmt.dao.RequestDAO;
import com.acme.leavemgmt.model.Request;
import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.Csrf;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@WebServlet(name = "RequestCreateServlet", urlPatterns = {"/request/create"})
public class RequestCreateServlet extends HttpServlet {

    private final RequestDAO dao = new RequestDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Tạo token CSRF cho form
        Csrf.protect(req);

        // Hiện form
        req.getRequestDispatcher("/WEB-INF/views/request/create.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Bảo đảm đọc tiếng Việt đúng
        req.setCharacterEncoding("UTF-8");

        // 1) Check login
        HttpSession session = req.getSession(false);
        User me = (session != null) ? (User) session.getAttribute("currentUser") : null;
        if (me == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // 2) CSRF
        if (!Csrf.isTokenValid(req)) {
            resp.sendError(400, "CSRF token invalid");
            return;
        }

        // 3) Lấy dữ liệu từ form
        String type    = optUpper(req.getParameter("type"), "ANNUAL");
        String title   = trimToNull(req.getParameter("title"));
        String reason  = trimToNull(req.getParameter("reason"));
        String sStart  = trimToNull(req.getParameter("start_date"));
        String sEnd    = trimToNull(req.getParameter("end_date"));

        LocalDate start = parseDate(sStart);
        LocalDate end   = parseDate(sEnd);

        // 4) Validate
        List<String> errors = new ArrayList<>();
        Set<String> allowed = new HashSet<>(Arrays.asList("ANNUAL","SICK","WFH","UNPAID"));
        if (!allowed.contains(type)) errors.add("Loại nghỉ không hợp lệ.");
        if (reason == null || reason.length() < 20) errors.add("Lý do tối thiểu 20 ký tự.");
        if (start == null || end == null) errors.add("Vui lòng chọn đủ Từ ngày và Đến ngày.");
        if (start != null && end != null && end.isBefore(start)) errors.add("Khoảng ngày không hợp lệ (Đến ngày < Từ ngày).");

        if (!errors.isEmpty()) {
            // Trả lại form với thông báo + giữ lại dữ liệu đã nhập
            req.setAttribute("error", errors.get(0));

            // Các param để JSP hiển thị lại (JSP của bạn đang dùng param.*, nên forward là được)
            req.setAttribute("javax.servlet.forward.request_uri", req.getRequestURI()); // giữ nguyên context
            Csrf.protect(req); // đẩy lại token vào request

            req.getRequestDispatcher("/WEB-INF/views/request/create.jsp").forward(req, resp);
            return;
        }

        // 5) Tạo model
        Request r = new Request();
        r.setUserId(me.getId());
        r.setCreatedBy(me.getId());
        r.setType(type);
        r.setTitle(title);      // <-- ĐÃ SET TITLE
        r.setReason(reason);
        r.setStartDate(start);
        r.setEndDate(end);
        // Có thể đặt trước status, hoặc để DAO mặc định PENDING:
        try {
            long id = dao.createRequest(r); // DAO phải INSERT cột title bằng setNString(...)
            // Về chi tiết đơn vừa tạo (hoặc list)
            resp.sendRedirect(req.getContextPath() + "/request/detail?id=" + id + "&msg=created");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    // ===== Helpers =====
    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String optUpper(String s, String def) {
        String v = trimToNull(s);
        return v == null ? def : v.toUpperCase();
    }

    private static LocalDate parseDate(String s) {
        if (s == null) return null;
        try {
            return LocalDate.parse(s);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }
}
