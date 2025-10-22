<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp" %>
<!DOCTYPE html>
<html lang="vi" data-theme="${sessionScope.theme != null ? sessionScope.theme : 'light'}">
<head>
  <meta charset="UTF-8">
  <title>Danh sách đơn nghỉ phép</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <meta name="color-scheme" content="light dark">
  <!-- CSS đã tách riêng -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/list.css?v=1">
</head>
<body>

<%@ include file="/WEB-INF/views/common/_header.jsp" %>

<div class="wrap">
  <!-- KPI nhỏ cho quản lý -->
  <c:if test="${sessionScope.role == 'MANAGER'}">
    <div class="kpi" style="margin-bottom:12px">
      <div class="box">📝 Đơn chờ duyệt: <b>${stats.pendingCount}</b></div>
      <div class="box">✅ Đã duyệt tháng này: <b>${stats.approvedThisMonth}</b></div>
      <div class="box">⛳ Tỉ lệ duyệt: <b>${stats.approvalRate}%</b></div>
    </div>
  </c:if>

  <div class="toolbar">
    <a class="btn btn-primary" href="${pageContext.request.contextPath}/request/create">+ Tạo đơn</a>
    <a class="btn" href="${pageContext.request.contextPath}/request/agenda">📅 Agenda</a>
    <button class="btn" id="btnExportCsv" type="button" title="Xuất CSV">⇩ Xuất CSV</button>
    <a class="btn btn-ghost" href="${pageContext.request.contextPath}/request/list" id="btnRefresh" title="Làm mới (R)">⟲ Làm mới</a>

    <!-- Quick filter chips -->
    <div class="chips">
      <span class="chip" data-quick="week">Tuần này</span>
      <span class="chip" data-quick="month">Tháng này</span>
      <span class="chip" data-quick="pending">Đang chờ</span>
      <span class="chip" data-quick="approved">Đã duyệt</span>
      <span class="chip" data-quick="mine">Của tôi</span>
      <span class="chip" data-quick="clear">Xóa lọc</span>
    </div>

    <!-- Lọc nâng cao -->
    <form id="filterForm" method="get" action="${pageContext.request.contextPath}/request/list" class="right" style="display:flex;gap:8px;align-items:center;margin-left:auto">
      <label class="sr" for="from">Từ ngày</label>
      <input type="date" id="from" name="from" value="${param.from}">
      <label class="sr" for="to">Đến ngày</label>
      <input type="date" id="to" name="to" value="${param.to}">
      <select name="status" aria-label="Trạng thái" id="statusSel">
        <option value="">Trạng thái</option>
        <option value="PENDING"   ${param.status == 'PENDING'   ? 'selected':''}>Chờ duyệt</option>
        <option value="APPROVED"  ${param.status == 'APPROVED'  ? 'selected':''}>Đã duyệt</option>
        <option value="REJECTED"  ${param.status == 'REJECTED'  ? 'selected':''}>Từ chối</option>
        <option value="CANCELLED" ${param.status == 'CANCELLED' ? 'selected':''}>Đã hủy</option>
      </select>
      <select name="mine" aria-label="Phạm vi" id="mineSel">
        <option value="">Của mọi người</option>
        <option value="1" ${param.mine == '1' ? 'selected' : ''}>Chỉ của tôi</option>
        <c:if test="${sessionScope.role == 'MANAGER'}">
          <option value="team" ${param.mine == 'team' ? 'selected' : ''}>Cấp dưới của tôi</option>
        </c:if>
      </select>
      <input type="text" id="q" name="q" value="${fn:escapeXml(param.q)}" placeholder="Tìm lý do, người tạo...">
      <select name="sort" aria-label="Sắp xếp">
        <option value="">Sắp xếp</option>
        <option value="created_desc" ${param.sort=='created_desc'?'selected':''}>Mới nhất</option>
        <option value="created_asc"  ${param.sort=='created_asc'?'selected':''}>Cũ nhất</option>
        <option value="from_asc"     ${param.sort=='from_asc'?'selected':''}>Ngày bắt đầu ↑</option>
        <option value="from_desc"    ${param.sort=='from_desc'?'selected':''}>Ngày bắt đầu ↓</option>
      </select>
      <button class="btn" type="submit">Lọc</button>
    </form>
  </div>

  <c:if test="${not empty requestScope.message}">
    <div class="msg ok">${requestScope.message}</div>
  </c:if>
  <c:if test="${not empty requestScope.error}">
    <div class="msg no">${requestScope.error}</div>
  </c:if>

  <div class="card">
    <div class="card-head">
      <div class="muted">Tổng: <b>${totalItems}</b> đơn • Trang <b>${page}</b>/<b>${totalPages}</b></div>
      <c:if test="${sessionScope.role == 'MANAGER'}">
        <form id="bulkForm" method="post" action="${pageContext.request.contextPath}/request/bulk" style="display:flex;gap:8px;align-items:center">
          <input type="hidden" name="csrf" value="${sessionScope.csrfToken}">
          <span class="muted">Chọn: <b id="selCount">0</b></span>
          <select name="action" id="bulkAction">
            <option value="">Bulk action…</option>
            <option value="approve">Duyệt</option>
            <option value="reject">Từ chối</option>
            <option value="cancel">Hủy</option>
          </select>
          <input name="note" placeholder="Ghi chú (tuỳ chọn)" />
          <button class="btn" type="submit" id="bulkSubmit" disabled>Thực hiện</button>
        </form>
      </c:if>
    </div>

    <!-- cuộn ngang mượt trên mobile -->
    <div class="table-scroll">
      <table id="reqTable" aria-describedby="tableDesc">
        <caption id="tableDesc" class="sr">Danh sách đơn nghỉ phép</caption>
        <thead>
          <tr>
            <th class="row-select">
              <c:if test="${sessionScope.role == 'MANAGER'}">
                <input type="checkbox" id="chkAll" title="Chọn tất cả (A)">
              </c:if>
            </th>
            <th style="width:70px">ID</th>
            <th>Nội dung</th>
            <th>Người tạo</th>
            <th>Khoảng thời gian</th>
            <th>Số ngày</th>
            <th>Trạng thái</th>
            <th>Người xử lý</th>
            <th style="width:250px">Thao tác</th>
          </tr>
        </thead>
        <tbody>
        <c:forEach var="r" items="${requests}">
          <tr>
            <td>
              <c:if test="${sessionScope.role == 'MANAGER'}">
                <input type="checkbox" class="rowChk" name="ids" form="bulkForm" value="${r.id}">
              </c:if>
            </td>
            <td>#${r.id}</td>
            <td title="${fn:escapeXml(r.title)}">
              <div class="cell-reason">
                ${fn:escapeXml(r.reason)}
              </div>
            </td>
            <td>#${r.createdBy} <c:if test="${not empty r.createdByName}">– ${r.createdByName}</c:if></td>
            <td>
              <c:choose>
                <c:when test="${not empty r.startDate}">
                  <fmt:parseDate value="${r.startDate}" pattern="yyyy-MM-dd" var="sd"/>
                  <fmt:parseDate value="${r.endDate}"   pattern="yyyy-MM-dd" var="ed"/>
                  <fmt:formatDate value="${sd}" pattern="dd/MM/yyyy"/> – <fmt:formatDate value="${ed}" pattern="dd/MM/yyyy"/>
                </c:when>
                <c:otherwise>—</c:otherwise>
              </c:choose>
            </td>
            <td>
              <c:if test="${not empty r.startDate}">
                <fmt:parseDate value="${r.startDate}" pattern="yyyy-MM-dd" var="sd2"/>
                <fmt:parseDate value="${r.endDate}"   pattern="yyyy-MM-dd" var="ed2"/>
                <c:set var="days" value="${(ed2.time - sd2.time) / (1000*60*60*24) + 1}" />
                ${days}
              </c:if>
            </td>
            <td>
              <span class="pill ${fn:toUpperCase(r.status)}">
                <c:choose>
                  <c:when test="${fn:toUpperCase(r.status)=='PENDING'}">⏳</c:when>
                  <c:when test="${fn:toUpperCase(r.status)=='APPROVED'}">✅</c:when>
                  <c:when test="${fn:toUpperCase(r.status)=='REJECTED'}">⛔</c:when>
                  <c:otherwise>🗑</c:otherwise>
                </c:choose>
                ${fn:toUpperCase(r.status)}
              </span>
            </td>
            <td>
              <c:if test="${not empty r.processedBy}">#${r.processedBy} <c:if test="${not empty r.processedByName}">– ${r.processedByName}</c:if></c:if>
              <c:if test="${empty r.processedBy}"><span class="muted">—</span></c:if>
            </td>
            <td class="table-actions">
              <a class="btn-icon" href="${pageContext.request.contextPath}/request/detail?id=${r.id}" title="Xem">Xem</a>
              <c:if test="${sessionScope.role == 'MANAGER' && fn:toLowerCase(r.status) == 'pending'}">
                <button class="btn-icon ok" data-open-approve data-id="${r.id}" title="Duyệt">Duyệt</button>
                <button class="btn-icon warn" data-open-reject data-id="${r.id}" title="Từ chối">Từ chối</button>
              </c:if>
              <c:if test="${fn:toLowerCase(r.status) == 'pending' && r.createdBy == sessionScope.userId}">
                <a class="btn-icon danger" href="#" data-cancel data-id="${r.id}" title="Hủy">Hủy</a>
              </c:if>
              <a class="btn-icon" href="${pageContext.request.contextPath}/request/duplicate?id=${r.id}" title="Nhân bản">Nhân bản</a>
            </td>
          </tr>
        </c:forEach>

        <c:if test="${empty requests}">
          <tr><td colspan="9" class="empty">Không có dữ liệu phù hợp bộ lọc.</td></tr>
        </c:if>
        </tbody>
      </table>
    </div>

    <!-- Bulk sticky bar -->
    <c:if test="${sessionScope.role == 'MANAGER'}">
      <div class="bulkbar" id="bulkbar">
        <span class="badgeSel">Đã chọn: <b id="selCount2">0</b></span>
        <button class="btn" type="button" id="selAllPage">Chọn tất cả trang</button>
        <button class="btn" type="button" id="selNone">Bỏ chọn</button>
        <button class="btn" type="button" id="gotoTop">Lên đầu</button>
      </div>
    </c:if>

    <c:if test="${totalPages > 1}">
      <div class="pagination">
        <c:url var="baseUrl" value="/request/list">
          <c:param name="q" value="${param.q}" />
          <c:param name="status" value="${param.status}" />
          <c:param name="mine" value="${param.mine}" />
          <c:param name="from" value="${param.from}" />
          <c:param name="to" value="${param.to}" />
          <c:param name="sort" value="${param.sort}" />
        </c:url>

        <c:if test="${page>1}">
          <a href="${baseUrl}&page=${page-1}" aria-label="Trang trước">‹ Trước</a>
        </c:if>
        <c:forEach var="p" begin="1" end="${totalPages}">
          <c:choose>
            <c:when test="${p == page}">
              <span aria-current="page">${p}</span>
            </c:when>
            <c:otherwise>
              <a href="${baseUrl}&page=${p}">${p}</a>
            </c:otherwise>
          </c:choose>
        </c:forEach>
        <c:if test="${page<totalPages}">
          <a href="${baseUrl}&page=${page+1}" aria-label="Trang sau">Sau ›</a>
        </c:if>
      </div>
    </c:if>
  </div>
