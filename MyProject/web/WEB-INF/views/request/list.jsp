<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html><html><head><title>Login</title></head>
<body>
<h2>Leave Management - Login</h2>
<form method="post" action="${pageContext.request.contextPath}/login">
    Username: <input name="username" required>
    Password: <input type="password" name="password" required>
    <button type="submit">Login</button>
</form>
<div style="color:red">${requestScope.error}</div>
</body></html>
