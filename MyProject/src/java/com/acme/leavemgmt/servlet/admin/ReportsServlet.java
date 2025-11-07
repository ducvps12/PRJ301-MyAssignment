package com.acme.leavemgmt.servlet.admin;

import com.acme.leavemgmt.dao.ReportsDAO;
import com.acme.leavemgmt.util.DBConnection; // dùng util bạn đã có

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet(urlPatterns = {"/admin/reports", "/admin/reports/*"})
public class ReportsServlet extends HttpServlet {

  // ===== Helpers =====
  private static LocalDate pDate(HttpServletRequest req, String k, LocalDate def) {
    try {
      String v = req.getParameter(k);
      return (v == null || v.isBlank()) ? def : LocalDate.parse(v);
    } catch (Exception e) { return def; }
  }
  private static Integer pInt(HttpServletRequest req, String k) {
    try {
      String v = req.getParameter(k);
      return (v == null || v.isBlank()) ? null : Integer.valueOf(v);
    } catch (Exception e) { return null; }
  }
  private static String pStr(HttpServletRequest req, String k) {
    String v = req.getParameter(k);
    return (v == null || v.isBlank()) ? null : v.trim();
  }

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");

    // Định tuyến
    String pathInfo = req.getPathInfo();         // có thể null
    String path = (pathInfo == null) ? "/" : pathInfo;

    // Khoảng mặc định 30 ngày gần nhất
    LocalDate today = LocalDate.now();
    LocalDate to    = pDate(req, "to",   today);
    LocalDate from  = pDate(req, "from", today.minusDays(29));

    // >>> MỞ KẾT NỐI QUA DBConnection <<<
    // Nếu DBConnection của bạn là instance method, đổi dòng dưới thành:
    // try (Connection cnn = new DBConnection().getConnection()) {
    try (Connection cnn = DBConnection.getConnection()) {

      ReportsDAO dao = new ReportsDAO(cnn);

      // Trang danh mục "Bộ báo cáo"
      if ("/".equals(path)) {
        req.getRequestDispatcher("/WEB-INF/views/reports/reports_home.jsp").forward(req, resp);
        return;
      }

      // ----- Daily -----
      if ("/requests/daily".equals(path)) {
        Integer deptId = pInt(req, "deptId");
        String  status = pStr(req, "status");

        LinkedHashMap<LocalDate, Integer> daily    = dao.requestsDaily(from, to, deptId, status);
        LinkedHashMap<String, Integer>    byDept   = dao.requestsByDept(from, to);
        LinkedHashMap<String, Integer>    byStatus = dao.requestsByStatus(from, to);

        // Xuất CSV
        if ("csv".equalsIgnoreCase(pStr(req, "export"))) {
          resp.setContentType("text/csv; charset=UTF-8");
          resp.setHeader("Content-Disposition", "attachment; filename=\"requests-daily.csv\"");
          try (PrintWriter out = resp.getWriter()) {
            out.println("day,count");
            for (Map.Entry<LocalDate,Integer> e : daily.entrySet()) {
              out.println(e.getKey() + "," + e.getValue());
            }
          }
          return;
        }

        req.setAttribute("from", from);
        req.setAttribute("to", to);
        req.setAttribute("daily", daily);
        req.setAttribute("byDept", byDept);
        req.setAttribute("byStatus", byStatus);
        req.getRequestDispatcher("/WEB-INF/views/reports/requests_daily.jsp").forward(req, resp);
        return;
      }

      // ----- Monthly (reuse daily) -----
      if ("/requests/monthly".equals(path)) {
        resp.sendRedirect(req.getContextPath() + "/admin/reports/requests/daily?from="
            + from.withDayOfMonth(1) + "&to=" + to);
        return;
      }

      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
  }
}
