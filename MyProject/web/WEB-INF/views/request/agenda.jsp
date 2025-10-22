<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<title>Agenda – Lịch nghỉ phòng ban</title>
<meta name="viewport" content="width=device-width, initial-scale=1">
<style>
  :root{
    --b:#e5e7eb; --bg:#f7f7f8; --muted:#6b7280; --ok:#10b981; --no:#ef4444; --pend:#d97706; --pri:#2563eb;
    --head:#f8fafc; --weekend:#fff7ed; --today:#e0f2fe;
  }
  *{box-sizing:border-box}
  body{font-family:system-ui,Segoe UI,Roboto,Helvetica,Arial,sans-serif;background:var(--bg);margin:0}
  .wrap{max-width:1200px;margin:28px auto;padding:0 16px}
  h2{margin:0 0 12px 0}
  .toolbar{display:flex;flex-wrap:wrap;gap:10px;align-items:center;margin-bottom:12px}
  .toolbar .box{background:#fff;border:1px solid var(--b);border-radius:12px;padding:12px;display:flex;gap:8px;align-items:center}
  .btn{padding:8px 12px;border:1px solid var(--b);border-radius:10px;background:#fff;cursor:pointer;text-decoration:none;color:#111}
  .btn-primary{background:var(--pri);color:#fff;border-color:var(--pri)}
  input,select{padding:8px 10px;border:1px solid var(--b);border-radius:10px;background:#fff}
  .card{background:#fff;border:1px solid var(--b);border-radius:12px;overflow:auto}
  table{border-collapse:separate;border-spacing:0;width:max-content;min-width:100%}
  th,td{border-top:1px solid var(--b);border-left:1px solid var(--b);padding:10px 12px;text-align:center;white-space:nowrap}
  th:last-child, td:last-child{border-right:1px solid var(--b)}
  tr:last-child td{border-bottom:1px solid var(--b)}
  thead th{position:sticky;top:0;background:var(--head);z-index:2}
  .col-name{position:sticky;left:0;background:#fff;z-index:1;text-align:left;font-weight:600}
  .weekend{background:var(--weekend)}
  .today{background:var(--today)}
  .ok{color:var(--ok);font-weight:600}
  .no{color:var(--no);font-weight:700}
  .pend{color:var(--pend);font-weight:700}
  tfoot td{font-weight:700;background:#f8fafc}
  .legend{display:flex;gap:12px;color:var(--muted);font-size:14px;flex-wrap:wrap}
  .muted{color:var(--muted)}
  .badge{display:inline-block;padding:2px 8px;border-radius:999px;border:1px solid var(--b);font-size:12px;background:#fff}
  .hidden{display:none}
  .cap-high{background:#fee2e2 !important; color:#7f1d1d}
  .cap-warn{background:#fffbeb !important; color:#92400e}
  tbody tr:hover td{background:#f9fafb}
  td.hovercol, th.hovercol{background:#f1f5f9 !important}
  @media print{
    .toolbar{display:none}
    body{background:#fff}
    .card{border:none}
    .col-name{position:static}
    thead th{position:static}
  }
</style>
</head>
<body>
    <%@ include file="/WEB-INF/views/common/_header.jsp" %>


<div class="wrap">
  <h2>Agenda – Lịch nghỉ phòng ban</h2>

  <!-- Thanh điều khiển -->
  <div class="toolbar">
    <form class="box" method="get" action="${pageContext.request.contextPath}/request/agenda" id="rangeForm">
      <span class="muted">Khoảng:</span>
      <input type="date" name="from" value="${from}">
      <input type="date" name="to"   value="${to}">
      <button class="btn" type="submit">Xem</button>
      <button type="button" class="btn" id="prevWeek">◀ Tuần trước</button>
      <button type="button" class="btn" id="thisWeek">Hôm nay</button>
      <button type="button" class="btn" id="nextWeek">Tuần sau ▶</button>
    </form>

    <div class="box">
      <input id="search" placeholder="Tìm tên nhân viên…">
      <label style="display:flex;gap:6px;align-items:center"><input type="checkbox" id="onlyAbsent"> Chỉ hiện người đang nghỉ</label>
      <label style="display:flex;gap:6px;align-items:center"><input type="checkbox" id="onlyChanged"> Chỉ người có biến động</label>
      <label style="display:flex;gap:6px;align-items:center"><input type="checkbox" id="toggleWeekend" checked> Hiện cuối tuần</label>
    </div>

    <div class="box">
      <button class="btn" id="btnCsv" type="button">Xuất CSV</button>
      <button class="btn" type="button" id="btnCopy">Copy link bộ lọc</button>
      <button class="btn" onclick="window.print()">In</button>
      <a class="btn" href="${pageContext.request.contextPath}/request/list">Quay lại danh sách</a>
    </div>
  </div>

  <div class="legend" style="margin-bottom:8px">
    <span><span class="ok">✅</span> đi làm</span>
    <span><span class="pend">⏳</span> nghỉ (chờ duyệt)</span>
    <span><span class="no">❌</span> nghỉ (đã duyệt)</span>
    <c:if test="${not empty sessionScope.department}">
      <span class="badge">Phòng ban: ${sessionScope.department}</span>
    </c:if>
    <c:if test="${not empty from && not empty to}">
      <span class="muted">
        Từ <b><fmt:formatDate value="${java.sql.Date.valueOf(from)}" pattern="dd/MM/yyyy"/></b>
        đến <b><fmt:formatDate value="${java.sql.Date.valueOf(to)}" pattern="dd/MM/yyyy"/></b>
      </span>
    </c:if>
  </div>

  <!-- Bảng agenda -->
  <div class="card" id="tableWrap">
    <table id="agendaTbl" aria-describedby="agenda table">
      <thead>
      <tr>
        <th class="col-name">Nhân viên</th>
        <c:forEach var="d" items="${days}">
          <th class="${d.dayOfWeek.value >= 6 ? 'weekend' : ''}" data-date="${d}" data-dow="${d.dayOfWeek.value}">
            <div style="display:flex;flex-direction:column;gap:2px">
              <span class="muted">
                <c:choose>
                  <c:when test="${d.dayOfWeek.value==1}">Mon</c:when>
                  <c:when test="${d.dayOfWeek.value==2}">Tue</c:when>
                  <c:when test="${d.dayOfWeek.value==3}">Wed</c:when>
                  <c:when test="${d.dayOfWeek.value==4}">Thu</c:when>
                  <c:when test="${d.dayOfWeek.value==5}">Fri</c:when>
                  <c:when test="${d.dayOfWeek.value==6}">Sat</c:when>
                  <c:otherwise>Sun</c:otherwise>
                </c:choose>
              </span>
              <b><fmt:formatDate value="${java.sql.Date.valueOf(d)}" pattern="dd/MM"/></b>
            </div>
          </th>
        </c:forEach>
      </tr>
      </thead>

      <tbody>
      <c:forEach var="u" items="${users}">
        <c:set var="uid" value="${u.id}" />
        <tr data-user="${fn:toLowerCase(u.fullName)}">
          <td class="col-name">
            ${u.fullName} <span class="muted"><c:if test="${not empty uid}">(#${uid})</c:if></span>
          </td>

          <c:forEach var="d" items="${days}">
            <%-- Ưu tiên bản đồ chi tiết nếu có: absentApproved / absentPending --%>
            <c:set var="isApproved" value="${absentApproved[uid] != null and absentApproved[uid].contains(d)}"/>
            <c:set var="isPending"  value="${absentPending[uid]  != null and absentPending[uid].contains(d)}"/>
            <%-- fallback khi chỉ có absent: coi như approved --%>
            <c:if test="${isApproved == false and isPending == false}">
              <c:set var="isApproved" value="${absent[uid] != null and absent[uid].contains(d)}"/>
            </c:if>

            <td class="${d.dayOfWeek.value >= 6 ? 'weekend' : ''}"
                data-absent="${(isApproved or isPending) ? '1' : '0'}"
                data-pending="${isPending ? '1' : '0'}"
                title="<fmt:formatDate value='${java.sql.Date.valueOf(d)}' pattern='dd/MM/yyyy'/>">
              <c:choose>
                <c:when test="${isPending}"><span class="pend">⏳</span></c:when>
                <c:when test="${isApproved}"><span class="no">❌</span></c:when>
                <c:otherwise><span class="ok">✅</span></c:otherwise>
              </c:choose>
            </td>
          </c:forEach>
        </tr>
      </c:forEach>

      <c:if test="${empty users}">
        <tr><td class="col-name" colspan="${fn:length(days)+1}" style="text-align:center;color:var(--muted);padding:28px">Không có dữ liệu người dùng.</td></tr>
      </c:if>
      </tbody>

      <tfoot>
      <tr>
        <td class="col-name">Tổng người nghỉ</td>
        <c:forEach var="d" items="${days}">
          <td data-col-total="0">0</td>
        </c:forEach>
      </tr>
      </tfoot>
    </table>
  </div>
</div>

<script>
(function(){
  const $  = s => document.querySelector(s);
  const $$ = s => Array.from(document.querySelectorAll(s));

  // ======= Range helpers
  const form   = $("#rangeForm");
  const inFrom = form.querySelector("input[name=from]");
  const inTo   = form.querySelector("input[name=to]");
  const toISO  = d => new Date(d).toISOString().slice(0,10);
  const add    = (d,n)=>{const x=new Date(d); x.setDate(x.getDate()+n); return x;};
  const weekOf = (dt)=>{
    const d = new Date(dt);
    const dow = d.getDay()==0?7:d.getDay();
    const mon = add(d,1-dow), sun = add(mon,6);
    return [toISO(mon), toISO(sun)];
  };
  const setRange = (f,t)=>{ inFrom.value=f; inTo.value=t; form.submit(); };

  $("#thisWeek").onclick = ()=>{ const [f,t] = weekOf(new Date()); setRange(f,t); };
  $("#prevWeek").onclick = ()=>{
    const f = inFrom.value ? new Date(inFrom.value) : new Date();
    const f2 = add(f,-7), t2 = add(f2,6); setRange(toISO(f2), toISO(t2));
  };
  $("#nextWeek").onclick = ()=>{
    const t = inTo.value ? new Date(inTo.value) : new Date();
    const f2 = add(t,1), t2 = add(f2,6); setRange(toISO(f2), toISO(t2));
  };

  // ======= Highlight today column (theo index nguyên thủy, vẫn đúng dù cột bị ẩn bằng display:none)
  const todayISO = new Date().toISOString().slice(0,10);
  const ths = $$("#agendaTbl thead th");
  ths.forEach((th,i)=>{
    if (th.dataset.date === todayISO){
      th.classList.add("today");
      $$("#agendaTbl tbody tr").forEach(tr=> tr.children[i]?.classList.add("today"));
      $$("#agendaTbl tfoot tr").forEach(tr=> tr.children[i]?.classList.add("today"));
    }
  });

  // ======= Hover column/row
  $$("#agendaTbl tbody td").forEach(td=>{
    td.addEventListener("mouseenter",()=>{
      const idx = Array.from(td.parentElement.children).indexOf(td);
      $$("#agendaTbl tr").forEach(tr=>{
        const c = tr.children[idx];
        if (c) c.classList.add("hovercol");
      });
    });
    td.addEventListener("mouseleave",()=> $$(".hovercol").forEach(c=>c.classList.remove("hovercol")));
  });

  // ======= Recompute totals (đếm theo các cột đang hiển thị)
  const capacity = Number("${capacityPerDay}") || 0; // optional backend
  function recompute(){
    const headThAll = Array.from(document.querySelectorAll("#agendaTbl thead th")).slice(1); // bỏ cột tên
    const bodyRows  = Array.from(document.querySelectorAll("#agendaTbl tbody tr:not(.hidden)"));
    const footTds   = Array.from(document.querySelectorAll("#agendaTbl tfoot tr td"));

    headThAll.forEach((th, idxFrom1) => {
      // idxFrom1 là index tính từ 1 (vì cột 0 là tên)
      if (getComputedStyle(th).display === "none") return;

      let total = 0;
      bodyRows.forEach(tr => {
        const cell = tr.children[idxFrom1 + 0]; // vì tr.children[0] là col-name, th ở đây đã slice(1)
        if (cell && cell.getAttribute("data-absent") === "1") total++;
      });

      const ft = footTds[idxFrom1 + 1]; // footer: col 0 là label "Tổng người nghỉ"
      if (ft){
        ft.textContent = String(total);
        ft.dataset.colTotal = total;
        ft.classList.remove("cap-high","cap-warn");
        if (capacity > 0){
          if (total >= capacity) ft.classList.add("cap-high");
          else if (total >= Math.ceil(capacity * 0.7)) ft.classList.add("cap-warn");
        }
      }
    });
  }

  // ======= Filters
  const search = $("#search");
  const onlyAbsent = $("#onlyAbsent");
  const onlyChanged = $("#onlyChanged");

  function applyFilters(){
    const term = (search.value||"").trim().toLowerCase();
    $$("#agendaTbl tbody tr").forEach(tr=>{
      const name = tr.dataset.user || "";
      const matchName = !term || name.includes(term);
      let matchAbsent = true;
      if (onlyAbsent.checked){
        matchAbsent = tr.querySelector('td[data-absent="1"]') !== null;
      }
      let matchChanged = true;
      if (onlyChanged.checked){
        matchChanged = tr.querySelector('td[data-absent="1"], td[data-pending="1"]') !== null;
      }
      tr.classList.toggle("hidden", !(matchName && matchAbsent && matchChanged));
    });
    recompute();
  }

  search.addEventListener("input",  applyFilters);
  onlyAbsent.addEventListener("change",  applyFilters);
  onlyChanged.addEventListener("change", applyFilters);

  // ======= Toggle weekend
  const toggleWeekend = $("#toggleWeekend");
  function applyWeekend(){
    const show = toggleWeekend.checked;
    $$("#agendaTbl th.weekend, #agendaTbl td.weekend").forEach(td=>{
      td.style.display = show ? "" : "none";
    });
    recompute(); // cập nhật tổng khi ẩn/hiện cuối tuần
  }
  toggleWeekend.addEventListener("change", applyWeekend);

  // ======= Export CSV (tôn trọng hàng/cột đang hiển thị)
  $("#btnCsv").onclick = ()=>{
    const table = $("#agendaTbl");
    const headCells = Array.from(table.tHead.rows[0].cells);
    // cột hiển thị (display != none)
    const visibleIdx = headCells
      .map((c,i)=>({i,disp:getComputedStyle(c).display}))
      .filter(x=>x.disp!=="none")
      .map(x=>x.i);

    const rows=[];
    // header
    rows.push(visibleIdx.map(i=> (i===0?"Nhan vien": headCells[i].innerText.trim().replace(/\n/g,' '))).join(","));
    // body
    $$("#agendaTbl tbody tr:not(.hidden)").forEach(tr=>{
      const arr = visibleIdx.map(i=>{
        const t = tr.cells[i]?.innerText.trim() || "";
        return '"' + t.replace(/"/g,'""') + '"';
      });
      rows.push(arr.join(","));
    });
    // footer
    const fcs = Array.from(table.tFoot.rows[0].cells);
    rows.push(visibleIdx.map(i=> i===0?"Tong nghi": (fcs[i]?.innerText.trim() || "")).join(","));

    const blob = new Blob([rows.join("\n")],{type:"text/csv;charset=utf-8;"});
    const url = URL.createObjectURL(blob);
    const a=document.createElement("a"); a.href=url; a.download="agenda.csv"; a.click(); URL.revokeObjectURL(url);
  };

  // ======= Copy current filter link
  $("#btnCopy").onclick = ()=>{
    const url = new URL(window.location.href);
    const params = new URLSearchParams();
    ["from","to"].forEach(k=>{
      const v = form.querySelector(`[name=${k}]`).value; if(v) params.set(k,v);
    });
    if (search.value) params.set("q", search.value);
    if ($("#toggleWeekend").checked===false) params.set("hide_weekend","1");
    if (onlyAbsent.checked) params.set("only_absent","1");
    if (onlyChanged.checked) params.set("only_changed","1");
    url.search = params.toString();
    navigator.clipboard.writeText(url.toString()).then(()=> alert("Đã copy link bộ lọc!"));
  };

  // ======= Khởi tạo lần đầu
  applyWeekend();   // đảm bảo trạng thái hiển thị cột cuối tuần
  recompute();      // tính tổng ngay khi load
})();
</script>
</body>
</html>
    <%@ include file="/WEB-INF/views/common/_footer.jsp" %>
