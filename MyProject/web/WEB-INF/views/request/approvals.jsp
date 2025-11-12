<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp" %>
<jsp:include page="/WEB-INF/views/layout/_header.jsp" />
<jsp:include page="/WEB-INF/views/layout/_sidebar.jsp" />


<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="data" value="${not empty pending ? pending : items}" />
<c:set var="csrfParam" value="${empty requestScope.csrfParam ? '_csrf' : requestScope.csrfParam}" />
<c:set var="csrf" value="${empty csrf ? (empty requestScope.csrfToken ? '' : requestScope.csrfToken) : csrf}" />
<c:set var="pageTitle" value="Phê duyệt đơn nghỉ"/>

<style>
  :root{ --card:#fff; --bd:#e5e7eb; --ink:#0f172a; --muted:#64748b; --table:#f6f7fb; }
  .wrap{max-width:1400px;margin:0 auto;}
  .toolbar{display:flex;justify-content:space-between;align-items:center;background:var(--card);
    border:1px solid var(--bd);border-radius:8px;padding:14px 16px;margin-bottom:16px}
  .toolbar h1{margin:0;font-size:20px;font-weight:700}
  .btn{border:1px solid var(--bd);border-radius:8px;padding:8px 12px;background:#fff;cursor:pointer;text-decoration:none;color:#111827}
  .btn:hover{background:#eef2f7}
  .btn.small{padding:6px 10px;font-size:12px}
  .btn.ok{background:#10b981;border-color:#10b981;color:#fff}
  .btn.danger{background:#ef4444;border-color:#ef4444;color:#fff}
  .btn.primary{background:#111827;border-color:#111827;color:#fff}
  .btn[disabled]{opacity:.7;cursor:not-allowed}
  .chip{border:1px dashed var(--bd);border-radius:8px;padding:4px 8px;font-size:12px}
  .table-wrap{background:#fff;border:1px solid var(--bd);border-radius:8px;overflow:hidden}
  table{width:100%;border-collapse:collapse;min-width:900px}
  thead{background:var(--table)}
  th,td{padding:12px 14px;border-bottom:1px solid var(--bd);text-align:left}
  tbody tr:hover{background:#f7f8fb}
  .bar-bottom{display:flex;justify-content:space-between;align-items:center;background:var(--table);border:1px solid var(--bd);border-top:none;border-radius:0 0 8px 8px;padding:12px}
  .modal{position:fixed;inset:0;background:rgba(0,0,0,.55);display:none;align-items:center;justify-content:center;padding:20px;z-index:50}
  .modal.show{display:flex !important}
  .dialog{max-width:560px;width:100%;background:#fff;border:1px solid var(--bd);border-radius:8px;padding:20px}
  .toast{position:fixed;right:20px;bottom:20px;display:flex;gap:10px;flex-direction:column;z-index:60}
  .t{background:#fff;border:1px solid var(--bd);padding:10px 12px;border-radius:8px}
  .muted{color:var(--muted)}
  .alert{border:1px solid #fecaca;background:#fef2f2;color:#7f1d1d;border-radius:8px;padding:10px 12px;margin:12px 0}
</style>

<div class="wrap">
  <div class="toolbar" role="region" aria-label="Thanh công cụ phê duyệt">
    <h1>Phê duyệt đơn nghỉ</h1>
    <div>
      <button id="csvBtn" class="btn" type="button">Export CSV</button>
      <button id="excelBtn" class="btn primary small" type="button">Export Excel</button>
    </div>
  </div>

  <c:if test="${not empty flash}">
    <div class="t">${flash}</div>
  </c:if>

  <c:if test="${not empty dbError}">
    <div class="alert">Không tải được dữ liệu từ cơ sở dữ liệu (mã: ${dbError}). Vui lòng thử lại sau.</div>
  </c:if>

  <div class="table-wrap">
    <table id="tbl" aria-label="Danh sách đơn chờ duyệt">
      <thead>
        <tr>
          <th style="width:40px"><input type="checkbox" id="chkAll" aria-label="Chọn tất cả"></th>
          <th>#</th>
          <th>Nhân sự</th>
          <th>Loại</th>
          <th>Từ ngày</th>
          <th>Đến ngày</th>
          <th>Lý do</th>
          <th>Thao tác</th>
        </tr>
      </thead>
      <tbody id="pendingBody">
        <c:forEach var="r" items="${data}" varStatus="vs">
          <tr data-id="${r.id}" data-name="${fn:escapeXml(r.fullName)}"
              data-type="${fn:escapeXml(r.type)}" data-reason="${fn:escapeXml(r.reason)}"
              class="row-pending">
            <td><input type="checkbox" class="rowChk" name="ids" value="${r.id}" aria-label="Chọn đơn #${r.id}"></td>
            <td><strong>${vs.index + 1}</strong></td>
            <td><strong>${r.fullName}</strong></td>
            <td><span class="chip">${r.type}</span></td>
            <td>
              <c:choose>
                <c:when test="${not empty r.from}">
                  <fmt:formatDate value="${r.from}" pattern="dd/MM/yyyy"/>
                </c:when><c:otherwise>—</c:otherwise>
              </c:choose>
            </td>
            <td>
              <c:choose>
                <c:when test="${not empty r.to}">
                  <fmt:formatDate value="${r.to}" pattern="dd/MM/yyyy"/>
                </c:when><c:otherwise>—</c:otherwise>
              </c:choose>
            </td>
            <td style="max-width:280px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap"
                title="${fn:escapeXml(r.reason)}">${r.reason}</td>
            <td style="display:flex;gap:6px">
              <a class="btn small" href="${ctx}/request/detail?id=${r.id}">Xem</a>
              <button class="btn small ok act-single" data-action="APPROVE" data-id="${r.id}" type="button">Duyệt</button>
              <button class="btn small danger act-single" data-action="REJECT"  data-id="${r.id}" type="button">Từ chối</button>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty data}">
          <tr><td colspan="8" class="muted" style="text-align:center;padding:24px">Không có đơn chờ duyệt</td></tr>
        </c:if>
      </tbody>
    </table>
  </div>

  <div class="bar-bottom">
    <div class="muted">Đã chọn: <b id="selCount">0</b></div>
    <div style="display:flex;gap:8px">
      <button id="bulkApproveBtn" class="btn ok small" type="button">Duyệt</button>
      <button id="bulkRejectBtn" class="btn danger small" type="button">Từ chối</button>
    </div>
  </div>
</div>

<!-- Modal -->
<div id="modal" class="modal" role="dialog" aria-modal="true" aria-labelledby="mTitle" aria-describedby="mDesc">
  <form id="mForm" class="dialog" method="post" action="${ctx}/request/approvals">
    <h3 id="mTitle" style="margin:0 0 8px">Xác nhận</h3>
    <p id="mDesc" class="muted">Bạn có chắc muốn thực hiện thao tác này?</p>
    <textarea id="mNote" name="note" placeholder="Ghi chú (không bắt buộc)" style="width:100%;min-height:110px;margin-top:8px" maxlength="500"></textarea>
    <input type="hidden" name="${csrfParam}" value="${csrf}">
    <input type="hidden" name="action" id="mAction" value="">
    <input type="hidden" name="id" id="mId" value="">
    <div id="mIds"></div>
    <div style="display:flex;gap:8px;justify-content:flex-end;margin-top:12px">
      <button type="button" id="mCancel" class="btn">Hủy</button>
      <button id="mOk" class="btn primary" type="submit">Xác nhận</button>
    </div>
  </form>
</div>

<div class="toast" id="toast"></div>

<script>
(function(){
  const $ = s=>document.querySelector(s), $$=s=>Array.from(document.querySelectorAll(s));

  function toast(msg){
    const t=document.createElement('div');t.className='t';t.textContent=msg;
    $('#toast').appendChild(t);setTimeout(()=>{t.style.opacity='0';setTimeout(()=>t.remove(),150)},2000);
  }

  const chkAll=$('#chkAll'), selCount=$('#selCount');
  function updateSel(){ selCount.textContent = $$('.rowChk:checked').length; }
  chkAll?.addEventListener('change',()=>{ $$('.rowChk').forEach(c=>c.checked=chkAll.checked); updateSel(); });
  $$('.rowChk').forEach(c=>c.addEventListener('change',()=>{ if(!c.checked) chkAll.checked=false; updateSel(); }));

  const modal=$('#modal'), mForm=$('#mForm'), mAction=$('#mAction'), mId=$('#mId'), mIds=$('#mIds'),
        mTitle=$('#mTitle'), mDesc=$('#mDesc'), mNote=$('#mNote');

  function openModal(action, ids){
    if(!ids || !ids.length){ toast('Chưa chọn đơn'); return; }
    mAction.value=action; mIds.innerHTML=''; mId.value='';
    if(ids.length===1){ mTitle.textContent=(action==='APPROVE'?'Duyệt':'Từ chối')+' đơn #'+ids[0]; mId.value=ids[0]; }
    else{ mTitle.textContent=(action==='APPROVE'?'Duyệt':'Từ chối')+' '+ids.length+' đơn';
      ids.forEach(id=>{ const i=document.createElement('input'); i.type='hidden'; i.name='ids'; i.value=id; mIds.appendChild(i); });
    }
    mDesc.textContent='Bạn có thể thêm ghi chú trước khi xác nhận.'; mNote.value='';
    modal.classList.add('show'); mNote.focus();
  }
  function closeModal(){ modal.classList.remove('show'); }
  document.addEventListener('keydown', e=>{ if(e.key==='Escape' && modal.classList.contains('show')) closeModal(); });
  $('#mCancel')?.addEventListener('click', closeModal);
  modal?.addEventListener('click', e=>{ if(e.target===modal) closeModal(); });

  $$('.act-single').forEach(b=>b.addEventListener('click',()=>openModal(b.dataset.action,[b.dataset.id])));
  const getSelectedIds=()=>$$('.rowChk:checked').map(c=>c.value).filter(Boolean);
  $('#bulkApproveBtn')?.addEventListener('click',()=>openModal('APPROVE',getSelectedIds()));
  $('#bulkRejectBtn')?.addEventListener('click',()=>openModal('REJECT',getSelectedIds()));

  // Export CSV
  function rowsVisible(){ return $$('#pendingBody > tr.row-pending'); }
  $('#csvBtn')?.addEventListener('click',()=>{
    const rows=rowsVisible(); if(!rows.length) return toast('Không có dữ liệu');
    const header=['ID','Nhân sự','Loại','Từ','Đến','Lý do'];
    const data=rows.map(r=>{
      const t=r.querySelectorAll('td');
      return [ r.dataset.id, t[2]?.innerText.trim()||'', t[3]?.innerText.trim()||'',
               t[4]?.innerText.trim()||'', t[5]?.innerText.trim()||'',
               (t[6]?.innerText||'').replace(/\s+/g,' ').trim() ];
    });
    const csv=[header].concat(data).map(a=>a.map(v=>{
      v=(v??'')+''; return /[",\n]/.test(v)?'"'+v.replace(/"/g,'""')+'"':v;
    }).join(',')).join('\n');
    const blob=new Blob(['\ufeff'+csv],{type:'text/csv;charset=utf-8;'}),url=URL.createObjectURL(blob);
    const a=Object.assign(document.createElement('a'),{href:url,download:'approvals_'+new Date().toISOString().slice(0,10)+'.csv'});
    document.body.appendChild(a);a.click();a.remove();URL.revokeObjectURL(url);
    toast('Đã xuất CSV');
  });

  // Export Excel (HTML table)
  $('#excelBtn')?.addEventListener('click',()=>{
    const rows=rowsVisible(); if(!rows.length) return toast('Không có dữ liệu');
    let html='<table><thead><tr><th>ID</th><th>Nhân sự</th><th>Loại</th><th>Từ</th><th>Đến</th><th>Lý do</th></tr></thead><tbody>';
    rows.forEach(r=>{
      const t=r.querySelectorAll('td');
      html+='<tr><td>'+r.dataset.id+'</td><td>'+(t[2]?.innerText.trim()||'')
        +'</td><td>'+(t[3]?.innerText.trim()||'')+'</td><td>'+(t[4]?.innerText.trim()||'')
        +'</td><td>'+(t[5]?.innerText.trim()||'')+'</td><td>'+((t[6]?.innerText||'').trim())+'</td></tr>';
    });
    html+='</tbody></table>';
    const blob=new Blob(['\ufeff'+html],{type:'application/vnd.ms-excel'}),url=URL.createObjectURL(blob);
    const a=Object.assign(document.createElement('a'),{href:url,download:'approvals_'+new Date().toISOString().slice(0,10)+'.xls'});
    document.body.appendChild(a);a.click();a.remove();URL.revokeObjectURL(url);
    toast('Đã xuất Excel');
  });
})();
</script>

<jsp:include page="/WEB-INF/views/layout/_footer.jsp" />
