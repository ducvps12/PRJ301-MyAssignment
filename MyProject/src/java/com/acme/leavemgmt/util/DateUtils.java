package com.acme.leavemgmt.util;

import java.time.*;
import java.time.format.DateTimeFormatter;

public final class DateUtils {
  private DateUtils(){}

  public static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  public static final DateTimeFormatter DMY_HM = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

  public static String fmt(LocalDate d){ return d == null ? "" : d.format(DMY); }
  public static String fmt(LocalDateTime dt){ return dt == null ? "" : dt.format(DMY_HM); }

  public static String humanRange(LocalDate start, LocalDate end){
    if (start == null && end == null) return "";
    if (start != null && end == null) return fmt(start) + " → ?";
    if (start == null) return "? → " + fmt(end);
    if (start.equals(end)) return fmt(start);
    return fmt(start) + " → " + fmt(end);
  }
}
