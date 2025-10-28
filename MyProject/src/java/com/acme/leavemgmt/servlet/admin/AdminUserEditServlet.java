package com.acme.leavemgmt.servlet.admin;

import com.acme.leavemgmt.util.DBConnection;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;

@WebServlet(name="AdminUserEditServlet", urlPatterns={"/admin/users/edit"})
public class AdminUserEditServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    int id = Integer.parseInt(req.getParameter("id"));
    try (Connection c = DBConnection.getConnection()) {
      PreparedStatement ps = c.prepareStatement(
        "SELECT id, username, full_name, email, role, department, status FROM Users WHERE id=?");
      ps.setInt(1, id);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) req.setAttribute("user", rs);
    } catch (SQLException e) { throw new ServletException(e); }
    req.getRequestDispatcher("/WEB-INF/views/admin/user_edit.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.setCharacterEncoding("UTF-8");
    int id = Integer.parseInt(req.getParameter("id"));
    String fullName = req.getParameter("fullName");
    String email = req.getParameter("email");
    String role = req.getParameter("role");
    String dept = req.getParameter("department");
    try (Connection c = DBConnection.getConnection()) {
      PreparedStatement ps = c.prepareStatement(
        "UPDATE Users SET full_name=?, email=?, role=?, department=? WHERE id=?");
      ps.setString(1, fullName);
      ps.setString(2, email);
      ps.setString(3, role);
      ps.setString(4, dept);
      ps.setInt(5, id);
      ps.executeUpdate();
    } catch (SQLException e) { throw new ServletException(e); }
    resp.sendRedirect(req.getContextPath() + "/admin/users?updated=1");
  }
}
