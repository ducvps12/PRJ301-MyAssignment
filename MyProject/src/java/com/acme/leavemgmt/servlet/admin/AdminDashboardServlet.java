package com.acme.leavemgmt.servlet.admin;

import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet(urlPatterns = {"/admin"})
public class AdminDashboardServlet extends HttpServlet {

  // Bảng & khóa người tạo – tự dò
  private static final String[] CANDIDATE_TABLES     = {"[dbo].[Requests]", "[dbo].[LeaveRequest]"};
  private static final String[] CANDIDATE_USER_KEYS  = {"requester_id","user_id","created_by"};

  // Các cột có thể khác tên giữa các schema
  private static final String[] CANDIDATE_TITLE_COLS   = {"title","subject","reason","content","description","note"};
  private static final String[] CANDIDATE_CREATED_COLS = {"created_at","createdAt","created","created_date","createdDate","created_time","createdTime"};
  private static final String[] CANDIDATE_START_COLS   = {"start_date","startDate","from_date","fromDate","date_from","dateFrom"};
  private static final String[] CANDIDATE_END_COLS     = {"end_date","endDate","to_date","toDate","date_to","dateTo"};
  private static final String[] CANDIDATE_DAYS_COLS    = {"days","total_days","totalDays","num_days","numDays"};

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
      final String table  = detectFirstExistingTable(cn, CANDIDATE_TABLES);
      if (table == null) throw new SQLException("Không tìm thấy bảng Requests/LeaveRequest");
      final String userKey = detectFirstExistingColumn(cn, table, CANDIDATE_USER_KEYS);
      if (userKey == null) throw new SQLException("Không tìm thấy cột requester_id/user_id/created_by");

      // Dò các cột linh hoạt
      final String colTitle   = detectFirstExistingColumnAny(cn, table, CANDIDATE_TITLE_COLS);
      final String colCreated = nvl(detectFirstExistingColumnAny(cn, table, CANDIDATE_CREATED_COLS), "created_at");
      final String colStart   = detectFirstExistingColumnAny(cn, table, CANDIDATE_START_COLS);
      final String colEnd     = detectFirstExistingColumnAny(cn, table, CANDIDATE_END_COLS);
      final String colDays    = detectFirstExistingColumnAny(cn, table, CANDIDATE_DAYS_COLS);

      // status: VARCHAR hay số (0/1/2)
      final boolean statusIsNumeric = isNumericStatus(cn, table);

      Map<String, Object> kpis   = loadKpis(cn, table, userKey, colCreated, deptFilter, statusIsNumeric);
      List<Map<String,Object>> recent = loadRecentRequests(cn, table, userKey, colTitle, colCreated, colStart, colEnd, colDays, deptFilter, 10, statusIsNumeric);
      List<Map<String,Object>> today  = loadTodayOnLeave(cn, table, userKey, colStart, colEnd, deptFilter, statusIsNumeric);

