package com.acme.leavemgmt.servlet;

import com.acme.leavemgmt.dao.RequestDAO;
import com.acme.leavemgmt.model.Request;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/request/detail")
public class RequestDetailServlet extends HttpServlet {
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
            req.getRequestDispatcher("/WEB-INF/views/request/detail.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private Integer safeInt(String s) {
        try { return Integer.valueOf(s); } catch (Exception ignore) { return null; }
    }
}
