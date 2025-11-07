package com.acme.leavemgmt.service;

import com.acme.leavemgmt.dao.AttendanceDAO;
import com.acme.leavemgmt.model.AttendanceRecord;
import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.Csrf;
import com.acme.leavemgmt.util.SimpleDS;

import javax.sql.DataSource;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import javax.naming.InitialContext;

/**
 * GET  /attendance            : list + filter (user thường chỉ thấy của mình; admin xem theo userId hoặc all)
 * GET  /attendance?export=csv : xuất CSV theo filter hiện tại
 * POST /attendance/clock      : check-in / check-out (action=in|out, date=yyyy-MM-dd optional)
 * GET  /attendance/admin      : như /attendance nhưng bắt buộc quyền admin
 */
@WebServlet(
    urlPatterns = {"/attendance", "/attendance/clock", "/attendance/admin"},
    initParams = { @WebInitParam(name = "jndiName", value = "java:comp/env/jdbc/LeaveMgmt") }
)
public class AttendanceServlet extends HttpServlet {

  private static final int MAX_PAGE_SIZE = 200;
  private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();

  private transient AttendanceDAO dao;

  @Override
  public void init(ServletConfig cfg) throws ServletException {
    super.init(cfg);
    final var ctx = cfg.getServletContext();

    DataSource ds = null;

    // 1) Thử lấy DS đã gắn sẵn trong Context (nếu có)
    Object o = ctx.getAttribute("DS");
    if (o instanceof DataSource) ds = (DataSource) o;

    // 2) Fallback JNDI (nếu bạn đã cấu hình)
    if (ds == null) {
      String jndiName = Optional.ofNullable(cfg.getInitParameter("jndiName"))
          .filter(s -> !s.isBlank()).orElse("java:comp/env/jdbc/LeaveMgmt");
      try {
        ds = (DataSource) new InitialContext().lookup(jndiName);
        ctx.log("[AttendanceServlet] DS via JNDI: " + jndiName);
      } catch (Exception ignore) {
        ctx.log("[AttendanceServlet] JNDI not available, will use DBConnection.");
      }
    }

    // 3) Dùng luôn DBConnection của bạn (không JNDI)
    if (ds == null) {
      ds = new SimpleDS();
      ctx.setAttribute("DS", ds); // cache cho servlet/DAO khác dùng chung
      ctx.log("[AttendanceServlet] DS via DBConnection::getConnection");
    }

    this.dao = new AttendanceDAO(ds);
    ctx.log("[AttendanceServlet] DAO initialized");
  }

  // ======= GET: danh sách / export CSV =======
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    final String servletPath = req.getServletPath(); // "/attendance" | "/attendance/admin"
    final HttpSession ss = req.getSession(false);
    final User me = (User) (ss != null ? ss.getAttribute("currentUser") : null);
    if (me == null) {
      resp.sendRedirect(req.getContextPath() + "/login");
      return;
    }

    final boolean isAdmin = hasAdmin(me);

    if ("/attendance/admin".equals(servletPath) && !isAdmin) {
      resp.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    final LocalDate today = LocalDate.now(DEFAULT_ZONE);
    final LocalDate from  = optDate(req, "from", today.withDayOfMonth(1));
    final LocalDate to    = optDate(req, "to",   today);
    final String   status = normStatus(req.getParameter("status"));

    int page = intParam(req, "page", 1);
    int size = intParam(req, "size", 20);
    if (page < 1) page = 1;
    if (size < 1) size = 20;
    if (size > MAX_PAGE_SIZE) size = MAX_PAGE_SIZE;

    Long viewUserId = isAdmin ? optLong(req, "userId", null) : toLongSafe(me.getId());

    List<AttendanceRecord> rows = dao.list(viewUserId, from, to, status, page, size);

    // Export CSV
    if ("csv".equalsIgnoreCase(s(req.getParameter("export")))) {
      exportCsv(resp, rows, from, to, viewUserId, status);
      return;
    }

    req.setAttribute("rows", rows);
    req.setAttribute("from", from);
    req.setAttribute("to", to);
    req.setAttribute("status", status);
    req.setAttribute("page", page);
    req.setAttribute("size", size);
    req.setAttribute("isAdmin", isAdmin);
    req.setAttribute("viewUserId", viewUserId);

    req.getRequestDispatcher("/WEB-INF/views/attendance/list.jsp").forward(req, resp);
  }

  // ======= POST: chấm công nhanh (in/out) =======
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    if (!"/attendance/clock".equals(req.getServletPath())) {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    Csrf.verify(req);

    final HttpSession ss = req.getSession(false);
    final User me = (User) (ss != null ? ss.getAttribute("currentUser") : null);
    if (me == null) {
      resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    final String action = s(req.getParameter("action"));   // "in" | "out"
    if (!"in".equalsIgnoreCase(action) && !"out".equalsIgnoreCase(action)) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "action must be in|out");
      return;
    }

