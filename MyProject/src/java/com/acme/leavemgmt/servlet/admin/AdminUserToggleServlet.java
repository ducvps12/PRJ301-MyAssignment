package com.acme.leavemgmt.servlet.admin;

import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet(name="AdminUserToggleServlet", urlPatterns={"/admin/users/toggle"})
public class AdminUserToggleServlet extends HttpServlet {

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    // 0) Quyền
    HttpSession s = req.getSession(false);
    User me = (s!=null) ? (User) s.getAttribute("currentUser") : null;
    if (me==null || !(me.isAdmin() || me.isLead())) {
      resp.sendError(HttpServletResponse.SC_FORBIDDEN); return;
    }

    // 1) Lấy id / ids và action
    List<Integer> ids = new ArrayList<>();
    String id = req.getParameter("id");
    if (id != null && !id.isBlank()) try { ids.add(Integer.parseInt(id)); } catch (NumberFormatException ignore) {}

    String[] bulkIds = req.getParameterValues("ids");
    if (bulkIds != null) for (String x : bulkIds) {
      if (x!=null && !x.isBlank()) try { ids.add(Integer.parseInt(x)); } catch (NumberFormatException ignore) {}
    }
    if (ids.isEmpty()) { resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing id(s)"); return; }

    String action = Optional.ofNullable(req.getParameter("action")).orElse("").trim().toLowerCase(Locale.ROOT);
    // action: "", "activate", "deactivate" (nếu rỗng thì toggle theo CASE WHEN)

    int updated = 0;
    try (Connection c = DBConnection.getConnection()) {
      if ("activate".equals(action) || "deactivate".equals(action)) {
        String sql = "UPDATE Users SET status=? WHERE id IN (" + qs(ids.size()) + ")";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
          ps.setString(1, "activate".equals(action) ? "ACTIVE" : "INACTIVE");
          bindIds(ps, 2, ids);
          updated = ps.executeUpdate();
        }
      } else {
        // Toggle: ACTIVE -> INACTIVE, ngược lại -> ACTIVE
        String sql = "UPDATE Users SET status=CASE WHEN status='ACTIVE' THEN 'INACTIVE' ELSE 'ACTIVE' END " +
                     "WHERE id IN (" + qs(ids.size()) + ")";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
          bindIds(ps, 1, ids);
          updated = ps.executeUpdate();
        }
      }
    } catch (SQLException e) {
      throw new ServletException(e);
    }

    // 3) Flash + điều hướng về list (giữ filter/paging)
    String back = buildBackUrl(req, "/admin/users");
    if (s != null) s.setAttribute("flash", "Đã cập nhật trạng thái " + updated + " tài khoản.");
    // Nếu muốn hiện trang kết quả thay vì redirect, comment dòng dưới và forward đến JSP:
    resp.sendRedirect(req.getContextPath() + back);
    //req.setAttribute("count", updated);
    //req.setAttribute("ids", ids);
    //req.getRequestDispatcher("/WEB-INF/views/admin/toggle.jsp").forward(req, resp);
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
