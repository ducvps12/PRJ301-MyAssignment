<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="cp" value="${pageContext.request.contextPath}" />
<%-- ======= NULL-SAFE DEFAULTS (tránh 500 khi pg/scope/now rỗng) ======= --%>
<c:if test="${empty now}">
  <c:set var="now" value="<%= new java.util.Date() %>" />
</c:if>
<c:set var="hasPg"  value="${not empty pg}" />
<c:set var="page"   value="${hasPg ? pg.page  : 1}" />
<c:set var="size"   value="${hasPg ? pg.size  : 20}" />
<c:set var="total"  value="${hasPg ? pg.total : 0}" />
<c:set var="items"  value="${hasPg ? pg.items : null}" />
<c:set var="scopeSafe" value="${empty scope ? 'user' : scope}" />
<c:set var="userFilter" value="${requestScope.userFilter}" />

<!DOCTYPE html>
<html lang="vi" data-theme="light">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>Lịch sử hoạt động</title>
  <style>
    :root{
      --bg:#f7f7f8;--card:#fff;--muted:#6b7280;--b:#e5e7eb;--ink:#0f172a;--ink-2:#111827;--brand:#3b82f6;
      --ok:#16a34a;--warn:#f59e0b;--bad:#ef4444;--chip:#eef2ff;--chip-b:#c7d2fe;--hl:#fff7ed;
    }
    [data-theme="dark"]{
      --bg:#0b1220;--card:#0f172a;--muted:#9aa4b2;--b:#1f2a44;--ink:#e5e7eb;--ink-2:#f9fafb;--brand:#60a5fa;
      --ok:#22c55e;--warn:#fbbf24;--bad:#f87171;--chip:#0b1220;--chip-b:#334155;--hl:#111827;
    }
    *{box-sizing:border-box}
    html,body{height:100%}
    body{margin:0;background:var(--bg);color:var(--ink);font:15px/1.45 system-ui,Segoe UI,Roboto,Arial,sans-serif}
    a{color:inherit;text-decoration:none}
    .app{display:grid;grid-template-columns:260px 1fr;min-height:100vh}
    .sidebar{position:sticky;top:0;height:100vh;background:var(--card);border-right:1px solid var(--b)}
    .sb-head{display:flex;align-items:center;gap:10px;padding:16px;border-bottom:1px solid var(--b)}
    .logo{width:28px;height:28px;border-radius:8px;background:var(--brand)}
    .brand{font-weight:700}
    .nav{padding:10px}
    .nav a{display:flex;align-items:center;gap:10px;padding:10px 12px;border-radius:12px;border:1px solid transparent}
    .nav a:hover{background:rgba(59,130,246,.08);border-color:var(--b)}
    .nav a.active{background:rgba(59,130,246,.15);border-color:var(--chip-b)}
    .section-h{margin:10px 12px;color:var(--muted);font-size:12px;text-transform:uppercase;letter-spacing:.06em}
    .main{display:flex;flex-direction:column;min-width:0}
    .topbar{position:sticky;top:0;background:var(--card);border-bottom:1px solid var(--b);z-index:20}
    .topbar-inner{display:flex;align-items:center;gap:12px;padding:10px 16px}
    .search{flex:1;display:flex;align-items:center;gap:8px;border:1px solid var(--b);background:var(--bg);border-radius:12px;padding:8px 10px}
    .search input{flex:1;border:0;background:transparent;outline:none;color:var(--ink)}
    .tb-btn{border:1px solid var(--b);padding:8px 12px;border-radius:12px;background:var(--card)}
    .wrap{padding:18px;display:grid;grid-template-columns:1fr;gap:16px}
    .card{background:var(--card);border:1px solid var(--b);border-radius:16px;overflow:hidden}
    .toolbar{display:flex;flex-wrap:wrap;gap:10px;align-items:center;justify-content:space-between;padding:12px 14px;border-bottom:1px solid var(--b);position:sticky;top:49px;background:var(--card);z-index:10}
    .muted{color:var(--muted)}
    .chips{display:flex;align-items:center;gap:6px;flex-wrap:wrap}
    .chip{padding:6px 10px;border:1px solid var(--chip-b);background:var(--chip);border-radius:999px;font-size:12px}
    .filters{display:flex;gap:8px;align-items:center;flex-wrap:wrap}
    .input{padding:8px 10px;border:1px solid var(--b);border-radius:12px;background:var(--card);color:var(--ink)}
    .btn{border:1px solid var(--b);background:var(--card);padding:8px 12px;border-radius:12px;display:inline-flex;align-items:center;gap:8px}
    .btn[aria-disabled="true"]{opacity:.5;pointer-events:none}
    .btn.primary{background:var(--brand);border-color:transparent;color:white}
    .btn.good{border-color:transparent;background:var(--ok);color:white}
    .btn.ghost{background:transparent}
    table{width:100%;border-collapse:separate;border-spacing:0}
    thead th{position:sticky;top:97px;background:var(--card);border-bottom:1px solid var(--b);font-weight:600}
    th,td{padding:10px 12px;border-bottom:1px solid var(--b);text-align:left;vertical-align:top}
    tbody tr:hover{background:rgba(59,130,246,.05)}
    td code{font-family:ui-monospace,Menlo,Consolas,monospace}
    .row-actions{display:flex;gap:8px}
    .badge{display:inline-block;padding:2px 8px;border-radius:999px;border:1px solid var(--b);font-size:12px}
    .pager{display:flex;gap:8px;justify-content:flex-end;padding:12px}
    .dense th, .dense td{padding:6px 8px}
    .footer{margin:10px 0 20px;color:var(--muted);text-align:center}
    @media (max-width: 1000px){
      .app{grid-template-columns:1fr}
      .sidebar{position:relative;height:auto}
      .toolbar{top:49px}
      thead .hide-md{display:none}
    }
  </style>
