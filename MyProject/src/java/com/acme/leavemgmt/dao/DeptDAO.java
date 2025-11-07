package com.acme.leavemgmt.dao;

import java.sql.*;
import java.util.*;
import javax.sql.DataSource;

public class DeptDAO {
  private final DataSource ds;
  public DeptDAO(DataSource ds){ this.ds = ds; }

    public DeptDAO() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

  public static class Dept {
    private Long id; private String code; private String name;
    public Long getId(){return id;}   public void setId(Long id){this.id=id;}
    public String getCode(){return code;} public void setCode(String code){this.code=code;}
    public String getName(){return name;} public void setName(String name){this.name=name;}
  }

  /** Lấy danh sách phòng ban. Đổi tên bảng/cột cho khớp schema nếu cần. */
  public List<Dept> listAll() {
    String sql = "SELECT id, code, name FROM Departments ORDER BY name, code";
    try (Connection c = ds.getConnection();
         PreparedStatement ps = c.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
      List<Dept> out = new ArrayList<>();
      while (rs.next()) {
        Dept d = new Dept();
        long id = rs.getLong("id"); d.setId(rs.wasNull()?null:id);
        d.setCode(rs.getString("code"));
        d.setName(rs.getString("name"));
        out.add(d);
      }
      return out;
    } catch (Exception e) {
      return Collections.emptyList();
    }
  }
}
