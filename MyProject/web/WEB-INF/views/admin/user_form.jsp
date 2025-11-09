<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@include file="/WEB-INF/views/common/_taglibs.jsp"%>

<%@ include file="/WEB-INF/views/audit/_audit_header.jsp" %>
  
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!-- Defaults -->
<c:if test="${empty f_status}">
  <c:set var="f_status" value="ACTIVE" />
</c:if>
<c:if test="${empty mode}">
  <c:set var="mode" value="create" />
</c:if>
<c:set var="isEdit" value="${mode eq 'edit'}" />

<!-- Form action -->
<c:choose>
  <c:when test="${isEdit}">
    <c:set var="formAction" value="${ctx}/admin/users/edit?id=${f_id}" />
  </c:when>
  <c:otherwise>
    <c:set var="formAction" value="${ctx}/admin/users/create" />
  </c:otherwise>
</c:choose>


<div class="main-body user-form-page">
  <div class="container-xl">
<br><br>

    <!-- Header / Title -->
    <div class="uf-head">
      <div class="uf-left">
        <div class="uf-title">
          <c:choose>
            <c:when test="${isEdit}">Chỉnh sửa người dùng</c:when>
            <c:otherwise>Tạo người dùng mới</c:otherwise>
          </c:choose>
        </div>
        <div class="uf-sub">
          <c:choose>
            <c:when test="${isEdit}">Cập nhật thông tin tài khoản. Username không thể thay đổi.</c:when>
            <c:otherwise>Nhập thông tin nhân sự, hệ thống sẽ gợi ý username + phân quyền nhanh.</c:otherwise>
          </c:choose>
        </div>
        <div class="uf-steps">
          <span class="step active">1. Thông tin cơ bản</span>
          <span class="step active">2. Tài khoản & phân quyền</span>
          <c:if test="${isEdit}">
            <span class="badge-editing">Đang chỉnh sửa #${f_id}</span>
          </c:if>
        </div>
      </div>

      <!-- Avatar -->
      <div class="uf-right">
        <div class="avatar-box" title="Ảnh đại diện tạm">
          <div class="avatar-initial" id="avatarInitial">
            <c:choose>
              <c:when test="${not empty f_full_name}">
                <c:out value="${fn:toUpperCase(fn:substring(f_full_name,0,1))}" />
              </c:when>
              <c:otherwise>U</c:otherwise>
            </c:choose>
          </div>
          <span class="avatar-status ${f_status eq 'INACTIVE' ? 'offline' : 'online'}"></span>
          <input type="file" id="avatarInput" accept="image/*" class="avatar-input" />
        </div>
      </div>
    </div>

    <!-- Errors -->
    <c:if test="${not empty errs}">
      <div class="alert alert-danger uf-alert" role="alert">
        <strong>Có lỗi xảy ra:</strong>
        <ul class="mb-0">
          <c:forEach var="e" items="${errs}">
            <li><c:out value="${e}" /></li>
          </c:forEach>
        </ul>
      </div>
    </c:if>

    <!-- Quick role buttons -->
    <div class="uf-quick-row">
      <div class="qa-title">Chọn nhanh quyền:</div>
      <div class="qa-btns">
        <button type="button" class="qa-btn qa-admin" data-role="ADMIN"      data-dept="IT">Admin hệ thống</button>
        <button type="button" class="qa-btn qa-lead"  data-role="DIV_LEADER" data-dept="QA">Trưởng phòng / Div lead</button>
        <button type="button" class="qa-btn qa-tl"    data-role="TEAM_LEAD"  data-dept="IT">Team lead</button>
        <button type="button" class="qa-btn qa-staff" data-role="STAFF"      data-dept="SALE">Nhân viên</button>
      </div>
      <div class="qa-hint">tip: click 1 nút để tự set <b>Role + Phòng ban</b>.</div>
    </div>

    <form method="post" id="userForm" action="${formAction}" class="uf-card" autocomplete="off">
      <!-- CSRF -->
      <input type="hidden" name="_csrf" value="${csrf}"/>
      <c:if test="${isEdit}">
        <input type="hidden" name="id" value="${f_id}"/>
      </c:if>

      <!-- Grid -->
      <div class="uf-grid">
        <!-- Left -->
        <div class="uf-col">
          <div class="uf-block">
            <h3 class="uf-block-title">1. Thông tin cơ bản</h3>

            <div class="uf-field">
              <label>Họ tên <span class="req">*</span></label>
              <input type="text" name="full_name" id="fullNameInput"
                     value="<c:out value='${f_full_name}'/>"
                     placeholder="VD: Nguyễn Văn A" required />
              <small class="f-hint">Dùng tên thật để tiện phê duyệt / báo cáo.</small>
            </div>

            <div class="uf-field-group">
              <div class="uf-field">
                <label>Email</label>
                <div class="input-with-right">
                  <input type="email" name="email" id="emailInput"
                         value="<c:out value='${f_email}'/>"
                         placeholder="name@company.com" />
                  <button type="button" class="tiny-btn" id="btnAutoMail">@company</button>
                </div>
                <small class="f-hint" id="emailHint"></small>
              </div>

              <div class="uf-field">
                <label>Số điện thoại</label>
                <input type="tel" name="phone" id="phoneInput"
                       value="<c:out value='${f_phone}'/>"
                       placeholder="0912345678" />
                <small class="f-hint" id="phoneHint"></small>
              </div>
            </div>

            <div class="uf-field-group">
              <div class="uf-field">
                <label>Ngày sinh</label>
                <input type="date" name="birthday" value="<c:out value='${f_birthday}'/>" />
              </div>
              <div class="uf-field">
                <label>Địa chỉ</label>
                <input type="text" name="address"
                       value="<c:out value='${f_address}'/>"
                       placeholder="Số nhà, đường, quận/huyện..." />
              </div>
            </div>

          </div>
        </div>

        <!-- Right -->
        <div class="uf-col">
          <div class="uf-block">
            <h3 class="uf-block-title">2. Tài khoản & phân quyền</h3>

            <div class="uf-field-group">
              <div class="uf-field">
                <label>Username <span class="req">*</span></label>
                <input type="text" name="username" id="usernameInput"
                       value="<c:out value='${f_username}'/>"
                       placeholder="ten.nguoi.dung"
                       <c:if test="${isEdit}">readonly</c:if>
                       required />
                <c:if test="${isEdit}">
                  <small class="f-hint">Username không thể thay đổi.</small>
                </c:if>
              </div>

              <div class="uf-field">
                <label>Mật khẩu <c:if test="${!isEdit}"><span class="req">*</span></c:if></label>
                <div class="pw-wrap">
                  <input type="password" name="password" id="passwordInput"
                         <c:choose>
                           <c:when test="${isEdit}">placeholder="Để trống nếu không đổi"</c:when>
                           <c:otherwise>value="123456"</c:otherwise>
                         </c:choose>
                         autocomplete="new-password" />
                  <button type="button" class="pw-btn" id="btnTogglePw" title="Hiện/ẩn">👁</button>
                  <button type="button" class="pw-btn" id="btnGenPw" title="Tạo mật khẩu ngẫu nhiên">⚡</button>
                  <button type="button" class="pw-btn" id="btnCopyPw" title="Copy mật khẩu">📋</button>
                </div>
                <div class="password-strength" id="passwordStrength" style="display:none">
                  <div class="strength-bar"><div class="strength-fill" id="strengthFill"></div></div>
                  <small class="strength-text" id="strengthText"></small>
                </div>
                <small class="f-hint">
                  <c:choose>
                    <c:when test="${isEdit}">Để trống nếu không muốn thay đổi mật khẩu.</c:when>
                    <c:otherwise>Mặc định: 123456 – có thể reset sau.</c:otherwise>
                  </c:choose>
                </small>
              </div>
            </div>

            <div class="uf-field-group">
              <div class="uf-field">
                <label>Trạng thái</label>
                <select name="status" id="statusSelect">
                  <option value="ACTIVE"   <c:if test="${f_status == 'ACTIVE'}">selected</c:if>>ACTIVE</option>
                  <option value="INACTIVE" <c:if test="${f_status == 'INACTIVE'}">selected</c:if>>INACTIVE</option>
                  <option value="SUSPEND"  <c:if test="${f_status == 'SUSPEND'}">selected</c:if>>SUSPEND</option>
                </select>
              </div>

              <div class="uf-field">
                <label>Role <span class="req">*</span></label>
                <select name="role" id="roleSelect" required>
                  <option value="">-- Chọn --</option>
                  <option value="ADMIN"      <c:if test="${f_role == 'ADMIN'}">selected</c:if>>ADMIN</option>
                  <option value="DIV_LEADER" <c:if test="${f_role == 'DIV_LEADER'}">selected</c:if>>DIV_LEADER</option>
                  <option value="TEAM_LEAD"  <c:if test="${f_role == 'TEAM_LEAD'}">selected</c:if>>TEAM_LEAD</option>
                  <option value="STAFF"      <c:if test="${f_role == 'STAFF' || empty f_role}">selected</c:if>>STAFF</option>
                  <option value="INTERN"     <c:if test="${f_role == 'INTERN'}">selected</c:if>>INTERN</option>
                  <option value="ON_PROB"    <c:if test="${f_role == 'ON_PROB'}">selected</c:if>>Trên thử việc</option>
                </select>
              </div>

              <div class="uf-field">
                <label>Phòng ban <span class="req">*</span></label>
                <select name="department" id="departmentSelect" required>
                  <option value="">-- Chọn --</option>
                  <option value="IT"   <c:if test="${f_department == 'IT'}">selected</c:if>>IT</option>
                  <option value="QA"   <c:if test="${f_department == 'QA'}">selected</c:if>>QA</option>
                  <option value="SALE" <c:if test="${f_department == 'SALE'}">selected</c:if>>SALE</option>
                  <option value="HR"   <c:if test="${f_department == 'HR'}">selected</c:if>>HR</option>
                  <option value="FIN"  <c:if test="${f_department == 'FIN'}">selected</c:if>>FIN</option>
                  <option value="MKT"  <c:if test="${f_department == 'MKT'}">selected</c:if>>Marketing</option>
                </select>
              </div>
            </div>

            <details class="uf-advanced">
              <summary>Thiết lập nâng cao</summary>
              <div class="uf-field">
                <label>Ghi chú nội bộ</label>
                <textarea rows="3" name="note" placeholder="VD: Intern 3 tháng, đang chờ ký chính thức, báo cáo trực tiếp cho DIV_LEADER của QA."></textarea>
              </div>
              <div class="uf-adv-hint">Phần này chỉ lưu để quản trị xem, nhân viên không thấy.</div>
            </details>
          </div>
        </div>
      </div>

      <!-- Footer actions -->
      <div class="uf-footer">
        <button type="submit" id="submitBtn" class="primary-btn">
          <span class="btn-text">
            <c:choose>
              <c:when test="${isEdit}">Cập nhật</c:when>
              <c:otherwise>Tạo mới</c:otherwise>
            </c:choose>
          </span>
          <span class="btn-spinner" style="display:none">⏳ Đang gửi...</span>
        </button>
        <a href="${ctx}/admin/users" class="ghost-btn" id="btnCancel">Hủy</a>
        <span class="uf-shortcut">Ctrl+S để lưu • Esc để về danh sách</span>
        <span class="uf-draft" id="draftStatus" style="display:none;">Đã lưu nháp</span>
      </div>
    </form>
  </div>
