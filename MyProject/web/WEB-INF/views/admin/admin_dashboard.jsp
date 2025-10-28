<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%-- HEADER + SIDEBAR DÙNG CHUNG --%>
<%@ include file="/WEB-INF/views/common/_admin_header.jsp" %>
<%@ include file="/WEB-INF/views/common/_admin_sidebar.jsp" %>

<style>
  /* ==== Elevate the whole page look ==== */
  .topbar {
    background: linear-gradient(180deg, rgba(255,255,255,.9), rgba(255,255,255,.7));
    backdrop-filter: blur(6px);
  }
  @media (prefers-color-scheme: dark){
    .topbar { background: linear-gradient(180deg, rgba(15,23,42,.9), rgba(15,23,42,.6)); }
  }
  .tools { display:flex; align-items:center; gap:8px; flex-wrap:wrap }
  .btn.primary{ border-color: transparent; background: linear-gradient(135deg, var(--pri), #8ab4ff); color:#fff }
  .btn.ghost{ background: transparent }
  .btn.icon{ width:38px; height:38px; justify-content:center; padding:0 }

  /* KPI grid */
  .kpis{ display:grid; grid-template-columns:repeat(4,minmax(0,1fr)); gap:14px; margin-bottom:14px }
  .card.kpi{ position:relative; overflow:hidden }
  .card.kpi:after{
    content:""; position:absolute; inset:auto -30% -30% auto; width:140px; height:140px;
    background:radial-gradient( circle at 70% 30%, rgba(99,102,241,.2), transparent 60%);
    transform:rotate(25deg);
  }
  .sub { color:var(--muted); font-size:12px }
  .num.big { font-size:28px; font-weight:800; letter-spacing:.2px }

  /* Progress ring */
  .ring { width:56px; height:56px; position:relative }
  .ring svg { transform:rotate(-90deg) }
  .ring .val { position:absolute; inset:0; display:grid; place-items:center; font-size:12px; font-weight:700 }

  /* Table controls */
  .tablebar{ display:flex; align-items:center; justify-content:space-between; gap:12px; margin-bottom:10px; flex-wrap:wrap }
  .input{ border:1px solid var(--bd); border-radius:10px; padding:8px 10px; background:var(--card) }
  .switch{ display:inline-flex; align-items:center; gap:6px; cursor:pointer; user-select:none; font-size:13px }
  .switch input{ appearance:none; width:34px; height:20px; border-radius:999px; background:#e5e7eb; position:relative; outline:none; transition:.2s }
  .switch input:checked{ background:#c7d2fe }
  .switch input:before{ content:""; position:absolute; width:16px; height:16px; border-radius:50%; background:#fff; top:2px; left:2px; transition:.2s }
  .switch input:checked:before{ transform:translateX(14px) }

  /* Table states */
  .table.compact th,.table.compact td{ padding:6px 10px }
  .sticky thead tr th{ position:sticky; top:56px; z-index:5 } /* dưới topbar */

  /* Status pill colors (giữ class cũ) */
  .status.APPROVED{background:rgba(34,197,94,.12);border-color:rgba(34,197,94,.3); color:#065f46}
  .status.PENDING{background:rgba(245,158,11,.12);border-color:rgba(245,158,11,.3); color:#92400e}
  .status.REJECTED{background:rgba(239,68,68,.12);border-color:rgba(239,68,68,.3); color:#991b1b}

  /* Toast */
  .toast{ position:fixed; right:16px; bottom:16px; max-width:420px; z-index:50; display:none }
  .toast.show{ display:block; animation:toastIn .2s ease-out }
  .toast .tcard{ background:var(--card); border:1px solid var(--bd); border-radius:12px; padding:10px 12px; box-shadow:0 8px 30px rgba(0,0,0,.08) }
  @keyframes toastIn{ from{ opacity:0; transform:translateY(8px) } to{ opacity:1; transform:none } }

  /* Help modal */
  .modal{ position:fixed; inset:0; display:none; align-items:center; justify-content:center; background:rgba(0,0,0,.35); z-index:60 }
  .modal.open{ display:flex }
  .modal .box{ width:min(560px, calc(100% - 24px)); background:var(--card); border:1px solid var(--bd); border-radius:16px; padding:16px }

  /* Hover effects */
  .table tbody tr:hover{ background:rgba(148,163,184,.08) }

  @media (max-width:1100px){ .kpis{ grid-template-columns:repeat(2,minmax(0,1fr)) } }
  @media (max-width:720px){ .kpis{ grid-template-columns:1fr } }
</style>

<div class="main">
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

  <div class="content">
    <h2 style="margin:0 0 8px">Admin Dashboard</h2>
    <div class="muted" style="margin-bottom:14px">Tổng quan tình hình nghỉ phép</div>

    <!-- KPIs -->
    <div class="kpis">
      <div class="card kpi">
        <div class="sub">Đang chờ duyệt</div>
        <div class="num big" title="Tất cả trạng thái đang chờ">${kpis.pendingAll}</div>
      </div>

      <div class="card kpi">
        <div class="sub">Đã duyệt trong tháng</div>
        <div class="num big">${kpis.approvedThisMonth}</div>
      </div>

      <div class="card kpi">
        <div class="sub">Tổng đơn trong tháng</div>
        <div class="num big">${kpis.totalThisMonth}</div>
      </div>

      <div class="card kpi" style="display:flex; align-items:center; gap:12px">
        <div class="ring" title="Approval rate 30 ngày">
          <%
            // Chuẩn bị giá trị ring ở server để chắc chắn không NaN
            Double rate = (Double) request.getAttribute("kpis.approvalRate30d");
          %>
          <c:set var="rate" value="${kpis.approvalRate30d}" />
          <svg viewBox="0 0 36 36" width="56" height="56" data-rate="${kpis.approvalRate30d}">
            <circle cx="18" cy="18" r="16" fill="none" stroke="rgba(148,163,184,.35)" stroke-width="4"></circle>
            <circle id="ringProgress" cx="18" cy="18" r="16" fill="none" stroke="url(#grad)" stroke-width="4"
                    stroke-linecap="round" stroke-dasharray="0 100"></circle>
            <defs>
              <linearGradient id="grad" x1="0" y1="0" x2="1" y2="1">
                <stop offset="0%" stop-color="#60a5fa" />
                <stop offset="100%" stop-color="#16a34a" />
              </linearGradient>
            </defs>
          </svg>
          <div class="val"><fmt:formatNumber value="${kpis.approvalRate30d}" maxFractionDigits="0"/>%</div>
        </div>
        <div>
          <div class="sub">Tỉ lệ duyệt 30 ngày</div>
          <div class="muted" style="font-size:12px">Base: ${kpis.approvalBase30d}</div>
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
            <label class="switch" title="Sticky header">
              <input id="toggleSticky" type="checkbox"><span>Sticky header</span>
            </label>
            <label class="switch" title="Chế độ Compact">
              <input id="toggleCompact" type="checkbox"><span>Compact</span>
            </label>
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
            <th>Trạng thái</th>
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
                <span class="pill" style="margin-left:6px">${r.days}d</span>
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
              <tr>
                <th>Nhân sự</th>
                <th>Phòng ban</th>
                <th>Từ</th>
                <th>Đến</th>
                <th>Số ngày</th>
              </tr>
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
    </div> <!-- /content -->
  </div>   <!-- /main -->
</div>     <!-- /layout -->
<%-- FOOTER DÙNG CHUNG --%>
<%@ include file="/WEB-INF/views/common/_admin_footer.jsp" %>

<script>
  // === User prefs (theme, compact, sticky) ===
  const LS = {
    theme: 'lm_theme',   // 'dark' | 'light'
    sticky: 'lm_sticky', // '1' | '0'
    compact: 'lm_compact'
  };

  const recentTable = document.getElementById('recentTable');
  const searchBox   = document.getElementById('searchBox');
  const exportBtn   = document.getElementById('exportBtn');
  const stickyCb    = document.getElementById('toggleSticky');
  const compactCb   = document.getElementById('toggleCompact');
  const themeBtn    = document.getElementById('themeBtn');
  const copyLinkBtn = document.getElementById('copyLinkBtn');

  // Init theme
  (function initTheme(){
    const pref = localStorage.getItem(LS.theme);
    if (pref === 'dark') document.documentElement.classList.add('dark');
    themeBtn?.addEventListener('click', ()=>{
      const isDark = document.documentElement.classList.toggle('dark');
      localStorage.setItem(LS.theme, isDark ? 'dark' : 'light');
      toast(isDark ? 'Đã bật Dark mode' : 'Đã tắt Dark mode');
    });
  })();

  // Remember table options
  (function initToggles(){
    const sticky = localStorage.getItem(LS.sticky) === '1';
    const compact = localStorage.getItem(LS.compact) === '1';
    stickyCb.checked = sticky; compactCb.checked = compact;
    if (sticky) recentTable.classList.add('sticky');
    if (compact) recentTable.classList.add('compact');

    stickyCb.addEventListener('change', ()=>{
      recentTable.classList.toggle('sticky', stickyCb.checked);
      localStorage.setItem(LS.sticky, stickyCb.checked ? '1' : '0');
    });
    compactCb.addEventListener('change', ()=>{
      recentTable.classList.toggle('compact', compactCb.checked);
      localStorage.setItem(LS.compact, compactCb.checked ? '1' : '0');
    });
  })();

  // Copy link
  copyLinkBtn?.addEventListener('click', async ()=>{
    try {
      await navigator.clipboard.writeText(location.href);
      toast('Đã copy link trang vào clipboard');
    } catch { toast('Không copy được link', true); }
  });

  // Search/Filter in table
  function normalize(s){ return (s||'').toString().toLowerCase().normalize('NFKD').replace(/[\u0300-\u036f]/g,''); }
  searchBox?.addEventListener('input', ()=>{
    const q = normalize(searchBox.value);
    Array.from(recentTable.tBodies[0].rows).forEach(tr=>{
      const text = normalize(tr.innerText);
      tr.style.display = text.indexOf(q) >= 0 ? '' : 'none';
    });
  });

  // Keyboard shortcuts
  window.addEventListener('keydown', (e)=>{
    if ((e.key === '/' || e.key === 'f') && !/input|textarea/i.test(e.target.tagName)) {
      e.preventDefault(); searchBox?.focus();
    }
    if (e.key === '?') {
      e.preventDefault(); openHelp();
    }
  });

  // Sort table
  let sortState = { idx: -1, dir: 1 };
  recentTable?.querySelectorAll('thead th[data-sort]').forEach((th, idx)=>{
    th.style.cursor = 'pointer';
    th.title = 'Click để sắp xếp';
    th.addEventListener('click', ()=>{
      const type = th.dataset.sort;
      sortState.dir = (sortState.idx === idx) ? -sortState.dir : 1;
      sortState.idx = idx;
      const rows = Array.from(recentTable.tBodies[0].rows);
      rows.sort((a,b)=>{
        const ta=a.cells[idx].innerText.trim(), tb=b.cells[idx].innerText.trim();
        if (type==='num') return (parseFloat(ta)||0)-(parseFloat(tb)||0);
        if (type==='date') return new Date(ta) - new Date(tb);
        return ta.localeCompare(tb,'vi',{sensitivity:'base'});
      });
      if (sortState.dir<0) rows.reverse();
      rows.forEach(r=>recentTable.tBodies[0].appendChild(r));
    });
  });

  // Export CSV
  function toCSV(table){
    const rows = [...table.querySelectorAll('tr')].filter(r=>r.offsetParent!==null);
    return rows.map(r=>[...r.children].map(td=>{
      const t = td.innerText.replace(/\s+/g,' ').trim().replaceAll('"','""');
      return `"${t}"`;
    }).join(',')).join('\n');
  }
  exportBtn?.addEventListener('click', ()=>{
    const csv = toCSV(recentTable);
    const blob = new Blob([csv], {type:'text/csv;charset=utf-8;'});
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = 'recent-requests.csv';
    document.body.appendChild(a); a.click(); a.remove();
    toast('Đã xuất CSV cho Đơn gần đây');
  });

  // Progress ring animate
  (function ring(){
    const svg = document.querySelector('.ring svg'); if(!svg) return;
    const rate = parseFloat(svg.dataset.rate || '0'); // 0..100
    const c = 2*Math.PI*16; // circumference r=16
    const dash = Math.max(0, Math.min(100, rate)) * c / 100;
    const circle = document.getElementById('ringProgress');
    let cur=0, steps=20;
    const timer = setInterval(()=>{
      cur += (dash/steps);
      circle.setAttribute('stroke-dasharray', cur + ' ' + (c - cur));
      if (cur >= dash) { circle.setAttribute('stroke-dasharray', dash + ' ' + (c - dash)); clearInterval(timer); }
    }, 12);
  })();

  // Toast small helper
  function toast(msg, danger){
    let box = document.querySelector('.toast');
    if(!box){ box = document.createElement('div'); box.className='toast'; box.innerHTML='<div class="tcard"></div>'; document.body.appendChild(box); }
    box.querySelector('.tcard').textContent = msg;
    box.classList.add('show'); if(danger) box.querySelector('.tcard').style.borderColor='#fecaca';
    setTimeout(()=>box.classList.remove('show'), 2200);
  }

  // Help modal
  function openHelp(){
    let m = document.getElementById('helpModal');
    if(!m){
      m = document.createElement('div'); m.id='helpModal'; m.className='modal';
      m.innerHTML = `<div class="box">
        <h3 style="margin:0 0 8px">Phím tắt & Mẹo</h3>
        <ul style="line-height:1.7;margin:0 0 8px 18px">
          <li><b>/</b> hoặc <b>f</b> – nhảy vào ô tìm kiếm “Đơn gần đây”.</li>
          <li>Click tiêu đề cột để sắp xếp.</li>
          <li>“Sticky header”, “Compact” sẽ được lưu cho lần sau.</li>
          <li>🌓 để bật/tắt Dark mode.</li>
          <li>🔗 sao chép link hiện tại.</li>
        </ul>
        <div style="text-align:right"><button class="btn" onclick="document.getElementById('helpModal').classList.remove('open')">Đóng</button></div>
      </div>`;
      m.addEventListener('click', e=>{ if(e.target===m) m.classList.remove('open'); });
      document.body.appendChild(m);
    }
    m.classList.add('open');
  }
</script>
