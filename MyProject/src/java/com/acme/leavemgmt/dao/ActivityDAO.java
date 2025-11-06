package com.acme.leavemgmt.dao;

import com.acme.leavemgmt.model.Activity;
import com.acme.leavemgmt.util.DBConnection;
import jakarta.servlet.http.HttpServletRequest;

import java.io.PrintWriter;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** DAO cho bảng dbo.User_Activity */
public class ActivityDAO {

  private static final String TBL = "[dbo].[User_Activity]";

  /* ========================= SEARCH ========================= */
  public Page<Activity> search(Integer userId, String action, String q,
                               LocalDate from, LocalDate to,
                               int page, int size) {
    final int p = Math.max(1, page);
    final int s = Math.max(1, size);

    StringBuilder where = new StringBuilder(" WHERE 1=1 ");
    List<Object> params = new ArrayList<>();

    if (userId != null) { where.append(" AND ua.user_id = ? "); params.add(userId); }
    if (action != null && !action.trim().isEmpty()) { where.append(" AND ua.action = ? "); params.add(action.trim()); }
    if (q != null && !q.trim().isEmpty()) {
      where.append(" AND ( ua.note LIKE ? OR ua.ip_addr LIKE ? OR ua.user_agent LIKE ? OR ua.entity_type LIKE ? ) ");
      String like = "%" + q.trim() + "%";
      params.add(like); params.add(like); params.add(like); params.add(like);
    }
    if (from != null) { where.append(" AND ua.created_at >= ? "); params.add(Timestamp.valueOf(from.atStartOfDay())); }
    if (to != null)   { where.append(" AND ua.created_at <  ? "); params.add(Timestamp.valueOf(to.plusDays(1).atStartOfDay())); }

    String sqlCount = "SELECT COUNT(*) FROM " + TBL + " ua " + where;

    String sqlPage =
        "SELECT ua.id, ua.user_id, ua.action, ua.entity_type, ua.entity_id, " +
        "       ua.note, ua.ip_addr, ua.user_agent, ua.created_at, " +
        "       u.username AS " + Activity.COL_USER_NAME + " " +
        "FROM " + TBL + " ua " +
        "LEFT JOIN [dbo].[Users] u ON ua.user_id = u.id " +
        where + " " +
        "ORDER BY ua.created_at DESC " +
        "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    List<Activity> items = new ArrayList<>();
    int total = 0;

    try (Connection c = DBConnection.getConnection()) {
      // count
      try (PreparedStatement ps = c.prepareStatement(sqlCount)) {
        bind(ps, params);
        try (ResultSet rs = ps.executeQuery()) { if (rs.next()) total = rs.getInt(1); }
      }
      // page
      try (PreparedStatement ps = c.prepareStatement(sqlPage)) {
        List<Object> pparams = new ArrayList<>(params);
        pparams.add((p - 1) * s);
        pparams.add(s);
        bind(ps, pparams);
        try (ResultSet rs = ps.executeQuery()) {
          while (rs.next()) items.add(Activity.from(rs));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return new Page<>(items, total, p, s);
  }

  /* ========================= EXPORT CSV ========================= */
  public void exportCsv(Integer userId, String action, String q,
                        LocalDate from, LocalDate to,
                        PrintWriter out) {
    StringBuilder where = new StringBuilder(" WHERE 1=1 ");
    List<Object> params = new ArrayList<>();

    if (userId != null) { where.append(" AND ua.user_id = ? "); params.add(userId); }
    if (action != null && !action.trim().isEmpty()) { where.append(" AND ua.action = ? "); params.add(action.trim()); }
    if (q != null && !q.trim().isEmpty()) {
      where.append(" AND ( ua.note LIKE ? OR ua.ip_addr LIKE ? OR ua.user_agent LIKE ? OR ua.entity_type LIKE ? ) ");
      String like = "%" + q.trim() + "%";
      params.add(like); params.add(like); params.add(like); params.add(like);
    }
    if (from != null) { where.append(" AND ua.created_at >= ? "); params.add(Timestamp.valueOf(from.atStartOfDay())); }
    if (to != null)   { where.append(" AND ua.created_at <  ? "); params.add(Timestamp.valueOf(to.plusDays(1).atStartOfDay())); }

    String sql =
        "SELECT ua.created_at, ua.user_id, u.username AS " + Activity.COL_USER_NAME + ", ua.action, " +
        "       ua.entity_type, ua.entity_id, ua.note, ua.ip_addr, ua.user_agent " +
        "FROM " + TBL + " ua " +
        "LEFT JOIN [dbo].[Users] u ON ua.user_id = u.id " +
        where + " " +
        "ORDER BY ua.created_at DESC";

    out.println("time,userId,username,action,entityType,entityId,note,ip,userAgent");
    try (Connection c = DBConnection.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
      bind(ps, params);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          String time = ts(rs.getTimestamp("created_at"));
          String uid  = toStr(rs.getObject("user_id"));
          String un   = csv(rs.getString(Activity.COL_USER_NAME));
          String ac   = csv(rs.getString("action"));
          String et   = csv(rs.getString("entity_type"));
          String eid  = toStr(rs.getObject("entity_id"));
          String note = csv(rs.getString("note")).replace('\n',' ').replace('\r',' ');
          String ip   = csv(rs.getString("ip_addr"));
          String ua   = csv(rs.getString("user_agent")).replace('\n',' ').replace('\r',' ');
          out.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
              time, uid, un, ac, et, eid, note, ip, ua);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    out.flush();
  }

  /* ========================= Page wrapper ========================= */
  public static class Page<T> {
    private final List<T> items;
    private final int total;
    private final int page;
    private final int size;

    public Page(List<T> items, int total, int page, int size) {
      this.items = items; this.total = total; this.page = page; this.size = size;
    }
    public List<T> getItems() { return items; }
    public int getTotal() { return total; }
    public int getTotalItems() { return total; }  // alias cho JSP
    public int getPage() { return page; }
    public int getSize() { return size; }
    public int getPageSize() { return size; }     // alias cho JSP
    public int getTotalPages() {
      return size <= 0 ? 1 : (int)Math.max(1, (total + size - 1) / size);
    }
    public boolean isHasNext() { return page < getTotalPages(); }
    public boolean isHasPrev() { return page > 1; }
  }

  /* ========================= Ghi log ========================= */
  public boolean log(Integer userId, String action, String entityType,
                     Integer entityId, String note, String ip, String ua) throws SQLException {
    String sql =
        "INSERT INTO " + TBL + " " +
        "(user_id, action, entity_type, entity_id, note, ip_addr, user_agent, created_at) " +
        "VALUES (?,?,?,?,?,?,?, SYSDATETIME())";

    try (Connection c = DBConnection.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {

      if (userId == null) ps.setNull(1, Types.INTEGER); else ps.setInt(1, userId);
      ps.setString(2, trim(action, 64));
      ps.setString(3, trim(entityType, 64));
      if (entityId == null) ps.setNull(4, Types.INTEGER); else ps.setInt(4, entityId);
      ps.setString(5, trim(note, 2000));
      ps.setString(6, trim(ip, 64));
      ps.setString(7, trim(ua, 255));
      return ps.executeUpdate() > 0;
    }
  }

  public boolean log(HttpServletRequest req, Integer userId, String action,
                     String entityType, Integer entityId, String note) throws SQLException {
    String ip = clientIp(req);
    String ua = userAgent(req);
    String path = req.getRequestURI();
    String finalNote = (note == null || note.trim().isEmpty()) ? path : (note + " | " + path);
    return log(userId, action, entityType, entityId, finalNote, ip, ua);
  }

  public void insert(Activity a) throws SQLException {
    if (a == null) throw new IllegalArgumentException("Activity is null");
    log(a.getUserId(), a.getAction(), a.getEntityType(), a.getEntityId(),
        a.getNote(), a.getIpAddr(), a.getUserAgent());
  }

  /* ========================= Helpers ========================= */
  private static void bind(PreparedStatement ps, List<Object> params) throws SQLException {
    for (int i = 0; i < params.size(); i++) {
      Object v = params.get(i);
      int idx = i + 1;
      if (v == null) {
        ps.setNull(idx, Types.NULL);
      } else if (v instanceof Integer) {
        ps.setInt(idx, (Integer) v);
      } else if (v instanceof String) {
        ps.setString(idx, (String) v);
      } else if (v instanceof Timestamp) {
        ps.setTimestamp(idx, (Timestamp) v);
      } else if (v instanceof Long) {
        ps.setLong(idx, (Long) v);
      } else if (v instanceof Boolean) {
        ps.setBoolean(idx, (Boolean) v);
      } else {
        ps.setObject(idx, v);
      }
    }
  }

  private static String trim(String s, int max) {
    if (s == null) return null;
    return s.length() <= max ? s : s.substring(0, max);
  }

  private static String clientIp(HttpServletRequest r) {
    String xff = r.getHeader("X-Forwarded-For");
    if (xff != null && !xff.trim().isEmpty()) return xff.split(",")[0].trim();
    return r.getRemoteAddr();
  }

  private static String userAgent(HttpServletRequest r) {
    String ua = r.getHeader("User-Agent");
    return ua == null ? null : trim(ua, 255);
  }

  private static String ts(Timestamp t) {
    if (t == null) return "";
    return t.toLocalDateTime().toString().replace('T', ' ');
  }

  private static String toStr(Object o) {
    return o == null ? "" : String.valueOf(o);
  }

  private static String csv(String s) {
    if (s == null) return "";
    String v = s.replace("\"", "\"\"");
    if (v.contains(",") || v.contains("\n") || v.contains("\r")) return "\"" + v + "\"";
    return v;
  }
}
