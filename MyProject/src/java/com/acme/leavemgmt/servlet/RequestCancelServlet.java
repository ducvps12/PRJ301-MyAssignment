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

@WebServlet("/request/cancel")
public class RequestCancelServlet extends HttpServlet {
    private final RequestDAO dao = new RequestDAO();

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

        User me = (User) req.getSession().getAttribute("user");
        if (me == null) { resp.sendRedirect(req.getContextPath() + "/login"); return; }

        String note = req.getParameter("note"); // có thể null

        try {
            // Kiểm tra nhanh trước khi hủy để show err thân thiện
            Request r = dao.findById(id);
            if (r == null) { resp.sendError(HttpServletResponse.SC_NOT_FOUND); return; }

            if (!(r.getCreatedBy() == me.getUserId() && "INPROGRESS".equalsIgnoreCase(r.getStatus()))) {
                req.setAttribute("r", r);
                req.setAttribute("error", "Bạn chỉ có thể hủy đơn của mình khi trạng thái còn INPROGRESS.");
                req.getRequestDispatcher("/WEB-INF/views/request/cancel.jsp").forward(req, resp);
                return;
            }

            boolean ok = dao.cancelRequest(id, me.getUserId(), me.getFullName(), note);
            if (!ok) {
                req.setAttribute("r", r);
                req.setAttribute("error", "Hủy không thành công. Vui lòng thử lại.");
                req.getRequestDispatcher("/WEB-INF/views/request/cancel.jsp").forward(req, resp);
                return;
            }

            // Quay về detail
            resp.sendRedirect(req.getContextPath() + "/request/detail?id=" + id + "&msg=cancelled");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private Integer safeInt(String s) {
        try { return Integer.valueOf(s); } catch (Exception ignore) { return null; }
    }
}
