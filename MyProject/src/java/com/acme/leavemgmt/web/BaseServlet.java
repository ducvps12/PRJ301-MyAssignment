package com.acme.leavemgmt.web;

import jakarta.servlet.http.HttpServlet;
import javax.naming.InitialContext;
import javax.sql.DataSource;

public abstract class BaseServlet extends HttpServlet {
  protected DataSource getDS() {
    try {
      DataSource ds = (DataSource) getServletContext().getAttribute("DS");
      if (ds == null) {
        ds = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/LeaveDB");
        getServletContext().setAttribute("DS", ds);
      }
      return ds;
    } catch (Exception e) {
      throw new IllegalStateException("DataSource not available", e);
    }
  }
}
