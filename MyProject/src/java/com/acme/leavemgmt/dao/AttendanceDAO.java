package com.acme.leavemgmt.dao;

import com.acme.leavemgmt.model.AttendanceRecord;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

/**
 * AttendanceDAO – thao tác chấm công theo ngày/ca.
 *
 * Bảng: dbo.time_entries
 *   id BIGINT IDENTITY PK,
 *   user_id BIGINT NOT NULL,
 *   work_date DATE NOT NULL,
 *   shift_code NVARCHAR(16) NOT NULL, -- MORNING | AFTERNOON | NIGHT
 *   check_in DATETIME2 NULL,
 *   check_out DATETIME2 NULL,
 *   late_minutes INT NOT NULL DEFAULT 0,
 *   ot_minutes   INT NOT NULL DEFAULT 0,
 *   status NVARCHAR(16) NOT NULL DEFAULT 'PRESENT',
 *   notes NVARCHAR(255) NULL,
 *   UNIQUE(user_id, work_date, shift_code)
 */
public class AttendanceDAO {

  private static final String TBL = "dbo.time_entries";
  private final DataSource ds;

  public AttendanceDAO(DataSource ds) { this.ds = ds; }

  /* ========================= Helpers ========================= */

  private AttendanceRecord map(ResultSet rs) throws SQLException {
    AttendanceRecord r = new AttendanceRecord();
    r.setId(rs.getLong("id"));
    r.setUserId(rs.getLong("user_id"));
    r.setWorkDate(rs.getDate("work_date").toLocalDate());
    try { r.setShiftCode(rs.getString("shift_code")); } catch (SQLException ignore) {}

    Timestamp ci = rs.getTimestamp("check_in");
    Timestamp co = rs.getTimestamp("check_out");
    if (ci != null) r.setCheckIn(ci.toLocalDateTime());
    if (co != null) r.setCheckOut(co.toLocalDateTime());

    r.setStatus(rs.getString("status"));
    r.setLateMinutes(getIntNullable(rs, "late_minutes"));
    r.setOtMinutes(getIntNullable(rs, "ot_minutes"));
    r.setNotes(rs.getString("notes"));

    try {
      Timestamp cAt = rs.getTimestamp("created_at");
      Timestamp uAt = rs.getTimestamp("updated_at");
      if (cAt != null) r.setCreatedAt(cAt.toLocalDateTime());
      if (uAt != null) r.setUpdatedAt(uAt.toLocalDateTime());
    } catch (SQLException ignore) {}

    return r;
  }

  private int getIntNullable(ResultSet rs, String col) throws SQLException {
    int v = rs.getInt(col);
    return rs.wasNull() ? 0 : v;
  }

  private void setNullableTimestamp(PreparedStatement ps, int idx, LocalDateTime dt) throws SQLException {
    if (dt == null) ps.setNull(idx, Types.TIMESTAMP);
    else ps.setTimestamp(idx, Timestamp.valueOf(dt));
  }

  private void setNullableInt(PreparedStatement ps, int idx, Integer v) throws SQLException {
    if (v == null) ps.setNull(idx, Types.INTEGER);
    else ps.setInt(idx, v);
  }

  private static String nz(String s){ return s == null ? "" : s; }

  /* =========================== API =========================== */

  /** API cũ (không theo ca) – vẫn giữ để không phá code hiện hành. */
  public List<AttendanceRecord> list(Long userId, LocalDate from, LocalDate to,
                                     String status, int page, int size) {
    return list(userId, from, to, status, null, page, size);
  }

