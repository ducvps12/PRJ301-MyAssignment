<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head><meta charset="UTF-8"><title>Division Dashboard</title></head>
<body>
<h1>Division Dashboard</h1>
<p>Xin chào, ${sessionScope.currentUser.fullName} – Phòng: ${sessionScope.currentUser.department}</p>

<c:if test="${not empty stats}">
  <!-- render stats ở đây -->
  <pre>${stats}</pre>
</c:if>
</body>
</html>
