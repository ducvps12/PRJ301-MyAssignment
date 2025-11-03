package com.acme.leavemgmt.util;

import java.util.Optional;

public final class AppConfig {
  private AppConfig(){}

  public static String get(String key, String defVal){
    String v = System.getProperty(key);
    if (v == null || v.isEmpty()) v = System.getenv(key);
    return (v == null || v.isEmpty()) ? defVal : v;
  }

  // ==== Common keys ====
  public static String baseUrl() { return get("APP_BASE_URL", "http://localhost:8080"); }

  // Telegram
  public static String telegramBotToken(){ return get("TELEGRAM_BOT_TOKEN", ""); }
  public static String telegramDefaultChatId(){ return get("TELEGRAM_CHAT_ID", ""); } // optional

  // Google OAuth (server-side)
  public static String googleClientId(){ return get("GOOGLE_CLIENT_ID", ""); }
  public static String googleClientSecret(){ return get("GOOGLE_CLIENT_SECRET", ""); }
  public static String googleRedirectUri(){ return get("GOOGLE_REDIRECT_URI", baseUrl()+"/oauth/google/callback"); }
}
