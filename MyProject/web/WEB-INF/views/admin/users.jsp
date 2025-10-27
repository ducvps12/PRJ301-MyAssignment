<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@include file="/WEB-INF/views/common/_taglibs.jsp"%>
<%@include file="/WEB-INF/views/common/_header.jsp"%>

<c:set var="cp" value="${pageContext.request.contextPath}" />

<div class="container users-page">
  <!-- Page Header / Toolbar -->
  <div class="page-head">
    <div class="titles">
      <h1>Quản lý Người dùng</h1>
      <p class="subtitle">Tìm kiếm, lọc, sắp xếp, thao tác nhanh – tất cả trong một.</p>
    </div>
    <div class="actions">
      <a href="${cp}/admin/users/create" class="btn pri" title="Thêm người dùng (Alt+N)">+ Thêm</a>
      <button id="btnExportCsv" class="btn" title="Xuất CSV (Alt+E)">Xuất CSV</button>
      <div class="divider"></div>
      <button id="btnDensity" class="btn" title="Đổi mật độ hiển thị">Mật độ</button>
      <button id="btnTheme" class="btn" title="Đổi theme (Alt+T)">Theme</button>
    </div>
  </div>

  <!-- Smart Filters -->
  <form id="filterForm" method="get" action="${cp}/admin/users" class="card filters" role="search" aria-label="Bộ lọc người dùng">
    <div class="grid">
      <div class="field">
        <label for="q">Từ khóa</label>
        <div class="input-wrap">
          <input id="q" class="input" type="text" name="q" value="${fn:escapeXml(param.q)}"
                 placeholder="Tìm theo tên, email, username… (phím / để focus)" />
          <button class="ghost clear" type="button" id="btnClear" aria-label="Xóa từ khóa">&times;</button>
        </div>
      </div>

      <div class="field">
        <label for="status">Trạng thái</label>
        <select id="status" name="status" class="input">
          <option value="">-- Tất cả --</option>
          <option value="ACTIVE"   ${param.status == 'ACTIVE'   ? 'selected':''}>ACTIVE</option>
          <option value="INACTIVE" ${param.status == 'INACTIVE' ? 'selected':''}>INACTIVE</option>
        </select>
      </div>

      <div class="field">
        <label for="size">Hiển thị</label>
        <select id="size" name="size" class="input">
          <c:set var="ps" value="${page.pageSize}" />
          <option value="10"  ${ps==10  ? 'selected':''}>10 / trang</option>
          <option value="20"  ${ps==20  ? 'selected':''}>20 / trang</option>
          <option value="50"  ${ps==50  ? 'selected':''}>50 / trang</option>
          <option value="100" ${ps==100 ? 'selected':''}>100 / trang</option>
        </select>
      </div>

      <div class="field">
        <label>&nbsp;</label>
        <div class="btn-row">
          <button class="btn pri" type="submit" id="btnFilter">Lọc</button>
          <button class="btn ghost" type="button" id="btnReset">Đặt lại</button>
          <label class="chk">
            <input type="checkbox" id="autoApply" />
            <span>Tự động áp dụng</span>
          </label>
        </div>
      </div>
    </div>
  </form>

  <!-- Bulk Actions (appear when rows selected) -->
  <div id="bulkBar" class="bulk hidden" aria-live="polite">
    <div class="left">
      <strong id="bulkCount">0</strong> mục đã chọn
    </div>
    <div class="right">
      <form id="bulkForm" method="post" action="${cp}/admin/users/bulk" class="inline">
        <input type="hidden" name="csrf" value="${csrf}">
        <input type="hidden" name="action" id="bulkActionInput">
        <div id="bulkIds"></div>
        <button class="btn small" type="button" data-bulk="activate">Kích hoạt</button>
        <button class="btn small" type="button" data-bulk="deactivate">Vô hiệu</button>
        <button class="btn small danger" type="button" data-bulk="resetpw">Reset PW</button>
      </form>
      <button class="btn ghost small" id="bulkClear">Bỏ chọn</button>
    </div>
  </div>

  <!-- Table Card -->
  <div class="card table-card">
    <div class="card-header">
      <div class="card-title">Danh sách người dùng</div>
      <div class="card-tools">
        <label class="chk">
          <input type="checkbox" id="toggleSticky" />
          <span>Sticky header</span>
        </label>
        <label class="chk">
          <input type="checkbox" id="toggleCompact" />
          <span>Compact</span>
        </label>
      </div>
    </div>

    <div class="table-wrap" id="tableWrap" tabindex="0" aria-label="Bảng người dùng">
      <table class="table" id="usersTable">
        <thead>
          <tr>
            <th class="sel"><input type="checkbox" id="checkAll" aria-label="Chọn tất cả"></th>
            <th data-sort="idx" class="sortable">#</th>
            <th data-sort="fullName" class="sortable">Họ tên</th>
            <th data-sort="email" class="sortable">Email</th>
            <th data-sort="username" class="sortable">Username</th>
            <th data-sort="role" class="sortable">Role</th>
            <th data-sort="department" class="sortable">Phòng ban</th>
            <th data-sort="status" class="sortable">Trạng thái</th>
            <th style="width:280px;">Thao tác</th>
          </tr>
        </thead>
        <tbody id="tbody">
        <c:forEach var="u" items="${page.data}" varStatus="vs">
          <tr data-id="${u.id}">
            <td class="sel"><input type="checkbox" class="rowChk" aria-label="Chọn hàng"></td>
            <td data-key="idx">${(page.pageIndex-1)*page.pageSize + vs.index + 1}</td>
            <td data-key="fullName">
              <div class="name-cell">
                <span class="avatar" aria-hidden="true">${fn:substring(u.fullName,0,1)}</span>
                <span class="name">${u.fullName}</span>
              </div>
            </td>
            <td data-key="email">
              <span class="mono email">${u.email}</span>
              <button class="icon-btn copy" data-copy="${u.email}" title="Copy email" aria-label="Copy email">⧉</button>
            </td>
            <td data-key="username" class="mono">${u.username}</td>
            <td data-key="role"><span class="chip">${u.role}</span></td>
            <td data-key="department"><span class="chip ghost">${u.department}</span></td>
            <td data-key="status">
              <span class="badge ${u.status}">${u.status}</span>
            </td>
            <td class="actions">
              <a class="btn small" href="${cp}/admin/users/detail?id=${u.id}">Xem</a>
              <a class="btn small" href="${cp}/admin/users/edit?id=${u.id}">Sửa</a>

              <!-- Toggle status -->
              <form method="post" action="${cp}/admin/users/toggle" class="inline need-confirm" data-confirm="Xác nhận thay đổi trạng thái?">
                <input type="hidden" name="csrf" value="${csrf}">
                <input type="hidden" name="id" value="${u.id}">
                <input type="hidden" name="q" value="${fn:escapeXml(param.q)}">
                <input type="hidden" name="status" value="${param.status}">
                <input type="hidden" name="page" value="${page.pageIndex}">
                <input type="hidden" name="size" value="${page.pageSize}">
                <button class="btn small">
                  <c:choose>
                    <c:when test="${u.status=='ACTIVE'}">Vô hiệu</c:when>
                    <c:otherwise>Kích hoạt</c:otherwise>
                  </c:choose>
                </button>
              </form>

              <!-- Reset password -->
              <form method="post" action="${cp}/admin/users/resetpw" class="inline need-confirm" data-confirm="Reset mật khẩu về 123456?">
                <input type="hidden" name="csrf" value="${csrf}">
                <input type="hidden" name="id" value="${u.id}">
                <input type="hidden" name="q" value="${fn:escapeXml(param.q)}">
                <input type="hidden" name="status" value="${param.status}">
                <input type="hidden" name="page" value="${page.pageIndex}">
                <input type="hidden" name="size" value="${page.pageSize}">
                <button class="btn small danger">Reset PW</button>
              </form>
            </td>
          </tr>
        </c:forEach>

        <c:if test="${empty page.data}">
          <tr class="empty">
            <td colspan="9">
              <div class="empty-state">
                <div class="art">🗂️</div>
                <div class="msg">Không có dữ liệu phù hợp bộ lọc hiện tại.</div>
                <button class="btn ghost" type="button" id="btnEmptyReset">Xóa bộ lọc</button>
              </div>
            </td>
          </tr>
        </c:if>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <div class="paging">
      <div class="left">
        Trang <strong>${page.pageIndex}</strong>/<strong>${page.totalPages}</strong> —
        Tổng <strong>${page.totalItems}</strong> người dùng
      </div>
      <div class="mid">
        <c:forEach begin="1" end="${page.totalPages}" var="p">
          <a class="btn small ${p==page.pageIndex?'active':''}"
             href="${cp}/admin/users?page=${p}&size=${page.pageSize}&q=${fn:escapeXml(param.q)}&status=${param.status}">
            ${p}
          </a>
        </c:forEach>
      </div>
      <div class="right">
        <a class="btn small" href="${cp}/admin/users?page=1&size=${page.pageSize}&q=${fn:escapeXml(param.q)}&status=${param.status}">« Đầu</a>
        <a class="btn small" href="${cp}/admin/users?page=${page.pageIndex>1?page.pageIndex-1:1}&size=${page.pageSize}&q=${fn:escapeXml(param.q)}&status=${param.status}">‹ Trước</a>
        <a class="btn small" href="${cp}/admin/users?page=${page.pageIndex<page.totalPages?page.pageIndex+1:page.totalPages}&size=${page.pageSize}&q=${fn:escapeXml(param.q)}&status=${param.status}">Sau ›</a>
        <a class="btn small" href="${cp}/admin/users?page=${page.totalPages}&size=${page.pageSize}&q=${fn:escapeXml(param.q)}&status=${param.status}">Cuối »</a>
      </div>
    </div>
  </div>
