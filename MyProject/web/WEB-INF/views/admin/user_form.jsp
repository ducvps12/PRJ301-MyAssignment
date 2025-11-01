<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ include file="/WEB-INF/views/common/_admin_header.jsp" %>
<%@ include file="/WEB-INF/views/common/_admin_sidebar.jsp" %>

<div class="main-body">
  <div class="container">
    <div class="page-header">
      <h2 style="margin-bottom:0">
        <c:choose>
          <c:when test="${mode eq 'create'}">Tạo người dùng</c:when>
          <c:otherwise>Chỉnh sửa người dùng</c:otherwise>
        </c:choose>
      </h2>
      <p class="muted">Nhập thông tin tài khoản nhân sự</p>
    </div>

    <c:if test="${not empty errs}">
      <div class="alert alert-danger">
        <ul style="margin:0;padding-left:18px">
          <c:forEach var="e" items="${errs}">
            <li>${e}</li>
          </c:forEach>
        </ul>
      </div>
    </c:if>

    <form method="post" class="card" style="padding:20px;max-width:780px">
      <input type="hidden" name="csrf" value="${csrf}"/>

      <div class="row">
        <div class="col-6 mb-3">
          <label class="form-label">Họ tên *</label>
          <input type="text" name="full_name" class="form-control"
                 value="${f_full_name}" placeholder="VD: Ms QA Lead" required>
        </div>
        <div class="col-6 mb-3">
          <label class="form-label">Email</label>
          <input type="email" name="email" class="form-control"
                 value="${f_email}" placeholder="name@company.com">
        </div>
      </div>

      <div class="row">
        <div class="col-4 mb-3">
          <label class="form-label">Username *</label>
          <input type="text" name="username" class="form-control"
                 value="${f_username}" placeholder="qa.s1" required>
        </div>
        <div class="col-4 mb-3">
          <label class="form-label">Mật khẩu *</label>
          <input type="text" name="password" class="form-control"
                 value="123456" />
          <small class="text-muted">Mặc định 123456 – có thể reset sau</small>
        </div>
        <div class="col-4 mb-3">
          <label class="form-label">Trạng thái</label>
          <select name="status" class="form-select">
            <option value="ACTIVE"  ${f_status == 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
            <option value="INACTIVE" ${f_status == 'INACTIVE' ? 'selected' : ''}>INACTIVE</option>
          </select>
        </div>
      </div>

      <div class="row">
        <div class="col-4 mb-3">
          <label class="form-label">Role *</label>
          <select name="role" class="form-select" required>
            <option value="">-- Chọn --</option>
            <option value="ADMIN"      ${f_role == 'ADMIN' ? 'selected':''}>ADMIN</option>
            <option value="DIV_LEADER" ${f_role == 'DIV_LEADER' ? 'selected':''}>DIV_LEADER</option>
            <option value="TEAM_LEAD"  ${f_role == 'TEAM_LEAD' ? 'selected':''}>TEAM_LEAD</option>
            <option value="STAFF"      ${f_role == 'STAFF' ? 'selected':''}>STAFF</option>
          </select>
        </div>
        <div class="col-4 mb-3">
          <label class="form-label">Phòng ban *</label>
          <select name="department" class="form-select" required>
            <option value="">-- Chọn --</option>
            <option value="IT"   ${f_department == 'IT' ? 'selected' : ''}>IT</option>
            <option value="QA"   ${f_department == 'QA' ? 'selected' : ''}>QA</option>
            <option value="SALE" ${f_department == 'SALE' ? 'selected' : ''}>SALE</option>
            <option value="HR"   ${f_department == 'HR' ? 'selected' : ''}>HR</option>
          </select>
        </div>
      </div>

      <div class="mt-4 d-flex gap-2">
        <button type="submit" class="btn btn-primary">Lưu</button>
        <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-light">Hủy</a>
      </div>
    </form>
  </div>
</div>

<%@ include file="/WEB-INF/views/common/_admin_footer.jsp" %>
