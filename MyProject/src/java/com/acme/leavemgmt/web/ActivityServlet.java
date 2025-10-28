// com.acme.leavemgmt.web.ActivityServlet
package com.acme.leavemgmt.web;

import com.acme.leavemgmt.dao.ActivityDAO;
import com.acme.leavemgmt.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(urlPatterns = {"/me/activity", "/admin/activity"})
public class ActivityServlet extends HttpServlet {
  private final ActivityDAO dao = new ActivityDAO();

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    User me = (User) req.getSession().getAttribute("user");
    boolean isAdmin = me != null && User.ROLE_ADMIN.equals(me.getRole());

    String path = req.getServletPath();
    Integer userFilter = null;

    if ("/me/activity".equals(path)) {
      userFilter = me == null ? null : me.getId();
    } else { // /admin/activity
      if (!isAdmin) { resp.sendError(403); return; }
      // admin có thể không truyền userId -> xem tất cả
      String uid = req.getParameter("userId");
      if (uid != null && !uid.isBlank()) userFilter = Integer.valueOf(uid);
    }

    int page = parseInt(req.getParameter("page"), 1);
    int size = parseInt(req.getParameter("size"), 20);

    try {
      ActivityDAO.Page<java.util.Map<String,Object>> pg =
          dao.pageByUser(userFilter, page, size);
      req.setAttribute("pg", pg);
      req.setAttribute("userFilter", userFilter);
      req.getRequestDispatcher("/WEB-INF/views/activity/list.jsp").forward(req, resp);
    } catch (SQLException e) {
      throw new ServletException(e);
    }
  }

  private int parseInt(String s, int def){
    try { return Integer.parseInt(s); } catch(Exception ignore){ return def; }
  }
}
