package com.acme.leavemgmt.dao;

import com.acme.leavemgmt.model.WorkReport;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * WorkDAO – Báo cáo công việc hằng ngày & Việc cần làm (todo)
 *
 * Bảng gợi ý:
 *   work_reports(id PK, user_id, work_date, type, hours, content, tags, created_at, updated_at)
 *   todos(id PK, title, assignee_id, due_date, priority, status, tags, note, created_at, updated_at)
 */
public class WorkDAO {

    private final DataSource ds;

    public WorkDAO(DataSource ds) { this.ds = ds; }

    /* ============================ REPORTS ============================ */

    /** Upsert báo cáo theo UNIQUE(user_id, work_date, type). */
    public void upsertReport(WorkReport r) {
        if (r == null || r.getUserId() == null || r.getWorkDate() == null || isBlank(r.getType())) {
            throw new IllegalArgumentException("Report thiếu userId/workDate/type");
        }

        final String UPDATE =
            "UPDATE work_reports SET hours=?, content=?, tags=?, updated_at=? " +
            "WHERE user_id=? AND work_date=? AND type=?";
        final String INSERT =
            "INSERT INTO work_reports(user_id, work_date, type, hours, content, tags, created_at, updated_at) " +
            "VALUES(?,?,?,?,?,?,?,?)";

        LocalDateTime now = LocalDateTime.now();

        try (Connection c = ds.getConnection()) {
            c.setAutoCommit(false);

            int changed;
            try (PreparedStatement ps = c.prepareStatement(UPDATE)) {
                ps.setBigDecimal(1, toBigDecimal(r.getHours()));                 // <- an toàn kiểu
                ps.setString(2, nullToEmpty(r.getContent()));
                ps.setString(3, nullToEmpty(r.getTags()));
                ps.setTimestamp(4, Timestamp.valueOf(now));
                ps.setLong(5, r.getUserId());
                ps.setDate(6, java.sql.Date.valueOf(r.getWorkDate()));           // <- java.sql.Date
                ps.setString(7, r.getType());
                changed = ps.executeUpdate();
            }

            if (changed == 0) {
                try (PreparedStatement ps = c.prepareStatement(INSERT)) {
                    ps.setLong(1, r.getUserId());
                    ps.setDate(2, java.sql.Date.valueOf(r.getWorkDate()));
                    ps.setString(3, r.getType());
                    ps.setBigDecimal(4, toBigDecimal(r.getHours()));
                    ps.setString(5, nullToEmpty(r.getContent()));
                    ps.setString(6, nullToEmpty(r.getTags()));
                    ps.setTimestamp(7, Timestamp.valueOf(now));
                    ps.setTimestamp(8, Timestamp.valueOf(now));
                    ps.executeUpdate();
                }
            }

            c.commit();
        } catch (SQLException e) {
            throw new RuntimeException("WorkDAO.upsertReport error", e);
        }
    }

    /** Liệt kê báo cáo với các bộ lọc cơ bản. */
    public List<WorkReport> listReports(Long userId, LocalDate from, LocalDate to, String type) {
        StringBuilder sql = new StringBuilder(
            "SELECT id,user_id,work_date,type,hours,content,tags,created_at,updated_at " +
            "FROM work_reports WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (userId != null)                       { sql.append(" AND user_id=?");   params.add(userId); }
        if (from != null)                         { sql.append(" AND work_date>=?"); params.add(java.sql.Date.valueOf(from)); }
        if (to != null)                           { sql.append(" AND work_date<=?"); params.add(java.sql.Date.valueOf(to)); }
        if (!isBlank(type))                       { sql.append(" AND type=?");       params.add(type); }
        sql.append(" ORDER BY work_date DESC, id DESC");

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                List<WorkReport> out = new ArrayList<>();
                while (rs.next()) out.add(mapReport(rs));
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException("WorkDAO.listReports error", e);
        }
    }

