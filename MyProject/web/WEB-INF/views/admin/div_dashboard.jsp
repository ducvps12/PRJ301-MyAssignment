<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="vi" data-theme="${param.theme == 'dark' ? 'dark' : 'light'}">
<head>
  <meta charset="UTF-8">
  <title>Division Dashboard</title>
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <style>
    /* ====== Design System: Light/Dark ====== */
    :root{
      --bg:#f7f7f8; --card:#fff; --ink:#0f172a; --muted:#64748b; --bd:#e5e7eb; --ink-inv:#fff;
      --pri:#111827; --pri-2:#334155; --ok:#16a34a; --warn:#d97706; --no:#dc2626; --info:#2563eb;
      --table:#f8fafc; --shadow:0 10px 24px rgba(2,6,23,.06); --chip:#eef2ff; --chip-bd:#c7d2fe;
      --ring:0 0 0 3px rgba(37,99,235,.25);
    }
    html[data-theme="dark"]{
      --bg:#0b0f14; --card:#10161d; --ink:#ecf2f8; --muted:#9fb0c3; --bd:#1e2835; --ink-inv:#0b0f14;
      --pri:#2aa0ff; --pri-2:#6fc3ff; --ok:#22c55e; --warn:#f59e0b; --no:#ef4444; --info:#60a5fa;
      --table:#0f1720; --shadow:0 10px 30px rgba(0,0,0,.35); --chip:#0f1720; --chip-bd:#1f2a36;
      --ring:0 0 0 3px rgba(42,160,255,.25);
    }
    *{box-sizing:border-box}
    body{font-family:system-ui,-apple-system,Segoe UI,Roboto,Helvetica,Arial,sans-serif;margin:0;background:var(--bg);color:var(--ink)}
    a{color:inherit;text-decoration:none}
    .wrap{max-width:1200px;margin:24px auto;padding:0 16px}
    h1{margin:0 0 12px;font-size:26px}
    .row{display:flex;gap:12px;flex-wrap:wrap;align-items:center}
    .card{background:var(--card);border:1px solid var(--bd);border-radius:14px;padding:14px;box-shadow:var(--shadow)}
    .toolbar{display:flex;flex-wrap:wrap;gap:10px;align-items:center;justify-content:space-between;margin:6px 0 14px}
    .toolbar .left,.toolbar .right{display:flex;gap:10px;align-items:center}
    .input, select{padding:9px 11px;border:1px solid var(--bd);border-radius:10px;background:var(--card);color:var(--ink)}
    .input:focus, select:focus{outline:none;box-shadow:var(--ring);border-color:transparent}
    .btn{display:inline-flex;gap:8px;align-items:center;padding:9px 12px;border:1px solid var(--bd);border-radius:10px;background:var(--card);cursor:pointer;font-size:13px}
    .btn.primary{background:var(--pri);color:var(--ink-inv);border-color:var(--pri)}
    .btn.ghost{background:transparent}
    .btn.small{padding:6px 9px;font-size:12px}
    .btn.danger{background:var(--no);color:#fff;border-color:var(--no)}
    .btn.warn{background:var(--warn);color:#fff;border-color:var(--warn)}
    .btn.ok{background:var(--ok);color:#fff;border-color:var(--ok)}
    .section{margin-top:16px}
    .cards{display:grid;grid-template-columns:repeat(4,1fr);gap:12px}
    @media (max-width:980px){ .cards{grid-template-columns:repeat(2,1fr)} }
    @media (max-width:620px){ .cards{grid-template-columns:1fr} }
    .stat h2{margin:.25rem 0 0 0;font-size:24px}
    .muted{color:var(--muted)}
    .kpi-foot{display:flex;align-items:center;justify-content:space-between;margin-top:6px;font-size:12px;color:var(--muted)}
    .bar{height:8px;background:rgba(148,163,184,.25);border-radius:999px;overflow:hidden;margin-top:8px}
    .bar>i{display:block;height:100%;background:linear-gradient(90deg,var(--ok),#3b82f6);width:0%}
    .grid-2{display:grid;grid-template-columns:1fr 1fr;gap:12px}
    @media (max-width:980px){ .grid-2{grid-template-columns:1fr} }
    .table-wrap{background:var(--card);border:1px solid var(--bd);border-radius:12px;overflow:auto}
    table{width:100%;border-collapse:collapse;min-width:840px}
    th,td{padding:10px;border-bottom:1px solid rgba(148,163,184,.15);text-align:left;font-size:14px}
    thead th{background:var(--table);position:sticky;top:0;z-index:1}
    tbody tr:hover{background:rgba(148,163,184,.08)}
    .nowrap{white-space:nowrap}
    .badge{display:inline-block;padding:2px 8px;border-radius:999px;font-size:12px;border:1px solid var(--chip-bd);background:var(--chip)}
    .badge.ok{background:rgba(34,197,94,.12);border-color:rgba(34,197,94,.35)}
    .badge.warn{background:rgba(245,158,11,.12);border-color:rgba(245,158,11,.35)}
    .badge.no{background:rgba(239,68,68,.12);border-color:rgba(239,68,68,.35)}
    .chip{display:inline-flex;gap:6px;align-items:center;padding:4px 8px;border-radius:999px;border:1px dashed var(--bd);font-size:12px}
    .pill{padding:2px 6px;border-radius:6px;background:rgba(148,163,184,.15);font-size:12px}
    .sticky-head{position:sticky;top:0;background:linear-gradient(var(--bg),var(--bg));padding:8px 0;z-index:5}
    .empty{padding:24px;text-align:center;color:var(--muted)}
    .sr-only{position:absolute;left:-9999px;width:1px;height:1px;overflow:hidden}
    /* bottom bar */
    .bar-bottom{display:flex;gap:12px;align-items:center;justify-content:space-between;padding:10px}
    .bulk{display:flex;gap:8px;align-items:center}
    .hidden{display:none !important}
    /* Modal */
    .modal{position:fixed;inset:0;background:rgba(0,0,0,.45);display:none;align-items:center;justify-content:center;padding:16px;z-index:999}
    .modal .dialog{max-width:520px;width:100%;background:var(--card);border:1px solid var(--bd);border-radius:14px;box-shadow:var(--shadow);padding:16px}
    .modal .dialog h3{margin:0 0 8px}
    .modal textarea{width:100%;min-height:110px;padding:10px;border:1px solid var(--bd);border-radius:10px;background:var(--card);color:var(--ink)}
    .modal .actions{display:flex;gap:8px;justify-content:flex-end;margin-top:12px}
    /* Toast */
    .toast{position:fixed;right:16px;bottom:16px;display:flex;flex-direction:column;gap:8px;z-index:1000}
    .toast .t{background:var(--card);border:1px solid var(--bd);box-shadow:var(--shadow);border-left:5px solid var(--ok);padding:10px 12px;border-radius:10px}
    .toast .t.error{border-left-color:var(--no)}
    /* Skeleton */
    .sk{background:linear-gradient(90deg,rgba(148,163,184,.12),rgba(148,163,184,.24),rgba(148,163,184,.12));background-size:200% 100%;animation:sh 1.4s ease infinite;border-radius:8px}
    @keyframes sh{0%{background-position:200% 0}100%{background-position:-200% 0}}
  </style>
</head>
<body>

<%@ include file="/WEB-INF/views/common/_admin_header.jsp" %>
<%@ include file="/WEB-INF/views/common/_admin_sidebar.jsp" %>

<div class="wrap">
  <div class="toolbar sticky-head">
    <div class="left">
      <h1 style="margin:0">Division Dashboard</h1>
      <span class="pill">Phòng: <b>${empty dept ? 'N/A' : dept}</b></span>
      <span class="pill">Xin chào <b>${sessionScope.currentUser != null ? sessionScope.currentUser.fullName : 'Guest'}</b></span>
    </div>
    <div class="right">
      <input id="q" class="input" type="search" placeholder="Tìm nhanh: tên / lý do / loại (phím /)" aria-label="Tìm nhanh" />
      <button id="themeBtn" class="btn" type="button" title="Đổi theme (t)">🌓 <span class="sr-only">Đổi theme</span></button>
      <button id="csvBtn" class="btn" type="button" title="Xuất CSV">⬇️ CSV</button>
    </div>
  </div>

  <!-- Bộ lọc server-side -->
  <form method="get" class="card row" action="${pageContext.request.contextPath}/admin/div" autocomplete="off">
    <c:if test="${canSwitchDept}">
      <label>Phòng:
        <input class="input" type="text" name="dept" value="${fn:escapeXml(dept)}" style="width:160px" placeholder="VD: SALE, IT">
      </label>
    </c:if>
    <label>Từ ngày:
      <input class="input" type="date" name="from" value="${from}">
    </label>
    <label>Đến ngày:
      <input class="input" type="date" name="to" value="${to}">
    </label>
    <button class="btn" type="submit">Lọc</button>
    <a class="btn ghost" href="${pageContext.request.contextPath}/admin/div">Reset</a>
    <span class="muted">Gợi ý: dùng hộp tìm nhanh bên phải để lọc tức thời.</span>
  </form>

  <!-- KPI -->
  <c:if test="${not empty stats}">
    <c:set var="den" value="${stats.approvalDenominator}" />
    <c:set var="num" value="${stats.approvalNumerator}" />
    <div class="cards section">
      <div class="card stat">
        <div>Headcount</div>
        <h2><fmt:formatNumber value="${stats.headcount}" groupingUsed="true"/></h2>
        <div class="kpi-foot"><span class="muted">Nhân sự active</span><span class="chip">Dept <b>${dept}</b></span></div>
      </div>
      <div class="card stat">
        <div>Đơn chờ duyệt</div>
        <h2><fmt:formatNumber value="${stats.pendingCount}" /></h2>
        <div class="bar"><i style="width:${stats.pendingCount > 0 ? 100 : 5}%"></i></div>
        <div class="kpi-foot"><span class="muted">Cần xử lý</span><span class="badge warn">Pending</span></div>
      </div>
      <div class="card stat">
        <div>Đã duyệt tháng này</div>
        <h2><fmt:formatNumber value="${stats.approvedThisMonth}" /></h2>
        <div class="bar"><i style="width:${stats.approvedThisMonth > 0 ? 100 : 8}%"></i></div>
        <div class="kpi-foot"><span class="muted">Theo approved_at</span><span class="badge ok">Approved</span></div>
      </div>
      <div class="card stat">
        <div>Tỉ lệ duyệt (ước)</div>
        <c:set var="rate" value="${den == 0 ? 0 : (num * 100.0 / den)}"/>
        <h2><fmt:formatNumber value="${rate}" maxFractionDigits="1"/>%</h2>
        <div class="bar"><i style="width:${rate}%"></i></div>
        <div class="kpi-foot"><span class="muted">${num}/${den}</span><span class="badge">SLA</span></div>
      </div>
    </div>
  </c:if>

  <!-- 2 bảng -->
  <div class="grid-2 section">
    <!-- Pending -->
    <div class="card" style="padding:0">
      <div style="display:flex;align-items:center;justify-content:space-between;padding:12px 14px;border-bottom:1px solid var(--bd)">
        <h3 style="margin:0">Đơn chờ duyệt</h3>
        <div class="row">
          <label class="chip">Hiển thị
            <select id="pp" aria-label="Số dòng mỗi trang">
              <option>5</option><option selected>10</option><option>20</option><option>50</option>
            </select>
          </label>
          <button id="bulkApproveBtn" class="btn ok small" type="button" title="Duyệt hàng loạt (a)">Duyệt hàng loạt</button>
          <button id="bulkRejectBtn" class="btn danger small" type="button">Từ chối hàng loạt</button>
        </div>
      </div>

      <div class="table-wrap">
        <table id="tblPending" aria-label="Danh sách đơn chờ duyệt">
          <thead>
          <tr>
            <th style="width:34px"><input type="checkbox" id="chkAll"></th>
            <th class="nowrap">#</th>
            <th>Nhân sự</th>
            <th>Loại</th>
            <th class="nowrap">Từ</th>
            <th class="nowrap">Đến</th>
            <th>Lý do</th>
            <th class="nowrap">Thao tác</th>
          </tr>
          </thead>
          <tbody id="pendingBody">
          <c:forEach var="r" items="${pending}" varStatus="vs">
            <tr data-id="${r.id}" data-name="${fn:escapeXml(r.fullName)}" data-type="${fn:escapeXml(r.type)}" data-reason="${fn:escapeXml(r.reason)}">
              <td><input type="checkbox" class="rowChk"></td>
              <td class="nowrap">${vs.index + 1}</td>
              <td>${r.fullName}</td>
              <td><span class="badge warn">${r.type}</span></td>
              <td class="nowrap">
                <c:choose>
                  <c:when test="${not empty r.from}"><fmt:formatDate value="${r.from}" pattern="yyyy-MM-dd"/></c:when>
                  <c:otherwise>—</c:otherwise>
                </c:choose>
              </td>
              <td class="nowrap">
                <c:choose>
                  <c:when test="${not empty r.to}"><fmt:formatDate value="${r.to}" pattern="yyyy-MM-dd"/></c:when>
                  <c:otherwise>—</c:otherwise>
                </c:choose>
              </td>
              <td class="muted" style="max-width:260px; overflow:hidden; text-overflow:ellipsis;">${r.reason}</td>
              <td class="nowrap">
                <a class="btn small ghost" href="${pageContext.request.contextPath}/request/${r.id}">Xem</a>
                <button class="btn small ok act-single" data-action="approve" data-id="${r.id}" type="button">Approve</button>
                <button class="btn small danger act-single" data-action="reject" data-id="${r.id}" type="button">Reject</button>
              </td>
            </tr>
          </c:forEach>
          <c:if test="${empty pending}">
            <tr><td colspan="8" class="empty">Không có đơn chờ duyệt</td></tr>
          </c:if>
          </tbody>
        </table>
      </div>

      <div class="bar-bottom">
        <div class="bulk">
          <span class="muted">Đã chọn: <b id="selCount">0</b></span>
          <span id="selHint" class="muted hidden">Nhấn <b>a</b> để duyệt nhanh</span>
        </div>
        <div class="row">
          <button id="prev" class="btn small" type="button">◀ Trang trước</button>
          <span class="muted">Trang <b id="page">1</b>/<b id="pages">1</b></span>
          <button id="next" class="btn small" type="button">Trang sau ▶</button>
        </div>
      </div>
    </div>

    <!-- Today off -->
    <div class="card" style="padding:0">
      <div style="display:flex;align-items:center;justify-content:space-between;padding:12px 14px;border-bottom:1px solid var(--bd)">
        <h3 style="margin:0">Đang nghỉ hôm nay</h3>
        <span class="chip">Today: <b><fmt:formatDate value="<%= new java.util.Date() %>" pattern="yyyy-MM-dd"/></b></span>
      </div>
      <div class="table-wrap">
        <table aria-label="Danh sách đang nghỉ hôm nay">
          <thead><tr>
            <th class="nowrap">#</th><th>Nhân sự</th><th>Loại</th><th class="nowrap">Từ</th><th class="nowrap">Đến</th>
          </tr></thead>
          <tbody>
          <c:forEach var="t" items="${todayOff}" varStatus="vs">
            <tr>
              <td class="nowrap">${vs.index + 1}</td>
              <td>${t.fullName}</td>
              <td><span class="badge ok">${t.type}</span></td>
              <td class="nowrap">
                <c:choose>
                  <c:when test="${not empty t.from}"><fmt:formatDate value="${t.from}" pattern="yyyy-MM-dd"/></c:when>
                  <c:otherwise>—</c:otherwise>
                </c:choose>
              </td>
              <td class="nowrap">
                <c:choose>
                  <c:when test="${not empty t.to}"><fmt:formatDate value="${t.to}" pattern="yyyy-MM-dd"/></c:when>
                  <c:otherwise>—</c:otherwise>
                </c:choose>
              </td>
            </tr>
          </c:forEach>
          <c:if test="${empty todayOff}">
            <tr><td colspan="5" class="empty">Không ai nghỉ hôm nay</td></tr>
          </c:if>
          </tbody>
        </table>
      </div>
    </div>
  </div>

  <!-- Modal xác nhận (dùng chung) -->
  <div id="modal" class="modal" role="dialog" aria-modal="true" aria-labelledby="mTitle">
    <form id="mForm" class="dialog" method="post" action="${pageContext.request.contextPath}/request/approve">
      <h3 id="mTitle">Xác nhận</h3>
      <p id="mDesc" class="muted" style="margin-top:0">Bạn có chắc muốn thực hiện thao tác này?</p>
      <label for="mNote" class="muted">Ghi chú (không bắt buộc):</label>
      <textarea id="mNote" name="note" placeholder="Lý do/ghi chú …"></textarea>
      <input type="hidden" name="_csrf" value="${csrf}">
      <input type="hidden" name="action" id="mAction" value="">
      <!-- single -->
      <input type="hidden" name="id" id="mId" value="">
      <!-- bulk -->
      <div id="mIds"></div>
      <div class="actions">
        <button type="button" id="mCancel" class="btn">Hủy</button>
        <button id="mOk" class="btn primary" type="submit">Xác nhận</button>
      </div>
    </form>
  </div>

  <!-- Toast -->
  <div class="toast" id="toast"></div>

</div>

<script>
(function(){
  const $ = s=>document.querySelector(s);
  const $$ = s=>Array.from(document.querySelectorAll(s));
  const toast = (msg, ok=true)=>{
    const t = document.createElement('div');
    t.className = 't'+(ok?'':' error');
    t.textContent = msg;
    $('#toast').appendChild(t);
    setTimeout(()=>t.remove(), 3200);
  };

  // Theme
  const themeBtn = $('#themeBtn');
  const keyTheme = 'lm.theme';
  const applyTheme = (v)=>document.documentElement.setAttribute('data-theme', v);
  (function initTheme(){
    const v = localStorage.getItem(keyTheme) || (matchMedia('(prefers-color-scheme: dark)').matches?'dark':'light');
    applyTheme(v);
  })();
  themeBtn.addEventListener('click', ()=>{
    const cur = document.documentElement.getAttribute('data-theme')==='dark'?'dark':'light';
    const nxt = cur==='dark'?'light':'dark';
    applyTheme(nxt); localStorage.setItem(keyTheme, nxt);
  });

  // search + filter
  const q = $('#q');
  function matches(tr, term){
    term = term.trim().toLowerCase();
    if(!term) return true;
    const name = tr.dataset.name ? tr.dataset.name.toLowerCase() : '';
    const type = tr.dataset.type ? tr.dataset.type.toLowerCase() : '';
    const reason = tr.dataset.reason ? tr.dataset.reason.toLowerCase() : '';
    return name.includes(term) || type.includes(term) || reason.includes(term);
  }

  // pagination
  const ppSel = $('#pp'), pageEl = $('#page'), pagesEl = $('#pages');
  const prevBtn = $('#prev'), nextBtn = $('#next');
  let curPage = 1, perPage = parseInt(ppSel.value,10)||10;
  function renderPage(){
    const rows = $$('#pendingBody > tr');
    const visibles = rows.filter(r=>!r.classList.contains('filtered-out'));
    const total = visibles.length;
    const pages = Math.max(1, Math.ceil(total/perPage));
    if(curPage>pages) curPage=pages;
    const start = (curPage-1)*perPage;
    const end = start+perPage;
    visibles.forEach((r,i)=> r.style.display = (i>=start && i<end)?'':'none');
    pageEl.textContent = String(curPage);
    pagesEl.textContent = String(pages);
  }
  function reFilter(){
    const term = q.value;
    $$('#pendingBody > tr').forEach(tr=>{
      tr.classList.toggle('filtered-out', !matches(tr, term));
    });
    curPage = 1;
    renderPage();
  }
  q.addEventListener('input', reFilter);
  ppSel.addEventListener('change', ()=>{ perPage = parseInt(ppSel.value,10)||10; renderPage(); });
  prevBtn.addEventListener('click', ()=>{ if(curPage>1){curPage--; renderPage();} });
  nextBtn.addEventListener('click', ()=>{ curPage++; renderPage(); });

  // bulk select
  const chkAll = $('#chkAll');
  const selCount = $('#selCount');
  const selHint = $('#selHint');
  function updateBulk(){
    const chks = $$('.rowChk:checked');
    selCount.textContent = chks.length;
    selHint.classList.toggle('hidden', chks.length===0);
  }
  chkAll.addEventListener('change', ()=>{
    $$('.rowChk').forEach(c=>{ c.checked = chkAll.checked; });
    updateBulk();
  });
  $$('.rowChk').forEach(c=> c.addEventListener('change', ()=>{
    if(!c.checked) chkAll.checked = false;
    updateBulk();
  }));

  // modal
  const modal = $('#modal'),
        mTitle = $('#mTitle'),
        mDesc = $('#mDesc'),
        mForm = $('#mForm'),
        mAction = $('#mAction'),
        mId = $('#mId'),
        mIds = $('#mIds'),
        mCancel = $('#mCancel'),
        mNote = $('#mNote');

  function openModal(args){
    const action = args.action;
    const ids = args.ids;
    mAction.value = action;
    mIds.innerHTML = '';
    mId.value = '';

    if(ids.length === 1){
      mTitle.textContent = (action === 'approve' ? 'Duyệt' : 'Từ chối') + ' đơn #' + ids[0];
      mDesc.textContent = 'Bạn có thể thêm ghi chú trước khi xác nhận.';
      mId.value = ids[0];
    }else{
      mTitle.textContent = (action === 'approve' ? 'Duyệt' : 'Từ chối') + ' ' + ids.length + ' đơn đã chọn';
      mDesc.textContent = 'Hệ thống sẽ gửi thao tác cho tất cả các đơn.';
      ids.forEach(function(id){
        const inp = document.createElement('input');
        inp.type='hidden'; inp.name='ids'; inp.value=id;
        mIds.appendChild(inp);
      });
    }
    mNote.value = '';
    modal.style.display='flex';
    mNote.focus();
  }
  function closeModal(){ modal.style.display='none'; }

  mCancel.addEventListener('click', function(){ closeModal(); });
  modal.addEventListener('click', function(e){ if(e.target===modal) closeModal(); });

  // single buttons
  $$('.act-single').forEach(function(btn){
    btn.addEventListener('click', function(){
      const id = btn.dataset.id;
      openModal({action:btn.dataset.action, ids:[id]});
    });
  });

  // bulk buttons
  const bulkApproveBtn = $('#bulkApproveBtn'), bulkRejectBtn = $('#bulkRejectBtn');
  function getSelectedIds(){
    return $$('.rowChk:checked').map(c=> c.closest('tr').dataset.id);
  }
  bulkApproveBtn.addEventListener('click', function(){
    const ids = getSelectedIds();
    if(ids.length===0) return toast('Chưa chọn đơn nào', false);
    openModal({action:'approve', ids:ids});
  });
  bulkRejectBtn.addEventListener('click', function(){
    const ids = getSelectedIds();
    if(ids.length===0) return toast('Chưa chọn đơn nào', false);
    openModal({action:'reject', ids:ids});
  });

  // CSV
  const csvBtn = $('#csvBtn');
  csvBtn.addEventListener('click', function(){
    const rows = $$('#pendingBody > tr').filter(r=> r.style.display !== 'none');
    if(rows.length===0){ toast('Không có dữ liệu để xuất', false); return; }
    const header = ['ID','Nhân sự','Loại','Từ','Đến','Lý do'];
    const data = rows.map(r=>{
      const id = r.dataset.id;
      const tds = r.querySelectorAll('td');
      return [
        id,
        tds[2].innerText.trim(),
        tds[3].innerText.replace(/\s+/g,' ').trim(),
        tds[4].innerText.trim(),
        tds[5].innerText.trim(),
        tds[6].innerText.replace(/\s+/g,' ').trim()
      ];
    });
    const csv = [header].concat(data).map(a=>a.map(v=>{
      v = (v??'').toString();
      if(v.includes('"')||v.includes(',')||v.includes('\n')) v='"'+v.replace(/"/g,'""')+'"';
      return v;
    }).join(',')).join('\n');
    const blob = new Blob([csv], {type:'text/csv;charset=utf-8;'});
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    const todayISO = (new Date()).toISOString().slice(0, 10);
    a.download = 'pending_' + todayISO + '.csv';
    document.body.appendChild(a); a.click(); a.remove();
    URL.revokeObjectURL(url);
    toast('Đã tải CSV');
  });

  // shortcuts
  document.addEventListener('keydown', function(e){
    if(e.key==='/'){ e.preventDefault(); q.focus(); }
    if(e.key==='t'){ e.preventDefault(); themeBtn.click(); }
    if(e.key==='a'){
      const ids = getSelectedIds();
      if(ids.length>0){ e.preventDefault(); openModal({action:'approve', ids:ids}); }
    }
  });

  // init
  reFilter();
  updateBulk();
})();
</script>

<%@ include file="/WEB-INF/views/common/_admin_footer.jsp" %>
</body>
</html>
