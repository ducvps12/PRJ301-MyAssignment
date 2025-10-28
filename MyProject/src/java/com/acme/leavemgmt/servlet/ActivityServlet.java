package com.acme.leavemgmt.servlet;

import com.acme.leavemgmt.dao.ActivityDAO;
import com.acme.leavemgmt.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;

/**
 * /activity  -> xem lịch sử của chính mình
 * /admin/activity?userId=...  -> admin xem tất cả / lọc theo user
 */
@WebServlet(name="ActivityServlet", urlPatterns = {"/activity", "/admin/activity"})
public class ActivityServlet extends HttpServlet {
  private final ActivityDAO dao = new ActivityDAO();

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    User me = (User) req.getSession().getAttribute("user");
    String path = req.getServletPath();

    // /activity: bắt buộc đăng nhập
    if ("/activity".equals(path)) {
      if (me == null) { resp.sendRedirect(req.getContextPath()+"/login"); return; }
      handleUser(me.getId(), req, resp);
      return;
    }

    // /admin/activity: bắt buộc ADMIN
    if (me == null || !"ADMIN".equals(me.getRole())) { resp.sendError(403); return; }

    Integer userId = null;
    String uid = req.getParameter("userId");
    if (uid != null && !uid.isBlank()) try { userId = Integer.valueOf(uid); } catch (Exception ignore){}

    handleAdmin(userId, req, resp);
  }

  private void handleUser(Integer userId, HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    int page = parseInt(req.getParameter("page"), 1);
    int size = parseInt(req.getParameter("size"), 20);
    try {
      var pg = dao.pageByUser(userId, page, size);
      req.setAttribute("pg", pg);
      req.setAttribute("scope", "me");
      req.getRequestDispatcher("/WEB-INF/views/activity.jsp").forward(req, resp);
    } catch (SQLException e) { throw new ServletException(e); }
  }

  private void handleAdmin(Integer userId, HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    int page = parseInt(req.getParameter("page"), 1);
    int size = parseInt(req.getParameter("size"), 20);
    try {
      var pg = dao.pageByUser(userId, page, size);
      req.setAttribute("pg", pg);
      req.setAttribute("scope", "admin");
      req.setAttribute("userFilter", userId);
      req.getRequestDispatcher("/WEB-INF/views/activity.jsp").forward(req, resp);
    } catch (SQLException e) { throw new ServletException(e); }
  }

  private int parseInt(String s, int def){ try { return Integer.parseInt(s); } catch(Exception e){ return def; } }
}
