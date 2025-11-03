package com.acme.leavemgmt.servlet.admin;

import com.acme.leavemgmt.dao.HrDAO;
import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.Csrf;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet("/admin/hr/list")
public class AdminHrListServlet extends HttpServlet {
    private final HrDAO dao = new HrDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String q = req.getParameter("q");
            String role = req.getParameter("role");
            Integer div = parseInt(req.getParameter("division"));
            int page = Math.max(1, parseInt(req.getParameter("page")) == null ? 1 : parseInt(req.getParameter("page")));
            int pageSize = 20;

            List<User> users = dao.searchUsers(q, role, div, page, pageSize);
            req.setAttribute("users", users);
            Csrf.addToken(req);
            req.getRequestDispatcher("/WEB-INF/views/admin/hr_list.jsp").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(500, e.getMessage());
        }
    }

    private Integer parseInt(String s) {
        try { return (s==null||s.isBlank())?null:Integer.parseInt(s); }
        catch (NumberFormatException e){ return null; }
    }
}
