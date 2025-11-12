package com.acme.leavemgmt.servlet;

import com.acme.leavemgmt.dao.NotificationDAO;
import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.AppInit;            // CHỈ giữ 1 AppInit duy nhất
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import javax.sql.DataSource;
import java.io.IOException;

@WebServlet(urlPatterns = {"/notif/read", "/notif/read-all"})
public class NotificationServlet extends HttpServlet {
  private NotificationDAO dao;

  @Override public void init() {
    DataSource ds = AppInit.getDataSource();
    if (ds == null) {
      Object v = getServletContext().getAttribute("DS");
      if (v instanceof DataSource) ds = (DataSource) v;
    }
    if (ds == null) throw new IllegalStateException("DataSource not set");
    dao = new NotificationDAO(ds);
  }

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    HttpSession s = req.getSession(false);
    User me = (s == null) ? null : (User) s.getAttribute("currentUser");
    if (me == null) { resp.sendError(401); return; }

    String p = req.getServletPath();
    try {
      if ("/notif/read".equals(p)) {
        int id = Integer.parseInt(req.getParameter("id"));
        dao.setReadByUser(id, me.getUserId(), true);
        resp.setStatus(204);
        return;
      }
      if ("/notif/read-all".equals(p)) {
        dao.markAllRead(me.getUserId());
        // Cho phép gọi bằng form thường
        String ref = req.getHeader("Referer");
        resp.sendRedirect(ref != null ? ref : req.getContextPath() + "/portal");
        return;
      }
      resp.sendError(404);
    } catch (Exception e) {
      resp.sendError(500, e.getMessage());
    }
  }
}
