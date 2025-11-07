<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp"%>
<c:set var="cp" value="${pageContext.request.contextPath}"/>

<!DOCTYPE html>
<html lang="vi" data-theme="${sessionScope.theme != null ? sessionScope.theme : 'auto'}">
<head>
  <meta charset="UTF-8">
  <title><c:out value="${empty title ? 'Portal · LeaveMgmt' : title}"/></title>
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <style>
    :root{
      --bg:#f7f7f8; --card:#fff; --ink:#0f172a; --muted:#64748b; --bd:#e5e7eb; --ink-inv:#fff;
      --card-2:#f3f4f6;
    }
    html,body{height:100%}
    body{margin:0;background:var(--bg);color:var(--ink);font-family:system-ui,-apple-system,Segoe UI,Roboto,Ubuntu,Cantarell,'Helvetica Neue',Arial,'Noto Sans',sans-serif}
    .with-psb{ margin-left:var(--sbw); transition:margin-left .25s ease; }
    @media(max-width:1100px){ .with-psb{ margin-left:0 } }
    .container{ max-width:1200px; margin:0 auto; padding:18px; }
    .compact .container{ padding:12px; }
  </style>
</head>
<body>

  <%@ include file="/WEB-INF/views/common/_portal_sidebar.jsp" %>
  <%@ include file="/WEB-INF/views/common/_portal_header.jsp" %>

  <main class="with-psb" role="main">
    <div class="container">
      <!-- ======= PAGE CONTENT WILL BE INCLUDED HERE ======= -->
      <jsp:include page="${contentPage}" />
    </div>
  </main>

</body>
</html>
