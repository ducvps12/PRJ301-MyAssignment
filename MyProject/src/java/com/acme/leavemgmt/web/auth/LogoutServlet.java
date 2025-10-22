package com.acme.leavemgmt.web.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet(name = "LogoutServlet", urlPatterns = {"/auth/logout"})
public class LogoutServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    HttpSession session = req.getSession(false);
    if (session != null) session.invalidate();

    // Option 1: quay về trang login
    resp.sendRedirect(req.getContextPath() + "/login");
    // Option 2: quay về trang chủ (nếu guest vẫn xem được)
    // resp.sendRedirect(req.getContextPath() + "/");
  }

  // Nếu bạn muốn chỉ cho phép POST logout, có thể forward POST -> GET:
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    doGet(req, resp);
  }
}
