<%@ include file="/WEB-INF/views/common/_taglibs.jsp" %>
<%
  // ti?n: l?y URI hi?n t?i ?? set 'active'
  String _uri = request.getRequestURI();
  request.setAttribute("_uri", _uri);
%>

<header class="app-header" role="banner">
  <a class="brand" href="${pageContext.request.contextPath}/" aria-label="Trang ch?">LeaveMgmt</a>

  <nav class="nav" role="navigation" aria-label="Chính">
    <!-- Luôn hi?n th? cho m?i user -->
    <a href="${pageContext.request.contextPath}/request/list"
       class="${_uri.startsWith(pageContext.request.contextPath.concat('/request')) ? 'active' : ''}">
      Requests
    </a>
    <a href="${pageContext.request.contextPath}/request/agenda"
       class="${_uri.startsWith(pageContext.request.contextPath.concat('/request/agenda')) ? 'active' : ''}">
      Agenda
    </a>

    <!-- Ch? MANAGER/ADMIN m?i th?y nút Approve -->
    <c:if test="${sessionScope.user != null and (sessionScope.user.manager or sessionScope.user.admin)}">
      <a href="${pageContext.request.contextPath}/request/approve"
         class="${_uri.startsWith(pageContext.request.contextPath.concat('/request/approve')) ? 'active' : ''}">
        Approvals
      </a>
    </c:if>

    <!-- Ch? ADMIN/LEADER m?i th?y khu Admin -->
    <c:if test="${sessionScope.user != null and (sessionScope.user.admin or sessionScope.user.leader)}">
      <a href="${pageContext.request.contextPath}/admin"
         class="${_uri == pageContext.request.contextPath.concat('/admin') ? 'active' : ''}">
        Dashboard
      </a>
      <a href="${pageContext.request.contextPath}/admin/users"
         class="${_uri.startsWith(pageContext.request.contextPath.concat('/admin/users')) ? 'active' : ''}">
        Users
      </a>
    </c:if>
  </nav>

  <div class="spacer" aria-hidden="true"></div>

  <div class="userbox" role="group" aria-label="Tài kho?n">
    <span class="avatar" aria-hidden="true">
      <c:choose>
        <c:when test="${not empty sessionScope.user and not empty sessionScope.user.fullName}">
          ${fn:substring(sessionScope.user.fullName,0,1)}
        </c:when>
        <c:otherwise>U</c:otherwise>
      </c:choose>
    </span>
    <span class="name">
      <c:choose>
        <c:when test="${not empty sessionScope.user}">
          ${sessionScope.user.displayName}
        </c:when>
        <c:otherwise>Guest</c:otherwise>
      </c:choose>
    </span>
    <c:if test="${not empty sessionScope.user}">
      <a class="logout" href="${pageContext.request.contextPath}/auth/logout" title="??ng xu?t">Logout</a>
    </c:if>
  </div>
</header>

<style>
  :root{--bd:#e5e7eb;--tx:#111827;--muted:#6b7280;--bg:#f9fafb;--pri:#111827}
  *{box-sizing:border-box}
  body{margin:0;font:14px/1.45 system-ui,Segoe UI,Roboto,Arial;background:var(--bg);color:var(--tx)}
  .app-header{display:flex;align-items:center;gap:16px;padding:10px 16px;background:#fff;border-bottom:1px solid var(--bd);position:sticky;top:0;z-index:50}
  .brand{font-weight:800;text-decoration:none;color:var(--pri)}
  .nav a{display:inline-block;margin-right:8px;padding:6px 10px;border:1px solid var(--bd);border-radius:10px;text-decoration:none;color:var(--pri);background:#fff;transition:.15s}
  .nav a:hover{background:var(--pri);color:#fff;border-color:var(--pri)}
  .nav a.active{background:var(--pri);color:#fff;border-color:var(--pri)}
  .spacer{flex:1}
  .userbox{display:flex;align-items:center;gap:10px;color:var(--pri)}
  .avatar{display:inline-grid;place-items:center;width:28px;height:28px;border-radius:50%;border:1px solid var(--bd);font-weight:700}
  .logout{margin-left:6px;padding:4px 8px;border:1px solid var(--bd);border-radius:8px;text-decoration:none;color:var(--pri)}
  .logout:hover{background:#f3f4f6}
  @media (max-width:720px){
    .nav a{margin-right:6px;padding:6px 8px}
    .brand{font-size:15px}
  }
</style>
