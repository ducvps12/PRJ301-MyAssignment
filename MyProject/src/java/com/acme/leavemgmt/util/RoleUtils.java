package com.acme.leavemgmt.util;

import com.acme.leavemgmt.model.User;
import java.sql.*;

public final class RoleUtils {
  private RoleUtils(){}

  public static boolean hasRole(User u, String... roles){
    if (u == null || u.getRole() == null) return false;
    String r = u.getRole();
    for (String x : roles){
      if (r.equalsIgnoreCase(x)) return true;
    }
    return false;
  }

  // Kiểm tra quy tắc phê duyệt dựa Approver_Rules(from_role -> to_role)
  public static boolean canApprove(String approverRole, String requesterRole) {
    String sql = "SELECT 1 FROM Approver_Rules WHERE from_role = ? AND to_role = ? AND can_approve = 1";
    try (Connection cn = DBConnection.getConnection();
         PreparedStatement ps = cn.prepareStatement(sql)){
      ps.setString(1, requesterRole);
      ps.setString(2, approverRole);
      try(ResultSet rs = ps.executeQuery()){
        return rs.next();
      }
    } catch (Exception e){
      e.printStackTrace();
      return false;
    }
  }
}
