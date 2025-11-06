package com.acme.leavemgmt.servlet.admin;

import com.acme.leavemgmt.dao.SupportTicketDAO;
import com.acme.leavemgmt.dao.SupportTicketDAO.Page;
import com.acme.leavemgmt.model.SupportTicket;
import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.Csrf;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.*;

import java.io.IOException;

@WebServlet("/admin/support")
public class AdminSupportServlet extends HttpServlet {
  private final SupportTicketDAO dao = new SupportTicketDAO();

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    try {
      String view = req.getParameter("view");
      if ("detail".equals(view)) {
        int id = Integer.parseInt(req.getParameter("id"));
        req.setAttribute("ticket", dao.find(id));
        Csrf.addToken(req);
        req.getRequestDispatcher("/WEB-INF/views/admin/support_detail.jsp").forward(req, resp);
        return;
      }
      String status = trim(req.getParameter("status"));
      String q      = trim(req.getParameter("q"));
      int page = Math.max(1, parseInt(req.getParameter("page"), 1));
      int size = Math.min(100, Math.max(10, parseInt(req.getParameter("size"), 20)));

      Page<SupportTicket> result = dao.search(status, q, page, size);
      req.setAttribute("result", result);
      req.setAttribute("status", status);
      req.setAttribute("q", q);
      Csrf.addToken(req);
      req.getRequestDispatcher("/WEB-INF/views/admin/support_list.jsp").forward(req, resp);
    } catch (Exception e) {
      e.printStackTrace();
      resp.sendError(500, e.getMessage());
    }
  }

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      if (!Csrf.validate(req)) { resp.sendError(400, "CSRF invalid"); return; }
      String action = req.getParameter("action");
      if ("update".equals(action)) {
        int id = Integer.parseInt(req.getParameter("id"));
        String status = req.getParameter("status");
        String note = req.getParameter("note");
        User admin = (User) req.getSession().getAttribute("currentUser");
        Integer adminId = admin==null?null:admin.getId();
        dao.updateStatus(id, status, adminId, note);
        resp.sendRedirect(req.getContextPath()+"/admin/support?view=detail&id="+id+"&msg=updated");
        return;
      }
      resp.sendError(400, "Unknown action");
    } catch (Exception e) {
      e.printStackTrace();
      resp.sendError(500, e.getMessage());
    }
  }

  private static String trim(String s){ return s==null?null:s.trim(); }
  private static int parseInt(String s, int def){ try { return Integer.parseInt(s); } catch(Exception e){ return def; } }
}
