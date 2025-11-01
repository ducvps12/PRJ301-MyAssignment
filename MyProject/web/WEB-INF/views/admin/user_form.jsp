<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ include file="/WEB-INF/views/common/_admin_header.jsp" %>
<%@ include file="/WEB-INF/views/common/_admin_sidebar.jsp" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!-- nếu chưa có status thì gán mặc định ACTIVE -->
<c:if test="${empty f_status}">
  <c:set var="f_status" value="ACTIVE" />
</c:if>

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

    <!-- thông báo lỗi -->
    <c:if test="${not empty errs}">
      <div class="alert alert-danger">
        <ul style="margin:0;padding-left:18px">
          <c:forEach var="e" items="${errs}">
            <li><c:out value="${e}"/></li>
          </c:forEach>
        </ul>
      </div>
    </c:if>

    <!-- form: post về đúng URL hiện tại -->
    <form method="post"
          action="${pageContext.request.requestURI}"
          class="card"
          style="padding:20px;max-width:780px">
      <input type="hidden" name="csrf" value="${csrf}"/>

      <div class="row">
        <div class="col-6 mb-3">
          <label class="form-label">Họ tên *</label>
          <input type="text"
                 name="full_name"
                 class="form-control"
                 value="<c:out value='${f_full_name}'/>"
                 placeholder="VD: Ms QA Lead"
                 required>
        </div>
        <div class="col-6 mb-3">
          <label class="form-label">Email</label>
          <input type="email"
                 name="email"
                 class="form-control"
                 value="<c:out value='${f_email}'/>"
                 placeholder="name@company.com">
        </div>
      </div>

      <div class="row">
        <div class="col-4 mb-3">
          <label class="form-label">Username *</label>
          <input type="text"
                 name="username"
                 class="form-control"
                 value="<c:out value='${f_username}'/>"
                 placeholder="qa.s1"
                 required>
        </div>
        <div class="col-4 mb-3">
          <label class="form-label">Mật khẩu *</label>
          <!-- để dạng password nhìn gọn hơn -->
          <input type="password"
                 name="password"
                 class="form-control"
                 value="123456" />
          <small class="text-muted">Mặc định 123456 – có thể reset sau</small>
        </div>
        <div class="col-4 mb-3">
          <label class="form-label">Trạng thái</label>
          <select name="status" class="form-select">
            <option value="ACTIVE"
              <c:if test="${f_status == 'ACTIVE'}">selected</c:if>>ACTIVE</option>
            <option value="INACTIVE"
              <c:if test="${f_status == 'INACTIVE'}">selected</c:if>>INACTIVE</option>
          </select>
        </div>
      </div>

      <div class="row">
        <div class="col-4 mb-3">
          <label class="form-label">Role *</label>
          <select name="role" class="form-select" required>
            <option value="">-- Chọn --</option>
            <option value="ADMIN"
              <c:if test="${f_role == 'ADMIN'}">selected</c:if>>ADMIN</option>
            <option value="DIV_LEADER"
              <c:if test="${f_role == 'DIV_LEADER'}">selected</c:if>>DIV_LEADER</option>
            <option value="TEAM_LEAD"
              <c:if test="${f_role == 'TEAM_LEAD'}">selected</c:if>>TEAM_LEAD</option>
            <option value="STAFF"
              <c:if test="${f_role == 'STAFF' || empty f_role}">selected</c:if>>STAFF</option>
          </select>
        </div>
        <div class="col-4 mb-3">
          <label class="form-label">Phòng ban *</label>
          <select name="department" class="form-select" required>
            <option value="">-- Chọn --</option>
            <option value="IT"
              <c:if test="${f_department == 'IT'}">selected</c:if>>IT</option>
            <option value="QA"
              <c:if test="${f_department == 'QA'}">selected</c:if>>QA</option>
            <option value="SALE"
              <c:if test="${f_department == 'SALE'}">selected</c:if>>SALE</option>
            <option value="HR"
              <c:if test="${f_department == 'HR'}">selected</c:if>>HR</option>
          </select>
        </div>
      </div>

      <div class="mt-4 d-flex gap-2">
        <button type="submit" class="btn btn-primary">Lưu</button>
        <a href="${ctx}/admin/users" class="btn btn-light">Hủy</a>
      </div>
    </form>
  </div>
</div>

<!-- auto điền username theo họ tên -->
<script>
  (function() {
    const full = document.querySelector('input[name="full_name"]');
    const user = document.querySelector('input[name="username"]');
    // chỉ auto nếu đang ở mode create và chưa gõ username
    const isCreate = '${mode}' === 'create';
    if (full && user && isCreate) {
      full.addEventListener('input', function() {
        if (user.dataset.touched === '1') return;
        const v = this.value
          .trim()
          .toLowerCase()
          .normalize("NFD").replace(/[\u0300-\u036f]/g, "")
          .replace(/[^a-z0-9\s]/g, "")
          .replace(/\s+/g, '.');
        user.value = v;
      });
      user.addEventListener('input', function() {
        this.dataset.touched = '1';
      });
    }
  })();
</script>

<%@ include file="/WEB-INF/views/common/_admin_footer.jsp" %>
