package com.acme.leavemgmt.servlet;

import com.acme.leavemgmt.dao.RequestDAO;
import com.acme.leavemgmt.model.Request;
import com.acme.leavemgmt.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet(name="ApprovalsServlet", urlPatterns={"/approvals","/approvals/"})
public class ApprovalsServlet extends HttpServlet {
  private final RequestDAO requestDAO = new RequestDAO();

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    HttpSession s = req.getSession(false);
    User me = (s!=null) ? (User)s.getAttribute("currentUser") : null;
    if (me==null){ resp.sendRedirect(req.getContextPath()+"/login?next=/approvals"); return; }
    if (!(me.isAdmin() || me.isLeader())) { resp.sendError(403); return; }

    // Lấy các đơn PENDING mà me được quyền duyệt
    List<Request> items = requestDAO.findPendingForApprover(me);
    req.setAttribute("items", items);
    req.getRequestDispatcher("/WEB-INF/views/request/approvals.jsp").forward(req, resp);
  }
}