    final LocalDate workDate = optDate(req, "date", LocalDate.now(DEFAULT_ZONE));
    final Long meId = toLongSafe(me.getId());
    if (meId == null) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid user id");
      return;
    }

    AttendanceRecord ar = dao.findByUserDate(meId, workDate);
    if (ar == null) {
      ar = new AttendanceRecord();
      ar.setUserId(meId);
      ar.setWorkDate(workDate);
      ar.setStatus("PRESENT");
      ar.setLateMinutes(0);
      ar.setOtMinutes(0);
    }

    final LocalDateTime now = LocalDateTime.now(DEFAULT_ZONE);

    if ("in".equalsIgnoreCase(action)) {
      if (ar.getCheckIn() == null) {
        ar.setCheckIn(now);
        LocalDateTime cutoff = workDate.atTime(8, 30);
        int late = now.isAfter(cutoff) ? (int) Duration.between(cutoff, now).toMinutes() : 0;
        ar.setLateMinutes(Math.max(0, late));
        resp.setHeader("X-Result", "checked-in");
      } else {
        resp.setHeader("X-Result", "already-in");
      }
    } else { // "out"
      if (ar.getCheckOut() == null) {
        ar.setCheckOut(now);
        LocalDateTime otStart = workDate.atTime(17, 30);
        int ot = now.isAfter(otStart) ? (int) Duration.between(otStart, now).toMinutes() : 0;
        int curOt = nzInt(ar.getOtMinutes(), 0); // an toàn cho int/Integer
        ar.setOtMinutes(Math.max(curOt, ot));
        resp.setHeader("X-Result", "checked-out");
      } else {
        resp.setHeader("X-Result", "already-out");
      }
    }

    dao.upsert(ar);
    resp.sendRedirect(req.getContextPath() + "/attendance?from=" + workDate + "&to=" + workDate);
  }

  // ======= Helpers =======

  private static boolean hasAdmin(User u) {
    if (u == null) return false;
    String r = null;
    try { if (u.getRole() != null) r = u.getRole(); } catch (Exception ignored) {}
    try { var m = u.getClass().getMethod("getRoleCode"); Object v = m.invoke(u);
          if (r == null && v instanceof String) r = (String) v; } catch (Exception ignored) {}
    if (r == null) return false;
    r = r.trim().toUpperCase(Locale.ROOT);
    return "ADMIN".equals(r) || "SYS_ADMIN".equals(r) || "HR_ADMIN".equals(r)
        || "DIV_LEADER".equals(r) || "TEAM_LEAD".equals(r);
  }

  private static String s(String v){ return v==null? "": v.trim(); }

  private static int intParam(HttpServletRequest r, String k, int d){
    try{ return Integer.parseInt(r.getParameter(k)); }catch(Exception e){ return d; }
  }

  private static LocalDate optDate(HttpServletRequest r, String k, LocalDate def){
    try{
      String v=r.getParameter(k);
      return (v==null||v.isBlank())? def: LocalDate.parse(v);
    }catch(Exception e){ return def; }
  }

  private static Long optLong(HttpServletRequest r, String k, Long def){
    try{
      String v=r.getParameter(k);
      return (v==null||v.isBlank())? def: Long.valueOf(v.trim());
    }catch(Exception e){ return def; }
  }

  /** Chuyển id kiểu bất kỳ về Long an toàn (hợp cả primitive lẫn wrapper). */
  private static Long toLongSafe(Object id){
    if (id==null) return null;
    if (id instanceof Long) return (Long) id;
    if (id instanceof Integer) return ((Integer) id).longValue();
    if (id instanceof Short) return ((Short) id).longValue();
    if (id instanceof Byte) return ((Byte) id).longValue();
    if (id instanceof String) try{ return Long.valueOf(((String)id).trim()); }catch(Exception ignore){}
    return null;
  }

  private static String normStatus(String status){
    String s=s(status).toUpperCase(Locale.ROOT);
    if (s.isEmpty()) return "";
    switch (s){
      case "PRESENT": case "ABSENT": case "LEAVE": case "WFH": return s;
      default: return "";
    }
  }

  private static void exportCsv(HttpServletResponse resp, List<AttendanceRecord> rows,
                                LocalDate from, LocalDate to, Long userId, String status)
      throws IOException {
    resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
    resp.setContentType("text/csv; charset=UTF-8");
    String fn = "attendance_" + (userId == null ? "all" : userId) + "_" + from + "_" + to + ".csv";
    resp.setHeader("Content-Disposition", "attachment; filename=\"" + fn + "\"");

    try (PrintWriter w = resp.getWriter()){
      w.println("UserId,WorkDate,Status,CheckIn,CheckOut,LateMinutes,OtMinutes");
      for (AttendanceRecord r : rows){
        String line = String.join(",",
            toStr(r.getUserId()),                              // an toàn cho long/Long
            toStr(r.getWorkDate()),                            // LocalDate
            csvCell(r.getStatus()),                            // String
            toStr(r.getCheckIn()),                             // LocalDateTime
            toStr(r.getCheckOut()),                            // LocalDateTime
            String.valueOf(nzInt(r.getLateMinutes(), 0)),      // int/Integer
            String.valueOf(nzInt(r.getOtMinutes(), 0))         // int/Integer
        );
        w.println(line);
      }
    }
  }

  /** Chuẩn hoá giá trị bất kỳ sang chuỗi; null -> "" (hợp cả primitive vì có auto-boxing). */
  private static String toStr(Object o){ return (o == null) ? "" : String.valueOf(o); }

  /** Lấy int an toàn từ cả int/Integer/null. null -> defaultVal. */
  private static int nzInt(Object x, int defaultVal){
    if (x == null) return defaultVal;
    if (x instanceof Number) return ((Number) x).intValue();
    try { return Integer.parseInt(String.valueOf(x)); } catch (Exception e) { return defaultVal; }
  }

  private static String csvCell(String s){
    if (s==null) return "";
    String v=s.replace("\"","\"\"");
    return (v.contains(",")||v.contains("\"")) ? ("\"" + v + "\"") : v;
  }
}
