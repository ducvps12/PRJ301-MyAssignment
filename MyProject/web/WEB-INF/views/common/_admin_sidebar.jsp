<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp" %>

<c:if test="${empty requestScope.__SIDEBAR_INCLUDED}">
  <c:set var="__SIDEBAR_INCLUDED" scope="request" value="1" />

  <%-- context-path tiện dùng --%>
  <c:set var="ctx" value="${empty ctx ? pageContext.request.contextPath : ctx}" />

  <%-- ===== Chuẩn hoá role & set cờ ===== --%>
  <c:set var="u" value="${sessionScope.currentUser}" />
  <c:set var="rawRole" value="${empty u ? '' : (not empty u.roleCode ? u.roleCode : (not empty u.role ? u.role : ''))}" />
  <c:set var="R" value="${fn:toUpperCase(fn:trim(rawRole))}" />

  <c:set var="isAdmin"      value="${R eq 'ADMIN'}"/>
  <c:set var="isSysAdmin"   value="${R eq 'SYS_ADMIN'}"/>
  <c:set var="isHRAdmin"    value="${R eq 'HR_ADMIN'}"/>
  <c:set var="isHRStaff"    value="${R eq 'HR_STAFF'}"/>
  <c:set var="isDeptMgr"    value="${R eq 'DEPT_MANAGER'}"/>
  <c:set var="isDivLead"    value="${R eq 'DIV_LEADER'}"/>
  <c:set var="isTeamLead"   value="${R eq 'TEAM_LEAD'}"/>
  <c:set var="isStaff"      value="${R eq 'STAFF' or R eq 'PROBATION' or R eq 'INTERN'}"/>
  <c:set var="isRestricted" value="${R eq 'SUSPENDED' or R eq 'UNDER_REVIEW' or R eq 'TERMINATED'}"/>

  <%-- ===== CSS riêng cho sidebar (an toàn, không phụ thuộc file ngoài) ===== --%>
  <style>
    .sb{--bg:#0b1324;--fg:#e7e9ee;--muted:#9aa3b2;--bd:#1c2744;--hover:#131c33;--active:#1a2442;--acc:#5c8dff}
    .sb{width:260px;min-height:100vh;background:var(--bg);color:var(--fg);border-right:1px solid var(--bd);position:sticky;top:0}
    .sb .brand{padding:14px 16px;border-bottom:1px solid var(--bd);display:flex;align-items:center;justify-content:space-between;gap:10px}
    .sb .meta b{display:block;font-size:15px}
    .sb .meta small{color:var(--muted)}
    .sb .btn{height:30px;padding:0 10px;border-radius:8px;border:1px solid var(--bd);background:#101a32;color:var(--fg);cursor:pointer}
    .sb nav{padding:10px}
    .sb .sec{margin:10px 0 6px;font-size:11px;letter-spacing:.06em;color:var(--muted);text-transform:uppercase}
    .sb details{border-radius:10px;overflow:hidden;margin-bottom:8px;border:1px solid transparent}
    .sb details[open]{border-color:var(--bd);background:var(--active)}
    .sb summary{list-style:none;cursor:pointer;padding:10px 12px;font-weight:600}
    .sb a{display:block;padding:8px 14px 8px 22px;color:var(--fg);text-decoration:none;border-left:2px solid transparent;font-size:14px}
    .sb a:hover{background:var(--hover)}
    .sb a.active{border-left-color:var(--acc);background:var(--active)}
    .sb .divider{height:1px;background:var(--bd);margin:10px 0}
    .sb .badge{display:inline-block;min-width:20px;padding:0 6px;border-radius:999px;background:#2a3a6a;margin-left:8px;text-align:center;font-size:12px}
    .sb.mini{width:72px}
    .sb.mini .meta, .sb.mini .sec, .sb.mini .group{display:none}
    .sb.mini .brand{justify-content:center}
    @media(max-width:1100px){ .sb{position:fixed;left:-280px;top:0;height:100dvh;transition:left .2s ease} .sb.open{left:0;box-shadow:0 20px 60px rgba(0,0,0,.35)} }
  </style>

  <aside class="sb" id="sidebar" aria-label="Main navigation">
    <div class="brand">
      <div class="meta">
        <b>Admin Panel</b>
        <small>Department: <c:out value="${viewDepartment}"/> · Role: <c:out value="${R != '' ? R : 'GUEST'}"/></small>
      </div>
      <div class="actions">
        <button class="btn" id="btnExpandAll" title="Mở/đóng toàn bộ">Expand</button>
        <button class="btn" id="btnMini" title="Thu gọn sidebar">Mini</button>
      </div>
    </div>

    <nav role="navigation">

      <!-- ADMIN -->
      <c:if test="${isAdmin}">
        <div class="sec">Tổng quan</div>
        <a class="mb8" href="${ctx}/admin">Dashboard</a>
        <div class="divider"></div>
        <details data-key="sb-ops">
          <summary>Vận hành hệ thống</summary>
          <div class="group">
            <a href="${ctx}/admin/users">Tài khoản</a>
            <a href="${ctx}/admin/notifications">Thông báo</a>
            <a href="${ctx}/admin/support">Hỗ trợ</a>
            <a href="${ctx}/admin/settings">Cấu hình (Sys_Settings)</a>
          </div>
        </details>
        <details data-key="sb-audit">
          <summary>Giám sát & báo cáo</summary>
          <div class="group">
            <a href="${ctx}/admin/audit">Nhật ký hệ thống</a>
            <a href="${ctx}/admin/reports">Bộ báo cáo</a>
          </div>
        </details>
        <div class="divider"></div>
        <a href="${ctx}/">Trang chủ</a>
      </c:if>

      <!-- SYS_ADMIN -->
      <c:if test="${isSysAdmin}">
        <div class="sec">Tổng quan</div>
        <a class="mb8" href="${ctx}/admin">Dashboard</a>
        <a class="mb8" href="${ctx}/admin/div">Division Dashboard</a>
        <div class="divider"></div>
        <details data-key="sb-ops">
          <summary>Vận hành hệ thống</summary>
          <div class="group">
            <a href="${ctx}/admin/users">Tài khoản</a>
            <a href="${ctx}/admin/notifications">Thông báo</a>
            <a href="${ctx}/admin/support">Hỗ trợ</a>
            <a href="${ctx}/admin/settings">Cấu hình (Sys_Settings)</a>
          </div>
        </details>
        <details data-key="sb-audit">
          <summary>Giám sát & báo cáo</summary>
          <div class="group">
            <a href="${ctx}/admin/audit">Nhật ký hệ thống</a>
            <a href="${ctx}/admin/reports">Bộ báo cáo</a>
          </div>
        </details>
        <div class="divider"></div>
        <a href="${ctx}/">Trang chủ</a>
      </c:if>

      <!-- HR -->
      <c:if test="${isHRAdmin or isHRStaff}">
        <div class="sec">Tổng quan</div>
        <a class="mb8" href="${ctx}/admin">Dashboard</a>
        <div class="divider"></div>
        <details data-key="sb-org">
          <summary>Tổ chức & nhân sự</summary>
          <div class="group">
            <a href="${ctx}/admin/hr/employees">Nhân sự (Users)</a>
            <c:if test="${isHRAdmin}">
              <a href="${ctx}/admin/hr/roles">Vai trò (Roles)</a>
              <a href="${ctx}/admin/hr/role-history">Lịch sử vai trò</a>
              <a href="${ctx}/admin/hr/departments">Phòng ban</a>
              <a href="${ctx}/admin/hr/divisions">Khối/nhóm</a>
              <a href="${ctx}/admin/hr/titles">Chức danh</a>
              <a href="${ctx}/admin/hr/employment-statuses">Trạng thái làm việc</a>
            </c:if>
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
        <details data-key="sb-audit">
          <summary>Báo cáo</summary>
          <div class="group">
            <a href="${ctx}/admin/reports">Bộ báo cáo</a>
          </div>
        </details>
        <div class="divider"></div>
        <a href="${ctx}/">Trang chủ</a>
      </c:if>

      <!-- LÃNH ĐẠO -->
      <c:if test="${isDivLead or isDeptMgr or isTeamLead}">
        <div class="sec">Tổng quan</div>
        <a class="mb8" href="${ctx}/admin">Dashboard</a>
        <a class="mb8" href="${ctx}/admin/div">Division Dashboard</a>
        <div class="divider"></div>
        <details data-key="sb-requests">
          <summary>Đơn nghỉ phép</summary>
          <div class="group">
            <a href="${ctx}/request/list">Tất cả đơn</a>
            <a href="${ctx}/request/list?me=1">Đơn của tôi</a>
            <a href="${ctx}/admin/requests/pending">Đang chờ duyệt</a>
            <a href="${ctx}/admin/requests?status=APPROVED">Đã duyệt</a>
            <a href="${ctx}/admin/requests?status=REJECTED">Từ chối</a>
            <a href="${ctx}/request/new">Tạo đơn mới</a>
          </div>
        </details>
        <details data-key="sb-audit">
          <summary>Báo cáo</summary>
          <div class="group">
            <a href="${ctx}/admin/reports/requests/daily">Theo ngày</a>
            <a href="${ctx}/admin/reports/requests/monthly">Theo tháng</a>
          </div>
        </details>
        <div class="divider"></div>
        <a href="${ctx}/">Trang chủ</a>
      </c:if>

      <!-- NHÂN VIÊN -->
      <c:if test="${isStaff}">
        <div class="sec">Tổng quan</div>
        <a class="mb8" href="${ctx}/admin">Dashboard</a>
        <div class="divider"></div>
        <details data-key="sb-requests">
          <summary>Đơn nghỉ phép</summary>
          <div class="group">
            <a href="${ctx}/request/list?me=1">Đơn của tôi</a>
            <a href="${ctx}/request/new">Tạo đơn mới</a>
          </div>
        </details>
        <div class="divider"></div>
        <a href="${ctx}/">Trang chủ</a>
      </c:if>

      <!-- RESTRICTED -->
      <c:if test="${isRestricted}">
        <div class="sec">Tài khoản</div>
        <a class="mb8" href="${ctx}/profile">Hồ sơ của tôi</a>
        <div class="divider"></div>
        <a href="${ctx}/">Trang chủ</a>
      </c:if>

      <!-- GUEST -->
      <c:if test="${empty R}">
        <div class="sec">Chung</div>
        <a class="mb8" href="${ctx}/login">Đăng nhập</a>
        <a href="${ctx}/">Trang chủ</a>
      </c:if>

    </nav>
  </aside>

  <script>
    (function(){
      var sb = document.getElementById('sidebar');
      if(!sb) return;

      // Active link + auto open group
      var here = location.pathname + location.search;
      var groupsToOpen = new Set();
      sb.querySelectorAll('a[href]').forEach(function(a){
        var href=a.getAttribute('href'); if(!href) return;
        if(here.indexOf(href)===0){ a.classList.add('active'); var d=a.closest('details'); if(d) groupsToOpen.add(d); }
      });
      groupsToOpen.forEach(function(d){ d.open=true; });

      // Remember open/close of details
      var boxes = sb.querySelectorAll('details[data-key]');
      boxes.forEach(function(d){
        var k=d.getAttribute('data-key'), s=localStorage.getItem(k);
        if(s==='open') d.open=true; if(s==='close') d.open=false;
        d.addEventListener('toggle', function(){ localStorage.setItem(k, d.open?'open':'close'); });
      });

      // Expand / Collapse all
      var btnEx=document.getElementById('btnExpandAll');
      function refreshBtn(){ btnEx.textContent = Array.from(boxes).some(d=>!d.open) ? 'Expand' : 'Collapse'; }
      btnEx && btnEx.addEventListener('click', function(){
        var anyClosed = Array.from(boxes).some(d=>!d.open);
        boxes.forEach(function(d){ d.open=anyClosed; localStorage.setItem(d.getAttribute('data-key'), d.open?'open':'close'); });
        refreshBtn();
      });
      btnEx && refreshBtn();

      // Mini mode
      var btnMini=document.getElementById('btnMini'); var miniKey='sb.mini';
      if(localStorage.getItem(miniKey)==='1'){ sb.classList.add('mini'); if(btnMini) btnMini.textContent='Full'; }
      btnMini && btnMini.addEventListener('click', function(){
        sb.classList.toggle('mini'); var on=sb.classList.contains('mini');
        localStorage.setItem(miniKey, on?'1':'0'); btnMini.textContent=on?'Full':'Mini';
      });

      // Mobile toggle helper (gọi từ topbar: window.toggleSidebar())
      window.toggleSidebar = function(){ sb.classList.toggle('open'); };
    })();
  </script>
</c:if>
