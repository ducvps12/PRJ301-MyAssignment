package com.acme.leavemgmt.servlet;

import com.acme.leavemgmt.dao.RequestDAO;
import com.acme.leavemgmt.model.Request;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(urlPatterns = {"/request/detail", "/request/detail/*"})
public class RequestDetailServlet extends HttpServlet {
    private final RequestDAO dao = new RequestDAO();

    /** Đọc id từ query (?id=) hoặc path (/request/detail/{id}) */
    private Integer readId(HttpServletRequest req) {
        String id = req.getParameter("id");
        if (id == null) {
            String pi = req.getPathInfo();        // ví dụ "/21"
            if (pi != null && pi.length() > 1) id = pi.substring(1);
        }
        try { return (id != null && id.matches("\\d+")) ? Integer.valueOf(id) : null; }
        catch (Exception ignore) { return null; }
    }

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        Integer id = readId(req);
        if (id == null) { resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid id"); return; }

        try {
            Request r = dao.findById(id);
            if (r == null) { resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Request not found"); return; }

            req.setAttribute("r", r);
            req.getRequestDispatcher("/WEB-INF/views/request/detail.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
