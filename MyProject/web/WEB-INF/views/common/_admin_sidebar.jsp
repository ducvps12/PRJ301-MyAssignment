<%-- _admin_sidebar.jsp (FINAL)
  - KHÔNG khai báo taglib ở partial.
  - Trang ngoài phải include: /WEB-INF/views/common/_taglibs.jsp (jakarta.tags.*)
--%>

<%-- đảm bảo có ctx; nếu trang ngoài đã set thì giữ nguyên --%>
<c:set var="ctx" value="${empty ctx ? pageContext.request.contextPath : ctx}" />
<c:set var="u"   value="${sessionScope.currentUser}" />
<c:set var="r"   value="${u != null ? u.role : ''}" />

<%-- PHÂN QUYỀN MENU --%>
<c:set var="canDashboard"
       value="${r eq 'ADMIN' or r eq 'SYS_ADMIN' or r eq 'DIV_LEADER' or r eq 'TEAM_LEAD' or r eq 'HR_ADMIN' or r eq 'DEPT_MANAGER'}" />
<c:set var="canRequests"  value="${r ne 'TERMINATED' and r ne 'SUSPENDED' and r ne ''}" />
<c:set var="canUsers"     value="${r eq 'ADMIN' or r eq 'SYS_ADMIN' or r eq 'HR_ADMIN' or r eq 'DEPT_MANAGER'}" />
<c:set var="canDivDash"   value="${r eq 'DIV_LEADER' or r eq 'DEPT_MANAGER' or r eq 'ADMIN' or r eq 'SYS_ADMIN'}" />
<c:set var="canHR"        value="${r eq 'HR_ADMIN' or r eq 'HR_STAFF' or r eq 'ADMIN' or r eq 'SYS_ADMIN'}" />
<c:set var="canSettings"  value="${r eq 'ADMIN' or r eq 'SYS_ADMIN' or r eq 'HR_ADMIN' or r eq 'DEPT_MANAGER'}" />

