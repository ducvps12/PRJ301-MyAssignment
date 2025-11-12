package com.acme.leavemgmt.servlet;

import com.acme.leavemgmt.dao.UserDAO;
import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.AuditLog;
import com.acme.leavemgmt.util.DBConnection;

import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

  @Resource(name = "jdbc/LeaveDB")
  private DataSource injectedDs;

  private DataSource resolveDs() {
    try {
      if (injectedDs != null) return injectedDs;
      Object v = getServletContext().getAttribute("DS");
      if (v instanceof DataSource) return (DataSource) v;
      InitialContext ic = new InitialContext();
      return (DataSource) ic.lookup("java:comp/env/jdbc/LeaveDB");
    } catch (NamingException e) {
      return null;
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.getRequestDispatcher("/WEB-INF/views/index.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");

    String username = trim(req.getParameter("username"));
    String password = trim(req.getParameter("password"));

    if (username.isEmpty() || password.isEmpty()) {
      req.setAttribute("error", "Vui lòng nhập đầy đủ tài khoản và mật khẩu.");
      req.getRequestDispatcher("/WEB-INF/views/index.jsp").forward(req, resp);
      return;
    }

    DataSource ds = resolveDs();

    // Mở DAO theo DS nếu có; không thì mở Connection thuần
    try (UserDAO userDAO = (ds != null)
            ? new UserDAO(ds)                                  // ctor UserDAO(DataSource)
            : new UserDAO(DBConnection.getConnection())) {      // ctor UserDAO(Connection)

      User u = userDAO.findByUsernameAndPassword(username, password);
      if (u == null) {
        AuditLog.log(req, "LOGIN_FAIL", "USER", null, "Sai tài khoản/mật khẩu");
        req.setAttribute("error", "Sai tài khoản hoặc mật khẩu.");
        req.getRequestDispatcher("/WEB-INF/views/index.jsp").forward(req, resp);
        return;
      }

      // reset session chống fixation
      HttpSession old = req.getSession(false);
      if (old != null) old.invalidate();

      HttpSession session = req.getSession(true);
      session.setAttribute("currentUser", u);

      // Chuẩn hoá role & set cờ
      String role = (u.getRole() != null ? u.getRole() : u.getRoleCode());
      role = role == null ? "" : role.trim().toUpperCase();

      boolean isAdmin = "ADMIN".equals(role) || "SYS_ADMIN".equals(role) || "HR_ADMIN".equals(role);
      boolean isManager = isAdmin || "TEAM_LEAD".equals(role) || "DIV_LEADER".equals(role);

      session.setAttribute("userId", u.getUserId());
      session.setAttribute("fullName", u.getFullName());
      session.setAttribute("role", role);
      session.setAttribute("isAdmin", isAdmin);
      session.setAttribute("isManager", isManager);
      session.setAttribute("dept", u.getDepartment());
      session.setAttribute("deptId", u.getDepartmentId());

      AuditLog.log(req, "LOGIN", "USER", u.getUserId(), "Đăng nhập thành công");

      String ctx = req.getContextPath();
      String dest = isAdmin ? "/admin" : "/portal";
      resp.sendRedirect(ctx + dest);

    } catch (SQLException e) {
      throw new ServletException("Database error during login", e);
    }
  }

  private static String trim(String s) { return s == null ? "" : s.trim(); }
}
