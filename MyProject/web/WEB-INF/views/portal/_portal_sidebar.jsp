<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp"%>
<c:set var="cp" value="${pageContext.request.contextPath}"/>
<c:set var="u" value="${sessionScope.currentUser}"/>
<c:set var="role" value="${empty u ? '' : (empty u.role ? (empty u.roleCode ? '' : u.roleCode) : u.role)}"/>
<c:set var="R" value="${fn:toUpperCase(fn:trim(role))}"/>
<c:set var="isHR" value="${R=='HR_ADMIN' or R=='DIV_LEADER' or R=='TEAM_LEAD' or R=='ADMIN' or R=='SYS_ADMIN'}"/>
<c:set var="isAdmin" value="${R=='ADMIN' or R=='SYS_ADMIN'}"/>
<c:set var="page" value="${empty page ? '' : page}"/>

<style>
  :root{ --sbw:280px }
  .psb{--bg:#0b1324;--fg:#e7e9ee;--muted:#9aa3b2;--bd:#1c2744;--hover:#131c33;--active:#1a2442;--acc:#5c8dff;
       position:fixed;left:0;top:0;bottom:0;width:var(--sbw);background:var(--bg);color:var(--fg);border-right:1px solid var(--bd);
       z-index:30;transition:width .2s ease, transform .2s ease; transform:translateX(0)}
  .psb .brand{padding:14px 16px;border-bottom:1px solid var(--bd);display:flex;align-items:center;justify-content:space-between}
  .psb .meta b{display:block} .psb .meta small{color:var(--muted)}
  .psb nav{padding:10px}
  .psb .sec{margin:10px 0 6px;font-size:11px;color:var(--muted);text-transform:uppercase}
  .psb a{display:block;padding:8px 14px 8px 22px;color:var(--fg);border-left:2px solid transparent;border-radius:8px;text-decoration:none}
  .psb a:hover{background:var(--hover)} .psb a.active{border-left-color:var(--acc);background:var(--active)}
  .psb .btn{height:28px;padding:0 10px;border-radius:8px;border:1px solid var(--bd);background:#101a32;color:var(--fg);cursor:pointer}
  /* Mini */
  .psb.mini{ --sbw:72px } .psb.mini .hide-mini, .psb.mini .sec{ display:none }
  /* Mobile */
  @media(max-width:1100px){ .psb{ transform:translateX(-100%) } .psb.open{ transform:translateX(0); box-shadow:0 20px 60px rgba(0,0,0,.35)} }
  /* Content push */
  .with-psb{ margin-left:var(--sbw); transition:margin-left .2s ease } @media(max-width:1100px){ .with-psb{ margin-left:0 } }
</style>

<aside class="psb" id="portalSidebar" aria-label="Portal navigation">
  <div class="brand">
    <div class="meta"><b class="hide-mini">Navigation</b><small class="hide-mini">Role: <c:out value="${R!='' ? R : 'GUEST'}"/></small></div>
    <button class="btn" id="btnMiniPSB">Mini</button>
  </div>
  <nav>
    <div class="sec">Work</div>
    <a href="${cp}/portal"          class="${page=='portal.home'?'active':''}">Tổng quan</a>
    <a href="${cp}/request/list"    class="${page=='portal.req'?'active':''}">Requests</a>
    <a href="${cp}/attendance"      class="${page=='portal.att'?'active':''}">Chấm công</a>
    <a href="${cp}/work"            class="${page=='portal.work'?'active':''}">Báo cáo</a>
    <a href="${cp}/work/todos"      class="${page=='portal.todos'?'active':''}">Việc HR</a>

    <c:if test="${isHR}">
      <div class="sec">HR</div>
      <a href="${cp}/payroll"       class="${page=='portal.payroll'?'active':''}">Lương thưởng</a>
      <a href="${cp}/recruit/job"   class="${page=='portal.recruit'?'active':''}">Tuyển dụng</a>
    </c:if>

    <c:if test="${isAdmin}">
      <div class="sec">Admin</div>
      <a href="${cp}/admin"         class="${page=='portal.admin'?'active':''}">Dashboard</a>
      <a href="${cp}/admin/users"   class="${page=='portal.users'?'active':''}">Người dùng</a>
      <a href="${cp}/admin/audit"   class="${page=='portal.audit'?'active':''}">Nhật ký</a>
    </c:if>
  </nav>
</aside>

<script>
(function(){
  const sb=document.getElementById('portalSidebar'); const root=document.documentElement;
  // init --sbw dựa trên width hiện tại
  root.style.setProperty('--sbw', (sb.offsetWidth||280)+'px');
  // mini state
  const K='portal.sb.mini', btn=document.getElementById('btnMiniPSB');
  if(localStorage.getItem(K)==='1'){ sb.classList.add('mini'); btn.textContent='Full'; root.style.setProperty('--sbw', sb.offsetWidth+'px'); }
  btn.addEventListener('click',()=>{ sb.classList.toggle('mini'); const on=sb.classList.contains('mini'); localStorage.setItem(K,on?'1':'0'); btn.textContent=on?'Full':'Mini'; root.style.setProperty('--sbw', sb.offsetWidth+'px'); });
  // expose toggle for header
  window.togglePortalSidebar=()=>sb.classList.toggle('open');
})();
</script>
