package com.acme.leavemgmt.servlet;

import com.acme.leavemgmt.dao.RequestDAO;
import com.acme.leavemgmt.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@WebServlet(name = "AgendaServlet", urlPatterns = {"/request/agenda"})
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

        // Mặc định tuần hiện tại [Mon..Sun]
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        LocalDate sunday = monday.plusDays(6);

        // Parse tham số (an toàn với format sai)
        LocalDate from = parseOrDefault(req.getParameter("from"), monday);
        LocalDate to   = parseOrDefault(req.getParameter("to"),   sunday);

        // Quan trọng: dùng biến final/eff-final cho lambda
        final LocalDate start = from;
        final LocalDate end   = (to.isBefore(from)) ? from : to;

        // Danh sách ngày [start..end]
        List<LocalDate> days = Stream.iterate(start, d -> !d.isAfter(end), d -> d.plusDays(1))
                                     .collect(Collectors.toList());

        try {
            List<User> users = dao.listUsersByDepartment(dept);
            users.sort(Comparator.comparing(User::getFullName, String.CASE_INSENSITIVE_ORDER));

            Map<Integer, Set<LocalDate>> absent = dao.getApprovedAbsences(dept, start, end);

            req.setAttribute("users", users);
            req.setAttribute("absent", absent);
            req.setAttribute("from", start);
            req.setAttribute("to", end);
            req.setAttribute("days", days);

            req.getRequestDispatcher("/WEB-INF/views/request/agenda.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private static LocalDate parseOrDefault(String val, LocalDate def) {
        if (val == null || val.isBlank()) return def;
        try {
            return LocalDate.parse(val); // ISO-8601: yyyy-MM-dd
        } catch (DateTimeParseException ex) {
            return def;
        }
    }
}
