<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp" %>
<c:set var="cp" value="${pageContext.request.contextPath}"/>
<c:set var="u"  value="${sessionScope.currentUser}"/>
<c:set var="role" value="${empty u ? '' : (empty u.role ? (empty u.roleCode ? '' : u.roleCode) : u.role)}"/>
<c:set var="R" value="${fn:toUpperCase(fn:trim(role))}"/>
<c:set var="isHR" value="${R=='HR_ADMIN' or R=='DIV_LEADER' or R=='TEAM_LEAD' or R=='ADMIN' or R=='SYS_ADMIN'}"/>
<c:set var="isAdmin" value="${R=='ADMIN' or R=='SYS_ADMIN'}"/>

<style>
  .topbar{position:sticky;top:0;z-index:40;background:var(--card,#fff);border-bottom:1px solid var(--bd,#e5e7eb)}
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

    <div style="display:flex;align-items:center;gap:6px">
      <button class="ibtn" id="btnTheme" title="Đổi theme">🌗</button>
      <button class="ibtn" id="btnDensity" title="Đổi mật độ">⋮</button>
      <a class="btn" href="${cp}/request/create">＋ Tạo đơn</a>
      <div class="avatar" title="${u!=null?u.fullName:'Guest'}"><c:out value="${u!=null?fn:substring(u.fullName,0,1):'G'}"/></div>
    </div>
  </div>
</header>

<script>
(function(){
  // Prefs
  const K='portal.pref', pf=Object.assign({theme:null,density:'normal'}, JSON.parse(localStorage.getItem(K)||'{}'));
  if(pf.theme==='light') document.documentElement.setAttribute('data-theme','light');
  if(pf.density==='compact') document.body.classList.add('compact');
  const elTheme=document.getElementById('btnTheme'), elDen=document.getElementById('btnDensity');
  function toggleTheme(){
    const cur=document.documentElement.getAttribute('data-theme');
    if(cur==='light') document.documentElement.removeAttribute('data-theme');
    else document.documentElement.setAttribute('data-theme','light');
    pf.theme=(cur==='light'?null:'light'); localStorage.setItem(K, JSON.stringify(pf));
  }
  elTheme?.addEventListener('click', toggleTheme);
  elDen?.addEventListener('click', ()=>{const on=document.body.classList.toggle('compact'); pf.density=on?'compact':'normal'; localStorage.setItem(K, JSON.stringify(pf));});
  // Shortcuts
  document.addEventListener('keydown', e=>{
    if(e.key==='/' && !/input|textarea/i.test(e.target.tagName)){ e.preventDefault(); const x=document.getElementById('qGlobal'); x?.focus(); x?.select(); }
    if(e.altKey && e.key.toLowerCase()==='t'){ e.preventDefault(); toggleTheme(); }
  });
})();
</script>
