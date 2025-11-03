package com.acme.leavemgmt.util;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

public final class HtmlUtils {
  private HtmlUtils(){}

  public static String escape(String s){
    if (s == null) return "";
    return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")
            .replace("\"","&quot;").replace("'","&#39;");
  }

  // Status -> badge HTML (đưa thẳng vào JSP)
  public static String statusBadge(String status){
    if (status == null) status = "";
    String css = switch (status.toLowerCase()){
      case "approved"  -> "ok";
      case "rejected"  -> "no";
      case "cancelled" -> "muted";
      default          -> "warn"; // pending/inprogress
    };
    return "<span class='badge badge-"+css+"'>" + escape(status) + "</span>";
  }

  public static String buildUrl(HttpServletRequest req, String path){
    String ctx = req.getContextPath();
    if (!path.startsWith("/")) path = "/" + path;
    return ctx + path;
  }

  public static String buildUrlWithParams(String base, Map<String,String> params){
    if (params == null || params.isEmpty()) return base;
    String q = params.entrySet().stream()
      .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "=" +
                URLEncoder.encode(e.getValue()==null?"":e.getValue(), StandardCharsets.UTF_8))
      .collect(Collectors.joining("&"));
    return base + (base.contains("?") ? "&" : "?") + q;
  }

  // CSRF hidden input (tận dụng util Csrf của bạn)
  public static String csrfHidden(HttpServletRequest req){
    String token = Csrf.token(req); // bạn đã có com.acme.leavemgmt.util.Csrf
    return "<input type='hidden' name='_csrf' value='"+escape(token)+"'/>";
  }
}
