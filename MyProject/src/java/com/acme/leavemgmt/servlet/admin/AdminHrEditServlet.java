package com.acme.leavemgmt.servlet.admin;

import com.acme.leavemgmt.dao.HrDAO;
import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.Csrf;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

@WebServlet("/admin/hr/edit")
public class AdminHrEditServlet extends HttpServlet {
    private final HrDAO dao = new HrDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            int id = Integer.parseInt(req.getParameter("id"));
            User u = dao.findUser(id);
            if (u == null) { resp.sendError(404); return; }
            req.setAttribute("u", u);
            Csrf.addToken(req);
            req.getRequestDispatcher("/WEB-INF/views/admin/hr_edit.jsp").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace(); resp.sendError(500, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            if (!Csrf.isTokenValid(req)) { resp.sendError(403, "Bad CSRF"); return; }

            int id = Integer.parseInt(req.getParameter("id"));
            User u = dao.findUser(id);
            if (u == null) { resp.sendError(404); return; }

            u.setFullname(req.getParameter("fullname"));
            u.setEmail(req.getParameter("email"));
            u.setRole(req.getParameter("role"));
            u.setDivisionId(parseInt(req.getParameter("division_id")));
            u.setStatus(req.getParameter("status"));
            u.setJobTitle(req.getParameter("job_title"));
            u.setJoinDate(parseDate(req.getParameter("join_date")));
            u.setContractEnd(parseDate(req.getParameter("contract_end")));
            u.setSalary(parseDecimal(req.getParameter("salary")));

            dao.updateUser(u);
            resp.sendRedirect(req.getContextPath()+"/admin/hr/edit?id="+id+"&ok=1");
        } catch (Exception e) {
            e.printStackTrace(); resp.sendError(500, e.getMessage());
        }
    }

    private Integer parseInt(String s){ try{ return (s==null||s.isBlank())?null:Integer.parseInt(s);}catch(Exception e){return null;}}
    private LocalDate parseDate(String s){ try{ return (s==null||s.isBlank())?null:LocalDate.parse(s);}catch(Exception e){return null;}}
    private BigDecimal parseDecimal(String s){ try{ return (s==null||s.isBlank())?null:new BigDecimal(s);}catch(Exception e){return null;}}
}
