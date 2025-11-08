package com.acme.leavemgmt.service;

import com.acme.leavemgmt.dao.AttendanceDAO;
import com.acme.leavemgmt.dao.PayrollDAO;
import com.acme.leavemgmt.dao.PayrollDAO.PayrollItem;
import com.acme.leavemgmt.util.Csrf;

import javax.sql.DataSource;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

@WebServlet(urlPatterns = {"/payroll", "/payroll/run"})
public class PayrollServlet extends HttpServlet {

  private DataSource ds;
  private PayrollDAO payroll;
  private AttendanceDAO attendance;

  @Override public void init(ServletConfig cfg) throws ServletException {
    super.init(cfg);
    ds = (DataSource) cfg.getServletContext().getAttribute("DS");
    if (ds == null) throw new ServletException("Missing DataSource DS");
    payroll = new PayrollDAO(ds);
    attendance = new AttendanceDAO(ds);
  }

  /* ================== GET: luôn hiện nhân viên ACTIVE ================== */
  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");

    int y = intParam(req, "y", LocalDate.now().getYear());
    int m = intParam(req, "m", LocalDate.now().getMonthValue());

    long runId = payroll.findRun(y, m).orElseGet(() -> payroll.createRun(y, m));

    // Luôn có danh sách nhân viên bằng LEFT JOIN Users
    List<Map<String,Object>> itemsForView = payroll.listView(runId);

    req.setAttribute("y", y);
    req.setAttribute("m", m);
    req.setAttribute("runId", runId);
    req.setAttribute("items", itemsForView); // JSP dùng keys: full_name, base_salary, ...
    req.getRequestDispatcher("/WEB-INF/views/payroll/list.jsp").forward(req, resp);
  }

  /* ================== POST: tính & lưu ================== */
  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");
    Csrf.verify(req);

    int y = intParam(req, "y", LocalDate.now().getYear());
    int m = intParam(req, "m", LocalDate.now().getMonthValue());

    long runId = payroll.findRun(y, m).orElseGet(() -> payroll.createRun(y, m));

    // Lấy user ACTIVE + department + salary_decimal (nếu có)
    List<Emp> users = loadActiveUsers();
    if (users.isEmpty()) {
      resp.sendError(500, "Không tìm thấy nhân sự ACTIVE trong Users.");
      return;
    }

    for (Emp u : users) {
      if (u == null || u.id == null) continue;

      Map<String,Object> at = attendance.monthSummary(u.id, y, m);
      int workdays  = asInt(at.getOrDefault("presentDays", at.getOrDefault("workdays", 0)));
      int lateCount = asInt(at.getOrDefault("lateCount", 0));
      int otMinutes = asInt(at.getOrDefault("totalOTMin", 0));

      // === Tất cả đều BigDecimal ===
      BigDecimal base = (u.baseSalary != null) ? u.baseSalary : bd(10_000_000L);

      BigDecimal dayRate    = base.divide(bd(22), 2, RoundingMode.HALF_UP);
      BigDecimal baseSalary = dayRate.multiply(bd(workdays));

      BigDecimal hourly   = base.divide(bd(22 * 8), 2, RoundingMode.HALF_UP);
      BigDecimal otHours  = bd(otMinutes).divide(bd(60), 2, RoundingMode.HALF_UP);
      BigDecimal otPay    = otHours.multiply(hourly).multiply(bd(15, 10)); // 1.5x

      BigDecimal allowance = bd(500_000);                                // ví dụ phụ cấp
      BigDecimal penalty   = bd(lateCount).multiply(bd(30_000));         // ví dụ phạt

      BigDecimal insurance = baseSalary.multiply(bd(10, 100))            // 10%
                                       .setScale(0, RoundingMode.HALF_UP);
      BigDecimal taxable   = baseSalary.add(otPay).add(allowance).subtract(insurance);
      if (taxable.signum() < 0) taxable = BigDecimal.ZERO;
      BigDecimal tax = taxable.multiply(bd(5, 100))                      // 5%
                               .setScale(0, RoundingMode.HALF_UP);

      BigDecimal bonus = switch (safe(u.department).toUpperCase()) {
        case "SALE", "SALES" -> bd(1_000_000);
        case "QA"            -> bd(500_000);
        default              -> BigDecimal.ZERO;
      };

      BigDecimal net = baseSalary.add(allowance).add(otPay).add(bonus)
          .subtract(penalty).subtract(insurance).subtract(tax);

      PayrollItem pi = new PayrollItem();
      pi.setRunId(runId);
      pi.setUserId(u.id);
      pi.setBaseSalary(baseSalary);
      pi.setAllowance(allowance);
      pi.setOtPay(otPay);
      pi.setBonus(bonus);
      pi.setPenalty(penalty);
      pi.setInsurance(insurance);
      pi.setTax(tax);
      pi.setNetPay(net);
      pi.setNote(String.format("Auto calc %d-%02d", y, m));

      payroll.upsertItem(pi);
    }

    resp.sendRedirect(req.getContextPath() + "/payroll?y="+y+"&m="+m);
  }

  /* ================== Helpers ================== */
  private static int intParam(HttpServletRequest r, String k, int d){
    try { return Integer.parseInt(r.getParameter(k)); } catch(Exception e){ return d; }
  }
  private static BigDecimal bd(long v){ return BigDecimal.valueOf(v); }
  /** num/den (ví dụ 15/10=1.5; 5/100=0.05) */
  private static BigDecimal bd(int num, int den){
    return BigDecimal.valueOf(num).divide(BigDecimal.valueOf(den), 4, RoundingMode.HALF_UP);
  }
  private static BigDecimal bd(int v){ return BigDecimal.valueOf(v); }
  private static String safe(Object o){ return o == null ? "" : String.valueOf(o); }
  private static int asInt(Object o){
    try { return (o instanceof Number n) ? n.intValue() : Integer.parseInt(String.valueOf(o)); }
    catch (Exception e){ return 0; }
  }

  /** Tải user ACTIVE: id, department, salary_decimal (nếu có) */
  private List<Emp> loadActiveUsers() {
    final String SQL = "SELECT id, department, salary_decimal FROM dbo.Users WHERE status = 1";
    List<Emp> list = new ArrayList<>();
    try (Connection c = ds.getConnection();
         PreparedStatement ps = c.prepareStatement(SQL);
         ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        Emp e = new Emp();
        e.id = rs.getLong("id");
        try { e.department = rs.getString("department"); } catch (SQLException ignore) {}
        try { e.baseSalary = rs.getBigDecimal("salary_decimal"); } catch (SQLException ignore) {}
        list.add(e);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Load active users error", e);
    }
    return list;
  }

  /** cấu trúc tạm cho tính lương */
  static class Emp {
    Long id;
    String department;
    BigDecimal baseSalary; // từ Users.salary_decimal (nullable)
  }
}