</div>

<script>
(function(){
  const isEdit = '${isEdit}' === 'true';
  const form = document.getElementById('userForm');
  const submitBtn = document.getElementById('submitBtn');
  const fullName = document.getElementById('fullNameInput');
  const username = document.getElementById('usernameInput');
  const emailInput = document.getElementById('emailInput');
  const phoneInput = document.getElementById('phoneInput');
  const passwordInput = document.getElementById('passwordInput');
  const passwordStrength = document.getElementById('passwordStrength');
  const strengthFill = document.getElementById('strengthFill');
  const strengthText = document.getElementById('strengthText');
  const draftStatus = document.getElementById('draftStatus');
  const avatarInput = document.getElementById('avatarInput');
  const avatarInitial = document.getElementById('avatarInitial');
  const statusSelect = document.getElementById('statusSelect');

  /* Suggest username on name input (create only) */
  if (fullName && username && !isEdit) {
    fullName.addEventListener('input', function () {
      if (username.dataset.touched === '1') return;
      let v = this.value.trim().toLowerCase()
        .normalize("NFD").replace(/[\u0300-\u036f]/g, "")
        .replace(/[^a-z0-9\s]/g, "")
        .replace(/\s+/g, '.');
      username.value = v;
      if (v && avatarInitial) {
        const ch = this.value.trim().charAt(0).toUpperCase();
        if (ch) avatarInitial.textContent = ch;
      }
    });
    username.addEventListener('input', function(){ this.dataset.touched = '1'; });
  }

  /* Password strength meter */
  function updatePwStrength(pwd){
    if (!pwd) { passwordStrength.style.display = 'none'; return; }
    passwordStrength.style.display = 'block';
    let s = 0;
    if (pwd.length >= 8) s++;
    if (pwd.length >= 12) s++;
    if (/[a-z]/.test(pwd) && /[A-Z]/.test(pwd)) s++;
    if (/\d/.test(pwd)) s++;
    if (/[^a-zA-Z0-9]/.test(pwd)) s++;
    const pct = (s/5)*100;
    strengthFill.style.width = pct + '%';
    if (s <= 2)      { strengthFill.style.background = '#ef4444'; strengthText.textContent = 'Yếu'; }
    else if (s === 3){ strengthFill.style.background = '#f59e0b'; strengthText.textContent = 'Trung bình'; }
    else if (s === 4){ strengthFill.style.background = '#3b82f6'; strengthText.textContent = 'Mạnh'; }
    else             { strengthFill.style.background = '#22c55e'; strengthText.textContent = 'Rất mạnh'; }
  }
  if (passwordInput){
    passwordInput.addEventListener('input', function(){ updatePwStrength(this.value); });
  }

  /* Toggle password */
  const btnTogglePw = document.getElementById('btnTogglePw');
  if (btnTogglePw && passwordInput){
    btnTogglePw.addEventListener('click', function(){
      if (passwordInput.type === 'password'){ passwordInput.type = 'text'; this.textContent = '🙈'; }
      else { passwordInput.type = 'password'; this.textContent = '👁'; }
    });
  }

  /* Generate password */
  const btnGenPw = document.getElementById('btnGenPw');
  if (btnGenPw && passwordInput){
    btnGenPw.addEventListener('click', function(){
      const chars = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()';
      let pw = ''; for (let i=0; i<12; i++){ pw += chars.charAt(Math.floor(Math.random()*chars.length)); }
      passwordInput.value = pw; updatePwStrength(pw);
    });
  }

  /* Copy password */
  const btnCopyPw = document.getElementById('btnCopyPw');
  if (btnCopyPw && passwordInput){
    btnCopyPw.addEventListener('click', function(){
      passwordInput.select(); document.execCommand('copy');
      this.textContent = '✅'; setTimeout(()=>{ this.textContent = '📋'; },1500);
    });
  }

  /* Email / phone validation */
  if (emailInput){
    emailInput.addEventListener('blur', function(){
      const v = this.value.trim(), hint = document.getElementById('emailHint');
      if (v && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v)){ this.setCustomValidity('Email không hợp lệ'); if (hint) hint.textContent = 'Email không hợp lệ'; }
      else { this.setCustomValidity(''); if (hint) hint.textContent = ''; }
    });
  }
  if (phoneInput){
    phoneInput.addEventListener('blur', function(){
      const v = this.value.trim(), hint = document.getElementById('phoneHint');
      if (v && !/^[0-9]{9,11}$/.test(v)){ this.setCustomValidity('Số điện thoại phải 9-11 chữ số'); if (hint) hint.textContent = 'Số điện thoại phải 9-11 chữ số'; }
      else { this.setCustomValidity(''); if (hint) hint.textContent = ''; }
    });
  }

  /* Quick role clicks */
  document.querySelectorAll('.qa-btn').forEach(btn=>{
    btn.addEventListener('click', function(){
      const r = this.dataset.role, d = this.dataset.dept;
      const rSel = document.getElementById('roleSelect');
      const dSel = document.getElementById('departmentSelect');
      if (rSel) rSel.value = r;
      if (dSel && d) dSel.value = d;
      document.querySelectorAll('.qa-btn').forEach(b=>b.classList.remove('active'));
      this.classList.add('active');
    });
  });

  /* Auto dept by role (suggest only) */
  const roleSelect = document.getElementById('roleSelect');
  const deptSelect = document.getElementById('departmentSelect');
  if (roleSelect && deptSelect){
    roleSelect.addEventListener('change', function(){
      if (!deptSelect.value){
        const v = this.value;
        if (v === 'ADMIN')      deptSelect.value = 'IT';
        else if (v === 'DIV_LEADER') deptSelect.value = 'QA';
        else if (v === 'TEAM_LEAD')  deptSelect.value = 'IT';
        else if (v === 'INTERN')     deptSelect.value = 'HR';
      }
    });
  }

  /* @company helper */
  const btnAutoMail = document.getElementById('btnAutoMail');
  if (btnAutoMail && emailInput && fullName){
    btnAutoMail.addEventListener('click', function(){
      if (!emailInput.value && fullName.value){
        let base = fullName.value.trim().toLowerCase()
                    .normalize("NFD").replace(/[\u0300-\u036f]/g, "")
                    .replace(/[^a-z0-9\s]/g, "")
                    .replace(/\s+/g, '.');
        emailInput.value = base + '@company.com';
      } else if (emailInput.value && !/@/.test(emailInput.value)){
        emailInput.value = emailInput.value + '@company.com';
      }
    });
  }

  /* Draft (create) */
  const DRAFT_KEY = 'lm_user_form_draft';
  if (!isEdit && form){
    try {
      const j = localStorage.getItem(DRAFT_KEY);
      if (j){
        const data = JSON.parse(j);
        Object.keys(data).forEach(k=>{
          const el = form.querySelector('[name="'+k+'"]');
          if (el) el.value = data[k];
        });
      }
    } catch(e){}
    form.addEventListener('input', function(){
      const obj = {};
      form.querySelectorAll('input,select,textarea').forEach(el=>{ if (el.name) obj[el.name] = el.value; });
      localStorage.setItem(DRAFT_KEY, JSON.stringify(obj));
      if (draftStatus){
        draftStatus.style.display = 'inline-block';
        draftStatus.textContent = 'Đã lưu nháp ' + new Date().toLocaleTimeString();
      }
    });
  }

  /* Update avatar online/offline dot when status changes */
  if (statusSelect){
    statusSelect.addEventListener('change', function(){
      const dot = document.querySelector('.avatar-status');
      if (!dot) return;
      if (this.value === 'INACTIVE') { dot.classList.add('offline'); }
      else { dot.classList.remove('offline'); }
    });
  }

  /* Leave/submit handling */
  let isSubmitting = false;
  if (form){
    form.addEventListener('submit', function(){
      isSubmitting = true;
      const txt = submitBtn.querySelector('.btn-text');
      const sp  = submitBtn.querySelector('.btn-spinner');
      if (txt && sp){ txt.style.display = 'none'; sp.style.display = 'inline-block'; }
      submitBtn.disabled = true;
      if (!isEdit) localStorage.removeItem(DRAFT_KEY);
    });
  }
  window.addEventListener('beforeunload', function(e){
    if (!isSubmitting){ e.preventDefault(); e.returnValue = ''; }
  });

  /* Shortcuts */
  window.addEventListener('keydown', function(e){
    if ((e.ctrlKey || e.metaKey) && e.key === 's'){ e.preventDefault(); if (form) form.submit(); }
    if (e.key === 'Escape'){ const back = document.getElementById('btnCancel'); if (back){ window.location.href = back.getAttribute('href'); } }
  });

  /* Avatar preview */
  if (avatarInput && avatarInitial){
    avatarInput.addEventListener('change', function(e){
      const file = e.target.files[0]; if (!file) return;
      const reader = new FileReader();
      reader.onload = function(ev){
        avatarInitial.style.backgroundImage = 'url(' + ev.target.result + ')';
        avatarInitial.style.backgroundSize = 'cover';
        avatarInitial.style.backgroundPosition = 'center';
        avatarInitial.textContent = '';
      };
      reader.readAsDataURL(file);
    });
  }
})();
</script>
<script>
  // nút Mini/Expand trong sidebar hoặc header hãy cho data-action tương ứng
  document.addEventListener('click', (e)=>{
    if (e.target.closest('[data-action="sidebar-mini"]')) {
      document.body.classList.toggle('sb-mini');
    }
    if (e.target.closest('[data-action="sidebar-open"]')) {
      document.body.classList.toggle('sb-open'); // dùng cho mobile
    }
  });
