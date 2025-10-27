package com.acme.leavemgmt.servlet.request;

import com.acme.leavemgmt.dao.RequestDAO;
import com.acme.leavemgmt.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet(urlPatterns = {"/request/approve"})
public class ApproveServlet extends HttpServlet {

    private final RequestDAO dao = new RequestDAO();

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1) Check login
        User me = (User) req.getSession().getAttribute("currentUser");
        if (me == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // 2) Lấy & validate id
        String idStr = req.getParameter("id");
        if (idStr == null || !idStr.matches("\\d+")) {
            resp.sendError(400, "Invalid request id"); // Bạn đang gặp thông báo này
            return;
        }
        int id = Integer.parseInt(idStr);

        // 3) Lấy action
        String action = req.getParameter("action");
        boolean approve = "approve".equalsIgnoreCase(action); // mặc định khác "approve" coi là reject
        String note = req.getParameter("note");

        try {
            // 4) Kiểm tra quyền: admin hoặc là quản lý trực tiếp/leader của người tạo đơn
            if (!dao.canProcessRequest(id, me)) {
                resp.sendError(403, "Forbidden");
                return;
            }

            // 5) Thực hiện cập nhật
            boolean ok = dao.processDecision(id, me.getId(), approve, note);
            if (!ok) {
                // ví dụ: đơn đã được duyệt trước đó hoặc không tồn tại
                req.getSession().setAttribute("flash",
                        "Không thể cập nhật. Đơn có thể đã được xử lý.");
            } else {
                req.getSession().setAttribute("flash",
                        approve ? "Đã APPROVE đơn #" + id : "Đã REJECT đơn #" + id);
            }

            // 6) Điều hướng về list
            resp.sendRedirect(req.getContextPath() + "/request/list");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    // Nếu bạn vô tình gọi GET (click link), vẫn handle cho tiện
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doPost(req, resp);
    }
}
