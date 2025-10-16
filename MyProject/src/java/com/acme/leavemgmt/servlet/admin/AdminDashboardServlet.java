package com.acme.leavemgmt.servlet.admin;

import com.acme.leavemgmt.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;

public class AdminDashboardServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    try (Connection cn = DBConnection.getConnection();
         Statement st = cn.createStatement()) {

      int totalUsers = firstInt(st.executeQuery("SELECT COUNT(*) FROM Users"));
      int totalReq   = firstInt(st.executeQuery("SELECT COUNT(*) FROM Requests"));
      int pending    = firstInt(st.executeQuery(
                       "SELECT COUNT(*) FROM Requests WHERE status='INPROGRESS'"));
      int approved   = firstInt(st.executeQuery(
                       "SELECT COUNT(*) FROM Requests WHERE status='APPROVED'"));
      int rejected   = firstInt(st.executeQuery(
                       "SELECT COUNT(*) FROM Requests WHERE status='REJECTED'"));

      req.setAttribute("totalUsers", totalUsers);
      req.setAttribute("totalReq", totalReq);
      req.setAttribute("pending", pending);
      req.setAttribute("approved", approved);
      req.setAttribute("rejected", rejected);
      req.getRequestDispatcher("/WEB-INF/views/admin_dashboard.jsp").forward(req, resp);
    } catch (SQLException e) {
      throw new ServletException(e);
    }
  }
  private int firstInt(ResultSet rs) throws SQLException { rs.next(); return rs.getInt(1); }
}
