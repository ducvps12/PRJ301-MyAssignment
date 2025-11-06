package com.acme.leavemgmt.servlet.support;

import com.acme.leavemgmt.dao.SupportTicketDAO;
import com.acme.leavemgmt.model.SupportTicket;
import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.Csrf;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

@WebServlet("/support/feedback")
public class FeedbackServlet extends HttpServlet {
  private final SupportTicketDAO dao = new SupportTicketDAO();

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("application/json; charset=UTF-8");
    req.setCharacterEncoding(StandardCharsets.UTF_8.name());
    try (PrintWriter out = resp.getWriter()) {
      if (!Csrf.validate(req)) { resp.setStatus(400); out.print("{\"ok\":false,\"msg\":\"CSRF invalid\"}"); return; }

      String title = trim(req.getParameter("title"));
      String body  = trim(req.getParameter("body"));
      String tech  = trim(req.getParameter("tech"));
      String email = trim(req.getParameter("email"));
      String name  = trim(req.getParameter("name"));

      if (title==null || title.length()<4 || body==null || body.length()<10) {
        resp.setStatus(422); out.print("{\"ok\":false,\"msg\":\"Thiếu nội dung hợp lệ\"}"); return;
      }
      User me = (User) req.getSession().getAttribute("currentUser");
      SupportTicket t = new SupportTicket();
      if (me != null) { t.setUserId(me.getId()); if (name==null) name = me.getFullname(); if (email==null) email = me.getEmail(); }
      t.setUserName(name); t.setEmail(email); t.setTitle(title); t.setBody(body); t.setTechJson(tech);

      int id = dao.insert(t);

      out.printf("{\"ok\":true,\"id\":%d}", id);
    } catch (Exception e) {
      resp.setStatus(500);
      resp.getWriter().printf("{\"ok\":false,\"msg\":\"%s\"}", escape(e.getMessage()));
    }
  }
  private static String trim(String s){ return (s==null||s.isBlank())?null:s.trim(); }
  private static String escape(String s){ return s==null?"":s.replace("\\","\\\\").replace("\"","\\\""); }
}
