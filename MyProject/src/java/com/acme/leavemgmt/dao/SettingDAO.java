package com.acme.leavemgmt.dao;

import com.acme.leavemgmt.model.Setting;
import com.acme.leavemgmt.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SettingDAO {

    public List<Setting> findAll() throws SQLException {
        String sql = "SELECT id, setting_key, setting_value, data_type, group_name, description, is_active, updated_by, updated_at " +
                     "FROM Sys_Settings ORDER BY ISNULL(group_name, 'zzz'), setting_key";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Setting> list = new ArrayList<>();
            while (rs.next()) {
                Setting s = map(rs);
                list.add(s);
            }
            return list;
        }
    }

    public void updateValue(int id, String value, int userId) throws SQLException {
        String sql = "UPDATE Sys_Settings SET setting_value=?, updated_by=?, updated_at=SYSDATETIME() WHERE id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, value);
            ps.setInt(2, userId);
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    public void create(String key, String value, String dataType,
                       String groupName, String description, int userId) throws SQLException {
        String sql = "INSERT INTO Sys_Settings(setting_key, setting_value, data_type, group_name, description, updated_by) " +
                     "VALUES(?,?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.setString(3, dataType);
            ps.setString(4, groupName);
            ps.setString(5, description);
            ps.setInt(6, userId);
            ps.executeUpdate();
        }
    }

    private Setting map(ResultSet rs) throws SQLException {
        Setting s = new Setting();
        s.setId(rs.getInt("id"));
        s.setKey(rs.getString("setting_key"));
        s.setValue(rs.getString("setting_value"));
        s.setDataType(rs.getString("data_type"));
        s.setGroupName(rs.getString("group_name"));
        s.setDescription(rs.getString("description"));
        s.setActive(rs.getBoolean("is_active"));
        Timestamp ts = rs.getTimestamp("updated_at");
        if (ts != null) {
            s.setUpdatedAt(ts.toLocalDateTime());
        }
        int ub = rs.getInt("updated_by");
        if (!rs.wasNull()) {
            s.setUpdatedBy(ub);
        }
        return s;
    }
}
