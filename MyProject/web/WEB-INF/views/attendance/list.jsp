<%@ include file="/WEB-INF/views/common/_taglibs.jsp"%>
<jsp:include page="/WEB-INF/views/common/_user_header.jsp"/>
<div class="wrap">
  <div class="toolbar" style="display:flex;gap:8px;flex-wrap:wrap">
    <form method="get" class="inline">
      <input class="input" type="date" name="from" value="${param.from}" />
      <input class="input" type="date" name="to" value="${param.to}" />
      <select class="input" name="status">
        <option value="">Tr?ng thái</option>
        <option ${param.status=='PRESENT'?'selected':''}>PRESENT</option>
        <option ${param.status=='ABSENT'?'selected':''}>ABSENT</option>
        <option ${param.status=='LEAVE'?'selected':''}>LEAVE</option>
      </select>
      <button class="btn">L?c</button>
    </form>
    <form method="post" action="${pageContext.request.contextPath}/attendance/clock">
      <input type="hidden" name="csrf" value="${csrf}">
      <button class="btn">Check-in</button>
      <input type="hidden" name="action" value="in" />
    </form>
    <form method="post" action="${pageContext.request.contextPath}/attendance/clock">
      <input type="hidden" name="csrf" value="${csrf}">
      <button class="btn ghost">Check-out</button>
      <input type="hidden" name="action" value="out" />
    </form>
  </div>

  <div class="panel">
    <table class="table">
      <thead><tr><th>Ngày</th><th>Vào</th><th>Ra</th><th>Mu?n</th><th>V? s?m</th><th>OT</th><th>Tr?ng thái</th></tr></thead>
      <tbody>
      <c:forEach var="r" items="${rows}">
        <tr>
          <td><fmt:formatDate value="${r.workDate}" pattern="dd/MM/yyyy"/></td>
          <td><fmt:formatDate value="${r.checkin}" pattern="HH:mm"/></td>
          <td><fmt:formatDate value="${r.checkout}" pattern="HH:mm"/></td>
          <td>${r.lateMinutes}?</td>
          <td>${r.earlyMinutes}?</td>
          <td>${r.otMinutes/60.0}</td>
          <td><span class="badge ${r.status}">${r.status}</span></td>
        </tr>
      </c:forEach>
      </tbody>
    </table>
  </div>
</div>
