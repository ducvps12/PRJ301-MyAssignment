package com.acme.leavemgmt.servlet;

import com.acme.leavemgmt.util.Csrf;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet(urlPatterns = {"/support/*"})
public class SupportServlet extends HttpServlet {
  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String p = req.getPathInfo(); // /faq, /terms, /privacy, /guide, /contact, /status
    if (p == null) p = "/guide";
    String view;
    switch (p) {
      case "/faq"     -> view = "/WEB-INF/views/support/faq.jsp";
      case "/terms"   -> view = "/WEB-INF/views/support/terms.jsp";
      case "/privacy" -> view = "/WEB-INF/views/support/privacy.jsp";
      case "/contact" -> view = "/WEB-INF/views/support/contact.jsp";
      case "/status"  -> view = "/WEB-INF/views/support/status.jsp";
      default         -> view = "/WEB-INF/views/support/guide.jsp";
    }
    Csrf.addToken(req);
    req.getRequestDispatcher(view).forward(req, resp);
  }
}
