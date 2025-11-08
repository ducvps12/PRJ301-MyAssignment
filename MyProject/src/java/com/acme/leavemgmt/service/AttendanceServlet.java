package com.acme.leavemgmt.service;

import com.acme.leavemgmt.dao.AttendanceDAO;
import com.acme.leavemgmt.model.AttendanceRecord;
import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.Csrf;
import com.acme.leavemgmt.util.SimpleDS;

import javax.naming.InitialContext;
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

@WebServlet(
    urlPatterns = {"/attendance", "/attendance/clock", "/attendance/admin"},
    initParams = @WebInitParam(name = "jndiName", value = "java:comp/env/jdbc/LeaveMgmt")
)
public class AttendanceServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final int MAX_PAGE_SIZE = 200;
  private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();

  private transient AttendanceDAO dao;

  @Override public void init(ServletConfig cfg) throws ServletException {
    super.init(cfg);
    final var ctx = cfg.getServletContext();
    DataSource ds = null;

    Object o = ctx.getAttribute("DS");
    if (o instanceof DataSource) ds = (DataSource) o;

    if (ds == null) {
      String jndi = Optional.ofNullable(cfg.getInitParameter("jndiName"))
          .filter(s -> !s.isBlank()).orElse("java:comp/env/jdbc/LeaveMgmt");
      try {
        ds = (DataSource) new InitialContext().lookup(jndi);
        ctx.log("[AttendanceServlet] Using JNDI DS: " + jndi);
      } catch (Exception e) {
        ctx.log("[AttendanceServlet] JNDI not available: " + e.getMessage());
      }
    }

    if (ds == null) {
      ds = new SimpleDS();
      ctx.setAttribute("DS", ds);
      ctx.log("[AttendanceServlet] Using fallback SimpleDS");
    }

    this.dao = new AttendanceDAO(ds);
  }

  /* ============================ GET ============================ */

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    final String servletPath = req.getServletPath();
    final HttpSession ss = req.getSession(false);
    final User me = (User) (ss != null ? ss.getAttribute("currentUser") : null);
    if (me == null) { resp.sendRedirect(req.getContextPath() + "/login"); return; }

    final boolean isAdmin = hasAdmin(me);
    if ("/attendance/admin".equals(servletPath) && !isAdmin) {
      resp.sendError(HttpServletResponse.SC_FORBIDDEN); return;
    }

    final LocalDate today = LocalDate.now(DEFAULT_ZONE);
    final LocalDate from = optDate(req, "from", today.withDayOfMonth(1));
    final LocalDate to   = optDate(req, "to", today);
    final String status  = normStatus(req.getParameter("status"));
    final String shift   = normShift(req.getParameter("shift")); // MORNING|AFTERNOON|NIGHT|""

    int page = intParam(req, "page", 1);
    int size = intParam(req, "size", 20);
    if (page < 1) page = 1;
    if (size < 1) size = 20;
    if (size > MAX_PAGE_SIZE) size = MAX_PAGE_SIZE;

    Long viewUserId = isAdmin ? optLong(req, "userId", null) : toLongSafe(me.getId());

    // dùng API mới có shift
    List<AttendanceRecord> rows = dao.list(viewUserId, from, to, status,
        (shift.isEmpty() ? null : shift), page, size);

    if ("csv".equalsIgnoreCase(s(req.getParameter("export")))) {
      exportCsv(resp, rows, from, to, viewUserId, shift);
      return;
    }

    setNoCache(resp);
    Csrf.addToken(req); // để JSP lấy session token hiển thị vào form

    req.setAttribute("rows", rows);
    req.setAttribute("from", from);
    req.setAttribute("to", to);
    req.setAttribute("status", status);
    req.setAttribute("shift", shift);
    req.setAttribute("page", page);
    req.setAttribute("size", size);
    req.setAttribute("isAdmin", isAdmin);
    req.setAttribute("viewUserId", viewUserId);

    req.getRequestDispatcher("/WEB-INF/views/attendance/list.jsp").forward(req, resp);
  }

  /* ============================ POST ============================ */

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    if (!"/attendance/clock".equals(req.getServletPath())) {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND); return;
    }

    // CSRF: chấp nhận _csrf | csrf | csrfToken
    String provided = Optional.ofNullable(req.getParameter("_csrf"))
        .orElseGet(() -> Optional.ofNullable(req.getParameter("csrf"))
        .orElseGet(() -> req.getParameter("csrfToken")));
    boolean csrfOk;
    try {
      // Ưu tiên API của bạn nếu có overload verify(req, token)
      csrfOk = (provided != null && provided.length() > 0 && safeVerify(req, provided))
               || Csrf.isTokenValid(req);
    } catch (Throwable t) {
      csrfOk = Csrf.isTokenValid(req);
    }
    if (!csrfOk) {
      getServletContext().log("[AttendanceServlet] CSRF failed");
      resp.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF invalid"); return;
    }

    final HttpSession ss = req.getSession(false);
    final User me = (User) (ss != null ? ss.getAttribute("currentUser") : null);
    if (me == null) { resp.sendError(HttpServletResponse.SC_UNAUTHORIZED); return; }

    final String action = s(req.getParameter("action")); // "in"|"out"
    if (!"in".equalsIgnoreCase(action) && !"out".equalsIgnoreCase(action)) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "action must be in|out"); return;
    }

    final LocalDate workDate = optDate(req, "date", LocalDate.now(DEFAULT_ZONE));
    final Long meId = toLongSafe(me.getId());
    if (meId == null) { resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid user id"); return; }

    // Ca: lấy param hoặc tự suy ra theo giờ hiện tại
    Shift shift = parseShift(req.getParameter("shift"));
    if (shift == null) {
      shift = detectShift(LocalTime.now(DEFAULT_ZONE));
      if (shift == null) { // ngoài khung 3 ca → yêu cầu chọn ca
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ngoài khung giờ 3 ca. Hãy chọn ca.");
        return;
      }
    }

    AttendanceRecord ar = dao.findByUserDateShift(meId, workDate, shift.name());
    if (ar == null) {
      ar = new AttendanceRecord();
      ar.setUserId(meId);
      ar.setWorkDate(workDate);
      ar.setShiftCode(shift.name());
      ar.setStatus("PRESENT");
      ar.setLateMinutes(0);
      ar.setOtMinutes(0);
    }

    final LocalDateTime now = LocalDateTime.now(DEFAULT_ZONE);
    final LocalDateTime start = LocalDateTime.of(workDate, shiftStart(shift));
    final LocalDateTime end   = LocalDateTime.of(workDate, shiftEnd(shift));

    if ("in".equalsIgnoreCase(action)) {
      if (ar.getCheckIn() == null) {
        ar.setCheckIn(now);
        int late = now.isAfter(start) ? (int) Duration.between(start, now).toMinutes() : 0;
        ar.setLateMinutes(Math.max(0, late));
        resp.setHeader("X-Result", "checked-in");
      } else {
        resp.setHeader("X-Result", "already-in");
      }
    } else { // out
      if (ar.getCheckOut() == null) {
        ar.setCheckOut(now);
        int ot = now.isAfter(end) ? (int) Duration.between(end, now).toMinutes() : 0;
        int curOt = nzInt(ar.getOtMinutes(), 0);
        ar.setOtMinutes(Math.max(curOt, ot));
        resp.setHeader("X-Result", "checked-out");
      } else {
        resp.setHeader("X-Result", "already-out");
      }
    }

    dao.upsert(ar);

    if (isAjax(req)) {
      resp.setContentType("application/json; charset=UTF-8");
      try (PrintWriter w = resp.getWriter()) {
        w.write("{\"ok\":true,"
            + "\"result\":\"" + resp.getHeader("X-Result") + "\","
            + "\"date\":\"" + workDate + "\","
            + "\"shift\":\"" + shift.name() + "\"}");
      }
    } else {
      String redirect = req.getContextPath()
          + "/attendance?from=" + workDate + "&to=" + workDate + "&shift=" + shift.name();
      resp.setStatus(HttpServletResponse.SC_SEE_OTHER);
      resp.setHeader("Location", redirect);
    }
  }

  /* ============================ Helpers ============================ */

  private static boolean safeVerify(HttpServletRequest req, String token) {
    try {
      // nếu class Csrf có verify(req, token) → dùng; nếu không sẽ ném NoSuchMethodError
      return (boolean) Csrf.class.getMethod("verify", HttpServletRequest.class, String.class)
          .invoke(null, req, token);
    } catch (Throwable ignore) {
      return false;
    }
  }

  private enum Shift { MORNING, AFTERNOON, NIGHT }

  private static Shift parseShift(String s) {
    if (s == null) return null;
    s = s.trim().toUpperCase(Locale.ROOT);
    switch (s) {
      case "MORNING": case "SANG": case "SÁNG": return Shift.MORNING;
      case "AFTERNOON": case "CHIEU": case "CHIỀU": return Shift.AFTERNOON;
      case "NIGHT": case "TOI": case "TỐI": return Shift.NIGHT;
      default: return null;
    }
  }

  private static Shift detectShift(LocalTime now) {
    if (now.compareTo(LocalTime.of(8, 0)) >= 0 && now.compareTo(LocalTime.of(11, 30)) <= 0) return Shift.MORNING;
    if (now.compareTo(LocalTime.of(14, 0)) >= 0 && now.compareTo(LocalTime.of(18, 0)) <= 0) return Shift.AFTERNOON;
    if (now.compareTo(LocalTime.of(20, 0)) >= 0 && now.compareTo(LocalTime.MIDNIGHT) <= 0) return Shift.NIGHT;
    return null;
  }

  private static LocalTime shiftStart(Shift sh){
    switch (sh){
      case MORNING:   return LocalTime.of(8, 0);
      case AFTERNOON: return LocalTime.of(14, 0);
      case NIGHT:     return LocalTime.of(20, 0);
      default: throw new IllegalArgumentException();
    }
  }
  private static LocalTime shiftEnd(Shift sh){
    switch (sh){
      case MORNING:   return LocalTime.of(11, 30);
      case AFTERNOON: return LocalTime.of(18, 0);
      case NIGHT:     return LocalTime.MIDNIGHT; // 24:00
      default: throw new IllegalArgumentException();
    }
  }

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

  private static boolean isAjax(HttpServletRequest req) {
    String xr = req.getHeader("X-Requested-With");
    return (xr != null && xr.equalsIgnoreCase("XMLHttpRequest"))
        || "fetch".equalsIgnoreCase(req.getHeader("X-Client"));
  }

  private static void setNoCache(HttpServletResponse resp) {
    resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
    resp.setHeader("Pragma", "no-cache");
  }

  private static String s(String v){ return v == null ? "" : v.trim(); }

  private static int intParam(HttpServletRequest r, String k, int d){
    try { return Integer.parseInt(r.getParameter(k)); } catch (Exception e) { return d; }
  }

  private static LocalDate optDate(HttpServletRequest r, String k, LocalDate def){
    try {
      String v = r.getParameter(k);
      return (v == null || v.isBlank()) ? def : LocalDate.parse(v);
    } catch (Exception e) { return def; }
  }

  private static Long optLong(HttpServletRequest r, String k, Long def){
    try {
      String v = r.getParameter(k);
      return (v == null || v.isBlank()) ? def : Long.valueOf(v.trim());
    } catch (Exception e) { return def; }
  }

  private static Long toLongSafe(Object id){
    if (id == null) return null;
    if (id instanceof Long) return (Long) id;
    if (id instanceof Integer) return ((Integer) id).longValue();
    if (id instanceof Short) return ((Short) id).longValue();
    if (id instanceof Byte) return ((Byte) id).longValue();
    if (id instanceof String) try { return Long.valueOf(((String) id).trim()); } catch (Exception ignore) {}
    return null;
  }

  private static String normStatus(String status){
    String s = s(status).toUpperCase(Locale.ROOT);
    if (s.isEmpty()) return "";
    switch (s) {
      case "PRESENT": case "ABSENT": case "LEAVE": case "WFH": return s;
      default: return "";
    }
  }

  private static String normShift(String shift){
    Shift sh = parseShift(shift);
    return sh == null ? "" : sh.name();
  }

  private static void exportCsv(HttpServletResponse resp, List<AttendanceRecord> rows,
                                LocalDate from, LocalDate to, Long userId, String shift)
      throws IOException {
    resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
    resp.setContentType("text/csv; charset=UTF-8");
    String fn = "attendance_" + (userId == null ? "all" : userId) + "_"
        + (shift == null || shift.isEmpty() ? "ALLSHIFTS" : shift) + "_"
        + from + "_" + to + ".csv";
    resp.setHeader("Content-Disposition", "attachment; filename=\"" + fn + "\"");

    try (PrintWriter w = resp.getWriter()) {
      w.println("UserId,WorkDate,Shift,Status,CheckIn,CheckOut,LateMinutes,OtMinutes");
      for (AttendanceRecord r : rows) {
        String line = String.join(",",
            toStr(r.getUserId()),
            toStr(r.getWorkDate()),
            csvCell(r.getShiftCode()),
            csvCell(r.getStatus()),
            toStr(r.getCheckIn()),
            toStr(r.getCheckOut()),
            String.valueOf(nzInt(r.getLateMinutes(), 0)),
            String.valueOf(nzInt(r.getOtMinutes(), 0))
        );
        w.println(line);
      }
    }
  }

  private static String toStr(Object o){ return (o == null) ? "" : String.valueOf(o); }

  private static int nzInt(Object x, int def){
    if (x == null) return def;
    if (x instanceof Number) return ((Number) x).intValue();
    try { return Integer.parseInt(String.valueOf(x)); } catch (Exception e) { return def; }
  }

  private static String csvCell(String s){
    if (s == null) return "";
    String v = s.replace("\"", "\"\"");
    return (v.contains(",") || v.contains("\"")) ? ("\"" + v + "\"") : v;
  }
}
