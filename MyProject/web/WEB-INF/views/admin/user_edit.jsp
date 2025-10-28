<%@ page contentType="text/html; charset=UTF-8" %>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp" %>
<%@ include file="/WEB-INF/views/common/_header.jsp" %>

<div class="container" style="max-width:800px;margin:30px auto;">
  <h2>Sửa thông tin người dùng</h2>
  <form method="post" action="${pageContext.request.contextPath}/admin/users/edit" class="card" style="padding:16px;">
    <input type="hidden" name="id" value="${param.id}">

    <label>Họ tên</label>
    <input class="input" name="fullName" value="${user.full_name}" required>

    <label>Email</label>
    <input class="input" name="email" type="email" value="${user.email}" required>

    <label>Role</label>
    <select class="input" name="role">
      <option ${user.role=='STAFF'?'selected':''}>STAFF</option>
      <option ${user.role=='LEADER'?'selected':''}>LEADER</option>
      <option ${user.role=='ADMIN'?'selected':''}>ADMIN</option>
    </select>

    <label>Phòng ban</label>
    <input class="input" name="department" value="${user.department}">

    <div style="margin-top:16px;">
      <button class="btn pri">Lưu thay đổi</button>
      <a href="${pageContext.request.contextPath}/admin/users" class="btn ghost">Hủy</a>
    </div>
  </form>
</div>

<%@ include file="/WEB-INF/views/common/_footer.jsp" %>
