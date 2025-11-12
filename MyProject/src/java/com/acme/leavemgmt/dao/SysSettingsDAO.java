package com.acme.leavemgmt.dao;

import java.sql.*;
import java.util.*;
import javax.sql.DataSource;

public class SysSettingsDAO implements AutoCloseable {
    private final Connection cn;

    public SysSettingsDAO(DataSource ds) throws SQLException {
        this.cn = ds.getConnection();
    }

    public Map<String,String> loadAll() throws SQLException {
        Map<String,String> map = new HashMap<>();
        String sql = "SELECT setting_key, setting_value FROM dbo.Sys_Settings WHERE is_active=1";
        try (PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) map.put(rs.getString(1), rs.getString(2));
        }
        return map;
    }

    @Override
    public void close() throws SQLException { if (cn != null) cn.close(); }
}
