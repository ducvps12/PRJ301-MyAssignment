package com.acme.leavemgmt.servlet;

import com.acme.leavemgmt.dao.RequestDAO;
import com.acme.leavemgmt.model.Request;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name="RequestApproveServlet", urlPatterns={"/request/approve"})
public class RequestApproveServlet extends HttpServlet {
    private final RequestDAO dao = new RequestDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int id = Integer.parseInt(req.getParameter("id"));
        try {
            Request r = dao.findById(id);
            req.setAttribute("reqItem", r);
            req.getRequestDispatcher("/WEB-INF/views/request_approve.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession s = req.getSession(false);
        int managerId = (Integer) s.getAttribute("userId");
        String action = req.getParameter("action"); // approve | reject
        String note = req.getParameter("note");
        int id = Integer.parseInt(req.getParameter("id"));

        String status = "approve".equals(action) ? "APPROVED" : "REJECTED";

        try {
            dao.processRequest(id, managerId, status, note);
            resp.sendRedirect(req.getContextPath() + "/request/list?msg=processed");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