    private WorkReport mapReport(ResultSet rs) throws SQLException {
        WorkReport w = new WorkReport();
        w.setId(rs.getLong("id"));
        w.setUserId(rs.getLong("user_id"));
        w.setWorkDate(rs.getDate("work_date").toLocalDate());
        w.setType(rs.getString("type"));

        // DB có thể lưu INT/DECIMAL → ưu tiên getBigDecimal, fallback sang INT nếu null
        BigDecimal h = rs.getBigDecimal("hours");
        if (h == null && rs.getObject("hours") != null) {
            h = BigDecimal.valueOf(rs.getInt("hours"));
        }
        w.setHours(h);

        w.setContent(rs.getString("content"));
        w.setTags(rs.getString("tags"));
        Timestamp cAt = rs.getTimestamp("created_at");
        Timestamp uAt = rs.getTimestamp("updated_at");
        if (cAt != null) w.setCreatedAt(cAt.toLocalDateTime());
        if (uAt != null) w.setUpdatedAt(uAt.toLocalDateTime());
        return w;
    }

    /* ============================= TODOS ============================= */

    /** Danh sách việc cần làm (todo) – lọc trạng thái/assignee + phân trang. */
    public List<Map<String, Object>> listTodos(String status, Long assigneeId, int page, int size) {
        StringBuilder sql = new StringBuilder(
            "SELECT id,title,assignee_id,due_date,priority,status,tags,note,created_at,updated_at " +
            "FROM todos WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (!isBlank(status)) { sql.append(" AND status=?"); params.add(status); }
        if (assigneeId != null){ sql.append(" AND assignee_id=?"); params.add(assigneeId); }

        sql.append(" ORDER BY (CASE status WHEN 'OPEN' THEN 0 WHEN 'DOING' THEN 1 ELSE 2 END), due_date ASC, id DESC");

        if (page < 1) page = 1;
        if (size <= 0) size = 20;
        int offset = (page - 1) * size;
        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(offset); params.add(size);

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> out = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", rs.getLong("id"));
                    m.put("title", rs.getString("title"));
                    m.put("assigneeId", rs.getLong("assignee_id"));
                    java.sql.Date d = rs.getDate("due_date");
                    m.put("dueDate", d == null ? null : d.toLocalDate());
                    m.put("priority", rs.getString("priority"));
                    m.put("status", rs.getString("status"));
                    m.put("tags", rs.getString("tags"));
                    m.put("note", rs.getString("note"));
                    m.put("createdAt", toLdt(rs.getTimestamp("created_at")));
                    m.put("updatedAt", toLdt(rs.getTimestamp("updated_at")));
                    out.add(m);
                }
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException("WorkDAO.listTodos error", e);
        }
    }

    /** Thêm todo mới, trả về id. */
    public long addTodo(String title, Long assigneeId, LocalDate due,
                        String priority, String tags, String note) {
        final String sql =
            "INSERT INTO todos(title,assignee_id,due_date,priority,status,tags,note,created_at,updated_at) " +
            "VALUES(?,?,?,?,?,?,?, ?, ?)";
        LocalDateTime now = LocalDateTime.now();

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, title);
            if (assigneeId == null) ps.setNull(2, Types.BIGINT); else ps.setLong(2, assigneeId);
            if (due == null)       ps.setNull(3, Types.DATE);   else ps.setDate(3, java.sql.Date.valueOf(due));
            ps.setString(4, isBlank(priority) ? "NORMAL" : priority);
            ps.setString(5, "OPEN");
            ps.setString(6, nullToEmpty(tags));
            ps.setString(7, nullToEmpty(note));
            ps.setTimestamp(8, Timestamp.valueOf(now));
            ps.setTimestamp(9, Timestamp.valueOf(now));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
            return -1;
        } catch (SQLException e) {
            throw new RuntimeException("WorkDAO.addTodo error", e);
        }
    }

    /** Cập nhật trạng thái todo (OPEN/DOING/DONE/CANCELLED). */
    public void setTodoStatus(long id, String status) {
        final String sql = "UPDATE todos SET status=?, updated_at=? WHERE id=?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(3, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("WorkDAO.setTodoStatus error", e);
        }
    }

    /* ============================ helpers ============================ */

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }
    private static String nullToEmpty(String s) { return s == null ? "" : s; }

    private static BigDecimal toBigDecimal(Object v) {
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal bd) return bd;
        if (v instanceof Integer i)     return BigDecimal.valueOf(i);
        if (v instanceof Long l)        return BigDecimal.valueOf(l);
        if (v instanceof Double d)      return BigDecimal.valueOf(d);
        if (v instanceof Float f)       return BigDecimal.valueOf(f.doubleValue());
        try { return new BigDecimal(v.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }

    private static LocalDateTime toLdt(Timestamp t) { return t == null ? null : t.toLocalDateTime(); }
}
