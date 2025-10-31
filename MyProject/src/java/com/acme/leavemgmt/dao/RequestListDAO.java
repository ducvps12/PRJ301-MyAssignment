package com.acme.leavemgmt.dao;

import com.acme.leavemgmt.model.Request;
import com.acme.leavemgmt.util.DBConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RequestListDAO {

    /* ============== COUNT ================= */
    public int countByFilter(int meId,
                             String myDept,
                             String myRole,
                             LocalDate from,
                             LocalDate to,
                             String status,
                             String mine,
                             String q) throws SQLException {

        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(*)
            FROM dbo.Requests r
            LEFT JOIN dbo.Users u ON u.id = r.created_by
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        applyScope(sql, params, meId, myDept, myRole, mine);

        if (from != null) {
            sql.append(" AND r.start_date >= ? ");
            params.add(Date.valueOf(from));
        }
        if (to != null) {
            sql.append(" AND r.end_date <= ? ");
            params.add(Date.valueOf(to));
        }

        if (status != null && !status.isBlank()) {
            sql.append(" AND UPPER(r.status) = ? ");
            params.add(status.toUpperCase());
        }

        if (q != null && !q.isBlank()) {
            sql.append(" AND (r.reason LIKE ? OR u.full_name LIKE ?) ");
            params.add("%" + q + "%");
            params.add("%" + q + "%");
        }

        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /* ============== LIST ================= */
    public List<Request> findByFilter(int meId,
                                      String myDept,
                                      String myRole,
                                      LocalDate from,
                                      LocalDate to,
                                      String status,
                                      String mine,
                                      String q,
                                      String sort,
                                      int limit,
                                      int offset) throws SQLException {

        StringBuilder sql = new StringBuilder("""
            SELECT r.id,
                   r.reason,
                   r.status,
                   r.start_date,
                   r.end_date,
                   r.created_at,
                   r.created_by,
                   r.processed_by,
                   u.full_name  AS created_by_name,
                   up.full_name AS processed_by_name
            FROM dbo.Requests r
            LEFT JOIN dbo.Users u  ON u.id = r.created_by
            LEFT JOIN dbo.Users up ON up.id = r.processed_by
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        applyScope(sql, params, meId, myDept, myRole, mine);

        if (from != null) {
            sql.append(" AND r.start_date >= ? ");
            params.add(Date.valueOf(from));
        }
        if (to != null) {
            sql.append(" AND r.end_date <= ? ");
            params.add(Date.valueOf(to));
        }

        if (status != null && !status.isBlank()) {
            sql.append(" AND UPPER(r.status) = ? ");
            params.add(status.toUpperCase());
        }

        if (q != null && !q.isBlank()) {
            sql.append(" AND (r.reason LIKE ? OR u.full_name LIKE ?) ");
            params.add("%" + q + "%");
            params.add("%" + q + "%");
        }

        // sort an toàn
        String orderBy = switch (sort == null ? "" : sort) {
            case "created_asc"  -> " ORDER BY r.created_at ASC ";
            case "from_asc"     -> " ORDER BY r.start_date ASC, r.id DESC ";
            case "from_desc"    -> " ORDER BY r.start_date DESC, r.id DESC ";
            case "created_desc" -> " ORDER BY r.created_at DESC ";
            default             -> " ORDER BY r.created_at DESC ";
        };
        sql.append(orderBy);

        // paging
        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY ");
        params.add(offset);
        params.add(limit);

        List<Request> list = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
              while (rs.next()) {
    Request r = new Request();

    r.setId(rs.getInt("id"));
    r.setReason(rs.getString("reason"));
    r.setStatus(rs.getString("status"));

    // start_date
    java.sql.Date sd = rs.getDate("start_date");
    if (sd != null) {
        r.setStartDate(sd.toLocalDate());
    }

    // end_date
    java.sql.Date ed = rs.getDate("end_date");
    if (ed != null) {
        r.setEndDate(ed.toLocalDate());
    }

    // created_at (nếu model bạn để LocalDateTime hoặc Date)
    java.sql.Timestamp ts = rs.getTimestamp("created_at");
    if (ts != null) {
        r.setCreatedAt(ts.toLocalDateTime()); // hoặc r.setCreatedAt(ts);
    }

    r.setCreatedBy(rs.getInt("created_by"));

    Object pb = rs.getObject("processed_by");
    if (pb != null) {
        r.setProcessedBy(rs.getInt("processed_by"));
    }

    r.setCreatedByName(rs.getString("created_by_name"));
    r.setProcessedByName(rs.getString("processed_by_name"));

    list.add(r);
}

            }
        }

        return list;
    }

    /* ============== PRIVATE: scope ============= */
    private void applyScope(StringBuilder sql,
                            List<Object> params,
                            int meId,
                            String myDept,
                            String myRole,
                            String mine) {

        // bấm "Của tôi"
        if ("1".equals(mine)) {
            sql.append(" AND r.created_by = ? ");
            params.add(meId);
            return;
        }

        // bấm "team" và có quyền
        if ("team".equalsIgnoreCase(mine) &&
                ("MANAGER".equalsIgnoreCase(myRole) || "DIV_LEADER".equalsIgnoreCase(myRole))) {
            sql.append(" AND u.department = ? ");
            params.add(myDept);
            return;
        }

        // không chọn gì
        if (!"ADMIN".equalsIgnoreCase(myRole)
                && !"MANAGER".equalsIgnoreCase(myRole)
                && !"DIV_LEADER".equalsIgnoreCase(myRole)) {
            // nhân viên thường chỉ xem của mình
            sql.append(" AND r.created_by = ? ");
            params.add(meId);
        }
        // quản lý/admin -> để trống
    }
}
