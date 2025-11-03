<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>HR Dashboard</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css?v=1">
</head>
<body>
  <jsp:include page="/WEB-INF/views/common/_admin_header.jsp"/>

  <div class="container">
    <h2>HR Dashboard</h2>

    <div class="cards">
      <div class="card"><div class="kpi-label">Tổng nhân sự</div><div class="kpi">${totalEmployees}</div></div>
      <div class="card"><div class="kpi-label">Đang nghỉ hôm nay</div><div class="kpi">${onLeaveToday}</div></div>
      <div class="card"><div class="kpi-label">Intern</div><div class="kpi">${interns}</div></div>
      <div class="card"><div class="kpi-label">Sắp hết HĐ (≤30d)</div><div class="kpi">${contractEndingSoon}</div></div>
    </div>

    <h3>Nghỉ hôm nay</h3>
    <table class="table">
      <thead>
        <tr><th>Nhân sự</th><th>Phòng ban</th><th>Từ</th><th>Đến</th></tr>
      </thead>
      <tbody>
        <c:forEach items="${todayLeaves}" var="r">
          <tr>
            <td><c:out value="${r.fullName}"/></td>
            <td><c:out value="${empty r.divisionName ? '—' : r.divisionName}"/></td>
            <td><fmt:formatDate value="${r.startDate}" pattern="dd/MM/yyyy"/></td>
            <td><fmt:formatDate value="${r.endDate}"   pattern="dd/MM/yyyy"/></td>
          </tr>
        </c:forEach>
        <c:if test="${empty todayLeaves}">
          <tr><td colspan="4" class="muted">Hôm nay không có ai nghỉ.</td></tr>
        </c:if>
      </tbody>
    </table>
  </div>

  <jsp:include page="/WEB-INF/views/common/_admin_footer.jsp"/>
</body>
</html>
