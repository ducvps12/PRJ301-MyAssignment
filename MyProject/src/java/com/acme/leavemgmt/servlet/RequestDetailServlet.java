package com.acme.leavemgmt.servlet;

import com.acme.leavemgmt.dao.RequestDAO;
import com.acme.leavemgmt.model.Request;
import com.acme.leavemgmt.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@WebServlet(urlPatterns = {"/request/detail", "/request/detail/*"})
public class RequestDetailServlet extends HttpServlet {

    private final RequestDAO dao = new RequestDAO();

    /** Đọc id từ query (?id=) hoặc path (/request/detail/{id}) */
    private Integer readId(HttpServletRequest req) {
        String id = req.getParameter("id");
        if (id == null) {
            String pi = req.getPathInfo();   // ví dụ "/21"
            if (pi != null && pi.length() > 1) id = pi.substring(1);
        }
        try {
            return (id != null && id.matches("\\d+")) ? Integer.valueOf(id) : null;
        } catch (Exception ignore) {
            return null;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // === Chuẩn hóa charset + content-type ===
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html; charset=UTF-8");

        // === Lấy current user (nếu có) ===
        HttpSession ses = req.getSession(false);
        User me = (ses != null) ? (User) ses.getAttribute("currentUser") : null;
        req.setAttribute("me", me); // để JSP dùng nếu cần

        // === Đọc id ===
        Integer id = readId(req);
        if (id == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid id");
            return;
        }

        try {
            // === Nạp request chính ===
            Request r = dao.findById(id);
            if (r == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Request not found");
                return;
            }

            // === Kiểm soát quyền xem (null-safe) ===
            boolean isOwner = me != null && Objects.equals(r.getUserId(), me.getId());
            boolean isAdmin = me != null && (
                    "ADMIN".equalsIgnoreCase(me.getRole())
                 || "HR".equalsIgnoreCase(me.getRole())
                 || "DIV_LEADER".equalsIgnoreCase(me.getRole())
                 || "TEAM_LEAD".equalsIgnoreCase(me.getRole())
            );

            // nếu user_id bị null (dữ liệu cũ) thì vẫn cho admin xem
            boolean canView = isOwner || isAdmin || (r.getUserId() == null && isAdmin);
            if (!canView) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not have permission to view this request");
                return;
            }

            // === (An toàn) Gán history/attachments rỗng để JSP không null
            List<?> history;
            try {
                // Nếu bạn đã có DAO lấy history thì thay dòng dưới:
                // history = dao.listHistory(id);
                history = Collections.emptyList();
            } catch (Exception ignore) {
                history = Collections.emptyList();
            }
            List<Map<String, Object>> attachments = Collections.emptyList();

            // === Gán biến cho JSP ===
            req.setAttribute("r", r);
            req.setAttribute("history", history);
            req.setAttribute("attachments", attachments);

            // Các cờ UI
            boolean canCancel = isOwner && "PENDING".equalsIgnoreCase(r.getStatus());
            boolean canApprove = isAdmin && "PENDING".equalsIgnoreCase(r.getStatus());
            req.setAttribute("canCancel", canCancel);
            req.setAttribute("canApprove", canApprove);

            // === Forward ===
            req.getRequestDispatcher("/WEB-INF/views/request/detail.jsp").forward(req, resp);

        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
