package com.acme.leavemgmt.service;

import com.acme.leavemgmt.dao.AttendanceDAO;
import com.acme.leavemgmt.dao.PayrollDAO;
import com.acme.leavemgmt.dao.PayrollDAO.PayrollItem;
import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.Csrf;

import javax.sql.DataSource;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/payroll", "/payroll/run"})
public class PayrollServlet extends HttpServlet {

  private PayrollDAO payroll;
  private AttendanceDAO attendance;

  @Override public void init(ServletConfig cfg) throws ServletException {
    super.init(cfg);
    DataSource ds = (DataSource) cfg.getServletContext().getAttribute("DS");
    if (ds == null) throw new ServletException("Missing DataSource DS");
    this.payroll = new PayrollDAO(ds);
    this.attendance = new AttendanceDAO(ds);
  }

  // ================== GET: xem danh sách ==================
  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");

    int y = intParam(req, "y", LocalDate.now().getYear());
    int m = intParam(req, "m", LocalDate.now().getMonthValue());

    // nếu chưa có run thì tạo luôn cho tiện xem/sửa
    long runId = payroll.findRun(y, m).orElseGet(() -> payroll.createRun(y, m));

    // >>> DAO dạng đối tượng:
    List<PayrollItem> items = payroll.listItemsAsObjects(runId);

    req.setAttribute("y", y);
    req.setAttribute("m", m);
    req.setAttribute("runId", runId);
    req.setAttribute("items", items);
    req.getRequestDispatcher("/WEB-INF/views/payroll/list.jsp").forward(req, resp);
  }

  // ================== POST: tính & lưu ==================
  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");

    Csrf.verify(req);

    int y = intParam(req, "y", LocalDate.now().getYear());
    int m = intParam(req, "m", LocalDate.now().getMonthValue());

    // 1) đảm bảo có run
    long runId = payroll.findRun(y, m).orElseGet(() -> payroll.createRun(y, m));

    // 2) lấy danh sách user cần tính
    @SuppressWarnings("unchecked")
    List<User> users = (List<User>) getServletContext().getAttribute("ALL_USERS");
    if (users == null || users.isEmpty()) {
      resp.sendError(500, "Không có danh sách nhân sự để tính lương.");
      return;
    }

    // 3) Tính cho từng user
    for (User u : users) {
      if (u == null) continue;
      Object st = safeObj(u.getStatus());
      if (st != null && !"ACTIVE".equalsIgnoreCase(String.valueOf(st))) continue;

      Long userId = toLong(u.getId());
      if (userId == null) continue;

      Map<String,Object> at = attendance.monthSummary(userId, y, m);
      int workdays  = num(at.getOrDefault("presentDays", at.getOrDefault("workdays", 0))).intValue();
      int lateCount = num(at.getOrDefault("lateCount", 0)).intValue();
      int otMinutes = num(at.getOrDefault("totalOTMin", 0)).intValue();

      double baseInput = baseSalaryOf(u, 10_000_000d);
      BigDecimal base = bd(baseInput);

      BigDecimal dayRate    = base.divide(bd(22), 2, RoundingMode.HALF_UP);
      BigDecimal baseSalary = dayRate.multiply(bd(workdays));

      BigDecimal hourly = base.divide(bd(22 * 8), 2, RoundingMode.HALF_UP);
      BigDecimal otPay  = bd(otMinutes / 60.0).multiply(hourly).multiply(bd(1.5));

      BigDecimal allowance = bd(500_000);               // demo
      BigDecimal penalty   = bd(lateCount * 30_000);    // demo

      BigDecimal insurance = baseSalary.multiply(bd(0.10)).setScale(0, RoundingMode.HALF_UP);
      BigDecimal taxable   = baseSalary.add(otPay).add(allowance).subtract(insurance);
      if (taxable.signum() < 0) taxable = BigDecimal.ZERO;
      BigDecimal tax = taxable.multiply(bd(0.05)).setScale(0, RoundingMode.HALF_UP);

      BigDecimal bonus = switch (safeStr(u.getDepartment()).toUpperCase()) {
        case "SALE", "SALES" -> bd(1_000_000);
        case "QA"            -> bd(500_000);
        default              -> BigDecimal.ZERO;
      };

      BigDecimal net = baseSalary.add(allowance).add(otPay).add(bonus)
          .subtract(penalty).subtract(insurance).subtract(tax);

      PayrollItem pi = new PayrollItem();
      pi.setRunId(runId);
      pi.setUserId(userId);
      pi.setBaseSalary(baseSalary);
      pi.setAllowance(allowance);
      pi.setOtPay(otPay);
      pi.setBonus(bonus);
      pi.setPenalty(penalty);
      pi.setInsurance(insurance);
      pi.setTax(tax);
      pi.setNetPay(net);
      pi.setNote("Auto calc %d-%02d".formatted(y, m));

      payroll.upsertItem(pi);
    }

    resp.sendRedirect(req.getContextPath() + "/payroll?y="+y+"&m="+m);
  }

  // ================== Helpers ==================
  private static int intParam(HttpServletRequest r, String k, int d){
    try { return Integer.parseInt(r.getParameter(k)); } catch(Exception e){ return d; }
  }
  private static BigDecimal bd(double v){ return BigDecimal.valueOf(v); }
  private static Number num(Object o){
    if (o instanceof Number n) return n;
    try { return Double.valueOf(String.valueOf(o)); } catch(Exception e){ return 0; }
  }
  private static Object safeObj(Object o){ return o; }
  private static String safeStr(Object o){ return o == null ? "" : String.valueOf(o); }

  /** Lấy base salary từ User bằng nhiều tên getter phổ biến; nếu không có thì trả defaultVal. */
  private static double baseSalaryOf(User u, double defaultVal){
    if (u == null) return defaultVal;
    String[] guesses = {"getBaseSalary","getSalaryBase","getSalary","getWage","getMonthlySalary"};
    for (String m : guesses) {
      try {
        var meth = u.getClass().getMethod(m);
        Object val = meth.invoke(u);
        if (val instanceof Number)   return ((Number) val).doubleValue();
        if (val != null)             return Double.parseDouble(String.valueOf(val));
      } catch (Exception ignore) {}
    }
    return defaultVal;
  }

  /** Chuẩn hoá về Long an toàn (User#getId có thể là int/Integer/long/Long/String). */
  private static Long toLong(Object id) {
    if (id == null) return null;
    if (id instanceof Long)    return (Long) id;
    if (id instanceof Integer) return ((Integer) id).longValue();
    if (id instanceof Short)   return ((Short) id).longValue();
    if (id instanceof Byte)    return ((Byte) id).longValue();
    try { return Long.valueOf(String.valueOf(id).trim()); } catch (Exception e) { return null; }
  }
}
