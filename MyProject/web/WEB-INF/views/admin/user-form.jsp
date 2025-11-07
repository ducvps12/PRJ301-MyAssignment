

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@include file="/WEB-INF/views/common/_taglibs.jsp"%>

<%@ include file="/WEB-INF/views/common/_admin_header.jsp" %>
  <jsp:include page="/WEB-INF/views/audit/_audit_sidebar.jsp" />

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!-- Gán mặc định -->
<c:if test="${empty f_status}">
  <c:set var="f_status" value="ACTIVE" />
</c:if>
<c:if test="${empty mode}">
  <c:set var="mode" value="create" />
</c:if>
<c:set var="isEdit" value="${mode eq 'edit'}" />

<div class="main-body">
  <div class="container">
    <div class="page-header mb-4">
      <h2 class="mb-1">
        <c:choose>
          <c:when test="${isEdit}">Chỉnh sửa người dùng</c:when>
          <c:otherwise>Tạo người dùng mới</c:otherwise>
        </c:choose>
      </h2>
      <p class="text-muted">
        <c:choose>
          <c:when test="${isEdit}">Cập nhật thông tin tài khoản. Username không thể thay đổi.</c:when>
          <c:otherwise>Nhập thông tin tài khoản nhân sự và phân quyền ban đầu.</c:otherwise>
        </c:choose>
      </p>
    </div>

    <!-- Thông báo lỗi -->
    <c:if test="${not empty errors}">
      <div class="alert alert-danger" role="alert">
        <strong>Có lỗi xảy ra:</strong>
        <ul style="margin:8px 0 0 0;padding-left:20px">
          <c:forEach var="e" items="${errors}">
            <li><c:out value="${e}"/></li>
          </c:forEach>
        </ul>
      </div>
    </c:if>

    <!-- FORM -->
    <form method="post"
          action="${ctx}/admin/users/${isEdit ? 'edit' : 'create'}${isEdit ? '?id=' : ''}${isEdit ? f_id : ''}"
          class="card shadow-sm"
          style="padding:24px;max-width:800px">

      <input type="hidden" name="csrf" value="${csrf}"/>
      <c:if test="${isEdit}">
        <input type="hidden" name="id" value="${f_id}"/>
      </c:if>

      <div class="row">
        <div class="col-md-6 mb-3">
          <label class="form-label fw-semibold">Họ tên <span class="text-danger">*</span></label>
          <input type="text"
                 name="full_name"
                 class="form-control"
                 value="<c:out value='${f_full_name}'/>"
                 placeholder="VD: Ms QA Lead"
                 required>
        </div>
        <div class="col-md-6 mb-3">
          <label class="form-label fw-semibold">Email</label>
          <input type="email"
                 name="email"
                 class="form-control"
                 value="<c:out value='${f_email}'/>"
                 placeholder="name@company.com">
        </div>
      </div>

      <div class="row">
        <div class="col-md-4 mb-3">
          <label class="form-label fw-semibold">Số điện thoại</label>
          <input type="tel"
                 name="phone"
                 class="form-control"
                 value="<c:out value='${f_phone}'/>"
                 placeholder="0912345678">
        </div>
        <div class="col-md-4 mb-3">
          <label class="form-label fw-semibold">Ngày sinh</label>
          <input type="date"
                 name="birthday"
                 class="form-control"
                 value="<c:out value='${f_birthday}'/>">
        </div>
        <div class="col-md-4 mb-3">
          <label class="form-label fw-semibold">Địa chỉ</label>
          <input type="text"
                 name="address"
                 class="form-control"
                 value="<c:out value='${f_address}'/>"
                 placeholder="Số nhà, đường, quận/huyện...">
        </div>
      </div>

      <div class="row">
        <div class="col-md-4 mb-3">
          <label class="form-label fw-semibold">Username <span class="text-danger">*</span></label>
          <input type="text"
                 name="username"
                 class="form-control"
                 value="<c:out value='${f_username}'/>"
                 placeholder="qa.s1"
                 ${isEdit ? 'readonly' : ''}
                 required>
          <c:if test="${isEdit}">
            <small class="text-muted">Username không thể thay đổi</small>
          </c:if>
        </div>
        <div class="col-md-4 mb-3">
          <label class="form-label fw-semibold">
            Mật khẩu <c:if test="${!isEdit}"><span class="text-danger">*</span></c:if>
          </label>
          <div class="password-wrapper">
            <input type="password"
                   name="password"
                   id="passwordInput"
                   class="form-control"
                   ${isEdit ? '' : 'value="123456"'}
                   placeholder="${isEdit ? 'Để trống nếu không đổi' : '123456'}"
                   autocomplete="${isEdit ? 'new-password' : 'new-password'}">
            <button type="button" class="password-toggle" onclick="togglePassword()" aria-label="Hiện/ẩn mật khẩu">👁</button>
          </div>
          <div class="password-strength" id="passwordStrength" style="display:none">
            <div class="strength-bar">
              <div class="strength-fill" id="strengthFill"></div>
            </div>
            <small class="strength-text" id="strengthText"></small>
          </div>
          <small class="text-muted">
            <c:choose>
              <c:when test="${isEdit}">Để trống nếu không muốn thay đổi mật khẩu</c:when>
              <c:otherwise>Mặc định: 123456 – có thể reset sau</c:otherwise>
            </c:choose>
          </small>
        </div>
        <div class="col-md-4 mb-3">
          <label class="form-label fw-semibold">Trạng thái</label>
          <select name="status" class="form-select">
            <option value="ACTIVE"   <c:if test="${f_status == 'ACTIVE'}">selected</c:if>>ACTIVE</option>
            <option value="INACTIVE" <c:if test="${f_status == 'INACTIVE'}">selected</c:if>>INACTIVE</option>
          </select>
        </div>
      </div>

      <div class="row">
        <div class="col-md-4 mb-3">
          <label class="form-label fw-semibold">Role <span class="text-danger">*</span></label>
          <select name="role" class="form-select" required>
            <option value="">-- Chọn --</option>
            <option value="ADMIN"      <c:if test="${f_role == 'ADMIN'}">selected</c:if>>ADMIN</option>
            <option value="DIV_LEADER" <c:if test="${f_role == 'DIV_LEADER'}">selected</c:if>>DIV_LEADER</option>
            <option value="TEAM_LEAD"  <c:if test="${f_role == 'TEAM_LEAD'}">selected</c:if>>TEAM_LEAD</option>
            <option value="STAFF"      <c:if test="${f_role == 'STAFF' || empty f_role}">selected</c:if>>STAFF</option>
          </select>
        </div>
        <div class="col-md-4 mb-3">
          <label class="form-label fw-semibold">Phòng ban <span class="text-danger">*</span></label>
          <select name="department" class="form-select" required>
            <option value="">-- Chọn --</option>
            <option value="IT"   <c:if test="${f_department == 'IT'}">selected</c:if>>IT</option>
            <option value="QA"   <c:if test="${f_department == 'QA'}">selected</c:if>>QA</option>
            <option value="SALE" <c:if test="${f_department == 'SALE'}">selected</c:if>>SALE</option>
            <option value="HR"   <c:if test="${f_department == 'HR'}">selected</c:if>>HR</option>
            <option value="FIN"  <c:if test="${f_department == 'FIN'}">selected</c:if>>FIN</option>
          </select>
        </div>
      </div>

      <div class="mt-4 d-flex gap-2">
        <button type="submit" id="submitBtn" class="btn btn-primary px-4">
          <span class="btn-text">
            <c:choose>
              <c:when test="${isEdit}">Cập nhật</c:when>
              <c:otherwise>Tạo mới</c:otherwise>
            </c:choose>
          </span>
          <span class="btn-spinner" style="display:none">⏳</span>
        </button>
        <a href="${ctx}/admin/users" class="btn btn-light border px-4">Hủy</a>
      </div>
    </form>
  </div>
