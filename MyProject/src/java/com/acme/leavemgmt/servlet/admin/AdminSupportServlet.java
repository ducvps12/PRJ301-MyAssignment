package com.acme.leavemgmt.servlet.admin;

import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.Csrf;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import javax.sql.DataSource;

@WebServlet(urlPatterns = {"/admin/support","/admin/support/detail"})
public class AdminSupportServlet extends HttpServlet {

  /* ---------------- Ticket BEAN (có getter/setter để JSP EL đọc được) ---------------- */
  public static class Ticket {
    private long id;
    private String email;
    private String subject;
    private String body;
    private String status;     // OPEN | IN_PROGRESS | CLOSED
    private String assignee;
    private LocalDateTime createdAt;
    private final List<String> replies = new ArrayList<>();

    public Ticket() {}
    public Ticket(long id, String email, String subject, String body) {
      this.id = id;
      this.email = email;
      this.subject = subject;
      this.body = body;
      this.status = "OPEN";
      this.createdAt = LocalDateTime.now();
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAssignee() { return assignee; }
    public void setAssignee(String assignee) { this.assignee = assignee; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<String> getReplies() { return replies; }
  }

  /* ---------------- Helpers ---------------- */
  @SuppressWarnings("unchecked")
  private List<Ticket> store(HttpSession s){
    var ctx = s.getServletContext();
    List<Ticket> list = (List<Ticket>) ctx.getAttribute("ADMIN_TICKETS");
    if (list == null) {
      list = Collections.synchronizedList(new ArrayList<>());
      // seed
      list.add(new Ticket(1, "hradmin@company.com", "Không nhận được mail", "Giúp mình kiểm tra SMTP"));
      list.add(new Ticket(2, "demo@company.com", "Sai số ngày phép", "Bảng phép đang hiển thị 0"));
      ctx.setAttribute("ADMIN_TICKETS", list);
    }
    return list;
  }
  private User cur(HttpServletRequest req){
    HttpSession ses = req.getSession(false);
    return (ses != null) ? (User) ses.getAttribute("currentUser") : null;
  }

  /* ---------------- GET ---------------- */
  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    User u = cur(req);
    if (u == null || !u.isAdmin()) { resp.sendError(403); return; }

    List<Ticket> list = store(req.getSession());
    String path = req.getServletPath();
    Csrf.protect(req);

    if ("/admin/support/detail".equals(path)) {
      String idStr = req.getParameter("id");
      if (idStr == null) { resp.sendError(400); return; }
      long id = Long.parseLong(idStr);
      Ticket t = list.stream().filter(x -> x.getId() == id).findFirst().orElse(null);
      if (t == null) { resp.sendError(404); return; }
      req.setAttribute("t", t);
      req.getRequestDispatcher("/WEB-INF/views/admin/support_detail.jsp").forward(req, resp);
      return;
    }

    // list
    req.setAttribute("items", list);
    req.getRequestDispatcher("/WEB-INF/views/admin/support_list.jsp").forward(req, resp);
  }

  /* ---------------- POST ---------------- */
  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    User u = cur(req);
    if (u == null || !u.isAdmin()) { resp.sendError(403); return; }
    if (!Csrf.isTokenValid(req)) { resp.sendError(400, "CSRF invalid"); return; }

    List<Ticket> list = store(req.getSession());
    String action = Optional.ofNullable(req.getParameter("action")).orElse("reply");

    if ("reply".equals(action)) {
      long id = Long.parseLong(req.getParameter("id"));
      String msg = req.getParameter("message");
      list.stream().filter(x -> x.getId() == id).findFirst().ifPresent(t -> {
        if (msg != null && !msg.isBlank()) t.getReplies().add("ADMIN: " + msg.trim());
      });
      Csrf.rotate(req.getSession(false));
      resp.sendRedirect(req.getContextPath() + "/admin/support/detail?id=" + id + "&ok=1");
      return;
    }

    if ("assign".equals(action)) {
      long id = Long.parseLong(req.getParameter("id"));
      String who = req.getParameter("assignee");
      list.stream().filter(x -> x.getId() == id).findFirst().ifPresent(t -> t.setAssignee(who));
    } else if ("status".equals(action)) {
      long id = Long.parseLong(req.getParameter("id"));
      String st = req.getParameter("status");
      list.stream().filter(x -> x.getId() == id).findFirst().ifPresent(t -> t.setStatus(st));
    } else if ("create".equals(action)) {
      long nextId = list.stream().mapToLong(Ticket::getId).max().orElse(0) + 1;
      Ticket t = new Ticket(nextId,
          req.getParameter("email"),
          req.getParameter("subject"),
          req.getParameter("body"));
      list.add(0, t);
    }

    Csrf.rotate(req.getSession(false));
    resp.sendRedirect(req.getContextPath() + "/admin/support?ok=1");
  }
}
