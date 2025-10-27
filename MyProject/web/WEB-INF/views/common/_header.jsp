<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp" %>
<c:set var="cp" value="${pageContext.request.contextPath}" />
<%
  String _uri = request.getRequestURI();
  request.setAttribute("_uri", _uri);
%>

<!-- Skip link cho a11y -->
<a class="skip-link" href="#main" tabindex="0">Bỏ qua tới nội dung</a>




<header class="app-header pro" role="banner">
  <div class="wrap">
    <!-- Left: Brand + Hamburger -->
    <div class="left">
      <a class="brand" href="${cp}/" aria-label="Trang chủ">
        <img src="https://i.imgur.com/tumQO30.png" alt="LeaveMgmt"/>
        <span class="brand-text">LeaveMgmt</span>
      </a>
      <button id="btnHamburger" class="hamburger" aria-label="Mở menu" aria-expanded="false" aria-controls="mainnav" data-no-overlay="true">
        <span></span><span></span><span></span>
      </button>
    </div>

    <!-- Main Nav -->
    <nav id="mainnav" class="nav" role="navigation" aria-label="Chính">
      <c:set var="u"          value="${sessionScope.currentUser}" />
      <c:set var="isAdmin"    value="${u != null && u.role == 'ADMIN'}" />
      <c:set var="isDivLead"  value="${u != null && u.role == 'DIV_LEADER'}" />
      <c:set var="isTeamLead" value="${u != null && u.role == 'TEAM_LEAD'}" />
      <c:set var="isLead"     value="${isDivLead or isTeamLead}" />

      <a href="${cp}/request/list"
         class="navlink ${fn:startsWith(_uri, cp.concat('/request/list')) ? 'active' : ''}"
         aria-current="${fn:startsWith(_uri, cp.concat('/request/list')) ? 'page' : ''}">
        Requests
      </a>

      <a href="${cp}/request/agenda"
         class="navlink ${fn:startsWith(_uri, cp.concat('/request/agenda')) ? 'active' : ''}"
         aria-current="${fn:startsWith(_uri, cp.concat('/request/agenda')) ? 'page' : ''}">
        Agenda
      </a>

      <c:if test="${isAdmin or isLead}">
        <a href="${cp}/request/approve"
           class="navlink ${fn:startsWith(_uri, cp.concat('/request/approve')) ? 'active' : ''}"
           aria-current="${fn:startsWith(_uri, cp.concat('/request/approve')) ? 'page' : ''}">
          Approvals
        </a>
      </c:if>

      <c:if test="${isDivLead}">
        <a href="${cp}/admin/div"
           class="navlink ${fn:startsWith(_uri, cp.concat('/admin/div')) ? 'active' : ''}"
           aria-current="${fn:startsWith(_uri, cp.concat('/admin/div')) ? 'page' : ''}">
          Division Dashboard
        </a>
      </c:if>

      <c:if test="${isAdmin}">
        <a href="${cp}/admin"
           class="navlink ${fn:startsWith(_uri, cp.concat('/admin')) && !fn:startsWith(_uri, cp.concat('/admin/div')) ? 'active' : ''}"
           aria-current="${fn:startsWith(_uri, cp.concat('/admin')) && !fn:startsWith(_uri, cp.concat('/admin/div')) ? 'page' : ''}">
          Admin Dashboard
        </a>
        <a href="${cp}/admin/users"
           class="navlink ${fn:startsWith(_uri, cp.concat('/admin/users')) ? 'active' : ''}"
           aria-current="${fn:startsWith(_uri, cp.concat('/admin/users')) ? 'page' : ''}">
          Users
        </a>
      </c:if>

      <div class="spacer" aria-hidden="true"></div>

      <!-- Quick Search -->
      <form action="${cp}/request/list" method="get" class="quicksearch" data-no-overlay="true" role="search">
        <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true"><path fill="currentColor" d="M10 2a8 8 0 105.293 14.293l4.207 4.207 1.414-1.414-4.207-4.207A8 8 0 0010 2zm0 2a6 6 0 110 12A6 6 0 0110 4z"/></svg>
        <input name="q" id="headSearch" type="search" placeholder="Tìm nhanh (ấn / )..." value="${param.q}" autocomplete="off" />
        <button type="button" class="qs-clear" title="Xóa" aria-label="Xóa tìm kiếm" hidden>&times;</button>
      </form>

      <!-- Notification -->
      <div class="notif-wrap">
        <button id="btnNotif" class="iconbtn" title="Thông báo" aria-haspopup="true" aria-expanded="false" aria-controls="notifmenu" data-no-overlay="true">
          <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true"><path d="M12 22a2 2 0 0 0 2-2H10a2 2 0 0 0 2 2Zm8-6V11a8 8 0 1 0-16 0v5l-2 2v1h20v-1l-2-2Z" fill="currentColor"/></svg>
          <span class="badge" aria-hidden="true">3</span>
        </button>
        <div id="notifmenu" class="menu notif" hidden>
          <div class="menu-title">Thông báo</div>
          <a href="${cp}/request/list">Bạn có 2 đơn chờ duyệt</a>
          <a href="${cp}/request/agenda">Hôm nay có 3 người nghỉ</a>
          <a href="${cp}/admin/users">1 người dùng mới được thêm</a>
          <div class="divider"></div>
          <button type="button" class="markread">Đánh dấu đã đọc</button>
        </div>
      </div>

      <!-- User -->
      <div class="userbox">
        <button id="btnUser" class="userbtn" aria-haspopup="true" aria-expanded="false" aria-controls="usermenu" data-no-overlay="true">
          <span class="avatar" aria-hidden="true">
            <c:choose>
              <c:when test="${not empty u && not empty u.fullName}">${fn:substring(u.fullName,0,1)}</c:when>
              <c:otherwise>U</c:otherwise>
            </c:choose>
          </span>
          <span class="name"><c:out value="${u != null ? u.displayName : 'Guest'}"/></span>
          <svg class="chev" viewBox="0 0 20 20" width="16" height="16" aria-hidden="true"><path d="M5 7l5 6 5-6" fill="none" stroke="currentColor" stroke-width="2"/></svg>
        </button>
        <div id="usermenu" class="menu" hidden>
          <a href="${cp}/profile">Thông tin cá nhân</a>
          <a href="${cp}/activity">Lịch sử hoạt động</a>
          <div class="divider"></div>
          <a class="logout" href="${cp}/logout">Đăng xuất</a>
        </div>
      </div>
    </nav>
  </div>
  <div class="active-underline" aria-hidden="true"></div>