</div>

<script>
(function() {
  // Auto gợi ý username theo họ tên (chỉ khi create)
  const full = document.querySelector('input[name="full_name"]');
  const user = document.querySelector('input[name="username"]');
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
    user.addEventListener('input', () => user.dataset.touched = '1');
  }

  // Password strength indicator
  const pwdInput = document.getElementById('passwordInput');
  const pwdStrength = document.getElementById('passwordStrength');
  const strengthFill = document.getElementById('strengthFill');
  const strengthText = document.getElementById('strengthText');
  
  if (pwdInput && pwdStrength) {
    pwdInput.addEventListener('input', function() {
      const pwd = this.value;
      if (!pwd || pwd.length === 0) {
        pwdStrength.style.display = 'none';
        return;
      }
      pwdStrength.style.display = 'block';
      
      let strength = 0;
      let feedback = '';
      
      if (pwd.length >= 8) strength++;
      if (pwd.length >= 12) strength++;
      if (/[a-z]/.test(pwd) && /[A-Z]/.test(pwd)) strength++;
      if (/\d/.test(pwd)) strength++;
      if (/[^a-zA-Z0-9]/.test(pwd)) strength++;
      
      const percentage = (strength / 5) * 100;
      strengthFill.style.width = percentage + '%';
      
      if (strength <= 2) {
        strengthFill.style.background = '#ef4444';
        feedback = 'Yếu';
      } else if (strength === 3) {
        strengthFill.style.background = '#f59e0b';
        feedback = 'Trung bình';
      } else if (strength === 4) {
        strengthFill.style.background = '#3b82f6';
        feedback = 'Mạnh';
      } else {
        strengthFill.style.background = '#22c55e';
        feedback = 'Rất mạnh';
      }
      
      strengthText.textContent = feedback;
    });
  }

  // Toggle password visibility
  window.togglePassword = function() {
    const input = document.getElementById('passwordInput');
    const btn = document.querySelector('.password-toggle');
    if (input && btn) {
      if (input.type === 'password') {
        input.type = 'text';
        btn.textContent = '🙈';
      } else {
        input.type = 'password';
        btn.textContent = '👁';
      }
    }
  };

  // Form submission loading state
  const form = document.querySelector('form');
  const submitBtn = document.getElementById('submitBtn');
  if (form && submitBtn) {
    form.addEventListener('submit', function() {
      const btnText = submitBtn.querySelector('.btn-text');
      const btnSpinner = submitBtn.querySelector('.btn-spinner');
      if (btnText && btnSpinner) {
        btnText.style.display = 'none';
        btnSpinner.style.display = 'inline';
        submitBtn.disabled = true;
      }
    });
  }

  // Client-side validation
  const emailInput = document.querySelector('input[name="email"]');
  if (emailInput) {
    emailInput.addEventListener('blur', function() {
      const email = this.value.trim();
      if (email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        this.setCustomValidity('Email không hợp lệ');
      } else {
        this.setCustomValidity('');
      }
    });
  }

  const phoneInput = document.querySelector('input[name="phone"]');
  if (phoneInput) {
    phoneInput.addEventListener('blur', function() {
      const phone = this.value.trim();
      if (phone && !/^[0-9]{10,11}$/.test(phone)) {
        this.setCustomValidity('Số điện thoại phải có 10-11 chữ số');
      } else {
        this.setCustomValidity('');
      }
    });
  }
})();
</script>

