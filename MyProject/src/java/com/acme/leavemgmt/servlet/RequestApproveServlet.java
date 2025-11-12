package com.acme.leavemgmt.servlet;

import com.acme.leavemgmt.dao.ActivityDAO;
import com.acme.leavemgmt.dao.RequestDAO;
import com.acme.leavemgmt.model.Request;
import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.Csrf;
import com.acme.leavemgmt.util.WebUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Locale;

@WebServlet(name="RequestApproveServlet", urlPatterns={"/request/approve","/request/approve/*"})
public class RequestApproveServlet extends HttpServlet {

  private static final String ST_PENDING  = "PENDING";
  private static final String ST_APPROVED = "APPROVED";
  private static final String ST_REJECTED = "REJECTED";

  private final RequestDAO requestDAO = new RequestDAO();
  private final ActivityDAO activityDAO = new ActivityDAO();

  // ===== Helpers =====
  private boolean hasApproveRole(User u){
    if(u==null) return false;
    try{ if(u.isAdmin() || u.isLeader() || u.canApproveRequests()) return true; } catch(Throwable ignore){}
    String r = (u.getRoleCode()!=null?u.getRoleCode():u.getRole());
    r = r==null? "" : r.trim().toUpperCase(Locale.ROOT);
    return r.equals("ADMIN")||r.equals("SYS_ADMIN")||r.equals("HR_ADMIN")
        ||r.equals("DIV_LEADER")||r.equals("TEAM_LEAD")||r.equals("MANAGER")||r.equals("LEADER");
  }
  private static int actorId(User u){
    try{ if(u.getUserId()>0) return u.getUserId(); }catch(Throwable ignore){}
    try{ if(u.getId()>0)     return u.getId();     }catch(Throwable ignore){}
    return 0;
  }
  private boolean requireLogin(User u, HttpServletRequest req, HttpServletResponse resp) throws IOException {
    if(u!=null) return false;
    String nextRaw = req.getRequestURI() + (req.getQueryString()!=null?("?"+req.getQueryString()):"");
    String next = URLEncoder.encode(nextRaw, StandardCharsets.UTF_8);
    resp.sendRedirect(resp.encodeRedirectURL(req.getContextPath()+"/login?next="+next));
    return true;
  }
  private Integer readId(HttpServletRequest req){
    String idRaw = req.getParameter("id");
    if(idRaw==null){
      String pi=req.getPathInfo(); // "/123"
      if(pi!=null && pi.length()>1) idRaw = pi.substring(1);
    }
    if(idRaw==null || !idRaw.matches("\\d+")) return null;
    try{ return Integer.valueOf(idRaw);}catch(Exception e){ return null; }
  }
  private List<Integer> readIds(HttpServletRequest req){
    List<Integer> ids = new ArrayList<>();
    Integer single = readId(req);
    if(single!=null){ ids.add(single); return ids; }
    String[] many = req.getParameterValues("ids");
    if(many!=null) for(String x: many) if(x!=null && x.matches("\\d+")) ids.add(Integer.parseInt(x));
    return ids;
  }
  private static void json(HttpServletResponse resp, int code, String body) throws IOException {
    resp.setStatus(code);
    resp.setHeader("Cache-Control","no-store, no-cache, must-revalidate");
    resp.setContentType("application/json; charset=UTF-8");
    try(PrintWriter w = resp.getWriter()){ w.write(body); }
  }

  // Chỉ chấp nhận POST (AJAX); GET trả 405
  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.sendError(405);
  }

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    req.setCharacterEncoding("UTF-8");

    HttpSession s = req.getSession(false);
    User me = (s==null)? null : (User) s.getAttribute("currentUser");
    if (requireLogin(me, req, resp)) return;
    if (!hasApproveRole(me)) { json(resp, 403, "{\"success\":false,\"message\":\"FORBIDDEN\"}"); return; }

    // CSRF
    if (!Csrf.isTokenValid(req)) { json(resp, 400, "{\"success\":false,\"message\":\"CSRF_INVALID\"}"); return; }

    // action: approve | reject
    String action = Optional.ofNullable(req.getParameter("action")).orElse("").trim().toLowerCase(Locale.ROOT);
    String target = "approve".equals(action) ? ST_APPROVED : "reject".equals(action) ? ST_REJECTED : null;
    if (target == null) { json(resp, 400, "{\"success\":false,\"message\":\"BAD_ACTION\"}"); return; }

    // note
    String noteRaw = Optional.ofNullable(req.getParameter("note")).orElse("");
    String note = noteRaw.trim();
    if (note.length()>500) note = note.substring(0,500);

    // ids (bulk) hoặc id (single)
    List<Integer> ids = readIds(req);
    if (ids.isEmpty()) { json(resp, 400, "{\"success\":false,\"message\":\"NO_IDS\"}"); return; }

    int actor = actorId(me);
    if (actor <= 0) { json(resp, 401, "{\"success\":false,\"message\":\"AUTH_NO_ACTOR\"}"); return; }

    int ok=0, skipped=0, denied=0, fail=0;
    List<Integer> failed = new ArrayList<>();
    String ip = WebUtil.clientIp(req), ua = WebUtil.userAgent(req);
    String actName = ST_REJECTED.equals(target) ? "REJECT_REQUEST" : "APPROVE_REQUEST";

    for (Integer id : ids) {
      try {
        Request r = requestDAO.findById(id);
        if (r == null) { fail++; failed.add(id); continue; }

        boolean allowed = requestDAO.isAllowedToApprove(me, r);
        if (!allowed) { denied++; failed.add(id); continue; }

        boolean done = requestDAO.updateStatusIfPending(id, target, actor, note);
        if (done) ok++; else { skipped++; failed.add(id); }

        activityDAO.log(actor, actName, "REQUEST", id,
          done? ("Processed "+target) : ("Skip: not "+ST_PENDING), ip, ua);

      } catch (SQLException e) {
        fail++; failed.add(id);
      }
    }

    // coi là success nếu có ít nhất 1 bản ghi cập nhật → UI sẽ reload
    boolean success = ok > 0;
    String failedStr = failed.stream().map(String::valueOf).collect(Collectors.joining(","));
    String body = String.format(Locale.US,
      "{\"success\":%s,\"ok\":%d,\"skipped\":%d,\"denied\":%d,\"fail\":%d,\"status\":\"%s\",\"failed\":\"%s\"}",
      success?"true":"false", ok, skipped, denied, fail, target, failedStr);

    json(resp, 200, body);
  }
}
