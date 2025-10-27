<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>Division Dashboard</title>
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <style>
    :root{ --bg:#fafafa; --card:#fff; --ink:#111827; --muted:#6b7280; --bd:#e5e7eb; --ink-inv:#fff;
           --pri:#111827; --ok:#16a34a; --warn:#d97706; --no:#dc2626; --table:#f8fafc;
           --shadow:0 10px 24px rgba(2,6,23,.06); }
    *{box-sizing:border-box}
    body{font-family:system-ui,-apple-system,Segoe UI,Roboto,Helvetica,Arial,sans-serif;margin:0;background:var(--bg);color:var(--ink)}
    .wrap{max-width:1100px;margin:24px auto;padding:0 16px}
    h1{margin:0 0 12px}
    .row{display:flex;gap:12px;flex-wrap:wrap;align-items:center}
    .card{background:var(--card);border:1px solid var(--bd);border-radius:12px;padding:14px;box-shadow:var(--shadow)}
    .section{margin-top:16px}
    .cards{display:grid;grid-template-columns:repeat(4,1fr);gap:12px}
    @media (max-width:980px){ .cards{grid-template-columns:repeat(2,1fr)} }
    @media (max-width:620px){ .cards{grid-template-columns:1fr} }
    .stat h2{margin:.25rem 0 0 0;font-size:22px}
    .muted{color:var(--muted)}
    .grid-2{display:grid;grid-template-columns:1fr 1fr;gap:12px}
    @media (max-width:980px){ .grid-2{grid-template-columns:1fr} }
    .btn{display:inline-block;padding:8px 12px;border:1px solid var(--bd);border-radius:10px;background:#fff;text-decoration:none;font-size:13px;cursor:pointer}
    .btn.primary{background:var(--pri);color:var(--ink-inv);border-color:var(--pri)}
    .btn.small{padding:6px 9px;font-size:12px}
    .btn.ghost{background:transparent}
    .filter .input{padding:9px 11px;border:1px solid var(--bd);border-radius:10px;background:#fff}
    .filter label{display:flex;align-items:center;gap:6px}
    .table-wrap{background:var(--card);border:1px solid var(--bd);border-radius:12px;overflow:auto}
    table{width:100%;border-collapse:collapse;min-width:720px}
    th,td{padding:10px;border-bottom:1px solid #f1f5f9;text-align:left;font-size:14px}
    thead th{background:var(--table);position:sticky;top:0;z-index:1}
    tbody tr:hover{background:#fcfcfd}
    .nowrap{white-space:nowrap}
    .badge{display:inline-block;padding:2px 8px;border-radius:999px;font-size:12px;border:1px solid var(--bd)}
    .badge.ok{background:#f1fdf4;border-color:#bbf7d0}
    .badge.warn{background:#fff8eb;border-color:#fed7aa}
  </style>
</head>
<body>
<div class="wrap">
  <h1>Division Dashboard</h1>

  <p>
    Xin chào,
    <b>${sessionScope.currentUser != null ? sessionScope.currentUser.fullName : 'Guest'}</b>
    – Phòng: <b>${empty dept ? 'N/A' : dept}</b>
  </p>

  <!-- Bộ lọc -->
  <form method="get" class="card row filter" action="${pageContext.request.contextPath}/admin/div" autocomplete="off">
    <c:if test="${canSwitchDept}">
      <label>Phòng:
        <input class="input" type="text" name="dept" value="${fn:escapeXml(dept)}" style="width:160px" placeholder="VD: SALE, IT">
      </label>
    </c:if>
    <label>Từ ngày:
      <input class="input" type="date" name="from" value="${from}">
    </label>
    <label>Đến ngày:
      <input class="input" type="date" name="to" value="${to}">
    </label>
    <button class="btn" type="submit">Lọc</button>
    <a class="btn ghost" href="${pageContext.request.contextPath}/admin/div">Reset</a>
  </form>

  <!-- Thống kê -->
  <c:if test="${not empty stats}">
    <c:set var="den" value="${stats.approvalDenominator}" />
    <c:set var="num" value="${stats.approvalNumerator}" />
    <div class="cards section">
      <div class="card stat">
        <div>Headcount</div>
        <h2><fmt:formatNumber value="${stats.headcount}" groupingUsed="true"/></h2>
        <div class="muted">Nhân sự đang active</div>
      </div>
      <div class="card stat">
        <div>Đơn chờ duyệt</div>
        <h2><fmt:formatNumber value="${stats.pendingCount}" /></h2>
        <div class="muted">Trong phòng ${dept}</div>
      </div>
      <div class="card stat">
        <div>Đã duyệt tháng này</div>
        <h2><fmt:formatNumber value="${stats.approvedThisMonth}" /></h2>
        <div class="muted">Theo approved_at</div>
      </div>
      <div class="card stat">
        <div>Tỉ lệ duyệt (khoảng)</div>
        <h2><fmt:formatNumber value="${den == 0 ? 0 : (num * 100.0 / den)}" maxFractionDigits="1"/>%</h2>
        <div class="muted">${num}/${den}</div>
      </div>
    </div>
  </c:if>

  <!-- 2 bảng -->
  <div class="grid-2 section">
    <div>
      <h3 style="margin:0 0 8px">Đơn chờ duyệt</h3>
      <div class="table-wrap">
        <table aria-label="Danh sách đơn chờ duyệt">
          <thead>
          <tr><th class="nowrap">#</th><th>Nhân sự</th><th>Loại</th><th class="nowrap">Từ</th><th class="nowrap">Đến</th><th>Lý do</th><th class="nowrap"></th></tr>
          </thead>
          <tbody>
          <c:forEach var="r" items="${pending}" varStatus="vs">
            <tr>
              <td class="nowrap">${vs.index + 1}</td>
              <td>${r.fullName}</td>
              <td><span class="badge warn">${r.type}</span></td>
              <td class="nowrap"><c:choose><c:when test="${not empty r.from}"><fmt:formatDate value="${r.from}" pattern="yyyy-MM-dd"/></c:when><c:otherwise>—</c:otherwise></c:choose></td>
              <td class="nowrap"><c:choose><c:when test="${not empty r.to}"><fmt:formatDate value="${r.to}" pattern="yyyy-MM-dd"/></c:when><c:otherwise>—</c:otherwise></c:choose></td>
              <td class="muted">${r.reason}</td>
              <td class="nowrap">
                <form method="post" action="${pageContext.request.contextPath}/request/approve" style="display:inline">
                  <input type="hidden" name="_csrf" value="${csrf}">
                  <input type="hidden" name="id" value="${r.id}">
                  <input type="hidden" name="action" value="approve">
                  <button class="btn small primary" onclick="return confirm('Duyệt đơn #${r.id}?')">Approve</button>
                </form>
                <form method="post" action="${pageContext.request.contextPath}/request/approve" style="display:inline">
                  <input type="hidden" name="_csrf" value="${csrf}">
                  <input type="hidden" name="id" value="${r.id}">
                  <input type="hidden" name="action" value="reject">
                  <button class="btn small" onclick="return confirm('Từ chối đơn #${r.id}?')">Reject</button>
                </form>
              </td>
            </tr>
          </c:forEach>
          <c:if test="${empty pending}">
            <tr><td colspan="7" class="muted" style="text-align:center;">Không có đơn chờ duyệt</td></tr>
          </c:if>
          </tbody>
        </table>
      </div>
    </div>

    <div>
      <h3 style="margin:0 0 8px">Đang nghỉ hôm nay</h3>
      <div class="table-wrap">
        <table aria-label="Danh sách đang nghỉ hôm nay">
          <thead><tr><th class="nowrap">#</th><th>Nhân sự</th><th>Loại</th><th class="nowrap">Từ</th><th class="nowrap">Đến</th></tr></thead>
        <tbody>
          <c:forEach var="t" items="${todayOff}" varStatus="vs">
            <tr>
              <td class="nowrap">${vs.index + 1}</td>
              <td>${t.fullName}</td>
              <td><span class="badge ok">${t.type}</span></td>
              <td class="nowrap"><c:choose><c:when test="${not empty t.from}"><fmt:formatDate value="${t.from}" pattern="yyyy-MM-dd"/></c:when><c:otherwise>—</c:otherwise></c:choose></td>
              <td class="nowrap"><c:choose><c:when test="${not empty t.to}"><fmt:formatDate value="${t.to}" pattern="yyyy-MM-dd"/></c:when><c:otherwise>—</c:otherwise></c:choose></td>
            </tr>
          </c:forEach>
          <c:if test="${empty todayOff}">
            <tr><td colspan="5" class="muted" style="text-align:center;">Không ai nghỉ hôm nay</td></tr>
          </c:if>
        </tbody>
        </table>
      </div>
    </div>
  </div>

</div>
</body>
</html>
