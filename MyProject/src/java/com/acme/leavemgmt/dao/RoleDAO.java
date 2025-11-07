package com.acme.leavemgmt.dao;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * RoleDAO – lấy danh sách vai trò, tìm theo id/code.
 * Thiết kế chịu được cả 2 kiểu schema:
 *   A) Roles(id BIGINT PK, code NVARCHAR, name NVARCHAR, is_active BIT, created_at ...)
 *   B) Roles(code NVARCHAR PK, name NVARCHAR, is_active BIT, created_at ...)  // KHÔNG có cột id
 *
 * => DAO chọn "SELECT *" và map có kiểm tra cột để không lỗi khi thiếu "id".
 */
public class RoleDAO {

  private final DataSource ds;

  public RoleDAO(DataSource ds) {
    this.ds = ds;
  }

    public RoleDAO() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

  /** POJO nhỏ gọn nếu dự án bạn chưa có model Role. Nếu đã có model riêng, hãy bỏ class này đi. */
  public static class Role {
    private Long id;          // có thể null nếu bảng không có cột id
    private String code;
    private String name;
    private Boolean active;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
  }

  /* ===================== Queries ===================== */

  /** Danh sách tất cả roles (ORDER BY name, code). */
  public List<Role> listAll() throws SQLException {
    String sql = "SELECT * FROM Roles"; // dùng * để tương thích schema có/không có 'id'
    try (Connection c = ds.getConnection();
         PreparedStatement ps = c.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

      List<Role> out = new ArrayList<>();
      while (rs.next()) out.add(mapRole(rs));
      // sắp xếp phụ nếu DB không có order: name asc rồi code asc
      out.sort((a,b) -> {
        int n = nullSafe(a.getName()).compareToIgnoreCase(nullSafe(b.getName()));
        return n != 0 ? n : nullSafe(a.getCode()).compareToIgnoreCase(nullSafe(b.getCode()));
      });
      return out;
    }
  }

  /** Tìm theo ID (trả null nếu bảng không có cột id hoặc không thấy). */
  public Role findById(Long id) throws SQLException {
    if (id == null) return null;
    // Nếu bảng không có cột id, câu lệnh này sẽ lỗi => fallback sang SELECT * + lọc ở client
    String sql = "SELECT * FROM Roles WHERE id = ?";
    try (Connection c = ds.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setLong(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() ? mapRole(rs) : null;
      }
    } catch (SQLException e) {
      // Fallback: bảng không có cột id
      for (Role r : listAll()) if (id.equals(r.getId())) return r;
      return null;
    }
  }

  /** Tìm theo CODE (ổn định dù có/không có id). */
  public Role findByCode(String code) throws SQLException {
    if (code == null || code.isBlank()) return null;
    String sql = "SELECT * FROM Roles WHERE code = ?";
    try (Connection c = ds.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, code);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() ? mapRole(rs) : null;
      }
    }
  }

  /* ===================== Helpers ===================== */

  private static Role mapRole(ResultSet rs) throws SQLException {
    Role r = new Role();
    // id: chỉ set khi có cột
    if (hasColumn(rs, "id")) {
      Long v = getLongObj(rs, "id");
      r.setId(v);
    }
    r.setCode(getStringSafe(rs, "code"));
    r.setName(getStringSafe(rs, "name"));
    if (hasColumn(rs, "is_active")) r.setActive(getBooleanObj(rs, "is_active"));
    else if (hasColumn(rs, "active")) r.setActive(getBooleanObj(rs, "active"));
    return r;
  }

  /** Kiểm tra cột tồn tại trong ResultSet (không ném lỗi). */
  private static boolean hasColumn(ResultSet rs, String col) {
    try {
      rs.findColumn(col);
      return true;
    } catch (SQLException e) {
      return false;
    }
  }

  private static String getStringSafe(ResultSet rs, String col) throws SQLException {
    return hasColumn(rs, col) ? rs.getString(col) : null;
    }

  private static Long getLongObj(ResultSet rs, String col) throws SQLException {
    if (!hasColumn(rs, col)) return null;
    long v = rs.getLong(col);
    return rs.wasNull() ? null : v;
  }

  private static Boolean getBooleanObj(ResultSet rs, String col) throws SQLException {
    if (!hasColumn(rs, col)) return null;
    boolean v = rs.getBoolean(col);
    return rs.wasNull() ? null : v;
  }

  private static String nullSafe(String s){ return s == null ? "" : s; }
}
