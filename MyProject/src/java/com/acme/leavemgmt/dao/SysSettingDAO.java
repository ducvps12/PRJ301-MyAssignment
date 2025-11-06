// src/main/java/com/acme/leavemgmt/dao/SysSettingDAO.java
package com.acme.leavemgmt.dao;

import com.acme.leavemgmt.model.SysSetting;
import com.acme.leavemgmt.util.DBConnection;
import java.sql.*;
import java.util.*;

public class SysSettingDAO {

    private SysSetting map(ResultSet rs) throws SQLException {
        SysSetting s = new SysSetting();
        s.setId(rs.getInt("id"));
        s.setSettingKey(rs.getString("setting_key"));
        s.setSettingValue(rs.getString("setting_value"));
        s.setDataType(rs.getString("data_type"));
        s.setGroupName(rs.getString("group_name"));
        s.setDescription(rs.getString("description"));
        s.setActive(rs.getBoolean("is_active"));
        s.setUpdatedBy((Integer) rs.getObject("updated_by"));
        Timestamp ts = rs.getTimestamp("updated_at");
        if (ts != null) s.setUpdatedAt(ts.toLocalDateTime());
        return s;
    }

    public List<SysSetting> findAllActive() throws SQLException {
        String sql = "SELECT * FROM Sys_Settings WHERE is_active = 1 ORDER BY group_name, setting_key";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<SysSetting> list = new ArrayList<>();
            while (rs.next()) list.add(map(rs));
            return list;
        }
    }

    public Map<String, SysSetting> asMap() throws SQLException {
        Map<String, SysSetting> map = new LinkedHashMap<>();
        for (SysSetting s : findAllActive()) map.put(s.getSettingKey(), s);
        return map;
    }

    public SysSetting findByKey(String key) throws SQLException {
        String sql = "SELECT * FROM Sys_Settings WHERE setting_key = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void upsert(String key, String value, String dataType,
                       String group, String desc, boolean active, Integer updatedBy) throws SQLException {
        String sql =
          "MERGE Sys_Settings AS t " +
          "USING (SELECT ? AS setting_key) AS s " +
          "ON (t.setting_key = s.setting_key) " +
          "WHEN MATCHED THEN UPDATE SET setting_value=?, data_type=?, group_name=?, description=?, is_active=?, updated_by=?, updated_at=SYSDATETIME() " +
          "WHEN NOT MATCHED THEN INSERT(setting_key,setting_value,data_type,group_name,description,is_active,updated_by,updated_at) " +
          "VALUES(?,?,?,?,?,?,?,SYSDATETIME());";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
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
    }


// ---- Cache TTL đơn giản ----
private static volatile Map<String, SysSetting> CACHE = Collections.emptyMap();
private static volatile long EXPIRES_AT = 0L; // epoch millis
private static final long TTL_MS = 60_000;

public Map<String, SysSetting> asMapCached() throws SQLException {
    long now = System.currentTimeMillis();
    if (now < EXPIRES_AT && !CACHE.isEmpty()) return CACHE;
    Map<String, SysSetting> map = asMap();
    CACHE = map;
    EXPIRES_AT = now + TTL_MS;
    return CACHE;
}
public void invalidateCache(){ CACHE = Collections.emptyMap(); EXPIRES_AT = 0L; }

// ---- tiện ích đọc giá trị theo kiểu ----
public String get(String key) throws SQLException {
    SysSetting s = asMapCached().get(key);
    return s == null ? null : s.getSettingValue();
}
public boolean getBool(String key, boolean defVal) throws SQLException {
    String v = get(key);
    if (v == null) return defVal;
    return "1".equals(v) || "true".equalsIgnoreCase(v) || "yes".equalsIgnoreCase(v);
}
public int getInt(String key, int defVal) throws SQLException {
    String v = get(key);
    try { return v == null ? defVal : Integer.parseInt(v.trim()); }
    catch (Exception e) { return defVal; }
}











}
