<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp" %>
<c:set var="cp" value="${pageContext.request.contextPath}"/>
<c:set var="u"  value="${sessionScope.currentUser}"/>
<c:set var="role" value="${empty u ? '' : (empty u.role ? (empty u.roleCode ? '' : u.roleCode) : u.role)}"/>
<c:set var="R" value="${fn:toUpperCase(fn:trim(role))}"/>
<c:set var="isHR" value="${R=='HR_ADMIN' or R=='DIV_LEADER' or R=='TEAM_LEAD' or R=='ADMIN' or R=='SYS_ADMIN'}"/>
<c:set var="isAdmin" value="${R=='ADMIN' or R=='SYS_ADMIN'}"/>

<style>
  /* header né sidebar bằng --sbw */
  .topbar{
    position:sticky;top:0;z-index:60;
    background:var(--card,#fff);
    border-bottom:1px solid var(--bd,#e5e7eb);
    margin-left:var(--sbw);
    width:calc(100% - var(--sbw));
    transition:margin-left .25s ease,width .25s ease;
  }
  @media(max-width:1100px){ .topbar{margin-left:0;width:100%;} }

  .tb{max-width:1200px;margin:0 auto;padding:8px 16px;display:flex;align-items:center;gap:10px}
  .logo{width:28px;height:28px;border-radius:8px;background:linear-gradient(180deg,#60a5fa,#2563eb);display:grid;place-items:center;color:#fff;font-weight:900}
  .brand{display:flex;align-items:center;gap:8px;font-weight:900;text-decoration:none;color:inherit}
  .nav{display:flex;gap:6px}
  .nav a{color:var(--muted,#64748b);text-decoration:none;padding:8px 10px;border-radius:8px}
  .nav a:hover{background:var(--card-2,#f3f4f6)}
  .search{flex:1}
  .search input{width:100%;padding:8px 12px;border:1px solid var(--bd);border-radius:10px;background:var(--card-2,#f9fafb)}
  .ibtn{width:34px;height:34px;border:1px solid var(--bd);border-radius:10px;background:var(--card-2,#f9fafb);display:grid;place-items:center;cursor:pointer}
  .btn{padding:8px 12px;border-radius:10px;border:1px solid transparent;background:linear-gradient(180deg,#3b82f6,#2563eb);color:#fff;text-decoration:none}
  .avatar{width:28px;height:28px;border-radius:10px;background:#dbeafe;display:grid;place-items:center;font-weight:800;color:#1e3a8a}
  @media(max-width:1100px){ .search{display:none} }

  /* Quick actions + dropdown */
  .qa-wrap{display:flex;align-items:center;gap:6px;margin-left:auto}
  .qa-pill{display:inline-flex;align-items:center;gap:8px;padding:6px 10px;border:1px solid var(--bd,#e5e7eb);border-radius:999px;background:var(--card-2,#f9fafb);text-decoration:none;color:inherit;font-weight:600;white-space:nowrap}
  .qa-pill:hover{background:#eef2ff;border-color:#d6dcff}
  .dd{position:relative}
  .dd-btn{display:inline-flex;align-items:center;gap:8px}
  .dd-menu{position:absolute;right:0;top:calc(100% + 8px);min-width:220px;background:var(--card,#fff);border:1px solid var(--bd,#e5e7eb);border-radius:12px;padding:8px;box-shadow:0 10px 30px rgba(0,0,0,.08);display:none;z-index:70}
  .dd.open .dd-menu{display:block}
  .dd-menu a{display:block;padding:8px 10px;border-radius:8px;text-decoration:none;color:inherit}
  .dd-menu a:hover{background:var(--card-2,#f3f4f6)}
  .meta-small{font-size:12px;color:var(--muted,#64748b)}
</style>

<header class="topbar">
  <div class="tb">
    <button class="ibtn" onclick="window.togglePortalSidebar?.()" title="Menu" aria-label="Menu">☰</button>
    <a href="${cp}/portal" class="brand"><span class="logo">LM</span><span>LeaveMgmt</span></a>

    <nav class="nav">
      <a href="${cp}/request/list">Requests</a>
      <a href="${cp}/attendance">Chấm công</a>
      <c:if test="${isHR}">
        <a href="${cp}/work">Báo cáo</a>
        <a href="${cp}/work/todos">Việc HR</a>
        <a href="${cp}/payroll">Lương</a>
        <a href="${cp}/recruit/job">Tuyển dụng</a>
      </c:if>
      <c:if test="${isAdmin}">
        <a href="${cp}/admin">Admin</a>
      </c:if>
    </nav>

    <div class="search"><input id="qGlobal" type="search" placeholder="Tìm nhanh (ấn /)" /></div>

    <!-- QUICK ACTIONS + USER DROPDOWN -->
    <div class="qa-wrap">
      <a class="qa-pill" href="${cp}/request/create">＋ Tạo đơn</a>
      <a class="qa-pill" href="${cp}/attendance">⏱️ Chấm công</a>
      <a class="qa-pill" href="${cp}/work">📊 Báo cáo</a>

      <button class="ibtn" id="btnTheme" title="Đổi theme">🌗</button>
      <button class="ibtn" id="btnDensity" title="Đổi mật độ">⋮</button>

      <div class="dd" id="ddUser">
        <button class="ibtn dd-btn" aria-haspopup="true" aria-expanded="false" aria-controls="menuUser">
          <div class="avatar" title="${u ne null ? u.fullName : 'Guest'}">
            <c:out value="${u ne null ? fn:substring(u.fullName,0,1) : 'G'}"/>
          </div>
        </button>
        <div class="dd-menu" id="menuUser" role="menu" aria-label="Tài khoản">
          <div style="padding:6px 8px 10px">
            <b><c:out value="${u ne null ? u.fullName : 'Guest'}"/></b><br>
            <small class="meta-small"><c:out value="${u ne null ? (empty u.email ? '' : u.email) : ''}"/></small>
          </div>
          <div style="height:1px;background:var(--bd,#e5e7eb);margin:6px 0"></div>
          <a role="menuitem" href="${cp}/account/profile">Hồ sơ</a>
          <a role="menuitem" href="${cp}/account/security">Bảo mật</a>
          <div style="height:1px;background:var(--bd,#e5e7eb);margin:6px 0"></div>
          <a role="menuitem" href="${cp}/logout">Đăng xuất</a>
        </div>
      </div>
    </div>
  </div>
</header>

<script>
(function(){
  // Prefs
  const K='portal.pref',
        pf=Object.assign({theme:null,density:'normal'}, JSON.parse(localStorage.getItem(K)||'{}'));
  if(pf.theme==='light') document.documentElement.setAttribute('data-theme','light');
  if(pf.density==='compact') document.body.classList.add('compact');

  const elTheme=document.getElementById('btnTheme'),
        elDen=document.getElementById('btnDensity');

  function toggleTheme(){
    const cur=document.documentElement.getAttribute('data-theme');
    if(cur==='light') document.documentElement.removeAttribute('data-theme');
    else document.documentElement.setAttribute('data-theme','light');
    pf.theme=(cur==='light'?null:'light'); localStorage.setItem(K, JSON.stringify(pf));
  }
  elTheme?.addEventListener('click', toggleTheme);
  elDen?.addEventListener('click', ()=>{
    const on=document.body.classList.toggle('compact');
    pf.density=on?'compact':'normal';
    localStorage.setItem(K, JSON.stringify(pf));
  });

  // Search shortcuts + dropdown user
  document.addEventListener('keydown', e=>{
    if(e.key==='/' && !/input|textarea/i.test(e.target.tagName)){
      e.preventDefault(); const x=document.getElementById('qGlobal'); x?.focus(); x?.select();
    }
    if(e.altKey && e.key.toLowerCase()==='t'){ e.preventDefault(); toggleTheme(); }
  });

  // user dropdown
  (function(){
    const dd = document.getElementById('ddUser');
    const btn = dd?.querySelector('.dd-btn');
    btn?.addEventListener('click', ()=>{
      dd.classList.toggle('open');
      btn.setAttribute('aria-expanded', dd.classList.contains('open') ? 'true':'false');
    });
    document.addEventListener('click', (e)=>{
      if(!dd.contains(e.target)) dd.classList.remove('open');
    });
  })();
})();
</script>
