// com.acme.leavemgmt.util.WebUtil
package com.acme.leavemgmt.util;

import jakarta.servlet.http.HttpServletRequest;

public final class WebUtil {
  private WebUtil() {}
  public static String clientIp(HttpServletRequest r) {
    String[] hdrs = {"X-Forwarded-For","X-Real-IP","CF-Connecting-IP"};
    for (String h : hdrs) {
      String v = r.getHeader(h);
      if (v != null && !v.isBlank()) return v.split(",")[0].trim();
    }
    return r.getRemoteAddr();
  }
  public static String userAgent(HttpServletRequest r) {
    String ua = r.getHeader("User-Agent");
    return ua == null ? "" : (ua.length() > 255 ? ua.substring(0,255) : ua);
  }
}
