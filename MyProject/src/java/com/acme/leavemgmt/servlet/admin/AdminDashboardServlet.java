package com.acme.leavemgmt.servlet.admin;

import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;
import java.util.*;
@WebServlet(urlPatterns = {"/admin"})          // <-- THÊM DÒNG NÀY

public class AdminDashboardServlet extends HttpServlet {

  // cấu hình “mềm” – sẽ tự phát hiện khi run
  private static final String[] CANDIDATE_TABLES = {"[dbo].[Requests]", "[dbo].[LeaveRequest]"};
  private static final String[] CANDIDATE_USER_KEYS = {"requester_id","user_id","created_by"};

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession s = req.getSession(false);
    User me = (s != null) ? (User) s.getAttribute("currentUser") : null;
    if (me == null) { resp.sendRedirect(req.getContextPath()+"/login?next="+req.getRequestURI()); return; }
    if (!(me.isAdmin() || me.isLead())) { resp.sendError(HttpServletResponse.SC_FORBIDDEN); return; }

    final String deptFilter = (me.isAdmin() ? null : safe(me.getDepartment()));

    try (Connection cn = DBConnection.getConnection()) {
      // ==== PHÁT HIỆN SCHEMA ====
      final String table = detectFirstExistingTable(cn, CANDIDATE_TABLES);
      if (table == null) throw new SQLException("Không tìm thấy bảng Requests/LeaveRequest");

      final String userKey = detectFirstExistingColumn(cn, table, CANDIDATE_USER_KEYS);
      if (userKey == null) throw new SQLException("Không tìm thấy cột requester_id/user_id/created_by");

      // status: có thể là VARCHAR hoặc số (0=PENDING,1=APPROVED,2=REJECTED)
      final boolean statusIsNumeric = isNumericStatus(cn, table);

      Map<String, Object> kpis   = loadKpis(cn, table, userKey, deptFilter, statusIsNumeric);
      List<Map<String,Object>> recent = loadRecentRequests(cn, table, userKey, deptFilter, 10, statusIsNumeric);
      List<Map<String,Object>> today  = loadTodayOnLeave(cn, table, userKey, deptFilter, statusIsNumeric);

      // default an toàn cho JSP (tránh null)
      kpis.putIfAbsent("pendingAll", 0);
      kpis.putIfAbsent("approvedThisMonth", 0);
      kpis.putIfAbsent("totalThisMonth", 0);
      kpis.putIfAbsent("approvalRate30d", 0.0);

      req.setAttribute("kpis", kpis);
      req.setAttribute("recentRequests", recent);
      req.setAttribute("todayOnLeave", today);
      req.setAttribute("viewDepartment", deptFilter == null ? "ALL" : deptFilter);

      req.getRequestDispatcher("/WEB-INF/views/admin/admin_dashboard.jsp").forward(req, resp);

    } catch (SQLException e) {
      // log đầy đủ stack; show gọn ra UI (nếu muốn)
      throw new ServletException("Cannot load admin dashboard data: "+ e.getMessage(), e);
    }
  }

  // ===== Schema helpers =====
  private static String detectFirstExistingTable(Connection cn, String[] candidates) throws SQLException {
    for (String t : candidates) {
      // bỏ [dbo].[] khi hỏi metadata
      String bare = t.replace("[dbo].","").replace("[","").replace("]","");
      try (ResultSet rs = cn.getMetaData().getTables(null, null, bare, null)) {
        if (rs.next()) return t;
      }
    }
    return null;
  }
  private static String detectFirstExistingColumn(Connection cn, String table, String[] candidates) throws SQLException {
    String bare = table.replace("[dbo].","").replace("[","").replace("]","");
    for (String c : candidates) {
      try (ResultSet rs = cn.getMetaData().getColumns(null, null, bare, c)) {
        if (rs.next()) return c;
      }
    }
    return null;
  }
  private static boolean isNumericStatus(Connection cn, String table) throws SQLException {
    String bare = table.replace("[dbo].","").replace("[","").replace("]","");
    try (ResultSet rs = cn.getMetaData().getColumns(null, null, bare, "status")) {
      if (rs.next()) {
        int dataType = rs.getInt("DATA_TYPE"); // java.sql.Types
        return switch (dataType) {
          case Types.INTEGER, Types.SMALLINT, Types.TINYINT, Types.BIGINT -> true;
          default -> false;
        };
      }
    }
    // fallback: coi là text
    return false;
  }

  // ===== Queries (đã vá) =====
  private Map<String, Object> loadKpis(Connection cn, String table, String userKey, String dept, boolean statusNumeric) throws SQLException {
    Map<String, Object> m = new HashMap<>();

    String statusPending  = statusNumeric ? "0" : "'PENDING'";
    String statusApproved = statusNumeric ? "1" : "'APPROVED'";
    String statusRejected = statusNumeric ? "2" : "'REJECTED'";

    // Pending
    String sqlPending =
      "SELECT COUNT(*) FROM " + table + " lr JOIN [dbo].[Users] u ON u.id = lr." + userKey +
      " WHERE " + statusExpr("lr.status", statusNumeric) + " = " + statusPending +
      (dept != null ? " AND u.department = ?" : "");
    try (PreparedStatement ps = cn.prepareStatement(sqlPending)) {
      if (dept != null) ps.setString(1, dept);
      try (ResultSet rs = ps.executeQuery()) { rs.next(); m.put("pendingAll", rs.getInt(1)); }
    }

    // Approved this month
    String sqlApprovedMonth =
      "SELECT COUNT(*) FROM " + table + " lr JOIN [dbo].[Users] u ON u.id = lr." + userKey +
      " WHERE " + statusExpr("lr.status", statusNumeric) + " = " + statusApproved +
      " AND MONTH(lr.created_at) = MONTH(GETDATE()) AND YEAR(lr.created_at) = YEAR(GETDATE())" +
      (dept != null ? " AND u.department = ?" : "");
    try (PreparedStatement ps = cn.prepareStatement(sqlApprovedMonth)) {
      if (dept != null) ps.setString(1, dept);
      try (ResultSet rs = ps.executeQuery()) { rs.next(); m.put("approvedThisMonth", rs.getInt(1)); }
    }

    // Approval rate 30d
    String sqlAR =
      "SELECT " +
      " SUM(CASE WHEN " + statusExpr("lr.status", statusNumeric) + " = " + statusApproved + " THEN 1 ELSE 0 END) AS appr," +
      " SUM(CASE WHEN " + statusExpr("lr.status", statusNumeric) + " IN (" + statusApproved + "," + statusRejected + ") THEN 1 ELSE 0 END) AS base" +
      " FROM " + table + " lr JOIN [dbo].[Users] u ON u.id = lr." + userKey +
      " WHERE lr.created_at >= DATEADD(DAY, -30, CAST(GETDATE() AS date))" +
      (dept != null ? " AND u.department = ?" : "");
    try (PreparedStatement ps = cn.prepareStatement(sqlAR)) {
      if (dept != null) ps.setString(1, dept);
      try (ResultSet rs = ps.executeQuery()) {
        rs.next();
        int appr = rs.getInt("appr");
        int base = rs.getInt("base");
        m.put("approvalRate30d", base == 0 ? 0.0 : (appr * 100.0 / base));
        m.put("approvalBase30d", base);
      }
    }

    // Total this month
    String sqlTotalMonth =
      "SELECT COUNT(*) FROM " + table + " lr JOIN [dbo].[Users] u ON u.id = lr." + userKey +
      " WHERE MONTH(lr.created_at) = MONTH(GETDATE()) AND YEAR(lr.created_at) = YEAR(GETDATE())" +
      (dept != null ? " AND u.department = ?" : "");
    try (PreparedStatement ps = cn.prepareStatement(sqlTotalMonth)) {
      if (dept != null) ps.setString(1, dept);
      try (ResultSet rs = ps.executeQuery()) { rs.next(); m.put("totalThisMonth", rs.getInt(1)); }
    }
    return m;
  }

  private List<Map<String,Object>> loadRecentRequests(Connection cn, String table, String userKey, String dept, int limit, boolean statusNumeric) throws SQLException {
    // Dùng OFFSET…FETCH để khỏi lệ thuộc TOP(?)
    String sql =
      "SELECT lr.id, lr.title, " + statusToUpper("lr.status", statusNumeric) + " AS status, " +
      " CAST(lr.start_date AS date) AS start_date, CAST(lr.end_date AS date) AS end_date, lr.created_at, " +
      " DATEDIFF(DAY, lr.start_date, lr.end_date) + 1 AS days, u.full_name AS requester, u.department " +
      "FROM " + table + " lr JOIN [dbo].[Users] u ON u.id = lr." + userKey + " " +
      (dept != null ? "WHERE u.department = ? " : "") +
      "ORDER BY lr.created_at DESC " +
      "OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY";

    List<Map<String,Object>> list = new ArrayList<>();
    try (PreparedStatement ps = cn.prepareStatement(sql)) {
      int i = 1;
      if (dept != null) ps.setString(i++, dept);
      ps.setInt(i, Math.max(1, limit));
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          Map<String,Object> row = new LinkedHashMap<>();
          row.put("id", rs.getInt("id"));
          row.put("title", rs.getString("title"));
          row.put("status", rs.getString("status"));
          row.put("days", rs.getInt("days"));
          row.put("startDate", rs.getDate("start_date"));
          row.put("endDate", rs.getDate("end_date"));
          row.put("createdAt", rs.getTimestamp("created_at"));
          row.put("requester", rs.getString("requester"));
          row.put("department", rs.getString("department"));
          list.add(row);
        }
      }
    }
    return list;
  }

  private List<Map<String,Object>> loadTodayOnLeave(Connection cn, String table, String userKey, String dept, boolean statusNumeric) throws SQLException {
    String statusApproved = statusNumeric ? "1" : "'APPROVED'";
    String sql =
      "SELECT u.full_name AS requester, u.department, " +
      " CAST(lr.start_date AS date) AS start_date, CAST(lr.end_date AS date) AS end_date, " +
      " DATEDIFF(DAY, lr.start_date, lr.end_date) + 1 AS days " +
      "FROM " + table + " lr JOIN [dbo].[Users] u ON u.id = lr." + userKey + " " +
      "WHERE " + statusExpr("lr.status", statusNumeric) + " = " + statusApproved + " " +
      " AND CAST(GETDATE() AS date) BETWEEN CAST(lr.start_date AS date) AND CAST(lr.end_date AS date) " +
      (dept != null ? " AND u.department = ? " : "") +
      "ORDER BY u.full_name ASC";

    List<Map<String,Object>> list = new ArrayList<>();
    try (PreparedStatement ps = cn.prepareStatement(sql)) {
      if (dept != null) ps.setString(1, dept);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          Map<String,Object> row = new LinkedHashMap<>();
          row.put("requester", rs.getString("requester"));
          row.put("department", rs.getString("department"));
          row.put("startDate", rs.getDate("start_date"));
          row.put("endDate", rs.getDate("end_date"));
          row.put("days", rs.getInt("days"));
          list.add(row);
        }
      }
    }
    return list;
  }

  private static String statusExpr(String col, boolean numeric) {
    return numeric ? col : "UPPER(" + col + ")";
  }
  private static String statusToUpper(String col, boolean numeric) {
    return numeric
      ? "CASE " + col + " WHEN 0 THEN 'PENDING' WHEN 1 THEN 'APPROVED' WHEN 2 THEN 'REJECTED' ELSE 'UNKNOWN' END"
      : "UPPER(" + col + ")";
  }
  private static String safe(String s) { return (s == null) ? null : s.trim(); }
}
