package com.acme.leavemgmt.servlet;

import com.acme.leavemgmt.dao.RequestDAO;
import com.acme.leavemgmt.model.Request;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name="RequestListServlet", urlPatterns={"/request/list"})
public class RequestListServlet extends HttpServlet {
    private final RequestDAO dao = new RequestDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession s = req.getSession(false);
        int userId = (Integer) s.getAttribute("userId");
        String role = (String) s.getAttribute("role");
        String dept = (String) s.getAttribute("department");

        try {
            List<Request> mine = dao.listMyRequests(userId);
            req.setAttribute("mine", mine);

            if (!"EMPLOYEE".equalsIgnoreCase(role)) {
                req.setAttribute("subs", dao.listSubordinateRequests(dept));
            }
            req.getRequestDispatcher("/WEB-INF/views/request_list.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
