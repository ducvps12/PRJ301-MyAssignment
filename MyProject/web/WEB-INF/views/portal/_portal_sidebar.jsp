<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp"%>
<c:set var="cp" value="${pageContext.request.contextPath}"/>
<c:set var="u" value="${sessionScope.currentUser}"/>
<c:set var="role" value="${empty u ? '' : (empty u.role ? (empty u.roleCode ? '' : u.roleCode) : u.role)}"/>
<c:set var="R" value="${fn:toUpperCase(fn:trim(role))}"/>
<c:set var="isHR" value="${R=='HR_ADMIN' or R=='DIV_LEADER' or R=='TEAM_LEAD' or R=='ADMIN' or R=='SYS_ADMIN'}"/>
<c:set var="isAdmin" value="${R=='ADMIN' or R=='SYS_ADMIN'}"/>
<c:set var="page" value="${empty page ? '' : page}"/>
<c:set var="reqCount" value="${empty requestScope.reqCount ? (empty sessionScope.reqCount ? 0 : sessionScope.reqCount) : requestScope.reqCount}"/>

<style>
  html,body{margin:0} /* tránh hở viền gây lệch */

  :root{
    --sbw:280px;     /* sidebar width full */
    --sbw-mini:72px; /* mini mode */
    --ph-h:64px;     /* header height (đồng bộ header) */
    --pri:#2563eb; --ink:#0f172a; --muted:#64748b; --bd:#e5e7eb;
    --card:#fff; --hover:#f4f6fb; --active:#eef2ff; --scroll:#c7d2fe;
  }
  @media(prefers-color-scheme:dark){
    :root{--ink:#e5e7eb;--muted:#94a3b8;--bd:#1f2937;--card:#0b1220;
          --hover:#111827;--active:#0f172a;--scroll:#334155;}
  }

  /* ===== SIDEBAR WRAPPER ===== */
  #portalSidebar.psb{
    position:fixed;
    left:0; bottom:0; right:auto;
    top:var(--ph-h); /* chừa đúng chiều cao header cố định */
    width:var(--sbw);
    z-index:90; /* thấp hơn header */
    background:var(--card);
    color:inherit;
    border-right:1px solid var(--bd);
    display:flex; flex-direction:column;
    transition:width .2s ease, transform .2s ease;
    transform:translateX(0);
    will-change:transform,width;
  }

  /* ===== Header trong sidebar ===== */
  .psb .brand{
    padding:14px;
    border-bottom:1px solid var(--bd);
    display:flex; align-items:center; justify-content:space-between; gap:10px;
  }
  .psb .logo{
    width:36px;height:36px;border-radius:10px;
    display:grid;place-items:center;color:#fff;font-weight:800;
    background:linear-gradient(135deg,#2563eb,#1d4ed8);
  }
  .psb .btn{
    height:30px;padding:0 10px;border-radius:10px;
    border:1px solid var(--bd); background:transparent; cursor:pointer;
    font-weight:600; color:inherit;
  }

  /* ===== Nội dung cuộn ===== */
  .psb .body{ flex:1; overflow:auto; padding:10px 8px 16px; }
  .psb .body::-webkit-scrollbar{width:10px}
  .psb .body::-webkit-scrollbar-thumb{background:var(--scroll);border-radius:999px}

  /* ===== Group & link ===== */
  .psb .sec{ margin:12px 6px 8px; display:flex; align-items:center; justify-content:space-between; }
  .psb .ttl{
    font-size:11px;letter-spacing:.08em;color:var(--muted);
    text-transform:uppercase;display:flex;gap:6px;align-items:center;
  }
  .psb .toggle{ border:none;background:transparent;color:var(--muted);cursor:pointer;font-size:14px; transition:transform .2s; }
  .psb .group[aria-expanded="false"] .list{ display:none }
  .psb .group[aria-expanded="false"] .toggle{ transform:rotate(-90deg) }

  .psb .list{margin:0;padding:0;list-style:none}
  .psb a.link{
    --padL:18px;
    position:relative; display:flex; gap:10px; align-items:center;
    border-radius:10px;
    padding:10px 10px 10px calc(var(--padL) + 14px);
    color:inherit; text-decoration:none; border:1px solid transparent; transition:.15s;
  }
  .psb a.link .ic{
    width:18px;height:18px; position:absolute; left:10px; top:50%; transform:translateY(-50%);
    color:var(--muted);
  }
  .psb a.link .lbl{white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
  .psb a.link .badge{
    margin-left:auto;font-size:12px;padding:2px 8px;border-radius:999px;
    border:1px solid var(--bd);color:var(--muted)
  }
  .psb a.link:hover{background:var(--hover);border-color:var(--bd)}
  .psb a.link.active{
    background:var(--active);
    box-shadow:inset 0 0 0 1px color-mix(in oklab,var(--pri) 35%, transparent);
  }

  /* ===== Mini mode ===== */
  .psb.mini{width:var(--sbw-mini)}
  .psb.mini .hide-mini{display:none!important}
  .psb.mini a.link{--padL:0px;padding-left:40px}
  .psb.mini a.link .badge{display:none}

  /* ===== Footer ===== */
  .psb .foot{
    padding:10px 12px; border-top:1px solid var(--bd);
    display:flex; gap:10px; align-items:center;
  }
  .psb .foot .avt{
    width:36px;height:36px;border-radius:10px; display:grid;place-items:center;
    background:linear-gradient(135deg,#1d4ed8,#2563eb); color:#fff;font-weight:800;
  }
  .psb .foot .me{line-height:1.1}
  .psb .foot .me small{color:var(--muted)}

  /* ===== Overlay (mobile) ===== */
  .psb-overlay{
    position:fixed;inset:0; background:rgba(2,6,23,.45);
    z-index:80; display:none;
  }

  /* ===== Responsive ===== */
  @media(max-width:1100px){
    #portalSidebar.psb{
      transform:translateX(-100%);
      left:auto; right:0; /* trượt từ trái sang phải -> tránh cấn layout */
      box-shadow:none;
      width:min(86vw, 320px); /* dùng chiều rộng hợp lý trên mobile */
    }
    #portalSidebar.psb.open{
      transform:translateX(0);
      box-shadow:0 10px 30px rgba(0,0,0,.25);
    }
  }

  .svg{width:18px;height:18px;display:block}
</style>

<aside class="psb" id="portalSidebar" role="navigation" aria-label="Portal navigation">
  <div class="brand">
    <div style="display:flex;gap:10px;align-items:center">
      <div class="logo" aria-hidden="true">LM</div>
      <div class="hide-mini">
        <b>NeMark Portal</b><br/>
        <small style="color:var(--muted)">Role: <c:out value="${R!='' ? R : 'GUEST'}"/></small>
      </div>
    </div>
    <div>
      <button class="btn" id="btnMiniPSB" title="Alt+B">Mini</button>
    </div>
  </div>

  <div class="body" id="sbScroll">
    <!-- WORK -->
    <section class="group" id="grpWork" aria-expanded="true">
      <div class="sec">
        <div class="ttl">Work</div>
        <button class="toggle" data-toggle="#grpWork" aria-label="Thu gọn Work">▾</button>
      </div>
      <ul class="list">
        <li>
          <a class="link ${page=='portal.home'?'active':''}" href="${cp}/portal">
            <span class="ic"><svg class="svg" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M3 10.5 12 3l9 7.5"/><path d="M5 10v10h14V10"/></svg></span>
            <span class="lbl">Tổng quan</span>
          </a>
        </li>
        <li>
          <a class="link ${page=='portal.req'?'active':''}" href="${cp}/request/list">
            <span class="ic"><svg class="svg" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M4 6h16M4 12h16M4 18h10"/></svg></span>
            <span class="lbl">Requests</span>
            <c:if test="${reqCount > 0}">
              <span class="badge"><c:out value="${reqCount}"/></span>
            </c:if>
          </a>
        </li>
        <li>
          <a class="link ${page=='portal.att'?'active':''}" href="${cp}/attendance">
            <span class="ic"><svg class="svg" viewBox="0 0 24 24" fill="none" stroke="currentColor"><circle cx="12" cy="12" r="9"/><path d="M12 7v6l4 2"/></svg></span>
            <span class="lbl">Chấm công</span>
          </a>
        </li>
        <li>
          <a class="link ${page=='portal.work'?'active':''}" href="${cp}/work">
            <span class="ic"><svg class="svg" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M3 12l4 4 7-8 7 10"/><path d="M21 21H3"/></svg></span>
            <span class="lbl">Báo cáo</span>
          </a>
        </li>
        <li>
          <a class="link ${page=='portal.todos'?'active':''}" href="${cp}/work/todos">
            <span class="ic"><svg class="svg" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M9 11l3 3L22 4"/><path d="M3 7h5M3 12h5M3 17h5"/></svg></span>
            <span class="lbl">Việc HR</span>
          </a>
        </li>
      </ul>
    </section>

    <c:if test="${isHR}">
      <section class="group" id="grpHR" aria-expanded="true">
        <div class="sec">
          <div class="ttl">HR</div>
          <button class="toggle" data-toggle="#grpHR" aria-label="Thu gọn HR">▾</button>
        </div>
        <ul class="list">
          <li>
            <a class="link ${page=='portal.payroll'?'active':''}" href="${cp}/payroll">
              <span class="ic"><svg class="svg" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M3 7h18v10H3z"/><circle cx="12" cy="12" r="2"/></svg></span>
              <span class="lbl">Lương thưởng</span>
            </a>
          </li>
          <li>
            <a class="link ${page=='portal.recruit'?'active':''}" href="${cp}/recruit/job">
              <span class="ic"><svg class="svg" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M4 7h16v10H4z"/><path d="M8 7V5h8v2"/></svg></span>
              <span class="lbl">Tuyển dụng</span>
            </a>
          </li>
        </ul>
      </section>
    </c:if>

    <c:if test="${isAdmin}">
      <section class="group" id="grpAdmin" aria-expanded="true">
        <div class="sec">
          <div class="ttl">Admin</div>
          <button class="toggle" data-toggle="#grpAdmin" aria-label="Thu gọn Admin">▾</button>
        </div>
        <ul class="list">
          <li>
            <a class="link ${page=='portal.admin'?'active':''}" href="${cp}/admin">
              <span class="ic"><svg class="svg" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M4 21V9l8-6 8 6v12"/></svg></span>
              <span class="lbl">Dashboard</span>
            </a>
          </li>
          <li>
            <a class="link ${page=='portal.users'?'active':''}" href="${cp}/admin/users">
              <span class="ic"><svg class="svg" viewBox="0 0 24 24" fill="none" stroke="currentColor"><circle cx="9" cy="7" r="4"/><path d="M17 11a4 4 0 1 0 0-8"/><path d="M3 21a6 6 0 0 1 12 0"/><path d="M15 21a6 6 0 0 1 6-6"/></svg></span>
              <span class="lbl">Người dùng</span>
            </a>
          </li>
          <li>
            <a class="link ${page=='portal.audit'?'active':''}" href="${cp}/admin/audit">
              <span class="ic"><svg class="svg" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M3 4h18v4H3z"/><path d="M7 8v12M12 8v12M17 8v12"/></svg></span>
              <span class="lbl">Nhật ký</span>
            </a>
          </li>
        </ul>
      </section>
    </c:if>
  </div>

  <div class="foot">
    <div class="avt"><c:out value="${empty u ? 'G' : fn:substring(u.fullName,0,1)}"/></div>
    <div class="me hide-mini">
      <b><c:out value="${empty u ? 'Guest' : u.fullName}"/></b>
      <small>ID: <c:out value="${empty u ? 'N/A' : u.id}"/></small>
    </div>
    <div style="margin-left:auto;display:flex;gap:6px">
      <button class="btn" id="btnPin" title="Ghim sidebar">Pin</button>
      <a class="btn hide-mini" href="${cp}/logout">Logout</a>
    </div>
  </div>
</aside>
<div class="psb-overlay" id="psbOverlay" onclick="window.togglePortalSidebar()"></div>

<script>
(function(){
  const sb=document.getElementById('portalSidebar');
  const root=document.documentElement;
  const overlay=document.getElementById('psbOverlay');
  const K_MINI='portal.sb.mini',K_COLL='portal.sb.collapse',K_THEME='portal.theme',K_PIN='portal.sb.pin';
  const SB_FULL=280,SB_MINI=72;

  function setSBW(){
    root.style.setProperty('--sbw',(sb.classList.contains('mini')?SB_MINI:SB_FULL)+'px');
  }
  if(localStorage.getItem(K_MINI)==='1') sb.classList.add('mini');
  setSBW();

  // restore collapse (lưu/đọc true|false chuẩn)
  let coll={};try{coll=JSON.parse(localStorage.getItem(K_COLL)||'{}')}catch(e){}
  Object.entries(coll).forEach(([id,ex])=>{
    const el=document.getElementById(id);
    if(el) el.setAttribute('aria-expanded', String(ex)==='true');
  });

  const btnMini=document.getElementById('btnMiniPSB');
  const btnTheme=document.getElementById('btnThemeSB');
  const btnPin=document.getElementById('btnPin');

  const updateMiniBtn=()=>btnMini && (btnMini.textContent=sb.classList.contains('mini')?'Full':'Mini');
  updateMiniBtn();

  function toggleMini(){
    sb.classList.toggle('mini');
    localStorage.setItem(K_MINI, sb.classList.contains('mini')?'1':'0');
    setSBW(); updateMiniBtn();
  }
  btnMini?.addEventListener('click', toggleMini);
  // Shortcut Alt+B
  addEventListener('keydown', e=>{ if(e.altKey && e.key.toLowerCase()==='b'){ e.preventDefault(); toggleMini(); }});

  // collapse groups
  document.querySelectorAll('[data-toggle]').forEach(tg=>{
    tg.addEventListener('click',()=>{
      const id=tg.getAttribute('data-toggle').replace('#','');
      const el=document.getElementById(id); if(!el) return;
      const now=(el.getAttribute('aria-expanded')!=='true');
      el.setAttribute('aria-expanded', now);
      coll[id]=now; localStorage.setItem(K_COLL, JSON.stringify(coll));
    });
  });

  // theme cycle: auto -> dark -> light
  function applyTheme(){
    const t=localStorage.getItem(K_THEME)||'auto';
    root.dataset.theme=t;
    btnTheme && (btnTheme.textContent=t==='auto'?'Theme: Auto':(t==='dark'?'Theme: Dark':'Theme: Light'));
  }
  btnTheme && btnTheme.addEventListener('click',()=>{
    const cur=localStorage.getItem(K_THEME)||'auto';
    const nxt=cur==='auto'?'dark':(cur==='dark'?'light':'auto');
    localStorage.setItem(K_THEME,nxt); applyTheme();
  });
  applyTheme();

  // pin: khi pin thì auto bỏ mini
  if(localStorage.getItem(K_PIN)==='1'){ sb.classList.remove('mini'); setSBW(); updateMiniBtn(); }
  btnPin?.addEventListener('click', ()=>{
    const cur=localStorage.getItem(K_PIN)==='1';
    localStorage.setItem(K_PIN', cur?'0':'1');
    if(!cur) sb.classList.remove('mini');
    setSBW(); updateMiniBtn();
  });

  // mobile toggle api
  window.togglePortalSidebar=()=>{
    const open=!sb.classList.contains('open');
    sb.classList.toggle('open', open);
    overlay.style.display=open?'block':'none';
  };

  // Esc đóng trên mobile
  addEventListener('keydown', e=>{
    if(e.key==='Escape' && sb.classList.contains('open')){
      e.preventDefault(); window.togglePortalSidebar();
    }
  });

  // breakpoint change -> đóng overlay nếu đổi trạng thái
  let ww=window.innerWidth;
  window.addEventListener('resize',()=>{
    const now=window.innerWidth;
    if((ww<=1100&&now>1100)||(ww>1100&&now<=1100)){
      sb.classList.remove('open'); overlay.style.display='none';
    }
    ww=now;
  });
})();
</script>
