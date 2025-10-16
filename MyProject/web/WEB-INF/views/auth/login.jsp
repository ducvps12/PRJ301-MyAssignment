<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@include file="/WEB-INF/views/common/_taglibs.jsp"%>
<!DOCTYPE html>
<html lang="vi">
<head><meta charset="UTF-8"><title>Login</title></head>
<body>
  <div class="container" style="max-width:420px;margin:48px auto">
    <h2>Đăng nhập</h2>
    <form method="post" action="${pageContext.request.contextPath}/auth/login" class="card" style="padding:16px">
      <label>Username</label>
      <input class="input" name="username" required>
      <label style="margin-top:8px">Password</label>
      <input class="input" type="password" name="password" required>
      <button class="btn" style="margin-top:12px;width:100%">Đăng nhập</button>
    </form>
  </div>
  <%@include file="/WEB-INF/views/common/_footer.jsp"%>
</body>
</html>
