<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp" %>

<c:set var="cp" value="${pageContext.request.contextPath}" />
<c:set var="u" value="${sessionScope.currentUser}" />
<c:set var="rawRole" value="${empty u ? '' : (not empty u.roleCode ? u.roleCode : (not empty u.role ? u.role : ''))}" />
<c:set var="R" value="${fn:toUpperCase(fn:trim(rawRole))}" />
<c:set var="isAdmin" value="${R eq 'ADMIN' or R eq 'SYS_ADMIN'}" />

<style>
  .au-sb{--bg:#0b1324;--fg:#e7e9ee;--muted:#9aa3b2;--bd:#1c2744;--hover:#131c33;--active:#1a2442;--acc:#5c8dff}
  .au-sb{position:fixed;left:0;top:0;bottom:0;width:var(--sbw);background:var(--bg);color:var(--fg);border-right:1px solid var(--bd);z-index:25}
  .au-sb .brand{padding:14px 16px;border-bottom:1px solid var(--bd);display:flex;align-items:center;justify-content:space-between}
  .au-sb .meta b{display:block;font-size:15px}
  .au-sb .meta small{color:var(--muted)}
  .au-sb .btn{height:28px;padding:0 10px;border-radius:8px;border:1px solid var(--bd);background:#101a32;color:var(--fg);cursor:pointer}
  .au-sb nav{padding:10px}
  .au-sb .sec{margin:10px 0 6px;font-size:11px;letter-spacing:.06em;color:var(--muted);text-transform:uppercase}
  .au-sb a{display:block;padding:8px 14px 8px 22px;color:var(--fg);border-left:2px solid transparent}
  .au-sb a:hover{background:var(--hover)}
  .au-sb a.active{border-left-color:var(--acc);background:var(--active)}
  .au-sb .divider{height:1px;background:var(--bd);margin:10px 0}
  .au-sb.mini{width:72px}
  .au-sb.mini .meta, .au-sb.mini .sec, .au-sb.mini .hide-mini{display:none}
  @media(max-width:1100px){ .au-sb{left:-280px;transition:left .2s ease} .au-sb.open{left:0;box-shadow:0 20px 60px rgba(0,0,0,.35)} }
</style>

<aside class="au-sb" id="auditSidebar" aria-label="Audit navigation">
  <div class="brand">
    <div class="meta">
      <b>Audit</b>
      <small>Role: <c:out value="${R != '' ? R : 'GUEST'}"/></small>
    </div>
    <div>
      <button class="btn" id="btnMiniAu">Mini</button>
    </div>
  </div>

  <nav>
    <div class="sec">Audit</div>
    <a href="${cp}/admin/audit" class="active">Nhật ký hệ thống</a>
    <c:if test="${isAdmin}">
      <a href="${cp}/admin/reports">Bộ báo cáo</a>
    </c:if>

    <div class="divider"></div>
    <div class="sec">Điều hướng</div>
    <a href="${cp}/admin">Dashboard</a>
    <a href="${cp}/">Trang chủ</a>
  </nav>
</aside>

<script>
  (function(){
    const sb = document.getElementById('auditSidebar');
    // set --sbw khi load
    document.documentElement.style.setProperty('--sbw', (sb?.offsetWidth||220)+'px');

    // Mobile toggle hook cho header
    window.toggleSidebar = () => sb.classList.toggle('open');

    // Mini mode
    const k='au.sb.mini';
    if(localStorage.getItem(k)==='1'){ sb.classList.add('mini'); document.getElementById('btnMiniAu').textContent='Full'; document.documentElement.style.setProperty('--sbw', (sb.offsetWidth)+'px');}
    document.getElementById('btnMiniAu').addEventListener('click',()=>{
      sb.classList.toggle('mini');
      const on=sb.classList.contains('mini');
      localStorage.setItem(k, on?'1':'0');
      document.getElementById('btnMiniAu').textContent=on?'Full':'Mini';
      document.documentElement.style.setProperty('--sbw', (sb.offsetWidth)+'px');
    });
  })();
</script>
