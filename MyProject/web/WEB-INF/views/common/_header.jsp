<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp" %>
<%
  String _uri = request.getRequestURI();
  request.setAttribute("_uri", _uri);
%>

<c:set var="cp" value="${pageContext.request.contextPath}" />

<header class="app-header" role="banner">
  <!-- Brand + hamburger -->
  <div class="left">
    <a class="brand" href="${cp}/" aria-label="Trang chủ">LeaveMgmt</a>
    <button id="btnHamburger" class="hamburger" aria-label="Mở menu" aria-expanded="false" aria-controls="mainnav">
      <span></span><span></span><span></span>
    </button>
  </div>

  <!-- Main Nav -->
  <nav id="mainnav" class="nav" role="navigation" aria-label="Chính">
    <a href="${cp}/request/list"
       class="${fn:startsWith(_uri, cp.concat('/request')) ? 'active' : ''}"
       aria-current="${fn:startsWith(_uri, cp.concat('/request')) ? 'page' : ''}">
      Requests
    </a>
    <a href="${cp}/request/agenda"
       class="${fn:startsWith(_uri, cp.concat('/request/agenda')) ? 'active' : ''}"
       aria-current="${fn:startsWith(_uri, cp.concat('/request/agenda')) ? 'page' : ''}">
      Agenda
    </a>

    <c:if test="${sessionScope.currentUser != null && (sessionScope.currentUser.admin || sessionScope.currentUser.lead)}">
      <a href="${cp}/request/approve"
         class="${fn:startsWith(_uri, cp.concat('/request/approve')) ? 'active' : ''}"
         aria-current="${fn:startsWith(_uri, cp.concat('/request/approve')) ? 'page' : ''}">
        Approvals
      </a>
    </c:if>

    <c:if test="${sessionScope.currentUser != null && (sessionScope.currentUser.admin || sessionScope.currentUser.lead)}">
      <a href="${cp}/admin"
         class="${
            _uri == cp.concat('/admin') ||
            fn:startsWith(_uri, cp.concat('/admin/dashboard')) ||
            fn:startsWith(_uri, cp.concat('/admin?'))
            ? 'active' : ''}"
         aria-current="${
            _uri == cp.concat('/admin') ||
            fn:startsWith(_uri, cp.concat('/admin/dashboard')) ? 'page' : ''}">
        Dashboard
      </a>
      <c:if test="${sessionScope.currentUser.admin}">
        <a href="${cp}/admin/users"
           class="${fn:startsWith(_uri, cp.concat('/admin/users')) ? 'active' : ''}"
           aria-current="${fn:startsWith(_uri, cp.concat('/admin/users')) ? 'page' : ''}">
          Users
        </a>
      </c:if>
    </c:if>
  </nav>

  <div class="spacer" aria-hidden="true"></div>

  <!-- User dropdown -->
  <div class="userbox">
    <button id="btnUser" class="userbtn" aria-haspopup="true" aria-expanded="false" aria-controls="usermenu">
      <span class="avatar" aria-hidden="true">
        <c:choose>
          <c:when test="${not empty sessionScope.currentUser && not empty sessionScope.currentUser.fullName}">
            ${fn:substring(sessionScope.currentUser.fullName,0,1)}
          </c:when>
          <c:otherwise>U</c:otherwise>
        </c:choose>
      </span>
      <span class="name">
        <c:choose>
          <c:when test="${not empty sessionScope.currentUser}">
            ${sessionScope.currentUser.displayName}
          </c:when>
          <c:otherwise>Guest</c:otherwise>
        </c:choose>
      </span>
      <svg class="chev" viewBox="0 0 20 20" width="16" height="16" aria-hidden="true"><path d="M5 7l5 6 5-6" fill="none" stroke="currentColor" stroke-width="2"/></svg>
    </button>
<div id="usermenu" class="menu">
  <a href="${cp}/profile">Thông tin cá nhân</a>
  <a href="${cp}/activity">Lịch sử hoạt động</a>
  <div class="divider"></div>
  <a class="logout" href="${cp}/logout">Đăng xuất</a>
</div>

    
  </div>
</header>

<!-- Loading Overlay -->
<div id="appOverlay" class="loading-overlay" aria-hidden="true" aria-live="polite">
  <div class="spinner" role="status" aria-label="Đang tải..."></div>
</div>
<noscript>
  <style>.loading-overlay{display:none!important}</style>
</noscript>

