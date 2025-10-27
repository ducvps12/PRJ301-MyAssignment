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

  @Override public void init(FilterConfig filterConfig) {}
  @Override public void destroy() {}

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest  r = (HttpServletRequest) req;
    HttpServletResponse w = (HttpServletResponse) res;

    // Preflight cho CORS
    if ("OPTIONS".equalsIgnoreCase(r.getMethod())) {
      chain.doFilter(req, res);
      return;
    }

    // ==== 1) Bắt buộc đăng nhập ====
    HttpSession session = r.getSession(false);
    User me = (session != null) ? (User) session.getAttribute("currentUser") : null;

    if (me == null) {
      String nextRaw = r.getRequestURI() + (r.getQueryString() != null ? "?" + r.getQueryString() : "");
      String next = URLEncoder.encode(nextRaw, StandardCharsets.UTF_8.name());
      // Dùng session-scope flash vì redirect
      r.getSession(true).setAttribute("flash", new Flash("warn", "Vui lòng đăng nhập để tiếp tục."));
      w.sendRedirect(r.getContextPath() + "/login?next=" + next);
      return;
    }

    // ==== 2) Xác định role & path ====
    String role = safeUpper(me.getRole());
    String ctx  = r.getContextPath();
    String path = normalizePath(r.getRequestURI().substring(ctx.length())); // vd: /admin/div

    boolean isAdmin    = "ADMIN".equals(role);
    boolean isDivLead  = "DIV_LEADER".equals(role);
    boolean isTeamLead = "TEAM_LEAD".equals(role);

    // ==== 3) Tự điều hướng dashboard theo vai trò (UX) ====
    // Nếu DIV_LEADER vào /admin tổng thì đẩy về /admin/div (dashboard theo phòng ban)
    if ("/admin".equals(path) && isDivLead && !isAdmin) {
      String to = ctx + "/admin/div";
      w.sendRedirect(to);
      return;
    }

    // ==== 4) Phân quyền (deny-by-default) ====
    boolean allowed = false;

    if (path.startsWith("/admin")) {
      // /admin/users chỉ cho ADMIN
      if (path.startsWith("/admin/users")) {
        allowed = isAdmin;
      }
      // /admin/div cho DIV_LEADER hoặc ADMIN (dashboard theo phòng ban)
      else if (path.startsWith("/admin/div")) {
        allowed = isDivLead || isAdmin;
      }
      // Các trang admin còn lại: chỉ ADMIN
      else {
        allowed = isAdmin;
      }
    }

    // /request/approve và con: ADMIN | DIV_LEADER | TEAM_LEAD
    if (path.equals("/request/approve") || path.startsWith("/request/approve/")) {
      allowed = isAdmin || isDivLead || isTeamLead;
    }

    // ==== 5) Chặn + phản hồi rõ ràng ====
    if (!allowed) {
      String msg = "Bạn không có quyền truy cập trang này. Vai trò hiện tại: " + (role.isEmpty() ? "UNKNOWN" : role);

      if (wantsJson(r)) {
        w.setStatus(HttpServletResponse.SC_FORBIDDEN);
        w.setContentType("application/json; charset=UTF-8");
        w.getWriter().write("{\"error\":\"FORBIDDEN\",\"message\":\"" + escapeJson(msg) + "\"}");
        return;
      }

      // Forward sang trang 403 tuỳ biến; nếu lỗi thì dùng sendError
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

    // ==== 6) Cho qua + headers chống cache & bảo mật cơ bản ====
    noStore(w);
    securityHeaders(w);

    chain.doFilter(req, res);
  }

  // ========= Helpers =========

  private static String normalizePath(String p) {
    if (p == null || p.isEmpty()) return "/";
    // Bỏ slash cuối: "/admin/" -> "/admin"
    if (p.length() > 1 && p.endsWith("/")) p = p.substring(0, p.length() - 1);
    return p;
  }

  private static String safeUpper(String s) { return (s == null) ? "" : s.trim().toUpperCase(); }

  private static boolean wantsJson(HttpServletRequest r) {
    String xhr = r.getHeader("X-Requested-With");
    String acc = r.getHeader("Accept");
    return "XMLHttpRequest".equalsIgnoreCase(xhr)
        || (acc != null && (acc.toLowerCase().contains("application/json") || acc.toLowerCase().contains("+json")));
  }

  private static String escapeJson(String s) {
    return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  private static void noStore(HttpServletResponse w) {
    w.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
    w.setHeader("Pragma", "no-cache");
    w.setDateHeader("Expires", 0);
  }

  private static void securityHeaders(HttpServletResponse w) {
    // Một số header an toàn tối thiểu
    if (w.getHeader("X-Content-Type-Options") == null) w.setHeader("X-Content-Type-Options", "nosniff");
    if (w.getHeader("X-Frame-Options") == null) w.setHeader("X-Frame-Options", "SAMEORIGIN");
    if (w.getHeader("Referrer-Policy") == null) w.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
  }

  // Flash helper (session-scope)
  public static class Flash {
    public final String type; // ok|warn|error
    public final String text;
    public Flash(String type, String text) { this.type = type; this.text = text; }
  }
}