</script>

<style>
:root{
  --bg:#eef2f7; --card:#fff; --card-2:#fff; --bd:rgba(15,23,42,.08);
  --tx:#0f172a; --muted:#64748b; --pri:#6366f1; --pri2:#4f46e5;
  --err:#ef4444; --ok:#22c55e; --warn:#f97316; --radius:20px;
}
body.dark .user-form-page{
  --bg:#0f172a; --card:rgba(15,23,42,.45); --card-2:rgba(15,23,42,.6);
  --bd:rgba(255,255,255,.08); --tx:#e2e8f0; --muted:rgba(226,232,240,.6);
}

/* === Layout constants === */
:root{
  --sbw: 240px;        /* sidebar width (normal) */
  --sbw-mini: 72px;    /* sidebar width (mini)   */
  --header-h: 60px;    /* header bar height      */
}

/* Sidebar của Audit (file _audit_sidebar.jsp) */
.audit-sidebar, .audit__sidebar{
  position: fixed;
  inset: 0 auto 0 0;   /* top:0; bottom:0; left:0 */
  width: var(--sbw);
  z-index: 900;        /* dưới header một chút */
}

/* Header của Audit (file _audit_header.jsp) nếu đang fixed/sticky */
.audit-header, .audit__header{
  position: sticky;
  top: 0;
  z-index: 950;        /* cao hơn sidebar để nổi phía trên */
}

