package com.acme.leavemgmt.servlet.admin;

import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.AuditLog;
import com.acme.leavemgmt.util.Csrf;
import com.acme.leavemgmt.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name="AdminUsersServlet", urlPatterns={"/admin/users"})
public class AdminUsersServlet extends HttpServlet {

  public static class Row {
    public int id;
    public String username, fullName, email, role, department, status; // ACTIVE/INACTIVE
  }
  public static class Page<T> {
    public int pageIndex, pageSize, totalPages, totalItems;
    public List<T> data = new ArrayList<>();
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    // 1) Guard quyền
    User me = (User) req.getSession().getAttribute("currentUser");
    if (me == null || !(me.isAdmin() || me.isLeader())) { // tự bạn hiện có isAdmin/isLeader
      resp.sendError(403); return;
    }

    // 2) Param
    String q = n(req.getParameter("q"));
    String status = n(req.getParameter("status")); // ACTIVE/INACTIVE/""
    int page = parseInt(req.getParameter("page"), 1);
    int size = parseInt(req.getParameter("size"), 10);
    if (size <= 0 || size > 100) size = 10;
    int offset = (page - 1) * size;

    // 3) WHERE
    StringBuilder where = new StringBuilder(" WHERE 1=1 ");
    List<Object> params = new ArrayList<>();
    if (!q.isBlank()) {
      where.append(" AND (username LIKE ? OR full_name LIKE ? OR email LIKE ?) ");
      String kw = "%" + q + "%";
      params.add(kw); params.add(kw); params.add(kw);
    }
    if ("ACTIVE".equalsIgnoreCase(status))   where.append(" AND status = 1 ");
    else if ("INACTIVE".equalsIgnoreCase(status)) where.append(" AND status = 0 ");

    // 4) SQL (SQL Server)
    String sqlCount = "SELECT COUNT(*) FROM Users " + where;
    String sqlData  =
        "SELECT id, username, full_name AS fullName, email, role, department, " +
        "       CASE WHEN status=1 THEN 'ACTIVE' ELSE 'INACTIVE' END AS status " +
        "FROM Users " + where + " ORDER BY id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    Page<Row> pageObj = new Page<>();
    pageObj.pageIndex = page; pageObj.pageSize = size;

    try (Connection cn = DBConnection.getConnection()) {
      // count
      try (PreparedStatement ps = cn.prepareStatement(sqlCount)) {
        bind(ps, params);
        try (ResultSet rs = ps.executeQuery()) { if (rs.next()) pageObj.totalItems = rs.getInt(1); }
      }
      // data
      try (PreparedStatement ps = cn.prepareStatement(sqlData)) {
        List<Object> p2 = new ArrayList<>(params);
        p2.add(offset); p2.add(size);
        bind(ps, p2);
        try (ResultSet rs = ps.executeQuery()) {
          while (rs.next()) {
            Row r = new Row();
            r.id = rs.getInt("id");
            r.username = rs.getString("username");
            r.fullName = rs.getString("fullName");
            r.email = rs.getString("email");
            r.role = rs.getString("role");
            r.department = rs.getString("department");
            r.status = rs.getString("status");
            pageObj.data.add(r);
          }
        }
      }
      pageObj.totalPages = (int)Math.ceil((pageObj.totalItems*1.0)/pageObj.pageSize);
    } catch (SQLException e) {
      throw new ServletException(e);
    }

    // 5) CSRF token cho form POST
    String csrf = Csrf.ensureToken(req.getSession());
    req.setAttribute("csrf", csrf);

    // 6) No-store
    resp.setHeader("Cache-Control", "no-store");

    // 7) Audit view
    try { AuditLog.log(req, "ADMIN_USERS_VIEW", "USER", me.getId(), "q="+q+", status="+status+", page="+page); } catch (Throwable ignored) {}

    req.setAttribute("page", pageObj);
    req.getRequestDispatcher("/WEB-INF/views/admin/users.jsp").forward(req, resp);
  }

  private static void bind(PreparedStatement ps, List<Object> params) throws SQLException {
    for (int i = 0; i < params.size(); i++) {
      Object v = params.get(i);
      if (v instanceof Integer) ps.setInt(i+1, (Integer) v);
      else ps.setString(i+1, String.valueOf(v));
    }
  }
  private static String n(String s){ return s==null? "": s.trim(); }
  private static int parseInt(String s, int d){ try{ return Integer.parseInt(s);}catch(Exception e){return d;} }
}
