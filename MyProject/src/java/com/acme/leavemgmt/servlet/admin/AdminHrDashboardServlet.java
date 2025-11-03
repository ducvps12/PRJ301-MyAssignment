package com.acme.leavemgmt.servlet.admin;

import com.acme.leavemgmt.dao.HrDAO;
import com.acme.leavemgmt.util.Csrf;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/admin/hr")
public class AdminHrDashboardServlet extends HttpServlet {
    private final HrDAO dao = new HrDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("totalEmployees", dao.countEmployees());
            req.setAttribute("onLeaveToday", dao.countOnLeaveToday());
            req.setAttribute("interns", dao.countInterns());
            req.setAttribute("contractEndingSoon", dao.countContractEndingInDays(30));
            req.setAttribute("todayLeaves", dao.listOnLeaveToday(10));
            Csrf.addToken(req);
            req.getRequestDispatcher("/WEB-INF/views/admin/hr_dashboard.jsp").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(500, e.getMessage());
        }
    }
}