  /** API mới (theo ca). */
  public List<AttendanceRecord> list(Long userId, LocalDate from, LocalDate to,
                                     String status, String shift, int page, int size) {
    StringBuilder sql = new StringBuilder(
        "SELECT id,user_id,work_date,shift_code,status,check_in,check_out,late_minutes,ot_minutes,notes " +
        "FROM " + TBL + " WHERE 1=1");
    List<Object> params = new ArrayList<>();

    if (userId != null) { sql.append(" AND user_id=?"); params.add(userId); }
    if (from   != null) { sql.append(" AND work_date>=?"); params.add(java.sql.Date.valueOf(from)); }
    if (to     != null) { sql.append(" AND work_date<=?"); params.add(java.sql.Date.valueOf(to)); }
    if (status != null && !status.isBlank()) { sql.append(" AND status=?"); params.add(status); }
    if (shift  != null && !shift.isBlank())  { sql.append(" AND shift_code=?"); params.add(shift); }

    sql.append(" ORDER BY work_date DESC, user_id ASC, shift_code ASC");
    if (page < 1) page = 1;
    if (size <= 0) size = 20;
    int offset = (page - 1) * size;

    // SQL Server paging
    sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
    params.add(offset);
    params.add(size);

    try (Connection c = ds.getConnection();
         PreparedStatement ps = c.prepareStatement(sql.toString())) {

      for (int i = 0; i < params.size(); i++) {
        Object p = params.get(i);
        int idx = i + 1;
        if (p instanceof java.sql.Date)      ps.setDate(idx, (java.sql.Date) p);
        else if (p instanceof Integer)       ps.setInt(idx, (Integer) p);
        else if (p instanceof Long)          ps.setLong(idx, (Long) p);
        else if (p instanceof String)        ps.setString(idx, (String) p);
        else                                 ps.setObject(idx, p);
      }

      try (ResultSet rs = ps.executeQuery()) {
        List<AttendanceRecord> out = new ArrayList<>();
        while (rs.next()) out.add(map(rs));
        return out;
      }
    } catch (SQLException e) {
      throw new RuntimeException("AttendanceDAO.list error", e);
    }
  }

