// src/main/java/com/acme/leavemgmt/util/AppConfig.java
package com.acme.leavemgmt.util;

import com.acme.leavemgmt.dao.SysSettingDAO;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

public final class AppConfig {

  private static final Properties P = new Properties();
  private static volatile boolean LOADED = false;

  private AppConfig(){}

  /* ===== Public getters ===== */

  /** Ưu tiên: -Dprop > ENV/ENV_UPPER > DB(Sys_Settings) > app/mail.properties */
  public static String get(String key) { return get(key, null); }

  public static String get(String key, String defVal){
    // 1) JVM -D
    String v = System.getProperty(key);
    if (v != null && !v.isEmpty()) return v;

    // 2) ENV
    v = fromEnv(key);
    if (v != null && !v.isEmpty()) return v;

    // 3) DB Sys_Settings
    v = getFromDb(key);
    if (v != null && !v.isEmpty()) return v;

    // 4) app.properties / mail.properties
    ensureLoaded();
    v = P.getProperty(key);
    return (v == null || v.isEmpty()) ? defVal : v;
  }

  /** Alias để code cũ dùng str(...) vẫn ok. */
  public static String str(String key) { return get(key, null); }

  public static boolean getBool(String key, boolean defVal){
    String v = get(key);
    if (v == null) return defVal;
    v = v.trim();
    return "1".equals(v) || "true".equalsIgnoreCase(v) || "yes".equalsIgnoreCase(v);
  }

  public static int getInt(String key, int defVal){
    try { return Integer.parseInt(String.valueOf(get(key))); }
    catch (Exception ignore){ return defVal; }
  }

  /* ===== Convenience ===== */
  public static String baseUrl() { return get("APP_BASE_URL", "http://localhost:8080"); }
  public static String telegramBotToken(){ return get("TELEGRAM_BOT_TOKEN", ""); }
  public static String telegramDefaultChatId(){ return get("TELEGRAM_CHAT_ID", ""); }
  public static String googleClientId(){ return get("GOOGLE_CLIENT_ID", ""); }
  public static String googleClientSecret(){ return get("GOOGLE_CLIENT_SECRET", ""); }
  public static String googleRedirectUri(){ return get("GOOGLE_REDIRECT_URI", baseUrl()+"/oauth/google/callback"); }

  public static boolean isMailEnabled(){
    String v = get("mail.enabled");
    if (v == null || v.isBlank()) v = get("mail_enabled");
    if (v == null) return true;
    return "1".equals(v) || "true".equalsIgnoreCase(v) || "yes".equalsIgnoreCase(v);
  }

  /* ===== Internals ===== */

  private static void ensureLoaded(){
    if (LOADED) return;
    synchronized (AppConfig.class){
      if (LOADED) return;
      loadProps("app.properties");
      loadProps("mail.properties");
      LOADED = true;
    }
  }

  private static void loadProps(String name){
    try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream(name)) {
      if (in != null) P.load(in);
    } catch (Exception ignore) {}
  }

  private static String fromEnv(String key){
    String v = System.getenv(key);
    if (v == null || v.isEmpty()) {
      String alt = key.toUpperCase(Locale.ROOT).replace('.', '_'); // smtp.host -> SMTP_HOST
      v = System.getenv(alt);
    }
    return v;
  }

  private static String getFromDb(String key){
    try {
      SysSettingDAO dao = new SysSettingDAO();
      String dbKey = mapToDbKey(key);
      String v = dao.get(dbKey);
      if ((v == null || v.isBlank()) && "mail.from".equals(key)) v = dao.get(SysSettingDAO.K_MAIL_USERNAME);
      if ((v == null || v.isBlank()) && "mail.fromName".equals(key)) v = dao.get(SysSettingDAO.K_SITE_NAME);
      return (v == null || v.isBlank()) ? null : v.trim();
    } catch (Exception ignore){ return null; }
  }

  private static String mapToDbKey(String key){
    if (key == null) return null;
    switch (key) {
      case "smtp.host":     return SysSettingDAO.K_MAIL_HOST;
      case "smtp.port":     return SysSettingDAO.K_MAIL_PORT;
      case "smtp.user":     return SysSettingDAO.K_MAIL_USERNAME;
      case "smtp.pass":     return SysSettingDAO.K_MAIL_PASSWORD;
      case "smtp.starttls": return SysSettingDAO.K_MAIL_STARTTLS;
      case "mail.from":     return SysSettingDAO.K_MAIL_FROM;
      case "mail.fromName": return SysSettingDAO.K_MAIL_FROM_NAME;
      case "mail.enabled":  return SysSettingDAO.K_MAIL_ENABLED;
      case "brand.name":    return SysSettingDAO.K_SITE_NAME;
      default: return key;
    }
  }
}
