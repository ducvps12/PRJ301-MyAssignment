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

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    ServletContext ctx = sce.getServletContext();
    try {
      // 1) Lấy DataSource qua JNDI
      DataSource ds = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/LeaveDB");
      if (ds == null) throw new IllegalStateException("JNDI jdbc/LeaveDB not found");
      ctx.setAttribute(CTX_DS, ds);

      // 2) Test kết nối một phát cho chắc (log URL cho DEV)
      try (Connection c = ds.getConnection()) {
        String url = null;
        try { url = c.getMetaData().getURL(); } catch (Exception ignore) {}
        ctx.log("[AppInit] DataSource OK " + (url != null ? ("→ " + url) : ""));
      }

      // 3) Khởi tạo DAO dùng chung
      DeptDAO deptDAO       = new DeptDAO(ds);
      DivisionDAO divDAO    = new DivisionDAO(ds);
      RoleDAO roleDAO       = new RoleDAO(ds);

      ctx.setAttribute(CTX_DEPT_DAO, deptDAO);
      ctx.setAttribute(CTX_DIV_DAO,  divDAO);
      ctx.setAttribute(CTX_ROLE_DAO, roleDAO);

      // 4) Preload danh mục để render nhanh ở JSP (có thể null-safe nếu DB trống)
      List<?> depts     = safe(() -> deptDAO.listAll());
      List<?> divisions = safe(() -> divDAO.listAll());
      List<?> roles     = safe(() -> roleDAO.listAll());
      ctx.setAttribute(CTX_DEPTS,     depts);
      ctx.setAttribute(CTX_DIVISIONS, divisions);
      ctx.setAttribute(CTX_ROLES,     roles);

      ctx.log("[AppInit] Preloaded catalogs: depts=" + depts.size()
              + ", divisions=" + divisions.size() + ", roles=" + roles.size());

    } catch (Exception e) {
      // Nên throw để Tomcat fail fast khi cấu hình DB sai
      throw new IllegalStateException("Could not init application (DataSource/DAOs).", e);
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    // DataSource là pool do container quản lý, không cần đóng ở đây
    sce.getServletContext().log("[AppInit] contextDestroyed");
  }

  /* ===== helpers ===== */
  @FunctionalInterface
  private interface SqlSupplier<T> { T get() throws Exception; }
  private static <T> T safe(SqlSupplier<T> fn) {
    try { return fn.get(); } catch (Exception e) { return (T) Collections.emptyList(); }
  }
}
