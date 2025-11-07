package com.acme.leavemgmt.dao;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * RecruitDAO – tuyển dụng: quản lý Job + Application (pipeline)
 *
 * Giả định bảng:
 *  - jobs(id PK, title, dept, status, headcount, description, created_at, updated_at)
 *  - applications(id PK, job_id FK, candidate_name, email, phone, stage, score, note,
 *                 cv_url, created_at, updated_at)
 *  - app_events(id PK, app_id FK, actor_id, from_stage, to_stage, score, note, created_at)
 *
 * Stage ví dụ: APPLIED -> SCREEN -> INTERVIEW -> OFFER -> HIRED/REJECTED
 */
public class RecruitDAO {

    private final DataSource ds;

    public RecruitDAO(DataSource ds) { this.ds = ds; }

    /* ----------------------------- JOBS ----------------------------- */

    /** Danh sách job + tìm kiếm + lọc trạng thái + phân trang. */
    public List<Map<String, Object>> jobs(String q, String status, int page, int size) {
        StringBuilder sql = new StringBuilder(
            "SELECT id, title, dept, status, headcount, created_at, updated_at " +
            "FROM jobs WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (q != null && !q.isBlank()) {
            sql.append(" AND (title LIKE ? OR dept LIKE ?) ");
            String like = "%" + q.trim() + "%";
            params.add(like); params.add(like);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND status = ? ");
            params.add(status);
        }

        sql.append(" ORDER BY created_at DESC ");
        if (page < 1) page = 1;
        if (size <= 0) size = 20;
        int offset = (page - 1) * size;
        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY "); // SQL Server
        params.add(offset); params.add(size);

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i=0;i<params.size();i++) ps.setObject(i+1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> out = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", rs.getLong("id"));
                    m.put("title", rs.getString("title"));
                    m.put("dept", rs.getString("dept"));
                    m.put("status", rs.getString("status"));
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

    /** Tạo Job mới, trả về id. */
    public long createJob(String title, String dept, String status,
                          Integer headcount, String description) {
        final String sql =
            "INSERT INTO jobs(title, dept, status, headcount, description, created_at, updated_at) " +
            "VALUES(?,?,?,?,?,?,?)";
        LocalDateTime now = LocalDateTime.now();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, title);
            ps.setString(2, dept);
            ps.setString(3, nullToEmpty(status));
            ps.setInt(4, headcount == null ? 1 : headcount);
            ps.setString(5, description);
            ps.setTimestamp(6, Timestamp.valueOf(now));
            ps.setTimestamp(7, Timestamp.valueOf(now));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
            // Fallback nếu DB không trả keys
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery("SELECT SCOPE_IDENTITY()")) {
                if (rs.next()) return rs.getLong(1);
            } catch (SQLException ignore) {}
            return -1;
        } catch (SQLException e) {
            throw new RuntimeException("RecruitDAO.createJob error", e);
        }
    }

    /* -------------------------- APPLICATIONS ------------------------- */

    /**
     * Danh sách ứng tuyển theo jobId (bắt buộc), lọc theo stage, phân trang.
     * Nếu stage = null/"" => bỏ lọc stage.
     */
    public List<Map<String, Object>> applications(Long jobId, String stage, int page, int size) {
        if (jobId == null) throw new IllegalArgumentException("jobId is required");
        StringBuilder sql = new StringBuilder(
            "SELECT id, job_id, candidate_name, email, phone, stage, score, note, cv_url, created_at, updated_at " +
            "FROM applications WHERE job_id=? ");
        List<Object> params = new ArrayList<>();
        params.add(jobId);

        if (stage != null && !stage.isBlank()) {
            sql.append(" AND stage = ? ");
            params.add(stage);
        }
        sql.append(" ORDER BY created_at DESC ");

        if (page < 1) page = 1;
        if (size <= 0) size = 20;
        int offset = (page - 1) * size;
        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY ");
        params.add(offset); params.add(size);

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i=0;i<params.size();i++) ps.setObject(i+1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> out = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", rs.getLong("id"));
                    m.put("jobId", rs.getLong("job_id"));
                    m.put("name", rs.getString("candidate_name"));
                    m.put("email", rs.getString("email"));
                    m.put("phone", rs.getString("phone"));
                    m.put("stage", rs.getString("stage"));
                    m.put("score", rs.getInt("score"));
                    m.put("note", rs.getString("note"));
                    m.put("cvUrl", rs.getString("cv_url"));
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

    /**
     * Di chuyển ứng viên giữa các stage + lưu event (audit).
     * Trả về void, ném lỗi nếu appId không tồn tại.
     */
    public void moveStage(Long appId, String nextStage, Integer score, String note, Long actorId) {
        if (appId == null || nextStage == null || nextStage.isBlank())
            throw new IllegalArgumentException("appId/nextStage is required");
        final String GET  = "SELECT stage FROM applications WHERE id=?";
        final String UPD  = "UPDATE applications SET stage=?, score=?, note=?, updated_at=? WHERE id=?";
        final String EVIN = "INSERT INTO app_events(app_id, actor_id, from_stage, to_stage, score, note, created_at) " +
                            "VALUES(?,?,?,?,?,?,?)";
        LocalDateTime now = LocalDateTime.now();

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
                ps.setTimestamp(4, Timestamp.valueOf(now));
                ps.setLong(5, appId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(EVIN)) {
                ps.setLong(1, appId);
                if (actorId == null) ps.setNull(2, Types.BIGINT); else ps.setLong(2, actorId);
                ps.setString(3, fromStage);
                ps.setString(4, nextStage);
                if (score == null) ps.setNull(5, Types.INTEGER); else ps.setInt(5, score);
                ps.setString(6, note);
                ps.setTimestamp(7, Timestamp.valueOf(now));
                ps.executeUpdate();
            }
            c.commit();
        } catch (SQLException e) {
            throw new RuntimeException("RecruitDAO.moveStage error", e);
        }
    }

    /* ----------------------------- helpers ----------------------------- */

    private LocalDateTime ts(ResultSet rs, String col) throws SQLException {
        Timestamp t = rs.getTimestamp(col);
        return t == null ? null : t.toLocalDateTime();
    }
    private String nullToEmpty(String s){ return s == null ? "" : s; }
}
