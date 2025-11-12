package com.acme.leavemgmt.util;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;

/**
 * Google OAuth / OIDC helper (no external libs).
 *
 * Public API:
 *  - buildAuthUrl(clientId, redirectUri, allowedHd, state, nonce)
 *  - exchangeCodeForTokens(clientId, clientSecret, redirectUri, code) -> Map<String,String>
 *  - verifyIdToken(idToken) -> Map<String,String>
 *  - buildAuthUrl(state) / exchangeCodeForTokens(code) dùng ConfigProps (tuỳ chọn)
 *  - refreshAccessToken(refreshToken)  (tuỳ chọn)
 *  - unsafeDecodeIdToken(idToken)      (debug)
 */
public final class GoogleOAuthHelper {

    private GoogleOAuthHelper() {}

    /* ===================== Config ===================== */
    private static final String UA = "LeaveMgmt/1.0 (+https://example.com)";
    private static final int CONNECT_TIMEOUT = 10_000;
    private static final int READ_TIMEOUT    = 10_000;

    /* ===================== Public API ===================== */

    /** Xây URL cho bước /oauth2/v2/auth */
    public static String buildAuthUrl(String clientId,
                                      String redirectUri,
                                      String allowedHd,
                                      String state,
                                      String nonce) {
        String base = "https://accounts.google.com/o/oauth2/v2/auth";
        Map<String,String> q = new LinkedHashMap<>();
        q.put("client_id", nz(clientId));
        q.put("redirect_uri", nz(redirectUri));
        q.put("response_type", "code");
        q.put("scope", "openid email profile");
        q.put("access_type", "online");                // đủ dùng cho đăng nhập
        q.put("include_granted_scopes", "true");
        if (!isBlank(state)) q.put("state", state);
        if (!isBlank(nonce)) q.put("nonce", nonce);
        if (!isBlank(allowedHd)) q.put("hd", allowedHd.trim());
        return base + "?" + toQuery(q);
    }

    /** Đổi authorization code lấy access_token + id_token (trả về Map đơn giản) */
    public static Map<String,String> exchangeCodeForTokens(String clientId,
                                                           String clientSecret,
                                                           String redirectUri,
                                                           String code) throws IOException {
        String tokenUrl = "https://oauth2.googleapis.com/token";
        Map<String,String> form = new LinkedHashMap<>();
        form.put("code", nz(code));
        form.put("client_id", nz(clientId));
        form.put("client_secret", nz(clientSecret));
        form.put("redirect_uri", nz(redirectUri));
        form.put("grant_type", "authorization_code");

        String resp = postForm(tokenUrl, form);
        Map<String,String> out = parseJsonStringMap(resp);

        // Chuẩn hoá message lỗi (nếu có)
        if (out.containsKey("error") && !out.containsKey("error_description")) {
            out.put("error_description", out.get("error"));
        }
        return out;
    }

    /**
     * Xác minh id_token bằng Google tokeninfo.
     * Trả Map<String,String> chứa claims: iss, aud, email, email_verified, sub, hd, name, picture, exp, ...
     * Lưu ý: tokeninfo xác minh chữ ký phía Google. Bạn có thể kiểm 'aud' theo clientId nếu muốn.
     */
    public static Map<String,String> verifyIdToken(String idToken) throws IOException {
        String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" +
                URLEncoder.encode(nz(idToken), StandardCharsets.UTF_8);
        String json = httpGet(url);
        Map<String,String> m = parseJsonStringMap(json);

        // chuẩn hoá boolean
        if (m.containsKey("email_verified")) {
            m.put("email_verified", String.valueOf("true".equalsIgnoreCase(m.get("email_verified"))));
        }
        return m;
    }

    /* ===================== Overloads dùng ConfigProps (tuỳ chọn) ===================== */

    public static String buildAuthUrl(String state){
        String cb = ConfigProps.get(
                "GOOGLE_REDIRECT_URI",
                ConfigProps.get("APP_BASE_URL","http://localhost/MyProject") + "/oauth/google/callback"
        );
        return buildAuthUrl(
                ConfigProps.get("GOOGLE_CLIENT_ID",""),
                cb,
                ConfigProps.get("GOOGLE_ALLOWED_HD",""),
                state,
                randomNonce()
        );
    }

    public static Map<String,Object> exchangeCodeForTokens(String code) throws IOException {
        Map<String,String> m = exchangeCodeForTokens(
                ConfigProps.get("GOOGLE_CLIENT_ID",""),
                ConfigProps.get("GOOGLE_CLIENT_SECRET",""),
                ConfigProps.get("GOOGLE_REDIRECT_URI",
                        ConfigProps.get("APP_BASE_URL","http://localhost/MyProject") + "/oauth/google/callback"),
                code
        );
        return new LinkedHashMap<>(m); // giữ compatibility với code cũ (Map<String,Object>)
    }

