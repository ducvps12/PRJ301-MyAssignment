package com.acme.leavemgmt.servlet;

import com.acme.leavemgmt.dao.RequestDAO;
import com.acme.leavemgmt.util.Csrf;
import com.acme.leavemgmt.util.Servlets;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class RequestBulkServlet extends HttpServlet {
    private final RequestDAO dao = new RequestDAO();

    // Chỉ cho phép 3 action hợp lệ
    private static final Set<String> ALLOWED = Set.of("APPROVE", "REJECT", "CANCEL");

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!Servlets.isManager(req)) { resp.sendError(403); return; }

        // NOTE: nếu dự án bạn dùng Csrf.isTokenValid(req) thì đổi lại cho khớp
        if (!Csrf.isTokenValid(req)) { resp.sendError(400, "Invalid CSRF"); return; }

        String actionRaw = Servlets.trim(req.getParameter("action"));      // approve|reject|cancel (từ UI)
        String noteRaw   = Servlets.trim(req.getParameter("note"));
        String[] idsArr  = req.getParameterValues("ids");

        // Chuẩn hoá action thành UPPER
        String action = (actionRaw == null ? "" : actionRaw.trim().toUpperCase());
        switch (action) {
            case "APPROVE": action = "APPROVE"; break;
            case "REJECT":  action = "REJECT";  break;
            case "CANCEL":  action = "CANCEL";  break;
            default:        action = "";        break;
        }

        if (idsArr == null || idsArr.length == 0 || !ALLOWED.contains(action)) {
            flash(req, "Chưa chọn bản ghi hoặc hành động không hợp lệ.");
            redirectBack(req, resp);
            return;
        }

        // Parse id an toàn, bỏ qua id rác
        List<Integer> ids = Arrays.stream(idsArr)
                .map(String::trim)
                .filter(s -> s.matches("\\d+"))
                .map(Integer::valueOf)
                .distinct()
                .collect(Collectors.toList());

        if (ids.isEmpty()) {
            flash(req, "Danh sách ID không hợp lệ.");
            redirectBack(req, resp);
            return;
        }

        // Giới hạn độ dài note để tránh quá dài / lưu log
        String note = (noteRaw == null ? "" : noteRaw);
        if (note.length() > 1000) note = note.substring(0, 1000);

        int actorId = Servlets.currentUserId(req);

        try {
            // tuỳ dự án: nếu leader chỉ được duyệt trong phòng/ban mình,
            // hãy truyền thêm scope (dept/div) vào DAO
            int affected = dao.bulkUpdate(ids, action, actorId, note);
            flash(req, "Đã xử lý " + affected + " bản ghi.");
            redirectBack(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void flash(HttpServletRequest req, String msg) {
        req.getSession().setAttribute("flash", msg);
    }

    // ưu tiên quay lại trang trước, fallback về /request/list
    private void redirectBack(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String referer = req.getHeader("Referer");
        String to = (referer != null && referer.contains("/request")) ?
                referer : (req.getContextPath() + "/request/list");
        resp.sendRedirect(to);
    }
}
