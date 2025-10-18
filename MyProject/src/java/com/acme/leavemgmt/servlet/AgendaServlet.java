package com.acme.leavemgmt.servlet;

import com.acme.leavemgmt.dao.RequestDAO;
import com.acme.leavemgmt.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@WebServlet(name="AgendaServlet", urlPatterns={"/request/agenda"})
public class AgendaServlet extends HttpServlet {
    private final RequestDAO dao = new RequestDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }
        String dept = (String) s.getAttribute("department");

        // Range: mặc định tuần hiện tại [Mon..Sun]
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        LocalDate sunday = monday.plusDays(6);

        LocalDate from = Optional.ofNullable(req.getParameter("from"))
                .map(LocalDate::parse).orElse(monday);
        LocalDate to = Optional.ofNullable(req.getParameter("to"))
                .map(LocalDate::parse).orElse(sunday);

        if (to.isBefore(from)) to = from;

        // Danh sách ngày [from..to]
        List<LocalDate> days = Stream.iterate(from, d -> !d.isAfter(to), d -> d.plusDays(1))
                .collect(Collectors.toList());

        try {
            List<User> users = dao.listUsersByDepartment(dept);
            // có thể sort để đẹp hơn
            users.sort(Comparator.comparing(User::getFullName, String.CASE_INSENSITIVE_ORDER));

            Map<Integer, Set<LocalDate>> absent = dao.getApprovedAbsences(dept, from, to);

            req.setAttribute("users", users);
            req.setAttribute("absent", absent);
            req.setAttribute("from", from);
            req.setAttribute("to", to);
            req.setAttribute("days", days);
            req.getRequestDispatcher("/WEB-INF/views/request/agenda.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
