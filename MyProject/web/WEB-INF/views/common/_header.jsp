<%@include file="/WEB-INF/views/common/_taglibs.jsp"%>
<header class="app-header">
  <a class="brand" href="${pageContext.request.contextPath}/">LeaveMgmt</a>
  <nav class="nav">
    <a href="${pageContext.request.contextPath}/admin">Dashboard</a>
    <a href="${pageContext.request.contextPath}/admin/users">Users</a>
    <a href="${pageContext.request.contextPath}/request">Requests</a>
  </nav>
  <div class="spacer"></div>
  <div class="userbox">
    <span class="avatar">A</span>
    <span class="name">${sessionScope.currentUser != null ? sessionScope.currentUser.fullName : 'Admin'}</span>
  </div>
</header>

<style>
  :root{--bd:#e5e7eb;--tx:#111827;--muted:#6b7280;--bg:#f9fafb}
  *{box-sizing:border-box}
  body{margin:0;font:14px/1.45 system-ui,Segoe UI,Roboto,Arial;background:var(--bg);color:var(--tx)}
  .app-header{display:flex;align-items:center;gap:16px;padding:10px 16px;background:#fff;border-bottom:1px solid var(--bd);position:sticky;top:0;z-index:50}
  .brand{font-weight:800;text-decoration:none;color:#111827}
  .nav a{display:inline-block;margin-right:10px;padding:6px 10px;border:1px solid var(--bd);border-radius:10px;text-decoration:none;color:#111827;background:#fff}
  .nav a:hover{background:#111827;color:#fff;border-color:#111827}
  .spacer{flex:1}
  .userbox{display:flex;align-items:center;gap:8px;color:#111827}
  .avatar{display:inline-grid;place-items:center;width:28px;height:28px;border-radius:50%;border:1px solid var(--bd)}
</style>
