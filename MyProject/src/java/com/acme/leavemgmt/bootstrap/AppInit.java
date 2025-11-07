package com.acme.leavemgmt.bootstrap;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import javax.naming.InitialContext;
import javax.sql.DataSource;

@WebListener
public class AppInit implements ServletContextListener {
  @Override public void contextInitialized(ServletContextEvent sce) {
    try {
      DataSource ds = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/LeaveDB");
      sce.getServletContext().setAttribute("DS", ds);
    } catch (Exception e) {
      throw new IllegalStateException("Could not init DataSource via JNDI", e);
    }
  }
}