      // default an toàn cho JSP
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
      throw new ServletException("Cannot load admin dashboard data: " + e.getMessage(), e);
    }
  }

  // ===== Helpers dò schema =====
  private static String detectFirstExistingTable(Connection cn, String[] candidates) throws SQLException {
    for (String t : candidates) {
      String bare = t.replace("[dbo].","").replace("[","").replace("]","");
      try (ResultSet rs = cn.getMetaData().getTables(null, "dbo", bare, null)) {
        if (rs.next()) return t;
      }
      try (ResultSet rs = cn.getMetaData().getTables(null, null, bare, null)) { // fallback
        if (rs.next()) return t;
      }
    }
    return null;
  }

  private static String detectFirstExistingColumn(Connection cn, String table, String[] candidates) throws SQLException {
    String bare = table.replace("[dbo].","").replace("[","").replace("]","");
    for (String c : candidates) {
      try (ResultSet rs = cn.getMetaData().getColumns(null, "dbo", bare, c)) {
        if (rs.next()) return c;
      }
      try (ResultSet rs = cn.getMetaData().getColumns(null, null, bare, c)) {
        if (rs.next()) return c;
      }
    }
    return null;
  }

  private static String detectFirstExistingColumnAny(Connection cn, String table, String[] candidates) throws SQLException {
    return detectFirstExistingColumn(cn, table, candidates);
  }

  private static boolean isNumericStatus(Connection cn, String table) throws SQLException {
    String bare = table.replace("[dbo].","").replace("[","").replace("]","");
    for (String candidate : new String[]{"status","state"}) {
      try (ResultSet rs = cn.getMetaData().getColumns(null, "dbo", bare, candidate)) {
        if (rs.next()) {
          int dt = rs.getInt("DATA_TYPE");
          return dt == Types.INTEGER || dt == Types.SMALLINT || dt == Types.TINYINT || dt == Types.BIGINT;
        }
      }
    }
    return false; // coi là text
  }

  // ===== Queries =====
  private Map<String, Object> loadKpis(Connection cn, String table, String userKey, String colCreated,
                                       String dept, boolean statusNumeric) throws SQLException {
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
      " AND MONTH(lr."+colCreated+") = MONTH(GETDATE()) AND YEAR(lr."+colCreated+") = YEAR(GETDATE())" +
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
      " WHERE lr."+colCreated+" >= DATEADD(DAY, -30, CAST(GETDATE() AS date))" +
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
      " WHERE MONTH(lr."+colCreated+") = MONTH(GETDATE()) AND YEAR(lr."+colCreated+") = YEAR(GETDATE())" +
      (dept != null ? " AND u.department = ?" : "");
    try (PreparedStatement ps = cn.prepareStatement(sqlTotalMonth)) {
      if (dept != null) ps.setString(1, dept);
      try (ResultSet rs = ps.executeQuery()) { rs.next(); m.put("totalThisMonth", rs.getInt(1)); }
    }
    return m;
  }

  private List<Map<String,Object>> loadRecentRequests(Connection cn, String table, String userKey,
                                                      String colTitle, String colCreated,
                                                      String colStart, String colEnd, String colDays,
                                                      String dept, int limit, boolean statusNumeric) throws SQLException {

    // Title: ưu tiên cột thực tế; nếu rỗng thì hiển thị 'Request #id'
    String titleExpr = (colTitle != null)
      ? "NULLIF(LTRIM(RTRIM(lr."+colTitle+")), '')"
      : null;
    String selectTitle = (titleExpr != null)
      ? "COALESCE("+titleExpr+", 'Request #' + CAST(lr.id AS varchar(20)))"
      : "'Request #' + CAST(lr.id AS varchar(20))";

    // Days: nếu có cột days thì dùng; nếu không có start/end thì coi days = 1
    String daysExpr;
    if (colDays != null) {
      daysExpr = "lr."+colDays;
    } else if (colStart != null && colEnd != null) {
      daysExpr = "DATEDIFF(DAY, lr."+colStart+", lr."+colEnd+") + 1";
    } else {
      daysExpr = "1";
    }

    String selStart = (colStart != null) ? "CAST(lr."+colStart+" AS date)" : "NULL";
    String selEnd   = (colEnd   != null) ? "CAST(lr."+colEnd+"   AS date)" : "NULL";

    String sql =
      "SELECT lr.id, " + selectTitle + " AS title, " +
      statusToUpper("lr.status", statusNumeric) + " AS status, " +
      selStart + " AS start_date, " + selEnd + " AS end_date, lr."+colCreated+" AS created_at, " +
      daysExpr + " AS days, u.full_name AS requester, u.department " +
      "FROM " + table + " lr JOIN [dbo].[Users] u ON u.id = lr." + userKey + " " +
      (dept != null ? "WHERE u.department = ? " : "") +
      "ORDER BY lr."+colCreated+" DESC " +
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

  private List<Map<String,Object>> loadTodayOnLeave(Connection cn, String table, String userKey,
                                                    String colStart, String colEnd,
                                                    String dept, boolean statusNumeric) throws SQLException {
    // Cần start/end để xác định đang nghỉ hôm nay; nếu không có thì trả rỗng.
    if (colStart == null || colEnd == null) return Collections.emptyList();

    String statusApproved = statusNumeric ? "1" : "'APPROVED'";
    String sql =
      "SELECT u.full_name AS requester, u.department, " +
      " CAST(lr."+colStart+" AS date) AS start_date, CAST(lr."+colEnd+" AS date) AS end_date, " +
      " DATEDIFF(DAY, lr."+colStart+", lr."+colEnd+") + 1 AS days " +
      "FROM " + table + " lr JOIN [dbo].[Users] u ON u.id = lr." + userKey + " " +
      "WHERE " + statusExpr("lr.status", statusNumeric) + " = " + statusApproved + " " +
      " AND CAST(GETDATE() AS date) BETWEEN CAST(lr."+colStart+" AS date) AND CAST(lr."+colEnd+" AS date) " +
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
  private static String nvl(String v, String def) { return (v == null || v.isBlank()) ? def : v; }
}
