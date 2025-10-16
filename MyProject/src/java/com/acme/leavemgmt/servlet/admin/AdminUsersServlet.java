package com.acme.leavemgmt.servlet.admin;

import com.acme.leavemgmt.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class AdminUsersServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String sql = "SELECT id, username, full_name, role, department FROM Users ORDER BY id DESC";
    List<Map<String,Object>> users = new ArrayList<>();
    try (Connection cn = DBConnection.getConnection();
         PreparedStatement ps = cn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        Map<String,Object> m = new HashMap<>();
        m.put("id", rs.getInt("id"));
        m.put("username", rs.getString("username"));
        m.put("full_name", rs.getString("full_name"));
        m.put("role", rs.getString("role"));
        m.put("department", rs.getString("department"));
        users.add(m);
      }
    } catch (SQLException e) {
      throw new ServletException(e);
    }
    req.setAttribute("users", users);
    req.getRequestDispatcher("/WEB-INF/views/admin_users.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    // action=resetpw&id=...
    String action = req.getParameter("action");
    if ("resetpw".equals(action)) {
      String id = req.getParameter("id");
      String newPw = "123456"; // default
      String sql = "UPDATE Users SET password = ? WHERE id = ?";
      try (Connection cn = DBConnection.getConnection();
           PreparedStatement ps = cn.prepareStatement(sql)) {
        ps.setString(1, newPw);
        ps.setInt(2, Integer.parseInt(id));
        ps.executeUpdate();
      } catch (SQLException e) {
        throw new ServletException(e);
      }
    }
    resp.sendRedirect(req.getContextPath() + "/admin/users");
  }
}
