package com.acme.pt2.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebFilter(urlPatterns = {"/auth/*"})
public class AuthFilter implements Filter {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest req  = (HttpServletRequest) request;
    HttpServletResponse resp = (HttpServletResponse) response;

    HttpSession session = req.getSession(false);
    Object currentUser = (session == null) ? null : session.getAttribute("currentUser"); // đổi tên attr nếu bạn dùng khác

    if (currentUser == null) {
      // Chưa đăng nhập → chuyển về trang login (kèm next để quay lại sau khi login)
      String next = URLEncoder.encode(getFullURL(req), StandardCharsets.UTF_8);
      resp.sendRedirect(req.getContextPath() + "/login?next=" + next);
      return;
    }

    chain.doFilter(request, response);
  }

  private String getFullURL(HttpServletRequest req) {
    String q = req.getQueryString();
    String url = req.getRequestURI();
    if (q != null && !q.isEmpty()) url += "?" + q;
    return url.substring(req.getContextPath().length());
  }
}
