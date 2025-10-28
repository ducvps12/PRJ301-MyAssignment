package com.acme.leavemgmt.servlet.admin;

import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet(name="AdminUserResetPwServlet", urlPatterns={"/admin/users/resetpw"})
public class AdminUserResetPwServlet extends HttpServlet {

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession s = req.getSession(false);
    User me = (s!=null) ? (User) s.getAttribute("currentUser") : null;
    if (me==null || !(me.isAdmin() || me.isLead())) {
      resp.sendError(HttpServletResponse.SC_FORBIDDEN); return;
    }

    List<Integer> ids = new ArrayList<>();
    String id = req.getParameter("id");
    if (id != null && !id.isBlank()) try { ids.add(Integer.parseInt(id)); } catch (NumberFormatException ignore) {}
    String[] bulkIds = req.getParameterValues("ids");
    if (bulkIds != null) for (String x : bulkIds) {
      if (x!=null && !x.isBlank()) try { ids.add(Integer.parseInt(x)); } catch (NumberFormatException ignore) {}
    }
    if (ids.isEmpty()) { resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing id(s)"); return; }

    String defaultPw = "123456";
    int updated = 0;

    try (Connection c = DBConnection.getConnection()) {
      // Nếu DB dùng cột password_hash + SHA2_256 thì đổi lệnh dưới cho phù hợp.
      // 1) cố thử cập nhật cột password trước
      String sql = "UPDATE Users SET password=? WHERE id IN (" + qs(ids.size()) + ")";
      try (PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setString(1, defaultPw);
        bindIds(ps, 2, ids);
        updated = ps.executeUpdate();
      }
    } catch (SQLException e) {
      throw new ServletException(e);
    }

    String back = buildBackUrl(req, "/admin/users");
    if (s != null) s.setAttribute("flash", "Đã reset mật khẩu " + updated + " tài khoản về \"" + defaultPw + "\".");
    resp.sendRedirect(req.getContextPath() + back);

    // Nếu muốn hiển thị trang kết quả:
    //req.setAttribute("count", updated);
    //req.setAttribute("ids", ids);
    //req.setAttribute("defaultPw", defaultPw);
    //req.getRequestDispatcher("/WEB-INF/views/admin/resetpw.jsp").forward(req, resp);
  }

  private static String qs(int n){ return String.join(",", Collections.nCopies(n, "?")); }
  private static void bindIds(PreparedStatement ps, int start, List<Integer> ids) throws SQLException {
    int i = start;
    for (Integer id : ids) ps.setInt(i++, id);
  }
  private static String buildBackUrl(HttpServletRequest req, String base){
    String q = nv(req,"q"), st = nv(req,"status"), page = nv(req,"page"), size = nv(req,"size");
    StringBuilder sb = new StringBuilder(base).append("?page=").append(page.isEmpty()?"1":page)
        .append("&size=").append(size.isEmpty()?"20":size);
    if(!q.isEmpty()) sb.append("&q=").append(encode(q));
    if(!st.isEmpty()) sb.append("&status=").append(encode(st));
    return sb.toString();
  }
  private static String nv(HttpServletRequest r,String k){ String v=r.getParameter(k); return v==null?"":v; }
  private static String encode(String s){ return s.replace(" ", "%20"); }
}
