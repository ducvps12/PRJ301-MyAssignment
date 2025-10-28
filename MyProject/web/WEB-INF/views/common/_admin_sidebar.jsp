<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<div class="sidebar">
  <div style="padding:14px 16px;border-bottom:1px solid var(--bd);display:flex;align-items:center;gap:10px">
    <div style="width:28px;height:28px;border-radius:8px;background:var(--pri)"></div>
    <div style="line-height:1">
      <div class="brand">Admin</div>
      <div class="muted" style="font-size:12px">Department: ${viewDepartment}</div>
    </div>
  </div>
  <nav style="padding:10px">
    <a href="${pageContext.request.contextPath}/admin" class="btn" style="width:100%;margin-bottom:8px">📊 Dashboard</a>
    <a href="${pageContext.request.contextPath}/request/list" class="btn" style="width:100%;margin-bottom:8px">📄 Requests</a>
    <a href="${pageContext.request.contextPath}/admin/users" class="btn" style="width:100%;margin-bottom:8px">👥 Users</a>
    <a href="${pageContext.request.contextPath}/" class="btn" style="width:100%;">🏠 Home</a>
  </nav>
</div>