</div>

<!-- Modal approve/reject -->
<dialog id="approveDlg">
  <form method="post" action="${pageContext.request.contextPath}/request/approve">
    <div class="modal-head">Duyệt đơn</div>
    <input type="hidden" name="id" id="approveId">
    <input type="hidden" name="ok" value="1">
    <input type="hidden" name="csrf" value="${sessionScope.csrfToken}">
    <label>Ghi chú cho nhân viên (tuỳ chọn)</label>
    <textarea name="note" rows="3" style="width:100%"></textarea>
    <div style="display:flex;gap:8px;justify-content:flex-end;margin-top:10px">
      <button type="button" class="btn btn-ghost" data-close>Đóng</button>
      <button class="btn btn-primary" type="submit">Xác nhận duyệt</button>
    </div>
  </form>
</dialog>

<dialog id="rejectDlg">
  <form method="post" action="${pageContext.request.contextPath}/request/approve">
    <div class="modal-head">Từ chối đơn</div>
    <input type="hidden" name="id" id="rejectId">
    <input type="hidden" name="ok" value="0">
    <input type="hidden" name="csrf" value="${sessionScope.csrfToken}">
    <label>Lý do từ chối (bắt buộc)</label>
    <textarea name="note" rows="3" required style="width:100%"></textarea>
    <div style="display:flex;gap:8px;justify-content:flex-end;margin-top:10px">
      <button type="button" class="btn btn-ghost" data-close>Đóng</button>
      <button class="btn danger" type="submit">Xác nhận từ chối</button>
    </div>
  </form>
