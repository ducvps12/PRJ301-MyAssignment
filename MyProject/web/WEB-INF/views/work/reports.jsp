<%@ page contentType="text/html; charset=UTF-8" %>
<h2>Reports</h2>
<p>Type: ${type}, From: ${from}, To: ${to}</p>
<ul>
  <c:forEach var="r" items="${reports}">
    <li>${r.workDate} - ${r.type} - ${fn:substring(r.content,0,80)}...</li>
  </c:forEach>
</ul>
