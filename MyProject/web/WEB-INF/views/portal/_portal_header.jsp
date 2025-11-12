<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp" %>
<c:set var="cp"   value="${pageContext.request.contextPath}"/>
<c:set var="u"    value="${sessionScope.currentUser}"/>
<c:set var="role" value="${empty u ? '' : (empty u.role ? (empty u.roleCode ? '' : u.roleCode) : u.role)}"/>
<c:set var="R"    value="${fn:toUpperCase(fn:trim(role))}"/>
<c:set var="isHR"    value="${R=='HR_ADMIN' or R=='DIV_LEADER' or R=='TEAM_LEAD' or R=='ADMIN' or R=='SYS_ADMIN'}"/>
<c:set var="isAdmin" value="${R=='ADMIN' or R=='SYS_ADMIN'}"/>
<c:set var="noti" value="${empty sessionScope.notiCount ? 0 : sessionScope.notiCount}"/>

<style>
  html,body{margin:0;padding:0}

  :root{
    --ph-h:64px;
    --bg:rgba(255,255,255,.65);
    --tx:#0f172a; --muted:#64748b; --bd:#e5e7eb; --ring:#93c5fd;
    --card:#fff; --pri:#2563eb; --ok:#16a34a;
  }
  @media (prefers-color-scheme: dark){
    :root{
      --bg:rgba(15,23,42,.45);
      --tx:#e5e7eb; --muted:#94a3b8; --bd:#1f2937; --ring:#60a5fa;
      --card:#0f172a; --pri:#3b82f6;
    }
  }

  .visually-hidden{position:absolute!important;clip:rect(0 0 0 0)!important;clip-path:inset(50%)!important;height:1px!important;width:1px!important;overflow:hidden!important;white-space:nowrap!important;padding:0!important;border:0!important}

  /* ===== Header ===== */
  #portalHeader{
    position:fixed; top:0; left:0; right:0; z-index:100;
    backdrop-filter:saturate(140%) blur(10px);
    background:var(--bg);
    border-bottom:1px solid var(--bd);
    min-height:var(--ph-h);
    padding-top:env(safe-area-inset-top);
    width:100%;
    box-sizing:border-box;
  }
  @supports not ((backdrop-filter: blur(1px))) {
    #portalHeader{ background: color-mix(in oklab, var(--card) 92%, white); }
  }
  @media (prefers-reduced-motion: reduce) {
    #portalHeader{ backdrop-filter:none }
  }

  .tb{
    max-width:1280px;
    margin:0 auto;
    padding:10px clamp(10px,2.5vw,16px);
    display:flex; align-items:center; gap:10px; width:100%;
  }

  .ibtn{appearance:none;border:1px solid var(--bd);background:transparent;border-radius:10px;padding:8px 10px;cursor:pointer;color:inherit;min-height:36px}
  @media (hover:hover){ .ibtn:hover{background:color-mix(in oklab,var(--pri) 8%, transparent)} }
  .ibtn:focus-visible{outline:2px solid var(--ring);outline-offset:2px}

  .brand{display:flex;align-items:center;gap:10px;color:inherit;text-decoration:none;font-weight:800;white-space:nowrap}
  .logo{width:30px;height:30px;border-radius:10px;background:linear-gradient(180deg,#60a5fa,#2563eb);color:#fff;display:grid;place-items:center;font-weight:900}

  .nav{display:flex;align-items:center;gap:4px;margin-left:6px;flex-wrap:wrap}
  .nav a{display:inline-flex;align-items:center;gap:8px;color:var(--muted);text-decoration:none;padding:8px 10px;border-radius:10px}
  @media (hover:hover){ .nav a:hover{background:color-mix(in oklab,var(--pri) 8%, transparent);color:var(--tx)} }
  .nav a.active{background:color-mix(in oklab,var(--pri) 14%, transparent);color:var(--tx);font-weight:700}
  .nav a[aria-current="page"]{font-weight:800}

  .svg{width:18px;height:18px;display:block}
  .search{flex:1;max-width:520px}
  .search .inp{width:100%;padding:10px 12px 10px 36px;border:1px solid var(--bd);border-radius:12px;background:var(--card);color:inherit;outline:none;-webkit-appearance:none}
  .search .inp:focus{border-color:var(--ring);box-shadow:0 0 0 3px color-mix(in oklab,var(--ring) 35%, transparent)}
  .search .kbd{position:absolute;inset:10px auto auto 12px;opacity:.6}
  .search-wrap{position:relative;width:100%}

  .qa{display:flex;align-items:center;gap:8px;margin-left:auto}
  .pill{display:inline-flex;align-items:center;gap:8px;padding:8px 12px;border:1px solid var(--bd);border-radius:999px;background:var(--card);text-decoration:none;color:inherit;font-weight:600;white-space:nowrap;min-height:36px}
  @media (hover:hover){ .pill:hover{border-color:color-mix(in oklab,var(--pri) 35%, var(--bd))} }
  .pill .svg{width:16px;height:16px}

  .bell{position:relative}
  .bell .dot{position:absolute;top:-4px;right:-4px;min-width:18px;height:18px;border-radius:999px;background:#ef4444;color:#fff;font-size:11px;display:grid;place-items:center;padding:0 5px;border:1px solid rgba(0,0,0,.05)}

  .dd{position:relative}
  .avatar{width:30px;height:30px;border-radius:10px;background:#dbeafe;display:grid;place-items:center;font-weight:800;color:#1e3a8a}
  .dd-menu{position:absolute;right:0;top:calc(100% + 8px);min-width:240px;background:var(--card);border:1px solid var(--bd);border-radius:12px;padding:8px;box-shadow:0 12px 30px rgba(0,0,0,.12);display:none;z-index:70}
  .dd.open .dd-menu{display:block}
  .dd-menu a{display:block;padding:10px;border-radius:10px;text-decoration:none;color:inherit}
  @media (hover:hover){ .dd-menu a:hover{background:color-mix(in oklab,var(--pri) 8%, transparent)} }

  .chip{padding:2px 8px;border-radius:999px;border:1px solid var(--bd);font-size:12px}
  .spacer{flex:1}

  @media(max-width:1100px){ .search{max-width:360px} }
  @media(max-width:900px){ .search{display:none} }
  @media(max-width:860px){ .nav{display:none} }
</style>

<header id="portalHeader" role="banner">
  <div class="tb">
    <button class="ibtn" id="btnHamburger" title="Menu" aria-label="Menu">
      <svg class="svg" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M3 6h18M3 12h18M3 18h18"/></svg>
    </button>

    <a href="${cp}/portal" class="brand" aria-label="Trang chủ Portal">
      <span class="logo">LM</span><span>LeaveMgmt</span>
      <c:if test="${not empty u}">
        <span class="chip" title="Role của bạn" style="margin-left:4px">${fn:toUpperCase(role)}</span>
      </c:if>
    </a>

    <nav class="nav" aria-label="Điều hướng chính">
      <a href="${cp}/request/list" data-nav="requests" title="Requests">
        <svg class="svg" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M4 6h16M4 12h16M4 18h10"/></svg><span>Requests</span>
      </a>
      <a href="${cp}/attendance" data-nav="attendance" title="Chấm công">
        <svg class="svg" viewBox="0 0 24 24" fill="none" stroke="currentColor"><circle cx="12" cy="12" r="9"/><path d="M12 7v6l4 2"/></svg><span>Chấm công</span>
      </a>
      <c:if test="${isHR}">
        <a href="${cp}/work" data-nav="reports" title="Báo cáo">
          <svg class="svg" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M3 12l4 4 7-8 7 10"/><path d="M21 21H3"/></svg><span>Báo cáo</span>
        </a>
        <a href="${cp}/work/todos" data-nav="hr" title="Việc HR">
          <svg class="svg" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M9 11l3 3L22 4"/><path d="M3 7h5M3 12h5M3 17h5"/></svg><span>Việc HR</span>
        </a>
        <a href="${cp}/payroll" data-nav="payroll" title="Lương">
          <svg class="svg" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M3 7h18v10H3z"/><circle cx="12" cy="12" r="2"/></svg><span>Lương</span>
        </a>
        <a href="${cp}/recruit/job" data-nav="recruit" title="Tuyển dụng">
          <svg class="svg" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M4 7h16v10H4z"/><path d="M8 7V5h8v2"/></svg><span>Tuyển dụng</span>
        </a>

   <a href="${cp}/request/approvals" data-nav="recruit" title="Duyệt đơn">
          <svg class="svg" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M4 7h16v10H4z"/><path d="M8 7V5h8v2"/></svg><span>Duyệt đơn</span>
        </a>


        <a href="${cp}/request/agenda" data-nav="recruit" title="Agenda">
          <svg class="svg" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M4 7h16v10H4z"/><path d="M8 7V5h8v2"/></svg><span>Agenda</span>
        </a>
      </c:if>
      <c:if test="${isAdmin}">
        <a href="${cp}/admin" data-nav="admin" title="Admin">
          <svg class="svg" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M12 2l7 4v6c0 5-3 8-7 10-4-2-7-5-7-10V6l7-4z"/></svg><span>Admin</span>
        </a>
      </c:if>
    </nav>

   

    <div class="spacer"></div>

    <div class="qa">
      <a class="pill" href="${cp}/request/create" title="Tạo đơn">
        <svg class="svg" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M12 5v14M5 12h14"/></svg><span>Tạo đơn</span>
      </a>
      <a class="pill" href="${cp}/attendance" title="Chấm công">
        <svg class="svg" viewBox="0 0 24 24" fill="none" stroke="currentColor"><circle cx="12" cy="12" r="9"/><path d="M12 7v6l4 2"/></svg><span>Chấm công</span>
      </a>

      <div class="dd" id="ddNoti">
  <button class="ibtn bell" aria-label="Thông báo" aria-haspopup="menu" aria-expanded="false" title="Thông báo">
    <svg class="svg" viewBox="0 0 24 24" fill="none" stroke="currentColor">
      <path d="M6 8a6 6 0 0 1 12 0v5l2 2H4l2-2V8"/>
      <path d="M10 19a2 2 0 0 0 4 0"/>
    </svg>
    <c:if test="${requestScope.newsUnread gt 0}">
      <span class="dot"><c:out value="${requestScope.newsUnread}"/></span>
    </c:if>
  </button>

  <div class="dd-menu" role="menu" aria-label="Thông báo">
    <div style="padding:6px 8px 10px;display:flex;align-items:center;justify-content:space-between;gap:6px">
      <div><b>Thông báo</b>
        <span class="chip"><c:out value="${requestScope.newsUnread}"/></span>
      </div>
      <form id="markAllNews" method="post" action="${cp}/notif/read-all">
        <button type="submit" class="ibtn" style="padding:6px 8px">Đã đọc hết</button>
      </form>
    </div>

    <c:forEach var="n" items="${requestScope.news}">
      <a class="item" data-id="${n.id}" role="menuitem"
         href="${empty n.linkUrl ? '#' : n.linkUrl}"
         style="display:flex;gap:10px;padding:10px;border-radius:10px;text-decoration:none;color:inherit;border:1px solid var(--bd);margin:6px 0">
        <div class="thumb" style="width:56px;height:40px;border-radius:6px;overflow:hidden;background:#e2e8f0;flex-shrink:0">
          <img loading="lazy"
               src="https://images.pexels.com/photos/3184291/pexels-photo-3184291.jpeg?auto=compress&cs=tinysrgb&w=480&h=300&fit=crop"
               alt="">
        </div>
        <div class="meta" style="flex:1">
          <div class="t" style="font-weight:700">
            <c:out value="${n.title}"/>
            <c:if test="${!n.read}"><span style="color:#16a34a"> • Mới</span></c:if>
          </div>
          <c:if test="${not empty n.body}">
            <div class="d" style="color:var(--muted);font-size:12px"><c:out value="${n.body}"/></div>
          </c:if>
          <div class="d" style="color:var(--muted);font-size:12px">
            <fmt:formatDate value="${n.createdAt}" pattern="dd/MM/yyyy HH:mm"/>
          </div>
        </div>
      </a>
    </c:forEach>

    <c:if test="${empty requestScope.news}">
      <div style="padding:12px;color:var(--muted)">Không có thông báo.</div>
    </c:if>
  </div>
</div>



        
        <div class="dd-menu" role="menu" aria-label="Thông báo">
          <div style="padding:6px 8px 10px"><b>Thông báo</b> <span class="chip">${noti}</span></div>
          <a role="menuitem" href="${cp}/request/list?mine=1">Đơn của tôi</a>
          <a role="menuitem" href="${cp}/attendance">Nhật ký chấm công</a>
        </div>
      </div>

   

      <div class="dd" id="ddUser">
        <button class="ibtn dd-btn" aria-haspopup="menu" aria-expanded="false" aria-controls="menuUser">
          <div class="avatar" title="${u ne null ? u.fullName : 'Guest'}">
            <c:out value="${u ne null ? fn:substring(u.fullName,0,1) : 'G'}"/>
          </div>
        </button>
        <div class="dd-menu" id="menuUser" role="menu" aria-label="Tài khoản" tabindex="-1">
          <div style="padding:6px 8px 10px">
            <b><c:out value="${u ne null ? u.fullName : 'Guest'}"/></b><br>
            <small class="muted"><c:out value="${u ne null ? (empty u.email ? '' : u.email) : ''}"/></small>
          </div>
          <div style="height:1px;background:var(--bd);margin:6px 0"></div>
          <a role="menuitem" href="${cp}/profile">Hồ sơ</a>
                    <a role="menuitem" href="${cp}/activity">Lịch sử hoạt động</a>

          <a role="menuitem" href="${cp}/account/change-password">Bảo mật</a>
          <div style="height:1px;background:var(--bd);margin:6px 0"></div>
          <c:choose>
            <c:when test="${empty u}"><a role="menuitem" href="${cp}/login">Đăng nhập</a></c:when>
            <c:otherwise><a role="menuitem" href="${cp}/logout">Đăng xuất</a></c:otherwise>
          </c:choose>
        </div>
      </div>
    </div>
  </div>
</header>

<script>
(function(){
  // đồng bộ --ph-h theo chiều cao thật của header
  const H = document.getElementById('portalHeader');
  let rafId = null;
  const setHH = () => {
    if(rafId) cancelAnimationFrame(rafId);
    rafId = requestAnimationFrame(()=>{
      if(!H) return;
      const h = Math.ceil(H.getBoundingClientRect().height);
      document.documentElement.style.setProperty('--ph-h', h + 'px');
    });
  };
  setHH();
  addEventListener('load', setHH, {passive:true});
  addEventListener('resize', setHH, {passive:true});
  if('ResizeObserver' in window){ new ResizeObserver(setHH).observe(H); }

  // Hamburger -> toggle sidebar (đã khai báo trong sidebar)
  document.getElementById('btnHamburger')?.addEventListener('click', ()=> {
    window.togglePortalSidebar?.();
  });

  // prefs: theme + density
  const KEY='portal.pref';
  const pf=Object.assign({theme:null,density:'normal'}, JSON.parse(localStorage.getItem(KEY)||'{}'));
  if(pf.theme==='light') document.documentElement.setAttribute('data-theme','light');
  if(pf.density==='compact') document.body.classList.add('compact');

  function toggleTheme(){
    const cur=document.documentElement.getAttribute('data-theme');
    if(cur==='light') document.documentElement.removeAttribute('data-theme');
    else document.documentElement.setAttribute('data-theme','light');
    pf.theme=(cur==='light'?null:'light'); localStorage.setItem(KEY, JSON.stringify(pf));
  }
  document.getElementById('btnTheme')?.addEventListener('click', toggleTheme);
  document.getElementById('btnDensity')?.addEventListener('click', ()=> {
    const on=document.body.classList.toggle('compact');
    pf.density=on?'compact':'normal';
    localStorage.setItem(KEY, JSON.stringify(pf));
  });

  // phím tắt
  document.addEventListener('keydown', e=>{
    if(e.key==='/' && !/input|textarea/i.test(e.target.tagName)){
      e.preventDefault(); const x=document.getElementById('qGlobal'); x?.focus(); x?.select();
    }
    if(e.altKey && e.key.toLowerCase()==='t'){ e.preventDefault(); toggleTheme(); }
    if(e.key==='Escape'){ document.querySelectorAll('.dd.open').forEach(dd=>dd.classList.remove('open')); }
  });

  // Dropdown a11y
  function bindDD(id){
    const dd=document.getElementById(id); if(!dd) return;
    const btn=dd.querySelector('button'); const menu=dd.querySelector('.dd-menu');
    function close(){ dd.classList.remove('open'); btn?.setAttribute('aria-expanded','false'); }
    function toggle(ev){
      ev.stopPropagation();
      const open = !dd.classList.contains('open');
      document.querySelectorAll('.dd.open').forEach(x=> x!==dd && x.classList.remove('open'));
      dd.classList.toggle('open', open);
      btn?.setAttribute('aria-expanded', open ? 'true' : 'false');
      if(open) menu?.focus?.();
    }
    btn?.setAttribute('aria-expanded','false');
    btn?.setAttribute('aria-haspopup','menu');
    btn?.addEventListener('click', toggle);
    document.addEventListener('click', close);
  }
  bindDD('ddUser'); bindDD('ddNoti');

  // Active nav theo URL
  const path=location.pathname.replace(/\/+$/,'').toLowerCase();
  document.querySelectorAll('.nav a').forEach(a=>{
    const href=a.getAttribute('href'); if(!href) return;
    const p=href.replace(location.origin,'').replace(/\/+$/,'').toLowerCase();
    if(path.startsWith(p)){ a.classList.add('active'); a.setAttribute('aria-current','page'); }
  });
})();
</script>
