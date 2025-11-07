<%@ include file="/WEB-INF/views/common/_taglibs.jsp"%>
<jsp:include page="/WEB-INF/views/common/_admin_header.jsp"/>
<div class="wrap">
  <div class="toolbar" style="display:flex;gap:8px">
    <form method="get" class="inline">
      <input class="input" type="number" name="y" value="${y}" style="width:110px">
      <input class="input" type="number" name="m" value="${m}" style="width:90px">
      <button class="btn">Xem</button>
    </form>
    <form method="post" action="${cp}/payroll/run" class="inline">
      <input type="hidden" name="csrf" value="${csrf}">
      <input type="hidden" name="y" value="${y}"><input type="hidden" name="m" value="${m}">
      <button class="btn pri">T?o/Tính b?ng l??ng</button>
    </form>
  </div>

  <c:choose>
    <c:when test="${empty runId}">
      <div class="empty">Ch?a có run tháng này. Nh?n ?T?o/Tính b?ng l??ng?.</div>
    </c:when>
    <c:otherwise>
      <div class="panel">
        <table class="table">
          <thead><tr>
            <th>Nhân viên</th><th>C? b?n</th><th>Ph? c?p</th><th>OT</th><th>Th??ng</th>
            <th>Ph?t</th><th>B?o hi?m</th><th>Thu?</th><th>Th?c l?nh</th>
          </tr></thead>
          <tbody>
          <c:forEach var="i" items="${items}">
            <tr>
              <td>${i.userId}</td>
              <td><fmt:formatNumber value="${i.baseSalary}" type="currency"/></td>
              <td><fmt:formatNumber value="${i.allowance}" type="currency"/></td>
              <td><fmt:formatNumber value="${i.otPay}" type="currency"/></td>
              <td><fmt:formatNumber value="${i.bonus}" type="currency"/></td>
              <td><fmt:formatNumber value="${i.penalty}" type="currency"/></td>
              <td><fmt:formatNumber value="${i.insurance}" type="currency"/></td>
              <td><fmt:formatNumber value="${i.tax}" type="currency"/></td>
              <td><b><fmt:formatNumber value="${i.netPay}" type="currency"/></b></td>
            </tr>
          </c:forEach>
          </tbody>
        </table>
      </div>
    </c:otherwise>
  </c:choose>
</div>
