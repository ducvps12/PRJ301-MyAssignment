package com.acme.leavemgmt.servlet.admin;

import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet(name = "AdminDashboardServlet", urlPatterns = {"/admin"})
public class AdminDashboardServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    // ---- 1) AuthN + AuthZ ----
    HttpSession s = req.getSession(false);
    User me = (s != null) ? (User) s.getAttribute("currentUser") : null;
    if (me == null) {
      resp.sendRedirect(req.getContextPath() + "/login");
      return;
    }
    if (!(me.isAdmin() || me.isLead())) {       // chỉ ADMIN & các *_LEAD/ *_LEADER
      resp.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    String deptFilter = me.isAdmin() ? null : me.getDepartment(); // admin: toàn hệ thống

    // ---- 2) Load số liệu tổng quan ----
    try (Connection cn = DBConnection.getConnection()) {

      Map<String, Object> kpis = loadKpis(cn, deptFilter);
      List<Map<String, Object>> recent = loadRecentRequests(cn, deptFilter, 10);
      List<Map<String, Object>> today = loadTodayOnLeave(cn, deptFilter);

      req.setAttribute("kpis", kpis);
      req.setAttribute("recentRequests", recent);
      req.setAttribute("todayOnLeave", today);
      req.setAttribute("viewDepartment", deptFilter == null ? "ALL" : deptFilter);

      // ---- 3) Forward về JSP ----
      req.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(req, resp);

    } catch (SQLException e) {
      throw new ServletException("Cannot load dashboard data", e);
    }
  }

  // ================== Helpers ==================

  /** KPIs: pendingAll, approvedThisMonth, approvalRate30d, totalThisMonth */
  private Map<String, Object> loadKpis(Connection cn, String dept) throws SQLException {
    Map<String, Object> m = new HashMap<>();

    // 2.1 Pending
    String sqlPending = """
      SELECT COUNT(*) 
      FROM LeaveRequest lr 
      JOIN Users u ON u.id = lr.requester_id
      WHERE lr.status = 'PENDING' %s
      """.formatted(dept != null ? "AND u.department = ?" : "");
    try (PreparedStatement ps = cn.prepareStatement(sqlPending)) {
      if (dept != null) ps.setString(1, dept);
      try (ResultSet rs = ps.executeQuery()) {
        rs.next(); m.put("pendingAll", rs.getInt(1));
      }
    }

    // 2.2 Approved this month
    String sqlApprovedMonth = """
      SELECT COUNT(*) 
      FROM LeaveRequest lr 
      JOIN Users u ON u.id = lr.requester_id
      WHERE lr.status = 'APPROVED'
        AND MONTH(lr.created_at) = MONTH(GETDATE())
        AND YEAR(lr.created_at) = YEAR(GETDATE())
        %s
      """.formatted(dept != null ? "AND u.department = ?" : "");
    try (PreparedStatement ps = cn.prepareStatement(sqlApprovedMonth)) {
      if (dept != null) ps.setString(1, dept);
      try (ResultSet rs = ps.executeQuery()) {
        rs.next(); m.put("approvedThisMonth", rs.getInt(1));
      }
    }

    // 2.3 Approval rate 30d = approved / (approved + rejected)
    String sqlAR = """
      SELECT
        SUM(CASE WHEN lr.status='APPROVED' THEN 1 ELSE 0 END) AS appr,
        SUM(CASE WHEN lr.status IN ('APPROVED','REJECTED') THEN 1 ELSE 0 END) AS base
      FROM LeaveRequest lr
      JOIN Users u ON u.id = lr.requester_id
      WHERE lr.created_at >= DATEADD(DAY, -30, CAST(GETDATE() AS date))
      %s
      """.formatted(dept != null ? "AND u.department = ?" : "");
    try (PreparedStatement ps = cn.prepareStatement(sqlAR)) {
      if (dept != null) ps.setString(1, dept);
      try (ResultSet rs = ps.executeQuery()) {
        rs.next();
        int appr = rs.getInt("appr");
        int base = rs.getInt("base");
        double rate = (base == 0) ? 0.0 : (appr * 100.0 / base);
        m.put("approvalRate30d", rate);
        m.put("approvalBase30d", base);
      }
    }

    // 2.4 Total requests this month
    String sqlTotalMonth = """
      SELECT COUNT(*)
      FROM LeaveRequest lr
      JOIN Users u ON u.id = lr.requester_id
      WHERE MONTH(lr.created_at) = MONTH(GETDATE())
        AND YEAR(lr.created_at) = YEAR(GETDATE())
        %s
      """.formatted(dept != null ? "AND u.department = ?" : "");
    try (PreparedStatement ps = cn.prepareStatement(sqlTotalMonth)) {
      if (dept != null) ps.setString(1, dept);
      try (ResultSet rs = ps.executeQuery()) {
        rs.next(); m.put("totalThisMonth", rs.getInt(1));
      }
    }

    return m;
  }

  /** Bảng “Yêu cầu mới nhất” */
  private List<Map<String, Object>> loadRecentRequests(Connection cn, String dept, int limit)
      throws SQLException {
    String sql = """
      SELECT TOP (?) 
             lr.id, lr.title, lr.status, lr.days,
             lr.start_date, lr.end_date, lr.created_at,
             u.full_name AS requester, u.department
      FROM LeaveRequest lr
      JOIN Users u ON u.id = lr.requester_id
      %s
      ORDER BY lr.created_at DESC
      """.formatted(dept != null ? "WHERE u.department = ?" : "");

    List<Map<String, Object>> list = new ArrayList<>();
    try (PreparedStatement ps = cn.prepareStatement(sql)) {
      int i = 1;
      ps.setInt(i++, limit);
      if (dept != null) ps.setString(i, dept);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          Map<String, Object> row = new LinkedHashMap<>();
          row.put("id", rs.getInt("id"));
          row.put("title", rs.getString("title"));
          row.put("status", rs.getString("status"));
          row.put("days", rs.getBigDecimal("days"));
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

  /** Danh sách đang nghỉ hôm nay (đã APPROVED) */
  private List<Map<String, Object>> loadTodayOnLeave(Connection cn, String dept)
      throws SQLException {
    String sql = """
      SELECT u.full_name AS requester, u.department, lr.start_date, lr.end_date, lr.days
      FROM LeaveRequest lr
      JOIN Users u ON u.id = lr.requester_id
      WHERE lr.status = 'APPROVED'
        AND CAST(GETDATE() AS date) BETWEEN lr.start_date AND lr.end_date
        %s
      ORDER BY u.full_name ASC
      """.formatted(dept != null ? "AND u.department = ?" : "");

    List<Map<String, Object>> list = new ArrayList<>();
    try (PreparedStatement ps = cn.prepareStatement(sql)) {
      if (dept != null) ps.setString(1, dept);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          Map<String, Object> row = new LinkedHashMap<>();
          row.put("requester", rs.getString("requester"));
          row.put("department", rs.getString("department"));
          row.put("startDate", rs.getDate("start_date"));
          row.put("endDate", rs.getDate("end_date"));
          row.put("days", rs.getBigDecimal("days"));
          list.add(row);
        }
      }
    }
    return list;
  }
}
