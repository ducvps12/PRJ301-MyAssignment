<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>Approvals</title>
  <style>
    body{font-family:system-ui,Segoe UI,Roboto,Arial;margin:24px}
    table{width:100%;border-collapse:collapse}
    th,td{padding:10px;border-bottom:1px solid #e5e7eb;text-align:left}
    .pill{padding:4px 8px;border-radius:999px;background:#fde68a}
    a.btn{padding:6px 10px;border:1px solid #d1d5db;border-radius:8px;text-decoration:none}
  </style>
</head>
<body>
  <h2>Đơn chờ duyệt</h2>
  <table>
    <thead>
      <tr><th>ID</th><th>Nội dung</th><th>Người tạo</th><th>Khoảng thời gian</th><th>Trạng thái</th><th>Thao tác</th></tr>
    </thead>
    <tbody>
    <c:forEach var="r" items="${items}">
      <tr>
        <td>#${r.id}</td>
        <td><c:out value="${r.title}"/></td>
        <td><c:out value="${r.requester.fullName}"/></td>
        <td>
          <fmt:formatDate value="${r.startDate}" pattern="dd/MM/yyyy"/> –
          <fmt:formatDate value="${r.endDate}" pattern="dd/MM/yyyy"/>
        </td>
        <td><span class="pill">${r.status}</span></td>
        <td>
          <a class="btn" href="${pageContext.request.contextPath}/request/approve/${r.id}">Duyệt</a>
        </td>
      </tr>
    </c:forEach>
    <c:if test="${empty items}">
      <tr><td colspan="6" style="color:#6b7280">Không có đơn nào cần duyệt.</td></tr>
    </c:if>
    </tbody>
  </table>
</body>
</html>
