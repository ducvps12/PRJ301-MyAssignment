<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@include file="/WEB-INF/views/common/_taglibs.jsp"%>

<style>
  /* ====== Design System: Premium Light/Dark ====== */
  :root{
    --bg:#f8fafc; --card:#ffffff; --ink:#0f172a; --muted:#64748b; --bd:#e2e8f0; --ink-inv:#ffffff;
    --pri:#2563eb; --pri-hover:#1d4ed8; --pri-2:#3b82f6; --ok:#10b981; --warn:#f59e0b; --no:#ef4444; --info:#06b6d4;
    --table:#f1f5f9; --shadow:0 4px 6px -1px rgba(0,0,0,.1), 0 2px 4px -1px rgba(0,0,0,.06);
    --shadow-lg:0 20px 25px -5px rgba(0,0,0,.1), 0 10px 10px -5px rgba(0,0,0,.04);
    --shadow-xl:0 25px 50px -12px rgba(0,0,0,.25);
    --chip:#eff6ff; --chip-bd:#bfdbfe; --ring:0 0 0 3px rgba(37,99,235,.1);
    --gradient-1:linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    --gradient-2:linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    --gradient-3:linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    --gradient-4:linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  }
  [data-theme="dark"]{
    --bg:#0f172a; --card:#1e293b; --ink:#f1f5f9; --muted:#94a3b8; --bd:#334155; --ink-inv:#0f172a;
    --pri:#3b82f6; --pri-hover:#2563eb; --pri-2:#60a5fa; --ok:#22c55e; --warn:#fbbf24; --no:#f87171; --info:#22d3ee;
    --table:#1e293b; --shadow:0 4px 6px -1px rgba(0,0,0,.3), 0 2px 4px -1px rgba(0,0,0,.2);
    --shadow-lg:0 20px 25px -5px rgba(0,0,0,.4), 0 10px 10px -5px rgba(0,0,0,.2);
    --shadow-xl:0 25px 50px -12px rgba(0,0,0,.5);
    --chip:#1e3a8a; --chip-bd:#3b82f6; --ring:0 0 0 3px rgba(59,130,246,.2);
  }

  *{box-sizing:border-box; margin:0; padding:0}
  body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif; line-height:1.6}

  /* ====== Layout & Container ====== */
  .wrap{max-width:1400px; margin:0 auto; padding:24px 20px; color:var(--ink); animation:fadeIn .4s ease-out}
  @keyframes fadeIn{from{opacity:0; transform:translateY(10px)} to{opacity:1; transform:translateY(0)}}

  /* ====== Header Toolbar ====== */
  .toolbar{display:flex; flex-wrap:wrap; gap:16px; align-items:center; justify-content:space-between; margin-bottom:24px; padding:20px 24px; background:var(--card); border:1px solid var(--bd); border-radius:16px; box-shadow:var(--shadow); position:sticky; top:0; z-index:100; backdrop-filter:blur(10px); background:rgba(255,255,255,.95)}
  [data-theme="dark"] .toolbar{background:rgba(30,41,59,.95)}
  .toolbar .left{display:flex; gap:16px; align-items:center; flex-wrap:wrap}
  .toolbar .right{display:flex; gap:12px; align-items:center; flex-wrap:wrap}
  .toolbar h1{margin:0; font-size:28px; font-weight:700; background:var(--gradient-1); -webkit-background-clip:text; -webkit-text-fill-color:transparent; background-clip:text}
  .pill{display:inline-flex; align-items:center; gap:6px; padding:6px 12px; border-radius:20px; background:var(--chip); border:1px solid var(--chip-bd); font-size:13px; font-weight:500; color:var(--ink); transition:all .2s ease}
  .pill:hover{transform:translateY(-1px); box-shadow:var(--shadow)}

  /* ====== Search & Inputs ====== */
  .input, select{padding:10px 14px; border:2px solid var(--bd); border-radius:12px; background:var(--card); color:var(--ink); font-size:14px; transition:all .2s ease; width:100%}
  .input:focus, select:focus{outline:none; border-color:var(--pri); box-shadow:var(--ring); transform:scale(1.01)}
  .input::placeholder{color:var(--muted)}
  .search-wrapper{position:relative; min-width:280px}
  .search-wrapper::before{content:'🔍'; position:absolute; left:12px; top:50%; transform:translateY(-50%); pointer-events:none; opacity:.5}
  .search-wrapper .input{padding-left:36px}

  /* ====== Buttons ====== */
  .btn{display:inline-flex; gap:8px; align-items:center; padding:10px 16px; border:2px solid transparent; border-radius:12px; background:var(--card); color:var(--ink); cursor:pointer; font-size:14px; font-weight:500; transition:all .2s ease; text-decoration:none; white-space:nowrap; position:relative; overflow:hidden}
  .btn::before{content:''; position:absolute; inset:0; background:currentColor; opacity:0; transition:opacity .2s}
  .btn:hover::before{opacity:.1}
  .btn:active{transform:scale(.98)}
  .btn.primary{background:var(--pri); color:var(--ink-inv); border-color:var(--pri); box-shadow:0 4px 12px rgba(37,99,235,.3)}
  .btn.primary:hover{background:var(--pri-hover); transform:translateY(-2px); box-shadow:0 6px 16px rgba(37,99,235,.4)}
  .btn.ghost{background:transparent; border-color:var(--bd)}
  .btn.ghost:hover{background:var(--table); border-color:var(--pri)}
  .btn.small{padding:6px 12px; font-size:12px}
  .btn.danger{background:var(--no); color:#fff; border-color:var(--no); box-shadow:0 4px 12px rgba(239,68,68,.3)}
  .btn.danger:hover{background:#dc2626; transform:translateY(-2px); box-shadow:0 6px 16px rgba(239,68,68,.4)}
  .btn.ok{background:var(--ok); color:#fff; border-color:var(--ok); box-shadow:0 4px 12px rgba(16,185,129,.3)}
  .btn.ok:hover{background:#059669; transform:translateY(-2px); box-shadow:0 6px 16px rgba(16,185,129,.4)}
  .btn.warn{background:var(--warn); color:#fff; border-color:var(--warn)}
  .btn.warn:hover{background:#d97706; transform:translateY(-2px)}
  .btn:disabled{opacity:.5; cursor:not-allowed; transform:none !important}

  /* ====== Cards ====== */
  .card{background:var(--card); border:1px solid var(--bd); border-radius:16px; padding:20px; box-shadow:var(--shadow); transition:all .3s ease; position:relative; overflow:hidden}
  .card::before{content:''; position:absolute; top:0; left:0; right:0; height:4px; background:var(--gradient-1); opacity:0; transition:opacity .3s}
  .card:hover{transform:translateY(-4px); box-shadow:var(--shadow-lg); border-color:var(--pri)}
  .card:hover::before{opacity:1}
  .card.stat{padding:24px; text-align:center}
  .card.stat h2{margin:.5rem 0; font-size:32px; font-weight:800; background:var(--gradient-1); -webkit-background-clip:text; -webkit-text-fill-color:transparent; background-clip:text}
  .card.stat .label{font-size:13px; color:var(--muted); text-transform:uppercase; letter-spacing:.05em; font-weight:600}

  /* ====== KPI Cards Grid ====== */
  .cards{display:grid; grid-template-columns:repeat(auto-fit, minmax(240px, 1fr)); gap:20px; margin:24px 0}
  .kpi-foot{display:flex; align-items:center; justify-content:space-between; margin-top:12px; font-size:12px; color:var(--muted)}
  .bar{height:6px; background:rgba(148,163,184,.2); border-radius:999px; overflow:hidden; margin-top:12px; position:relative}
  .bar>i{display:block; height:100%; background:var(--gradient-4); width:0%; border-radius:999px; transition:width 1s ease-out; position:relative}
  .bar>i::after{content:''; position:absolute; inset:0; background:linear-gradient(90deg, transparent, rgba(255,255,255,.3), transparent); animation:shimmer 2s infinite}
  @keyframes shimmer{0%{transform:translateX(-100%)} 100%{transform:translateX(100%)}}

  /* ====== Badges & Chips ====== */
  .badge{display:inline-flex; align-items:center; gap:4px; padding:4px 10px; border-radius:12px; font-size:12px; font-weight:600; border:1px solid; transition:all .2s}
  .badge.ok{background:rgba(16,185,129,.15); border-color:rgba(16,185,129,.3); color:var(--ok)}
  .badge.warn{background:rgba(245,158,11,.15); border-color:rgba(245,158,11,.3); color:var(--warn)}
  .badge.no{background:rgba(239,68,68,.15); border-color:rgba(239,68,68,.3); color:var(--no)}
  .badge.info{background:rgba(6,182,212,.15); border-color:rgba(6,182,212,.3); color:var(--info)}
  .chip{display:inline-flex; align-items:center; gap:6px; padding:4px 10px; border-radius:20px; border:1px dashed var(--bd); font-size:12px; background:var(--chip)}

  /* ====== Tables ====== */
  .table-wrap{background:var(--card); border:1px solid var(--bd); border-radius:16px; overflow:hidden; box-shadow:var(--shadow)}
  table{width:100%; border-collapse:collapse; min-width:800px}
  thead{background:var(--table); position:sticky; top:0; z-index:10}
  th{padding:14px 16px; text-align:left; font-size:13px; font-weight:600; color:var(--muted); text-transform:uppercase; letter-spacing:.05em; border-bottom:2px solid var(--bd)}
  td{padding:14px 16px; border-bottom:1px solid var(--bd); font-size:14px; transition:background .2s}
  tbody tr{transition:all .2s ease; cursor:pointer}
  tbody tr:hover{background:rgba(37,99,235,.05); transform:scale(1.01)}
  tbody tr.selected{background:rgba(37,99,235,.1); border-left:4px solid var(--pri)}
  tbody tr:last-child td{border-bottom:none}
  .nowrap{white-space:nowrap}
  .empty{padding:48px 24px; text-align:center; color:var(--muted)}
  .empty::before{content:'📋'; display:block; font-size:48px; margin-bottom:12px; opacity:.5}
  .empty p{margin-top:8px; font-size:14px}

  /* ====== Grid Layout ====== */
  .grid-2{display:grid; grid-template-columns:repeat(2, 1fr); gap:24px; margin:24px 0}
  @media (max-width:1200px){.grid-2{grid-template-columns:1fr}}
  .section{margin-top:24px}

  /* ====== Filter Form ====== */
  .filter-form{display:flex; flex-wrap:wrap; gap:12px; align-items:flex-end; padding:20px; background:var(--card); border:1px solid var(--bd); border-radius:16px; box-shadow:var(--shadow); margin-bottom:24px}
  .filter-form label{display:flex; flex-direction:column; gap:6px; font-size:13px; font-weight:500; color:var(--muted)}
  .filter-form .input{min-width:160px}

  /* ====== Pagination ====== */
  .bar-bottom{display:flex; align-items:center; justify-content:space-between; padding:16px 20px; background:var(--table); border-top:1px solid var(--bd); flex-wrap:wrap; gap:12px}
  .bulk{display:flex; gap:12px; align-items:center; flex-wrap:wrap}
  .pagination{display:flex; gap:8px; align-items:center}
  .pagination .btn.small{min-width:40px; justify-content:center}

  /* ====== Modal ====== */
  .modal{position:fixed; inset:0; background:rgba(0,0,0,.6); backdrop-filter:blur(4px); display:none; align-items:center; justify-content:center; padding:20px; z-index:9999; animation:fadeIn .2s}
  .modal.show{display:flex}
  .modal .dialog{max-width:560px; width:100%; background:var(--card); border:1px solid var(--bd); border-radius:20px; box-shadow:var(--shadow-xl); padding:28px; animation:slideUp .3s ease-out; position:relative}
  @keyframes slideUp{from{opacity:0; transform:translateY(20px)} to{opacity:1; transform:translateY(0)}}
  .modal .dialog h3{margin:0 0 12px; font-size:20px; font-weight:700}
  .modal .dialog p{margin:0 0 16px; color:var(--muted); line-height:1.6}
  .modal textarea{width:100%; min-height:120px; padding:12px; border:2px solid var(--bd); border-radius:12px; background:var(--card); color:var(--ink); font-family:inherit; font-size:14px; resize:vertical; transition:all .2s}
  .modal textarea:focus{outline:none; border-color:var(--pri); box-shadow:var(--ring)}
  .modal .actions{display:flex; gap:10px; justify-content:flex-end; margin-top:20px}

  /* ====== Toast Notifications ====== */
  .toast{position:fixed; right:20px; bottom:20px; display:flex; flex-direction:column; gap:12px; z-index:10000; max-width:400px}
  .toast .t{background:var(--card); border:1px solid var(--bd); box-shadow:var(--shadow-xl); border-left:4px solid var(--ok); padding:16px 20px; border-radius:12px; animation:slideInRight .3s ease-out; display:flex; align-items:center; gap:12px; min-width:300px}
  .toast .t.error{border-left-color:var(--no)}
  .toast .t.warn{border-left-color:var(--warn)}
  .toast .t.info{border-left-color:var(--info)}
  @keyframes slideInRight{from{opacity:0; transform:translateX(100%)} to{opacity:1; transform:translateX(0)}}

  /* ====== Loading Skeleton ====== */
  .sk{background:linear-gradient(90deg, rgba(148,163,184,.1), rgba(148,163,184,.2), rgba(148,163,184,.1)); background-size:200% 100%; animation:shimmer 1.5s ease infinite; border-radius:8px; height:20px}
  @keyframes shimmer{0%{background-position:200% 0} 100%{background-position:-200% 0}}
  .skeleton-row{display:flex; gap:12px; padding:12px}
  .skeleton-row .sk{flex:1}

  /* ====== Tooltips ====== */
  .tooltip{position:relative; cursor:help}
  .tooltip::after{content:attr(data-tooltip); position:absolute; bottom:100%; left:50%; transform:translateX(-50%) translateY(-8px); padding:6px 10px; background:#1f2937; color:#fff; border-radius:6px; font-size:12px; white-space:nowrap; opacity:0; pointer-events:none; transition:opacity .2s, transform .2s; z-index:1000}
  .tooltip:hover::after{opacity:1; transform:translateX(-50%) translateY(-4px)}

  /* ====== Confetti Animation ====== */
  @keyframes confetti{0%{transform:translateY(0) rotate(0deg); opacity:1} 100%{transform:translateY(500px) rotate(720deg); opacity:0}}
  .confetti{position:fixed; width:10px; height:10px; background:var(--ok); animation:confetti 2s ease-out forwards; pointer-events:none; z-index:10001}

  /* ====== Responsive ====== */
  @media (max-width:768px){
    .wrap{padding:16px}
    .toolbar{padding:16px; flex-direction:column; align-items:stretch}
    .toolbar .left, .toolbar .right{justify-content:center}
    .cards{grid-template-columns:1fr}
    .search-wrapper{min-width:100%}
    table{font-size:12px}
    th,td{padding:10px 8px}
  }

  /* ====== Accessibility ====== */
  .sr-only{position:absolute; left:-9999px; width:1px; height:1px; overflow:hidden}
  :focus-visible{outline:2px solid var(--pri); outline-offset:2px; border-radius:4px}
  @media (prefers-reduced-motion: reduce){*, *::before, *::after{animation-duration:.01ms !important; transition-duration:.01ms !important}}
</style>

<div class="wrap">
  <!-- Header Toolbar -->
  <div class="toolbar">
    <div class="left">
      <h1>📊 Division Dashboard</h1>
      <span class="pill">🏢 Phòng: <b>${empty dept ? 'N/A' : dept}</b></span>
      <span class="pill">👤 <b>${sessionScope.currentUser != null ? sessionScope.currentUser.fullName : 'Guest'}</b></span>
    </div>
    <div class="right">
      <div class="search-wrapper">
        <input id="q" class="input" type="search" placeholder="Tìm nhanh: tên / lý do / loại (phím /)" aria-label="Tìm nhanh" />
      </div>
      <button id="themeBtn" class="btn tooltip" type="button" title="Đổi theme" data-tooltip="Nhấn T để đổi theme">🌓</button>
      <button id="refreshBtn" class="btn tooltip" type="button" title="Làm mới" data-tooltip="Nhấn R để refresh">⟳</button>
      <button id="csvBtn" class="btn tooltip" type="button" title="Xuất CSV" data-tooltip="Nhấn E để export">⬇️ CSV</button>
      <button id="excelBtn" class="btn primary small tooltip" type="button" title="Xuất Excel" data-tooltip="Xuất Excel với format đẹp">📊 Excel</button>
    </div>
  </div>

  <!-- Filter Form -->
  <form method="get" class="filter-form" action="${pageContext.request.contextPath}/admin/div" autocomplete="off" id="filterForm">
    <c:if test="${canSwitchDept}">
      <label>
        Phòng ban
        <input class="input" type="text" name="dept" value="${fn:escapeXml(dept)}" placeholder="VD: SALE, IT">
      </label>
    </c:if>
    <label>
      Từ ngày
      <input class="input" type="date" name="from" value="${from}">
    </label>
    <label>
      Đến ngày
      <input class="input" type="date" name="to" value="${to}">
    </label>
    <button class="btn primary" type="submit">🔍 Lọc</button>
    <a class="btn ghost" href="${pageContext.request.contextPath}/admin/div">🔄 Reset</a>
    <span class="muted" style="align-self:center; font-size:12px">💡 Tip: Dùng hộp tìm nhanh để lọc tức thời</span>
  </form>

  <!-- KPI Cards -->
  <c:if test="${not empty stats}">
    <c:set var="den" value="${stats.approvalDenominator}" />
    <c:set var="num" value="${stats.approvalNumerator}" />
    <c:set var="rate" value="${den == 0 ? 0 : (num * 100.0 / den)}"/>
    <div class="cards">
      <div class="card stat" style="background:linear-gradient(135deg, rgba(102,126,234,.1), rgba(118,75,162,.1))">
        <div class="label">👥 Headcount</div>
        <h2 id="statHeadcount"><fmt:formatNumber value="${stats.headcount}" groupingUsed="true"/></h2>
        <div class="kpi-foot">
          <span class="muted">Nhân sự active</span>
          <span class="chip">Dept <b>${dept}</b></span>
        </div>
      </div>
      <div class="card stat" style="background:linear-gradient(135deg, rgba(245,158,11,.1), rgba(251,146,60,.1))">
        <div class="label">⏳ Đơn chờ duyệt</div>
        <h2 id="statPending"><fmt:formatNumber value="${stats.pendingCount}" /></h2>
        <div class="bar"><i id="barPending" style="width:${stats.pendingCount > 0 ? Math.min(stats.pendingCount * 10, 100) : 5}%"></i></div>
        <div class="kpi-foot">
          <span class="muted">Cần xử lý</span>
          <span class="badge warn">Pending</span>
        </div>
      </div>
      <div class="card stat" style="background:linear-gradient(135deg, rgba(16,185,129,.1), rgba(5,150,105,.1))">
        <div class="label">✅ Đã duyệt tháng này</div>
        <h2 id="statApproved"><fmt:formatNumber value="${stats.approvedThisMonth}" /></h2>
        <div class="bar"><i id="barApproved" style="width:${stats.approvedThisMonth > 0 ? Math.min(stats.approvedThisMonth * 5, 100) : 8}%"></i></div>
        <div class="kpi-foot">
          <span class="muted">Theo approved_at</span>
          <span class="badge ok">Approved</span>
        </div>
      </div>
      <div class="card stat" style="background:linear-gradient(135deg, rgba(6,182,212,.1), rgba(14,165,233,.1))">
        <div class="label">📈 Tỉ lệ duyệt</div>
        <h2 id="statRate"><fmt:formatNumber value="${rate}" maxFractionDigits="1"/>%</h2>
        <div class="bar"><i id="barRate" style="width:${rate}%"></i></div>
        <div class="kpi-foot">
          <span class="muted">${num}/${den}</span>
          <span class="badge info">SLA</span>
        </div>
      </div>
    </div>
  </c:if>

  <!-- Two Column Tables -->
  <div class="grid-2">
    <!-- Pending Requests Table -->
    <div class="card" style="padding:0; overflow:hidden">
      <div style="display:flex; align-items:center; justify-content:space-between; padding:20px 24px; border-bottom:2px solid var(--bd); background:var(--table)">
        <h3 style="margin:0; font-size:18px; font-weight:700">⏳ Đơn chờ duyệt</h3>
        <div style="display:flex; gap:10px; align-items:center; flex-wrap:wrap">
          <label class="chip" style="margin:0">
            Hiển thị
            <select id="pp" aria-label="Số dòng mỗi trang" style="padding:4px 8px; border:1px solid var(--bd); border-radius:8px; background:var(--card); margin-left:6px">
              <option>5</option><option selected>10</option><option>20</option><option>50</option><option>100</option>
            </select>
          </label>
          <button id="bulkApproveBtn" class="btn ok small tooltip" type="button" title="Duyệt hàng loạt (A)" data-tooltip="Nhấn A để duyệt nhanh">✅ Duyệt</button>
          <button id="bulkRejectBtn" class="btn danger small tooltip" type="button" title="Từ chối hàng loạt (R)" data-tooltip="Nhấn R để từ chối">❌ Từ chối</button>
        </div>
      </div>

      <div class="table-wrap">
        <table id="tblPending" aria-label="Danh sách đơn chờ duyệt">
          <thead>
            <tr>
              <th style="width:40px"><input type="checkbox" id="chkAll" aria-label="Chọn tất cả"></th>
              <th class="nowrap">#</th>
              <th>👤 Nhân sự</th>
              <th>📋 Loại</th>
              <th class="nowrap">📅 Từ</th>
              <th class="nowrap">📅 Đến</th>
              <th>💬 Lý do</th>
              <th class="nowrap">⚡ Thao tác</th>
            </tr>
          </thead>
          <tbody id="pendingBody">
            <c:forEach var="r" items="${pending}" varStatus="vs">
              <tr data-id="${r.id}" data-name="${fn:escapeXml(r.fullName)}" data-type="${fn:escapeXml(r.type)}" data-reason="${fn:escapeXml(r.reason)}" class="row-pending">
                <td><input type="checkbox" class="rowChk" aria-label="Chọn đơn ${r.id}"></td>
                <td class="nowrap"><strong>${vs.index + 1}</strong></td>
                <td><strong>${r.fullName}</strong></td>
                <td><span class="badge warn">${r.type}</span></td>
                <td class="nowrap">
                  <c:choose>
                    <c:when test="${not empty r.from}"><fmt:formatDate value="${r.from}" pattern="dd/MM/yyyy"/></c:when>
                    <c:otherwise>—</c:otherwise>
                  </c:choose>
                </td>
                <td class="nowrap">
                  <c:choose>
                    <c:when test="${not empty r.to}"><fmt:formatDate value="${r.to}" pattern="dd/MM/yyyy"/></c:when>
                    <c:otherwise>—</c:otherwise>
                  </c:choose>
                </td>
                <td class="muted" style="max-width:280px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap" title="${fn:escapeXml(r.reason)}">${r.reason}</td>
                <td class="nowrap" style="display:flex; gap:6px">
                  <a class="btn small ghost tooltip" href="${pageContext.request.contextPath}/request/detail?id=${r.id}" data-tooltip="Xem chi tiết">👁️</a>
                  <button class="btn small ok act-single tooltip" data-action="approve" data-id="${r.id}" type="button" data-tooltip="Duyệt đơn">✅</button>
                  <button class="btn small danger act-single tooltip" data-action="reject" data-id="${r.id}" type="button" data-tooltip="Từ chối">❌</button>
                </td>
              </tr>
            </c:forEach>
            <c:if test="${empty pending}">
              <tr><td colspan="8" class="empty"><div>📋</div><p>Không có đơn chờ duyệt</p></td></tr>
            </c:if>
          </tbody>
        </table>
      </div>

      <div class="bar-bottom">
        <div class="bulk">
          <span class="muted">Đã chọn: <b id="selCount" style="color:var(--pri)">0</b></span>
          <span id="selHint" class="muted hidden">💡 Nhấn <kbd style="padding:2px 6px; background:var(--table); border-radius:4px; font-size:11px">A</kbd> để duyệt nhanh</span>
        </div>
        <div class="pagination">
          <button id="prev" class="btn small" type="button" disabled>◀ Trước</button>
          <span class="muted" style="padding:0 12px">Trang <b id="page" style="color:var(--pri)">1</b>/<b id="pages">1</b></span>
          <button id="next" class="btn small" type="button" disabled>Sau ▶</button>
        </div>
      </div>
    </div>

    <!-- Today Off Table -->
    <div class="card" style="padding:0; overflow:hidden">
      <div style="display:flex; align-items:center; justify-content:space-between; padding:20px 24px; border-bottom:2px solid var(--bd); background:var(--table)">
        <h3 style="margin:0; font-size:18px; font-weight:700">🏖️ Đang nghỉ hôm nay</h3>
        <span class="chip">📅 <b><fmt:formatDate value="<%= new java.util.Date() %>" pattern="dd/MM/yyyy"/></b></span>
      </div>
      <div class="table-wrap">
        <table aria-label="Danh sách đang nghỉ hôm nay">
          <thead>
            <tr>
              <th class="nowrap">#</th>
              <th>👤 Nhân sự</th>
              <th>📋 Loại</th>
              <th class="nowrap">📅 Từ</th>
              <th class="nowrap">📅 Đến</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="t" items="${todayOff}" varStatus="vs">
              <tr>
                <td class="nowrap"><strong>${vs.index + 1}</strong></td>
                <td><strong>${t.fullName}</strong></td>
                <td><span class="badge ok">${t.type}</span></td>
                <td class="nowrap">
                  <c:choose>
                    <c:when test="${not empty t.from}"><fmt:formatDate value="${t.from}" pattern="dd/MM/yyyy"/></c:when>
                    <c:otherwise>—</c:otherwise>
                  </c:choose>
                </td>
                <td class="nowrap">
                  <c:choose>
                    <c:when test="${not empty t.to}"><fmt:formatDate value="${t.to}" pattern="dd/MM/yyyy"/></c:when>
                    <c:otherwise>—</c:otherwise>
                  </c:choose>
                </td>
              </tr>
            </c:forEach>
            <c:if test="${empty todayOff}">
              <tr><td colspan="5" class="empty"><div>🎉</div><p>Không ai nghỉ hôm nay</p></td></tr>
            </c:if>
          </tbody>
        </table>
      </div>
    </div>
  </div>

  <!-- Modal -->
  <div id="modal" class="modal" role="dialog" aria-modal="true" aria-labelledby="mTitle">
    <form id="mForm" class="dialog" method="post" action="${pageContext.request.contextPath}/request/approve">
      <h3 id="mTitle">⚠️ Xác nhận</h3>
      <p id="mDesc" class="muted">Bạn có chắc muốn thực hiện thao tác này?</p>
      <label for="mNote" style="display:block; margin-bottom:8px; font-weight:500; color:var(--ink)">💬 Ghi chú (không bắt buộc):</label>
      <textarea id="mNote" name="note" placeholder="Nhập lý do hoặc ghi chú cho thao tác này..."></textarea>
      <input type="hidden" name="_csrf" value="${csrf}">
      <input type="hidden" name="action" id="mAction" value="">
      <input type="hidden" name="id" id="mId" value="">
      <div id="mIds"></div>
      <div class="actions">
        <button type="button" id="mCancel" class="btn ghost">❌ Hủy</button>
        <button id="mOk" class="btn primary" type="submit">✅ Xác nhận</button>
      </div>
    </form>
  </div>

  <!-- Toast Container -->
  <div class="toast" id="toast"></div>
</div>

<script>
(function(){
  'use strict';
  const $ = s => document.querySelector(s);
  const $$ = s => Array.from(document.querySelectorAll(s));
  const ctx = '${pageContext.request.contextPath}';

  // ====== Toast System ======
  function toast(msg, type='success', duration=3500){
    const t = document.createElement('div');
    t.className = 't ' + (type === 'error' ? 'error' : type === 'warn' ? 'warn' : type === 'info' ? 'info' : '');
    const icon = type === 'error' ? '❌' : type === 'warn' ? '⚠️' : type === 'info' ? 'ℹ️' : '✅';
    t.innerHTML = '<span style="font-size:20px">' + icon + '</span><span>' + msg + '</span>';
    $('#toast').appendChild(t);
    setTimeout(() => {
      t.style.animation = 'slideInRight .3s ease-out reverse';
      setTimeout(() => t.remove(), 300);
    }, duration);
  }

  // ====== Confetti Effect ======
  function confetti(count=50){
    const colors = ['#10b981', '#3b82f6', '#f59e0b', '#ef4444', '#8b5cf6'];
    for(let i=0; i<count; i++){
      const c = document.createElement('div');
      c.className = 'confetti';
      c.style.left = Math.random() * 100 + '%';
      c.style.background = colors[Math.floor(Math.random() * colors.length)];
      c.style.animationDelay = Math.random() * 0.5 + 's';
      c.style.width = c.style.height = (Math.random() * 10 + 5) + 'px';
      document.body.appendChild(c);
      setTimeout(() => c.remove(), 2000);
    }
  }

  // ====== Theme Toggle ======
  const themeBtn = $('#themeBtn');
  const keyTheme = 'lm.theme';
  const applyTheme = v => {
    document.documentElement.setAttribute('data-theme', v);
    localStorage.setItem(keyTheme, v);
  };
  (function initTheme(){
    const v = localStorage.getItem(keyTheme) || (matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light');
    applyTheme(v);
  })();
  themeBtn?.addEventListener('click', () => {
    const cur = document.documentElement.getAttribute('data-theme') === 'dark' ? 'dark' : 'light';
    const nxt = cur === 'dark' ? 'light' : 'dark';
    applyTheme(nxt);
    toast('Đã chuyển sang chế độ ' + (nxt === 'dark' ? 'tối' : 'sáng'), 'info');
  });

  // ====== Refresh Button ======
  $('#refreshBtn')?.addEventListener('click', () => {
    $('#refreshBtn').style.animation = 'spin 1s linear';
    setTimeout(() => location.reload(), 300);
  });

  // ====== Search with Debounce ======
  const q = $('#q');
  let searchTimeout;
  function matches(tr, term){
    term = term.trim().toLowerCase();
    if(!term) return true;
    const name = (tr.dataset.name || '').toLowerCase();
    const type = (tr.dataset.type || '').toLowerCase();
    const reason = (tr.dataset.reason || '').toLowerCase();
    return name.includes(term) || type.includes(term) || reason.includes(term);
  }
  function reFilter(){
    const term = q?.value || '';
    $$('#pendingBody > tr.row-pending').forEach(tr => {
      const match = matches(tr, term);
      tr.classList.toggle('filtered-out', !match);
      tr.style.display = match ? '' : 'none';
    });
    curPage = 1;
    renderPage();
  }
  q?.addEventListener('input', () => {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(reFilter, 300);
  });

  // ====== Pagination ======
  const ppSel = $('#pp'), pageEl = $('#page'), pagesEl = $('#pages');
  const prevBtn = $('#prev'), nextBtn = $('#next');
  let curPage = 1, perPage = parseInt(ppSel?.value || '10', 10) || 10;
  function renderPage(){
    const rows = $$('#pendingBody > tr.row-pending').filter(r => !r.classList.contains('filtered-out') && r.style.display !== 'none');
    const total = rows.length;
    const pages = Math.max(1, Math.ceil(total / perPage));
    if(curPage > pages) curPage = pages;
    const start = (curPage - 1) * perPage;
    const end = start + perPage;
    rows.forEach((r, i) => {
      r.style.display = (i >= start && i < end) ? '' : 'none';
    });
    if(pageEl) pageEl.textContent = String(curPage);
    if(pagesEl) pagesEl.textContent = String(pages);
    if(prevBtn) prevBtn.disabled = curPage <= 1;
    if(nextBtn) nextBtn.disabled = curPage >= pages;
  }
  ppSel?.addEventListener('change', () => {
    perPage = parseInt(ppSel.value, 10) || 10;
    renderPage();
  });
  prevBtn?.addEventListener('click', () => {
    if(curPage > 1){ curPage--; renderPage(); }
  });
  nextBtn?.addEventListener('click', () => {
    curPage++; renderPage();
  });

  // ====== Bulk Selection ======
  const chkAll = $('#chkAll'), selCount = $('#selCount'), selHint = $('#selHint');
  function updateBulk(){
    const chks = $$('.rowChk:checked');
    const count = chks.length;
    if(selCount) selCount.textContent = count;
    if(selHint) selHint.classList.toggle('hidden', count === 0);
    $$('#pendingBody > tr').forEach(tr => {
      tr.classList.toggle('selected', tr.querySelector('.rowChk')?.checked);
    });
  }
  chkAll?.addEventListener('change', () => {
    $$('.rowChk').forEach(c => c.checked = chkAll.checked);
    updateBulk();
  });
  $$('.rowChk').forEach(c => {
    c.addEventListener('change', () => {
      if(!c.checked && chkAll) chkAll.checked = false;
      updateBulk();
    });
  });

  // ====== Modal System ======
  const modal = $('#modal'), mTitle = $('#mTitle'), mDesc = $('#mDesc'), mForm = $('#mForm');
  const mAction = $('#mAction'), mId = $('#mId'), mIds = $('#mIds'), mCancel = $('#mCancel'), mNote = $('#mNote');
  function openModal(args){
    const {action, ids} = args;
    mAction.value = action;
    mIds.innerHTML = '';
    mId.value = '';
    if(ids.length === 1){
      mTitle.textContent = (action === 'approve' ? '✅ Duyệt' : '❌ Từ chối') + ' đơn #' + ids[0];
      mDesc.textContent = 'Bạn có thể thêm ghi chú trước khi xác nhận.';
      mId.value = ids[0];
    } else {
      mTitle.textContent = (action === 'approve' ? '✅ Duyệt' : '❌ Từ chối') + ' ' + ids.length + ' đơn đã chọn';
      mDesc.textContent = 'Hệ thống sẽ xử lý tất cả các đơn đã chọn.';
      ids.forEach(id => {
        const inp = document.createElement('input');
        inp.type = 'hidden';
        inp.name = 'ids';
        inp.value = id;
        mIds.appendChild(inp);
      });
    }
    mNote.value = '';
    modal.classList.add('show');
    setTimeout(() => mNote?.focus(), 100);
  }
  function closeModal(){
    modal.classList.remove('show');
  }
  mCancel?.addEventListener('click', closeModal);
  modal?.addEventListener('click', e => {
    if(e.target === modal) closeModal();
  });
  mForm?.addEventListener('submit', function(e){
    e.preventDefault();
    const formData = new FormData(this);
    const action = formData.get('action');
    const loading = toast('Đang xử lý...', 'info', 0);
    fetch(this.action, {method:'POST', body:formData})
      .then(r => r.json().catch(() => ({success:r.ok})))
      .then(data => {
        if(data.success){
          confetti(30);
          toast(action === 'approve' ? '✅ Đã duyệt thành công!' : '❌ Đã từ chối!', 'success');
          setTimeout(() => location.reload(), 1500);
        } else {
          toast('❌ Có lỗi xảy ra: ' + (data.message || 'Vui lòng thử lại'), 'error');
        }
      })
      .catch(err => {
        toast('❌ Lỗi kết nối: ' + err.message, 'error');
      })
      .finally(() => {
        if(loading) loading.remove();
        closeModal();
      });
  });

  // ====== Single Actions ======
  $$('.act-single').forEach(btn => {
    btn.addEventListener('click', () => {
      openModal({action: btn.dataset.action, ids: [btn.dataset.id]});
    });
  });

  // ====== Bulk Actions ======
  const bulkApproveBtn = $('#bulkApproveBtn'), bulkRejectBtn = $('#bulkRejectBtn');
  const getSelectedIds = () => $$('.rowChk:checked').map(c => c.closest('tr').dataset.id).filter(Boolean);
  bulkApproveBtn?.addEventListener('click', () => {
    const ids = getSelectedIds();
    if(ids.length === 0) return toast('⚠️ Chưa chọn đơn nào', 'warn');
    openModal({action: 'approve', ids});
  });
  bulkRejectBtn?.addEventListener('click', () => {
    const ids = getSelectedIds();
    if(ids.length === 0) return toast('⚠️ Chưa chọn đơn nào', 'warn');
    openModal({action: 'reject', ids});
  });

  // ====== Export CSV ======
  $('#csvBtn')?.addEventListener('click', () => {
    const rows = $$('#pendingBody > tr.row-pending').filter(r => r.style.display !== 'none' && !r.classList.contains('filtered-out'));
    if(rows.length === 0){ toast('⚠️ Không có dữ liệu để xuất', 'warn'); return; }
    const header = ['ID', 'Nhân sự', 'Loại', 'Từ', 'Đến', 'Lý do'];
    const data = rows.map(r => {
      const id = r.dataset.id;
      const tds = r.querySelectorAll('td');
      return [
        id,
        tds[2]?.innerText.trim() || '',
        tds[3]?.innerText.replace(/\s+/g, ' ').trim() || '',
        tds[4]?.innerText.trim() || '',
        tds[5]?.innerText.trim() || '',
        (tds[6]?.innerText || '').replace(/\s+/g, ' ').trim()
      ];
    });
    const csv = [header].concat(data).map(a => a.map(v => {
      v = (v ?? '').toString();
      if(v.includes('"') || v.includes(',') || v.includes('\n')) v = '"' + v.replace(/"/g, '""') + '"';
      return v;
    }).join(',')).join('\n');
    const blob = new Blob(['\ufeff' + csv], {type: 'text/csv;charset=utf-8;'});
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'pending_' + new Date().toISOString().slice(0,10) + '.csv';
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
    toast('✅ Đã tải CSV thành công!', 'success');
  });

  // ====== Export Excel (HTML Table to Excel) ======
  $('#excelBtn')?.addEventListener('click', () => {
    const rows = $$('#pendingBody > tr.row-pending').filter(r => r.style.display !== 'none' && !r.classList.contains('filtered-out'));
    if(rows.length === 0){ toast('⚠️ Không có dữ liệu để xuất', 'warn'); return; }
    let html = '<table><thead><tr><th>ID</th><th>Nhân sự</th><th>Loại</th><th>Từ</th><th>Đến</th><th>Lý do</th></tr></thead><tbody>';
    rows.forEach(r => {
      const tds = r.querySelectorAll('td');
      html += '<tr>'
        + '<td>' + r.dataset.id + '</td>'
        + '<td>' + (tds[2]?.innerText.trim() || '') + '</td>'
        + '<td>' + (tds[3]?.innerText.trim() || '') + '</td>'
        + '<td>' + (tds[4]?.innerText.trim() || '') + '</td>'
        + '<td>' + (tds[5]?.innerText.trim() || '') + '</td>'
        + '<td>' + ((tds[6]?.innerText || '').trim()) + '</td>'
        + '</tr>';
    });
    html += '</tbody></table>';
    const blob = new Blob(['\ufeff' + html], {type: 'application/vnd.ms-excel'});
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'pending_' + new Date().toISOString().slice(0,10) + '.xls';
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
    toast('✅ Đã tải Excel thành công!', 'success');
  });

  // ====== Keyboard Shortcuts ======
  document.addEventListener('keydown', e => {
    if(e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') return;
    if(e.key === '/'){
      e.preventDefault();
      q?.focus();
    } else if(e.key === 't' || e.key === 'T'){
      e.preventDefault();
      themeBtn?.click();
    } else if(e.key === 'r' || e.key === 'R'){
      if(e.ctrlKey || e.metaKey) return;
      e.preventDefault();
      $('#refreshBtn')?.click();
    } else if(e.key === 'e' || e.key === 'E'){
      e.preventDefault();
      $('#csvBtn')?.click();
    } else if((e.key === 'a' || e.key === 'A') && !e.ctrlKey && !e.metaKey){
      const ids = getSelectedIds();
      if(ids.length > 0){
        e.preventDefault();
        bulkApproveBtn?.click();
      }
    } else if((e.key === 'r' || e.key === 'R') && !e.ctrlKey && !e.metaKey){
      const ids = getSelectedIds();
      if(ids.length > 0 && e.shiftKey){
        e.preventDefault();
        bulkRejectBtn?.click();
      }
    } else if(e.key === 'Escape'){
      closeModal();
    }
  });

  // ====== Animate KPI Bars on Load ======
  setTimeout(() => {
    $$('.bar > i').forEach(bar => {
      const w = bar.style.width;
      bar.style.width = '0%';
      setTimeout(() => { bar.style.width = w; }, 100);
    });
  }, 300);

  // ====== Initialize ======
  reFilter();
  updateBulk();
  renderPage();

  // ====== Auto-refresh every 60s (optional) ======
  // setInterval(() => { if(document.visibilityState === 'visible') location.reload(); }, 60000);
})();
</script>
