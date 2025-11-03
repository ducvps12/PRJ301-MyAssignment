package com.acme.leavemgmt.util;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ConfigProps {
  private static final Properties BASE = new Properties();
  private static final Properties LOCAL = new Properties();
  private static final Pattern VAR = Pattern.compile("\\$\\{([A-Za-z0-9_\\.\\-]+)}");

  static {
    // 1) Load app.properties
    loadClasspathProps(BASE, "app.properties");
    // 2) Load app.local.properties (override)
    loadClasspathProps(LOCAL, "app.local.properties");

    // 3) Resolve ${VAR} placeholders (in each file)
    resolvePlaceholders(BASE, key -> pickRaw(key, null));
    resolvePlaceholders(LOCAL, key -> pickRaw(key, null));
  }

  private ConfigProps() {}

  private static void loadClasspathProps(Properties p, String name){
    try (InputStream is = ConfigProps.class.getClassLoader().getResourceAsStream(name)) {
      if (is != null) {
        p.load(new java.io.InputStreamReader(is, StandardCharsets.UTF_8));
      }
    } catch (Exception e) {
      // ignore
    }
  }

  // Get with default
  public static String get(String key, String defVal) {
    // Priority: JVM (-D) / ENV -> app.local.properties -> app.properties -> defVal
    String v = pickRaw(key, null);
    return (v == null || v.isBlank()) ? defVal : v;
  }

  // Get required (throw if missing)
  public static String getRequired(String key) {
    String v = get(key, null);
    if (v == null || v.isBlank()) {
      throw new IllegalStateException("Missing config: " + key);
    }
    return v;
  }

  // --------- internals ---------
  private static String pickRaw(String key, String defVal){
    // JVM props
    String v = System.getProperty(key);
    if (v != null && !v.isBlank()) return v;

    // ENV (support both EXACT and with dots replaced by underscores)
    v = System.getenv(key);
    if (v != null && !v.isBlank()) return v;
    v = System.getenv(key.replace('.', '_'));
    if (v != null && !v.isBlank()) return v;

    // local -> base
    v = LOCAL.getProperty(key);
    if (v != null && !v.isBlank()) return v;

    v = BASE.getProperty(key);
    if (v != null && !v.isBlank()) return v;

    return defVal;
  }

  private static void resolvePlaceholders(Properties p, java.util.function.Function<String,String> resolver){
    for (String k : p.stringPropertyNames()){
      String raw = p.getProperty(k);
      p.setProperty(k, resolveValue(raw, resolver));
    }
  }

  private static String resolveValue(String val, java.util.function.Function<String,String> resolver){
    if (val == null) return null;
    String result = val;
    Matcher m = VAR.matcher(result);
    StringBuffer sb = new StringBuffer();
    while (m.find()){
      String name = m.group(1);
      String rep = resolver.apply(name);
      if (rep == null) rep = ""; // unresolved -> empty
      m.appendReplacement(sb, Matcher.quoteReplacement(rep));
    }
    m.appendTail(sb);
    return sb.toString();
  }
}