</head>
<body>
<div class="app">
  <!-- SIDEBAR -->
  <aside class="sidebar">
    <div class="sb-head">
      <div class="logo"></div>
      <div>
        <div class="brand">LeaveMgmt</div>
        <div class="muted" style="font-size:12px">Audit & Reports</div>
      </div>
    </div>
    <div class="nav">
      <div class="section-h">Điều hướng</div>
      <a href="${cp}/dashboard"><span>🏠</span> Tổng quan</a>
      <a href="${cp}/requests"><span>📝</span> Đơn nghỉ</a>
      <a href="${cp}/agenda"><span>📅</span> Lịch nhân sự</a>
      <a href="${cp}/activity" class="active"><span>🧭</span> Lịch sử hoạt động</a>
      <c:if test="${scopeSafe == 'admin'}">
        <div class="section-h">Quản trị</div>
        <a href="${cp}/admin/users"><span>👥</span> Người dùng</a>
        <a href="${cp}/admin/activity"><span>📜</span> Nhật ký hệ thống</a>
      </c:if>
    </div>
  </aside>

  <!-- MAIN -->
  <main class="main">
    <!-- TOPBAR -->
    <div class="topbar">
      <div class="topbar-inner">
        <div class="search" title="Nhấn / để tìm nhanh">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M21 21l-4.35-4.35" stroke="currentColor" stroke-width="2" stroke-linecap="round"/><circle cx="10.5" cy="10.5" r="6.5" stroke="currentColor" stroke-width="2"/></svg>
          <input id="qClient" type="text" placeholder="Tìm nhanh (action, entity, note, IP, UA)" />
          <button id="clearQ" class="tb-btn" type="button" title="Xóa tìm nhanh">✖</button>
        </div>
        <button id="toggleTheme" class="tb-btn" type="button" title="Dark / Light (phím d)">🌓</button>
        <div class="tb-btn" title="Tài khoản">${sessionScope.currentUser != null ? sessionScope.currentUser.fullName : 'Guest'}</div>
      </div>
    </div>

    <div class="wrap">
      <h2 style="margin:4px 0 0">Lịch sử hoạt động</h2>
      <div class="chips">
        <span class="chip">Scope: <strong><c:out value="${scopeSafe == 'admin' ? 'Admin' : 'Của tôi'}"/></strong></span>
        <span class="chip">Tổng bản ghi: <strong><c:out value="${total}"/></strong></span>
        <c:if test="${scopeSafe == 'admin' && not empty userFilter}">
          <span class="chip">userId = <code><c:out value="${userFilter}"/></code></span>
        </c:if>
      </div>

      <div class="card" id="card">
        <div class="toolbar">
          <div class="muted">Bộ lọc & thao tác</div>
          <div class="row-actions">
            <button class="btn" id="btnExport" type="button">⬇️ Xuất CSV</button>
            <button class="btn" id="btnColumns" type="button">📐 Cột</button>
            <button class="btn" id="btnDensity" type="button">↕️ Độ dày</button>
            <button class="btn ghost" id="btnHelp" type="button">❓ Trợ giúp</button>
          </div>
        </div>

        <c:if test="${scopeSafe == 'admin'}">
          <form method="get" action="${cp}/admin/activity" class="filters" style="padding:12px 14px;border-bottom:1px solid var(--b)">
            <label class="muted">userId</label>
            <input class="input" type="number" name="userId" value="${param.userId}"/>

            <label class="muted">size</label>
            <input class="input" type="number" name="size" value="${size}"/>

            <label class="muted">action</label>
            <input class="input" type="text" name="action" value="${fn:escapeXml(param.action)}"/>

            <label class="muted">q</label>
            <input class="input" type="text" name="q" value="${fn:escapeXml(param.q)}"/>

            <label class="muted">from</label>
            <input class="input" type="date" name="from" value="${param.from}"/>

            <label class="muted">to</label>
            <input class="input" type="date" name="to" value="${param.to}"/>

            <button class="btn primary" type="submit">Lọc</button>
            <a class="btn" href="${cp}/admin/activity">Xóa lọc</a>
          </form>
        </c:if>

        <div style="overflow:auto">
          <table id="tbl" aria-describedby="audit table">
            <thead>
              <tr>
                <th data-k="id">#</th>
                <th data-k="time">Thời gian</th>
                <th data-k="uid">userId</th>
                <th data-k="act">Hành động</th>
                <th data-k="entity" class="hide-md">Đối tượng</th>
                <th data-k="note">Ghi chú</th>
                <th data-k="net">IP / UA</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach var="r" items="${items}">
                <tr>
                  <td data-k="id"><c:out value="${r.id}"/></td>
                  <td data-k="time"><fmt:formatDate value="${r.createdAt}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                  <td data-k="uid"><c:out value="${r.userId}"/></td>
                  <td data-k="act"><span class="badge"><c:out value="${r.action}"/></span></td>
                  <td data-k="entity" class="hide-md">
                    <strong><c:out value="${r.entityType}"/></strong>
                    <div class="muted">ID: <c:out value="${r.entityId}"/></div>
                  </td>
                  <td data-k="note" style="max-width:520px">
                    <div class="note-text"><c:out value="${r.note}"/></div>
                    <button type="button" class="btn ghost btn-expand" title="Xem toàn bộ">🔎</button>
                  </td>
                  <td data-k="net" style="max-width:420px">
                    <div><code class="copyable" title="Nhấn để copy IP"><c:out value="${r.ipAddr}"/></code></div>
                    <div class="muted"><c:out value="${r.userAgent}"/></div>
                  </td>
                </tr>
              </c:forEach>
              <c:if test="${empty items}">
                <tr><td colspan="7" class="muted" style="padding:14px">Không có bản ghi.</td></tr>
              </c:if>
            </tbody>
          </table>
        </div>

        <div class="pager">
          <c:set var="prev" value="${page - 1}" />
          <c:set var="next" value="${page + 1}" />

          <c:choose>
            <c:when test="${scopeSafe == 'admin'}">
              <c:url var="uFirst" value="/admin/activity">
                <c:param name="page" value="1"/>
                <c:param name="size" value="${size}"/>
                <c:param name="userId" value="${param.userId}"/>
                <c:param name="action" value="${param.action}"/>
                <c:param name="q" value="${param.q}"/>
                <c:param name="from" value="${param.from}"/>
                <c:param name="to" value="${param.to}"/>
              </c:url>
              <c:url var="uPrev" value="/admin/activity">
                <c:param name="page" value="${prev}"/>
                <c:param name="size" value="${size}"/>
                <c:param name="userId" value="${param.userId}"/>
                <c:param name="action" value="${param.action}"/>
                <c:param name="q" value="${param.q}"/>
                <c:param name="from" value="${param.from}"/>
                <c:param name="to" value="${param.to}"/>
              </c:url>
              <c:url var="uNext" value="/admin/activity">
                <c:param name="page" value="${next}"/>
                <c:param name="size" value="${size}"/>
                <c:param name="userId" value="${param.userId}"/>
                <c:param name="action" value="${param.action}"/>
                <c:param name="q" value="${param.q}"/>
                <c:param name="from" value="${param.from}"/>
                <c:param name="to" value="${param.to}"/>
              </c:url>

              <a class="btn" aria-disabled="${page==1}" href="${page==1 ? '#' : uFirst}">« Đầu</a>
              <a class="btn" aria-disabled="${page==1}" href="${page==1 ? '#' : uPrev}">‹ Trước</a>
              <span>Trang <strong><c:out value="${page}"/></strong></span>
              <a class="btn" aria-disabled="${page*size>=total}" href="${page*size>=total ? '#' : uNext}">Tiếp ›</a>
            </c:when>

            <c:otherwise>
              <c:url var="uFirstU" value="/activity">
                <c:param name="page" value="1"/>
                <c:param name="size" value="${size}"/>
              </c:url>
              <c:url var="uPrevU" value="/activity">
                <c:param name="page" value="${prev}"/>
                <c:param name="size" value="${size}"/>
              </c:url>
              <c:url var="uNextU" value="/activity">
                <c:param name="page" value="${next}"/>
                <c:param name="size" value="${size}"/>
              </c:url>

              <a class="btn" aria-disabled="${page==1}" href="${page==1 ? '#' : uFirstU}">« Đầu</a>
              <a class="btn" aria-disabled="${page==1}" href="${page==1 ? '#' : uPrevU}">‹ Trước</a>
              <span>Trang <strong><c:out value="${page}"/></strong></span>
              <a class="btn" aria-disabled="${page*size>=total}" href="${page*size>=total ? '#' : uNextU}">Tiếp ›</a>
            </c:otherwise>
          </c:choose>
        </div>
      </div>

      <div class="footer">© <fmt:formatDate value="${now}" pattern="yyyy"/> LeaveMgmt • Made with ❤ for productivity</div>
    </div>
  </main>
