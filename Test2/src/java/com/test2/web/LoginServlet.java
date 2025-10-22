package com.test2.web;

import com.test2.util.DBConnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.*;

import java.io.IOException;
import java.sql.*;

@WebServlet(name="LoginServlet", urlPatterns={"/login"})
public class LoginServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.getRequestDispatcher("/login.html").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");
    resp.setContentType("text/plain; charset=UTF-8");

    // Hỗ trợ cả username/password và user/pass
    String username = firstNonNull(req.getParameter("username"), req.getParameter("user"));
    String password = firstNonNull(req.getParameter("password"), req.getParameter("pass"));

    boolean ok = false;
    if (username != null && password != null) {
      String sql = "SELECT username FROM dbo.Account WHERE username=? AND [password]=?";
      try (Connection c = DBConnection.getConnection();
           PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setString(1, username);
        ps.setString(2, password);
        try (ResultSet rs = ps.executeQuery()) {
          ok = rs.next();
        }
      } catch (SQLException e) {
        // Nếu lỗi DB, coi như fail nhưng trả thông báo rõ ràng
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.getWriter().print("Login failed (DB error): " + e.getMessage());
        return;
      }
    }

    if (ok) {
      // Quan trọng: vẫn set session để các bài sau (/create) dùng
      HttpSession session = req.getSession(true);
      session.setAttribute("currentUser", username);
      resp.setStatus(HttpServletResponse.SC_OK);
      resp.getWriter().print("Login Successful");
    } else {
      resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      resp.getWriter().print("Login failed");
    }
  }

  private static String firstNonNull(String a, String b){ return a != null ? a : b; }
}