</header>

<!-- App loading overlay (nếu dùng) -->
<div id="appOverlay" class="loading-overlay" aria-hidden="true" aria-live="polite">
  <div class="spinner" role="status" aria-label="Đang tải..."></div>
</div>
<noscript><style>.loading-overlay{display:none!important}</style></noscript>

<style>
  :root{
    --maxw:1180px;
    --bd:#e5e7eb; --tx:#111827; --muted:#6b7280; --bg:#f7f7fb; --card:#fff;
    --pri:#111827; --pri-2:#1f2937; --ring:#60a5fa; --brand:#007bff; --accent:#00c2ff;
  }
  @media (prefers-color-scheme: dark){
    :root{ --bg:#0b0c10; --card:#0f1115; --tx:#e5e7eb; --muted:#9aa0a6; --bd:#1f242b; --pri:#e5e7eb; --pri-2:#fff; --ring:#60a5fa; }
  }

  *{box-sizing:border-box}
  [hidden]{display:none!important}
  html,body{margin:0}
  body{font:14px/1.5 system-ui,Segoe UI,Roboto,Arial;background:var(--bg);color:var(--tx)}

  .skip-link{
    position:absolute;left:-9999px;top:-9999px;background:#111827;color:#fff;padding:8px 12px;border-radius:8px;z-index:2000
  }
  .skip-link:focus{left:12px;top:12px}

  /* ===== Header Pro ===== */
  .app-header.pro{
    position:sticky;top:0;z-index:1100;
    backdrop-filter:saturate(160%) blur(10px);
    background:linear-gradient(180deg, color-mix(in srgb, var(--card) 88%, transparent), color-mix(in srgb,var(--card) 94%, transparent));
    border-bottom:1px solid var(--bd);
    transition:box-shadow .25s ease,border-color .25s ease;
  }
  .app-header.pro.scrolled{box-shadow:0 10px 30px rgba(0,0,0,.10);border-color:color-mix(in srgb,var(--bd) 70%,#000 30%)}
  .app-header .wrap{
    max-width:var(--maxw); margin:0 auto; padding:8px 16px;
    display:flex; align-items:center; gap:12px;
  }

  .left{display:flex;align-items:center;gap:10px}
  .brand{display:flex;align-items:center;gap:10px;color:var(--pri-2);text-decoration:none;font-weight:800;letter-spacing:.2px}
  .brand img{height:32px;width:auto;display:block}
  .brand-text{display:none}
  @media (min-width:900px){ .brand-text{display:inline} }

  .hamburger{display:none;flex-direction:column;gap:4px;border:1px solid var(--bd);border-radius:10px;padding:7px 9px;background:transparent;color:var(--pri);cursor:pointer;transition:.2s}
  .hamburger span{width:18px;height:2px;background:currentColor;display:block;border-radius:1px;transition:transform .2s,opacity .2s}
  .hamburger.active span:nth-child(1){transform:translateY(6px) rotate(45deg)}
  .hamburger.active span:nth-child(2){opacity:0}
  .hamburger.active span:nth-child(3){transform:translateY(-6px) rotate(-45deg)}

  /* ===== Nav ===== */
  .nav{display:flex;align-items:center;gap:4px;flex:1;min-width:0;position:relative}
  .nav .navlink{
    display:inline-block;padding:8px 12px;border:1px solid transparent;border-radius:999px;
    text-decoration:none;color:var(--pri-2);background:transparent;transition:.18s;line-height:1;white-space:nowrap
  }
  .nav .navlink:hover{background:var(--pri);color:#fff}
  .nav .navlink.active{background:var(--pri);color:#fff}
  .nav .navlink:focus-visible{outline:2px solid var(--ring);outline-offset:2px}
  .spacer{flex:1}

  /* underline indicator (di chuyển theo link active) */
  .active-underline{
    position:absolute;left:0;bottom:0;height:2px;width:0;background:linear-gradient(90deg,var(--brand),var(--accent));border-radius:2px;transition:all .25s ease;pointer-events:none
  }

  /* Quick search */
  .quicksearch{position:relative;margin-right:6px}
  .quicksearch svg{position:absolute;left:10px;top:50%;transform:translateY(-50%);opacity:.6}
  .quicksearch input{
    height:36px;min-width:240px;padding:0 34px 0 30px;border-radius:10px;
    border:1px solid var(--bd);background:var(--card);color:var(--tx)
  }
  .quicksearch input::placeholder{color:var(--muted)}
  .quicksearch input:focus{outline:2px solid var(--ring);outline-offset:2px;border-color:transparent}
  .quicksearch .qs-clear{
    position:absolute;right:6px;top:50%;transform:translateY(-50%);
    width:26px;height:26px;border-radius:50%;border:0;background:transparent;color:var(--muted);cursor:pointer
  }

  /* Icon button + badge */
  .iconbtn{
    position:relative;display:inline-grid;place-items:center;
    width:36px;height:36px;border:1px solid var(--bd);
    border-radius:10px;background:var(--card);color:var(--pri-2);margin-right:6px;cursor:pointer;transition:.18s
  }
  .iconbtn:hover{background:color-mix(in srgb,var(--card) 90%,#000 10%)}
  .badge{position:absolute;top:-6px;right:-6px;min-width:16px;height:16px;padding:0 4px;background:#ef4444;color:#fff;font-size:11px;line-height:16px;border-radius:999px}

  .menu{
    position:absolute;right:0;top:110%;background:var(--card);border:1px solid var(--bd);
    border-radius:12px;min-width:240px;box-shadow:0 12px 28px rgba(0,0,0,.18);padding:6px;z-index:1200
  }
  .menu a, .menu .markread, .menu .logout{display:block;width:100%;text-align:left;padding:10px 12px;border-radius:8px;text-decoration:none;color:var(--tx);background:transparent;border:0;cursor:pointer}
  .menu a:hover, .menu .markread:hover, .menu .logout:hover{background:color-mix(in srgb,var(--card) 90%,#000 10%)}
  .menu .divider{height:1px;background:var(--bd);margin:6px 0}
  .menu .logout{color:#ef4444}
  .menu .menu-title{font-weight:700;padding:8px 12px;color:var(--pri-2)}

  .notif-wrap{position:relative}

  /* User box */
  .userbox{position:relative}
  .userbtn{
    display:flex;align-items:center;gap:8px;height:36px;background:var(--card);
    border:1px solid var(--bd);border-radius:10px;padding:0 10px;cursor:pointer;color:var(--pri-2)
  }
  .userbtn:focus-visible{outline:2px solid var(--ring);outline-offset:2px}
  .avatar{display:inline-grid;place-items:center;width:26px;height:26px;border-radius:50%;
    border:1px solid var(--bd);font-weight:700;background:var(--card)}
  .name{max-width:140px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
  .chev{opacity:.6}

  /* Loading overlay */
  .loading-overlay{position:fixed;inset:0;background:rgba(255,255,255,.92);display:flex;align-items:center;justify-content:center;z-index:9999;opacity:1;visibility:visible;transition:opacity .25s ease, visibility .25s ease}
  .loading-overlay.hide{opacity:0;visibility:hidden}
  .spinner{width:44px;height:44px;border-radius:50%;border:3px solid #e5e7eb;border-top-color:#111827;animation:spin .9s linear infinite}
  @keyframes spin{to{transform:rotate(360deg)}}
  @media (prefers-color-scheme: dark){
    .loading-overlay{background:rgba(0,0,0,.6)}
    .spinner{border-color:#2b3138;border-top-color:#e5e7eb}
  }

  /* Mobile */
  @media (max-width: 1100px){
    .quicksearch{display:none}
  }
  @media (max-width: 880px){
    .hamburger{display:flex}
    .nav{
      position:fixed;inset:auto 0 0 0;top:56px;
      display:none;flex-direction:column;gap:10px;background:var(--card);
      border-top:1px solid var(--bd);padding:12px 16px;box-shadow:0 16px 40px rgba(0,0,0,.16)
    }
    .nav.show{display:flex}
    .nav .navlink{width:100%}
  }
</style>

<script>
(function () {
  const $  = (sel, root=document) => root.querySelector(sel);
  const $$ = (sel, root=document) => Array.from(root.querySelectorAll(sel));

  const header   = document.querySelector('.app-header.pro');
  const hamb     = $('#btnHamburger');
  const nav      = $('#mainnav');
  const underline= document.querySelector('.active-underline');
  const btnUser  = $('#btnUser');
  const menuUser = $('#usermenu');
  const btnNotif = $('#btnNotif');
  const menuNotif= $('#notifmenu');
  const overlay  = $('#appOverlay');
  const qs       = $('#headSearch');
  const qsClear  = document.querySelector('.qs-clear');

  /* Header shadow when scrolled */
  const onScrollShadow = () => {
    if (window.scrollY > 6) header.classList.add('scrolled');
    else header.classList.remove('scrolled');
  };
  onScrollShadow(); window.addEventListener('scroll', onScrollShadow);

  /* Active underline positioner */
  function placeUnderline() {
    const active = document.querySelector('.nav .navlink.active');
    if (!active || !underline) { underline.style.width = 0; return; }
    const rect = active.getBoundingClientRect();
    const hw   = header.getBoundingClientRect();
    underline.style.width = rect.width + 'px';
    underline.style.left  = (rect.left - hw.left + 16) + 'px';
    underline.style.bottom= 0;
  }
  placeUnderline();
  window.addEventListener('resize', placeUnderline);

  /* Hamburger / mobile menu */
  if (hamb && nav) {
    const toggle = (open) => {
      nav.classList.toggle('show', open);
      hamb.classList.toggle('active', open);
      hamb.setAttribute('aria-expanded', String(open));
      document.body.style.overflow = open ? 'hidden' : '';
    };
    hamb.addEventListener('click', () => toggle(!nav.classList.contains('show')));
    document.addEventListener('click', (e)=>{
      if (!nav.classList.contains('show')) return;
      if (!nav.contains(e.target) && !hamb.contains(e.target)) toggle(false);
    });
    window.addEventListener('resize', ()=>{ if (window.innerWidth>880) toggle(false); });
  }

  /* Dropdown helpers */
  function dropdown(btn, menu){
    if (!btn || !menu) return;
    const open = () => { menu.removeAttribute('hidden'); btn.setAttribute('aria-expanded','true'); };
    const close= () => { menu.setAttribute('hidden',''); btn.setAttribute('aria-expanded','false'); };
    btn.addEventListener('click', (e)=>{ e.stopPropagation(); menu.hasAttribute('hidden') ? open() : close(); });
    document.addEventListener('click', (e)=>{ if(!menu.contains(e.target) && !btn.contains(e.target)) close(); });
    document.addEventListener('keydown', (e)=>{ if(e.key==='Escape') close(); });
    return {open,close};
  }
  const userDD  = dropdown(btnUser, menuUser);
  const notifDD = dropdown(btnNotif, menuNotif);

  /* Mark notifications as read (demo) */
  const markBtn = menuNotif ? menuNotif.querySelector('.markread') : null;
  if (markBtn && btnNotif) {
    markBtn.addEventListener('click', ()=>{
      const dot = btnNotif.querySelector('.badge');
      if (dot) dot.remove();
      notifDD && notifDD.close();
    });
  }

  /* Overlay lifecycle (giữ như cũ) */
  const hideOverlay = () => overlay && overlay.classList.add('hide');
  const showOverlay = () => overlay && overlay.classList.remove('hide');
  window.addEventListener('load', hideOverlay);
  window.addEventListener('pageshow', hideOverlay);
  document.addEventListener('visibilitychange', () => { if (document.visibilityState === 'visible') hideOverlay(); });
  window.addEventListener('popstate', hideOverlay);
  window.addEventListener('beforeunload', showOverlay);

  /* Chỉ bật overlay khi thật sự điều hướng */
  function shouldIgnoreLinkClick(e, a){
    if (e.defaultPrevented) return true;
    if (e.button !== 0) return true;
    if (e.metaKey || e.ctrlKey || e.shiftKey || e.altKey) return true;
    if (!a) return true;
    if (a.hasAttribute('download')) return true;
    if (a.dataset.noOverlay === 'true') return true;
    const href = a.getAttribute('href') || '';
    if (!href || href.startsWith('#') || href.startsWith('javascript:')) return true;
    const target = (a.getAttribute('target') || '_self').toLowerCase();
    if (target && target !== '_self') return true;
    try { const url = new URL(href, window.location.href); if (url.href === window.location.href) return true; } catch(_){}
    return false;
  }
  document.addEventListener('click', function(e){
    const a = e.target.closest && e.target.closest('a');
    if (shouldIgnoreLinkClick(e, a)) return;
    showOverlay();
  }, true);
  document.addEventListener('submit', function(e){
    const form = e.target;
    if (form && form.dataset.noOverlay === 'true') return;
    showOverlay();
  }, true);

  /* Quick Search: phím tắt, nút clear */
  if (qs) {
    document.addEventListener('keydown', (e)=>{
      if (e.key === '/' && document.activeElement.tagName !== 'INPUT' && document.activeElement.tagName !== 'TEXTAREA') {
        e.preventDefault(); qs.focus(); qs.select();
      }
      // Go-to shortcuts: g r (requests), g a (agenda)
      if (e.key.toLowerCase()==='r' && e.ctrlKey) location.href='${cp}/request/list';
    });
    qs.addEventListener('input', ()=>{
      if (qsClear) qsClear.hidden = !qs.value;
    });
    if (qsClear){
      qsClear.addEventListener('click', ()=>{ qs.value=''; qs.focus(); qsClear.hidden=true; });
    }
  }

  /* Respect Reduced Motion */
  try {
    if (window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
      document.querySelectorAll('.hamburger span').forEach(s=>s.style.transition='none');
    }
  } catch(_){}

  /* Re-place underline after dropdown/layout changes */
  setTimeout(placeUnderline, 0);
})();
</script>
