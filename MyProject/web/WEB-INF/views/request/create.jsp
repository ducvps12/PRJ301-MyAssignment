<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>Tạo đơn nghỉ phép</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">

  <style>
    :root{
      --bg:#f7f7f8; --card:#fff; --bd:#e5e7eb; --muted:#6b7280; --txt:#111827;
      --primary:#1a73e8; --primary-600:#1664c7; --ok:#0b65c2; --err:#b91c1c;
      --ring:#93c5fd; --shadow:0 2px 10px rgba(0,0,0,.06);
    }
    *{box-sizing:border-box}
    html,body{margin:0;height:100%}
    body{font-family:system-ui,-apple-system,Segoe UI,Roboto,Helvetica,Arial,sans-serif;background:var(--bg);color:var(--txt)}
    .wrap{max-width:760px;margin:32px auto;padding:0 16px}
    .page-title{display:flex;align-items:center;gap:10px;margin:0 0 16px}
    .page-title .badge{font-size:12px;background:#eef2f7;border:1px solid var(--bd);border-radius:999px;padding:4px 10px;color:#374151}
    .card{background:var(--card);border:1px solid var(--bd);border-radius:14px;box-shadow:var(--shadow);padding:20px}
    h2{margin:0 0 4px}
    .sub{margin:0 0 16px;color:var(--muted);font-size:13px}
    label{display:block;font-weight:600;margin-top:12px;margin-bottom:6px}
    .req{color:var(--err)}
    .field{position:relative}
    input[type="text"], input[type="date"], textarea{
      width:100%;padding:11px 12px;border:1px solid #d1d5db;border-radius:10px;background:#fff;
      transition:border-color .15s, box-shadow .15s;
    }
    input:focus, textarea:focus{outline:none;border-color:var(--ring);box-shadow:0 0 0 3px rgba(147,197,253,.45)}
    textarea{min-height:120px;resize:vertical}
    .row{display:grid;grid-template-columns:1fr 1fr;gap:12px}
    @media (max-width:640px){ .row{grid-template-columns:1fr} }
    .actions{margin-top:18px;display:flex;flex-wrap:wrap;gap:8px}
    .btn{padding:10px 14px;border:1px solid transparent;border-radius:10px;cursor:pointer;font-weight:600;display:inline-flex;align-items:center;gap:8px}
    .btn:disabled{opacity:.65;cursor:not-allowed}
    .btn-primary{background:var(--primary);color:#fff}
    .btn-primary:hover{background:var(--primary-600)}
    .btn-muted{background:#eef2f7;color:#111;border-color:#e5e7eb}
    .btn-ghost{background:transparent;border-color:var(--bd);color:#111}
    .alert{padding:10px 12px;border-radius:10px;margin-bottom:12px;border:1px solid}
    .alert-error{background:#fde8e8;border-color:#fecaca;color:var(--err)}
    .alert-ok{background:#e7f5ff;border-color:#b6e0fe;color:var(--ok)}
    small.muted{color:var(--muted)}
    .hint{font-size:12px;color:var(--muted);margin-top:6px}
    .counter{position:absolute;right:10px;bottom:8px;font-size:12px;color:var(--muted)}
    .inline{display:flex;align-items:center;gap:10px;flex-wrap:wrap}
    .quick{display:flex;gap:8px;flex-wrap:wrap;margin-top:6px}
    .quick .chip{border:1px solid var(--bd);background:#fafafa;border-radius:999px;padding:6px 10px;font-size:12px;cursor:pointer}
    .kpi{display:flex;gap:10px;flex-wrap:wrap;margin-top:8px}
    .kpi .pill{background:#f1f5f9;border:1px solid var(--bd);border-radius:999px;padding:6px 10px;font-size:12px}
    .error-text{color:var(--err);font-size:12px;margin-top:6px;display:none}
    .show{display:block}
    /* Modal preview */
    .modal{position:fixed;inset:0;display:none;place-items:center;background:rgba(0,0,0,.4);padding:16px;z-index:50}
    .modal.open{display:grid}
    .modal .sheet{max-width:640px;width:100%;background:#fff;border-radius:14px;border:1px solid var(--bd);box-shadow:var(--shadow);padding:18px}
    .sheet h3{margin:0 0 8px}
    .sheet .grid{display:grid;grid-template-columns:140px 1fr;gap:8px}
    .sheet .grid div{padding:6px 0;border-bottom:1px dashed #e5e7eb}
    .spinner{width:16px;height:16px;border-radius:50%;border:2px solid #fff;border-top-color:transparent;animation:spin .9s linear infinite}
    @keyframes spin{to{transform:rotate(360deg)}}
    /* Toast */
    .toast{position:fixed;right:14px;bottom:14px;display:none;background:#111827;color:#fff;padding:10px 12px;border-radius:10px;box-shadow:var(--shadow);font-size:13px}
    .toast.show{display:block}
  </style>
</head>
<body>
  <div class="wrap">
    <div class="page-title">
      <h2 style="margin:0">Tạo đơn nghỉ phép</h2>
      <span class="badge">Trạng thái sau khi gửi: Inprogress</span>
    </div>
    <p class="sub">Điền thông tin dưới đây. Bạn có thể xem trước và lưu bản nháp; nhấn <b>Ctrl+Enter</b> để gửi nhanh.</p>

    <div class="card" role="region" aria-label="Tạo đơn">
      <!-- Thông báo server -->
      <c:if test="${not empty error}">
        <div class="alert alert-error" aria-live="polite">${error}</div>
      </c:if>
      <c:if test="${not empty message}">
        <div class="alert alert-ok" aria-live="polite">${message}</div>
      </c:if>

      <!-- Form -->
      <form id="leaveForm" method="post" action="${pageContext.request.contextPath}/request/create" novalidate>
        <input type="hidden" name="_csrf" value="${sessionScope._csrf}"/>

        <!-- Title -->
        <label for="title">Tiêu đề (tùy chọn)</label>
        <div class="field">
          <input id="title" name="title" type="text" placeholder="VD: Nghỉ phép cá nhân"
                 value="${fn:escapeXml(param.title)}" autocomplete="off" aria-describedby="titleHint"/>
          <div id="titleHint" class="hint">Nếu để trống, hệ thống vẫn tạo đơn bình thường.</div>
        </div>

        <!-- Reason -->
        <label for="reason">Lý do <span class="req">*</span></label>
        <div class="field">
          <textarea id="reason" name="reason" required
                    placeholder="Mô tả lý do xin nghỉ (tối thiểu 20 ký tự)">${fn:escapeXml(param.reason)}</textarea>
          <div class="counter" id="reasonCounter">0</div>
        </div>
        <div class="error-text" id="reasonError">Vui lòng nhập lý do tối thiểu 20 ký tự.</div>
        <small class="muted">Không nhập thông tin nhạy cảm.</small>

        <!-- Dates -->
        <div class="row">
          <div>
            <label for="start_date">Từ ngày <span class="req">*</span></label>
            <input id="start_date" name="start_date" type="date" required
                   value="${fn:escapeXml(param.start_date)}" aria-describedby="dateHelp"/>
          </div>
          <div>
            <label for="end_date">Đến ngày <span class="req">*</span></label>
            <input id="end_date" name="end_date" type="date" required
                   value="${fn:escapeXml(param.end_date)}"/>
          </div>
        </div>

        <div class="quick" aria-label="Chọn nhanh">
          <span class="chip" data-quick="today">Hôm nay</span>
          <span class="chip" data-quick="tomorrow">Ngày mai</span>
          <span class="chip" data-quick="next3">3 ngày tới</span>
          <span class="chip" data-quick="clear">Xóa ngày</span>
        </div>

        <div class="kpi" id="kpiBar">
          <span class="pill" id="rangeInfo">Chưa chọn khoảng ngày</span>
          <span class="pill" id="daysInfo" style="display:none"></span>
          <span class="pill" id="warnInfo" style="display:none;color:#b45309;background:#fff7ed;border-color:#fed7aa">Đến ngày < Từ ngày</span>
        </div>

        <!-- Actions -->
        <div class="actions">
          <button class="btn btn-primary" id="submitBtn" type="submit">
            <span class="btn-label">Gửi đơn</span>
          </button>
          <button class="btn btn-ghost" id="previewBtn" type="button">Xem trước</button>
          <a class="btn btn-muted" href="${pageContext.request.contextPath}/request/list">Quay lại</a>
          <button class="btn btn-muted" id="saveDraft" type="button" title="Lưu bản nháp vào máy">Lưu nháp</button>
          <button class="btn btn-muted" id="clearDraft" type="button" title="Xóa bản nháp">Xóa nháp</button>
        </div>
      </form>
    </div>
  </div>

  <!-- Preview Modal -->
  <div class="modal" id="previewModal" role="dialog" aria-modal="true" aria-labelledby="previewTitle">
    <div class="sheet">
      <h3 id="previewTitle">Xem trước đơn nghỉ</h3>
      <div class="grid" id="previewGrid"></div>
      <div class="actions" style="margin-top:14px">
        <button class="btn btn-primary" id="confirmSubmit"><span>Xác nhận gửi</span></button>
        <button class="btn btn-muted" id="closePreview" type="button">Đóng</button>
      </div>
    </div>
  </div>

  <!-- Toast -->
  <div class="toast" id="toast"></div>

  <script>
    (function(){
      const $ = (s,sc)=> (sc||document).querySelector(s);
      const $$ = (s,sc)=> (sc||document).querySelectorAll(s);

      const form = $('#leaveForm');
      const title = $('#title');
      const reason = $('#reason');
      const reasonCounter = $('#reasonCounter');
      const reasonError = $('#reasonError');
      const s = $('#start_date');
      const e = $('#end_date');
      const rangeInfo = $('#rangeInfo');
      const daysInfo = $('#daysInfo');
      const warnInfo = $('#warnInfo');
      const submitBtn = $('#submitBtn');
      const previewBtn = $('#previewBtn');
      const saveDraftBtn = $('#saveDraft');
      const clearDraftBtn = $('#clearDraft');
      const modal = $('#previewModal');
      const previewGrid = $('#previewGrid');
      const confirmSubmit = $('#confirmSubmit');
      const closePreview = $('#closePreview');
      const toast = $('#toast');

      // ---- Helpers
      const todayStr = (d=new Date()) => d.toISOString().slice(0,10);
      const parse = (v)=> v? new Date(v+"T00:00:00") : null;
      const diffDaysInc = (a,b)=> Math.floor((b-a)/86400000)+1;

      function showToast(msg, ms=1800){
        toast.textContent = msg; toast.classList.add('show');
        setTimeout(()=>toast.classList.remove('show'), ms);
      }
      function setLoading(btn, on){
        if(on){
          btn.disabled = true;
          const span = btn.querySelector('.btn-label') || btn.querySelector('span') || btn;
          const spin = document.createElement('span'); spin.className='spinner';
          btn._oldLabel = span.textContent; span.textContent = '';
          span.appendChild(spin);
        }else{
          btn.disabled = false;
          const span = btn.querySelector('.btn-label') || btn.querySelector('span') || btn;
          if(btn._oldLabel!=null){ span.textContent = btn._oldLabel; btn._oldLabel=null; }
        }
      }

      // ---- Reason counter + early validation
      function syncReason(){
        const len = reason.value.trim().length;
        reasonCounter.textContent = len;
        if(len>0 && len<20){ reasonError.classList.add('show'); }
        else { reasonError.classList.remove('show'); }
      }
      reason.addEventListener('input', syncReason); syncReason();

      // ---- Date constraints + KPIs
      function syncDateMinMax(){
        const t = todayStr();
        s.min = t; e.min = s.value || t;
        if(e.value) s.max = e.value; else s.removeAttribute('max');
        if(s.value) e.min = s.value; else e.min = t;
      }
      function syncKPIs(){
        syncDateMinMax();
       rangeInfo.textContent = (s.value && e.value)
  ? ('Khoảng: ' + s.value + ' → ' + e.value)
  : 'Chưa chọn khoảng ngày';
        warnInfo.style.display = 'none';
        daysInfo.style.display = 'none';
        if(s.value && e.value){
          const sd = parse(s.value), ed = parse(e.value);
          if(ed < sd){
            warnInfo.style.display = 'inline-flex';
          }else{
            daysInfo.style.display = 'inline-flex';
           daysInfo.textContent = 'Số ngày (bao gồm 2 đầu): ' + diffDaysInc(sd, ed);

          }
        }
      }
      s.addEventListener('change', syncKPIs);
      e.addEventListener('change', syncKPIs);
      syncKPIs();

      // ---- Quick chips
      $$('.chip').forEach(ch=>{
        ch.addEventListener('click', ()=>{
          const t = new Date();
          const name = ch.dataset.quick;
          if(name==='today'){
            const d = todayStr(t);
            s.value = d; e.value = d;
          }else if(name==='tomorrow'){
            const tm = new Date(t.getTime()+86400000);
            const d = todayStr(tm);
            s.value = d; e.value = d;
          }else if(name==='next3'){
            const d1 = todayStr(t);
            const t3 = new Date(t.getTime()+2*86400000);
            const d3 = todayStr(t3);
            s.value = d1; e.value = d3;
          }else if(name==='clear'){
            s.value = ''; e.value = '';
          }
          syncKPIs();
        });
      });

      // ---- Local draft
      const KEY = 'leave_form_draft_v1';
      function saveDraft(){
        const data = { title:title.value, reason:reason.value, s:s.value, e:e.value };
        localStorage.setItem(KEY, JSON.stringify(data));
        showToast('Đã lưu nháp trên máy.');
      }
      function loadDraft(){
        try{
          const raw = localStorage.getItem(KEY); if(!raw) return;
          const d = JSON.parse(raw);
          if(d.title!=null) title.value = d.title;
          if(d.reason!=null) reason.value = d.reason;
          if(d.s!=null) s.value = d.s;
          if(d.e!=null) e.value = d.e;
          syncReason(); syncKPIs();
          showToast('Đã khôi phục bản nháp.');
        }catch(_){}
      }
      function clearDraft(){
        localStorage.removeItem(KEY);
        showToast('Đã xóa bản nháp.');
      }
      saveDraftBtn.addEventListener('click', saveDraft);
      clearDraftBtn.addEventListener('click', clearDraft);
      loadDraft();

      // ---- Preview
      function openPreview(){
        // Basic validate
        const errs = [];
        if(reason.value.trim().length < 20) errs.push('Lý do tối thiểu 20 ký tự.');
        if(!s.value || !e.value) errs.push('Vui lòng chọn đầy đủ Từ ngày / Đến ngày.');
        if(s.value && e.value && parse(e.value) < parse(s.value)) errs.push('Khoảng ngày không hợp lệ.');
        if(errs.length){
          showToast(errs[0]);
          reason.focus();
          return;
        }
        // Fill grid
        previewGrid.innerHTML = '';
        const rows = [
          ['Tiêu đề', title.value || '(trống)'],
          ['Lý do', reason.value.trim()],
          ['Từ ngày', s.value],
          ['Đến ngày', e.value],
          ['Số ngày', s.value && e.value ? diffDaysInc(parse(s.value), parse(e.value)) : '-'],
          ['Trạng thái sau khi gửi', 'Inprogress']
        ];
        rows.forEach(([k,v])=>{
          const kdiv = document.createElement('div'); kdiv.textContent = k;
          const vdiv = document.createElement('div'); vdiv.textContent = v;
          previewGrid.appendChild(kdiv); previewGrid.appendChild(vdiv);
        });
        modal.classList.add('open');
      }
      function closeModal(){ modal.classList.remove('open'); }
      previewBtn.addEventListener('click', openPreview);
      closePreview.addEventListener('click', closeModal);
      modal.addEventListener('click', (ev)=>{ if(ev.target===modal) closeModal(); });

      // ---- Submit handling
      function canSubmit(){
        if(!form.reportValidity){ // Safari fallback
          if(!reason.value.trim() || !s.value || !e.value) return false;
        }
        if(reason.value.trim().length < 20) return false;
        if(parse(e.value) < parse(s.value)) return false;
        return true;
      }
      form.addEventListener('submit', (ev)=>{
        if(!canSubmit()){
          ev.preventDefault();
          showToast('Vui lòng kiểm tra lại thông tin.');
          return;
        }
        setLoading(submitBtn, true);
        saveDraft(); // lưu 1 phát trước khi gửi
      });
      confirmSubmit.addEventListener('click', ()=>{
        closeModal();
        if(canSubmit()){
          setLoading(submitBtn, true);
          form.submit();
        }else{
          showToast('Vui lòng kiểm tra lại thông tin.');
        }
      });

      // ---- Keyboard shortcuts
      document.addEventListener('keydown', (eKey)=>{
        if((eKey.ctrlKey || eKey.metaKey) && eKey.key.toLowerCase()==='enter'){
          eKey.preventDefault();
          if(!modal.classList.contains('open')) openPreview();
          else confirmSubmit.click();
        }
      });

      // ---- Initial sync
      (function init(){
        // Giới hạn min theo hôm nay
        const t = todayStr(); s.min = t; e.min = t;
        // Nếu chỉ có start (vd server trả lại sau lỗi), set end = start
        if(s.value && !e.value){ e.value = s.value; }
        syncKPIs(); syncReason();
      })();
    })();
  </script>
</body>
</html>
