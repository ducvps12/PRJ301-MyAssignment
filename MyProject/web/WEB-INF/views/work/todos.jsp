<%@ page contentType="text/html; charset=UTF-8" %>
<h2>Todos</h2>
<p>Status: ${status} | Assignee: ${assignee}</p>
<table border="1" cellpadding="4">
  <tr><th>ID</th><th>Title</th><th>Status</th><th>Due</th><th>Priority</th></tr>
  <c:forEach var="t" items="${todos}">
    <tr>
      <td>${t.id}</td><td>${t.title}</td><td>${t.status}</td>
      <td>${t.due_date}</td><td>${t.priority}</td>
    </tr>
  </c:forEach>
</table>
