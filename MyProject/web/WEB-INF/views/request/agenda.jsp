<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<title>Agenda – Lịch nghỉ phòng ban</title>
<meta name="viewport" content="width=device-width, initial-scale=1">
<style>
  :root{ --b:#e5e7eb; --bg:#f7f7f8; --muted:#6b7280; --ok:#10b981; --no:#ef4444; }
  body{font-family:system-ui,Segoe UI,Roboto,Helvetica,Arial,sans-serif;background:var(--bg);margin:0}
  .wrap{max-width:1200px;margin:28px auto;padding:0 16px}
  .toolbar{display:flex;flex-wrap:wrap;gap:10px;align-items:center;margin-bottom:12px}
  .toolbar .box{background:#fff;border:1px solid var(--b);border-radius:12px;padding:12px;display:flex;gap:8px;align-items:center}
  .btn{padding:8px 12px;border:1px solid var(--b);border-radius:10px;background:#fff;cursor:pointer}
  .btn-primary{background:#2563eb;color:#fff;border-color:#2563eb}
  input,select{padding:8px 10px;border:1px solid var(--b);border-radius:10px;background:#fff}
  .card{background:#fff;border:1px solid var(--b);border-radius:12px;overflow:auto}
  table{border-collapse:separate;border-spacing:0;width:max-content;min-width:100%}
  th,td{border-top:1px solid var(--b);border-left:1px solid var(--b);padding:10px 12px;text-align:center;white-space:nowrap}
  th:last-child, td:last-child{border-right:1px solid var(--b)}
  tr:last-child td{border-bottom:1px solid var(--b)}
  thead th{position:sticky;top:0;background:#f8fafc;z-index:2}
  .col-name{position:sticky;left:0;background:#fff;z-index:1;text-align:left;font-weight:600}
  .weekend{background:#fff7ed}
  .ok{color:var(--ok);font-weight:600}
  .no{color:var(--no);font-weight:700}
  tfoot td{font-weight:700;background:#f8fafc}
  .legend{display:flex;gap:12px;color:var(--muted);font-size:14px}
  .muted{color:var(--muted)}
  .badge{display:inline-block;padding:2px 8px;border-radius:999px;border:1px solid var(--b);font-size:12px}
  .hidden{display:none}
  @media print{
    .toolbar{display:none}
    body{background:#fff}
    .card{border:none}
  }
</style>
</head>
<body>
<div class="wrap">
  <h2 style="margin:0 0 12px 0">Agenda – Lịch nghỉ phòng ban</h2>

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
      <label style="display:flex;gap:6px;align-items:center">
        <input type="checkbox" id="onlyAbsent"> Chỉ hiện người đang nghỉ
      </label>
      <label style="display:flex;gap:6px;align-items:center">
        <input type="checkbox" id="toggleWeekend" checked> Hiện cuối tuần
      </label>
    </div>

    <div class="box">
      <button class="btn" id="btnCsv">Xuất CSV</button>
      <button class="btn" onclick="window.print()">In</button>
      <a class="btn" href="${pageContext.request.contextPath}/request/list">Quay lại danh sách</a>
    </div>
  </div>

  <div class="legend" style="margin-bottom:8px">
    <span><span class="ok">✅</span> đi làm</span>
    <span><span class="no">❌</span> nghỉ (đã duyệt)</span>
    <span class="badge">Phòng ban: ${sessionScope.department}</span>
    <span class="muted">Từ <b><fmt:formatDate value="${java.sql.Date.valueOf(from)}" pattern="dd/MM/yyyy"/></b>
      đến <b><fmt:formatDate value="${java.sql.Date.valueOf(to)}" pattern="dd/MM/yyyy"/></b></span>
  </div>

  <!-- Bảng agenda -->
  <div class="card" id="tableWrap">
    <table id="agendaTbl">
      <thead>
      <tr>
        <th class="col-name">Nhân viên</th>
        <c:forEach var="d" items="${days}">
          <th class="${d.dayOfWeek.value >= 6 ? 'weekend' : ''}"
              data-date="${d}" data-dow="${d.dayOfWeek.value}">
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
        <tr data-user="${fn:toLowerCase(u.fullName)}">
          <td class="col-name">
            ${u.fullName}
            <span class="muted">(#${u.userId})</span>
          </td>
          <c:forEach var="d" items="${days}">
            <c:set var="isAbsent"
                   value="${absent[u.userId] != null && absent[u.userId].contains(d)}"/>
            <td class="${d.dayOfWeek.value >= 6 ? 'weekend' : ''}"
                data-absent="${isAbsent ? 1 : 0}"
                title="<fmt:formatDate value='${java.sql.Date.valueOf(d)}' pattern='dd/MM/yyyy'/>">
              <c:choose>
                <c:when test="${isAbsent}"><span class="no">❌</span></c:when>
                <c:otherwise><span class="ok">✅</span></c:otherwise>
              </c:choose>
            </td>
          </c:forEach>
        </tr>
      </c:forEach>
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
  const qS = sel => document.querySelector(sel);
  const qSA = sel => Array.from(document.querySelectorAll(sel));

  // ======= Điều hướng tuần nhanh
  const form = qS("#rangeForm");
  const inFrom = form.querySelector("input[name=from]");
  const inTo   = form.querySelector("input[name=to]");

  function setRange(from, to){ inFrom.value = from; inTo.value = to; form.submit(); }
  function fmt(d){ return d.toISOString().slice(0,10); }
  function addDays(d, n){ const x = new Date(d); x.setDate(x.getDate()+n); return x; }
  function weekOf(date){
    const d = new Date(date);
    const day = d.getDay()==0 ? 7 : d.getDay(); // Mon=1..Sun=7
    const mon = addDays(d, 1-day), sun = addDays(mon, 6);
    return [fmt(mon), fmt(sun)];
  }

  qS("#thisWeek").onclick = () => {
    const [f,t] = weekOf(new Date());
    setRange(f,t);
  };
  qS("#prevWeek").onclick = () => {
    const f = new Date(inFrom.value||new Date());
    const f2 = addDays(f, -7), t2 = addDays(f2, 6);
    setRange(fmt(f2), fmt(t2));
  };
  qS("#nextWeek").onclick = () => {
    const t = new Date(inTo.value||new Date());
    const f2 = addDays(t, 1), t2 = addDays(f2, 6);
    setRange(fmt(f2), fmt(t2));
  };

  // ======= Đếm tổng người nghỉ theo cột
  function recomputeTotals(){
    const rows = qSA("#agendaTbl tbody tr:not(.hidden)");
    const cols = qSA("#agendaTbl thead th").length - 1; // trừ cột tên
    for (let c=0; c<cols; c++){
      let total = 0;
      rows.forEach(r=>{
        const cell = r.children[c+1]; // +1 bỏ cột tên
        if (cell && cell.dataset.absent === "1") total++;
      });
      const ftCell = qS(`#agendaTbl tfoot tr td:nth-child(${c+2})`);
      if (ftCell){ ftCell.textContent = total; ftCell.dataset.colTotal = total; }
    }
  }
  recomputeTotals();

  // ======= Tìm kiếm theo tên
  const search = qS("#search");
  const onlyAbsent = qS("#onlyAbsent");
  function applyFilters(){
    const term = (search.value || "").trim().toLowerCase();
    const rows = qSA("#agendaTbl tbody tr");
    rows.forEach(r=>{
      const name = r.dataset.user || "";
      const matchName = !term || name.includes(term);

      let matchAbsent = true;
      if (onlyAbsent.checked){
        matchAbsent = r.querySelector('td[data-absent="1"]') !== null;
      }
      r.classList.toggle("hidden", !(matchName && matchAbsent));
    });
    recomputeTotals();
  }
  search.addEventListener("input", applyFilters);
  onlyAbsent.addEventListener("change", applyFilters);

  // ======= Ẩn/Hiện cuối tuần
  const toggleWeekend = qS("#toggleWeekend");
  function applyWeekend(){
    const show = toggleWeekend.checked;
    qSA(`#agendaTbl th.weekend, #agendaTbl td.weekend`).forEach(td=>{
      td.style.display = show ? "" : "none";
    });
  }
  toggleWeekend.addEventListener("change", applyWeekend);
  applyWeekend();

  // ======= Xuất CSV nhanh (client-side) — bỏ cột đang ẩn
  qS("#btnCsv").onclick = () => {
    const table = qS("#agendaTbl");
    let rows = [];

    // visible column indices from the header row
    const headCells = Array.from(table.tHead.rows[0].cells);
    const visibleIdx = headCells
      .map((c,i)=> ({i, disp: getComputedStyle(c).display}))
      .filter(x=> x.disp !== "none")
      .map(x=> x.i);

    // header
    const heads = visibleIdx.map((i)=>{
      const c = headCells[i];
      return i===0 ? "Nhan vien" : c.innerText.trim().replace(/\n/g,' ');
    }).filter(Boolean);
    rows.push(heads.join(","));

    // body
    qSA("#agendaTbl tbody tr:not(.hidden)").forEach(tr=>{
      const arr = visibleIdx.map(i=>{
        const c = tr.cells[i];
        const t = c ? c.innerText.trim() : "";
        return `"${t.replace(/"/g,'""')}"`;
      });
      rows.push(arr.join(","));
    });

    // footer (tổng)
    const ftCells = Array.from(table.tFoot.rows[0].cells);
    const ft = visibleIdx.map((i)=> i===0 ? "Tong nghi" : (ftCells[i] ? ftCells[i].innerText.trim() : ""));
    rows.push(ft.join(","));

    const blob = new Blob([rows.join("\n")], {type:"text/csv;charset=utf-8;"});
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url; a.download = "agenda.csv"; a.click();
    URL.revokeObjectURL(url);
  };
})();
</script>


</body>
</html>