</div>

<!-- Toast & Modal (shared) -->
<div id="toast" class="toast" role="status" aria-live="polite"></div>
<dialog id="confirmDlg" class="confirm">
  <form method="dialog" class="confirm-body">
    <div class="confirm-title">Xác nhận</div>
    <div class="confirm-msg" id="confirmMsg">Bạn có chắc không?</div>
    <div class="confirm-actions">
      <button value="cancel" class="btn ghost">Hủy</button>
      <button value="ok" class="btn pri">Đồng ý</button>
    </div>
  </form>
</dialog>

<style>
/* ====== Design tokens ====== */
:root{
  --bg:#0b0f14; --card:#10161d; --card-2:#0d131a;
  --tx:#ecf2f8; --muted:#9fb0c3; --bd:#1f2a36;
  --pri:#2aa0ff; --pri2:#6fc3ff;
  --ok:#22c55e; --warn:#f59e0b; --danger:#ef4444;
  --ring:0 0 0 2px rgb(42 160 255 / .25);
  --shadow: 0 10px 30px rgb(0 0 0 / .35);
  --chip:#13202c; --chipg:#0f1720;
  --mono: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", monospace;
}
@media (prefers-color-scheme: light){
  :root{ --bg:#f7f7fb; --card:#fff; --card-2:#f9fafb; --tx:#111827; --muted:#6b7280; --bd:#e5e7eb; --chip:#eef2f7; --chipg:#f5f7fb; --shadow: 0 12px 30px rgba(17,24,39,.08); }
}
html,body{margin:0}
body{font:14px/1.55 system-ui,Segoe UI,Roboto,Helvetica,Arial;color:var(--tx);background:linear-gradient(120deg,rgba(42,160,255,.06),transparent 40%) , var(--bg);}

/* Container */
.container.users-page{max-width:1180px;margin:24px auto;padding:0 16px}

/* Head */
.page-head{display:flex;align-items:center;justify-content:space-between;gap:12px;margin:4px 0 16px}
.page-head .titles h1{font-size:22px;margin:0}
.page-head .subtitle{margin:2px 0 0 0;color:var(--muted)}
.page-head .actions{display:flex;align-items:center;gap:8px}
.divider{width:1px;height:28px;background:var(--bd);opacity:.6}

/* Controls */
.card{background:var(--card);border:1px solid var(--bd);border-radius:14px;box-shadow:var(--shadow)}
.filters{padding:14px}
.filters .grid{display:grid;grid-template-columns:1.2fr .8fr .6fr .8fr;gap:12px}
.field label{display:block;font-size:12px;color:var(--muted);margin:0 0 6px}
.input{width:100%;padding:10px 12px;border:1px solid var(--bd);border-radius:10px;background:var(--card-2);color:var(--tx);outline:none}
.input:focus{box-shadow:var(--ring);border-color:transparent}
.input-wrap{position:relative}
.input-wrap .clear{position:absolute;right:6px;top:50%;transform:translateY(-50%);border:0;background:transparent;font-size:18px;line-height:1;cursor:pointer;color:var(--muted)}
.btn{display:inline-flex;align-items:center;gap:6px;padding:8px 12px;border:1px solid var(--bd);border-radius:10px;background:var(--card-2);color:var(--tx);text-decoration:none;cursor:pointer}
.btn:hover{filter:brightness(1.08)}
.btn.small{font-size:12px;padding:6px 10px}
.btn.ghost{background:transparent}
.btn.pri{background:linear-gradient(180deg,var(--pri),var(--pri2));border-color:transparent;color:#fff}
.btn.danger{border-color:rgba(239,68,68,.35);background:rgba(239,68,68,.08)}

/* Chips / badges */
.chip{display:inline-block;padding:3px 8px;border-radius:999px;background:var(--chip);border:1px solid var(--bd)}
.chip.ghost{background:var(--chipg)}
.badge{display:inline-block;padding:3px 10px;border-radius:999px;border:1px solid var(--bd)}
.badge.ACTIVE{background:rgba(34,197,94,.12);border-color:rgba(34,197,94,.45)}
.badge.INACTIVE{background:rgba(239,68,68,.12);border-color:rgba(239,68,68,.45)}

/* Table */
.table-card .card-header{display:flex;justify-content:space-between;align-items:center;padding:10px 12px;border-bottom:1px solid var(--bd)}
.table-wrap{overflow:auto;max-height:62vh}
.table{width:100%;border-collapse:separate;border-spacing:0}
.table thead th{position:sticky;top:0;background:var(--card);z-index:2;border-bottom:1px solid var(--bd);padding:10px 10px;text-align:left;font-weight:600}
.table td{border-bottom:1px solid var(--bd);padding:10px}
.table tr:hover{background:rgba(96,165,250,.08)}
.table th.sel,.table td.sel{width:32px;text-align:center}
.name-cell{display:flex;align-items:center;gap:10px}
.avatar{width:24px;height:24px;border-radius:8px;background:linear-gradient(180deg,var(--pri2),var(--pri));display:inline-grid;place-items:center;font-weight:700}
.mono{font-family:var(--mono)}
.actions{display:flex;flex-wrap:wrap;gap:6px}
.sortable{cursor:pointer;user-select:none}
.sortable::after{content:" ⬍";opacity:.35}

/* Empty state */
.empty-state{padding:24px;text-align:center;color:var(--muted)}
.empty-state .art{font-size:28px}
.empty-state .msg{margin:8px 0 12px}

/* Paging */
.paging{display:grid;grid-template-columns:1fr auto 1fr;align-items:center;gap:8px;padding:12px;border-top:1px solid var(--bd)}
.paging .left{justify-self:start;color:var(--muted)}
.paging .mid{justify-self:center;display:flex;flex-wrap:wrap;gap:6px}
.paging .right{justify-self:end;display:flex;gap:6px}

/* Bulk bar */
.bulk{position:sticky;bottom:12px;display:flex;justify-content:space-between;align-items:center;gap:8px;padding:10px 12px;background:var(--card);border:1px solid var(--bd);border-radius:12px;box-shadow:var(--shadow);margin:12px 0}
.bulk.hidden{display:none}

/* Toast */
.toast{position:fixed;right:16px;bottom:16px;max-width:360px;background:var(--card);border:1px solid var(--bd);padding:10px 12px;border-radius:12px;box-shadow:var(--shadow);opacity:0;transform:translateY(8px);pointer-events:none;transition:.2s}

/* Confirm dialog */
.confirm{border:0;border-radius:14px;padding:0;background:transparent}
.confirm::backdrop{background:rgba(0,0,0,.35)}
.confirm-body{background:var(--card);border:1px solid var(--bd);border-radius:14px;min-width:320px;max-width:94vw;padding:14px}
.confirm-title{font-weight:700;margin-bottom:6px}
.confirm-actions{display:flex;justify-content:flex-end;gap:8px;margin-top:12px}

/* Compact mode */
.compact .table td{padding:6px 8px}
.compact .table thead th{padding:8px}

/* Responsive */
@media (max-width:900px){
  .filters .grid{grid-template-columns:1fr 1fr}
  .page-head{flex-direction:column;align-items:flex-start}
  .actions{width:100%;flex-wrap:wrap}
  .actions .divider{display:none}
  .table-wrap{max-height:none}
}

/* Light theme helper (toggle) */
:root[data-theme="light"]{
  --bg:#f7f8fb; --card:#fff; --card-2:#f9fafb; --tx:#111827; --muted:#6b7280; --bd:#e5e7eb; --shadow:0 12px 30px rgba(17,24,39,.08); --chip:#eef2f7; --chipg:#f5f7fb;
}
</style>

<script>
(() => {
  const $ = (s,root=document)=>root.querySelector(s);
  const $$ = (s,root=document)=>Array.from(root.querySelectorAll(s));
  const cp = "${cp}";
  const csrf = "${csrf}";

  const qs = new URLSearchParams(location.search);
  const filterForm = $("#filterForm");
  const q = $("#q"), status = $("#status"), size = $("#size");
  const btnClear = $("#btnClear"), btnReset = $("#btnReset");
  const autoApply = $("#autoApply");
  const btnDensity = $("#btnDensity"), btnTheme = $("#btnTheme");
  const table = $("#usersTable"), tbody = $("#tbody");
  const checkAll = $("#checkAll");
  const bulkBar = $("#bulkBar"), bulkCount = $("#bulkCount"), bulkIds = $("#bulkIds");
  const bulkForm = $("#bulkForm"), bulkClear = $("#bulkClear");
  const toast = $("#toast");
  const confirmDlg = $("#confirmDlg"), confirmMsg = $("#confirmMsg");
  const toggleSticky = $("#toggleSticky"), toggleCompact = $("#toggleCompact");
  const btnExportCsv = $("#btnExportCsv");
  const btnEmptyReset = $("#btnEmptyReset");

  // ===== Preferences (persist) =====
  const pref = {
    get k(){ return "users.pref"; },
    load(){
      try{ return JSON.parse(localStorage.getItem(this.k))||{} }catch{ return {} }
    },
    save(obj){ localStorage.setItem(this.k, JSON.stringify(obj)); }
  };
  const pf = Object.assign({theme:null, density:"normal", auto:false, sticky:true}, pref.load());
  if(pf.theme==="light") document.documentElement.setAttribute("data-theme","light");
  if(pf.density==="compact") document.body.classList.add("compact");
  autoApply.checked = !!pf.auto;
  toggleSticky.checked = pf.sticky;
  toggleCompact.checked = pf.density==="compact";

  // Sticky head toggle
  toggleSticky.addEventListener("change", () => {
    const thead = table.tHead;
    $$("th", thead).forEach(th => th.style.position = toggleSticky.checked ? "sticky":"static");
    pf.sticky = toggleSticky.checked; pref.save(pf);
  });
  // Compact toggle
  toggleCompact.addEventListener("change", () => {
    document.body.classList.toggle("compact", toggleCompact.checked);
    pf.density = toggleCompact.checked ? "compact":"normal"; pref.save(pf);
  });

  // ===== Auto apply (debounced submit) =====
  let t=null;
  function debouncedSubmit(){
    if(!autoApply.checked) return;
    clearTimeout(t); t=setTimeout(()=>filterForm.requestSubmit(), 420);
  }
  q.addEventListener("input", debouncedSubmit);
  status.addEventListener("change", debouncedSubmit);
  size.addEventListener("change", ()=>filterForm.requestSubmit());
  $("#btnFilter").addEventListener("click", ()=>pf.auto = autoApply.checked, {once:true});

  // Clear keyword
  btnClear.addEventListener("click", ()=>{ q.value=""; debouncedSubmit(); q.focus(); });
  // Reset filters to default
  function resetFilters(){
    q.value=""; status.value=""; size.value="20";
    autoApply.checked = pf.auto ?? false;
    location.href = cp + "/admin/users";
  }
  btnReset.addEventListener("click", resetFilters);
  btnEmptyReset?.addEventListener("click", resetFilters);

  // Save autoApply preference on toggle
  autoApply.addEventListener("change", ()=>{ pf.auto = autoApply.checked; pref.save(pf); });

  // ===== Keyboard shortcuts =====
  // / focus search, Alt+T theme, Alt+E export, Alt+N new
  window.addEventListener("keydown", (e)=>{
    if(e.key==="/" && document.activeElement.tagName!=="INPUT" && document.activeElement.tagName!=="TEXTAREA"){
      e.preventDefault(); q.focus(); q.select();
    }
    if(e.altKey && e.key.toLowerCase()==="t"){ e.preventDefault(); toggleTheme(); }
    if(e.altKey && e.key.toLowerCase()==="e"){ e.preventDefault(); exportCSV(); }
    if(e.altKey && e.key.toLowerCase()==="n"){ e.preventDefault(); location.href = cp+"/admin/users/create"; }
  });

  // ===== Sort (client-side on current page) =====
  let sortState = { key:null, dir:1 };
  $$(".sortable", table.tHead).forEach(th=>{
    th.addEventListener("click", ()=>{
      const key = th.dataset.sort;
      if(sortState.key===key) sortState.dir *= -1; else { sortState.key=key; sortState.dir=1; }
      $$(".sortable", table.tHead).forEach(t=>t.classList.remove("asc","desc"));
      th.classList.add(sortState.dir===1?"asc":"desc");
      sortRows(key, sortState.dir);
    });
  });
  function sortRows(key, dir){
    const rows = $$("#tbody tr").filter(tr=>!tr.classList.contains("empty"));
    rows.sort((a,b)=>{
      const va = a.querySelector(`[data-key="${key}"]`)?.textContent.trim() ?? "";
      const vb = b.querySelector(`[data-key="${key}"]`)?.textContent.trim() ?? "";
      const na = +va, nb = +vb;
      const isNum = !isNaN(na) && !isNaN(nb);
      return (isNum ? (na-nb) : va.localeCompare(vb)) * dir;
    });
    rows.forEach(r=>tbody.appendChild(r));
  }
// ===== Row selection & bulk =====
function updateBulkBar(){
  const chks = $$(".rowChk:checked");
  const ids = chks.map(chk=>chk.closest("tr").dataset.id);
  bulkCount.textContent = ids.length;
  bulkIds.innerHTML = "";
  ids.forEach(id=>{
    const inp = document.createElement("input");
    inp.type="hidden"; inp.name="ids"; inp.value=id;
    bulkIds.appendChild(inp);
  });
  bulkBar.classList.toggle("hidden", ids.length===0);
}
checkAll.addEventListener("change", ()=>{
  $$(".rowChk").forEach(chk=>{chk.checked=checkAll.checked});
  updateBulkBar();
});
$$(".rowChk").forEach(chk=>chk.addEventListener("change", ()=>{
  if(!chk.checked) checkAll.checked=false;
  updateBulkBar();
}));
bulkClear.addEventListener("click", ()=>{
  checkAll.checked=false; $$(".rowChk").forEach(c=>c.checked=false); updateBulkBar();
});

$$("[data-bulk]").forEach(btn=>{
  btn.addEventListener("click", async ()=>{
    const action = btn.dataset.bulk;
    const cnt = +bulkCount.textContent;
    if (cnt === 0) { toastMsg("Chưa chọn mục nào", "warn"); return; }

    // KHÔNG dùng template literal trong JSP
    let msg;
    if (action === "resetpw") {
      msg = "Reset mật khẩu " + cnt + " tài khoản về 123456?";
    } else {
      msg = (action === "activate" ? "Kích hoạt" : "Vô hiệu") + " " + cnt + " tài khoản?";
    }

    const ok = await confirmBox(msg);
    if (!ok) return;
    $("#bulkActionInput").value = action;
    bulkForm.submit();
  });
});

// ===== Confirm dialog for single forms =====
$$("form.need-confirm").forEach(f=>{
  f.addEventListener("submit", async (e)=>{
    e.preventDefault();
    const ok = await confirmBox(f.dataset.confirm || "Xác nhận thao tác?");
    if (ok) f.submit();
  });
});

// ===== Copy email buttons =====
$$(".icon-btn.copy").forEach(btn=>{
  btn.addEventListener("click", async ()=>{
    try {
      await navigator.clipboard.writeText(btn.dataset.copy);
      toastMsg("Đã copy email vào clipboard ✅");
    } catch {
      toastMsg("Không thể copy, hãy thử thủ công.", "warn");
    }
  });
});

// ===== Export CSV =====
function exportCSV(){
  const rows = $$("#tbody tr").filter(tr=>!tr.classList.contains("empty"));
  if (rows.length === 0) return toastMsg("Không có dữ liệu để xuất.", "warn");

  const headers = ["#", "Họ tên", "Email", "Username", "Role", "Phòng ban", "Trạng thái"];
  const data = rows.map(tr=>{
    const get = function(k){
      const el = tr.querySelector('[data-key="' + k + '"]');
      return (el && el.innerText ? el.innerText.trim() : "");
    };
    return [
      (tr.querySelector('[data-key="idx"]')?.innerText.trim() || ""),
      get("fullName"), get("email"), get("username"), get("role"), get("department"), get("status")
    ];
  });

  const csv = [headers].concat(data)
    .map(function(r){
      return r.map(function(s){
        s = (s || "").replaceAll('"','""');
        return '"' + s + '"';
      }).join(",");
    })
    .join("\n");

  const blob = new Blob([csv], {type:"text/csv;charset=utf-8;"});
  const a = document.createElement("a");
  a.href = URL.createObjectURL(blob);
  a.download = "users_" + new Date().toISOString().slice(0,19).replaceAll(":", "-") + ".csv";
  a.click();
}
btnExportCsv.addEventListener("click", exportCSV);

  // ===== Theme / Density toggles =====
  function toggleTheme(){
    const cur = document.documentElement.getAttribute("data-theme");
    const next = cur==="light" ? null : "light";
    if(next) document.documentElement.setAttribute("data-theme","light");
    else document.documentElement.removeAttribute("data-theme");
    pf.theme = next || null; pref.save(pf);
  }
  btnTheme.addEventListener("click", toggleTheme);
  btnDensity.addEventListener("click", ()=>{
    const now = document.body.classList.toggle("compact");
    toggleCompact.checked = now;
    pf.density = now?"compact":"normal"; pref.save(pf);
  });

  // ===== Toast helper =====
  let toastTimer=null;
  function toastMsg(msg, type){
    toast.textContent = msg;
    toast.style.borderColor = type==="warn" ? "rgba(245,158,11,.35)"
                         : type==="error" ? "rgba(239,68,68,.35)"
                         : "var(--bd)";
    toast.style.opacity = 1; toast.style.transform="translateY(0)";
    clearTimeout(toastTimer);
    toastTimer = setTimeout(()=>{ toast.style.opacity=0; toast.style.transform="translateY(8px)"; }, 2200);
  }

  // ===== Confirm helper (native <dialog>) =====
  function confirmBox(message){
    return new Promise(res=>{
      confirmMsg.textContent = message;
      confirmDlg.showModal();
      confirmDlg.addEventListener("close", function onClose(){
        confirmDlg.removeEventListener("close", onClose);
        res(confirmDlg.returnValue==="ok");
      });
    });
  }

  // Remember last scroll in table (nice touch)
  const scrollKey = "users.table.scrollLeft";
  const tw = $("#tableWrap");
  tw.scrollLeft = +(sessionStorage.getItem(scrollKey)||0);
  tw.addEventListener("scroll", ()=>sessionStorage.setItem(scrollKey, tw.scrollLeft));

  // Accessibility & small touches
  $("#tableWrap").addEventListener("keydown", (e)=>{
    const rows = $$("#tbody tr");
    const idx = rows.indexOf(document.activeElement.closest("tr"));
    if(e.key==="j" && idx>=0 && idx<rows.length-1){ rows[idx+1].querySelector(".rowChk").focus(); }
    if(e.key==="k" && idx>0){ rows[idx-1].querySelector(".rowChk").focus(); }
  });
})();
</script>

<%@include file="/WEB-INF/views/common/_footer.jsp"%>