    public static Map<String,Object> refreshAccessToken(String refreshToken) throws IOException {
        String tokenUrl = "https://oauth2.googleapis.com/token";
        Map<String,String> form = Map.of(
                "refresh_token", nz(refreshToken),
                "client_id", ConfigProps.get("GOOGLE_CLIENT_ID",""),
                "client_secret", ConfigProps.get("GOOGLE_CLIENT_SECRET",""),
                "grant_type", "refresh_token"
        );
        String resp = postForm(tokenUrl, form);
        return parseJsonObject(resp);
    }

    /** Decode payload (không verify) – chỉ để debug */
    public static Map<String,Object> unsafeDecodeIdToken(String idToken){
        try{
            String[] parts = nz(idToken).split("\\.");
            if (parts.length < 2) return Map.of();
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            return parseJsonObject(payload);
        }catch(Exception e){ return Map.of(); }
    }

    /* ===================== Helpers: HTTP ===================== */

    private static String httpGet(String url) throws IOException {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setRequestMethod("GET");
        c.setRequestProperty("User-Agent", UA);
        c.setConnectTimeout(CONNECT_TIMEOUT);
        c.setReadTimeout(READ_TIMEOUT);
        int code = c.getResponseCode();
        try (InputStream is = (code >= 200 && code < 300) ? c.getInputStream() : c.getErrorStream()) {
            String body = (is != null) ? new String(is.readAllBytes(), StandardCharsets.UTF_8) : "";
            if (code >= 200 && code < 300) return body;
            // trả về JSON lỗi đồng nhất để parse
            return "{\"error\":\"HTTP_" + code + "\",\"error_description\":" + quote(body) + "}";
        }
    }

    private static String postForm(String url, Map<String,String> form) throws IOException {
        byte[] body = toQuery(form).getBytes(StandardCharsets.UTF_8);
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("User-Agent", UA);
        conn.setDoOutput(true);
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setFixedLengthStreamingMode(body.length);
        try(OutputStream os = conn.getOutputStream()){ os.write(body); }
        int code = conn.getResponseCode();
        try(InputStream is = (code>=200 && code<300) ? conn.getInputStream() : conn.getErrorStream()){
            String resp = (is != null) ? new String(is.readAllBytes(), StandardCharsets.UTF_8) : "";
            if (code>=200 && code<300) return resp;
            return "{\"error\":\"HTTP_" + code + "\",\"error_description\":" + quote(resp) + "}";
        }
    }

    /* ===================== Helpers: Strings / Query ===================== */

    private static String toQuery(Map<String,String> q){
        StringBuilder b = new StringBuilder();
        boolean first = true;
        for (var e: q.entrySet()){
            if (!first) b.append("&"); first = false;
            b.append(URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8))
             .append("=")
             .append(URLEncoder.encode(Objects.toString(e.getValue(),""), StandardCharsets.UTF_8));
        }
        return b.toString();
    }
    private static String nz(String s){ return s==null? "": s; }
    private static boolean isBlank(String s){ return s==null || s.trim().isEmpty(); }
    private static String quote(String s){
        if (s == null) return "\"\"";
        return "\"" + s.replace("\\","\\\\").replace("\"","\\\"") + "\"";
    }

    /* ===================== Helpers: JSON (đủ dùng) ===================== */

    // Map<String,Object>
    private static Map<String,Object> parseJsonObject(String json){
        Map<String,Object> map = new LinkedHashMap<>();
        if (json == null) return map;
        String s = json.trim();
        if (s.startsWith("{")) s = s.substring(1);
        if (s.endsWith("}")) s = s.substring(0, s.length()-1);

        boolean inStr=false; StringBuilder buf=new StringBuilder();
        List<String> pairs=new ArrayList<>();
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

    // Map<String,String> – phục vụ servlet
    private static Map<String,String> parseJsonStringMap(String json){
        Map<String,Object> any = parseJsonObject(json);
        Map<String,String> out = new LinkedHashMap<>();
        for (var e: any.entrySet()) {
            out.put(e.getKey(), Objects.toString(e.getValue(), null));
        }
        return out;
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
        if ("true".equalsIgnoreCase(v))  return Boolean.TRUE;
        if ("false".equalsIgnoreCase(v)) return Boolean.FALSE;
        if ("null".equalsIgnoreCase(v))  return null;
        try { return v.contains(".") ? Double.parseDouble(v) : Long.parseLong(v); }
        catch(Exception ignore){ return v; }
    }

    private static String unquote(String s){
        s = s.trim();
        if (s.startsWith("\"")) s = s.substring(1);
        if (s.endsWith("\"")) s = s.substring(0, s.length()-1);
        return s.replace("\\\"", "\"").replace("\\\\","\\").replace("\\/","/");
    }

    private static String randomNonce() {
        byte[] b = new byte[16];
        new SecureRandom().nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }
}
