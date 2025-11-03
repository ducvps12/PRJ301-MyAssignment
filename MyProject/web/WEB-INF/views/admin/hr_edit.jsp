<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html><html lang="vi"><head>
  <meta charset="UTF-8"><title>Sửa nhân viên #${u.id}</title>
</head><body>
<jsp:include page="/WEB-INF/views/common/_admin_header.jsp"/>

<div class="container">
  <h2>Sửa nhân viên #${u.id}</h2>
  <c:if test="${param.ok==1}"><div class="alert success">Đã lưu.</div></c:if>

  <form method="post" action="${pageContext.request.contextPath}/admin/hr/edit">
    <input type="hidden" name="_csrf" value="${_csrf}">
    <input type="hidden" name="id" value="${u.id}">

    <div class="grid2">
      <label>Họ tên <input name="fullname" value="${u.fullname}" required></label>
      <label>Email <input name="email" value="${u.email}" type="email"></label>

      <label>Vai trò
        <select name="role">
          <c:forEach var="r" items="${['INTERN','STAFF','TEAM_LEAD','DIV_LEADER','HR_ADMIN','ADMIN']}">
            <option value="${r}" ${u.role==r?'selected':''}>${r}</option>
          </c:forEach>
        </select>
      </label>
      <label>Phòng ban (ID) <input name="division_id" value="${u.divisionId}"></label>

      <label>Trạng thái
        <select name="status">
          <c:forEach var="s" items="${['ACTIVE','ON_LEAVE','RESIGNED','TERMINATED']}">
            <option value="${s}" ${u.status==s?'selected':''}>${s}</option>
          </c:forEach>
        </select>
      </label>
      <label>Chức danh <input name="job_title" value="${u.jobTitle}"></label>

      <label>Ngày vào <input name="join_date" type="date" value="${u.joinDate}"></label>
      <label>Hết HĐ <input name="contract_end" type="date" value="${u.contractEnd}"></label>

      <label>Lương <input name="salary" type="number" step="0.01" value="${u.salary}"></label>
    </div>

    <div class="actions">
      <a class="btn ghost" href="${pageContext.request.contextPath}/admin/hr/list">Hủy</a>
      <button class="btn primary" type="submit">Lưu</button>
    </div>
  </form>
</div>

</body></html>
