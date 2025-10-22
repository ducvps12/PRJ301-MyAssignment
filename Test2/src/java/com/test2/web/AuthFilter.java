package com.test2.web;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebFilter(filterName="AuthFilter", urlPatterns="/create")
public class AuthFilter implements Filter {
  @Override public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest r = (HttpServletRequest) req;
    if (r.getSession().getAttribute("currentUser") == null) {
      r.setAttribute("error", "access denied");
      r.getRequestDispatcher("/WEB-INF/access-denied.jsp").forward(req, res);
      return;
    }
    chain.doFilter(req, res);
  }
}
