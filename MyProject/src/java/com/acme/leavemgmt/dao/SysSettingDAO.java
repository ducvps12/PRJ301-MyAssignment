// src/main/java/com/acme/leavemgmt/dao/SysSettingDAO.java
package com.acme.leavemgmt.dao;

import com.acme.leavemgmt.model.SysSetting;
import com.acme.leavemgmt.util.DBConnection;

import java.sql.*;
import java.util.*;

public class SysSettingDAO implements AutoCloseable {

    /* ===== Keys ===== */
    public static final String K_SITE_NAME         = "site_name";
    public static final String K_MAIL_ENABLED      = "mail_enabled";
    public static final String K_MAIL_HOST         = "mail_host";
    public static final String K_MAIL_PORT         = "mail_port";
    public static final String K_MAIL_USERNAME     = "mail_username";
    public static final String K_MAIL_PASSWORD     = "mail_password";
    public static final String K_MAIL_FROM         = "mail_from";
    public static final String K_MAIL_FROM_NAME    = "mail_fromName";
    public static final String K_MAIL_STARTTLS     = "mail_starttls";

    public static final String K_GOOGLE_ENABLED    = "oauth_google_enabled";
    public static final String K_GOOGLE_CLIENT_ID  = "google_client_id";
    public static final String K_GOOGLE_CLIENT_SEC = "google_client_secret";
    public static final String K_GOOGLE_REDIRECT   = "google_redirect_uri";
    public static final String K_GOOGLE_ALLOWED_HD = "google_allowed_hd";

    /* ===== State / ctor ===== */
    private final Connection cn;
    private final boolean ownsConnection;

    /** Khuyến nghị: dùng kết nối có sẵn */
    public SysSettingDAO(Connection c) { this.cn = c; this.ownsConnection = false; }

    /** Tiện dụng: tự mở/đóng connection (dùng với try-with-resources) */
    public SysSettingDAO() throws SQLException { this(DBConnection.getConnection()); }

    /* ===== Cache TTL (static) ===== */
    private static volatile Map<String, SysSetting> CACHE = Collections.emptyMap();
    private static volatile long EXPIRES_AT = 0L;
    private static final long TTL_MS = 60_000L;

    public void invalidateCache() { CACHE = Collections.emptyMap(); EXPIRES_AT = 0L; }

    private Map<String, SysSetting> asMapCached() throws SQLException {
        long now = System.currentTimeMillis();
        if (now < EXPIRES_AT && !CACHE.isEmpty()) return CACHE;
        Map<String, SysSetting> fresh = asMap();
        CACHE = fresh;
        EXPIRES_AT = now + TTL_MS;
        return CACHE;
    }

    /* ===== Mapping ===== */
    private SysSetting map(ResultSet rs) throws SQLException {
        SysSetting s = new SysSetting();
        s.setId(rs.getInt("id"));
        s.setSettingKey(rs.getString("setting_key"));
        s.setSettingValue(rs.getString("setting_value"));
        s.setDataType(rs.getString("data_type"));
        s.setGroupName(rs.getString("group_name"));
        s.setDescription(rs.getString("description"));
        s.setActive(rs.getBoolean("is_active"));
        Object updBy = rs.getObject("updated_by");
        if (updBy != null) s.setUpdatedBy((Integer) updBy);
        Timestamp ts = rs.getTimestamp("updated_at");
        if (ts != null) s.setUpdatedAt(ts.toLocalDateTime());
        return s;
    }

    /* ===== Queries ===== */
    public List<SysSetting> findAllActive() throws SQLException {
        final String sql = "SELECT * FROM dbo.Sys_Settings WHERE is_active=1 ORDER BY group_name, setting_key";
        try (PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<SysSetting> list = new ArrayList<>();
            while (rs.next()) list.add(map(rs));
            return list;
        }
    }

    public Map<String, SysSetting> asMap() throws SQLException {
        final String sql = "SELECT * FROM dbo.Sys_Settings WHERE is_active=1";
        try (PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            Map<String, SysSetting> map = new LinkedHashMap<>();
            while (rs.next()) {
                SysSetting s = map(rs);
                map.put(s.getSettingKey(), s);
            }
            return map;
        }
    }

