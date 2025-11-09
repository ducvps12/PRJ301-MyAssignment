<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="mytag" uri="http://acme.com/tags" %>
<!DOCTYPE html>
<html lang="vi">
<head><meta charset="UTF-8"><title>Tag Demo</title></head>
<body>
  <p><strong><mytag:ToVietnameseDate value="${requestScope.data}" /></strong></p>
  <!-- Ví dụ khác:
  <mytag:ToVietnameseDate value="2024-10-30" />
  <mytag:ToVietnameseDate value="30/10/2024" />
  -->
</body>
</html>
