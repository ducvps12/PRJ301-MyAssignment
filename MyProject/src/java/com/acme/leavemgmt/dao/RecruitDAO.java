package com.acme.leavemgmt.dao;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import javax.sql.DataSource;

public class RecruitDAO {
    private final DataSource ds;

    // ĐỔI schema ở đây nếu không dùng dbo
    private static final String T_JOBS   = "[dbo].[jobs]";
    private static final String T_APPS   = "[dbo].[applications]";
    private static final String T_EVENTS = "[dbo].[app_events]";

    public RecruitDAO(DataSource ds) { this.ds = ds; }

    /* ========================= JOBS ========================= */

    /** Danh sách job + tìm kiếm + lọc trạng thái + phân trang (SQL Server). */
    public List<Map<String, Object>> jobs(String q, String status, int page, int size) {
        StringBuilder sql = new StringBuilder(
            "SELECT id, title, dept, status, headcount, created_at, updated_at " +
            "FROM " + T_JOBS + " WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (q != null && !q.isBlank()) {
            sql.append(" AND (title LIKE ? OR dept LIKE ?) ");
            String like = "%" + q.trim() + "%";
            params.add(like); params.add(like);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND status = ? ");
            params.add(status.trim());
        }

        sql.append(" ORDER BY created_at DESC ");
        if (page < 1) page = 1;
        if (size <= 0) size = 20;
        int offset = (page - 1) * size;
        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY ");
        params.add(offset); params.add(size);

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> out = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id",        rs.getLong("id"));
                    m.put("title",     rs.getString("title"));
                    m.put("dept",      rs.getString("dept"));
                    m.put("status",    rs.getString("status"));
                    m.put("headcount", rs.getInt("headcount"));
                    m.put("createdAt", ts(rs, "created_at"));
                    m.put("updatedAt", ts(rs, "updated_at"));
                    out.add(m);
                }
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException("RecruitDAO.jobs error", e);
        }
    }

    /** Tạo Job mới, trả về id (dùng OUTPUT INSERTED.id chuẩn SQL Server). */
    public long createJob(String title, String dept, String status,
                          Integer headcount, String description) {
        final String sql =
            "INSERT INTO " + T_JOBS +
            " (title, dept, status, headcount, description, created_at, updated_at) " +
            " OUTPUT INSERTED.id " +
            " VALUES (?,?,?,?,?, SYSDATETIME(), SYSDATETIME())";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, dept);
            ps.setString(3, status == null ? "" : status);
            ps.setInt(4, headcount == null ? 1 : headcount);
            ps.setString(5, description);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
            return -1L;
        } catch (SQLException e) {
            throw new RuntimeException("RecruitDAO.createJob error", e);
        }
    }

    /* ===================== APPLICATIONS ===================== */

    /** List ứng viên theo jobId + stage + phân trang. */
    public List<Map<String, Object>> applications(Long jobId, String stage, int page, int size) {
        if (jobId == null) throw new IllegalArgumentException("jobId is required");

        StringBuilder sql = new StringBuilder(
            "SELECT id, job_id, candidate_name, email, phone, stage, score, note, cv_url, created_at, updated_at " +
            "FROM " + T_APPS + " WHERE job_id = ? ");
        List<Object> params = new ArrayList<>();
        params.add(jobId);

        if (stage != null && !stage.isBlank()) {
            sql.append(" AND stage = ? ");
            params.add(stage.trim());
        }
        sql.append(" ORDER BY created_at DESC ");

        if (page < 1) page = 1;
        if (size <= 0) size = 20;
        int offset = (page - 1) * size;
        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY ");
        params.add(offset); params.add(size);

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> out = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id",       rs.getLong("id"));
                    m.put("jobId",    rs.getLong("job_id"));
                    m.put("name",     rs.getString("candidate_name"));
                    m.put("email",    rs.getString("email"));
                    m.put("phone",    rs.getString("phone"));
                    m.put("stage",    rs.getString("stage"));
                    m.put("score",    rs.getInt("score"));
                    if (rs.wasNull()) m.put("score", null);
                    m.put("note",     rs.getString("note"));
                    m.put("cvUrl",    rs.getString("cv_url"));
                    m.put("createdAt", ts(rs, "created_at"));
                    m.put("updatedAt", ts(rs, "updated_at"));
                    out.add(m);
                }
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException("RecruitDAO.applications error", e);
        }
    }

    /** Move stage + ghi log sự kiện. */
    public void moveStage(Long appId, String nextStage, Integer score, String note, Long actorId) {
        if (appId == null || nextStage == null || nextStage.isBlank())
            throw new IllegalArgumentException("appId/nextStage is required");

        final String GET  = "SELECT stage FROM " + T_APPS + " WHERE id = ?";
        final String UPD  = "UPDATE " + T_APPS + " SET stage=?, score=?, note=?, updated_at=SYSDATETIME() WHERE id=?";
        final String EVIN = "INSERT INTO " + T_EVENTS +
                " (app_id, actor_id, from_stage, to_stage, score, note, created_at) " +
                " VALUES (?,?,?,?,?,?, SYSDATETIME())";

        try (Connection c = ds.getConnection()) {
            c.setAutoCommit(false);

            String fromStage = null;
            try (PreparedStatement ps = c.prepareStatement(GET)) {
                ps.setLong(1, appId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) fromStage = rs.getString(1);
                }
            }
            if (fromStage == null) {
                c.rollback();
                throw new RuntimeException("Application not found id=" + appId);
            }

            try (PreparedStatement ps = c.prepareStatement(UPD)) {
                ps.setString(1, nextStage);
                if (score == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, score);
                ps.setString(3, note);
                ps.setLong(4, appId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = c.prepareStatement(EVIN)) {
                ps.setLong(1, appId);
                if (actorId == null) ps.setNull(2, Types.BIGINT); else ps.setLong(2, actorId);
                ps.setString(3, fromStage);
                ps.setString(4, nextStage);
                if (score == null) ps.setNull(5, Types.INTEGER); else ps.setInt(5, score);
                ps.setString(6, note);
                ps.executeUpdate();
            }

            c.commit();
        } catch (SQLException e) {
            throw new RuntimeException("RecruitDAO.moveStage error", e);
        }
    }

    /* ======================= helpers ======================= */
    private LocalDateTime ts(ResultSet rs, String col) throws SQLException {
        Timestamp t = rs.getTimestamp(col);
        return t == null ? null : t.toLocalDateTime();
    }
}
