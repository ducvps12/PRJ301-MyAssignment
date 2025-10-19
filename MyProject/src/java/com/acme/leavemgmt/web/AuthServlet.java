package com.acme.leavemgmt.web.auth;

import com.acme.leavemgmt.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;

public class AuthServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    // Hiển thị trang login
    req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String username = req.getParameter("username");
    String password = req.getParameter("password");

    // TODO: thay bằng check DB. Tạm hardcode để test nhanh:
    if ("admin".equalsIgnoreCase(username) && "admin".equals(password)) {
      User me = new User();
      me.setId(1);
      me.setUsername("admin");
      me.setFullName("Administrator");
      me.setRole("ADMIN");
      me.setDepartment("IT");

      HttpSession session = req.getSession(true);
      session.setAttribute("user", me);
      session.setAttribute("userId", me.getId()); // nếu nơi khác đang dùng

      // Redirect về trang trước khi bị chặn (nếu có)
      String back = (String) session.getAttribute("redirectAfterLogin");
      if (back != null && !back.isBlank()) {
        session.removeAttribute("redirectAfterLogin");
        resp.sendRedirect(req.getContextPath() + back);
      } else {
        // hoặc về dashboard admin
        resp.sendRedirect(req.getContextPath() + "/admin");
      }
      return;
    }

    // Sai tài khoản
    req.setAttribute("error", "Sai username hoặc password.");
    req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, resp);
  }
}
