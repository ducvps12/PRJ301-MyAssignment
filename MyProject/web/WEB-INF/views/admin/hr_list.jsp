<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html><html lang="vi"><head>
  <meta charset="UTF-8"><title>HR · Danh sách nhân viên</title>
</head><body>
<jsp:include page="/WEB-INF/views/common/_admin_header.jsp"/>

<div class="container">
  <div class="flex between">
    <h2>Danh sách nhân viên</h2>
    <form method="get" class="inline">
      <input type="text" name="q" value="${param.q}" placeholder="Tìm tên, email, username">
      <select name="role">
        <option value="">-- Vai trò --</option>
        <option ${param.role=='STAFF'?'selected':''}>STAFF</option>
        <option ${param.role=='TEAM_LEAD'?'selected':''}>TEAM_LEAD</option>
        <option ${param.role=='DIV_LEADER'?'selected':''}>DIV_LEADER</option>
        <option ${param.role=='HR_ADMIN'?'selected':''}>HR_ADMIN</option>
        <option ${param.role=='ADMIN'?'selected':''}>ADMIN</option>
        <option ${param.role=='INTERN'?'selected':''}>INTERN</option>
      </select>
      <button type="submit">Lọc</button>
    </form>
  </div>

  <table class="table">
    <thead>
      <tr><th>#</th><th>Họ tên</th><th>Phòng ban</th><th>Vai trò</th><th>Trạng thái</th><th>Ngày vào</th><th>Hết HĐ</th><th></th></tr>
    </thead>
    <tbody>
      <c:forEach items="${users}" var="u">
        <tr>
          <td>${u.id}</td>
          <td>${u.fullname}</td>
          <td>${u.divisionId}</td>
          <td>${u.role}</td>
          <td>${u.status}</td>
          <td>${u.joinDate}</td>
          <td>${u.contractEnd}</td>
          <td><a class="btn" href="${pageContext.request.contextPath}/admin/hr/edit?id=${u.id}">Sửa</a></td>
        </tr>
      </c:forEach>
      <c:if test="${empty users}">
        <tr><td colspan="8" class="muted">Không có dữ liệu.</td></tr>
      </c:if>
    </tbody>
  </table>
</div>

</body></html>

