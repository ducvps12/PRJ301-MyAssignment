package com.acme.leavemgmt.servlet.admin;

import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServlet;

import java.io.IOException;
import java.sql.*;

@WebServlet(name = "AdminUserDetailServlet", urlPatterns = {"/admin/users/detail"})
public class AdminUserDetailServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    Integer id = parseInt(req.getParameter("id"));
    if (id == null || id <= 0) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid id");
      return;
    }

    String sql = "SELECT id, username, full_name, email, role, department, status " +
                 "FROM Users WHERE id = ?";

    try (Connection c = DBConnection.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {

      ps.setInt(1, id);

      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) {
          resp.sendError(HttpServletResponse.SC_NOT_FOUND);
          return;
        }

     User u = new User();
u.setId(rs.getInt("id"));
u.setUsername(rs.getString("username"));
u.setFullName(rs.getString("full_name"));
u.setEmail(rs.getString("email"));

u.setRole((Integer) rs.getObject("role"));
u.setDepartment((Integer) rs.getObject("department"));

Integer st = (Integer) rs.getObject("status");
u.setStatus(st == null ? 0 : st);

req.setAttribute("u", u);

      }

    } catch (SQLException e) {
      throw new ServletException(e);
    }

    req.getRequestDispatcher("/WEB-INF/views/admin/user_detail.jsp").forward(req, resp);
  }

  private Integer parseInt(String s) {
    try { return (s == null) ? null : Integer.valueOf(s.trim()); }
    catch (NumberFormatException e) { return null; }
  }
}
