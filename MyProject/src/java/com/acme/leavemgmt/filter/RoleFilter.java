package com.acme.leavemgmt.filter;

import com.acme.leavemgmt.model.User;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebFilter(
  filterName = "RoleFilter",
  urlPatterns = { "/admin", "/admin/*", "/request/approve", "/request/approve/*" }
)
public class RoleFilter implements Filter {

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest r = (HttpServletRequest) req;
    HttpServletResponse w = (HttpServletResponse) res;

    if ("OPTIONS".equalsIgnoreCase(r.getMethod())) {
      chain.doFilter(req, res);
      return;
    }

    // ==== 1) Đăng nhập ====
    HttpSession session = r.getSession(false);
    User me = (session != null) ? (User) session.getAttribute("currentUser") : null;
    if (me == null) {
      String nextRaw = r.getRequestURI() + (r.getQueryString() != null ? "?" + r.getQueryString() : "");
      String next = URLEncoder.encode(nextRaw, StandardCharsets.UTF_8.name());
      // Flash message (đặt để header.jsp hiển thị)
      r.getSession(true).setAttribute("flash", new Flash("warn", "Vui lòng đăng nhập để tiếp tục."));
      w.sendRedirect(r.getContextPath() + "/login?next=" + next);
      return;
    }

    // ==== 2) Xác định role & path ====
    String role = safeUpper(me.getRole());
    String ctx  = r.getContextPath();
    String path = r.getRequestURI().substring(ctx.length()); // vd: /admin/div

    // Chuẩn hoá: /admin/ -> /admin (tuỳ thích)
    if ("/admin/".equals(path)) path = "/admin";

    boolean isAdmin    = "ADMIN".equals(role);
    boolean isDivLead  = "DIV_LEADER".equals(role);
    boolean isTeamLead = "TEAM_LEAD".equals(role);

    // ==== 3) Phân quyền (mặc định chặn) ====
    boolean allowed = false;

    if ("/admin".equals(path) || path.startsWith("/admin/")) {
      if (path.startsWith("/admin/users")) {
        allowed = isAdmin;                                  // chỉ ADMIN
      } else if (path.startsWith("/admin/div")) {
        allowed = isDivLead || isAdmin;                     // DIV_LEADER hoặc ADMIN
      } else {
        allowed = isAdmin;                                  // dashboard/admin khác -> ADMIN
      }
    }

    if ("/request/approve".equals(path) || path.startsWith("/request/approve/")) {
      allowed = isAdmin || isDivLead || isTeamLead;         // phê duyệt: 3 nhóm
    }

    // ==== 4) Không có quyền -> báo rõ ràng ====
    if (!allowed) {
      String msg = "Bạn không có quyền truy cập trang này."
                 + " Vai trò hiện tại: " + (role.isEmpty() ? "UNKNOWN" : role);

      if (wantsJson(r)) {
        w.setStatus(HttpServletResponse.SC_FORBIDDEN);
        w.setContentType("application/json; charset=UTF-8");
        w.getWriter().write("{\"error\":\"FORBIDDEN\",\"message\":\"" + escapeJson(msg) + "\"}");
        return;
      }

      // Flash + forward trang 403 tuỳ biến
      r.setAttribute("code", 403);
      r.setAttribute("message", msg);
      w.setStatus(HttpServletResponse.SC_FORBIDDEN);
      try {
        r.getRequestDispatcher("/WEB-INF/views/errors/403.jsp").forward(r, w);
      } catch (Exception ex) {
        w.sendError(HttpServletResponse.SC_FORBIDDEN, msg);
      }
      return;
    }

    // ==== 5) Cho qua + chống cache ====
    w.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
    w.setHeader("Pragma", "no-cache");
    w.setDateHeader("Expires", 0);
    chain.doFilter(req, res);
  }

  // ==== Helpers ====
  private static String safeUpper(String s) { return s == null ? "" : s.toUpperCase(); }

  private static boolean wantsJson(HttpServletRequest r) {
    String xhr = r.getHeader("X-Requested-With");
    String acc = r.getHeader("Accept");
    return "XMLHttpRequest".equalsIgnoreCase(xhr)
        || (acc != null && (acc.toLowerCase().contains("application/json") || acc.toLowerCase().contains("+json")));
  }

  private static String escapeJson(String s) {
    return s == null ? "" : s.replace("\\","\\\\").replace("\"","\\\"");
  }

  @Override public void init(FilterConfig filterConfig) {}
  @Override public void destroy() {}

  // Flash helper (nếu bạn thích dùng object thay vì Map)
  public static class Flash {
    public final String type; // ok|warn|error
    public final String text;
    public Flash(String type, String text) { this.type = type; this.text = text; }
  }
}
