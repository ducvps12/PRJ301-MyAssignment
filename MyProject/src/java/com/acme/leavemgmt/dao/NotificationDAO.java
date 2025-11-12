package com.acme.leavemgmt.dao;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import javax.sql.DataSource;

public class NotificationDAO {

    public void setRead(int id, boolean read) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public void delete(int id) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    /* ====================== MODEL (Bean cho JSP EL) ====================== */
    public static class Row {
        private int id;
        private Integer userId;     // người nhận
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
    private static final String TBL = "[dbo].[Notifications]";
    private static final String BASE_SELECT =
        "SELECT id, user_id, title, body, link_url, is_read, created_at FROM " + TBL + " ";

    private final DataSource ds;
    public NotificationDAO(DataSource ds) { this.ds = ds; }

    private Row mapRow(ResultSet rs) throws SQLException {
        Row r = new Row();
        r.setId(rs.getInt("id"));
        int uid = rs.getInt("user_id");
        r.setUserId(rs.wasNull() ? null : uid);
        // title/body khai báo NVARCHAR => dùng getNString để giữ unicode chuẩn
        r.setTitle(rs.getNString("title"));
        r.setBody(rs.getNString("body"));
        r.setLinkUrl(rs.getString("link_url"));
        r.setRead(rs.getBoolean("is_read"));
        Timestamp ts = rs.getTimestamp("created_at");
        r.setCreatedAt(ts == null ? null : ts.toLocalDateTime());
        return r;
    }

    /* ====== CRUD cơ bản ====== */

    /** Tạo thông báo mới – yêu cầu userId KHÔNG NULL. */
    public void create(int userId, String title, String body, String linkUrl) throws SQLException {
        final String sql = "INSERT INTO " + TBL +
            " (user_id, title, body, link_url, is_read, created_at) " +
            "VALUES (?, ?, ?, ?, 0, SYSDATETIME())"; // dùng giờ server nội bộ
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setNString(2, safe(title, 255));
            ps.setNString(3, safe(body, 1000));
            if (linkUrl == null || linkUrl.isBlank()) ps.setNull(4, Types.VARCHAR);
            else ps.setString(4, linkUrl.trim());
            ps.executeUpdate();
        }
    }

    /** Xoá thông báo (an toàn theo user; trả về true nếu xoá được). */
    public boolean deleteByUser(int id, int userId) throws SQLException {
        final String sql = "DELETE FROM " + TBL + " WHERE id=? AND user_id=?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    /** Đánh dấu đã đọc/chưa đọc (an toàn theo user). */
    public boolean setReadByUser(int id, int userId, boolean read) throws SQLException {
        final String sql = "UPDATE " + TBL + " SET is_read=? WHERE id=? AND user_id=?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBoolean(1, read);
            ps.setInt(2, id);
            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;
        }
    }

    /** Đánh dấu đã đọc hết cho 1 user. Trả về số bản ghi cập nhật. */
    public int markAllRead(int userId) throws SQLException {
        final String sql = "UPDATE " + TBL + " SET is_read=1 WHERE user_id=? AND is_read=0";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate();
        }
    }

    /* ====== Truy vấn theo user ====== */

    /** Danh sách mới nhất của 1 user (giới hạn limit). */
    public List<Row> listForUser(int userId, int limit) throws SQLException {
        final String sql = BASE_SELECT +
            "WHERE user_id=? " +
            "ORDER BY is_read ASC, created_at DESC, id DESC " +
            "OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY"; // an toàn hơn TOP(?) với JDBC
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                List<Row> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        }
    }

    /** Phân trang cho 1 user (offset/limit). */
    public List<Row> listForUserPaged(int userId, int offset, int limit) throws SQLException {
        final String sql = BASE_SELECT +
            "WHERE user_id=? " +
            "ORDER BY created_at DESC, id DESC " +
            "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, Math.max(0, offset));
            ps.setInt(3, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                List<Row> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        }
    }

    /** Đếm số thông báo chưa đọc của 1 user. */
    public int countUnread(int userId) throws SQLException {
        final String sql = "SELECT COUNT(*) FROM " + TBL + " WHERE user_id=? AND is_read=0";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
        }
    }

    /* ====== Admin view (toàn hệ thống) ====== */

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

    /** Phân trang toàn bộ. */
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

    /* ====== Helpers ====== */
    private static String safe(String s, int max) {
        if (s == null) return "";
        s = s.trim();
        return s.length() > max ? s.substring(0, max) : s;
    }
}
