package com.acme.leavemgmt.service;

import com.acme.leavemgmt.dao.AttendanceDAO;
import com.acme.leavemgmt.dao.PayrollDAO;
import com.acme.leavemgmt.dao.PayrollDAO.PayrollItem; // <-- dùng inner class của DAO
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

  // ======= Danh sách =======
  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    int y = intParam(req, "y", LocalDate.now().getYear());
    int m = intParam(req, "m", LocalDate.now().getMonthValue());

    long runId = payroll.findRun(y, m).orElse(-1L);

    // FIX: đúng kiểu trả về của DAO
    List<PayrollItem> items = payroll.listItems(runId);

    req.setAttribute("y", y);
    req.setAttribute("m", m);
    req.setAttribute("runId", runId);
    req.setAttribute("items", items);
    req.getRequestDispatcher("/WEB-INF/views/payroll/list.jsp").forward(req, resp);
  }

  // ======= Tạo/tính run =======
  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    Csrf.verify(req);

    int y = intParam(req, "y", LocalDate.now().getYear());
    int m = intParam(req, "m", LocalDate.now().getMonthValue());

    // 1) đảm bảo có run
    long runId = payroll.findRun(y, m).orElseGet(() -> payroll.createRun(y, m));

    // 2) lấy danh sách user cần tính (tuỳ bạn – ở đây giả định có trong ServletContext)
    @SuppressWarnings("unchecked")
    List<User> users = (List<User>) getServletContext().getAttribute("ALL_USERS");
    if (users == null || users.isEmpty()) {
      resp.sendError(500, "Không có danh sách nhân sự để tính lương.");
      return;
    }

    // 3) Tính cho từng user (rule demo)
    for (User u : users) {
      if (u == null || !"ACTIVE".equalsIgnoreCase(String.valueOf(u.getStatus()))) continue;

      Long userId = toLong(u.getId()); // tránh lệch int/long
      Map<String,Object> at = attendance.monthSummary(userId, y, m);
      int workdays   = ((Number) at.getOrDefault("presentDays",
                          at.getOrDefault("workdays", 0))).intValue();
      int lateCount  = ((Number) at.getOrDefault("lateCount", 0)).intValue();
      int otMinutes  = ((Number) at.getOrDefault("totalOTMin", 0)).intValue();

      // Base salary: cố gắng đọc từ User bằng nhiều tên thường gặp; nếu không có => 10,000,000
      double baseInput = baseSalaryOf(u, 10_000_000d); 
      BigDecimal base = bd(baseInput);

      // lương theo ngày công (22 ngày/tháng)
      BigDecimal dayRate   = base.divide(bd(22), 2, RoundingMode.HALF_UP);
      BigDecimal baseSalary= dayRate.multiply(bd(workdays));

      // OT: 1.5x theo giờ (8h/ngày, 22 ngày)
      BigDecimal hourly = base.divide(bd(22 * 8), 2, RoundingMode.HALF_UP);
      BigDecimal otPay  = bd(otMinutes / 60.0).multiply(hourly).multiply(bd(1.5));

      // Phụ cấp demo
      BigDecimal allowance = bd(500_000);

      // Phạt đi muộn: 30k/lần
      BigDecimal penalty = bd(lateCount * 30_000);

      // BHXH 10% baseSalary
      BigDecimal insurance = baseSalary.multiply(bd(0.10)).setScale(0, RoundingMode.HALF_UP);

      // Thuế TNCN 5% trên (baseSalary + ot + allowance - insurance)
      BigDecimal taxable = baseSalary.add(otPay).add(allowance).subtract(insurance);
      if (taxable.signum() < 0) taxable = BigDecimal.ZERO;
      BigDecimal tax = taxable.multiply(bd(0.05)).setScale(0, RoundingMode.HALF_UP);

      // Thưởng theo phòng ban (demo)
      BigDecimal bonus = switch (safe(u.getDepartment())) {
        case "SALE", "SALES" -> bd(1_000_000);
        case "QA" -> bd(500_000);
        default -> BigDecimal.ZERO;
      };

      BigDecimal net = baseSalary.add(allowance).add(otPay).add(bonus)
          .subtract(penalty).subtract(insurance).subtract(tax);

      // Dùng đúng kiểu PayrollItem của DAO (các setter này tồn tại trong inner class của DAO)
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
      pi.setNote("Auto calc " + y + "-" + (m < 10 ? "0" : "") + m);

      payroll.upsertItem(pi);
    }

    resp.sendRedirect(req.getContextPath() + "/payroll?y="+y+"&m="+m);
  }

  // ===== Helpers =====
  private static int intParam(HttpServletRequest r, String k, int d){
    try { return Integer.parseInt(r.getParameter(k)); } catch(Exception e){ return d; }
  }
  private static String safe(Object s){ return s==null? "" : String.valueOf(s); }
  private static BigDecimal bd(double v){ return BigDecimal.valueOf(v); }

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
    if (id instanceof Long) return (Long) id;
    if (id instanceof Integer) return Long.valueOf(((Integer) id).longValue());
    if (id instanceof Short) return Long.valueOf(((Short) id).longValue());
    if (id instanceof Byte) return Long.valueOf(((Byte) id).longValue());
    try { return Long.valueOf(String.valueOf(id).trim()); } catch (Exception e) { return null; }
  }
}
