package com.acme.leavemgmt.util;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class GoogleOAuthHelper {
  private GoogleOAuthHelper(){}

  public static String buildAuthUrl(String state){
    String base = "https://accounts.google.com/o/oauth2/v2/auth";
    Map<String,String> q = new LinkedHashMap<>();
    q.put("client_id", ConfigProps.get("GOOGLE_CLIENT_ID",""));
    q.put("redirect_uri", ConfigProps.get("GOOGLE_REDIRECT_URI",
            ConfigProps.get("APP_BASE_URL","http://localhost:8080/MyProject") + "/oauth/google/callback"));
    q.put("response_type", "code");
    q.put("scope", "openid email profile");
    q.put("access_type", "offline");
    q.put("include_granted_scopes", "true");
    if (state != null) q.put("state", state);
    return base + "?" + toQuery(q);
  }

  public static Map<String,Object> exchangeCodeForTokens(String code) throws IOException {
    String tokenUrl = "https://oauth2.googleapis.com/token";
    Map<String,String> form = Map.of(
      "code", code,
      "client_id", ConfigProps.get("GOOGLE_CLIENT_ID",""),
      "client_secret", ConfigProps.get("GOOGLE_CLIENT_SECRET",""),
      "redirect_uri", ConfigProps.get("GOOGLE_REDIRECT_URI",
              ConfigProps.get("APP_BASE_URL","http://localhost:8080/MyProject") + "/oauth/google/callback"),
      "grant_type", "authorization_code"
    );
    String resp = postForm(tokenUrl, form);
    return parseJsonObject(resp);
  }

  public static Map<String,Object> refreshAccessToken(String refreshToken) throws IOException {
    String tokenUrl = "https://oauth2.googleapis.com/token";
    Map<String,String> form = Map.of(
      "refresh_token", refreshToken,
      "client_id", ConfigProps.get("GOOGLE_CLIENT_ID",""),
      "client_secret", ConfigProps.get("GOOGLE_CLIENT_SECRET",""),
      "grant_type", "refresh_token"
    );
    String resp = postForm(tokenUrl, form);
    return parseJsonObject(resp);
  }

  // Decode id_token (JWT) payload KHÔNG xác minh chữ ký (demo)
  public static Map<String,Object> unsafeDecodeIdToken(String idToken){
    try{
      String[] parts = idToken.split("\\.");
      if (parts.length < 2) return Map.of();
      String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
      return parseJsonObject(payload);
    }catch(Exception e){ return Map.of(); }
  }

  // ---------- helpers ----------
  private static String toQuery(Map<String,String> q){
    StringBuilder b = new StringBuilder();
    boolean first=true;
    for (var e: q.entrySet()){
      if (!first) b.append("&"); first=false;
      b.append(URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8))
       .append("=")
       .append(URLEncoder.encode(Objects.toString(e.getValue(),""), StandardCharsets.UTF_8));
    }
    return b.toString();
  }

  private static String postForm(String url, Map<String,String> form) throws IOException {
    byte[] body = toQuery(form).getBytes(StandardCharsets.UTF_8);
    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
    conn.setRequestMethod("POST");
    conn.setDoOutput(true);
    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
    conn.setFixedLengthStreamingMode(body.length);
    try(OutputStream os = conn.getOutputStream()){ os.write(body); }
    try(InputStream is = (conn.getResponseCode()>=200 && conn.getResponseCode()<300)
        ? conn.getInputStream() : conn.getErrorStream()){
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  // JSON parser rất tối giản (đủ cho token JSON phẳng của Google)
  private static Map<String,Object> parseJsonObject(String json){
    Map<String,Object> map = new LinkedHashMap<>();
    if (json == null) return map;
    String s = json.trim();
    if (s.startsWith("{")) s = s.substring(1);
    if (s.endsWith("}")) s = s.substring(0, s.length()-1);
    // tách theo dấu phẩy ngoài chuỗi
    boolean inStr=false; StringBuilder buf=new StringBuilder();
    java.util.List<String> pairs=new ArrayList<>();
    for (int i=0;i<s.length();i++){
      char c=s.charAt(i);
      if (c=='"' && (i==0 || s.charAt(i-1)!='\\')) inStr=!inStr;
      if (c==',' && !inStr){ pairs.add(buf.toString()); buf.setLength(0); }
      else buf.append(c);
    }
    if (buf.length()>0) pairs.add(buf.toString());

    for (String p: pairs){
      int idx = indexOfColonOutsideString(p);
      if (idx<0) continue;
      String key = unquote(p.substring(0,idx).trim());
      String val = p.substring(idx+1).trim();
      map.put(key, parseJsonValue(val));
    }
    return map;
  }

  private static int indexOfColonOutsideString(String s){
    boolean inStr=false;
    for (int i=0;i<s.length();i++){
      char c=s.charAt(i);
      if (c=='"' && (i==0 || s.charAt(i-1)!='\\')) inStr=!inStr;
      if (c==':' && !inStr) return i;
    }
    return -1;
  }

  private static Object parseJsonValue(String v){
    v = v.trim();
    if (v.startsWith("\"")) return unquote(v);
    if ("true".equals(v)) return Boolean.TRUE;
    if ("false".equals(v)) return Boolean.FALSE;
    if ("null".equals(v)) return null;
    try { if (v.contains(".")) return Double.parseDouble(v); else return Long.parseLong(v); }
    catch(Exception ignore){ return v; }
  }

  private static String unquote(String s){
    s = s.trim();
    if (s.startsWith("\"")) s = s.substring(1);
    if (s.endsWith("\"")) s = s.substring(0, s.length()-1);
    return s.replace("\\\"", "\"").replace("\\\\","\\").replace("\\/","/");
  }
}
