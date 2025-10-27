package com.acme.leavemgmt.filter;

import com.acme.leavemgmt.model.User;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * RoleFilter – Bảo vệ khu vực admin & page duyệt
 * Quy ước:
 *  - ADMIN: full quyền /admin/*
 *  - LEADER (DIV_LEADER/TEAM_LEAD/QA_LEAD/...): /admin/div*, /request/approve*
 *  - STAFF: không vào /admin/*
 *
 * Ghi chú: web.xml đang metadata-complete=true ⇒ @WebFilter bị ignore, mapping dùng web.xml.
 */
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

    // 0) Cho preflight qua
    if ("OPTIONS".equalsIgnoreCase(r.getMethod())) {
      chain.doFilter(req, res);
      return;
    }

    final String ctx  = r.getContextPath();                   // vd: /MyProject
    final String uri  = r.getRequestURI();                    // vd: /MyProject/admin/users
    String path = uri.substring(ctx.length());
    if (path.length() > 1 && path.endsWith("/")) path = path.substring(0, path.length()-1);

    // 1) Bỏ qua tài nguyên tĩnh & trang lỗi/health
    if (path.startsWith("/assets/") || path.startsWith("/static/")
        || path.startsWith("/favicon") || path.startsWith("/robots.txt")
        || path.equals("/__health")
        || path.startsWith("/WEB-INF/views/errors/")) {
      chain.doFilter(req, res);
      return;
    }

    // 2) Bắt buộc đăng nhập
    HttpSession session = r.getSession(false);
    User me = (session != null) ? (User) session.getAttribute("currentUser") : null;

    if (me == null) {
      String nextRaw = uri + (r.getQueryString() != null ? "?" + r.getQueryString() : "");
      String next = URLEncoder.encode(nextRaw, StandardCharsets.UTF_8.name());

      if (wantsJson(r)) {
        w.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        w.setContentType("application/json; charset=UTF-8");
        w.getWriter().write("{\"error\":\"UNAUTHORIZED\",\"message\":\"Vui lòng đăng nhập.\",\"next\":\"" + escapeJson(nextRaw) + "\"}");
        return;
      }

      r.getSession(true).setAttribute("flash", new Flash("warn", "Vui lòng đăng nhập để tiếp tục."));
      w.sendRedirect(ctx + "/login?next=" + next);
      return;
    }

    // 3) Tự điều hướng UX: leader vào /admin -> /admin/div
    if ("/admin".equals(path) && me.isLeader() && !me.isAdmin()) {
      w.sendRedirect(ctx + "/admin/div");
      return;
    }

    // 4) Quy tắc quyền (deny-by-default)
    boolean allowed = false;

    if (path.startsWith("/admin")) {
      if (me.isAdmin()) {
        allowed = true; // ADMIN full quyền /admin/*
      } else if (me.isLeader()) {
        // Leader chỉ Dashboard theo phòng ban
        if (path.startsWith("/admin/div")) allowed = true;
        // Cho phép thêm trang báo cáo dành riêng leader nếu có:
        // else if (path.startsWith("/admin/reports")) allowed = true;
        else allowed = false;
      } else {
        allowed = false;
      }
    }

    if (path.equals("/request/approve") || path.startsWith("/request/approve/")) {
      allowed = me.isAdmin() || me.isLeader();
    }

    if (!allowed) {
      String msg = "Bạn không có quyền truy cập trang này. Vai trò: " + me.getRoleCode();

      if (wantsJson(r)) {
        w.setStatus(HttpServletResponse.SC_FORBIDDEN);
        w.setContentType("application/json; charset=UTF-8");
        w.getWriter().write("{\"error\":\"FORBIDDEN\",\"message\":\"" + escapeJson(msg) + "\"}");
        return;
      }

      w.setStatus(HttpServletResponse.SC_FORBIDDEN);
      r.setAttribute("code", 403);
      r.setAttribute("message", msg);
      try {
        r.getRequestDispatcher("/WEB-INF/views/errors/403.jsp").forward(r, w);
      } catch (Exception ex) {
        w.sendError(HttpServletResponse.SC_FORBIDDEN, msg);
      }
      return;
    }

    // 5) Headers chống cache & bảo mật
    noStore(w);
    securityHeaders(w);

    chain.doFilter(req, res);
  }

  /* ===== Helpers ===== */

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
    if (w.getHeader("X-Content-Type-Options") == null) w.setHeader("X-Content-Type-Options", "nosniff");
    if (w.getHeader("X-Frame-Options") == null)       w.setHeader("X-Frame-Options", "SAMEORIGIN");
    if (w.getHeader("Referrer-Policy") == null)        w.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
  }

  // Flash helper (session-scope)
  public static class Flash {
    public final String type; // ok|warn|error
    public final String text;
    public Flash(String type, String text) { this.type = type; this.text = text; }
  }
}
