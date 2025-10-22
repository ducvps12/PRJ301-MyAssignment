// com.acme.leavemgmt.dao.ActivityDAO
package com.acme.leavemgmt.dao;

import com.acme.leavemgmt.util.DBConnection;
import java.sql.*;

public class ActivityDAO {
  public void log(int userId, String action, String entityType, Integer entityId,
                  String note, String ip, String ua) throws SQLException {
    String sql = "INSERT INTO User_Activity(user_id, action, entity_type, entity_id, note, ip_addr, user_agent) " +
                 "VALUES(?,?,?,?,?,?,?)";
    try (Connection c = DBConnection.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setInt(1, userId);
      ps.setString(2, action);
      ps.setString(3, entityType);
      if (entityId == null) ps.setNull(4, Types.INTEGER); else ps.setInt(4, entityId);
      ps.setString(5, note);
      ps.setString(6, ip);
      ps.setString(7, ua);
      ps.executeUpdate();
    }
  }
}
