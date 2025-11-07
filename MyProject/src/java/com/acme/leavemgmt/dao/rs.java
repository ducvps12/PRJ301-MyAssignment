package com.acme.leavemgmt.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;

/** ResultSet helpers: đọc null-safe và convert kiểu. */
public final class Rs {
  private Rs(){}

  public static Integer getIntObj(ResultSet rs, String col) throws SQLException {
    int v = rs.getInt(col);
    return rs.wasNull() ? null : v;
  }

  public static Long getLongObj(ResultSet rs, String col) throws SQLException {
    long v = rs.getLong(col);
    return rs.wasNull() ? null : v;
    }

  public static String getStr(ResultSet rs, String col) throws SQLException {
    String s = rs.getString(col);
    return s == null ? null : s.trim();
  }

  public static LocalDate getDate(ResultSet rs, String col) throws SQLException {
    java.sql.Date d = rs.getDate(col);
    return d == null ? null : d.toLocalDate();
  }

  public static java.util.Date getTimestampAsUtilDate(ResultSet rs, String col) throws SQLException {
    Timestamp t = rs.getTimestamp(col);
    return t == null ? null : new java.util.Date(t.getTime());
  }
}
