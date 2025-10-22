package com.test2.web;

import com.test2.util.DBConnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet(name="ListServlet", urlPatterns={"/list"})
public class ListServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    resp.setContentType("text/html; charset=UTF-8");
    PrintWriter out = resp.getWriter();

    // lấy đủ thông tin + tên môn + người tạo
    final String sql = """
        SELECT
          c.cid,
          c.coursename,
          c.startdate,
          c.enddate,
          c.online,
          s.subname       AS subject_name,
          a.username      AS created_by
        FROM dbo.Course  c
        LEFT JOIN dbo.Subject s ON s.subid = c.subid
        LEFT JOIN dbo.Account a ON a.username = c.created_by
        ORDER BY c.cid
        """;

    out.println("<!doctype html><html lang='vi'><meta charset='utf-8'><title>List</title><body>");
    out.println("<table border='1' cellpadding='6' cellspacing='0'>");
    out.println("<tr>"
        + "<th>courseID</th>"
        + "<th>name</th>"
        + "<th>from</th>"
        + "<th>to</th>"
        + "<th>online</th>"
        + "<th>subject name</th>"
        + "<th>created by</th>"
        + "</tr>");

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

      while (rs.next()) {
        out.println("<tr>"
            + td(rs.getInt("cid"))
            + td(rs.getString("coursename"))
            + td(dateStr(rs.getDate("startdate")))
            + td(dateStr(rs.getDate("enddate")))
            + td(rs.getBoolean("online") ? "true" : "false")
            + td(rs.getString("subject_name"))
            + td(rs.getString("created_by"))
            + "</tr>");
      }
    } catch (SQLException e) {
      out.println("<tr><td colspan='7' style='color:red'>SQL error: "
          + escape(e.getMessage()) + "</td></tr>");
      e.printStackTrace();
    }

    out.println("</table>");
    out.println("</body></html>");
  }

  private String td(Object v) { return "<td>" + escape(v == null ? "" : v.toString()) + "</td>"; }
  private String dateStr(java.sql.Date d){ return d==null ? "" : d.toString(); }
  private String escape(String s){
    return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
  }
}
