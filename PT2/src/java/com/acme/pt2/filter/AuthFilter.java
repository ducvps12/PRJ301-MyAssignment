package com.acme.pt2.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebFilter(filterName = "AuthFilter", urlPatterns = {"/auth/*"})
public class AuthFilter implements Filter {
  private String sessionAttr = "currentUser";
  private String loginPage   = "/login.jsp"; // đổi nếu bạn dùng page khác

  @Override public void init(FilterConfig cfg) {
    if (cfg.getInitParameter("sessionAttr") != null)
      sessionAttr = cfg.getInitParameter("sessionAttr");
    if (cfg.getInitParameter("loginPage") != null)
      loginPage = cfg.getInitParameter("loginPage");
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest req  = (HttpServletRequest) request;
    HttpServletResponse resp = (HttpServletResponse) response;

    HttpSession s = req.getSession(false);
    boolean loggedIn = s != null && s.getAttribute(sessionAttr) != null;

    if (!loggedIn) {
      String next = req.getRequestURI() +
                    (req.getQueryString() != null ? "?" + req.getQueryString() : "");
      String toLogin = req.getContextPath() + loginPage +
                       "?next=" + URLEncoder.encode(next, StandardCharsets.UTF_8);

      String accept = req.getHeader("Accept");
      boolean api = "XMLHttpRequest".equalsIgnoreCase(req.getHeader("X-Requested-With")) ||
                    (accept != null && accept.contains("application/json"));

      if (api) {
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write("{\"error\":\"unauthenticated\"}");
      } else {
        resp.sendRedirect(toLogin);
      }
      return;
    }
    chain.doFilter(request, response);
  }
}