  /** API cũ: theo (user, date). Ưu tiên MORNING→AFTERNOON→NIGHT. */
  public AttendanceRecord findByUserDate(Long userId, LocalDate date) {
    final String sql =
        "SELECT TOP 1 * FROM " + TBL + " WHERE user_id=? AND work_date=? " +
        "ORDER BY CASE shift_code WHEN 'MORNING' THEN 1 WHEN 'AFTERNOON' THEN 2 ELSE 3 END";
    try (Connection c = ds.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setLong(1, userId);
      ps.setDate(2, java.sql.Date.valueOf(date));
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() ? map(rs) : null;
      }
    } catch (SQLException e) {
      throw new RuntimeException("AttendanceDAO.findByUserDate error", e);
    }
  }

  /** API mới: theo (user, date, shift). */
  public AttendanceRecord findByUserDateShift(Long userId, LocalDate date, String shift) {
    final String sql = "SELECT * FROM " + TBL + " WHERE user_id=? AND work_date=? AND shift_code=?";
    try (Connection c = ds.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setLong(1, userId);
      ps.setDate(2, java.sql.Date.valueOf(date));
      ps.setString(3, shift);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() ? map(rs) : null;
      }
    } catch (SQLException e) {
      throw new RuntimeException("AttendanceDAO.findByUserDateShift error", e);
    }
  }

  /** UPSERT theo (user_id, work_date, shift_code). */
  public void upsert(AttendanceRecord ar) {
    if (ar == null || ar.getUserId() == null || ar.getWorkDate() == null || ar.getShiftCode() == null)
      throw new IllegalArgumentException("AttendanceRecord thiếu userId/workDate/shiftCode");

    final String SQL_UPDATE =
        "UPDATE " + TBL + " SET status=?, check_in=?, check_out=?, late_minutes=?, ot_minutes=?, notes=? " +
        "WHERE user_id=? AND work_date=? AND shift_code=?";
    final String SQL_INSERT =
        "INSERT INTO " + TBL + " (user_id, work_date, shift_code, status, check_in, check_out, late_minutes, ot_minutes, notes) " +
        "VALUES (?,?,?,?,?,?,?,?,?)";

    try (Connection c = ds.getConnection()) {
      c.setAutoCommit(false);
      int changed;
      try (PreparedStatement ps = c.prepareStatement(SQL_UPDATE)) {
        ps.setString(1, nz(ar.getStatus()));
        setNullableTimestamp(ps, 2, ar.getCheckIn());
        setNullableTimestamp(ps, 3, ar.getCheckOut());
        setNullableInt(ps, 4, ar.getLateMinutes());
        setNullableInt(ps, 5, ar.getOtMinutes());
        if (ar.getNotes() == null) ps.setNull(6, Types.NVARCHAR); else ps.setString(6, ar.getNotes());
        ps.setLong(7, ar.getUserId());
        ps.setDate(8, java.sql.Date.valueOf(ar.getWorkDate()));
        ps.setString(9, ar.getShiftCode());
        changed = ps.executeUpdate();
      }

      if (changed == 0) {
        try (PreparedStatement ps = c.prepareStatement(SQL_INSERT)) {
          ps.setLong(1, ar.getUserId());
          ps.setDate(2, java.sql.Date.valueOf(ar.getWorkDate()));
          ps.setString(3, ar.getShiftCode());
          ps.setString(4, nz(ar.getStatus()));
          setNullableTimestamp(ps, 5, ar.getCheckIn());
          setNullableTimestamp(ps, 6, ar.getCheckOut());
          setNullableInt(ps, 7, ar.getLateMinutes());
          setNullableInt(ps, 8, ar.getOtMinutes());
          if (ar.getNotes() == null) ps.setNull(9, Types.NVARCHAR); else ps.setString(9, ar.getNotes());
          ps.executeUpdate();
        }
      }
      c.commit();
    } catch (SQLException e) {
      throw new RuntimeException("AttendanceDAO.upsert error", e);
    }
  }

  /** Tổng hợp theo tháng cho 1 user. */
  public Map<String, Object> monthSummary(Long userId, int year, int month) {
    LocalDate d1 = LocalDate.of(year, month, 1);
    LocalDate d2 = d1.withDayOfMonth(d1.lengthOfMonth());

    final String sql =
        "SELECT " +
        "  COUNT(*) AS cnt, " +
        "  SUM(CASE WHEN late_minutes>0 THEN 1 ELSE 0 END) AS late_count, " +
        "  COALESCE(SUM(late_minutes),0) AS total_late, " +
        "  COALESCE(SUM(ot_minutes),0)   AS total_ot, " +
        "  SUM(CASE WHEN status='PRESENT' THEN 1 ELSE 0 END) AS present_days, " +
        "  SUM(CASE WHEN status='ABSENT'  THEN 1 ELSE 0 END) AS absent_days " +
        "FROM " + TBL + " WHERE user_id=? AND work_date BETWEEN ? AND ?";

    try (Connection c = ds.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {

      ps.setLong(1, userId);
      ps.setDate(2, java.sql.Date.valueOf(d1));
      ps.setDate(3, java.sql.Date.valueOf(d2));

      try (ResultSet rs = ps.executeQuery()) {
        Map<String,Object> m = new LinkedHashMap<>();
        if (rs.next()) {
          m.put("workdays",     rs.getInt("cnt"));
          m.put("lateCount",    rs.getInt("late_count"));
          m.put("totalLateMin", rs.getInt("total_late"));
          m.put("totalOTMin",   rs.getInt("total_ot"));
          m.put("presentDays",  rs.getInt("present_days"));
          m.put("absentDays",   rs.getInt("absent_days"));
        }
        return m;
      }
    } catch (SQLException e) {
      throw new RuntimeException("AttendanceDAO.monthSummary error", e);
    }
  }

  /** Hook stored-proc nếu cần. */
  public void rebuildTimesheet(Long userId, int y, int m) {
    // try (Connection c = ds.getConnection();
    //      CallableStatement cs = c.prepareCall("{ call dbo.sp_rebuild_timesheet(?,?,?) }")) {
    //   cs.setLong(1, userId); cs.setInt(2, y); cs.setInt(3, m);
    //   cs.execute();
    // }
  }
}
