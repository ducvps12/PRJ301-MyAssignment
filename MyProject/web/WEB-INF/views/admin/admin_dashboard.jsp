<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"  %>

<%-- KHÔNG include header ở đây để tránh lồng --%>
<%-- <%@ include file="/WEB-INF/views/common/_admin_header.jsp" %> --%>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8" />
  <title>Admin Dashboard · LeaveMgmt</title>
  <meta name="viewport" content="width=device-width,initial-scale=1" />
  <style>
    :root{
      --bg:#f7f9fc; --card:#fff; --tx:#0f172a; --muted:#64748b; --bd:#e5e7eb;
      --pri:#2563eb; --ok:#16a34a; --warn:#f59e0b; --err:#dc2626; --vio:#7c3aed;
    }
    @media (prefers-color-scheme: dark){
      :root{ --bg:#0b1220; --card:#0f172a; --tx:#e5e7eb; --muted:#94a3b8; --bd:#1f2937; }
    }
    *{box-sizing:border-box}
    body{margin:0;background:var(--bg);color:var(--tx);font-family:system-ui,Segoe UI,Roboto,Helvetica,Arial,sans-serif}
    a{color:inherit;text-decoration:none}

    /* Khung 2 cột */
    .main{display:grid;grid-template-columns:260px 1fr;min-height:100vh}
    /* đảm bảo sidebar chiếm cột trái cố định */
    .main > .sb{position:sticky; top:0; height:100vh}

    .content{padding:18px;max-width:1400px;margin:0 auto}
    .topbar{position:sticky;top:0;z-index:30;display:flex;align-items:center;justify-content:space-between;gap:12px;padding:10px 14px;border:1px solid var(--bd);border-radius:14px;background:linear-gradient(180deg,rgba(255,255,255,.85),rgba(255,255,255,.7));backdrop-filter:blur(6px);margin:10px 0}
    .tools{display:flex;align-items:center;gap:8px;flex-wrap:wrap}
    .btn{display:inline-flex;align-items:center;gap:8px;border:1px solid var(--bd);background:var(--card);padding:8px 12px;border-radius:10px;cursor:pointer}
    .btn.icon{width:38px;height:38px;justify-content:center;padding:0}
    .btn.primary{border-color:transparent;background:linear-gradient(135deg,var(--pri),#8ab4ff);color:#fff}
    .pill{display:inline-flex;align-items:center;gap:6px;padding:4px 10px;border:1px solid var(--bd);border-radius:999px;font-size:12px;color:var(--muted)}
    .muted{color:var(--muted)}
    .grid{display:grid;grid-template-columns:2fr 1fr;gap:14px}
    @media(max-width:1100px){.grid{grid-template-columns:1fr}}
    .card{background:var(--card);border:1px solid var(--bd);border-radius:16px;padding:14px;box-shadow:0 10px 30px rgba(0,0,0,.05)}

    /* KPI */
    .kpis{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:14px;margin-bottom:14px}
    @media(max-width:1100px){.kpis{grid-template-columns:repeat(2,1fr)}}
    @media(max-width:720px){.kpis{grid-template-columns:1fr}}
    .card.kpi{position:relative;overflow:hidden}
    .card.kpi:after{content:"";position:absolute;inset:auto -30% -30% auto;width:140px;height:140px;background:radial-gradient(circle at 70% 30%,rgba(99,102,241,.18),transparent 60%);transform:rotate(25deg)}
    .sub{color:var(--muted);font-size:12px}
    .num.big{font-size:28px;font-weight:800;letter-spacing:.2px}
    .ring{width:56px;height:56px;position:relative}
    .ring svg{transform:rotate(-90deg)}
    .ring .val{position:absolute;inset:0;display:grid;place-items:center;font-size:12px;font-weight:700}

    /* Table */
    .table{width:100%;border-collapse:separate;border-spacing:0}
    .table th,.table td{padding:10px 12px;border-bottom:1px solid var(--bd);text-align:left}
    .table thead th{font-size:12px;letter-spacing:.2px;text-transform:uppercase;color:var(--muted)}
    .table tbody tr:hover{background:rgba(148,163,184,.08)}
    .table.compact th,.table.compact td{padding:6px 10px}
    .sticky thead tr th{position:sticky;top:64px;z-index:5;background:var(--card)}

    .status{padding:4px 8px;border-radius:999px;border:1px solid transparent;font-size:12px;display:inline-flex;align-items:center;gap:6px}
    .status.APPROVED{background:rgba(34,197,94,.12);border-color:rgba(34,197,94,.3);color:#16a34a}
    .status.PENDING{background:rgba(245,158,11,.12);border-color:rgba(245,158,11,.3);color:#d97706}
    .status.REJECTED{background:rgba(239,68,68,.12);border-color:rgba(239,68,68,.3);color:#ef4444}

    .tablebar{display:flex;align-items:center;justify-content:space-between;gap:12px;margin-bottom:10px;flex-wrap:wrap}
    .input{border:1px solid var(--bd);border-radius:10px;padding:8px 10px;background:var(--card)}
    .switch{display:inline-flex;align-items:center;gap:6px;cursor:pointer;user-select:none;font-size:13px}
    .switch input{appearance:none;width:34px;height:20px;border-radius:999px;background:#e5e7eb;position:relative;outline:none;transition:.2s}
    .switch input:checked{background:#c7d2fe}
    .switch input:before{content:"";position:absolute;width:16px;height:16px;border-radius:50%;background:#fff;top:2px;left:2px;transition:.2s}
    .switch input:checked:before{transform:translateX(14px)}

    /* Charts mini-engine (Canvas) */
    .charts{display:grid;grid-template-columns:1.6fr 1fr;gap:14px;margin:14px 0}
    .charts .card h3{margin:0 0 10px}
    @media(max-width:1200px){.charts{grid-template-columns:1fr}}
    .chart-toolbar{display:flex;align-items:center;gap:8px;margin:-2px 0 8px;flex-wrap:wrap}
    .chartbox{position:relative}
    .chartbox .actions{position:absolute;right:8px;top:8px;display:flex;gap:6px}
    .chartbox canvas{width:100%;height:280px}
    .badge{font-size:11px;border:1px dashed var(--bd);padding:2px 8px;border-radius:999px;color:var(--muted)}

    .toast{position:fixed;right:16px;bottom:16px;max-width:420px;z-index:50;display:none}
    .toast.show{display:block;animation:toastIn .2s ease-out}
    .toast .tcard{background:var(--card);border:1px solid var(--bd);border-radius:12px;padding:10px 12px;box-shadow:0 8px 30px rgba(0,0,0,.08)}
    @keyframes toastIn{from{opacity:0;transform:translateY(8px)}to{opacity:1;transform:none}}
    .modal{position:fixed;inset:0;display:none;align-items:center;justify-content:center;background:rgba(0,0,0,.35);z-index:60}
    .modal.open{display:flex}
    .modal .box{width:min(560px, calc(100% - 24px));background:var(--card);border:1px solid var(--bd);border-radius:16px;padding:16px}
  </style>
</head>
<body>

<div class="main">
  <%-- ✅ Sidebar: đảm bảo _admin_sidebar.jsp có <aside id="sidebar" class="sb"> --%>
  <%@ include file="/WEB-INF/views/common/_admin_sidebar.jsp" %>

  <div class="content">
    <!-- TOPBAR -->
    <div class="topbar">
      <div class="tools">
        <button class="btn icon" onclick="toggleSidebar()" title="Toggle sidebar">☰</button>
        <button class="btn icon" onclick="location.reload()" title="Refresh">⟳</button>
        <button class="btn icon" onclick="window.print()" title="Print">🖨</button>
        <button class="btn icon" id="copyLinkBtn" title="Copy link">🔗</button>
      </div>
      <div class="muted">View: <span class="pill">${viewDepartment}</span></div>
      <div class="tools">
        <button class="btn" id="themeBtn" title="Toggle dark mode">🌓 Theme</button>
        <a class="btn" href="${pageContext.request.contextPath}/logout">Đăng xuất</a>
      </div>
    </div>

    <h2 style="margin:0 0 8px">Admin Dashboard</h2>
    <div class="muted" style="margin-bottom:14px">Tổng quan tình hình nghỉ phép</div>

    <!-- KPIs -->
    <div class="kpis">
      <div class="card kpi"><div class="sub">Đang chờ duyệt</div><div class="num big">${kpis.pendingAll}</div></div>
      <div class="card kpi"><div class="sub">Đã duyệt trong tháng</div><div class="num big">${kpis.approvedThisMonth}</div></div>
      <div class="card kpi"><div class="sub">Tổng đơn trong tháng</div><div class="num big">${kpis.totalThisMonth}</div></div>
      <div class="card kpi" style="display:flex;align-items:center;gap:12px">
        <div class="ring" title="Approval rate 30 ngày">
          <svg viewBox="0 0 36 36" width="56" height="56" data-rate="${kpis.approvalRate30d}">
            <circle cx="18" cy="18" r="16" fill="none" stroke="rgba(148,163,184,.35)" stroke-width="4"></circle>
            <circle id="ringProgress" cx="18" cy="18" r="16" fill="none" stroke="url(#grad)" stroke-width="4"
                    stroke-linecap="round" stroke-dasharray="0 100"></circle>
            <defs><linearGradient id="grad" x1="0" y1="0" x2="1" y2="1"><stop offset="0%" stop-color="#60a5fa"/><stop offset="100%" stop-color="#16a34a"/></linearGradient></defs>
          </svg>
          <div class="val"><fmt:formatNumber value="${kpis.approvalRate30d}" maxFractionDigits="0"/>%</div>
        </div>
        <div><div class="sub">Tỉ lệ duyệt 30 ngày</div><div class="muted" style="font-size:12px">Base: ${kpis.approvalBase30d}</div></div>
      </div>
    </div>

    <!-- Analytics -->
    <div class="card">
      <div class="chart-toolbar">
        <h3 style="margin:0">Analytics</h3>
        <span class="badge">Realtime · No libs</span>
        <div style="margin-left:auto;display:flex;gap:8px;align-items:center">
          <label class="muted" style="font-size:12px">Từ</label><input class="input" type="date" id="fromDate">
          <label class="muted" style="font-size:12px">Đến</label><input class="input" type="date" id="toDate">
          <button class="btn" id="applyRange">Áp dụng</button>
          <button class="btn" id="resetRange">Reset</button>
        </div>
      </div>
      <div class="charts">
        <div class="card chartbox">
          <div class="actions">
            <button class="btn icon" data-export="#lineChart"  title="Xuất PNG">⬇</button>
            <button class="btn icon" data-full="#lineChart"    title="Fullscreen">⤢</button>
          </div>
          <h3>Đơn theo ngày</h3>
          <canvas id="lineChart"  width="1200" height="320"></canvas>
        </div>

        <div class="card chartbox">
          <div class="actions">
            <button class="btn icon" data-export="#donutChart" title="Xuất PNG">⬇</button>
            <button class="btn icon" data-full="#donutChart"   title="Fullscreen">⤢</button>
          </div>
          <h3>Tỷ trọng trạng thái</h3>
          <canvas id="donutChart" width="600"  height="320"></canvas>
        </div>

        <div class="card chartbox">
          <div class="actions">
            <button class="btn icon" data-export="#barChart"   title="Xuất PNG">⬇</button>
            <button class="btn icon" data-full="#barChart"     title="Fullscreen">⤢</button>
          </div>
          <h3>Đơn theo phòng ban</h3>
          <canvas id="barChart"   width="1200" height="320"></canvas>
        </div>

        <div class="card chartbox">
          <div class="actions">
            <button class="btn icon" data-export="#heatChart"  title="Xuất PNG">⬇</button>
            <button class="btn icon" data-full="#heatChart"    title="Fullscreen">⤢</button>
          </div>
          <h3>Heatmap lịch nghỉ (tháng)</h3>
          <canvas id="heatChart" width="600"  height="320"></canvas>
        </div>
      </div>
    </div>

    <div class="grid">
      <!-- Recent requests -->
      <div class="card">
        <div class="tablebar">
          <div style="display:flex;align-items:center;gap:10px">
            <h3 style="margin:0">Đơn gần đây</h3>
            <a class="pill" href="${pageContext.request.contextPath}/request/list">Xem tất cả →</a>
          </div>
          <div style="display:flex;align-items:center;gap:8px;flex-wrap:wrap">
            <label class="switch" title="Sticky header"><input id="toggleSticky" type="checkbox"><span>Sticky</span></label>
            <label class="switch" title="Chế độ Compact"><input id="toggleCompact" type="checkbox"><span>Compact</span></label>
            <input id="searchBox" class="input" type="search" placeholder="Tìm nhanh (/ hoặc f)" />
            <button class="btn" id="exportBtn" title="Xuất CSV">⬇ Export</button>
          </div>
        </div>

        <table class="table" id="recentTable">
          <thead>
          <tr>
            <th data-sort="num">#</th>
            <th data-sort="text">Tiêu đề</th>
            <th data-sort="text">Người tạo</th>
            <th data-sort="text">Phòng ban</th>
            <th>Ngày</th>
            <th data-sort="text">Trạng thái</th>
            <th data-sort="date">Ngày tạo</th>
          </tr>
          </thead>
          <tbody>
          <c:forEach var="r" items="${recentRequests}">
            <tr>
              <td>${r.id}</td>
              <td><a href="${pageContext.request.contextPath}/request/detail?id=${r.id}">${r.title}</a></td>
              <td>${r.requester}</td>
              <td>${r.department}</td>
              <td>
                <fmt:formatDate value="${r.startDate}" pattern="dd/MM"/>–<fmt:formatDate value="${r.endDate}" pattern="dd/MM"/>
                <span class="pill" style="margin-left:6px" title="Số ngày">${r.days}d</span>
              </td>
              <td><span class="status ${r.status}">${r.status}</span></td>
              <td><fmt:formatDate value="${r.createdAt}" pattern="yyyy-MM-dd HH:mm"/></td>
            </tr>
          </c:forEach>
          <c:if test="${empty recentRequests}">
            <tr><td colspan="7" class="muted">Chưa có dữ liệu</td></tr>
          </c:if>
          </tbody>
        </table>
      </div>

      <!-- Today on leave -->
      <div class="card">
        <h3 style="margin:0 0 10px 0">Nghỉ hôm nay</h3>
        <c:choose>
          <c:when test="${empty todayOnLeave}">
            <div class="muted">Không có ai nghỉ hôm nay.</div>
          </c:when>
          <c:otherwise>
            <table class="table">
              <thead>
              <tr><th>Nhân sự</th><th>Phòng ban</th><th>Từ</th><th>Đến</th><th>Số ngày</th></tr>
              </thead>
              <tbody>
              <c:forEach var="t" items="${todayOnLeave}">
                <tr>
                  <td>${t.requester}</td>
                  <td>${t.department}</td>
                  <td><fmt:formatDate value="${t.startDate}" pattern="dd/MM"/></td>
                  <td><fmt:formatDate value="${t.endDate}" pattern="dd/MM"/></td>
                  <td>${t.days}</td>
                </tr>
              </c:forEach>
              </tbody>
            </table>
          </c:otherwise>
        </c:choose>
      </div>
    </div>

  </div>
</div>

<%-- FOOTER (nếu có) --%>
<%@ include file="/WEB-INF/views/common/_admin_footer.jsp" %>

<script>
  // Sidebar toggle (yêu cầu _admin_sidebar.jsp có id="sidebar")
  function toggleSidebar(){ document.getElementById('sidebar')?.classList.toggle('open'); }

  // LS keys + helpers
  const LS={theme:'lm_theme',sticky:'lm_sticky',compact:'lm_compact'};
  const $ = s => document.querySelector(s);
  const $$= s => Array.from(document.querySelectorAll(s));

  // Theme
  (function(){
    const pref=localStorage.getItem(LS.theme);
    if(pref==='dark') document.documentElement.classList.add('dark');
    document.getElementById('themeBtn')?.addEventListener('click',()=>{
      const on=document.documentElement.classList.toggle('dark');
      localStorage.setItem(LS.theme,on?'dark':'light');
      toast(on?'Đã bật Dark mode':'Đã tắt Dark mode');
    });
  })();

  // Copy link
  document.getElementById('copyLinkBtn')?.addEventListener('click', async ()=>{
    try{ await navigator.clipboard.writeText(location.href); toast('Đã copy link trang vào clipboard'); }
    catch{ toast('Không copy được link', true); }
  });

  // Table prefs + search + sort
  (function(){
    const recentTable = document.getElementById('recentTable');
    if(!recentTable) return;

    const sticky  = localStorage.getItem(LS.sticky)==='1';
    const compact = localStorage.getItem(LS.compact)==='1';
    document.getElementById('toggleSticky').checked  = sticky;
    document.getElementById('toggleCompact').checked = compact;
    if(sticky)  recentTable.classList.add('sticky');
    if(compact) recentTable.classList.add('compact');

    document.getElementById('toggleSticky').addEventListener('change', e=>{
      recentTable.classList.toggle('sticky', e.target.checked);
      localStorage.setItem(LS.sticky, e.target.checked ? '1':'0');
    });
    document.getElementById('toggleCompact').addEventListener('change', e=>{
      recentTable.classList.toggle('compact', e.target.checked);
      localStorage.setItem(LS.compact, e.target.checked ? '1':'0');
    });

    function normalize(s){return (s||'').toString().toLowerCase().normalize('NFKD').replace(/[\u0300-\u036f]/g,'');}
    document.getElementById('searchBox')?.addEventListener('input', e=>{
      const q=normalize(e.target.value);
      [...recentTable.tBodies[0].rows].forEach(tr=>{
        tr.style.display = normalize(tr.innerText).indexOf(q) >= 0 ? '' : 'none';
      });
    });

    let sortState={idx:-1,dir:1};
    recentTable.querySelectorAll('thead th[data-sort]').forEach((th,idx)=>{
      th.style.cursor='pointer'; th.title='Click để sắp xếp';
      th.addEventListener('click',()=>{
        const type=th.dataset.sort;
        sortState.dir=(sortState.idx===idx)?-sortState.dir:1; sortState.idx=idx;
        const rows=[...recentTable.tBodies[0].rows].filter(r=>r.style.display!=='none');
        rows.sort((a,b)=>{
          const ta=a.cells[idx].innerText.trim(), tb=b.cells[idx].innerText.trim();
          if(type==='num')  return (parseFloat(ta)||0)-(parseFloat(tb)||0);
          if(type==='date') return new Date(ta)-new Date(tb);
          return ta.localeCompare(tb,'vi',{sensitivity:'base'});
        });
        if(sortState.dir<0) rows.reverse();
        rows.forEach(r=>recentTable.tBodies[0].appendChild(r));
      });
    });
  })();

  // Shortcuts
  addEventListener('keydown', e=>{
    if((e.key==='/'||e.key==='f') && !/input|textarea/i.test(e.target.tagName)){
      e.preventDefault(); document.getElementById('searchBox')?.focus();
    }
    if(e.key==='?'){ e.preventDefault(); openHelp(); }
  });

  // KPI ring animation
  (function(){
    const svg=document.querySelector('.ring svg'); if(!svg) return;
    const rate=parseFloat(svg.dataset.rate||'0');
    const c=2*Math.PI*16, dash=Math.max(0,Math.min(100,rate))*c/100;
    const circle=document.getElementById('ringProgress');
    let cur=0, step=20;
    const t=setInterval(()=>{
      cur += dash/step;
      circle.setAttribute('stroke-dasharray', cur+' '+(c-cur));
      if(cur>=dash){ circle.setAttribute('stroke-dasharray', dash+' '+(c-dash)); clearInterval(t); }
    },12);
  })();

  // ---------- Data from server (nếu có) ----------
  const serverStatsDays   =[<c:forEach var="d" items="${statsDays}"   varStatus="s">${s.first?"":","}"${d}"</c:forEach>];
  const serverStatsCounts =[<c:forEach var="n" items="${statsCounts}" varStatus="s">${s.first?"":","}${n}</c:forEach>];
  const serverStatusPairs =[<c:forEach var="e" items="${statusCounts}" varStatus="s">${s.first?"":","}["${e.key}",${e.value}]</c:forEach>];
  const serverDeptPairs   =[<c:forEach var="e" items="${deptCounts}"   varStatus="s">${s.first?"":","}["${e.key}",${e.value}]</c:forEach>];
  const serverHeat        =[]; // có thể đổ ma trận 6x7 ở server

  // ---------- Mini chart engine (Canvas) ----------
  function drawLine(ctx,labels,series){
    const w=ctx.canvas.width,h=ctx.canvas.height; ctx.clearRect(0,0,w,h);
    const pad=40, max=Math.max(1,...series), step=(h-2*pad)/max;
    ctx.strokeStyle='#cbd5e1'; ctx.lineWidth=1; ctx.beginPath(); ctx.moveTo(pad,pad); ctx.lineTo(pad,h-pad); ctx.lineTo(w-pad,h-pad); ctx.stroke();
    ctx.globalAlpha=.3; for(let i=0;i<=max;i+=Math.ceil(max/5)){const y=h-pad-i*step; ctx.beginPath(); ctx.moveTo(pad,y); ctx.lineTo(w-pad,y); ctx.stroke()} ctx.globalAlpha=1;
    const dx=(w-2*pad)/Math.max(1,(series.length-1));
    ctx.lineWidth=2; ctx.strokeStyle='#2563eb'; ctx.beginPath();
    series.forEach((v,i)=>{const x=pad+i*dx, y=h-pad-v*step; (i?ctx.lineTo(x,y):ctx.moveTo(x,y))}); ctx.stroke();
    ctx.fillStyle='#2563eb'; series.forEach((v,i)=>{const x=pad+i*dx, y=h-pad-v*step; ctx.beginPath(); ctx.arc(x,y,3,0,Math.PI*2); ctx.fill()});
    ctx.fillStyle='#64748b'; ctx.font='12px system-ui';
    const skip=Math.ceil(labels.length/6); labels.forEach((lb,i)=>{ if(i%skip) return; const x=pad+i*dx; ctx.fillText(lb, x-16, h-pad+14) });
  }
  function drawDonut(ctx,pairs){
    const total=pairs.reduce((s,[_k,v])=>s+v,0)||1; ctx.clearRect(0,0,ctx.canvas.width,ctx.canvas.height);
    const cx=ctx.canvas.width/2, cy=ctx.canvas.height/2, r=Math.min(cx,cy)-10, ri=r*0.55;
    let a0=-Math.PI/2; const cols=['#16a34a','#f59e0b','#ef4444','#7c3aed','#2563eb','#10b981'];
    pairs.forEach(([k,v],i)=>{ const a1=a0+2*Math.PI*(v/total); ctx.beginPath(); ctx.moveTo(cx,cy); ctx.arc(cx,cy,r,a0,a1); ctx.closePath(); ctx.fillStyle=cols[i%cols.length]; ctx.fill(); a0=a1; });
    ctx.globalCompositeOperation='destination-out'; ctx.beginPath(); ctx.arc(cx,cy,ri,0,Math.PI*2); ctx.fill(); ctx.globalCompositeOperation='source-over';
    ctx.font='12px system-ui'; ctx.fillStyle='#64748b'; let y=14; pairs.forEach(([k,v],i)=>{ ctx.fillStyle=cols[i%cols.length]; ctx.fillRect(10,y-10,10,10); ctx.fillStyle='#64748b'; ctx.fillText(`${k}: ${v}`, 26, y); y+=16; });
  }
  function drawBar(ctx,pairs){
    const w=ctx.canvas.width,h=ctx.canvas.height; ctx.clearRect(0,0,w,h);
    const pad=40, max=Math.max(1,...pairs.map(p=>p[1])), step=(h-2*pad)/max; const bw=(w-2*pad)/Math.max(1,pairs.length);
    ctx.strokeStyle='#cbd5e1'; ctx.beginPath(); ctx.moveTo(pad,pad); ctx.lineTo(pad,h-pad); ctx.lineTo(w-pad,h-pad); ctx.stroke();
    pairs.forEach(([k,v],i)=>{ const x=pad+i*bw+8, y=h-pad-v*step, bh=v*step; ctx.fillStyle='#7c3aed'; ctx.fillRect(x,y,bw-16,bh);
      ctx.fillStyle='#64748b'; ctx.font='12px system-ui'; ctx.save(); ctx.translate(x+(bw-16)/2,h-pad+12); ctx.rotate(-Math.PI/4); ctx.fillText(k,0,0); ctx.restore(); });
  }
  function drawHeat(ctx,data){
    const w=ctx.canvas.width,h=ctx.canvas.height; ctx.clearRect(0,0,w,h);
    const rows=data.length, cols=data[0]?.length||7, cw=(w-60)/cols, ch=(h-40)/rows, max=Math.max(1,...data.flat());
    ctx.fillStyle='#64748b'; ctx.font='12px system-ui'; ['T2','T3','T4','T5','T6','T7','CN'].forEach((d,i)=>ctx.fillText(d,40+i*cw,16));
    data.forEach((row,r)=>row.forEach((v,c)=>{ const x=30+c*cw, y=20+r*ch, alpha=Math.max(0.08, v/max); ctx.fillStyle=`rgba(37,99,235,${alpha})`; ctx.fillRect(x,y,cw-6,ch-6); }));
  }

  // Parse/collect
  function inRange(d,range){ if(!range) return true; const {from,to}=range; if(from&&d<from) return false; if(to&&d>to) return false; return true; }
  function collectFromTable(range){
    const table=document.getElementById('recentTable'); if(!table) return {days:[],counts:[],byStatus:{},byDept:{},heat:[]};
    const rows=[...table.tBodies[0].rows].filter(r=>r.style.display!=='none');
    const byDay={}, byStatus={}, byDept={};
    rows.forEach(r=>{
      const status=r.cells[5]?.innerText.trim()||''; const dept=r.cells[3]?.innerText.trim()||'';
      const created=r.cells[6]?.innerText.trim(); const d=new Date(created);
      if(!isNaN(d) && inRange(d,range)){ const key=d.toISOString().slice(0,10); byDay[key]=(byDay[key]||0)+1; }
      if(status) byStatus[status]=(byStatus[status]||0)+1;
      if(dept)   byDept[dept]  =(byDept[dept]  ||0)+1;
    });
    const days=Object.keys(byDay).sort(); const counts=days.map(k=>byDay[k]);
    return {days,counts,byStatus,byDept,heat:[[0,0,0,0,0,0,0],[0,0,0,0,0,0,0],[0,0,0,0,0,0,0],[0,0,0,0,0,0,0],[0,0,0,0,0,0,0],[0,0,0,0,0,0,0]]};
  }
  function getRange(){ const f=$('#fromDate')?.value?new Date($('#fromDate').value):null; const t=$('#toDate')?.value?new Date($('#toDate').value+'T23:59:59'):null; return (f||t)?{from:f,to:t}:null }
  function toPairs(obj){ return Object.entries(obj).sort((a,b)=>b[1]-a[1]); }
  function buildData(range){
    if(serverStatsDays.length){
      return {
        days: serverStatsDays, counts: serverStatsCounts,
        byStatus: Object.fromEntries(serverStatusPairs),
        byDept:   Object.fromEntries(serverDeptPairs),
        heat: serverHeat.length?serverHeat:[[0,0,0,0,0,0,0],[0,0,0,0,0,0,0],[0,0,0,0,0,0,0],[0,0,0,0,0,0,0],[0,0,0,0,0,0,0],[0,0,0,0,0,0,0]],
      };
    }
    return collectFromTable(range);
  }
  function renderAll(){
    const range=getRange(), d=buildData(range);
    drawLine($('#lineChart').getContext('2d'), d.days, d.counts);
    drawDonut($('#donutChart').getContext('2d'), toPairs(d.byStatus));
    drawBar($('#barChart').getContext('2d'),   toPairs(d.byDept));
    drawHeat($('#heatChart').getContext('2d'), d.heat);
  }
  document.getElementById('applyRange')?.addEventListener('click',renderAll);
  document.getElementById('resetRange')?.addEventListener('click',()=>{$('#fromDate').value='';$('#toDate').value='';renderAll();});
  renderAll();

  // Export CSV
  (function(){
    const exportBtn=document.getElementById('exportBtn'); if(!exportBtn) return;
    function tableToCSV(table){
      const rows=[...table.querySelectorAll('tr')].filter(r=>r.offsetParent!==null);
      return rows.map(r=>[...r.children].map(td=>{
        const t=(td.innerText||'').replace(/\s+/g,' ').trim().replace(/"/g,'""');
        return `"${t}"`;
      }).join(',')).join('\n');
    }
    exportBtn.addEventListener('click', ()=>{
      const table=document.getElementById('recentTable'); if(!table){alert('Không tìm thấy bảng'); return;}
      const csv=tableToCSV(table); const blob=new Blob([csv],{type:'text/csv;charset=utf-8;'});
      const a=document.createElement('a'); a.href=URL.createObjectURL(blob); a.download='recent-requests.csv'; a.click();
      toast('Đã xuất CSV cho Đơn gần đây');
    });
  })();

  // Toast + Help
  function toast(msg,danger){let box=document.querySelector('.toast');if(!box){box=document.createElement('div');box.className='toast';box.innerHTML='<div class="tcard"></div>';document.body.appendChild(box)}box.querySelector('.tcard').textContent=msg;box.classList.add('show');if(danger) box.querySelector('.tcard').style.borderColor='#fecaca';setTimeout(()=>box.classList.remove('show'),2200)}
  function openHelp(){let m=document.getElementById('helpModal');if(!m){m=document.createElement('div');m.id='helpModal';m.className='modal';m.innerHTML=`<div class="box"><h3 style="margin:0 0 8px">Phím tắt & Mẹo</h3><ul style="line-height:1.7;margin:0 0 8px 18px"><li><b>/</b> hoặc <b>f</b> – tìm nhanh</li><li>Click tiêu đề cột để sắp xếp</li><li>Sticky/Compact sẽ được lưu</li><li>🌓 bật/tắt Dark mode</li><li>🔗 sao chép link hiện tại</li></ul><div style="text-align:right"><button class="btn" onclick="document.getElementById('helpModal').classList.remove('open')">Đóng</button></div></div>`;m.addEventListener('click',e=>{if(e.target===m) m.classList.remove('open')});document.body.appendChild(m)}m.classList.add('open')}
</script>

</body>
</html>
