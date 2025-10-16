<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@include file="/WEB-INF/views/common/_taglibs.jsp"%>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title><c:out value="${pageTitle != null ? pageTitle : 'Admin'}"/></title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
</head>
<body>
  <%@include file="/WEB-INF/views/common/_header.jsp"%>
  <main class="container">
    <jsp:include page="<c:out value='${content}'/>"/>
  </main>
  <%@include file="/WEB-INF/views/common/_footer.jsp"%>
</body>
</html>
