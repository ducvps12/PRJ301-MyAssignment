package com.acme.leavemgmt.dao;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class NotificationDAO {

    /* ====================== MODEL (bean-friendly for JSP EL) ====================== */
    public static class Row {
        private int id;
        private Integer userId;     // người tạo (admin)
        private String title;
        private String body;
        private String linkUrl;
        private boolean read;
        private LocalDateTime createdAt;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }

        public String getLinkUrl() { return linkUrl; }
        public void setLinkUrl(String linkUrl) { this.linkUrl = linkUrl; }

        public boolean isRead() { return read; }   // boolean -> isXxx()
        public void setRead(boolean read) { this.read = read; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    /* ====================== DAO ====================== */
    private static final String BASE_SELECT =
        "SELECT id, user_id, title, body, link_url, is_read, created_at " +
        "FROM Notifications ";

    private final DataSource ds;
    public NotificationDAO(DataSource ds) { this.ds = ds; }

    private Row mapRow(ResultSet rs) throws SQLException {
        Row r = new Row();
        r.setId(rs.getInt("id"));
        int uid = rs.getInt("user_id");
        r.setUserId(rs.wasNull() ? null : uid);
        r.setTitle(rs.getNString("title"));
        r.setBody(rs.getNString("body"));
        r.setLinkUrl(rs.getString("link_url"));
        r.setRead(rs.getBoolean("is_read"));
        Timestamp ts = rs.getTimestamp("created_at");
        r.setCreatedAt(ts == null ? null : ts.toLocalDateTime());
        return r;
    }

    /** Danh sách toàn bộ (mới nhất trước). */
    public List<Row> listAll() throws SQLException {
        final String sql = BASE_SELECT + "ORDER BY created_at DESC, id DESC";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Row> list = new ArrayList<>();
            while (rs.next()) list.add(mapRow(rs));
            return list;
        }
    }

    /** Phân trang cơ bản (SQL Server 2012+). */
    public List<Row> listPaged(int offset, int limit) throws SQLException {
        final String sql = BASE_SELECT +
            "ORDER BY created_at DESC, id DESC " +
            "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, Math.max(0, offset));
            ps.setInt(2, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                List<Row> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        }
    }

    /** Tạo thông báo mới – yêu cầu userId KHÔNG NULL (đã fix lỗi trước). */
    public void create(int userId, String title, String body, String linkUrl) throws SQLException {
        final String sql =
            "INSERT INTO Notifications (user_id, title, body, link_url, is_read, created_at) " +
            "VALUES (?, ?, ?, ?, 0, SYSUTCDATETIME())";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setNString(2, title);
            ps.setNString(3, body == null ? "" : body);
            ps.setString(4, (linkUrl == null || linkUrl.isBlank()) ? null : linkUrl.trim());
            ps.executeUpdate();
        }
    }

    /** Đánh dấu đã đọc/chưa đọc. */
    public void setRead(int id, boolean read) throws SQLException {
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "UPDATE Notifications SET is_read=? WHERE id=?")) {
            ps.setBoolean(1, read);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    /** Xoá thông báo. */
    public void delete(int id) throws SQLException {
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "DELETE FROM Notifications WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
