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
  :root{
    --sbw:280px; --rad:14px;
    --bg:#ffffff; --fg:#0f172a; --muted:#64748b; --bd:#e5e7eb;
    --hover:#f4f6fb; --active:#eef2ff; --acc:#5c8dff; --acc-2:#22d3ee;
    --chip:#f7f8ff; --shadow:0 8px 28px rgba(15,23,42,.08); --scroll:#c7d2fe;
  }
  @media (prefers-color-scheme: dark){
    :root{
      --bg:#0b1324; --fg:#e8ebf2; --muted:#9aa3b2; --bd:#1c2744;
      --hover:#121b32; --active:#1a2442; --chip:#0e1630; --scroll:#2a365a;
      --shadow:0 10px 30px rgba(0,0,0,.35);
    }
  }

  .psb{
    position:fixed; inset:0 auto 0 0; width:var(--sbw); z-index:50;
    background:var(--bg); color:var(--fg); border-right:1px solid var(--bd);
    display:flex; flex-direction:column; transition:width .25s ease, transform .25s ease;
    transform:translateX(0);
  }
  .psb .brand{
    padding:14px; border-bottom:1px solid var(--bd);
    display:flex; align-items:center; justify-content:space-between; gap:10px;
  }
  .psb .brand .meta{display:flex; gap:10px; align-items:center}
  .psb .brand .logo{
    width:36px;height:36px;border-radius:50%;display:grid;place-items:center;
    color:#fff;font-weight:800; user-select:none;
    background:linear-gradient(135deg,var(--acc),var(--acc-2));
    box-shadow:var(--shadow);
  }
  .psb .brand .titles b{display:block; line-height:1.1}
  .psb .brand .titles small{color:var(--muted)}
  .psb .btn{
    height:30px; padding:0 10px; border-radius:10px; border:1px solid var(--bd);
    background:var(--chip); color:var(--fg); cursor:pointer; font-weight:600;
  }
  .psb .btn:hover{background:var(--hover)}

  .psb .body{ flex:1; overflow:auto; padding:10px 8px 16px }
  .psb .sec{ margin:12px 6px 8px; display:flex; align-items:center; justify-content:space-between; }
  .psb .sec .ttl{
    font-size:11px; letter-spacing:.08em; color:var(--muted); text-transform:uppercase;
    display:flex; gap:6px; align-items:center;
  }
  .psb .sec .toggle{ border:none;background:transparent;color:var(--muted);cursor:pointer }
  .psb .sec .toggle:hover{ color:var(--fg) }

  .psb .list{ margin:0; padding:0; list-style:none }
  .psb a.link{
    --padL:18px;
    display:flex; gap:10px; align-items:center; border-radius:10px;
    padding:10px 10px 10px calc(var(--padL) + 14px);
    color:inherit; text-decoration:none; position:relative; border:1px solid transparent;
  }
  .psb a.link .ic{ width:18px; text-align:center; position:absolute; left:10px; top:50%; transform:translateY(-50%) }
  .psb a.link .lbl{ white-space:nowrap; overflow:hidden; text-overflow:ellipsis }
  .psb a.link .badge{ margin-left:auto; font-size:12px; padding:2px 8px; border-radius:999px; background:var(--active); color:var(--fg) }
  .psb a.link:hover{ background:var(--hover); border-color:var(--bd) }
  .psb a.link.active{ background:var(--active); box-shadow:inset 0 0 0 1px rgba(92,141,255,.35) }

  .psb .group[aria-expanded="false"] .list{ display:none }

  .psb.mini{ width:72px }
  .psb.mini .hide-mini{ display:none !important }
  .psb.mini a.link{ --padL:0px; padding-left:40px }
  .psb.mini a.link .badge{ display:none }
  .psb.mini .link.tooltip:hover::after{
    content:attr(data-title); position:fixed; left:76px;
    background:#111827; color:#fff; padding:6px 10px; border-radius:8px; font-size:12px;
    box-shadow:var(--shadow); white-space:nowrap; z-index:100;
  }

  .psb .foot{
    padding:10px 12px; border-top:1px solid var(--bd); display:flex; gap:10px; align-items:center;
  }
  .psb .foot .avt{
    width:36px;height:36px;border-radius:10px;display:grid;place-items:center;
    background:linear-gradient(135deg,var(--acc-2),var(--acc)); color:#fff;font-weight:800;
  }
  .psb .foot .me{ line-height:1.1 }
  .psb .foot .me small{ color:var(--muted) }
  .psb .foot .more{ margin-left:auto; display:flex; gap:6px }

  .psb-overlay{ position:fixed; inset:0; background:rgba(2,6,23,.45); z-index:40; display:none }

  .psb .body::-webkit-scrollbar{ width:10px }
  .psb .body::-webkit-scrollbar-thumb{ background:var(--scroll); border-radius:999px }

  @media (max-width:1100px){
    .psb{ transform:translateX(-100%) }
    .psb.open{ transform:translateX(0); box-shadow:var(--shadow) }
  }