    public SysSetting findByKey(String key) throws SQLException {
        final String sql = "SELECT * FROM dbo.Sys_Settings WHERE setting_key=?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<SysSetting> findByGroup(String group) throws SQLException {
        final String sql = "SELECT * FROM dbo.Sys_Settings WHERE group_name=? AND is_active=1 ORDER BY setting_key";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, group);
            try (ResultSet rs = ps.executeQuery()) {
                List<SysSetting> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        }
    }

    /* ===== Upsert ===== */
    public void upsert(String key, String value, String dataType,
                       String group, String desc, boolean active, Integer updatedBy) throws SQLException {
        final String sql =
            "MERGE dbo.Sys_Settings AS t " +
            "USING (SELECT ? AS setting_key) AS s ON (t.setting_key=s.setting_key) " +
            "WHEN MATCHED THEN UPDATE SET setting_value=?, data_type=?, group_name=?, description=?, is_active=?, updated_by=?, updated_at=SYSDATETIME() " +
            "WHEN NOT MATCHED THEN INSERT(setting_key,setting_value,data_type,group_name,description,is_active,updated_by,updated_at) " +
            "VALUES(?,?,?,?,?,?,?,SYSDATETIME());";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            int i=1;
            ps.setString(i++, key);
            ps.setString(i++, value);
            ps.setString(i++, dataType);
            ps.setString(i++, group);
            ps.setString(i++, desc);
            ps.setBoolean(i++, active);
            if (updatedBy == null) ps.setNull(i++, Types.INTEGER); else ps.setInt(i++, updatedBy);
            ps.executeUpdate();
        }
        invalidateCache();
    }

    public void upsertMany(Map<String,String> kv, String group, boolean active, Integer updatedBy) throws SQLException {
        final String sql =
            "MERGE dbo.Sys_Settings AS t " +
            "USING (SELECT ? AS setting_key) AS s ON (t.setting_key=s.setting_key) " +
            "WHEN MATCHED THEN UPDATE SET setting_value=?, data_type=?, group_name=?, is_active=?, updated_by=?, updated_at=SYSDATETIME() " +
            "WHEN NOT MATCHED THEN INSERT(setting_key,setting_value,data_type,group_name,is_active,updated_by,updated_at) " +
            "VALUES(?,?,?,?,?,?,SYSDATETIME());";
        boolean oldAuto = cn.getAutoCommit();
        cn.setAutoCommit(false);
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            for (Map.Entry<String,String> e : kv.entrySet()) {
                int i=1;
                ps.setString(i++, e.getKey());
                ps.setString(i++, e.getValue());
                ps.setString(i++, "string");
                ps.setString(i++, group);
                ps.setBoolean(i++, active);
                if (updatedBy == null) ps.setNull(i++, Types.INTEGER); else ps.setInt(i++, updatedBy);
                ps.addBatch();
            }
            ps.executeBatch();
            cn.commit();
        } catch (SQLException ex) {
            cn.rollback();
            throw ex;
        } finally {
            cn.setAutoCommit(oldAuto);
        }
        invalidateCache();
    }

    /* ===== Getters (dùng cache) ===== */
    public String get(String key) throws SQLException {
        SysSetting s = asMapCached().get(key);
        String v = (s == null) ? null : s.getSettingValue();
        return v == null ? null : v.trim();
    }
    public String get(String key, String defVal) throws SQLException {
        String v = get(key);
        return (v == null || v.isEmpty()) ? defVal : v;
    }
    public boolean getBool(String key, boolean defVal) throws SQLException {
        String v = get(key);
        if (v == null) return defVal;
        v = v.trim();
        return "1".equals(v) || "true".equalsIgnoreCase(v) || "yes".equalsIgnoreCase(v) || "on".equalsIgnoreCase(v);
    }
    public int getInt(String key, int defVal) throws SQLException {
        String v = get(key);
        try { return v == null ? defVal : Integer.parseInt(v.trim()); }
        catch (Exception e) { return defVal; }
    }

    /* ===== Mail & Google config ===== */
    public MailConfig mailConfig() throws SQLException {
        MailConfig m = new MailConfig();
        m.enabled  = getBool(K_MAIL_ENABLED, true);
        m.host     = get(K_MAIL_HOST, "smtp.gmail.com");
        m.port     = getInt(K_MAIL_PORT, 587);
        m.username = get(K_MAIL_USERNAME, "");
        m.password = get(K_MAIL_PASSWORD, "");
        m.from     = Optional.ofNullable(get(K_MAIL_FROM)).filter(s -> !s.isBlank()).orElse(m.username);
        m.fromName = Optional.ofNullable(get(K_MAIL_FROM_NAME)).filter(s -> !s.isBlank()).orElse(get(K_SITE_NAME, "LeaveMgmt"));
        m.starttls = getBool(K_MAIL_STARTTLS, true);
        return m;
    }
    public static final class MailConfig {
        public boolean enabled;
        public String host;
        public int    port;
        public String username;
        public String password;
        public String from;
        public String fromName;
        public boolean starttls;
        @Override public String toString() {
            return "MailConfig{enabled="+enabled+", host='"+host+"', port="+port+", username='"+username+
                   "', from='"+from+"', fromName='"+fromName+"', starttls="+starttls+"}";
        }
    }

    public GoogleConfig loadGoogleConfig(String contextPath, String scheme, String host, int port) throws SQLException {
        GoogleConfig cfg = new GoogleConfig();
        cfg.enabled      = getBool(K_GOOGLE_ENABLED, false);
        cfg.clientId     = nv(get(K_GOOGLE_CLIENT_ID));
        cfg.clientSecret = nv(get(K_GOOGLE_CLIENT_SEC));
        cfg.redirectUri  = nv(get(K_GOOGLE_REDIRECT));
        cfg.allowedHd    = nv(get(K_GOOGLE_ALLOWED_HD));
        if (cfg.redirectUri.isBlank()) {
            String p = (port==80 || port==443) ? "" : (":" + port);
            cfg.redirectUri = scheme + "://" + host + p + contextPath + "/oauth/google/callback";
        }
        return cfg;
    }
    public static final class GoogleConfig {
        public boolean enabled;
        public String clientId;
        public String clientSecret;
        public String redirectUri;
        public String allowedHd;
    }

    private static String nv(String s){ return s==null? "": s; }

    /* ===== AutoCloseable ===== */
    @Override public void close() {
        if (ownsConnection) {
            try { if (cn != null && !cn.isClosed()) cn.close(); } catch (SQLException ignore) {}
        }
    }
}
