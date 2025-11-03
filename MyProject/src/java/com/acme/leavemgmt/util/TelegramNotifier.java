package com.acme.leavemgmt.util;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public final class TelegramNotifier {
  private TelegramNotifier(){}

  public static boolean sendMessage(String chatId, String text){
    String token = AppConfig.telegramBotToken();
    if (token == null || token.isBlank()) return false;
    try {
      String api = "https://api.telegram.org/bot" + token + "/sendMessage";
      String data = "chat_id=" + URLEncoder.encode(chatId, StandardCharsets.UTF_8)
                  + "&text=" + URLEncoder.encode(text, StandardCharsets.UTF_8)
                  + "&parse_mode=HTML";
      byte[] post = data.getBytes(StandardCharsets.UTF_8);

      HttpURLConnection conn = (HttpURLConnection) new URL(api).openConnection();
      conn.setRequestMethod("POST");
      conn.setDoOutput(true);
      conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      conn.setFixedLengthStreamingMode(post.length);
      try(OutputStream os = conn.getOutputStream()){ os.write(post); }
      int code = conn.getResponseCode();
      return code >= 200 && code < 300;
    } catch (Exception e){
      e.printStackTrace();
      return false;
    }
  }

  public static boolean notifyDefault(String text){
    String chatId = AppConfig.telegramDefaultChatId();
    if (chatId == null || chatId.isBlank()) return false;
    return sendMessage(chatId, text);
  }
}
