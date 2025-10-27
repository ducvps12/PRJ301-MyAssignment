package com.acme.leavemgmt.util;

import com.acme.leavemgmt.dao.ActivityDAO;
import com.acme.leavemgmt.model.Activity;
import com.acme.leavemgmt.model.User;
import jakarta.servlet.http.HttpServletRequest;

public final class AuditLog {
  private static final ActivityDAO dao = new ActivityDAO();

    public static void log(HttpServletRequest req, String login_fail, String user, Object object, String sai_tài_khoảnmật_khẩu) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
  private AuditLog(){}

  public static void log(HttpServletRequest req, String action,
                         String entityType, Integer entityId, String note) {
    try {
      User cu = (User) req.getSession().getAttribute("currentUser");
      Activity a = new Activity();
      a.setUserId(cu!=null ? cu.getId() : null);
      a.setAction(action);
      a.setEntityType(entityType);
      a.setEntityId(entityId);
      a.setNote(note);
      a.setIpAddr(getIp(req));
      a.setUserAgent(getUA(req));
      dao.insert(a);
    } catch (Exception ignore) {}
  }

  private static String getIp(HttpServletRequest r){
    String xff = r.getHeader("X-Forwarded-For");
    return (xff!=null && !xff.isBlank()) ? xff.split(",")[0].trim() : r.getRemoteAddr();
  }
  private static String getUA(HttpServletRequest r){
    String ua = r.getHeader("User-Agent");
    return (ua!=null && ua.length()>255) ? ua.substring(0,255) : ua;
  }
}
