/*
 * RoleFilter – chặn truy cập theo vai trò
 * Áp dụng cho: /admin[/...], /request/approve[/...]
 */
package com.acme.leavemgmt.filter;

import com.acme.leavemgmt.model.User;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter(
  filterName = "RoleFilter",
  urlPatterns = {
      "/admin", "/admin/*",
      "/request/approve", "/request/approve/*"
  }
)
public class RoleFilter implements Filter {

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest r = (HttpServletRequest) req;
    HttpServletResponse w = (HttpServletResponse) res;

    // Cho phép preflight
    if ("OPTIONS".equalsIgnoreCase(r.getMethod())) {
      chain.doFilter(req, res);
      return;
    }

    // ==== 1. Kiểm tra đăng nhập ====
    HttpSession session = r.getSession(false);
    User me = (session != null) ? (User) session.getAttribute("currentUser") : null;

    if (me == null) {
      w.sendRedirect(r.getContextPath() + "/login");
      return;
    }

    // ==== 2. Xác định role ====
    String role = (me.getRole() == null ? "" : me.getRole()).toUpperCase();
    String path = r.getRequestURI().substring(r.getContextPath().length());

    boolean isLead = role.endsWith("_LEAD") || role.endsWith("_LEADER");

    // ==== 3. Phân quyền ====
    boolean allowed = true;

    if ("/admin".equals(path) || path.startsWith("/admin/")) {
      // Dashboard: ADMIN + LEAD
      if (!(role.equals("ADMIN") || isLead)) allowed = false;

      // /admin/users chỉ ADMIN
      if (path.startsWith("/admin/users") && !role.equals("ADMIN")) allowed = false;
    }

    if ("/request/approve".equals(path) || path.startsWith("/request/approve/")) {
      // Duyệt đơn: ADMIN + LEAD
      if (!(role.equals("ADMIN") || isLead)) allowed = false;
    }

    // ==== 4. Hành động khi không có quyền ====
    if (!allowed) {
      // Ghi log nhẹ để debug (tùy chọn)
      System.out.printf("[RoleFilter] ACCESS DENIED: %s -> %s%n", me, path);

      try {
        // Nếu có trang lỗi riêng
        r.setAttribute("code", 403);
        r.getRequestDispatcher("/WEB-INF/views/errors/403.jsp").forward(r, w);
      } catch (Exception ex) {
        w.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập trang này.");
      }
      return;
    }

    // ==== 5. Cho qua ====
    chain.doFilter(req, res);
  }

  @Override public void init(FilterConfig filterConfig) {}
  @Override public void destroy() {}
}
