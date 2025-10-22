package com.test2.web;

import com.test2.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name="CreateServlet", urlPatterns="/create")
public class CreateServlet extends HttpServlet {

  // load Subjects cho dropdown
  private List<int[]> loadSubjects() throws SQLException {
    String sql = "SELECT subid, subname FROM dbo.Subject ORDER BY subname";
    try (Connection cn = DBConnection.getConnection();
         PreparedStatement ps = cn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
      List<int[]> list = new ArrayList<>();
      while (rs.next()) {
        // [0]=id, [1]=tên được lấy qua request attribute khác (để đơn giản sẽ build HTML trực tiếp)
        // nhưng ở đây cứ trả id, còn tên sẽ ghép tại JSP.
      }
      return null; // (không dùng, vì mình render trực tiếp bằng JSP dưới)
    }
  }

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    // Lấy subjects cho view
    try (Connection cn = DBConnection.getConnection();
         PreparedStatement ps = cn.prepareStatement(
             "SELECT subid, subname FROM dbo.Subject ORDER BY subname");
         ResultSet rs = ps.executeQuery()) {
      req.setAttribute("subjectsRS", rs); // NetBeans copy rs vào request không phù hợp
    } catch (SQLException ignore) { }      // => Ta sẽ render qua truy vấn trực tiếp trong JSP cho gọn
    req.getRequestDispatcher("/WEB-INF/create.jsp").forward(req, resp);
  }

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.setCharacterEncoding("UTF-8");
    String coursename = req.getParameter("name");
    LocalDate from = LocalDate.parse(req.getParameter("from"));
    LocalDate to   = LocalDate.parse(req.getParameter("to"));
    boolean online = "on".equals(req.getParameter("online"));
    int subid      = Integer.parseInt(req.getParameter("subject"));
    String createdBy = (String) req.getSession().getAttribute("currentUser"); // username

    if (coursename==null || coursename.isBlank() || to.isBefore(from)) {
      req.setAttribute("msg", "Tên không rỗng và To ≥ From");
      req.getRequestDispatcher("/WEB-INF/create.jsp").forward(req, resp);
      return;
    }

    String sql = "INSERT INTO dbo.Course(coursename,startdate,enddate,online,subid,created_by) "
               + "VALUES (?,?,?,?,?,?)";
    try (Connection cn = DBConnection.getConnection();
         PreparedStatement ps = cn.prepareStatement(sql)) {
      ps.setString(1, coursename);
      ps.setDate(2, Date.valueOf(from));
      ps.setDate(3, Date.valueOf(to));
      ps.setBoolean(4, online);
      ps.setInt(5, subid);
      ps.setString(6, createdBy);
      ps.executeUpdate();
    } catch (SQLException e) {
      req.setAttribute("msg", "Lỗi lưu Course: " + e.getMessage());
      req.getRequestDispatcher("/WEB-INF/create.jsp").forward(req, resp);
    }
  }
}
