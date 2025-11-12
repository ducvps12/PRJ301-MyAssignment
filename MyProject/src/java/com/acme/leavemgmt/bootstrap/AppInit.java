package com.acme.leavemgmt.bootstrap;

import com.acme.leavemgmt.dao.DeptDAO;
import com.acme.leavemgmt.dao.DivisionDAO;
import com.acme.leavemgmt.dao.RoleDAO;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Collections;
import java.util.List;

@WebListener
public class AppInit implements ServletContextListener {

  public static final String CTX_DS         = "DS";
  public static final String CTX_DEPT_DAO   = "deptDAO";
  public static final String CTX_DIV_DAO    = "divisionDAO";
  public static final String CTX_ROLE_DAO   = "roleDAO";
  public static final String CTX_DEPTS      = "APP_DEPTS";
  public static final String CTX_DIVISIONS  = "APP_DIVISIONS";
  public static final String CTX_ROLES      = "APP_ROLES";

  // Cache DS dùng chung cho toàn ứng dụng (fail-fast nếu chưa init)
  private static volatile DataSource CACHED_DS;

  /** Lấy DataSource đã init. Ưu tiên cache; fallback JNDI. */
  public static DataSource getDataSource() {
    DataSource ds = CACHED_DS;
    if (ds != null) return ds;
    try {
      ds = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/LeaveDB");
      if (ds == null) throw new IllegalStateException("JNDI jdbc/LeaveDB not found");
      CACHED_DS = ds; // cache cho lần sau
      return ds;
    } catch (Exception e) {
      throw new IllegalStateException("DataSource not initialized. Ensure AppInit ran and JNDI is configured.", e);
    }
  }

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    ServletContext ctx = sce.getServletContext();
    try {
      // 1) Lấy DataSource qua JNDI
      DataSource ds = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/LeaveDB");
      if (ds == null) throw new IllegalStateException("JNDI jdbc/LeaveDB not found");
      CACHED_DS = ds;                  // cache tĩnh cho getDataSource()
      ctx.setAttribute(CTX_DS, ds);    // và đặt vào context cho nơi khác cần

      // 2) Test kết nối (log URL phục vụ DEV)
      try (Connection c = ds.getConnection()) {
        String url = null;
        try { url = c.getMetaData().getURL(); } catch (Exception ignore) {}
        ctx.log("[AppInit] DataSource OK " + (url != null ? ("→ " + url) : ""));
      }

      // 3) Khởi tạo DAO dùng chung
      DeptDAO deptDAO     = new DeptDAO(ds);
      DivisionDAO divDAO  = new DivisionDAO(ds);
      RoleDAO roleDAO     = new RoleDAO(ds);

      ctx.setAttribute(CTX_DEPT_DAO, deptDAO);
      ctx.setAttribute(CTX_DIV_DAO,  divDAO);
      ctx.setAttribute(CTX_ROLE_DAO, roleDAO);

      // 4) Preload danh mục (null-safe nếu DB trống/lỗi)
      List<?> depts     = safe(ctx, "[AppInit] preload depts",     deptDAO::listAll);
      List<?> divisions = safe(ctx, "[AppInit] preload divisions", divDAO::listAll);
      List<?> roles     = safe(ctx, "[AppInit] preload roles",     roleDAO::listAll);

      ctx.setAttribute(CTX_DEPTS,     depts);
      ctx.setAttribute(CTX_DIVISIONS, divisions);
      ctx.setAttribute(CTX_ROLES,     roles);

      ctx.log("[AppInit] Preloaded catalogs: depts=" + depts.size()
            + ", divisions=" + divisions.size() + ", roles=" + roles.size());

    } catch (Exception e) {
      // Throw để Tomcat fail-fast khi cấu hình DB sai
      throw new IllegalStateException("Could not init application (DataSource/DAOs).", e);
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    ServletContext ctx = sce.getServletContext();
    // Không cần đóng DS (container quản lý pool). Dọn các attr để gọn gàng.
    ctx.removeAttribute(CTX_DS);
    ctx.removeAttribute(CTX_DEPT_DAO);
    ctx.removeAttribute(CTX_DIV_DAO);
    ctx.removeAttribute(CTX_ROLE_DAO);
    ctx.removeAttribute(CTX_DEPTS);
    ctx.removeAttribute(CTX_DIVISIONS);
    ctx.removeAttribute(CTX_ROLES);
    CACHED_DS = null;
    ctx.log("[AppInit] contextDestroyed");
  }

  /* ===== helpers ===== */
  @FunctionalInterface
  private interface SqlSupplier<T> { T get() throws Exception; }

  private static <T> List<T> safe(ServletContext ctx, String tag, SqlSupplier<List<T>> fn) {
    try {
      return fn.get();
    } catch (Exception e) {
      if (ctx != null) ctx.log(tag + " failed → return empty list", e);
      return Collections.emptyList();
    }
  }
}
