package com.acme.leavemgmt.servlet.admin;

import com.acme.leavemgmt.dao.DivisionDAO;
import com.acme.leavemgmt.dao.DivisionDAO.Division;
import com.acme.leavemgmt.util.Csrf;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/admin/divisions")
public class AdminDivisionsServlet extends HttpServlet {
    private final DivisionDAO dao = new DivisionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String q = trim(req.getParameter("q"));
        int page = parseInt(req.getParameter("page"), 1);
        int size = parseInt(req.getParameter("size"), 10);

        try {
            int total = dao.count(q);
            List<Division> items = dao.list(q, page, size);

            req.setAttribute("q", q);
            req.setAttribute("page", page);
            req.setAttribute("size", size);
            req.setAttribute("total", total);
            req.setAttribute("items", items);
            Csrf.addToken(req);

            req.getRequestDispatcher("/WEB-INF/views/admin/divisions.jsp").forward(req, resp);
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendError(500, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            if (!Csrf.verify(req)) {
                resp.sendError(403, "Invalid CSRF token");
                return;
            }
            String action = trim(req.getParameter("action"));
            if ("create".equals(action)) {
                Division d = new Division();
                d.setCode(trim(req.getParameter("code")));
                d.setName(trim(req.getParameter("name")));
                d.setStatus(trim(req.getParameter("status")));
                dao.create(d);
                resp.sendRedirect(req.getContextPath()+"/admin/divisions?msg=created");
            } else if ("update".equals(action)) {
                Division d = new Division();
                d.setId(Integer.parseInt(req.getParameter("id")));
                d.setCode(trim(req.getParameter("code")));
                d.setName(trim(req.getParameter("name")));
                d.setStatus(trim(req.getParameter("status")));
                dao.update(d);
                resp.sendRedirect(req.getContextPath()+"/admin/divisions?msg=updated");
            } else if ("delete".equals(action)) {
                int id = Integer.parseInt(req.getParameter("id"));
                boolean ok = dao.delete(id);
                if (ok) resp.sendRedirect(req.getContextPath()+"/admin/divisions?msg=deleted");
                else    resp.sendRedirect(req.getContextPath()+"/admin/divisions?err=cannot_delete_has_users");
            } else {
                resp.sendError(400, "Unknown action");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendError(500, e.getMessage());
        }
    }

    private static String trim(String s){ return s==null?null:s.trim(); }
    private static int parseInt(String s, int def){
        try { return Integer.parseInt(s); } catch (Exception e){ return def; }
    }
}
