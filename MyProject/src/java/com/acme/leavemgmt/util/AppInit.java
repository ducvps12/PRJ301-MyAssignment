package com.acme.leavemgmt.util;

import jakarta.servlet.ServletContext;
import javax.naming.InitialContext;
import javax.sql.DataSource;

/** Giữ DS dùng chung cho toàn app. */
public final class AppInit {
  private static volatile DataSource DS;

  private AppInit(){}

  /** Gọi lúc startup (ContextListener) để gán DS nếu có để sẵn trong ServletContext. */
  public static void boot(ServletContext ctx){
    if (DS != null) return;
    Object v = ctx.getAttribute("DS");
    if (v instanceof DataSource) { DS = (DataSource) v; return; }
    try {
      InitialContext ic = new InitialContext();
      DS = (DataSource) ic.lookup("java:comp/env/jdbc/LeaveDB");
    } catch (Exception ignore) {}
  }

  public static DataSource getDataSource(){ return DS; }
}