<style>
  .form-label {
    font-weight: 600;
    margin-bottom: 6px;
    display: block;
    color: var(--tx);
  }
  .form-control, .form-select {
    width: 100%;
    padding: 10px 12px;
    border: 1px solid var(--bd);
    border-radius: 8px;
    background: var(--card-2);
    color: var(--tx);
    font-size: 14px;
  }
  .form-control:focus, .form-select:focus {
    outline: none;
    box-shadow: 0 0 0 3px color-mix(in oklab, var(--pri) 30%, transparent 70%);
    border-color: var(--pri);
  }
  .form-control[readonly] {
    background: color-mix(in oklab, var(--card-2) 70%, transparent 30%);
    cursor: not-allowed;
  }
  .password-wrapper {
    position: relative;
  }
  .password-toggle {
    position: absolute;
    right: 12px;
    top: 50%;
    transform: translateY(-50%);
    background: transparent;
    border: 0;
    cursor: pointer;
    font-size: 18px;
    padding: 4px;
    opacity: 0.7;
  }
  .password-toggle:hover {
    opacity: 1;
  }
  .password-strength {
    margin-top: 8px;
  }
  .strength-bar {
    height: 4px;
    background: var(--bd);
    border-radius: 2px;
    overflow: hidden;
    margin-bottom: 4px;
  }
  .strength-fill {
    height: 100%;
    transition: width 0.3s, background 0.3s;
    border-radius: 2px;
  }
  .strength-text {
    font-size: 11px;
    color: var(--muted);
    display: block;
  }
  .text-muted {
    color: var(--muted);
    font-size: 12px;
    margin-top: 4px;
    display: block;
  }
  .btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
  .text-danger {
    color: var(--err);
  }
  .alert-danger {
    background: rgba(239, 68, 68, 0.1);
    border: 1px solid rgba(239, 68, 68, 0.3);
    color: var(--err);
    padding: 12px 16px;
    border-radius: 8px;
    margin-bottom: 20px;
  }
  .btn {
    padding: 10px 20px;
    border-radius: 8px;
    border: 1px solid var(--bd);
    background: var(--card-2);
    color: var(--tx);
    text-decoration: none;
    display: inline-flex;
    align-items: center;
    gap: 6px;
    cursor: pointer;
    font-size: 14px;
  }
  .btn-primary {
    background: linear-gradient(180deg, var(--pri), var(--pri2));
    border-color: transparent;
    color: #fff;
  }
  .btn:hover {
    filter: brightness(1.08);
  }
  .d-flex {
    display: flex;
  }
  .gap-2 {
    gap: 8px;
  }
  .mb-3 {
    margin-bottom: 12px;
  }
  .mb-4 {
    margin-bottom: 20px;
  }
  .mt-4 {
    margin-top: 20px;
  }
  .page-header h2 {
    font-size: 24px;
    font-weight: 700;
    margin: 0;
  }
  .row {
    display: grid;
    grid-template-columns: repeat(12, 1fr);
    gap: 12px;
  }
  .col-md-6 {
    grid-column: span 6;
  }
  .col-md-4 {
    grid-column: span 4;
  }
  @media (max-width: 768px) {
    .row {
      grid-template-columns: 1fr;
    }
    .col-md-6, .col-md-4 {
      grid-column: span 1;
    }
  }
</style>

<%@ include file="/WEB-INF/views/common/_admin_footer.jsp" %>

