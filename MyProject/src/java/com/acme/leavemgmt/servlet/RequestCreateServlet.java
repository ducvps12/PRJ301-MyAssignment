package com.acme.leavemgmt.servlet;

import com.acme.leavemgmt.dao.RequestDAO;
import com.acme.leavemgmt.model.Request;
import com.acme.leavemgmt.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

@WebServlet(name = "RequestCreateServlet", urlPatterns = {"/request/create"})
public class RequestCreateServlet extends HttpServlet {

    private final RequestDAO dao = new RequestDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // show form tạo đơn
        req.getRequestDispatcher("/WEB-INF/views/request/create.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1) Check login
        HttpSession session = req.getSession(false);
        User me = (session != null) ? (User) session.getAttribute("currentUser") : null;
        if (me == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // 2) Lấy dữ liệu từ form
        // Lưu ý: bảng của bạn không có cột 'title', mà có 'type', 'reason', 'start_date', 'end_date'
        String type   = req.getParameter("type");          // ví dụ: ANNUAL, SICK, WFH...
        String reason = req.getParameter("reason");
        String sStart = req.getParameter("start_date");
        String sEnd   = req.getParameter("end_date");

        LocalDate start = (sStart != null && !sStart.isBlank())
                ? LocalDate.parse(sStart)
                : null;
        LocalDate end = (sEnd != null && !sEnd.isBlank())
                ? LocalDate.parse(sEnd)
                : null;

        // 3) Tạo model
        Request r = new Request();
        r.setUserId(me.getId());          // người gửi đơn
        r.setCreatedBy(me.getId());       // người tạo đơn (thường = user gửi)
        r.setType(type);
        r.setReason(reason);
        r.setStartDate(start);
        r.setEndDate(end);
        // nên để status mặc định trong DAO là 'PENDING'

        // 4) Lưu DB
        try {
            dao.createRequest(r);
            resp.sendRedirect(req.getContextPath() + "/request/list?msg=created");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
