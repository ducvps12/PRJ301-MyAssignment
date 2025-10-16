<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.acme.leavemgmt.model.Request" %>
<%
    Request r = (Request) request.getAttribute("reqItem");
%>
<!DOCTYPE html><html><head><title>Approve</title></head>
<body>
<h3>Review Request #<%= r.getId() %></h3>
<p>Title: <b><%= r.getTitle() %></b></p>
<p>From: <%= r.getStartDate() %> - To: <%= r.getEndDate() %></p>
<p>Reason: <pre><%= r.getReason() %></pre></p>

<form method="post" action="${pageContext.request.contextPath}/request/approve">
    <input type="hidden" name="id" value="<%= r.getId() %>">
    Manager note: <br/>
    <textarea name="note" rows="3" cols="40"></textarea><br/><br/>
    <button name="action" value="approve" type="submit">Approve</button>
    <button name="action" value="reject" type="submit">Reject</button>
</form>
<p><a href="${pageContext.request.contextPath}/request/list">Back</a></p>
</body></html>
