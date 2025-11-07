package com.acme.leavemgmt.dao;

import com.acme.leavemgmt.model.AttendanceRecord;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AttendanceDAO – thao tác chấm công
 *
 * Bảng giả định: attendance_records (UNIQUE(user_id, work_date))
 * Nếu dùng SQL Server có thể thay UPSERT bằng MERGE.
 */
public class AttendanceDAO {
    private final DataSource ds;

    public AttendanceDAO(DataSource ds) { this.ds = ds; }

    /* ========================= Helpers ========================= */

    private AttendanceRecord map(ResultSet rs) throws SQLException {
        AttendanceRecord r = new AttendanceRecord();
        r.setId(rs.getLong("id"));
        r.setUserId(rs.getLong("user_id"));
        r.setWorkDate(rs.getDate("work_date").toLocalDate());

        Timestamp ci = rs.getTimestamp("check_in");
        Timestamp co = rs.getTimestamp("check_out");
        if (ci != null) r.setCheckIn(ci.toLocalDateTime());
        if (co != null) r.setCheckOut(co.toLocalDateTime());

        r.setStatus(rs.getString("status"));
        r.setLateMinutes(safeInt(rs, "late_minutes"));
        r.setOtMinutes(safeInt(rs, "ot_minutes"));
        r.setNotes(rs.getString("notes"));

        Timestamp cAt = rs.getTimestamp("created_at");
        Timestamp uAt = rs.getTimestamp("updated_at");
        if (cAt != null) r.setCreatedAt(cAt.toLocalDateTime());
        if (uAt != null) r.setUpdatedAt(uAt.toLocalDateTime());
        return r;
    }

    private int safeInt(ResultSet rs, String col) throws SQLException {
        int v = rs.getInt(col);
        return rs.wasNull() ? 0 : v;
    }

    private void setNullableTimestamp(PreparedStatement ps, int idx, LocalDateTime dt) throws SQLException {
        if (dt == null) ps.setNull(idx, Types.TIMESTAMP);
        else ps.setTimestamp(idx, Timestamp.valueOf(dt));
    }

    private String nullToEmpty(String s){ return (s==null) ? "" : s; }

    /* =========================== API =========================== */

    /**
     * Danh sách chấm công có lọc + phân trang.
     * @param page 1-based
     */
    public List<AttendanceRecord> list(Long userId, LocalDate from, LocalDate to, String status, int page, int size) {
        StringBuilder sql = new StringBuilder(
            "SELECT id,user_id,work_date,status,check_in,check_out,late_minutes,ot_minutes,notes,created_at,updated_at " +
            "FROM attendance_records WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (userId != null) { sql.append(" AND user_id=?"); params.add(userId); }
        if (from   != null) { sql.append(" AND work_date>=?"); params.add(java.sql.Date.valueOf(from)); }
        if (to     != null) { sql.append(" AND work_date<=?"); params.add(java.sql.Date.valueOf(to)); }
        if (status != null && !status.isBlank()) { sql.append(" AND status=?"); params.add(status); }

        sql.append(" ORDER BY work_date DESC, user_id ASC");
        if (page < 1) page = 1;
        if (size <= 0) size = 20;
        int offset = (page - 1) * size;

        // SQL Server
        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(offset);
        params.add(size);

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof java.sql.Date)     ps.setDate(i + 1, (java.sql.Date) p);
                else if (p instanceof Integer)      ps.setInt(i + 1, (Integer) p);
                else if (p instanceof Long)         ps.setLong(i + 1, (Long) p);
                else                                ps.setObject(i + 1, p);
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

    /** Lấy bản ghi theo (user, date). */
    public AttendanceRecord findByUserDate(Long userId, LocalDate date) {
        final String sql =
            "SELECT id,user_id,work_date,status,check_in,check_out,late_minutes,ot_minutes,notes,created_at,updated_at " +
            "FROM attendance_records WHERE user_id=? AND work_date=?";
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

    /** UPSERT theo (user_id, work_date). */
    public void upsert(AttendanceRecord ar) {
        if (ar == null || ar.getUserId() == null || ar.getWorkDate() == null)
            throw new IllegalArgumentException("AttendanceRecord thiếu userId/workDate");

        final String SQL_UPDATE =
            "UPDATE attendance_records SET status=?, check_in=?, check_out=?, late_minutes=?, ot_minutes=?, notes=?, updated_at=? " +
            "WHERE user_id=? AND work_date=?";
        final String SQL_INSERT =
            "INSERT INTO attendance_records (user_id, work_date, status, check_in, check_out, late_minutes, ot_minutes, notes, created_at, updated_at) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?)";

        LocalDateTime now = LocalDateTime.now();

        try (Connection c = ds.getConnection()) {
            c.setAutoCommit(false);
            int changed;
            try (PreparedStatement ps = c.prepareStatement(SQL_UPDATE)) {
                ps.setString(1,  nullToEmpty(ar.getStatus()));
                ps.setString(1, nullToEmpty(ar.getStatus()));
setNullableTimestamp(ps, 2, ar.getCheckIn());
setNullableTimestamp(ps, 3, ar.getCheckOut());
ps.setInt(4, ar.getLateMinutes());   // <-- không check null
ps.setInt(5, ar.getOtMinutes());     // <-- không check null
ps.setString(6, ar.getNotes());
ps.setTimestamp(7, Timestamp.valueOf(now));
ps.setLong(8, ar.getUserId());
ps.setDate(9, java.sql.Date.valueOf(ar.getWorkDate()));

                changed = ps.executeUpdate();
            }

            if (changed == 0) {
                try (PreparedStatement ps = c.prepareStatement(SQL_INSERT)) {
                  ps.setLong(1, ar.getUserId());
ps.setDate(2, java.sql.Date.valueOf(ar.getWorkDate()));
ps.setString(3, nullToEmpty(ar.getStatus()));
setNullableTimestamp(ps, 4, ar.getCheckIn());
setNullableTimestamp(ps, 5, ar.getCheckOut());
ps.setInt(6, ar.getLateMinutes());   // <-- primitive int
ps.setInt(7, ar.getOtMinutes());     // <-- primitive int
ps.setString(8, ar.getNotes());
ps.setTimestamp(9, Timestamp.valueOf(now));
ps.setTimestamp(10, Timestamp.valueOf(now));

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
            "FROM attendance_records WHERE user_id=? AND work_date BETWEEN ? AND ?";

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

    /** Hook rebuild timesheet – bật khi có stored-proc riêng. */
    public void rebuildTimesheet(Long userId, int y, int m) {
        // Ví dụ:
        // try (Connection c = ds.getConnection();
        //      CallableStatement cs = c.prepareCall("{ call sp_rebuild_timesheet(?,?,?) }")) {
        //   cs.setLong(1, userId); cs.setInt(2, y); cs.setInt(3, m);
        //   cs.execute();
        // }
    }
}