<style>
  :root{--bd:#e5e7eb;--tx:#111827;--muted:#6b7280;--bg:#f9fafb;--pri:#111827}
  *{box-sizing:border-box}
  body{margin:0;font:14px/1.45 system-ui,Segoe UI,Roboto,Arial;background:var(--bg);color:var(--tx)}

  .app-header{display:flex;align-items:center;gap:16px;padding:10px 16px;background:#fff;border-bottom:1px solid var(--bd);position:sticky;top:0;z-index:100}
  .left{display:flex;align-items:center;gap:10px}
  .brand{font-weight:800;text-decoration:none;color:var(--pri);letter-spacing:.2px}
  .hamburger{display:none;flex-direction:column;gap:3px;border:1px solid var(--bd);border-radius:8px;padding:6px 8px;background:#fff;cursor:pointer}
  .hamburger span{width:18px;height:2px;background:var(--pri);display:block}

  .nav{display:flex;align-items:center;gap:8px}
  .nav a{display:inline-block;padding:6px 10px;border:1px solid var(--bd);border-radius:10px;text-decoration:none;color:var(--pri);background:#fff;transition:.15s}
  .nav a:hover{background:var(--pri);color:#fff;border-color:var(--pri)}
  .nav a.active{background:var(--pri);color:#fff;border-color:var(--pri)}

  .spacer{flex:1}

  .userbox{position:relative}
  .userbtn{display:flex;align-items:center;gap:10px;background:#fff;border:1px solid var(--bd);border-radius:10px;padding:6px 10px;cursor:pointer;color:var(--pri)}
  .avatar{display:inline-grid;place-items:center;width:28px;height:28px;border-radius:50%;border:1px solid var(--bd);font-weight:700}
  .chev{opacity:.6}
  .menu{position:absolute;right:0;top:110%;background:#fff;border:1px solid var(--bd);border-radius:12px;min-width:220px;box-shadow:0 12px 28px rgba(0,0,0,.08);padding:6px}
  .menu a,.menu .logout{display:block;width:100%;text-align:left;padding:9px 12px;border-radius:8px;border:none;background:#fff;color:var(--pri);text-decoration:none;cursor:pointer}
  .menu a:hover,.menu .logout:hover{background:#f3f4f6}
  .menu .divider{height:1px;background:var(--bd);margin:6px 0}

  /* Loading overlay */
  .loading-overlay{
    position:fixed;inset:0;background:rgba(255,255,255,.9);
    display:flex;align-items:center;justify-content:center;
    z-index:9999;opacity:1;visibility:visible;transition:opacity .25s ease, visibility .25s ease;
  }
  .loading-overlay.hide{opacity:0;visibility:hidden}
  .spinner{
    width:44px;height:44px;border-radius:50%;
    border:3px solid #e5e7eb;border-top-color:#111827;animation:spin .9s linear infinite;
  }
  @keyframes spin{to{transform:rotate(360deg)}}

  /* Mobile */
  @media (max-width: 840px){
    .hamburger{display:flex}
    .nav{display:none;position:absolute;left:0;right:0;top:56px;background:#fff;border-bottom:1px solid var(--bd);padding:10px 16px;flex-direction:column;gap:10px}
    .nav.show{display:flex}
    .nav a{width:100%}
  }
</style>

<script>
  (function () {
    const hamb = document.getElementById('btnHamburger');
    const nav = document.getElementById('mainnav');
    const btnUser = document.getElementById('btnUser');
    const menu = document.getElementById('usermenu');
    const overlay = document.getElementById('appOverlay');

    if (hamb && nav) {
      hamb.addEventListener('click', () => {
        const show = !nav.classList.contains('show');
        nav.classList.toggle('show', show);
        hamb.setAttribute('aria-expanded', show ? 'true' : 'false');
      });
    }

    if (btnUser && menu) {
      btnUser.addEventListener('click', (e) => {
        e.stopPropagation();
        const show = menu.hasAttribute('hidden');
        if (show) menu.removeAttribute('hidden'); else menu.setAttribute('hidden','');
        btnUser.setAttribute('aria-expanded', show ? 'true' : 'false');
      });
      document.addEventListener('click', (e)=>{
        if(!menu.contains(e.target) && !btnUser.contains(e.target)){
          menu.setAttribute('hidden','');
          btnUser.setAttribute('aria-expanded','false');
        }
      });
    }

    // Ẩn overlay khi trang sẵn sàng
    window.addEventListener('load', () => { if (overlay) overlay.classList.add('hide'); });

    // Hiện overlay khi điều hướng (click link) hoặc submit form
    function showOverlay() { if (overlay) overlay.classList.remove('hide'); }
    document.addEventListener('click', function(e){
      const a = e.target.closest('a');
      if (!a) return;
      const href = a.getAttribute('href') || '';
      if (href.startsWith('#') || href.startsWith('javascript:')) return;
      if (a.target && a.target !== '_self') return;
      showOverlay();
    }, true);
    document.addEventListener('submit', function(){ showOverlay(); }, true);
  })();
</script>
