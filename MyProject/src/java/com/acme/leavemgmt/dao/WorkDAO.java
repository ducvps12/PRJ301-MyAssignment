package com.acme.leavemgmt.dao;

import com.acme.leavemgmt.model.WorkReport;

import javax.sql.DataSource;

import java.math.BigDecimal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WorkDAO {

  private final DataSource ds;
  public WorkDAO(DataSource ds){ this.ds = ds; }

  /* ============================ REPORTS ============================ */

  /** Upsert theo (user_id, report_date, type). Lưu summary/blockers/plan_next theo schema hiện tại. */
  public void upsertReport(WorkReport r){
    if (r == null || r.getUserId() == null || r.getWorkDate() == null || isBlank(r.getType())) {
      throw new IllegalArgumentException("Report thiếu userId/report_date/type");
    }

    // Tách 3 phần từ content (nếu content null thì để rỗng)
    String summary  = extract(r.getContent(), "## Summary",  "## Blockers");
    String blockers = extract(r.getContent(), "## Blockers", "## Plan next");
    String planNext = extract(r.getContent(), "## Plan next", null);

    final String UPDATE =
        "UPDATE work_reports " +
        "SET summary=?, blockers=?, plan_next=?, " +
        (hasUpdatedAt() ? "updated_at=? " : "") +
        "WHERE user_id=? AND report_date=? AND type=?";
    final String INSERT =
        "INSERT INTO work_reports(user_id, report_date, type, summary, blockers, plan_next, created_at"
        + (hasUpdatedAt() ? ", updated_at" : "") + ") VALUES(?,?,?,?,?,?," +
        (hasUpdatedAt() ? "?,?" : "?" ) + ")";

    LocalDateTime now = LocalDateTime.now();

    try (Connection c = ds.getConnection()) {
      c.setAutoCommit(false);

      int changed;
      try (PreparedStatement ps = c.prepareStatement(UPDATE)) {
        int i = 1;
        ps.setString(i++, nv(summary));
        ps.setString(i++, nv(blockers));
        ps.setString(i++, nv(planNext));
        if (hasUpdatedAt()) ps.setTimestamp(i++, Timestamp.valueOf(now));
        ps.setLong(i++, r.getUserId());
        ps.setDate(i++, java.sql.Date.valueOf(r.getWorkDate())); // report_date
        ps.setString(i,   r.getType());
        changed = ps.executeUpdate();
      }

      if (changed == 0) {
        try (PreparedStatement ps = c.prepareStatement(INSERT)) {
          int i = 1;
          ps.setLong(i++, r.getUserId());
          ps.setDate(i++, java.sql.Date.valueOf(r.getWorkDate()));
          ps.setString(i++, r.getType());
          ps.setString(i++, nv(summary));
          ps.setString(i++, nv(blockers));
          ps.setString(i++, nv(planNext));
          ps.setTimestamp(i++, Timestamp.valueOf(now)); // created_at
          if (hasUpdatedAt()) ps.setTimestamp(i++, Timestamp.valueOf(now));
          ps.executeUpdate();
        }
      }

      c.commit();
    } catch (SQLException e) {
      throw new RuntimeException("WorkDAO.upsertReport error", e);
    }
  }

  /** Liệt kê báo cáo (map về WorkReport.content đã gộp 3 phần). */
  public List<WorkReport> listReports(Long userId, LocalDate from, LocalDate to, String type){
    StringBuilder sql = new StringBuilder(
        "SELECT id,user_id,report_date,type,summary,blockers,plan_next,created_at" +
        (hasUpdatedAt()? ",updated_at" : "") +
        " FROM work_reports WHERE 1=1");
    List<Object> params = new ArrayList<>();

    if (userId != null) { sql.append(" AND user_id=?"); params.add(userId); }
    if (from   != null) { sql.append(" AND report_date>=?"); params.add(java.sql.Date.valueOf(from)); }
    if (to     != null) { sql.append(" AND report_date<=?"); params.add(java.sql.Date.valueOf(to)); }
    if (!isBlank(type)) { sql.append(" AND type=?"); params.add(type); }
    sql.append(" ORDER BY report_date DESC, id DESC");

    try (Connection c = ds.getConnection();
         PreparedStatement ps = c.prepareStatement(sql.toString())) {
      for (int i=0;i<params.size();i++) ps.setObject(i+1, params.get(i));
      try (ResultSet rs = ps.executeQuery()) {
        List<WorkReport> out = new ArrayList<>();
        while (rs.next()) {
          WorkReport w = new WorkReport();
          w.setId(rs.getLong("id"));
          w.setUserId(rs.getLong("user_id"));
          w.setWorkDate(rs.getDate("report_date").toLocalDate());
          w.setType(rs.getString("type"));

          String content = ("## Summary\n" + nv(rs.getString("summary")) +
                            "\n\n## Blockers\n" + nv(rs.getString("blockers")) +
                            "\n\n## Plan next\n" + nv(rs.getString("plan_next"))).trim();
          w.setContent(content);

          Timestamp cAt = rs.getTimestamp("created_at");
          if (cAt != null) w.setCreatedAt(cAt.toLocalDateTime());
          if (hasUpdatedAt()) {
            Timestamp uAt = rs.getTimestamp("updated_at");
            if (uAt != null) w.setUpdatedAt(uAt.toLocalDateTime());
          }
          // hours/tags không có trong schema hiện tại → để null
          w.setHours((BigDecimal) null);
          w.setTags(null);

          out.add(w);
        }
        return out;
      }
    } catch (SQLException e) {
      throw new RuntimeException("WorkDAO.listReports error", e);
    }
  }

  /* ============================= TODOS ============================= */

  public List<Map<String,Object>> listTodos(String status, Long assigneeId, int page, int size){
    StringBuilder sql = new StringBuilder(
        "SELECT id,title,assignee_id,due_date,priority,status,tags,note,created_at,updated_at " +
        "FROM todos WHERE 1=1");
    List<Object> params = new ArrayList<>();
    if (!isBlank(status))  { sql.append(" AND status=?"); params.add(status); }
    if (assigneeId != null){ sql.append(" AND assignee_id=?"); params.add(assigneeId); }
    sql.append(" ORDER BY (CASE status WHEN 'OPEN' THEN 0 WHEN 'DOING' THEN 1 WHEN 'DONE' THEN 2 ELSE 9 END), due_date ASC, id DESC");

    if (page < 1) page = 1;
    if (size <= 0) size = 20;
    int offset = (page - 1) * size;
    sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
    params.add(offset); params.add(size);

    try (Connection c = ds.getConnection();
         PreparedStatement ps = c.prepareStatement(sql.toString())) {
      for (int i=0;i<params.size();i++) ps.setObject(i+1, params.get(i));
      try (ResultSet rs = ps.executeQuery()) {
        List<Map<String,Object>> out = new ArrayList<>();
        while (rs.next()){
          Map<String,Object> m = new LinkedHashMap<>();
          m.put("id", rs.getLong("id"));
          m.put("title", rs.getString("title"));
          m.put("assignee", rs.getObject("assignee_id"));
          java.sql.Date dd = rs.getDate("due_date");
          m.put("due_date", dd == null ? null : dd.toLocalDate());
          m.put("priority", rs.getString("priority"));
          m.put("status", rs.getString("status"));
          m.put("tags", rs.getString("tags"));
          m.put("note", rs.getString("note"));
          m.put("created_at", toLdt(rs.getTimestamp("created_at")));
          m.put("updated_at", toLdt(rs.getTimestamp("updated_at")));
          out.add(m);
        }
        return out;
      }
    } catch (SQLException e) {
      throw new RuntimeException("WorkDAO.listTodos error", e);
    }
  }

  public long addTodo(String title, Long assigneeId, LocalDate due, String priority, String tags, String note){
    final String sql =
        "INSERT INTO todos(title,assignee_id,due_date,priority,status,tags,note,created_at,updated_at) " +
        "VALUES(?,?,?,?,?,?,?,GETDATE(),GETDATE())";
    try (Connection c = ds.getConnection();
         PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, title);
      if (assigneeId == null) ps.setNull(2, Types.BIGINT); else ps.setLong(2, assigneeId);
      if (due == null) ps.setNull(3, Types.DATE); else ps.setDate(3, java.sql.Date.valueOf(due));
      ps.setString(4, isBlank(priority) ? "NORMAL" : priority);
      ps.setString(5, "OPEN");
      ps.setString(6, nv(tags));
      ps.setString(7, nv(note));
      ps.executeUpdate();
      try (ResultSet k = ps.getGeneratedKeys()){ if (k.next()) return k.getLong(1); }
      return -1;
    } catch (SQLException e) {
      throw new RuntimeException("WorkDAO.addTodo error", e);
    }
  }

  public void setTodoStatus(long id, String status){
    final String sql = "UPDATE todos SET status=?, updated_at=GETDATE() WHERE id=?";
    try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)){
      ps.setString(1, status);
      ps.setLong(2, id);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("WorkDAO.setTodoStatus error", e);
    }
  }

  /* ============================ helpers ============================ */

  private static boolean isBlank(String s){ return s == null || s.isBlank(); }
  private static String nv(String s){ return s == null ? "" : s; }
  private static LocalDateTime toLdt(Timestamp t){ return t == null ? null : t.toLocalDateTime(); }

  /** Đổi về true nếu bảng work_reports có cột updated_at. */
  private static boolean hasUpdatedAt(){ return false; }

  /** Lấy đoạn giữa 2 tiêu đề markdown; nếu next == null → tới hết chuỗi. */
  private static String extract(String content, String start, String next){
    if (content == null) return "";
    int s = content.indexOf(start);
    if (s < 0) return "";
    s += start.length();
    int e = (next == null) ? -1 : content.indexOf(next, s);
    String sub = (e < 0) ? content.substring(s) : content.substring(s, e);
    return sub.trim();
  }
}
