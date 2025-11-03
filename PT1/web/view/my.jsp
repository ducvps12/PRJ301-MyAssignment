<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="mytag" uri="http://acme.com/tags" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>Demo Custom Tag</title>
</head>
<body>
  <h1>Demo ToVietnameseDate</h1>
  <p>Kết quả:
    <strong><mytag:ToVietnameseDate value="${requestScope.data}" /></strong>
  </p>

  <p><a href="${pageContext.request.contextPath}/">Về trang chủ</a></p>
</body>
</html>
