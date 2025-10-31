package com.acme.leavemgmt.servlet;

import com.acme.leavemgmt.dao.RequestDAO;
import com.acme.leavemgmt.dao.RequestListDAO;
import com.acme.leavemgmt.model.Request;
import com.acme.leavemgmt.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@WebServlet(name = "RequestListServlet", urlPatterns = {"/request/list"})
public class RequestListServlet extends HttpServlet {

    // DAO nghiệp vụ (giữ lại để sau này dùng doPost, xem chi tiết, approve, ...)
    private final RequestDAO requestDAO = new RequestDAO();
    // DAO chuyên liệt kê + filter
    private final RequestListDAO requestListDAO = new RequestListDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1. Auth
        HttpSession ses = req.getSession(false);
        if (ses == null || ses.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        User me = (User) ses.getAttribute("currentUser");
        int meId = me.getId();
        String myDept = me.getDepartment();
        String myRole = me.getRole() == null ? "" : me.getRole().toUpperCase();

        // 2. Lấy param lọc
        String fromStr = req.getParameter("from");   // yyyy-MM-dd
        String toStr   = req.getParameter("to");
        String status  = req.getParameter("status");
        String mine    = req.getParameter("mine");
        String q       = req.getParameter("q");
        String sort    = req.getParameter("sort");
        String sizeStr = req.getParameter("size");
        String pageStr = req.getParameter("page");

        // 3. Parse
        LocalDate fromDate = null, toDate = null;
        try {
            if (fromStr != null && !fromStr.isBlank())
                fromDate = LocalDate.parse(fromStr);
            if (toStr != null && !toStr.isBlank())
                toDate = LocalDate.parse(toStr);
        } catch (DateTimeParseException ignored) { }

        int page = 1;
        try { page = Integer.parseInt(pageStr); } catch (Exception ignored) { }
        if (page < 1) page = 1;

        int size = 20;
        try { size = Integer.parseInt(sizeStr); } catch (Exception ignored) { }
        if (size <= 0) size = 20;

        try {
            // 4. Đếm bằng DAO list
            int totalItems = requestListDAO.countByFilter(
                    meId,
                    myDept,
                    myRole,
                    fromDate,
                    toDate,
                    status,
                    mine,
                    q
            );

            int totalPages = (int) Math.ceil((double) totalItems / size);
            if (totalPages == 0) totalPages = 1;
            if (page > totalPages) page = totalPages;

            int offset = (page - 1) * size;

            // 5. Lấy danh sách bằng DAO list
            List<Request> list = requestListDAO.findByFilter(
                    meId,
                    myDept,
                    myRole,
                    fromDate,
                    toDate,
                    status,
                    mine,
                    q,
                    sort,
                    size,
                    offset
            );

            // set attribute cho JSP
            req.setAttribute("requests", list);
            req.setAttribute("totalItems", totalItems);
            req.setAttribute("totalPages", totalPages);
            req.setAttribute("page", page);
            req.setAttribute("size", size);

            // giữ filter
            req.setAttribute("from", fromStr);
            req.setAttribute("to", toStr);
            req.setAttribute("statusFilter", status);
            req.setAttribute("mineFilter", mine);
            req.setAttribute("q", q);
            req.setAttribute("sort", sort);

        } catch (Exception e) {
            throw new ServletException(e);
        }

        // 6. Forward
        req.getRequestDispatcher("/WEB-INF/views/request/list.jsp").forward(req, resp);
    }
}