</style>

<aside class="psb" id="portalSidebar" role="navigation" aria-label="Portal navigation">
  <div class="brand">
    <div class="meta">
      <div class="logo" aria-hidden="true">HR</div>
      <div class="titles hide-mini">
        <b>NeMark Portal</b>
        <small>Role: <c:out value="${R!='' ? R : 'GUEST'}"/></small>
      </div>
    </div>
    <div class="quick">
      <button class="btn" id="btnMiniPSB">Mini</button>
      <button class="btn hide-mini" id="btnTheme">Theme</button>
    </div>
  </div>

  <!-- (ĐÃ BỎ Ô TÌM KIẾM) -->

  <div class="body" id="sbScroll">
    <section class="group" id="grpWork" aria-expanded="true">
      <div class="sec">
        <div class="ttl"><span>Work</span></div>
        <button class="toggle" data-toggle="#grpWork" aria-label="Thu gọn Work">▾</button>
      </div>
      <ul class="list">
        <li><a class="link tooltip ${page=='portal.home'?'active':''}" href="${cp}/portal" data-title="Tổng quan">
          <span class="ic">🏠</span><span class="lbl">Tổng quan</span>
        </a></li>
        <li><a class="link tooltip ${page=='portal.req'?'active':''}" href="${cp}/request/list" data-title="Requests">
          <span class="ic">🗂️</span><span class="lbl">Requests</span>
          <span class="badge" id="bdgReq">3</span>
        </a></li>
        <li><a class="link tooltip ${page=='portal.att'?'active':''}" href="${cp}/attendance" data-title="Chấm công">
          <span class="ic">🕒</span><span class="lbl">Chấm công</span>
        </a></li>
        <li><a class="link tooltip ${page=='portal.work'?'active':''}" href="${cp}/work" data-title="Báo cáo">
          <span class="ic">📈</span><span class="lbl">Báo cáo</span>
        </a></li>
        <li><a class="link tooltip ${page=='portal.todos'?'active':''}" href="${cp}/work/todos" data-title="Việc HR">
          <span class="ic">✅</span><span class="lbl">Việc HR</span>
          <span class="badge" data-hot="1">5</span>
        </a></li>
      </ul>
    </section>

    <c:if test="${isHR}">
      <section class="group" id="grpHR" aria-expanded="true">
        <div class="sec">
          <div class="ttl"><span>HR</span></div>
          <button class="toggle" data-toggle="#grpHR" aria-label="Thu gọn HR">▾</button>
        </div>
        <ul class="list">
          <li><a class="link tooltip ${page=='portal.payroll'?'active':''}" href="${cp}/payroll" data-title="Lương thưởng">
            <span class="ic">💰</span><span class="lbl">Lương thưởng</span>
          </a></li>
          <li><a class="link tooltip ${page=='portal.recruit'?'active':''}" href="${cp}/recruit/job" data-title="Tuyển dụng">
            <span class="ic">🧑‍💼</span><span class="lbl">Tuyển dụng</span>
            <span class="badge" data-hot="2">2</span>
          </a></li>
        </ul>
      </section>
    </c:if>

    <c:if test="${isAdmin}">
      <section class="group" id="grpAdmin" aria-expanded="true">
        <div class="sec">
          <div class="ttl"><span>Admin</span></div>
          <button class="toggle" data-toggle="#grpAdmin" aria-label="Thu gọn Admin">▾</button>
        </div>
        <ul class="list">
          <li><a class="link tooltip ${page=='portal.admin'?'active':''}" href="${cp}/admin" data-title="Admin Dashboard">
            <span class="ic">📊</span><span class="lbl">Dashboard</span>
          </a></li>
          <li><a class="link tooltip ${page=='portal.users'?'active':''}" href="${cp}/admin/users" data-title="Người dùng">
            <span class="ic">👥</span><span class="lbl">Người dùng</span>
          </a></li>
          <li><a class="link tooltip ${page=='portal.audit'?'active':''}" href="${cp}/admin/audit" data-title="Nhật ký hệ thống">
            <span class="ic">🧾</span><span class="lbl">Nhật ký</span>
          </a></li>
        </ul>
      </section>
    </c:if>
  </div>

  <div class="foot">
    <div class="avt">${empty u ? "?" : fn:substring(u.fullName,0,1)}</div>
    <div class="me hide-mini">
      <b><c:out value="${empty u ? 'Guest' : u.fullName}"/></b>
      <small>ID: <c:out value="${empty u ? 'N/A' : u.id}"/></small>
    </div>
    <div class="more">
      <button class="btn" id="btnPin">Pin</button>
      <a class="btn hide-mini" href="${cp}/logout">Logout</a>
    </div>
  </div>
