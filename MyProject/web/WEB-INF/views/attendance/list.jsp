<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp"%>
<jsp:include page="/WEB-INF/views/common/_user_header.jsp"/>

<c:set var="cp" value="${pageContext.request.contextPath}"/>

<style>
  :root{--bg:#f7f7f8;--card:#fff;--bd:#e5e7eb;--ink:#0f172a;--muted:#64748b;--pri:#2563eb;--pri-ink:#fff}
  body{background:var(--bg)}
  .wrap{max-width:1200px;margin:0 auto;padding:16px}
  .toolbar{display:flex;gap:8px;flex-wrap:wrap;align-items:center;margin-bottom:12px}
  .input{border:1px solid var(--bd);border-radius:10px;padding:8px 10px;background:#fff}
  .btn{border:1px solid var(--pri);background:var(--pri);color:var(--pri-ink);padding:8px 12px;border-radius:10px;cursor:pointer}
  .btn.ghost{background:#fff;color:var(--pri)}
  .panel{background:var(--card);border:1px solid var(--bd);border-radius:14px}
  .table{width:100%;border-collapse:collapse}
  .table th,.table td{padding:10px 12px;border-top:1px solid var(--bd);text-align:left;vertical-align:middle}
  .table thead th{background:#fafafa;position:sticky;top:0;z-index:1}
  .right{display:flex;gap:8px;margin-left:auto}
  .muted{color:var(--muted)}
  .badge{display:inline-block;padding:.2rem .5rem;border-radius:999px;border:1px solid var(--bd);font-size:12px}
  .badge.PRESENT{background:#ecfdf5;color:#065f46;border-color:#a7f3d0}
  .badge.ABSENT{background:#fff1f2;color:#9f1239;border-color:#fecdd3}
  .badge.LEAVE{background:#fefce8;color:#854d0e;border-color:#fde68a}
  .empty{padding:26px;text-align:center;color:var(--muted)}
  .footerbar{display:flex;align-items:center;justify-content:space-between;padding:10px 12px}
  .pager a{padding:6px 10px;border:1px solid var(--bd);border-radius:8px;margin-left:4px;text-decoration:none;color:var(--ink);background:#fff}
  .pager .on{background:var(--pri);color:#fff;border-color:var(--pri)}
  .note{display:flex;gap:6px;align-items:center}
  .note input{min-width:180px}
</style>

<div class="wrap">
  <!-- Toolbar -->
  <div class="toolbar">
    <form method="get" class="inline" action="${cp}/attendance">
      <input class="input" type="date" name="from" value="${from}" />
      <input class="input" type="date" name="to"   value="${to}" />
      <select class="input" name="status">
        <option value="">Trạng thái</option>
        <option value="PRESENT" ${f_status=='PRESENT'?'selected':''}>PRESENT</option>
        <option value="ABSENT"  ${f_status=='ABSENT' ?'selected':''}>ABSENT</option>
        <option value="LEAVE"   ${f_status=='LEAVE'  ?'selected':''}>LEAVE</option>
      </select>
      <button class="btn" type="submit">Lọc</button>
    </form>

    <div class="right">
      <!-- Lưu ý: dùng sessionScope.csrf để luôn thấy token đúng -->
      <form method="post" action="${cp}/attendance/clock" class="js-clock">
        <input type="hidden" name="csrf" value="${sessionScope.csrf}">
        <input type="hidden" name="action" value="in" />
        <button class="btn" type="submit">Check-in</button>
      </form>
      <form method="post" action="${cp}/attendance/clock" class="js-clock">
        <input type="hidden" name="csrf" value="${sessionScope.csrf}">
        <input type="hidden" name="action" value="out" />
        <button class="btn ghost" type="submit">Check-out</button>
      </form>
    </div>
  </div>

  <!-- Bảng -->
  <div class="panel">
    <table class="table">
      <thead>
        <tr>
          <th>Ngày</th>
          <th>Vào</th>
          <th>Ra</th>
          <th>Muộn (phút)</th>
          <th>OT (giờ)</th>
          <th>Trạng thái</th>
          <th>Ghi chú</th>
        </tr>
      </thead>
      <tbody>
      <c:choose>
        <c:when test="${empty rows}">
          <tr><td colspan="7" class="empty">Chưa có dữ liệu trong khoảng lọc.</td></tr>
        </c:when>
        <c:otherwise>
          <c:forEach var="r" items="${rows}">
            <tr>
              <td><c:out value="${r.workDate}"/></td>
              <td><c:out value="${r.checkIn}"/></td>
              <td><c:out value="${r.checkOut}"/></td>
              <td><c:out value="${r.lateMinutes}"/></td>
              <td><fmt:formatNumber value="${r.otMinutes/60.0}" maxFractionDigits="2"/></td>
              <td><span class="badge ${r.status}"><c:out value="${r.status}"/></span></td>
              <td><c:out value="${r.notes}"/></td>
            </tr>
          </c:forEach>
        </c:otherwise>
      </c:choose>
      </tbody>
    </table>

    <!-- Footer: phân trang -->
    <div class="footerbar">
      <div class="muted">
        Từ <strong>${from}</strong> đến <strong>${to}</strong>
        <c:if test="${not empty f_status}"> · Trạng thái: <strong>${f_status}</strong></c:if>
      </div>
      <div class="pager">
        <c:set var="p" value="${page}"/><c:set var="s" value="${size}"/>
        <a href="${cp}/attendance?from=${from}&to=${to}&status=${f_status}&page=${p-1<1?1:p-1}&size=${s}">« Trước</a>
        <a class="on" href="${cp}/attendance?from=${from}&to=${to}&status=${f_status}&page=${p}&size=${s}">${p}</a>
        <a href="${cp}/attendance?from=${from}&to=${to}&status=${f_status}&page=${p+1}&size=${s}">Sau »</a>
      </div>
    </div>
  </div>
</div>

<script>
  // Chống double submit và thêm ?from=?to= hôm nay nếu rỗng
  document.querySelectorAll('form.js-clock').forEach(f=>{
    f.addEventListener('submit', e=>{
      const btn = f.querySelector('button[type="submit"]');
      if (btn){ btn.disabled = true; btn.textContent = 'Đang gửi...'; }
    });
  });
  // Hiển thị token để debug nếu cần:
  // console.log('csrf=', '<c:out value="${sessionScope.csrf}"/>');
</script>
