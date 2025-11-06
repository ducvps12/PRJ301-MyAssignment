package com.acme.leavemgmt.dao;

import com.acme.leavemgmt.model.SupportTicket;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupportTicketDAO {
  private final DataSource ds;
  public static class Page<T>{
    public final List<T> items; public final int page,size; public final int total;
    public Page(List<T> items,int page,int size,int total){this.items=items;this.page=page;this.size=size;this.total=total;}
    public int getTotalPages(){ return (int)Math.ceil(total/(double)size); }
    public List<T> getItems(){return items;} public int getPage(){return page;} public int getSize(){return size;} public int getTotal(){return total;}
  }
  public SupportTicketDAO(){ this.ds = Db.ds(); } // thay bằng cách lấy DataSource của bạn

  public int insert(SupportTicket t) throws SQLException {
    String sql = """
      INSERT INTO dbo.Support_Tickets(user_id,user_name,email,title,body,tech_json,status)
      VALUES(?,?,?,?,?,?, 'OPEN'); SELECT SCOPE_IDENTITY();
    """;
    try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      int i=1;
      if (t.getUserId()==null) ps.setNull(i++, Types.INTEGER); else ps.setInt(i++, t.getUserId());
      ps.setString(i++, t.getUserName());
      ps.setString(i++, t.getEmail());
      ps.setString(i++, t.getTitle());
      ps.setString(i++, t.getBody());
      ps.setString(i++, t.getTechJson());
      try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
    }
  }

  public Page<SupportTicket> search(String status, String q, int page, int size) throws SQLException {
    String where = " WHERE 1=1 ";
    if (status != null && !status.isBlank()) where += " AND status = ? ";
    if (q != null && !q.isBlank()) where += " AND (title LIKE ? OR body LIKE ? OR user_name LIKE ? OR email LIKE ?) ";

    String countSql = "SELECT COUNT(*) FROM dbo.Support_Tickets" + where;
    String dataSql = "SELECT * FROM dbo.Support_Tickets" + where + " ORDER BY created_at DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    try (Connection c = ds.getConnection()) {
      int total;
      try (PreparedStatement ps = c.prepareStatement(countSql)) {
        int i=1;
        if (status != null && !status.isBlank()) ps.setString(i++, status);
        if (q != null && !q.isBlank()) {
          String kw = "%"+q.trim()+"%";
          ps.setString(i++, kw); ps.setString(i++, kw); ps.setString(i++, kw); ps.setString(i++, kw);
        }
        try (ResultSet rs = ps.executeQuery()) { rs.next(); total = rs.getInt(1); }
      }
      List<SupportTicket> items = new ArrayList<>();
      try (PreparedStatement ps = c.prepareStatement(dataSql)) {
        int i=1;
        if (status != null && !status.isBlank()) ps.setString(i++, status);
        if (q != null && !q.isBlank()) {
          String kw = "%"+q.trim()+"%";
          ps.setString(i++, kw); ps.setString(i++, kw); ps.setString(i++, kw); ps.setString(i++, kw);
        }
        ps.setInt(i++, (page-1)*size);
        ps.setInt(i, size);
        try (ResultSet rs = ps.executeQuery()) {
          while (rs.next()) items.add(map(rs));
        }
      }
      return new Page<>(items,page,size,total);
    }
  }

  public SupportTicket find(int id) throws SQLException {
    try (Connection c = ds.getConnection();
         PreparedStatement ps = c.prepareStatement("SELECT * FROM dbo.Support_Tickets WHERE id=?")) {
      ps.setInt(1,id);
      try (ResultSet rs = ps.executeQuery()) { return rs.next()? map(rs):null; }
    }
  }

  public void updateStatus(int id, String status, Integer adminId, String note) throws SQLException {
    String sql = """
      UPDATE dbo.Support_Tickets
      SET status=?, handled_by=?, handled_at = CASE WHEN ? IN ('RESOLVED','CLOSED') THEN SYSDATETIME() ELSE handled_at END,
          note = CASE WHEN ? IS NULL OR LTRIM(RTRIM(?))='' THEN note ELSE ? END
      WHERE id=?""";
    try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, status);
      if (adminId==null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, adminId);
      ps.setString(3, status);
      ps.setString(4, note); ps.setString(5, note); ps.setString(6, note);
      ps.setInt(7, id);
      ps.executeUpdate();
    }
  }

  private static SupportTicket map(ResultSet rs) throws SQLException {
    SupportTicket t = new SupportTicket();
    t.setId(rs.getInt("id"));
    int uid = rs.getInt("user_id"); t.setUserId(rs.wasNull()?null:uid);
    t.setUserName(rs.getString("user_name"));
    t.setEmail(rs.getString("email"));
    t.setTitle(rs.getString("title"));
    t.setBody(rs.getString("body"));
    t.setTechJson(rs.getString("tech_json"));
    t.setStatus(rs.getString("status"));
    Timestamp ct = rs.getTimestamp("created_at"); t.setCreatedAt(ct==null?null:new java.util.Date(ct.getTime()));
    Timestamp ht = rs.getTimestamp("handled_at"); t.setHandledAt(ht==null?null:new java.util.Date(ht.getTime()));
    int hb = rs.getInt("handled_by"); t.setHandledBy(rs.wasNull()?null:hb);
    t.setNote(rs.getString("note"));
    return t;
  }
}