<style>
  .sb{--bg:#0b1324;--fg:#e7e9ee;--muted:#9aa3b2;--bd:#1c2744;--hover:#131c33;--active:#1a2442;--acc:#5c8dff}
  .sb{width:260px;min-height:100vh;background:var(--bg);color:var(--fg);border-right:1px solid var(--bd);position:sticky;top:0}
  .sb .brand{padding:14px 16px;border-bottom:1px solid var(--bd);display:flex;align-items:center;justify-content:space-between;gap:10px}
  .sb .brand .meta{line-height:1.35}
  .sb .brand b{display:block;font-size:15px}
  .sb .brand small{color:var(--muted)}
  .sb .brand .actions{display:flex;gap:6px}
  .sb .btn{height:30px;padding:0 10px;border-radius:8px;border:1px solid var(--bd);background:#101a32;color:var(--fg);cursor:pointer}
  .sb .btn:hover{background:var(--hover)}
  .sb nav{padding:10px}
  .sb .sec{margin:10px 0 6px;font-size:11px;letter-spacing:.06em;color:var(--muted);text-transform:uppercase}
  .sb details{border-radius:10px;overflow:hidden;margin-bottom:8px;border:1px solid transparent}
  .sb details[open]{border-color:var(--bd);background:var(--active)}
  .sb summary{list-style:none;cursor:pointer;padding:10px 12px;font-weight:600;outline:none}
  .sb summary:focus-visible{box-shadow:0 0 0 2px var(--acc) inset}
  .sb summary::-webkit-details-marker{display:none}
  .sb .group{padding:6px 0 10px}
  .sb a{display:block;padding:8px 14px 8px 22px;color:var(--fg);text-decoration:none;border-left:2px solid transparent;font-size:14px;outline:none}
  .sb a:hover{background:var(--hover)}
  .sb a:focus-visible{box-shadow:0 0 0 2px var(--acc) inset}
  .sb a.active{border-left-color:var(--acc);background:var(--active)}
  .sb .divider{height:1px;background:var(--bd);margin:10px 0}
  .sb .badge{display:inline-block;min-width:20px;padding:0 6px;border-radius:999px;background:#2a3a6a;margin-left:8px;text-align:center;font-size:12px}
  .sb .mb8{margin-bottom:8px}
  .sb.mini{width:72px}
  .sb.mini .brand .meta, .sb.mini .sec, .sb.mini .group{display:none}
  .sb.mini .brand{justify-content:center}
  @media(max-width:1100px){
    .sb{position:fixed;left:-280px;top:0;height:100dvh;transition:left .2s ease}
    .sb.open{left:0;box-shadow:0 20px 60px rgba(0,0,0,.35)}
  }
</style>

<aside class="sb" id="sidebar" aria-label="Main navigation">
  <div class="brand">
    <div class="meta">
      <b>Admin Panel</b>
      <small>Department: <c:out value="${viewDepartment}"/> · Role: <c:out value="${r != '' ? r : 'Guest'}"/></small>
    </div>
    <div class="actions">
      <button class="btn" id="btnExpandAll" title="Mở/đóng toàn bộ">Expand</button>
      <button class="btn" id="btnMini" title="Thu gọn sidebar">Mini</button>
    </div>
  </div>

  <nav role="navigation">
    <c:if test="${canDashboard}">
      <div class="sec">Tổng quan</div>
      <a class="mb8" href="${ctx}/admin">Dashboard
        <c:if test="${not empty requestScope.kpis and requestScope.kpis.pendingAll > 0}">
          <span class="badge">${requestScope.kpis.pendingAll}</span>
        </c:if>
      </a>
      <c:if test="${canDivDash}">
        <a class="mb8" href="${ctx}/admin/div">Division Dashboard</a>
      </c:if>
      <div class="divider"></div>
    </c:if>

    <c:if test="${canRequests}">
      <details data-key="sb-requests">
        <summary>Đơn nghỉ phép</summary>
        <div class="group">
          <a href="${ctx}/request/list">Tất cả đơn</a>
          <a href="${ctx}/request/list?me=1">Đơn của tôi</a>
          <a href="${ctx}/admin/requests/pending">Đang chờ duyệt
            <c:if test="${requestScope.kpis.pendingMyTeam > 0}">
              <span class="badge">${requestScope.kpis.pendingMyTeam}</span>
            </c:if>
          </a>
          <a href="${ctx}/admin/requests?status=APPROVED">Đã duyệt</a>
          <a href="${ctx}/admin/requests?status=REJECTED">Từ chối</a>
          <a href="${ctx}/request/new">Tạo đơn mới</a>
          <div class="divider"></div>
          <a href="${ctx}/admin/reports/requests/daily">Báo cáo theo ngày</a>
          <a href="${ctx}/admin/reports/requests/monthly">Báo cáo theo tháng</a>
        </div>
      </details>
    </c:if>

    <c:if test="${canHR}">
      <details data-key="sb-org">
        <summary>Tổ chức & nhân sự</summary>
        <div class="group">
          <a href="${ctx}/admin/hr/employees">Nhân sự (Users)</a>
          <a href="${ctx}/admin/hr/roles">Vai trò (Roles)</a>
          <a href="${ctx}/admin/hr/role-history">Lịch sử vai trò</a>
          <a href="${ctx}/admin/hr/departments">Phòng ban</a>
          <a href="${ctx}/admin/hr/divisions">Khối/nhóm</a>
          <a href="${ctx}/admin/hr/titles">Chức danh</a>
          <a href="${ctx}/admin/hr/employment-statuses">Trạng thái làm việc</a>
        </div>
      </details>

      <details data-key="sb-leave-master">
        <summary>Danh mục nghỉ phép</summary>
        <div class="group">
          <a href="${ctx}/admin/hr/leave-types">Loại nghỉ</a>
          <a href="${ctx}/admin/hr/approve-rules">Quy tắc duyệt</a>
          <a href="${ctx}/admin/hr/user-leave-balances">Tồn phép</a>
          <a href="${ctx}/admin/hr/holidays">Ngày nghỉ</a>
        </div>
      </details>
    </c:if>

    <c:if test="${canUsers or canSettings}">
      <details data-key="sb-ops">
        <summary>Vận hành hệ thống</summary>
        <div class="group">
          <c:if test="${canUsers}">
            <a href="${ctx}/admin/users">Tài khoản</a>
            <a href="${ctx}/admin/notifications">Thông báo</a>
            <a href="${ctx}/admin/support">Hỗ trợ</a>
          </c:if>
          <c:if test="${canSettings}">
            <a href="${ctx}/admin/settings">Cấu hình (Sys_Settings)</a>
          </c:if>
        </div>
      </details>
    </c:if>

    <details data-key="sb-audit">
      <summary>Giám sát & báo cáo</summary>
      <div class="group">
        <a href="${ctx}/admin/audit">Nhật ký hệ thống</a>
        <a href="${ctx}/admin/reports">Bộ báo cáo</a>
      </div>
    </details>

    <div class="divider"></div>
    <a href="${ctx}/">Trang chủ</a>
  </nav>
</aside>

<script>
(function(){
  var here = location.pathname + location.search;
  var groupsToOpen = new Set();
  document.querySelectorAll('.sb a').forEach(function(a){
    var href = a.getAttribute('href');
    if(href && here.indexOf(href) === 0){
      a.classList.add('active');
      var d = a.closest('details'); if(d) groupsToOpen.add(d);
    }
  });
  groupsToOpen.forEach(function(d){ d.open = true; });

  var boxes = document.querySelectorAll('.sb details[data-key]');
  boxes.forEach(function(d){
    var k = d.getAttribute('data-key');
    var s = localStorage.getItem(k);
    if(s === 'open') d.setAttribute('open','');
    if(s === 'close') d.removeAttribute('open');
    d.addEventListener('toggle', function(){ localStorage.setItem(k, d.open ? 'open' : 'close'); });
  });

  var btnEx = document.getElementById('btnExpandAll');
  function refreshBtnEx(){
    btnEx.textContent = Array.from(boxes).some(function(d){return !d.open;}) ? 'Expand' : 'Collapse';
  }
  btnEx.addEventListener('click', function(){
    var anyClosed = Array.from(boxes).some(function(d){return !d.open;});
    boxes.forEach(function(d){
      d.open = anyClosed;
      var k = d.getAttribute('data-key');
      localStorage.setItem(k, d.open ? 'open' : 'close');
    });
    refreshBtnEx();
  });
  refreshBtnEx();

  var sb = document.getElementById('sidebar');
  var btnMini = document.getElementById('btnMini');
  var miniKey = 'sb.mini';
  if(localStorage.getItem(miniKey) === '1'){ sb.classList.add('mini'); btnMini.textContent='Full'; }
  btnMini.addEventListener('click', function(){
    sb.classList.toggle('mini');
    var on = sb.classList.contains('mini');
    localStorage.setItem(miniKey, on ? '1' : '0');
    btnMini.textContent = on ? 'Full' : 'Mini';
  });

  window.toggleSidebar = function(){ sb.classList.toggle('open'); };

  document.addEventListener('keydown', function(e){
    if(e.key === '[' || e.key === ']'){
      var list = Array.from(document.querySelectorAll('.sb summary'));
      var idx = list.indexOf(document.activeElement);
      idx = idx === -1 ? 0 : (e.key === ']' ? Math.min(idx+1, list.length-1) : Math.max(idx-1, 0));
      list[idx].focus(); e.preventDefault();
    } else if(e.key === 'Enter' && document.activeElement.tagName === 'SUMMARY'){
      document.activeElement.parentElement.open = !document.activeElement.parentElement.open;
    }
  });
})();
</script>
