// com.acme.leavemgmt.dao.ActivityDAO
package com.acme.leavemgmt.dao;

import com.acme.leavemgmt.util.DBConnection;

import java.sql.*;
import java.util.*;

public class ActivityDAO {

  /** Ghi một activity */
  public void log(int userId, String action, String entityType, Integer entityId,
                  String note, String ip, String ua) throws SQLException {
    String sql = """
      INSERT INTO User_Activity(user_id, action, entity_type, entity_id, note, ip_addr, user_agent)
      VALUES(?,?,?,?,?,?,?)
      """;
    try (Connection c = DBConnection.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setInt(1, userId);
      ps.setString(2, n(action, 50));
      ps.setString(3, n(entityType, 50));
      if (entityId == null) ps.setNull(4, Types.INTEGER); else ps.setInt(4, entityId);
      ps.setString(5, n(note, 500));
      ps.setString(6, n(ip, 64));
      ps.setString(7, n(ua, 255));
      ps.executeUpdate();
    }
  }

  /** Lấy list activity gần đây của 1 user (phục vụ trang /activity) */
  public List<Map<String,Object>> listByUser(int userId, int limit, int offset) throws SQLException {
    String sql = """
      SELECT id, action, entity_type, entity_id, note, ip_addr, user_agent, created_at
      FROM User_Activity
      WHERE user_id = ?
      ORDER BY created_at DESC
      OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
      """;
    List<Map<String,Object>> out = new ArrayList<>();
    try (Connection c = DBConnection.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setInt(1, userId);
      ps.setInt(2, Math.max(0, offset));
      ps.setInt(3, Math.max(1, limit));
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          Map<String,Object> row = new LinkedHashMap<>();
          row.put("id", rs.getInt("id"));
          row.put("action", rs.getString("action"));
          row.put("entityType", rs.getString("entity_type"));
          row.put("entityId", rs.getObject("entity_id"));
          row.put("note", rs.getString("note"));
          row.put("ip", rs.getString("ip_addr"));
          row.put("ua", rs.getString("user_agent"));
          row.put("createdAt", rs.getTimestamp("created_at"));
          out.add(row);
        }
      }
    }
    return out;
  }

  /** Tổng số activity của user (để phân trang) */
  public int countByUser(int userId) throws SQLException {
    String sql = "SELECT COUNT(*) FROM User_Activity WHERE user_id = ?";
    try (Connection c = DBConnection.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setInt(1, userId);
      try (ResultSet rs = ps.executeQuery()) {
        rs.next(); return rs.getInt(1);
      }
    }
  }

  // ===== helpers =====
  private static String n(String s, int max) {
    if (s == null) return null;
    if (s.length() <= max) return s;
    return s.substring(0, max);
  }
}