</dialog>

<form id="cancelForm" method="post" action="${pageContext.request.contextPath}/request/cancel" style="display:none">
  <input type="hidden" name="id" id="cancelId">
  <input type="hidden" name="csrf" value="${sessionScope.csrfToken}">
</form>

<script>
(function(){
  var ctx = '<%=request.getContextPath()%>';

  // ---------- Select all & bulk counter ----------
  var chkAll = document.getElementById('chkAll');
  var chks = document.querySelectorAll('.rowChk');
  var selCount = document.getElementById('selCount');
  var selCount2 = document.getElementById('selCount2');
  var bulkSubmit = document.getElementById('bulkSubmit');
  var bulkbar = document.getElementById('bulkbar');

  function updateSel(){
    var n = 0; chks.forEach(function(c){ if(c.checked) n++; });
    if(selCount) selCount.textContent = n;
    if(selCount2) selCount2.textContent = n;
    if(bulkSubmit) bulkSubmit.disabled = n===0;
    if(bulkbar){ bulkbar.classList.toggle('show', n>0); }
  }
  if(chkAll){
    chkAll.addEventListener('change', function(e){
      chks.forEach(function(c){ c.checked = e.target.checked; });
      updateSel();
    });
  }
  chks.forEach(function(c){ c.addEventListener('change', updateSel); });
  updateSel();

  var selAllPage = document.getElementById('selAllPage');
  var selNone = document.getElementById('selNone');
  var gotoTop = document.getElementById('gotoTop');
  selAllPage && selAllPage.addEventListener('click', function(){ chks.forEach(function(c){ c.checked=true; }); updateSel(); });
  selNone && selNone.addEventListener('click', function(){ chks.forEach(function(c){ c.checked=false; }); updateSel(); });
  gotoTop && gotoTop.addEventListener('click', function(){ window.scrollTo({top:0,behavior:'smooth'}); });

  // ---------- Export CSV (escape-safe) ----------
  function csvEscape(s){
    if(s==null) return '';
    s = String(s);
    if(s.indexOf('"')>-1) s = s.replace(/"/g,'""');
    if(s.search(/["\n,]/)>-1) s = '"' + s + '"';
    return s;
  }
  var btnCsv = document.getElementById('btnExportCsv');
  btnCsv && btnCsv.addEventListener('click', function(){
    var trs = document.querySelectorAll('#reqTable tbody tr');
    var header = ['ID','Nội dung','Người tạo','Khoảng thời gian','Số ngày','Trạng thái','Người xử lý'];
    var lines = [header.join(',')];
    trs.forEach(function(tr){
      var tds = tr.querySelectorAll('td');
      if(!tds.length) return;
      var row = [];
      row.push((tds[1]||{}).innerText || '');
      row.push((tds[2]||{}).innerText || '');
      row.push((tds[3]||{}).innerText || '');
      row.push((tds[4]||{}).innerText || '');
      row.push((tds[5]||{}).innerText || '');
      row.push((tds[6]||{}).innerText || '');
      row.push((tds[7]||{}).innerText || '');
      lines.push(row.map(csvEscape).join(','));
    });
    var blob = new Blob([lines.join('\n')],{type:'text/csv;charset=utf-8;'});
    var a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = 'leave_requests.csv';
    a.click();
  });

  // ---------- Approve/Reject modals ----------
  var approveDlg=document.getElementById('approveDlg');
  var rejectDlg=document.getElementById('rejectDlg');
  document.querySelectorAll('[data-open-approve]').forEach(function(btn){
    btn.addEventListener('click',function(){
      document.getElementById('approveId').value = btn.getAttribute('data-id');
      approveDlg.showModal();
    });
  });
  document.querySelectorAll('[data-open-reject]').forEach(function(btn){
    btn.addEventListener('click',function(){
      document.getElementById('rejectId').value = btn.getAttribute('data-id');
      rejectDlg.showModal();
    });
  });
  document.querySelectorAll('dialog [data-close]').forEach(function(b){
    b.addEventListener('click',function(){ b.closest('dialog').close(); });
  });

  // ---------- Cancel with confirm ----------
  document.querySelectorAll('[data-cancel]').forEach(function(a){
    a.addEventListener('click', function(e){
      e.preventDefault();
      if(confirm('Bạn có chắc muốn hủy đơn này?')){
        document.getElementById('cancelId').value = a.getAttribute('data-id');
        document.getElementById('cancelForm').submit();
      }
    });
  });

  // ---------- Quick filter chips + persist filter ----------
  var chips = document.querySelectorAll('.chip');
  var fForm = document.getElementById('filterForm');
  var fFrom = document.getElementById('from');
  var fTo = document.getElementById('to');
  var fStatus = document.getElementById('statusSel');
  var fMine = document.getElementById('mineSel');
  var fQ = document.getElementById('q');
  var KEY='list_filters_v1';

  function iso(d){ return d.toISOString().slice(0,10); }
  function startOfWeek(d){
    var day = d.getDay(); // 0:CN
    var diff = (day===0?6:day-1); // về Thứ 2
    return new Date(d.getFullYear(), d.getMonth(), d.getDate()-diff);
  }
  function endOfWeek(d){
    var s = startOfWeek(d); return new Date(s.getFullYear(), s.getMonth(), s.getDate()+6);
  }
  function startOfMonth(d){ return new Date(d.getFullYear(), d.getMonth(), 1); }
  function endOfMonth(d){ return new Date(d.getFullYear(), d.getMonth()+1, 0); }

  chips.forEach(function(ch){
    ch.addEventListener('click', function(){
      var t = ch.getAttribute('data-quick');
      var now = new Date();
      if(t==='week'){
        fFrom.value = iso(startOfWeek(now));
        fTo.value = iso(endOfWeek(now));
      }else if(t==='month'){
        fFrom.value = iso(startOfMonth(now));
        fTo.value = iso(endOfMonth(now));
      }else if(t==='pending'){
        fStatus.value = 'PENDING';
      }else if(t==='approved'){
        fStatus.value = 'APPROVED';
      }else if(t==='mine'){
        fMine.value = '1';
      }else if(t==='clear'){
        fFrom.value = ''; fTo.value=''; fStatus.value=''; fMine.value=''; fQ.value='';
      }
      fForm.submit();
    });
  });

  // persist
  function saveFilters(){
    var obj = {from:fFrom.value,to:fTo.value,status:fStatus.value,mine:fMine.value,q:fQ.value};
    try{ localStorage.setItem(KEY, JSON.stringify(obj)); }catch(_){}
  }
  function loadFilters(){
    try{
      var raw = localStorage.getItem(KEY); if(!raw) return;
      var o = JSON.parse(raw);
      if(!o) return;
      if(!('${param.from}'||'').length && o.from) fFrom.value = o.from;
      if(!('${param.to}'||'').length && o.to) fTo.value = o.to;
      if(!('${param.status}'||'').length && o.status) fStatus.value = o.status;
      if(!('${param.mine}'||'').length && o.mine) fMine.value = o.mine;
      if(!('${fn:escapeXml(param.q)}'||'').length && o.q) fQ.value = o.q;
    }catch(_){}
  }
  // khi submit form lọc -> lưu
  fForm && fForm.addEventListener('submit', saveFilters);
  loadFilters();

  // ---------- Search highlight ----------
  function highlight(term){
    if(!term) return;
    var cells = document.querySelectorAll('.cell-reason');
    var re = null;
    try {
      re = new RegExp('(' + term.replace(/[.*+?^\\${}()|[\\]\\\\]/g, '\\\\$&') + ')', 'ig');
    } catch(e) { return; }
    cells.forEach(function(c){
      var txt = c.textContent || '';
      var html = txt.replace(re, '<mark>$1</mark>');
      c.innerHTML = html;
    });
  }
  highlight('${fn:escapeXml(param.q)}');

  // ---------- Keyboard shortcuts ----------
  document.addEventListener('keydown', function(e){
    var k = (e.key||'').toLowerCase();
    if(k==='/' && !e.ctrlKey && !e.metaKey){
      e.preventDefault();
      var q = document.getElementById('q'); if(q){ q.focus(); q.select(); }
    }
    if(k==='r' && !e.ctrlKey && !e.metaKey){
      e.preventDefault();
      document.getElementById('btnRefresh').click();
    }
    if(k==='a' && !e.ctrlKey && !e.metaKey && chkAll){
      e.preventDefault(); chkAll.click();
    }
  });
})();
</script>

<%@ include file="/WEB-INF/views/common/_footer.jsp" %>
</body>
</html>
