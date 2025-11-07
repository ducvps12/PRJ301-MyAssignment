package com.acme.leavemgmt.dao;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import jakarta.servlet.ServletContext;

/** Quản lý DataSource “dùng chung” cho toàn app. */
public final class DataSourceProvider {
  private static volatile DataSource DS; // optional

  private DataSourceProvider(){}

  /** Đăng ký thủ công (ví dụ trong AppInit). */
  public static void set(DataSource ds) { DS = ds; }

  /** Lấy DS đã set (có thể null). */
  public static DataSource get() { return DS; }

  /** Thử resolve theo thứ tự: attr "DS" → JNDI java:comp/env/jdbc/LeaveDB. */
  public static DataSource resolve(ServletContext ctx) {
    if (DS != null) return DS;
    if (ctx != null) {
      Object v = ctx.getAttribute("DS");
      if (v instanceof DataSource) {
        DS = (DataSource) v;
        return DS;
      }
    }
    try {
      DS = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/LeaveDB");
    } catch (NamingException ignore) {}
    return DS;
  }
}
