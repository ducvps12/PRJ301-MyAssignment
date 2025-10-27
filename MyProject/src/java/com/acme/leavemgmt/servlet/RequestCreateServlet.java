package com.acme.leavemgmt.servlet;

import com.acme.leavemgmt.dao.RequestDAO;
import com.acme.leavemgmt.model.Request;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

@WebServlet(name="RequestCreateServlet", urlPatterns={"/request/create"})
public class RequestCreateServlet extends HttpServlet {
    private final RequestDAO dao = new RequestDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/request/create.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession s = req.getSession(false);
        int userId = (Integer) s.getAttribute("userId");

        String title = req.getParameter("title");
        String reason = req.getParameter("reason");
        LocalDate start = LocalDate.parse(req.getParameter("start_date")); // yyyy-MM-dd
        LocalDate end = LocalDate.parse(req.getParameter("end_date"));

        Request r = new Request();
        r.setTitle(title);
        r.setReason(reason);
        r.setStartDate(start);
        r.setEndDate(end);
        r.setCreatedBy(userId);

        try {
            dao.createRequest(r);
            resp.sendRedirect(req.getContextPath() + "/request/list?msg=created");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}