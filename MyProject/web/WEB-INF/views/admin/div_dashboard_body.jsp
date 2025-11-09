<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp" %>

<style>
  :root{ --card:#fff; --bd:#e5e7eb; --ink:#0f172a; --muted:#64748b; --table:#f6f7fb; }
  .wrap{max-width:1400px;margin:0 auto;padding:20px}
  .toolbar{display:flex;justify-content:space-between;align-items:center;background:var(--card);
    border:1px solid var(--bd);border-radius:8px;padding:14px 16px;margin-bottom:16px}
  .toolbar h1{margin:0;font-size:20px;font-weight:700}
  .pill{border:1px solid var(--bd);border-radius:8px;padding:6px 10px;color:var(--ink);background:#f3f4f6;margin-left:8px}
  .btn{border:1px solid var(--bd);border-radius:8px;padding:8px 12px;background:#fff;cursor:pointer;text-decoration:none;color:#111827}
  .btn:hover{background:#eef2f7}
  .cards{display:grid;grid-template-columns:repeat(auto-fit,minmax(240px,1fr));gap:16px;margin:16px 0}
  .card{background:var(--card);border:1px solid var(--bd);border-radius:8px;padding:16px}
  .label{font-size:12px;color:var(--muted);text-transform:uppercase;letter-spacing:.04em}
  .kpi h2{margin:8px 0 4px;font-size:26px}
  .bar{height:6px;background:#e5e7eb;border-radius:999px;overflow:hidden;margin-top:10px}
  .bar>i{display:block;height:100%;background:#9ca3af;width:0%}
  .grid-2{display:grid;grid-template-columns:1fr 1fr;gap:16px}
  @media(max-width:1200px){.grid-2{grid-template-columns:1fr}}
  .table-wrap{background:#fff;border:1px solid var(--bd);border-radius:8px;overflow:hidden}
  table{width:100%;border-collapse:collapse}
  thead{background:var(--table)}
  th,td{padding:12px 14px;border-bottom:1px solid var(--bd);text-align:left}
  .empty{padding:28px;text-align:center;color:var(--muted)}
</style>

<div class="wrap">
  <div class="toolbar">
    <div>
      <h1>Division Dashboard
        <span class="pill">Phòng: <b>${empty dept ? 'N/A' : dept}</b></span>
      </h1>
    </div>
    <div>
      <a class="btn" href="${pageContext.request.contextPath}/request/approvals">Tới trang Phê duyệt</a>
    </div>
  </div>

  <!-- KPIs -->
  <c:if test="${not empty stats}">
    <c:set var="den" value="${stats.approvalDenominator}" />
    <c:set var="num" value="${stats.approvalNumerator}" />
    <c:set var="rate" value="${den == 0 ? 0 : (num * 100.0 / den)}"/>
    <div class="cards">
      <div class="card kpi">
        <div class="label">Headcount</div>
        <h2><fmt:formatNumber value="${stats.headcount}" groupingUsed="true"/></h2>
        <div style="color:#64748b;font-size:13px">Nhân sự active &nbsp;•&nbsp; Dept <b>${dept}</b></div>
      </div>
      <div class="card kpi">
        <div class="label">Đơn chờ duyệt</div>
        <h2><fmt:formatNumber value="${stats.pendingCount}"/></h2>
        <div class="bar"><i style="width:${stats.pendingCount>0? Math.min(stats.pendingCount*10,100):5}%"></i></div>
      </div>
      <div class="card kpi">
        <div class="label">Đã duyệt tháng này</div>
        <h2><fmt:formatNumber value="${stats.approvedThisMonth}"/></h2>
        <div class="bar"><i style="width:${stats.approvedThisMonth>0? Math.min(stats.approvedThisMonth*5,100):8}%"></i></div>
      </div>
      <div class="card kpi">
        <div class="label">Tỉ lệ duyệt</div>
        <h2><fmt:formatNumber value="${rate}" maxFractionDigits="1"/>%</h2>
        <div class="bar"><i style="width:${rate}%"></i></div>
        <div style="color:#64748b;font-size:13px">${num}/${den}</div>
      </div>
    </div>
  </c:if>

  <!-- Đang nghỉ hôm nay -->
  <div class="grid-2">
    <div class="card" style="grid-column:1 / -1;">
      <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:8px">
        <h3 style="margin:0;font-size:16px">Đang nghỉ hôm nay</h3>
        <div style="color:#64748b;font-size:13px">
          Ngày <b><fmt:formatDate value="<%= new java.util.Date() %>" pattern="dd/MM/yyyy"/></b>
        </div>
      </div>
      <div class="table-wrap">
        <table>
          <thead>
            <tr><th>#</th><th>Nhân sự</th><th>Loại</th><th>Từ ngày</th><th>Đến ngày</th></tr>
          </thead>
          <tbody>
            <c:forEach var="t" items="${todayOff}" varStatus="vs">
              <tr>
                <td><strong>${vs.index + 1}</strong></td>
                <td><strong>${t.fullName}</strong></td>
                <td>${t.type}</td>
                <td><c:choose><c:when test="${not empty t.from}">
                      <fmt:formatDate value="${t.from}" pattern="dd/MM/yyyy"/></c:when><c:otherwise>—</c:otherwise></c:choose></td>
                <td><c:choose><c:when test="${not empty t.to}">
                      <fmt:formatDate value="${t.to}" pattern="dd/MM/yyyy"/></c:when><c:otherwise>—</c:otherwise></c:choose></td>
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
</div>

<script>
  // animate KPI bars
  setTimeout(()=>document.querySelectorAll('.bar>i').forEach(b=>{const w=b.style.width;b.style.width='0%';setTimeout(()=>b.style.width=w,80)}),200);
</script>
