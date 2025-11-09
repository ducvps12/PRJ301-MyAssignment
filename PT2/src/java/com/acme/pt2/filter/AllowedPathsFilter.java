package com.acme.pt2.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@WebFilter(filterName = "AllowedPathsFilter", urlPatterns = {"/*"})
public class AllowedPathsFilter implements Filter {
  private final Set<String> exact = new HashSet<>();
  private final List<String> prefix = new ArrayList<>();
  private String allowFile = "/allowed_access.txt"; // nằm ở web root

  @Override public void init(FilterConfig cfg) throws ServletException {
    if (cfg.getInitParameter("allowFile") != null)
      allowFile = cfg.getInitParameter("allowFile");

    ServletContext ctx = cfg.getServletContext();
    try (InputStream in = ctx.getResourceAsStream(allowFile)) {
      if (in == null) throw new ServletException("Not found: " + allowFile);
      try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
        String line;
        while ((line = br.readLine()) != null) {
          line = line.trim();
          if (line.isEmpty() || line.startsWith("#")) continue;
          if (line.endsWith("/*")) {
            prefix.add(line.substring(0, line.length() - 1)); // giữ dấu '/'
          } else {
            exact.add(line);
          }
        }
      }
    } catch (IOException e) {
      throw new ServletException(e);
    }
  }

  private boolean allowed(String path) {
    if (exact.contains(path)) return true;
    for (String p : prefix) if (path.startsWith(p)) return true;
    return false;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse resp = (HttpServletResponse) response;

    String path = req.getRequestURI().substring(req.getContextPath().length());
    if (path.isEmpty()) path = "/";

    if (allowed(path)) {
      chain.doFilter(request, response);
    } else {
      resp.sendError(HttpServletResponse.SC_FORBIDDEN); // 403
    }
  }
}
