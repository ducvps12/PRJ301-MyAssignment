// com/acme/leavemgmt/web/Utf8Filter.java
package com.acme.leavemgmt.web;
import jakarta.servlet.*;
import java.io.IOException;

public class Utf8Filter implements Filter {
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {
    if (req.getCharacterEncoding() == null) req.setCharacterEncoding("UTF-8");
    res.setCharacterEncoding("UTF-8");
    chain.doFilter(req, res);
  }
}
