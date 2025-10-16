<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html><html><head><title>Create Request</title></head>
<body>
<h3>Create Leave Request</h3>
<form method="post" action="${pageContext.request.contextPath}/request/create">
    Title: <input name="title" required><br/>
    Reason: <textarea name="reason" required></textarea><br/>
    From: <input type="date" name="start_date" required>
    To: <input type="date" name="end_date" required><br/>
    <button type="submit">Send</button>
</form>
<p><a href="${pageContext.request.contextPath}/request/list">Back</a></p>
</body></html>
