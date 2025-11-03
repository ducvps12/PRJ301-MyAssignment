package com.acme.leavemgmt.servlet;

import com.acme.leavemgmt.dao.RequestDAO;
import com.acme.leavemgmt.model.Request;
import com.acme.leavemgmt.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Set;

@WebServlet("/request/cancel")
public class RequestCancelServlet extends HttpServlet {
    private final RequestDAO dao = new RequestDAO();
    private static final Set<String> CANCELABLE = Set.of("PENDING"); // thêm "INPROGRESS" nếu muốn

    /** Lấy user hiện tại từ session (currentUser|user) */
    private User current(HttpServletRequest req) {
        User u = (User) req.getSession().getAttribute("currentUser");
        if (u == null) u = (User) req.getSession().getAttribute("user");
        return u;
    }

    /** Lấy id người dùng từ model (getId hoặc getUserId) */
    private Integer uid(User u) {
        if (u == null) return null;
        try { Integer v = u.getId(); if (v != null) return v; } catch (Throwable ignore) {}
        try { return u.getUserId(); } catch (Throwable ignore) {}
        return null;
    }

    /** parse int an toàn */
    private Integer safeInt(String s) {
        try { return Integer.valueOf(s); } catch (Exception ignore) { return null; }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Integer id = safeInt(req.getParameter("id"));
        if (id == null) { resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing id"); return; }

        try {
            Request r = dao.findById(id);
            if (r == null) { resp.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
            req.setAttribute("r", r);
            req.getRequestDispatcher("/WEB-INF/views/request/cancel.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Integer id = safeInt(req.getParameter("id"));
        if (id == null) { resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing id"); return; }

        User me = current(req);
        Integer myId = uid(me);
        if (myId == null) { resp.sendRedirect(req.getContextPath() + "/login"); return; }

        String note = req.getParameter("note"); // optional

        try {
            Request r = dao.findById(id);
            if (r == null) { resp.sendError(HttpServletResponse.SC_NOT_FOUND); return; }

            // Chủ đơn: khớp user_id hoặc created_by (tương thích dữ liệu cũ/mới)
            boolean isOwner = Objects.equals(myId, r.getUserId()) || Objects.equals(myId, r.getCreatedBy());
            boolean cancelableStatus = r.getStatus() != null && CANCELABLE.contains(r.getStatus().toUpperCase());

            if (!isOwner || !cancelableStatus) {
                req.setAttribute("r", r);
                req.setAttribute("error", "Bạn chỉ có thể hủy đơn của mình khi trạng thái còn PENDING.");
                req.getRequestDispatcher("/WEB-INF/views/request/cancel.jsp").forward(req, resp);
                return;
            }

            boolean ok = dao.cancelRequest(id, myId, me.getFullName(), note);
            if (!ok) {
                req.setAttribute("r", r);
                req.setAttribute("error", "Hủy không thành công. Vui lòng thử lại.");
                req.getRequestDispatcher("/WEB-INF/views/request/cancel.jsp").forward(req, resp);
                return;
            }

            resp.sendRedirect(req.getContextPath() + "/request/detail?id=" + id + "&msg=cancelled");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
