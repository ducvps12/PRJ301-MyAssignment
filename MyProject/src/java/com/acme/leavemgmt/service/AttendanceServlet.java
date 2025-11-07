package com.acme.leavemgmt.service;

import com.acme.leavemgmt.dao.AttendanceDAO;
import com.acme.leavemgmt.model.AttendanceRecord;
import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.Csrf;

import javax.sql.DataSource;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * GET  /attendance           : list + filter (người thường chỉ thấy của mình; admin xem được theo userId)
 * POST /attendance/clock     : check-in / check-out (param: action=in|out, date=yyyy-MM-dd optional)
 * GET  /attendance/admin     : tương tự /attendance nhưng bắt buộc quyền admin
 *
 * Yêu cầu tối thiểu ở AttendanceDAO:
 *   - List<AttendanceRecord> list(Long userId, LocalDate from, LocalDate to, String status, int page, int size)
 *   - AttendanceRecord findByUserDate(Long userId, LocalDate workDate)
 *   - void upsert(AttendanceRecord record)
 */
@WebServlet(urlPatterns = {"/attendance", "/attendance/clock", "/attendance/admin"})
public class AttendanceServlet extends HttpServlet {

  private AttendanceDAO dao;

  @Override
  public void init(ServletConfig cfg) throws ServletException {
    super.init(cfg);
    DataSource ds = (DataSource) cfg.getServletContext().getAttribute("DS");
    if (ds == null) {
      throw new ServletException("Missing DataSource DS in ServletContext");
    }
    this.dao = new AttendanceDAO(ds);
  }

  // ======= GET: danh sách =======
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    final String servletPath = req.getServletPath();  // "/attendance" | "/attendance/admin"

    HttpSession ss = req.getSession(false);
    User me = (User) (ss != null ? ss.getAttribute("currentUser") : null);
    if (me == null) {
      resp.sendRedirect(req.getContextPath() + "/login");
      return;
    }

    boolean isAdmin = hasAdmin(me);

    // /attendance/admin bắt buộc quyền admin
    if ("/attendance/admin".equals(servletPath) && !isAdmin) {
      resp.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    LocalDate from  = optDate(req, "from", LocalDate.now().withDayOfMonth(1));
    LocalDate to    = optDate(req, "to",   LocalDate.now());
    String status   = s(req.getParameter("status"));
    int page        = intParam(req, "page", 1);
    int size        = intParam(req, "size", 20);

    // Nếu là admin thì có thể xem userId bất kỳ (nếu không truyền => xem tất cả),
    // còn user thường chỉ xem của chính mình.
    Long userId = null;
    if (isAdmin) {
      userId = optLong(req, "userId", null);
    } else {
      userId = toLong(me.getId()); // FIX: ép kiểu int -> Long an toàn
    }

    List<AttendanceRecord> rows = dao.list(userId, from, to, status, page, size);

    req.setAttribute("rows", rows);
    req.setAttribute("from", from);
    req.setAttribute("to", to);
    req.setAttribute("status", status);
    req.setAttribute("page", page);
    req.setAttribute("size", size);
    req.setAttribute("isAdmin", isAdmin);
    req.setAttribute("viewUserId", userId);

    req.getRequestDispatcher("/WEB-INF/views/attendance/list.jsp").forward(req, resp);
  }

  // ======= POST: chấm công nhanh (in/out) =======
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    // Chỉ cho phép POST vào /attendance/clock
    if (!"/attendance/clock".equals(req.getServletPath())) {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    Csrf.verify(req);

    HttpSession ss = req.getSession(false);
    User me = (User) (ss != null ? ss.getAttribute("currentUser") : null);
    if (me == null) {
      resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    String action = s(req.getParameter("action"));   // "in" | "out"
    if (!"in".equalsIgnoreCase(action) && !"out".equalsIgnoreCase(action)) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "action must be in|out");
      return;
    }

    LocalDate workDate = optDate(req, "date", LocalDate.now());

    // FIX: đảm bảo tham số userId dùng Long
    Long meId = toLong(me.getId());

    AttendanceRecord ar = dao.findByUserDate(meId, workDate);
    if (ar == null) {
      ar = new AttendanceRecord();
      ar.setUserId(meId);              // FIX: int -> Long
      ar.setWorkDate(workDate);
      ar.setStatus("PRESENT");
      ar.setLateMinutes(0);
      ar.setOtMinutes(0);
    }

    LocalDateTime now = LocalDateTime.now();

    if ("in".equalsIgnoreCase(action)) {
      if (ar.getCheckIn() == null) {
        ar.setCheckIn(now);

        // Ví dụ: đến sau 08:30 coi là trễ
        LocalDateTime cutoff = workDate.atTime(8, 30);
        int late = now.isAfter(cutoff)
            ? (int) java.time.Duration.between(cutoff, now).toMinutes()
            : 0;
        ar.setLateMinutes(Math.max(0, late));
      }
    } else { // "out"
      if (ar.getCheckOut() == null) {
        ar.setCheckOut(now);

      // khi tạo mới:
ar.setOtMinutes(0);

// khi check-out:
LocalDateTime otStart = workDate.atTime(17, 30);
int ot = now.isAfter(otStart)
    ? (int) java.time.Duration.between(otStart, now).toMinutes()
    : 0;

// chỉ cần max giữa giá trị hiện tại (int) và ot mới
ar.setOtMinutes(Math.max(ar.getOtMinutes(), ot));

      }
    }

    dao.upsert(ar);

    // Quay về list trong ngày
    String back = req.getContextPath() + "/attendance?from=" + workDate + "&to=" + workDate;
    resp.sendRedirect(back);
  }

  // ======= Helpers =======

  private static boolean hasAdmin(User u) {
    if (u == null || u.getRole() == null) return false;
    String r = u.getRole().trim().toUpperCase();
    return "ADMIN".equals(r) || "SYS_ADMIN".equals(r) || "HR_ADMIN".equals(r);
  }

  private static String s(String v) {
    return v == null ? "" : v.trim();
  }

  private static int intParam(HttpServletRequest r, String k, int d) {
    try { return Integer.parseInt(r.getParameter(k)); }
    catch (Exception e) { return d; }
  }

  private static LocalDate optDate(HttpServletRequest r, String k, LocalDate def) {
    try {
      String v = r.getParameter(k);
      return (v == null || v.isBlank()) ? def : LocalDate.parse(v);
    } catch (Exception e) { return def; }
  }

  private static Long optLong(HttpServletRequest r, String k, Long def) {
    try {
      String v = r.getParameter(k);
      if (v == null || v.isBlank()) return def;
      return Long.valueOf(v.trim());
    } catch (Exception e) { return def; }
  }

  /** Chuyển mọi kiểu id số nguyên (int/Integer/long/Long) về Long an toàn. */
  private static Long toLong(Object id) {
    if (id == null) return null;
    if (id instanceof Long) return (Long) id;
    if (id instanceof Integer) return Long.valueOf(((Integer) id).longValue());
    if (id instanceof Short) return Long.valueOf(((Short) id).longValue());
    if (id instanceof Byte) return Long.valueOf(((Byte) id).longValue());
    if (id instanceof String) {
      try { return Long.valueOf(((String) id).trim()); } catch (Exception ignored) {}
    }
    // Trường hợp `User#getId()` là int primitive:
    // Java sẽ box thành Integer khi truyền vào Object -> cover ở trên.
    return null;
  }
}
