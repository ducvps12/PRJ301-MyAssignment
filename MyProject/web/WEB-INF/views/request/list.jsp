<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>Danh sách đơn nghỉ phép</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <style>
    :root{
      --bd:#e5e7eb;--bg:#f8fafc;--muted:#6b7280;--ok:#16a34a;--no:#dc2626;--warn:#d97706;--pri:#2563eb;
    }
    *{box-sizing:border-box}
    body{font-family:system-ui,Segoe UI,Roboto,Helvetica,Arial,sans-serif;margin:24px;background:#fff}
    .wrap{max-width:1200px;margin:auto}
    .toolbar{display:flex;gap:8px;flex-wrap:wrap;align-items:center;margin-bottom:14px}
    .card{border:1px solid var(--bd);border-radius:14px;overflow:hidden;background:#fff}
    .card-head{display:flex;align-items:center;justify-content:space-between;padding:10px 12px;background:var(--bg)}
    table{width:100%;border-collapse:collapse}
    th,td{padding:10px 12px;border-top:1px solid #eef2f7;vertical-align:middle}
    th{background:#f9fafb;text-align:left;font-weight:600;white-space:nowrap}
    .btn,.chip{display:inline-flex;align-items:center;gap:6px;padding:8px 12px;border-radius:10px;border:1px solid var(--bd);text-decoration:none;color:#111;background:#fff;cursor:pointer}
    .btn-primary{background:var(--pri);border-color:var(--pri);color:#fff}
    .btn-ghost{background:#fff}
    .btn-icon{padding:6px 8px}
    .chip{font-size:12px;padding:4px 8px}
    .pill{display:inline-flex;gap:6px;align-items:center;padding:4px 10px;border-radius:999px;font-size:12px;font-weight:700;border:1px solid transparent}
    .pill.PENDING{background:#fff7ed;color:#9a3412;border-color:#fde68a}
    .pill.APPROVED{background:#ecfdf5;color:#14532d;border-color:#bbf7d0}
    .pill.REJECTED{background:#fef2f2;color:#7f1d1d;border-color:#fecaca}
    .pill.CANCELLED{background:#f3f4f6;color:#374151;border-color:#e5e7eb}
    .muted{color:var(--muted)}
    input[type="text"], input[type="date"], select{padding:8px 10px;border:1px solid #d1d5db;border-radius:8px}
    .right{display:flex;align-items:center;gap:8px;margin-left:auto}
    .table-actions a{margin-right:6px}
    .table-actions .danger{color:var(--no)}
    .table-actions .ok{color:var(--ok)}
    .table-actions .warn{color:var(--warn)}
    .pagination{display:flex;gap:6px;justify-content:flex-end;padding:10px}
    .pagination a,.pagination span{padding:6px 10px;border:1px solid var(--bd);border-radius:8px;text-decoration:none}
    .kpi{display:flex;gap:10px;flex-wrap:wrap}
    .kpi .box{border:1px solid var(--bd);border-radius:12px;padding:10px 12px;background:#fff}
    .empty{padding:28px;text-align:center;color:var(--muted)}
    .row-select{width:28px}
    .sticky-head thead th{position:sticky;top:0;z-index:1}
    /* modal */
    dialog{border:none;border-radius:14px;max-width:520px;width:90%}
    dialog::backdrop{background:rgba(0,0,0,.3)}
    .modal-head{font-weight:700;margin-bottom:8px}
    .sr{position:absolute;left:-10000px;top:auto;width:1px;height:1px;overflow:hidden}
    .msg{margin:8px 0;padding:8px 10px;border-radius:10px}
    .msg.ok{background:#ecfdf5;color:#166534}
    .msg.no{background:#fef2f2;color:#991b1b}
  </style>
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
    <a class="btn" href="${pageContext.request.contextPath}/request/agenda">📅 Xem Agenda</a>
    <button class="btn" id="btnExportCsv" type="button">⇩ Xuất CSV</button>
    <a class="btn btn-ghost" href="${pageContext.request.contextPath}/request/list">⟲ Làm mới</a>

    <!-- Lọc nâng cao -->
    <form method="get" action="${pageContext.request.contextPath}/request/list" class="right">
      <label class="sr" for="from">Từ ngày</label>
      <input type="date" id="from" name="from" value="${param.from}">
      <label class="sr" for="to">Đến ngày</label>
      <input type="date" id="to" name="to" value="${param.to}">
      <select name="status" aria-label="Trạng thái">
        <option value="">Trạng thái</option>
        <option value="PENDING"   ${param.status == 'PENDING'   ? 'selected':''}>Chờ duyệt</option>
        <option value="APPROVED"  ${param.status == 'APPROVED'  ? 'selected':''}>Đã duyệt</option>
        <option value="REJECTED"  ${param.status == 'REJECTED'  ? 'selected':''}>Từ chối</option>
        <option value="CANCELLED" ${param.status == 'CANCELLED' ? 'selected':''}>Đã hủy</option>
      </select>
      <select name="mine" aria-label="Phạm vi">
        <option value="">Của mọi người</option>
        <option value="1" ${param.mine == '1' ? 'selected' : ''}>Chỉ của tôi</option>
        <c:if test="${sessionScope.role == 'MANAGER'}">
          <option value="team" ${param.mine == 'team' ? 'selected' : ''}>Cấp dưới của tôi</option>
        </c:if>
      </select>
      <input type="text" name="q" value="${fn:escapeXml(param.q)}" placeholder="Tìm lý do, người tạo...">
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

  <div class="card sticky-head">
    <div class="card-head">
      <div class="muted">Tổng: <b>${totalItems}</b> đơn • Trang <b>${page}</b>/<b>${totalPages}</b></div>
      <c:if test="${sessionScope.role == 'MANAGER'}">
        <form id="bulkForm" method="post" action="${pageContext.request.contextPath}/request/bulk">
          <input type="hidden" name="csrf" value="${sessionScope.csrfToken}">
          <select name="action">
            <option value="">Bulk action…</option>
            <option value="approve">Duyệt</option>
            <option value="reject">Từ chối</option>
            <option value="cancel">Hủy</option>
          </select>
          <input name="note" placeholder="Ghi chú (tuỳ chọn)" />
          <button class="btn" type="submit">Thực hiện</button>
        </form>
      </c:if>
    </div>

    <table id="reqTable">
      <thead>
        <tr>
          <th class="row-select">
            <c:if test="${sessionScope.role == 'MANAGER'}">
              <input type="checkbox" id="chkAll">
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
            <div style="max-width:360px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">
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
              <%-- days inclusive: end - start + 1 --%>
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
            <a class="btn btn-icon" href="${pageContext.request.contextPath}/request/detail?id=${r.id}" title="Xem">Xem</a>
            <c:if test="${sessionScope.role == 'MANAGER' && fn:toLowerCase(r.status) == 'pending'}">
              <button class="btn ok btn-icon" data-open-approve data-id="${r.id}">Duyệt</button>
              <button class="btn warn btn-icon" data-open-reject data-id="${r.id}">Từ chối</button>
            </c:if>
            <c:if test="${fn:toLowerCase(r.status) == 'pending' && r.createdBy == sessionScope.userId}">
              <a class="btn danger btn-icon" href="#" data-cancel data-id="${r.id}">Hủy</a>
            </c:if>
            <a class="btn btn-icon" href="${pageContext.request.contextPath}/request/duplicate?id=${r.id}" title="Nhân bản">Nhân bản</a>
          </td>
        </tr>
      </c:forEach>

      <c:if test="${empty requests}">
        <tr><td colspan="9" class="empty">Không có dữ liệu phù hợp bộ lọc.</td></tr>
      </c:if>
      </tbody>
    </table>

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
          <a href="${baseUrl}&page=${page-1}">‹ Trước</a>
        </c:if>
        <c:forEach var="p" begin="1" end="${totalPages}">
          <c:choose>
            <c:when test="${p == page}">
              <span style="background:#f3f4f6">${p}</span>
            </c:when>
            <c:otherwise>
              <a href="${baseUrl}&page=${p}">${p}</a>
            </c:otherwise>
          </c:choose>
        </c:forEach>
        <c:if test="${page<totalPages}">
          <a href="${baseUrl}&page=${page+1}">Sau ›</a>
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
    <textarea name="note" rows="3" style="width:100%;padding:8px;border:1px solid #d1d5db;border-radius:8px"></textarea>
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
    <textarea name="note" rows="3" required style="width:100%;padding:8px;border:1px solid #d1d5db;border-radius:8px"></textarea>
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
  // Select all
  const chkAll = document.getElementById('chkAll');
  if (chkAll){
    chkAll.addEventListener('change', e=>{
      document.querySelectorAll('.rowChk').forEach(c=>c.checked=e.target.checked);
    });
  }

  // Export CSV
  document.getElementById('btnExportCsv').addEventListener('click', ()=>{
    const rows=[...document.querySelectorAll('#reqTable tr')].map(tr=>[...tr.children].slice(1,8).map(td=>td.innerText.trim()));
    const data = rows.map(r=>r.join(',')).join('\n');
    const blob = new Blob([data],{type:'text/csv;charset=utf-8;'});
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = 'leave_requests.csv';
    a.click();
  });

  // Approve/Reject modals
  const approveDlg=document.getElementById('approveDlg');
  const rejectDlg=document.getElementById('rejectDlg');
  document.querySelectorAll('[data-open-approve]').forEach(btn=>{
    btn.addEventListener('click',()=>{
      document.getElementById('approveId').value = btn.dataset.id;
      approveDlg.showModal();
    });
  });
  document.querySelectorAll('[data-open-reject]').forEach(btn=>{
    btn.addEventListener('click',()=>{
      document.getElementById('rejectId').value = btn.dataset.id;
      rejectDlg.showModal();
    });
  });
  document.querySelectorAll('dialog [data-close]').forEach(b=>{
    b.addEventListener('click',()=> b.closest('dialog').close());
  });

  // Cancel with confirm
  document.querySelectorAll('[data-cancel]').forEach(a=>{
    a.addEventListener('click', (e)=>{
      e.preventDefault();
      if(confirm('Bạn có chắc muốn hủy đơn này?')){
        document.getElementById('cancelId').value = a.dataset.id;
        document.getElementById('cancelForm').submit();
      }
    });
  });
</script>

<%@ include file="/WEB-INF/views/common/_footer.jsp" %>
</body>
</html>
