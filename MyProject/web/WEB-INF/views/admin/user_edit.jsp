<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp" %>
<%@ include file="/WEB-INF/views/common/_header.jsp" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="u" value="${empty u ? user : u}" />

<!-- Chuẩn hoá trạng thái -->
<c:set var="statusText" value="${not empty u && u.status != null ? fn:toUpperCase(u.status) : ''}" />
<c:set var="isActive" value="${statusText eq '1' or statusText eq 'ACTIVE' or statusText eq 'TRUE' or statusText eq 'ON'}" />

<style>
  :root{
    --bg:#f4f5fb; --card:#fff; --stroke:rgba(15,23,42,.06); --ink:#0f172a; --muted:#6b7280;
    --pri:#1f2937; --pri-acc:#111827; --brand:#4f46e5; --brand-soft:rgba(79,70,229,.14);
    --success:#059669; --danger:#dc2626; --radius:18px;
  }
  body{background:radial-gradient(180px 180px at 0 0, rgba(79,70,229,.10), transparent 60%), var(--bg); color:var(--ink)}
  .page-shell{max-width:1060px;margin:20px auto 40px;padding:0 16px}
  .page-title-bar{display:flex;justify-content:space-between;align-items:flex-end;margin-bottom:14px;gap:12px}
  .page-title h1{margin:0;font-size:22px;font-weight:800;letter-spacing:.2px}
  .badge-soft{display:inline-block;margin-left:6px;background:var(--brand-soft);color:#3730a3;padding:2px 8px;border-radius:999px;font-size:12px;font-weight:600}
  .muted-line{margin:.25rem 0 0;color:var(--muted);font-size:13px}

  .user-edit-layout{display:grid;grid-template-columns:300px 1fr;gap:18px;align-items:start}
  @media (max-width:900px){.user-edit-layout{grid-template-columns:1fr}}

  .card{background:var(--card);border:1px solid var(--stroke);border-radius:var(--radius);box-shadow:0 16px 40px rgba(2,6,23,.04)}
  .card-header{padding:14px 18px 10px;border-bottom:1px solid rgba(2,6,23,.05)}
  .card-body{padding:14px 18px 18px}

  .profile-box{text-align:center;padding:18px 16px 12px}
  .avatar-circle{
    width:88px;height:88px;border-radius:999px;margin:0 auto 10px;display:grid;place-items:center;
    background:linear-gradient(135deg,#4f46e5,#1d4ed8);color:#fff;font-weight:800;font-size:28px;
    box-shadow:0 10px 24px rgba(79,70,229,.25)
  }
  .profile-name{font-weight:800;font-size:18px}
  .profile-role{color:var(--muted);font-size:13px;margin-top:2px}

  .status-chip{display:inline-flex;align-items:center;gap:6px;padding:4px 10px;border-radius:999px;font-size:12px;font-weight:600}
  .status-active{background:rgba(5,150,105,.12);color:#065f46}
  .status-inactive{background:rgba(220,38,38,.12);color:#7f1d1d}

  .quick-actions{display:flex;justify-content:center;flex-wrap:wrap;gap:8px;margin-top:12px}
  .tag-btn{border:1px solid rgba(2,6,23,.08);background:#f8fafc;border-radius:999px;padding:6px 10px;cursor:pointer;font-size:12px;font-weight:600}
  .tag-btn:hover{background:#eef2f7}

  .mini-timeline{list-style:none;padding:0;margin:.5rem 0 0}
  .mini-timeline li{display:flex;gap:8px;margin-bottom:6px;align-items:flex-start}
  .mini-dot{width:6px;height:6px;border-radius:999px;background:#4f46e5;margin-top:6px}
  .mini-txt{font-size:12px;line-height:18px}

  .form-row{display:flex;gap:12px}
  .form-group{width:100%;margin-bottom:10px}
  .form-group label{display:flex;justify-content:space-between;align-items:center;font-weight:700;font-size:13px;margin-bottom:6px;color:#111827}

  .input{width:100%;height:38px;padding:8px 12px;border:1px solid rgba(2,6,23,.10);border-radius:12px;background:#f9fafb;font-size:13px}
  .input:focus{outline:none;border-color:#a5b4fc;box-shadow:0 0 0 3px rgba(99,102,241,.15);background:#fff}
  select.input{padding-right:30px;background-image:linear-gradient(45deg,transparent 50%,#94a3b8 55%),linear-gradient(135deg,#94a3b8 45%,transparent 55%);background-position:calc(100% - 18px) 52%, calc(100% - 13px) 52%;background-size:5px 5px,5px 5px;background-repeat:no-repeat}

  .form-footer{display:flex;justify-content:space-between;align-items:center;gap:12px;margin-top:6px}
  .btn{display:inline-flex;align-items:center;gap:8px;border:1px solid rgba(2,6,23,.12);background:#f3f4f6;color:var(--pri-acc);border-radius:10px;padding:8px 12px;font-weight:700;font-size:13px;text-decoration:none;cursor:pointer}
  .btn:hover{filter:brightness(.98)}
  .btn-primary{border-color:#4f46e5;background:linear-gradient(135deg,#4f46e5,#4338ca);color:#fff;box-shadow:0 10px 24px rgba(79,70,229,.25)}
  .btn-danger{border-color:#dc2626;background:#dc2626;color:#fff}
  .btn-ghost{background:transparent;border-color:rgba(2,6,23,.12);color:#475569}

  .toast{position:fixed;top:14px;right:14px;background:#111827;color:#fff;padding:10px 14px;border-radius:10px;font-size:13px;box-shadow:0 16px 36px rgba(0,0,0,.18);display:none;z-index:9999}
</style>

<div class="page-shell">
  <div class="page-title-bar">
    <div class="page-title">
      <h1>Sửa thông tin người dùng <span class="badge-soft">ID #<c:out value="${u.id}"/></span></h1>
      <p class="muted-line">Chỉnh sửa hồ sơ, phân quyền, trạng thái & phòng ban. Thay đổi có hiệu lực ngay.</p>
    </div>
    <div><a class="btn btn-ghost" href="${ctx}/admin/users">Về danh sách</a></div>
  </div>

  <div class="user-edit-layout">
    <!-- Sidebar -->
    <div class="card">
      <div class="profile-box">
        <div class="avatar-circle" id="avatarCircle">
          <c:out value="${empty u.fullName ? (empty u.username ? 'U' : fn:substring(u.username,0,1)) : fn:substring(u.fullName,0,1)}"/>
        </div>
        <div class="profile-name"><c:out value="${empty u.fullName ? u.username : u.fullName}"/></div>
        <div class="profile-role">Vai trò hiện tại: <strong><c:out value="${u.role}"/></strong></div>

        <c:choose>
          <c:when test="${isActive}"><span class="status-chip status-active">Đang hoạt động</span></c:when>
          <c:otherwise><span class="status-chip status-inactive">Đã khóa</span></c:otherwise>
        </c:choose>

        <div class="quick-actions">
          <button type="button" class="tag-btn" onclick="fillLeader()">Gán DIV_LEADER</button>
          <button type="button" class="tag-btn" onclick="fillDept('IT')">Phòng IT</button>
          <button type="button" class="tag-btn" onclick="fillDept('HR')">Phòng HR</button>
          <button type="button" class="tag-btn" onclick="genRandomPass()">Tạo mật khẩu tạm</button>
        </div>
      </div>

      <div class="card-body">
        <h4 style="margin:0 0 6px;font-size:13px;font-weight:800">Hoạt động gần đây</h4>
        <ul class="mini-timeline">
          <li><div class="mini-dot"></div><div class="mini-txt"><strong>Đăng nhập:</strong> ${empty u.lastLogin ? 'Chưa có' : u.lastLogin}</div></li>
          <li><div class="mini-dot"></div><div class="mini-txt"><strong>Tạo lúc:</strong> ${empty u.createdAt ? '—' : u.createdAt}</div></li>
          <li><div class="mini-dot"></div><div class="mini-txt"><strong>Cập nhật bởi Admin:</strong> ${empty u.updatedAt ? '—' : u.updatedAt}</div></li>
        </ul>

        <hr style="border:none;border-top:1px solid rgba(2,6,23,.06);margin:12px 0 10px">

        <p class="muted-line">Hành động nhanh</p>
        <div style="display:flex;gap:8px;flex-wrap:wrap">
          <a class="btn btn-danger" href="${ctx}/admin/users/reset?id=${u.id}">Reset mật khẩu</a>
          <c:choose>
            <c:when test="${isActive}">
              <a class="btn btn-ghost" style="color:#b91c1c" href="${ctx}/admin/users/deactivate?id=${u.id}">Khóa tài khoản</a>
            </c:when>
            <c:otherwise>
              <a class="btn btn-primary" href="${ctx}/admin/users/activate?id=${u.id}">Kích hoạt lại</a>
            </c:otherwise>
          </c:choose>
        </div>
      </div>
    </div>

    <!-- Main form -->
    <form method="post" action="${ctx}/admin/users/edit" class="card" id="userEditForm" autocomplete="off">
      <div class="card-header">
        <h3 style="margin:0 0 2px;font-size:16px;font-weight:800">Thông tin cơ bản</h3>
        <p class="muted-line">Điền đủ các trường cần thiết để phân quyền chính xác.</p>
      </div>
      <div class="card-body">

        <!-- CSRF bắt buộc -->
        <c:if test="${not empty _csrf}">
          <input type="hidden" name="_csrf" value="${_csrf}"/>
        </c:if>

        <input type="hidden" name="id" value="${u.id}"/>

        <div class="form-group">
          <label for="fullName">Họ và tên <span style="color:#dc2626">*</span> <span id="nameLen" class="muted-line"></span></label>
          <input class="input" id="fullName" name="fullName" value="${u.fullName}" maxlength="80" required>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="email">Email <span style="color:#dc2626">*</span></label>
            <input class="input" id="email" name="email" type="email" value="${u.email}" required>
          </div>
          <div class="form-group">
            <label for="username">Username</label>
            <input class="input" id="username" name="username" value="${u.username}" disabled>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="role">Role / Quyền <span style="color:#dc2626">*</span></label>
            <select class="input" name="role" id="role">
              <option value="STAFF"      <c:if test="${u.role=='STAFF'}">selected</c:if>>Nhân viên (STAFF)</option>
              <option value="TEAM_LEAD"  <c:if test="${u.role=='TEAM_LEAD'}">selected</c:if>>Trưởng nhóm (TEAM_LEAD)</option>
              <option value="DIV_LEADER" <c:if test="${u.role=='DIV_LEADER'}">selected</c:if>>Trưởng phòng (DIV_LEADER)</option>
              <option value="HR"         <c:if test="${u.role=='HR'}">selected</c:if>>Nhân sự (HR)</option>
              <option value="ADMIN"      <c:if test="${u.role=='ADMIN'}">selected</c:if>>Quản trị (ADMIN)</option>
            </select>
          </div>

          <div class="form-group">
            <label for="department">Phòng ban</label>
            <input class="input" id="department" name="department" value="${u.department}" placeholder="VD: IT, HR, Sales...">
          </div>
        </div>

        <div class="form-row">
          <div class="form-group" style="max-width:220px">
            <label for="status">Trạng thái</label>
            <select class="input" name="status" id="status">
              <option value="1" <c:if test="${isActive}">selected</c:if>>Đang hoạt động</option>
              <option value="0" <c:if test="${not isActive}">selected</c:if>>Tạm khóa</option>
            </select>
          </div>
          <div class="form-group">
            <label for="note">Ghi chú nội bộ</label>
            <input class="input" id="note" name="note" placeholder="Ví dụ: thử việc, sắp nghỉ, chuyển team...">
          </div>
        </div>

        <div class="form-footer">
          <div style="display:flex;gap:8px;flex-wrap:wrap">
            <button type="submit" class="btn btn-primary" id="submitBtn">Lưu thay đổi</button>
            <a href="${ctx}/admin/users" class="btn btn-ghost">Hủy</a>
          </div>
          <div class="muted-line">Ctrl+S (hoặc ⌘+S) để lưu nhanh</div>
        </div>

      </div>
    </form>
  </div>
</div>

<div class="toast" id="toast">Đã lưu</div>

<script>
  // Đếm ký tự tên
  (function(){
    const ip = document.getElementById('fullName');
    const len = document.getElementById('nameLen');
    if(!ip || !len) return;
    const fn = ()=> len.textContent = (ip.value||'').length + "/80";
    ip.addEventListener('input', fn); fn();
  })();

  // Ctrl/Cmd + S để submit
  document.addEventListener('keydown', function(e){
    if((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === 's'){
      e.preventDefault();
      const f = document.getElementById('userEditForm');
      if(f) f.submit();
    }
  });

  // Avatar initials
  (function(){
    const av = document.getElementById('avatarCircle');
    const name = document.getElementById('fullName');
    if(!av || !name) return;
    const gen = ()=>{
      const v = (name.value||'').trim();
      if(!v){ av.textContent='U'; return; }
      const p = v.split(/\s+/);
      av.textContent = (p[0][0]||'').toUpperCase() + (p.length>1 ? (p[p.length-1][0]||'').toUpperCase() : '');
    };
    name.addEventListener('input', gen); gen();
  })();

  // Toast
  function showToast(msg){
    const t = document.getElementById('toast'); if(!t) return;
    t.textContent = msg || 'Đã lưu'; t.style.display='block'; setTimeout(()=>t.style.display='none', 2300);
  }

  // Hiển thị toast khi ?updated=1
  (function(){
    const q = new URLSearchParams(location.search);
    if(q.get('updated') === '1') showToast('Lưu thành công');
  })();

  // Quick actions
  function fillLeader(){ document.getElementById('role').value='DIV_LEADER'; showToast('Đã gán DIV_LEADER'); }
  function fillDept(d){ document.getElementById('department').value=d; showToast('Đã chọn phòng ' + d); }
  function genRandomPass(){
    const p = Math.random().toString(36).slice(2,10);
    navigator.clipboard && navigator.clipboard.writeText(p);
    showToast('Mật khẩu tạm: ' + p + ' (đã copy)');
  }

  // Validate đơn giản
  (function(){
    const f = document.getElementById('userEditForm');
    if(!f) return;
    f.addEventListener('submit', function(e){
      const email = (document.getElementById('email').value||'').trim();
      if(!/@/.test(email)){
        e.preventDefault(); showToast('Email không hợp lệ');
      }
    });
  })();
</script>

<%@ include file="/WEB-INF/views/common/_footer.jsp" %>
