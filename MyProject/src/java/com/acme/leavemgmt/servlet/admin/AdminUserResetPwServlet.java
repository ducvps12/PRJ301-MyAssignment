package com.acme.leavemgmt.servlet.admin;

import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.AuditLog;
import com.acme.leavemgmt.util.Csrf;
import com.acme.leavemgmt.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;

@WebServlet(name="AdminUserResetPwServlet", urlPatterns={"/admin/users/resetpw"})
public class AdminUserResetPwServlet extends HttpServlet {

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    User me = (User) req.getSession().getAttribute("currentUser");
    if (me == null || !(me.isAdmin() || me.isLeader())) { resp.sendError(403); return; }
    if (!Csrf.valid(req)) { resp.sendError(400, "Bad CSRF"); return; }

    int id;
    try { id = Integer.parseInt(req.getParameter("id")); }
    catch (Exception e) { resp.sendError(400,"Bad id"); return; }

    String sql = "UPDATE Users SET password = ? WHERE id = ?";
    try (Connection cn = DBConnection.getConnection();
         PreparedStatement ps = cn.prepareStatement(sql)) {
      ps.setString(1, "123456"); // TODO: thay bằng hash
      ps.setInt(2, id);
      int n = ps.executeUpdate();
      try { AuditLog.log(req, "ADMIN_USER_RESETPW", "USER", me.getId(), "targetId="+id+", updated="+n); } catch (Throwable ignored) {}
    } catch (SQLException e) {
      throw new ServletException(e);
    }
    resp.sendRedirect(req.getContextPath()+"/admin/users?"+backQuery(req));
  }

  private static String backQuery(HttpServletRequest r){
    String q=r.getParameter("q"); String s=r.getParameter("status");
    String p=r.getParameter("page"); String size=r.getParameter("size");
    return "q="+enc(q)+"&status="+enc(s)+"&page="+enc(p)+"&size="+enc(size);
  }
  private static String enc(String s){ return s==null?"":s; }
}
