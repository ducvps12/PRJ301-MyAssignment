<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp" %>
<c:set var="cp" value="${pageContext.request.contextPath}"/>
<c:set var="u" value="${sessionScope.currentUser}"/>
<c:set var="role" value="${empty u ? '' : (empty u.role ? (empty u.roleCode ? '' : u.roleCode) : u.role)}"/>
<c:set var="R" value="${fn:toUpperCase(fn:trim(role))}"/>
<c:set var="isHR" value="${R=='HR_ADMIN' or R=='DIV_LEADER' or R=='TEAM_LEAD' or R=='ADMIN' or R=='SYS_ADMIN'}"/>
<c:set var="isAdmin" value="${R=='ADMIN' or R=='SYS_ADMIN'}"/>

<style>
  .topbar{position:sticky;top:0;z-index:40;background:var(--card,#fff);border-bottom:1px solid var(--bd,#e5e7eb)}
  .tb-wrap{max-width:1200px;margin:0 auto;padding:8px 16px;display:flex;align-items:center;gap:10px}
  .tb-brand{display:flex;align-items:center;gap:8px;font-weight:900}
  .tb-brand .logo{width:28px;height:28px;border-radius:8px;background:linear-gradient(180deg,#60a5fa,#3b82f6);display:grid;place-items:center;color:#fff}
  .tb-search{flex:1;display:flex;align-items:center;gap:8px}
  .tb-search input{width:100%;padding:8px 12px;border:1px solid var(--bd);border-radius:10px;background:var(--card-2,#f9fafb)}
  .tb-actions{display:flex;align-items:center;gap:6px}
  .icon-btn{width:34px;height:34px;border:1px solid var(--bd);border-radius:10px;background:var(--card-2,#f9fafb);display:grid;place-items:center;cursor:pointer}
  .btn-primary{padding:8px 12px;border-radius:10px;border:1px solid transparent;background:linear-gradient(180deg,#3b82f6,#2563eb);color:#fff;text-decoration:none}
  .avatar{width:28px;height:28px;border-radius:10px;background:#dbeafe;display:grid;place-items:center;font-weight:800;color:#1e3a8a}
  .tb-link{color:var(--muted,#64748b);text-decoration:none;padding:8px 10px;border-radius:8px}
  .tb-link:hover{background:var(--card-2,#f3f4f6)}
  @media(max-width:1100px){ .tb-search{display:none} }
</style>

<header class="topbar">
  <div class="tb-wrap">
    <!-- Mobile sidebar toggle -->
    <button class="icon-btn" onclick="window.toggleSidebar?.()" title="Menu" aria-label="Menu">☰</button>

    <a class="tb-brand tb-link" href="${cp}/home">
      <span class="logo">LM</span><span>LeaveMgmt</span>
    </a>

    <nav class="tb-nav" style="display:flex;gap:4px">
      <a class="tb-link" href="${cp}/request/list">Requests</a>
      <a class="tb-link" href="${cp}/attendance">Chấm công</a>
      <c:if test="${isHR}">
        <a class="tb-link" href="${cp}/work">Báo cáo</a>
        <a class="tb-link" href="${cp}/work/todos">Việc HR</a>
        <a class="tb-link" href="${cp}/payroll">Lương</a>
        <a class="tb-link" href="${cp}/recruit/job">Tuyển dụng</a>
      </c:if>
      <c:if test="${isAdmin}">
        <a class="tb-link" href="${cp}/admin">Admin</a>
      </c:if>
    </nav>

    <div class="tb-search">
      <input id="qGlobal" type="search" placeholder="Tìm nhanh (ấn /)" />
    </div>

    <div class="tb-actions">
      <button class="icon-btn" id="btnTheme" title="Đổi theme">🌗</button>
      <button class="icon-btn" id="btnDensity" title="Đổi mật độ">⋮</button>
      <a class="btn-primary" href="${cp}/request/create">＋ Tạo đơn</a>
      <div class="avatar" title="${u!=null?u.fullName:'Guest'}">
        <c:out value="${u != null ? fn:substring(u.fullName,0,1) : 'G'}"/>
      </div>
    </div>
  </div>
</header>

<script>
(function(){
  // Shortcuts
  document.addEventListener('keydown', e=>{
    if(e.key==='/' && !/input|textarea/i.test(e.target.tagName)){ e.preventDefault(); const x=document.getElementById('qGlobal'); if(x){x.focus(); x.select();}}
    if(e.altKey && e.key.toLowerCase()==='t') toggleTheme();
  });
  // Theme + Density save
  const prefKey='ui.pref', pf=Object.assign({theme:null,density:'normal'}, JSON.parse(localStorage.getItem(prefKey)||'{}'));
  if(pf.theme==='light') document.documentElement.setAttribute('data-theme','light');
  if(pf.density==='compact') document.body.classList.add('compact');
  document.getElementById('btnTheme')?.addEventListener('click', toggleTheme);
  document.getElementById('btnDensity')?.addEventListener('click', ()=>{
    const now=document.body.classList.toggle('compact'); pf.density=now?'compact':'normal'; localStorage.setItem(prefKey, JSON.stringify(pf));
  });
  function toggleTheme(){ const cur=document.documentElement.getAttribute('data-theme'); const next=cur==='light'?null:'light'; if(next) document.documentElement.setAttribute('data-theme','light'); else document.documentElement.removeAttribute('data-theme'); pf.theme=next; localStorage.setItem(prefKey, JSON.stringify(pf)); }
})();
</script>
