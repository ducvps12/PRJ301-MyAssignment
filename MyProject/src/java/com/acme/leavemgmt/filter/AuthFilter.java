package com.acme.leavemgmt.filter;

import com.acme.leavemgmt.model.User;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.IOException;

public class AuthFilter implements Filter {
  @Override public void init(FilterConfig cfg) {}
  @Override public void destroy() {}

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest  r = (HttpServletRequest) req;
    HttpServletResponse w = (HttpServletResponse) res;

    // 0) Bỏ qua forward/include để tránh loop khi forward tới /WEB-INF/*.jsp
    DispatcherType dt = r.getDispatcherType();
    if (dt == DispatcherType.FORWARD || dt == DispatcherType.INCLUDE) {
      chain.doFilter(req, res); return;
    }

    // 1) Preflight
    if ("OPTIONS".equalsIgnoreCase(r.getMethod())) { chain.doFilter(req, res); return; }

    String ctx  = r.getContextPath();                 // /MyProject
    String uri  = r.getRequestURI();                  // /MyProject/xxx
    String path = uri.startsWith(ctx) ? uri.substring(ctx.length()) : uri; // /xxx

    // 2) Public routes & static & /WEB-INF/**
    if (isPublic(path) || path.startsWith("/WEB-INF/")) { chain.doFilter(req, res); return; }

    // 3) Đã đăng nhập?
    HttpSession s = r.getSession(false);
    boolean loggedIn = false;
    if (s != null) {
      // Ưu tiên currentUser (mới)
      Object cu = s.getAttribute("currentUser");
      if (cu instanceof User) loggedIn = true;
      // Tương thích key cũ (nếu dự án còn dùng ở đâu đó)
      if (!loggedIn) {
        Object u = s.getAttribute("user");
        loggedIn = (u instanceof User) || (s.getAttribute("userId") != null);
      }
    }
    if (loggedIn) { chain.doFilter(req, res); return; }

    // 4) Tránh vòng lặp ngay chính /login
    if (path.equals("/login")) { chain.doFilter(req, res); return; }

    // 5) AJAX => 401 thay vì redirect
    if ("XMLHttpRequest".equalsIgnoreCase(r.getHeader("X-Requested-With"))) {
      w.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      w.setHeader("X-Auth-Required", "1");
      return;
    }

    // 6) Ghi nhớ URL cũ để quay lại (tuỳ chọn)
    String q = r.getQueryString();
    r.getSession(true).setAttribute("redirectAfterLogin", path + (q != null ? "?" + q : ""));

    // 7) Redirect tới /login của LoginServlet
    w.sendRedirect(ctx + "/login");
  }
private boolean isPublic(String path) {
  // Welcome & auth
  if (path.equals("/") || path.equals("/index.jsp")) return true;
  if (path.equals("/login") || path.equals("/logout")) return true;

  // 👉 Thêm forgot vào public (GET & POST đều qua đây)
  if (path.equals("/forgot") || path.equals("/reset-password") || path.equals("/register")) return true;

  // Static
  if (path.startsWith("/assets/") || path.startsWith("/static/") ||
      path.startsWith("/public/") || path.startsWith("/css/") ||
      path.startsWith("/js/") || path.startsWith("/img/") ||
      path.startsWith("/images/") ||
      path.endsWith(".css") || path.endsWith(".js") || path.endsWith(".png") ||
      path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".gif") ||
      path.endsWith(".svg") || path.endsWith(".ico") || path.endsWith(".webp")) {
    return true;
  }
  return false;
}

}
