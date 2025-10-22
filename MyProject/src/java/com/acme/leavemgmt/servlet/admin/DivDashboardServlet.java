package com.acme.leavemgmt.servlet.admin;

import com.acme.leavemgmt.dao.StatsDAO;
import com.acme.leavemgmt.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet(name = "DivDashboardServlet", urlPatterns = {"/admin/div"})
public class DivDashboardServlet extends HttpServlet {

    private final StatsDAO statsDAO = new StatsDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        User u = (session != null) ? (User) session.getAttribute("currentUser") : null;

        // Chưa đăng nhập
        if (u == null) {
            resp.sendRedirect(req.getContextPath() + "/login?next=" + req.getRequestURI());
            return;
        }

        // Quyền: DIV_LEADER (và ADMIN nếu bạn cho phép)
        boolean isDivLead = "DIV_LEADER".equalsIgnoreCase(u.getRole());
        boolean isAdmin   = "ADMIN".equalsIgnoreCase(u.getRole());
        if (!isDivLead && !isAdmin) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Lấy thống kê theo division của user
        var stats = statsDAO.getDivisionStats(u.getDepartment()); // không ném SQLException
        req.setAttribute("stats", stats);

        // Chống cache
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);

        req.getRequestDispatcher("/WEB-INF/views/admin/div_dashboard.jsp").forward(req, resp);
    }
}
