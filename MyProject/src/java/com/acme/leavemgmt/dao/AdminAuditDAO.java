package com.acme.leavemgmt.dao.admin;

import com.acme.leavemgmt.model.Activity;
import com.acme.leavemgmt.util.DBConnection;

import java.io.PrintWriter;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO chỉ dùng cho Admin xem toàn bộ Audit Log (JOIN Users để lấy full_name).
 * Không ảnh hưởng tới DAO audit cho người dùng hiện có.
 */
public class AdminAuditDAO {

    /* ===== Page wrapper riêng cho DAO này ===== */
    public static final class Page<T> {
        public final List<T> items;
        public final int page, size;
        public final long total;

        public Page(List<T> items, int page, int size, long total) {
            this.items = items; this.page = page; this.size = size; this.total = total;
        }
        public int totalPages() { return size <= 0 ? 1 : (int) Math.max(1, (total + size - 1) / size); }
    }

    /* ======== SEARCH + phân trang (JOIN Users để lấy họ tên) ======== */
    public Page<Activity> search(Integer userId, String action, String q,
                                 LocalDate from, LocalDate to,
                                 int page, int size) {

        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (userId != null) { where.append(" AND ua.user_id = ? "); params.add(userId); }
        if (action != null && !action.isBlank()) { where.append(" AND ua.action = ? "); params.add(action.trim()); }
        if (from != null) { where.append(" AND ua.created_at >= ? "); params.add(Timestamp.valueOf(from.atStartOfDay())); }
        if (to   != null) { where.append(" AND ua.created_at <  ? "); params.add(Timestamp.valueOf(to.plusDays(1).atStartOfDay())); }
        if (q != null && !q.isBlank()) {
            where.append("""
                AND (
                    ua.note LIKE ? OR ua.user_agent LIKE ? OR ua.ip_addr LIKE ?
                    OR ua.action LIKE ? OR ua.entity_type LIKE ? OR CAST(ua.entity_id AS nvarchar) LIKE ?
                )
            """);
            String like = "%" + q.trim() + "%";
            params.add(like); params.add(like); params.add(like);
            params.add(like); params.add(like); params.add(like);
        }

        String select = """
            SELECT ua.id, ua.user_id, u.full_name, ua.action, ua.entity_type, ua.entity_id,
                   ua.note, ua.ip_addr, ua.user_agent, ua.created_at
            FROM dbo.User_Activity ua
            JOIN dbo.Users u ON u.id = ua.user_id
        """;

        String countSql = "SELECT COUNT(1) FROM dbo.User_Activity ua JOIN dbo.Users u ON u.id = ua.user_id " + where;
        String dataSql  = select + where + " ORDER BY ua.created_at DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        List<Activity> items = new ArrayList<>();
        long total = 0;

        try (Connection cn = DBConnection.getConnection()) {
            // count
            try (PreparedStatement st = cn.prepareStatement(countSql)) {
                for (int i = 0; i < params.size(); i++) st.setObject(i + 1, params.get(i));
                try (ResultSet rs = st.executeQuery()) { if (rs.next()) total = rs.getLong(1); }
            }
            // page data
            try (PreparedStatement st = cn.prepareStatement(dataSql)) {
                int idx = 1;
                for (Object p : params) st.setObject(idx++, p);
                st.setInt(idx++, (page - 1) * size);
                st.setInt(idx, size);

                try (ResultSet rs = st.executeQuery()) {
                    while (rs.next()) {
                        Activity a = new Activity();
                        a.setId(rs.getInt("id"));
                        a.setUserId(rs.getInt("user_id"));
                        a.setUserName(rs.getString("full_name"));
                        a.setAction(rs.getString("action"));
                        a.setEntityType(rs.getString("entity_type"));
                        a.setEntityId((Integer) rs.getObject("entity_id"));
                        a.setNote(rs.getString("note"));
                        a.setIpAddr(rs.getString("ip_addr"));
                        a.setUserAgent(rs.getString("user_agent"));
                        a.setCreatedAt(rs.getTimestamp("created_at"));
                        items.add(a);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("AdminAuditDAO.search failed: " + e.getMessage(), e);
        }
        return new Page<>(items, page, size, total);
    }

    /* ========= EXPORT CSV theo bộ lọc hiện tại (không phân trang) ========= */
    public void exportCsv(Integer userId, String action, String q,
                          LocalDate from, LocalDate to, PrintWriter out) {
        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (userId != null) { where.append(" AND ua.user_id = ? "); params.add(userId); }
        if (action != null && !action.isBlank()) { where.append(" AND ua.action = ? "); params.add(action.trim()); }
        if (from != null) { where.append(" AND ua.created_at >= ? "); params.add(Timestamp.valueOf(from.atStartOfDay())); }
        if (to   != null) { where.append(" AND ua.created_at <  ? "); params.add(Timestamp.valueOf(to.plusDays(1).atStartOfDay())); }
        if (q != null && !q.isBlank()) {
            where.append("""
                AND (
                    ua.note LIKE ? OR ua.user_agent LIKE ? OR ua.ip_addr LIKE ?
                    OR ua.action LIKE ? OR ua.entity_type LIKE ? OR CAST(ua.entity_id AS nvarchar) LIKE ?
                )
            """);
            String like = "%" + q.trim() + "%";
            params.add(like); params.add(like); params.add(like);
            params.add(like); params.add(like); params.add(like);
        }

        String sql = """
            SELECT ua.created_at, ua.user_id, u.full_name, ua.action, ua.entity_type, ua.entity_id,
                   ua.note, ua.ip_addr, ua.user_agent
            FROM dbo.User_Activity ua
            JOIN dbo.Users u ON u.id = ua.user_id
        """ + where + " ORDER BY ua.created_at DESC";

        out.println("created_at,user_id,full_name,action,entity_type,entity_id,note,ip_addr,user_agent");

        try (Connection cn = DBConnection.getConnection();
             PreparedStatement st = cn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) st.setObject(i + 1, params.get(i));
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    out.print(csv(rs.getTimestamp(1))); out.print(',');
                    out.print(csv(rs.getInt(2)));        out.print(',');
                    out.print(csv(rs.getString(3)));     out.print(',');
                    out.print(csv(rs.getString(4)));     out.print(',');
                    out.print(csv(rs.getString(5)));     out.print(',');
                    out.print(csv(rs.getObject(6)));     out.print(',');
                    out.print(csv(rs.getString(7)));     out.print(',');
                    out.print(csv(rs.getString(8)));     out.print(',');
                    out.println(csv(rs.getString(9)));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("AdminAuditDAO.exportCsv failed: " + e.getMessage(), e);
        }
    }

    /* ========= Tiện ích: danh sách action distinct (fill dropdown) ========= */
    public List<String> listActions() {
        String sql = "SELECT DISTINCT ua.action FROM dbo.User_Activity ua ORDER BY ua.action";
        List<String> ls = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement st = cn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) ls.add(rs.getString(1));
        } catch (Exception e) {
            throw new RuntimeException("AdminAuditDAO.listActions failed: " + e.getMessage(), e);
        }
        return ls;
    }

    private static String csv(Object v) {
        if (v == null) return "";
        String s = String.valueOf(v).replace("\"", "\"\"");
        if (s.contains(",") || s.contains("\n") || s.contains("\r") || s.startsWith(" ") || s.endsWith(" "))
            return "\"" + s + "\"";
        return s;
    }
}
