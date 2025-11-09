<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
  // giả lập đăng nhập: nếu submit thì set session và redirect về 'next'
  request.setCharacterEncoding("UTF-8");
  if ("POST".equalsIgnoreCase(request.getMethod())) {
    String user = request.getParameter("username");
    if (user != null && !user.isBlank()) {
      session.setAttribute("currentUser", user); // AuthFilter kiểm tra key này
      String next = request.getParameter("next");
      if (next == null || next.isBlank()) next = request.getContextPath() + "/index.html";
      response.sendRedirect(next);
      return;
    }
  }
%>
<!DOCTYPE html>
<html lang="vi">
<head><meta charset="UTF-8"><title>Login</title></head>
<body>
  <h2>Đăng nhập (demo)</h2>
  <form method="post">
    <input name="username" placeholder="Tên đăng nhập"/>
    <button type="submit">Login</button>
    <input type="hidden" name="next" value="<%= request.getParameter("next") == null ? "" : request.getParameter("next") %>"/>
  </form>
</body>
</html>
