package com.acme.pt2.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@WebFilter("/*")
public class AllowedPathsFilter implements Filter {
  private Set<String> allowed; // chứa path chính xác hoặc tiền tố thư mục (kết thúc bằng '/')

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    ServletContext ctx = filterConfig.getServletContext();
    // Đọc từ WEB-INF để không bị lộ public
    try (InputStream is = ctx.getResourceAsStream("/WEB-INF/allowed_access.txt")) {
      if (is == null) throw new FileNotFoundException("WEB-INF/allowed_access.txt not found");
      try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
        allowed = br.lines()
            .map(String::trim)
            .filter(s -> !s.isEmpty() && !s.startsWith("#"))
            .collect(Collectors.toCollection(LinkedHashSet::new));
      }
    } catch (IOException e) {
      throw new ServletException("Cannot load allowed_access.txt", e);
    }
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest req  = (HttpServletRequest) request;
    HttpServletResponse resp = (HttpServletResponse) response;

    String ctxPath = req.getContextPath();
    String path = req.getRequestURI().substring(ctxPath.length()); // path bắt đầu bằng '/'

    // Mặc định servlet container đã chặn /WEB-INF và /META-INF, ta vẫn cho qua nếu là tài nguyên của chính app.
    // Kiểm tra whitelist: khớp chính xác hoặc là nằm trong một folder được whitelist (dòng kết thúc '/')
    if (isAllowed(path)) {
      chain.doFilter(request, response);
    } else {
      resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden by AllowedPathsFilter");
    }
  }

  private boolean isAllowed(String path) {
    if (allowed == null) return false;
    for (String rule : allowed) {
      if (rule.endsWith("/")) {
        // whitelist cả thư mục/prefix
        if (path.startsWith(rule)) return true;
      } else {
        if (path.equals(rule)) return true;
      }
    }
    return false;
  }
}