</aside>
<div class="psb-overlay" id="psbOverlay" onclick="window.togglePortalSidebar()"></div>

<script>
(function(){
  const sb   = document.getElementById('portalSidebar');
  const root = document.documentElement;
  const overlay = document.getElementById('psbOverlay');
  const K_MINI='portal.sb.mini', K_COLL='portal.sb.collapse', K_THEME='portal.theme', K_PIN='portal.sb.pin';
  const SB_FULL=280, SB_MINI=72;

  const qs=(s,el=document)=>el.querySelector(s);
  const qsa=(s,el=document)=>Array.from(el.querySelectorAll(s));
  const setSBW=()=>root.style.setProperty('--sbw', (sb.classList.contains('mini')?SB_MINI:SB_FULL)+'px');

  if(localStorage.getItem(K_MINI)==='1') sb.classList.add('mini');
  setSBW();

  // restore collapse
  let coll={}; try{coll=JSON.parse(localStorage.getItem(K_COLL)||'{}')}catch(e){}
  Object.entries(coll).forEach(([id,ex])=>{ const el=document.getElementById(id); if(el) el.setAttribute('aria-expanded', !!ex); });

  // buttons
  const btnMini=document.getElementById('btnMiniPSB');
  const btnTheme=document.getElementById('btnTheme');
  const btnPin=document.getElementById('btnPin');

  const updateMini=()=>btnMini.textContent=sb.classList.contains('mini')?'Full':'Mini';
  updateMini();
  btnMini.addEventListener('click',()=>{ sb.classList.toggle('mini'); localStorage.setItem(K_MINI,sb.classList.contains('mini')?'1':'0'); setSBW(); updateMini(); });

  // collapse groups
  qsa('[data-toggle]').forEach(tg=>{
    tg.addEventListener('click', ()=>{
      const id = tg.getAttribute('data-toggle').replace('#','');
      const el = document.getElementById(id); if(!el) return;
      const now = el.getAttribute('aria-expanded') !== 'true';
      el.setAttribute('aria-expanded', now); coll[id]=now; localStorage.setItem(K_COLL, JSON.stringify(coll));
    });
  });

  // scroll active into view
  const active = qs('.psb a.link.active'); if(active) active.scrollIntoView({block:'center', behavior:'instant'});

  // shortcuts (đã bỏ phím / vì không còn search)
  document.addEventListener('keydown',(e)=>{
    if (e.key==='['){ e.preventDefault(); btnMini.click(); }
    else if (e.key===']'){ e.preventDefault(); window.togglePortalSidebar(); }
    else if (e.key==='g' || e.key==='G'){ window.__seq='g'; setTimeout(()=>window.__seq='',800); }
    else if ((e.key==='d'||e.key==='D') && window.__seq==='g'){ const el=document.querySelector('a[href$="/admin"]'); if(el) location.href=el.href; window.__seq=''; }
    else if ((e.key==='r'||e.key==='R') && window.__seq==='g'){ const el=document.querySelector('a[href$="/request/list"]'); if(el) location.href=el.href; window.__seq=''; }
  });

  // theme
  function applyTheme(){
    const t=localStorage.getItem(K_THEME)||'auto';
    document.documentElement.dataset.theme=t;
    btnTheme && (btnTheme.textContent=t==='auto'?'Theme: Auto':(t==='dark'?'Theme: Dark':'Theme: Light'));
  }
  btnTheme && btnTheme.addEventListener('click',()=>{
    const cur=localStorage.getItem(K_THEME)||'auto';
    const nxt=cur==='auto'?'dark':(cur==='dark'?'light':'auto');
    localStorage.setItem(K_THEME,nxt); applyTheme();
  });
  applyTheme();

  // pin
  if(localStorage.getItem(K_PIN)==='1'){ sb.classList.remove('mini'); }
  btnPin.addEventListener('click', ()=>{
    const cur=localStorage.getItem(K_PIN)==='1';
    localStorage.setItem(K_PIN,cur?'0':'1');
    if(!cur) sb.classList.remove('mini');
    setSBW(); updateMini();
  });

  // mobile toggle api
  window.togglePortalSidebar=()=>{ sb.classList.toggle('open'); overlay.style.display=sb.classList.contains('open')?'block':'none'; };

  // breakpoint change -> close overlay
  let ww=window.innerWidth;
  window.addEventListener('resize',()=>{ const now=window.innerWidth; if((ww<=1100&&now>1100)||(ww>1100&&now<=1100)) sb.classList.remove('open'); ww=now; });
})();
</script>
