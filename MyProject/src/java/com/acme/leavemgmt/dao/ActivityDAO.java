// com.acme.leavemgmt.dao.ActivityDAO
package com.acme.leavemgmt.dao;

import com.acme.leavemgmt.model.Activity;
import com.acme.leavemgmt.util.DBConnection;
import jakarta.servlet.http.HttpServletRequest;

import java.sql.*;
import java.util.*;

/** DAO cho bảng dbo.User_Activity */
public class ActivityDAO {

  private static final String TBL = "[dbo].[User_Activity]";

    public void insert(Activity a) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

  /* ---------- Kiểu trả về phân trang gọn ---------- */
  public static class Page<T> {
    public final List<T> items;
    public final int total, page, size;
    public Page(List<T> items, int total, int page, int size) {
      this.items = items; this.total = total; this.page = page; this.size = size;
    }
  }

  /* ---------- Ghi log ---------- */

  /** Ghi một activity (userId có thể null) */
  public boolean log(Integer userId, String action, String entityType, Integer entityId,
                     String note, String ip, String ua) throws SQLException {
    String sql = """
      INSERT INTO """ + TBL + """
      (user_id, action, entity_type, entity_id, note, ip_addr, user_agent, created_at)
      VALUES(?,?,?,?,?,?,?, SYSDATETIME())
      """;
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

  /** Overload: lấy IP/UA từ request (có xét X-Forwarded-For) */
  public boolean log(HttpServletRequest req, Integer userId, String action,
                     String entityType, Integer entityId, String note) throws SQLException {
    String ip = clientIp(req);
    String ua = userAgent(req);
    // optional: thêm path vào note cho dễ tra cứu
    String n = (note == null || note.isBlank()) ? req.getRequestURI() : (note + " | " + req.getRequestURI());
    return log(userId, action, entityType, entityId, n, ip, ua);
  }

  /* ---------- Đọc log ---------- */

  /** Danh sách activity theo user; nếu userId=null -> trả tất cả (cho admin) */
  public List<Map<String,Object>> listByUser(Integer userId, int limit, int offset) throws SQLException {
    String sql = """
      SELECT id, user_id, action, entity_type, entity_id, note, ip_addr, user_agent, created_at
      FROM """ + TBL + """
      WHERE (? IS NULL OR user_id = ?)
      ORDER BY created_at DESC
      OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
      """;
    List<Map<String,Object>> out = new ArrayList<>();
    try (Connection c = DBConnection.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
      if (userId == null) { ps.setNull(1, Types.INTEGER); ps.setNull(2, Types.INTEGER); }
      else { ps.setInt(1, userId); ps.setInt(2, userId); }
      ps.setInt(3, Math.max(0, offset));
      ps.setInt(4, Math.max(1, limit));
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) out.add(map(rs));
      }
    }
    return out;
  }

  /** Tổng số activity theo user; userId=null -> đếm tất cả */
  public int countByUser(Integer userId) throws SQLException {
    String sql = "SELECT COUNT(*) FROM " + TBL + " WHERE (? IS NULL OR user_id = ?)";
    try (Connection c = DBConnection.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
      if (userId == null) { ps.setNull(1, Types.INTEGER); ps.setNull(2, Types.INTEGER); }
      else { ps.setInt(1, userId); ps.setInt(2, userId); }
      try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
    }
  }

  /** Gói gọn phân trang: page >=1, size >=1 */
  public Page<Map<String,Object>> pageByUser(Integer userId, int page, int size) throws SQLException {
    int p = Math.max(1, page), s = Math.max(1, size);
    int total = countByUser(userId);
    int offset = (p - 1) * s;
    List<Map<String,Object>> items = listByUser(userId, s, offset);
    return new Page<>(items, total, p, s);
  }

  /* ---------- helpers ---------- */

  private static Map<String,Object> map(ResultSet rs) throws SQLException {
    Map<String,Object> row = new LinkedHashMap<>();
    row.put("id", rs.getInt("id"));
    Object uid = rs.getObject("user_id");
    row.put("userId", uid instanceof Integer ? (Integer) uid : null);
    row.put("action", rs.getString("action"));
    row.put("entityType", rs.getString("entity_type"));
    row.put("entityId", rs.getObject("entity_id"));
    row.put("note", rs.getString("note"));
    row.put("ip", rs.getString("ip_addr"));
    row.put("ua", rs.getString("user_agent"));
    row.put("createdAt", rs.getTimestamp("created_at"));
    return row;
  }

  private static String trim(String s, int max) {
    if (s == null) return null;
    return s.length() <= max ? s : s.substring(0, max);
  }

  private static String clientIp(HttpServletRequest r){
    String xff = r.getHeader("X-Forwarded-For");
    return (xff != null && !xff.isBlank()) ? xff.split(",")[0].trim() : r.getRemoteAddr();
  }
  private static String userAgent(HttpServletRequest r){
    String ua = r.getHeader("User-Agent");
    return ua == null ? null : trim(ua, 255);
  }
}
