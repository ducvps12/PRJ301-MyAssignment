package com.acme.leavemgmt.web;

import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.IOException;

public class HelloServlet extends HttpServlet {
  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("text/plain; charset=UTF-8");
    resp.getWriter().println("OK - deployed");
  }
}
