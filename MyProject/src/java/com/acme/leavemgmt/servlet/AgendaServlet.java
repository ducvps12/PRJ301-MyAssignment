package com.acme.leavemgmt.servlet;

import com.acme.leavemgmt.dao.RequestDAO;
import com.acme.leavemgmt.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

@WebServlet(name="AgendaServlet", urlPatterns={"/agenda"})
public class AgendaServlet extends HttpServlet {
    private final RequestDAO dao = new RequestDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession s = req.getSession(false);
        String dept = (String) s.getAttribute("department");

        LocalDate from = LocalDate.parse(
                Optional.ofNullable(req.getParameter("from")).orElse(LocalDate.now().toString()));
        LocalDate to = LocalDate.parse(
                Optional.ofNullable(req.getParameter("to")).orElse(LocalDate.now().plusDays(6).toString()));

        try {
            List<User> users = dao.listUsersByDepartment(dept);
            Map<Integer, Set<LocalDate>> absent = dao.getApprovedAbsences(dept, from, to);

            req.setAttribute("users", users);
            req.setAttribute("absent", absent);
            req.setAttribute("from", from);
            req.setAttribute("to", to);
            req.getRequestDispatcher("/WEB-INF/views/agenda.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
