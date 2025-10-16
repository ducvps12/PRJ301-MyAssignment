<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.time.*,java.util.*,com.acme.leavemgmt.model.User" %>
<%
LocalDate from = (LocalDate) request.getAttribute("from");
LocalDate to   = (LocalDate) request.getAttribute("to");
List<User> users = (List<User>) request.getAttribute("users");
Map<Integer, Set<LocalDate>> absent = (Map<Integer, Set<LocalDate>>) request.getAttribute("absent");

List<LocalDate> days = new ArrayList<>();
for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) days.add(d);
%>
<!DOCTYPE html><html><head><title>Agenda</title></head>
<body>
<h3>Agenda - Department ${sessionScope.department}</h3>

<form method="get">
    From: <input type="date" name="from" value="<%= from %>">
    To:   <input type="date" name="to" value="<%= to %>">
    <button type="submit">View</button>
</form>

<table border="1" cellpadding="5">
<tr>
  <th>Employee</th>
  <% for (LocalDate d : days) { %>
   <th><%= d.getMonthValue() %>/<%= d.getDayOfMonth() %></th>
  <% } %>
</tr>

<% for (User u : users) { %>
<tr>
  <td><%= u.getFullName() %></td>
  <% Set<LocalDate> a = absent.getOrDefault(u.getId(), Collections.emptySet()); %>
  <% for (LocalDate d : days) { %>
    <td style="background: <%= a.contains(d) ? "#ffb3b3" : "#b3ffb3" %>;">
      <%= a.contains(d) ? "OFF" : "ON" %>
    </td>
  <% } %>
</tr>
<% } %>
</table>

<p><a href="${pageContext.request.contextPath}/request/list">Back</a></p>
</body></html>
