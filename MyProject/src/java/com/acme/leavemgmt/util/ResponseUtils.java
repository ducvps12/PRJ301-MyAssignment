package com.acme.leavemgmt.util;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class ResponseUtils {
  private ResponseUtils(){}

  public static void json(HttpServletResponse resp, int code, String body) throws IOException {
    resp.setStatus(code);
    resp.setContentType("application/json; charset=UTF-8");
    resp.getWriter().write(body==null?"{}":body);
  }

  public static void text(HttpServletResponse resp, int code, String body) throws IOException {
    resp.setStatus(code);
    resp.setContentType("text/plain; charset=UTF-8");
    resp.getWriter().write(body==null?"":body);
  }
}