</div>

<script>
  // Theme toggle with persistence
  (function(){
    const k='lgmt.theme';
    const saved=localStorage.getItem(k);
    if(saved){ document.documentElement.setAttribute('data-theme', saved); }
    const btn=document.getElementById('toggleTheme');
    btn.addEventListener('click',()=>{
      const cur=document.documentElement.getAttribute('data-theme')==='dark'?'light':'dark';
      document.documentElement.setAttribute('data-theme',cur);localStorage.setItem(k,cur);
    });
  })();

  // Client quick search (in-page filter)
  const q = document.getElementById('qClient');
  const clearQ = document.getElementById('clearQ');
  const tbl = document.getElementById('tbl');
  const rows = () => Array.from(tbl.tBodies[0].rows);
  function filterClient(){
    const needle = (q.value||'').toLowerCase().trim();
    rows().forEach(tr=>{
      const text = tr.innerText.toLowerCase();
      tr.style.display = text.includes(needle) ? '' : 'none';
    });
  }
  q.addEventListener('input', debounce(filterClient, 150));
  clearQ.addEventListener('click', ()=>{ q.value=''; filterClient(); q.focus(); });

  // Column sort
  Array.from(tbl.tHead.rows[0].cells).forEach((th,i)=>{
    th.style.cursor='pointer';
    th.title='Sắp xếp theo cột';
    th.addEventListener('click', ()=> sortBy(i));
  });
  let sortState = {idx:0, asc:true};
  function sortBy(idx){
    const bodyRows = rows().filter(r=>r.style.display!== 'none');
    const asc = sortState.idx===idx ? !sortState.asc : true;
    sortState={idx,asc};
    bodyRows.sort((a,b)=>{
      const A=a.cells[idx].innerText.trim();
      const B=b.cells[idx].innerText.trim();
      const nA=parseFloat(A.replace(/[^0-9.-]/g,''));
      const nB=parseFloat(B.replace(/[^0-9.-]/g,''));
      const bothNum=!isNaN(nA)&&!isNaN(nB);
      return (bothNum? nA-nB : A.localeCompare(B, 'vi', {numeric:true}))*(asc?1:-1);
    }).forEach(r=>tbl.tBodies[0].appendChild(r));
  }

  // Copy IP on click + expand note
  tbl.addEventListener('click', (e)=>{
    const t=e.target;
    if(t.classList.contains('copyable')){
      navigator.clipboard.writeText(t.innerText.trim());
      t.title='Đã copy!'; setTimeout(()=>t.title='Nhấn để copy IP',1200);
    }
    if(t.classList.contains('btn-expand')){
      const note = t.closest('td').querySelector('.note-text');
      if(!note) return;
      note.style.whiteSpace = note.style.whiteSpace==='normal' ? 'nowrap' : 'normal';
    }
  });

  // CSV Export (visible rows)
  document.getElementById('btnExport').addEventListener('click', ()=>{
    const visible = rows().filter(r=>r.style.display!== 'none');
    const headers = Array.from(tbl.tHead.rows[0].cells).map(th=>th.innerText.trim());
    const csv = [toCsv(headers)]
      .concat(visible.map(r=>toCsv(Array.from(r.cells).map(td=>td.innerText.replace(/\s+/g,' ').trim()))))
      .join('\n');
    const blob=new Blob([csv],{type:'text/csv;charset=utf-8;'});
    const a=document.createElement('a');a.href=URL.createObjectURL(blob);a.download='user-activity.csv';a.click();
  });
  function toCsv(arr){return arr.map(v=>'"'+(v||'').replaceAll('"','""')+'"').join(',');}

  // Columns toggle
  (function(){
    const btn=document.getElementById('btnColumns');
    const cols=Array.from(tbl.tHead.rows[0].cells).map((th,idx)=>({idx,label:th.innerText}));
    let open=false;
    const pop=document.createElement('div');
    pop.style.position='absolute';pop.style.right='16px';pop.style.top='120px';pop.style.background='var(--card)';
    pop.style.border='1px solid var(--b)';pop.style.borderRadius='12px';pop.style.padding='10px';
    pop.style.boxShadow='0 10px 30px rgba(0,0,0,.12)';
    pop.innerHTML = '<div style="font-weight:600;margin-bottom:6px">Ẩn/hiện cột</div>' +
      cols.map(c=>`<label style="display:flex;gap:8px;align-items:center;margin:6px 0"><input type="checkbox" data-idx="${c.idx}" checked />${c.label}</label>`).join('');
    document.body.appendChild(pop); pop.hidden=true;
    btn.addEventListener('click',()=>{open=!open;pop.hidden=!open});
    pop.addEventListener('change', (e)=>{
      const idx=+e.target.getAttribute('data-idx');
      const show=e.target.checked;
      tbl.tHead.rows[0].cells[idx].style.display= show? '' : 'none';
      rows().forEach(r=>{ r.cells[idx].style.display = show? '' : 'none'; });
    });
  })();

  // Density toggle
  document.getElementById('btnDensity').addEventListener('click', ()=>{
    tbl.classList.toggle('dense');
  });

  // Keyboard shortcuts
  let gPressed=false;
  window.addEventListener('keydown', (e)=>{
    if(e.key === '/' && e.target.tagName !== 'INPUT' && e.target.tagName !== 'TEXTAREA'){ e.preventDefault(); q.focus(); }
    if(e.key.toLowerCase()==='d'){ document.getElementById('toggleTheme').click(); }
    if(e.key==='g') gPressed=true;
    else if(gPressed && e.key==='n'){ gotoPage('next'); gPressed=false; }
    else if(gPressed && e.key==='p'){ gotoPage('prev'); gPressed=false; }
  });
  window.addEventListener('keyup', (e)=>{ if(e.key==='g') gPressed=false; });

  function gotoPage(dir){
    const linkSel = dir==='next'? '.pager a:last-child' : '.pager a:nth-child(2)';
    const a=document.querySelector(linkSel);
    if(a && a.getAttribute('aria-disabled')!=='true' && a.getAttribute('href') && a.getAttribute('href') !== '#'){
      location.href=a.getAttribute('href');
    }
  }

  // Debounce helper
  function debounce(fn,ms){ let t; return (...args)=>{ clearTimeout(t); t=setTimeout(()=>fn.apply(this,args),ms); }; }

  // Help
  document.getElementById('btnHelp').addEventListener('click', ()=>{
    alert('Phím tắt:\n / : Tìm nhanh  •  d : Dark/Light  •  g p : Trang trước  •  g n : Trang sau\n\nTính năng: Lọc nhanh, sắp xếp cột, ẩn/hiện cột, xuất CSV, copy IP.');
  });
</script>
</body>
</html>
