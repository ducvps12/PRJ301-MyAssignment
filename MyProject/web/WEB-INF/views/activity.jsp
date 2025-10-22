<table class="table">
  <thead>
    <tr><th>Th?i gian</th><th>Hành ??ng</th><th>??i t??ng</th><th>Ghi chú</th><th>IP</th></tr>
  </thead>
  <tbody>
  <c:forEach var="a" items="${items}">
    <tr>
      <td><fmt:formatDate value="${a.createdAt}" pattern="dd/MM/yyyy HH:mm"/></td>
      <td>${a.action}</td>
      <td>${a.entityType} <c:if test="${a.entityId != null}">#${a.entityId}</c:if></td>
      <td>${a.note}</td>
      <td>${a.ip}</td>
    </tr>
  </c:forEach>
  <c:if test="${empty items}">
    <tr><td colspan="5" style="text-align:center;opacity:.7;">Ch?a có ho?t ??ng</td></tr>
  </c:if>
  </tbody>
</table>