/* Nội dung chính phải chừa khoảng cho sidebar */
.main-body{
  margin-left: var(--sbw);
  padding-top: calc(var(--header-h) + 10px);
  min-height: 100vh;
}

/* Trạng thái thu gọn (mini) – chỉ cần gán class sb-mini lên body khi bấm nút Mini */
body.sb-mini .audit-sidebar, 
body.sb-mini .audit__sidebar{ width: var(--sbw-mini); }
body.sb-mini .main-body{ margin-left: var(--sbw-mini); }

/* Mobile: đẩy sidebar thành off-canvas, nội dung full width */
@media (max-width: 992px){
  .audit-sidebar, .audit__sidebar{
    transform: translateX(-100%);
    transition: transform .2s ease;
    width: var(--sbw);
  }
  body.sb-open .audit-sidebar, 
  body.sb-open .audit__sidebar{ transform: none; }
  .main-body{ margin-left: 0; }
}

.user-form-page{ background:radial-gradient(circle at top,#e0e7ff,#eef2f7 40%,#fff 70%); padding:2.6rem 0 3.5rem; min-height:calc(100vh - 70px); }
.uf-head{ display:flex; align-items:flex-start; justify-content:space-between; margin-bottom:1.8rem; gap:1rem; }
.uf-title{ font-size:1.6rem; font-weight:700; color:var(--tx); }
.uf-sub{ color:var(--muted); font-size:.9rem; margin-top:.3rem; }
.uf-steps{ display:flex; gap:.6rem; margin-top:.7rem; flex-wrap:wrap; }
.uf-steps .step{ background:rgba(99,102,241,.08); border:1px solid rgba(99,102,241,.15); padding:.35rem .7rem; border-radius:999px; font-size:.75rem; color:#4338ca; display:flex; align-items:center; gap:.35rem; }
.uf-steps .badge-editing{ background:rgba(244,63,94,.12); border:1px solid rgba(244,63,94,.2); padding:.35rem .7rem; border-radius:999px; font-size:.75rem; color:#be123c; }

.avatar-box{ position:relative; width:54px; height:54px; border-radius:999px; border:3px solid rgba(99,102,241,.35); display:flex; align-items:center; justify-content:center; background:linear-gradient(180deg,#6366f1,#4f46e5); color:#fff; font-weight:700; cursor:pointer; overflow:hidden; }
.avatar-box:hover::after{ content:"Đổi"; position:absolute; inset:0; background:rgba(15,23,42,.28); display:flex; align-items:center; justify-content:center; font-size:.6rem; }
.avatar-input{ position:absolute; inset:0; opacity:0; cursor:pointer; }
.avatar-status{ position:absolute; bottom:1px; right:0; width:11px; height:11px; border-radius:999px; border:2.5px solid #fff; background:var(--ok); }
.avatar-status.offline{ background:#f97316; }

.uf-alert{ border-radius:16px; margin-bottom:1.2rem; }
.uf-quick-row{ background:rgba(255,255,255,.7); backdrop-filter:saturate(140%) blur(3px); border:1px solid rgba(15,23,42,.03); border-radius:16px; display:flex; gap:1rem; align-items:center; padding:.7rem 1rem; margin-bottom:1.4rem; box-shadow:0 14px 40px rgba(15,23,42,.03); }
.qa-title{ font-weight:600; white-space:nowrap; }
.qa-btns{ display:flex; gap:.65rem; flex-wrap:wrap; }
.qa-btn{ border:none; background:rgba(99,102,241,.05); border:1px solid rgba(99,102,241,.12); padding:.35rem .7rem; border-radius:999px; font-size:.72rem; display:flex; align-items:center; gap:.35rem; cursor:pointer; transition:.2s; }
.qa-btn:hover{ background:rgba(99,102,241,.15); }
.qa-btn.active{ background:linear-gradient(180deg,#6366f1,#4f46e5); color:#fff; border-color:transparent; box-shadow:0 10px 30px rgba(79,70,229,.18); }

.uf-card{ background:rgba(255,255,255,.75); backdrop-filter:saturate(140%) blur(4px); border:1px solid rgba(15,23,42,.03); border-radius:20px; padding:1.4rem 1.4rem 1.1rem; box-shadow:0 20px 60px rgba(15,23,42,.05); }
.uf-grid{ display:grid; grid-template-columns:minmax(0,1fr) minmax(0,1fr); gap:1.2rem; }
.uf-block{ background:rgba(255,255,255,.35); border:1px solid rgba(15,23,42,.03); border-radius:16px; padding:1.2rem 1rem 1rem; }
.uf-block-title{ font-size:1rem; font-weight:700; margin-bottom:.75rem; }
.uf-field{ margin-bottom:.8rem; }
.uf-field label{ font-weight:600; color:var(--tx); margin-bottom:.35rem; display:block; }
.uf-field input, .uf-field select, .uf-field textarea{ width:100%; border:1px solid rgba(15,23,42,.06); background:rgba(255,255,255,.6); border-radius:10px; padding:.5rem .6rem; font-size:.85rem; transition:.15s; }
.uf-field input:focus, .uf-field select:focus, .uf-field textarea:focus{ outline:none; border-color:rgba(99,102,241,.55); box-shadow:0 0 0 3px rgba(99,102,241,.12); }
.f-hint{ font-size:.68rem; color:var(--muted); margin-top:.25rem; display:block; }
.req{ color:var(--err); }
.uf-field-group{ display:grid; grid-template-columns:repeat(2,minmax(0,1fr)); gap:.7rem; }
.input-with-right{ display:flex; gap:.4rem; }
.input-with-right input{ flex:1; }
.tiny-btn{ border:none; background:rgba(99,102,241,.12); border-radius:10px; padding:0 .7rem; font-size:.7rem; cursor:pointer; transition:.1s; white-space:nowrap; }
.tiny-btn:hover{ background:rgba(99,102,241,.25); }

.pw-wrap{ display:flex; background:rgba(255,255,255,.6); border:1px solid rgba(15,23,42,.06); border-radius:10px; overflow:hidden; }
.pw-wrap input{ border:none; background:transparent; flex:1; padding:.5rem .6rem; }
.pw-btn{ background:transparent; border:none; width:34px; display:flex; align-items:center; justify-content:center; cursor:pointer; font-size:.8rem; opacity:.8; }
.pw-btn:hover{ opacity:1; }
.password-strength{ margin-top:.4rem; }
.strength-bar{ height:4px; background:rgba(15,23,42,.08); border-radius:4px; overflow:hidden; }
.strength-fill{ height:100%; width:0; background:#ef4444; transition:.3s; }
.strength-text{ font-size:.68rem; margin-top:.2rem; color:var(--muted); }

.uf-advanced{ margin-top:.8rem; background:rgba(99,102,241,.03); border:1px dashed rgba(99,102,241,.2); border-radius:10px; padding:.4rem .5rem .2rem; }
.uf-advanced summary{ cursor:pointer; font-weight:600; color:var(--pri); }
.uf-advanced[open]{ background:rgba(99,102,241,.04); }
.uf-adv-hint{ font-size:.7rem; color:var(--muted); }

.uf-footer{ margin-top:1.1rem; display:flex; align-items:center; gap:.6rem; }
.primary-btn{ background:linear-gradient(180deg,var(--pri),var(--pri2)); border:none; color:#fff; padding:.6rem 1.5rem; border-radius:12px; font-weight:600; display:inline-flex; gap:.4rem; align-items:center; cursor:pointer; box-shadow:0 10px 35px rgba(99,102,241,.25); }
.primary-btn:disabled{ opacity:.6; cursor:not-allowed; }
.ghost-btn{ background:transparent; border:1px solid rgba(15,23,42,.12); border-radius:12px; padding:.5rem 1.2rem; text-decoration:none; color:var(--tx); font-weight:500; transition:.1s; }
.ghost-btn:hover{ background:rgba(15,23,42,.04); }
.uf-shortcut{ font-size:.7rem; color:var(--muted); }
.uf-draft{ background:rgba(34,197,94,.12); color:#166534; font-size:.66rem; padding:.25rem .55rem; border-radius:999px; }

@media (max-width:992px){ .uf-grid{grid-template-columns:1fr;} .uf-quick-row{flex-wrap:wrap;} .uf-head{flex-wrap:wrap;} .uf-footer{flex-wrap:wrap;} }
@media (max-width:576px){ .uf-field-group{grid-template-columns:1fr;} .uf-card{padding:1.1rem .9rem .8rem;} .qa-btn{width:100%;justify-content:center;} .uf-head{align-items:flex-start;} }
</style>

<%@ include file="/WEB-INF/views/common/_admin_footer.jsp" %>
