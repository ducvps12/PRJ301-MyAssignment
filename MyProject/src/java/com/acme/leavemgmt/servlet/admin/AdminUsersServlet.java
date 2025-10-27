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

  /** DÒNG NÀY QUAN TRỌNG: JavaBean để JSP EL truy cập được qua getter */
  public static class Row {
    private int id;
    private String username;
    private String fullName;
    private String email;
    private String role;
    private String department;
    private String status; // "ACTIVE"/"INACTIVE"

    // Getters (EL cần những hàm này)
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getDepartment() { return department; }
    public String getStatus() { return status; }

    // Setters (để servlet set dữ liệu)
    public void setId(int id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setDepartment(String department) { this.department = department; }
    public void setStatus(String status) { this.status = status; }
  }

  // Page có getter để JSP EL đọc được (page.data, page.pageIndex, ...)
  public static class Page<T> {
    private int pageIndex, pageSize, totalPages, totalItems;
    private List<T> data = new ArrayList<>();

    public int getPageIndex()  { return pageIndex; }
    public int getPageSize()   { return pageSize; }
    public int getTotalPages() { return totalPages; }
    public int getTotalItems() { return totalItems; }
    public List<T> getData()   { return data; }

    // Tiện ích cho JSP
    public boolean isHasPrev() { return pageIndex > 1; }
    public boolean isHasNext() { return pageIndex < Math.max(1, totalPages); }
    public int getPrevPage()   { return Math.max(1, pageIndex - 1); }
    public int getNextPage()   { return Math.min(Math.max(1, totalPages), pageIndex + 1); }
    public int getStartRow()   { return (pageIndex - 1) * pageSize; }

    // Setters để servlet set giá trị
    public void setPageIndex(int v)   { this.pageIndex = v; }
    public void setPageSize(int v)    { this.pageSize = v; }
    public void setTotalPages(int v)  { this.totalPages = v; }
    public void setTotalItems(int v)  { this.totalItems = v; }
    public void setData(List<T> list) { this.data = (list != null) ? list : new ArrayList<>(); }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    // 1) Guard quyền
    HttpSession session = req.getSession(false);
    User me = (session != null) ? (User) session.getAttribute("currentUser") : null;
    if (me == null || !(me.isAdmin() || me.isLeader())) {
      resp.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    // 2) Param
    String q = n(req.getParameter("q"));
    String status = n(req.getParameter("status")); // ACTIVE/INACTIVE/""
    int page = parseInt(req.getParameter("page"), 1);
    int size = parseInt(req.getParameter("size"), 10);
    if (size <= 0 || size > 100) size = 10;
    if (page <= 0) page = 1;
    int offset = (page - 1) * size;

    // 3) WHERE
    StringBuilder where = new StringBuilder(" WHERE 1=1 ");
    List<Object> params = new ArrayList<>();
    if (!q.isBlank()) {
      where.append(" AND (username LIKE ? OR full_name LIKE ? OR email LIKE ?) ");
      String kw = "%" + q + "%";
      params.add(kw); params.add(kw); params.add(kw);
    }
    if ("ACTIVE".equalsIgnoreCase(status))      where.append(" AND status = 1 ");
    else if ("INACTIVE".equalsIgnoreCase(status)) where.append(" AND status = 0 ");

    // 4) SQL (SQL Server)
    String sqlCount = "SELECT COUNT(*) FROM Users " + where;
    String sqlData  =
        "SELECT id, username, full_name AS fullName, email, role, department, " +
        "       CASE WHEN status=1 THEN 'ACTIVE' ELSE 'INACTIVE' END AS status " +
        "FROM Users " + where + " ORDER BY id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    Page<Row> pageObj = new Page<>();
    pageObj.setPageIndex(page);
    pageObj.setPageSize(size);

    try (Connection cn = DBConnection.getConnection()) {
      // count
      try (PreparedStatement ps = cn.prepareStatement(sqlCount)) {
        bind(ps, params);
        try (ResultSet rs = ps.executeQuery()) {
          if (rs.next()) pageObj.setTotalItems(rs.getInt(1));
        }
      }

      // Điều chỉnh page nếu vượt
      int totalPages = (int)Math.ceil((pageObj.getTotalItems() * 1.0) / size);
      if (totalPages == 0) totalPages = 1;
      if (page > totalPages) {
        page = totalPages;
        offset = (page - 1) * size;
        pageObj.setPageIndex(page);
      }

      // data
      try (PreparedStatement ps = cn.prepareStatement(sqlData)) {
        List<Object> p2 = new ArrayList<>(params);
        p2.add(offset); p2.add(size);
        bind(ps, p2);
        List<Row> rows = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
          while (rs.next()) {
            Row r = new Row();
            r.setId(rs.getInt("id"));
            r.setUsername(rs.getString("username"));
            r.setFullName(rs.getString("fullName")); // alias đảm bảo EL là fullName
            r.setEmail(rs.getString("email"));
            r.setRole(rs.getString("role"));
            r.setDepartment(rs.getString("department"));
            r.setStatus(rs.getString("status"));
            rows.add(r);
          }
        }
        pageObj.setData(rows);
      }

      pageObj.setTotalPages((int)Math.ceil((pageObj.getTotalItems() * 1.0) / size));
      if (pageObj.getTotalPages() == 0) pageObj.setTotalPages(1);

    } catch (SQLException e) {
      throw new ServletException(e);
    }

    // 5) CSRF token cho form POST
    String csrf = Csrf.ensureToken(req.getSession());
    req.setAttribute("csrf", csrf);

    // 6) No-store
    resp.setHeader("Cache-Control", "no-store");

    // 7) Audit view
    try {
      AuditLog.log(req, "ADMIN_USERS_VIEW", "USER", me.getId(),
          "q="+q+", status="+status+", page="+pageObj.getPageIndex());
    } catch (Throwable ignored) {}

    // 8) Attribute cho JSP
    req.setAttribute("page", pageObj);
    req.setAttribute("q", q);
    req.setAttribute("status", status);

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
