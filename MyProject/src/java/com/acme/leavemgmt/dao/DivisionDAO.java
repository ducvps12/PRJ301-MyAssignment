package com.acme.leavemgmt.dao;

import com.acme.leavemgmt.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;

public class DivisionDAO {

    /* ========== State ========== */
    private final DataSource ds; // có thể null -> fallback DBConnection

    /* ========== Ctors ========== */
    public DivisionDAO(DataSource ds) { this.ds = ds; }
    public DivisionDAO() { this.ds = null; }

    /* ========== Connection helper ========== */
    private Connection getConn() throws SQLException {
        return (ds != null) ? ds.getConnection() : DBConnection.getConnection();
    }

    /* ========== DTO ========== */
    public static class Division {
        private Integer id;
        private String code;
        private String name;
        private Boolean isActive;
        private Timestamp createdAt;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
        public Timestamp getCreatedAt() { return createdAt; }
        public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

        /** Cho UI/JSP cũ set theo chuỗi trạng thái */
        public void setStatus(String s) {
            if (s == null) { this.isActive = null; return; }
            String v = s.trim().toUpperCase();
            if ("ACTIVE".equals(v) || "1".equals(v) || "TRUE".equals(v)) this.isActive = true;
            else if ("INACTIVE".equals(v) || "0".equals(v) || "FALSE".equals(v)) this.isActive = false;
            else this.isActive = null;
        }
    }

    /* ========== Simple listAll() cho select box ========== */
    public List<Division> listAll() {
        String sql = "SELECT id, code, name, is_active, created_at FROM Divisions WHERE is_active = 1 ORDER BY name, code";
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Division> list = new ArrayList<>();
            while (rs.next()) {
                Division d = new Division();
                d.setId(rs.getInt("id"));
                d.setCode(rs.getString("code"));
                d.setName(rs.getString("name"));
                d.setIsActive(rs.getBoolean("is_active"));
                d.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(d);
            }
            return list;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /* ========== Các hàm phân trang / CRUD (giữ nguyên logic bạn đã viết) ========== */
    public int count(String q) throws SQLException {
        String base = "SELECT COUNT(*) FROM Divisions WHERE is_active = 1";
        String sql = (q == null || q.isBlank()) ? base : base + " AND (code LIKE ? OR name LIKE ?)";
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (sql.contains("?")) {
                String like = "%" + q + "%";
                ps.setString(1, like); ps.setString(2, like);
            }
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
        }
    }

    public List<Division> list(String q, int page, int pageSize) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("""
            SELECT id, code, name, is_active, created_at
            FROM Divisions
            WHERE is_active = 1
        """);
        List<Object> args = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            sb.append(" AND (code LIKE ? OR name LIKE ?) ");
            args.add("%"+q+"%"); args.add("%"+q+"%");
        }
        sb.append(" ORDER BY name OFFSET ? ROWS FETCH NEXT ? ROWS ONLY ");
        args.add((page-1)*pageSize); args.add(pageSize);

        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sb.toString())) {
            for (int i=0;i<args.size();i++) ps.setObject(i+1, args.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                List<Division> list = new ArrayList<>();
                while (rs.next()) {
                    Division d = new Division();
                    d.setId(rs.getInt("id"));
                    d.setCode(rs.getString("code"));
                    d.setName(rs.getString("name"));
                    d.setIsActive(rs.getBoolean("is_active"));
                    d.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(d);
                }
                return list;
            }
        }
    }

    public Division find(int id) throws SQLException {
        String sql = """
            SELECT id, code, name, is_active, created_at
            FROM Divisions
            WHERE id = ? AND is_active = 1
        """;
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Division d = new Division();
                d.setId(rs.getInt("id"));
                d.setCode(rs.getString("code"));
                d.setName(rs.getString("name"));
                d.setIsActive(rs.getBoolean("is_active"));
                d.setCreatedAt(rs.getTimestamp("created_at"));
                return d;
            }
        }
    }

    public void create(Division d) throws SQLException {
        String sql = """
            INSERT INTO Divisions(code, name, is_active, created_at)
            VALUES(?, ?, 1, SYSDATETIME())
        """;
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, d.getCode());
            ps.setString(2, d.getName());
            ps.executeUpdate();
        }
    }

    public void update(Division d) throws SQLException {
        String sql = """
            UPDATE Divisions
            SET code = ?, name = ?
            WHERE id = ? AND is_active = 1
        """;
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, d.getCode());
            ps.setString(2, d.getName());
            ps.setInt(3, d.getId());
            ps.executeUpdate();
        }
    }

    /** Xóa mềm: chuyển is_active=0. Trả false nếu còn user tham chiếu. */
    public boolean delete(int id) throws SQLException {
        try (Connection c = getConn()) {
            try (PreparedStatement chk = c.prepareStatement(
                    "SELECT COUNT(*) FROM Users WHERE division_id=? AND (deleted_at IS NULL OR 1=1)")) {
                chk.setInt(1, id);
                try (ResultSet rs = chk.executeQuery()) { rs.next(); if (rs.getInt(1) > 0) return false; }
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE Divisions SET is_active = 0 WHERE id = ? AND is_active = 1")) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        }
    }
}
