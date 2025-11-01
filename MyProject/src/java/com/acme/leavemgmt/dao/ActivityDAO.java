// com.acme.leavemgmt.dao.ActivityDAO
package com.acme.leavemgmt.dao;

import com.acme.leavemgmt.model.Activity;
import com.acme.leavemgmt.util.DBConnection;
import jakarta.servlet.http.HttpServletRequest;

import java.sql.*;
import java.util.*;

/** DAO cho bảng dbo.User_Activity */
public class ActivityDAO {

    // bảng trong DB
    private static final String TBL = "[dbo].[User_Activity]";

    /* =========================================================
       1. Kiểu trả về phân trang cho JSP
       ========================================================= */
    public static class Page<T> {
        // bạn đang forward attribute tên "pg" -> JSP gọi ${pg.xxx}
        private final List<T> items;
        private final int total;
        private final int page;
        private final int size;

        public Page(List<T> items, int total, int page, int size) {
            this.items = items;
            this.total = total;
            this.page = page;
            this.size = size;
        }

        // ====== các getter để EL đọc được ======
        public List<T> getItems() {
            return items;
        }

        public int getTotal() {
            return total;
        }

        public int getPage() {
            return page;
        }

        public int getSize() {
            return size;
        }

        // tổng số trang
        public int getTotalPages() {
            if (size <= 0) return 1;
            return (int) Math.ceil((double) total / (double) size);
        }

        public boolean isHasNext() {
            return page < getTotalPages();
        }

        public boolean isHasPrev() {
            return page > 1;
        }
    }

    /* =========================================================
       2. Ghi log
       ========================================================= */

    /**
     * Ghi một activity (userId có thể null).
     */
    public boolean log(Integer userId,
                       String action,
                       String entityType,
                       Integer entityId,
                       String note,
                       String ip,
                       String ua) throws SQLException {

        String sql = """
            INSERT INTO """ + TBL + """
            (user_id, action, entity_type, entity_id, note, ip_addr, user_agent, created_at)
            VALUES (?,?,?,?,?,?,?, SYSDATETIME())
            """;

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            // user_id
            if (userId == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, userId);
            }

            ps.setString(2, trim(action, 64));
            ps.setString(3, trim(entityType, 64));

            if (entityId == null) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, entityId);
            }

            ps.setString(5, trim(note, 2000));
            ps.setString(6, trim(ip, 64));
            ps.setString(7, trim(ua, 255));

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Overload: tự lấy IP & UA từ request.
     */
    public boolean log(HttpServletRequest req,
                       Integer userId,
                       String action,
                       String entityType,
                       Integer entityId,
                       String note) throws SQLException {
        String ip = clientIp(req);
        String ua = userAgent(req);
        String path = req.getRequestURI();
        String finalNote;
        if (note == null || note.isBlank()) {
            finalNote = path;
        } else {
            finalNote = note + " | " + path;
        }
        return log(userId, action, entityType, entityId, finalNote, ip, ua);
    }

    /**
     * Hoàn thiện luôn hàm insert(Activity a) cho NetBeans khỏi báo.
     * Nếu model Activity của bạn khác tên field thì chỉnh lại 5 dòng set* ở dưới.
     */
    public void insert(Activity a) throws SQLException {
        if (a == null) {
            throw new IllegalArgumentException("Activity is null");
        }
        // cố gắng map theo tên thuộc tính phổ biến
        log(
                a.getUserId(),               // Integer
                a.getAction(),               // String
                a.getEntityType(),           // String
                a.getEntityId(),             // Integer
                a.getNote(),                 // String
                a.getIpAddr(),               // String
                a.getUserAgent()             // String
        );
    }

    /* =========================================================
       3. Đọc log / phân trang
       ========================================================= */

    /**
     * Lấy danh sách activity theo user; nếu userId = null -> lấy tất cả (cho admin).
     */
    public List<Map<String, Object>> listByUser(Integer userId, int limit, int offset) throws SQLException {
        String sql = """
            SELECT id, user_id, action, entity_type, entity_id, note,
                   ip_addr, user_agent, created_at
            FROM """ + TBL + """
            WHERE (? IS NULL OR user_id = ?)
            ORDER BY created_at DESC
            OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
            """;

        List<Map<String, Object>> out = new ArrayList<>();

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            if (userId == null) {
                ps.setNull(1, Types.INTEGER);
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(1, userId);
                ps.setInt(2, userId);
            }

            ps.setInt(3, Math.max(0, offset));
            ps.setInt(4, Math.max(1, limit));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(map(rs));
                }
            }
        }

        return out;
    }

    /**
     * Đếm tổng số activity theo user; userId = null -> đếm tất cả.
     */
    public int countByUser(Integer userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + TBL + " WHERE (? IS NULL OR user_id = ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            if (userId == null) {
                ps.setNull(1, Types.INTEGER);
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(1, userId);
                ps.setInt(2, userId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /**
     * Gói gọn phân trang: page >= 1, size >= 1
     */
    public Page<Map<String, Object>> pageByUser(Integer userId, int page, int size) throws SQLException {
        int p = Math.max(1, page);
        int s = Math.max(1, size);
        int total = countByUser(userId);
        int offset = (p - 1) * s;
        List<Map<String, Object>> items = listByUser(userId, s, offset);
        return new Page<>(items, total, p, s);
    }

    /* =========================================================
       4. Helpers
       ========================================================= */

    private static Map<String, Object> map(ResultSet rs) throws SQLException {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", rs.getInt("id"));
        Object uid = rs.getObject("user_id");
        row.put("userId", (uid instanceof Integer) ? (Integer) uid : null);
        row.put("action", rs.getString("action"));
        row.put("entityType", rs.getString("entity_type"));
        row.put("entityId", rs.getObject("entity_id"));
        row.put("note", rs.getString("note"));
        row.put("ip", rs.getString("ip_addr"));
        row.put("ua", rs.getString("user_agent"));
        row.put("createdAt", rs.getTimestamp("created_at"));
        return row;
    }

    private static String trim(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }

    private static String clientIp(HttpServletRequest r) {
        String xff = r.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return r.getRemoteAddr();
    }

    private static String userAgent(HttpServletRequest r) {
        String ua = r.getHeader("User-Agent");
        return ua == null ? null : trim(ua, 255);
    }
}
