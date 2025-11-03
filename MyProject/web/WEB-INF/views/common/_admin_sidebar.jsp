<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="u" value="${sessionScope.currentUser}" />
<c:set var="r" value="${u != null ? u.role : ''}" />

<!-- Nếu sau này có nhiều role (List<String>), truyền thêm currentRolesCsv ở session -->
<c:set var="rolesCsv" value=",,${r},${sessionScope.currentRolesCsv}" />

<!-- Ma trận hiển thị menu -->
<c:set var="canDashboard"
       value="${r eq 'ADMIN' or r eq 'SYS_ADMIN' or r eq 'DIV_LEADER' or r eq 'TEAM_LEAD' or r eq 'HR_ADMIN' or r eq 'DEPT_MANAGER'}" />
<c:set var="canRequests"
       value="${r ne 'TERMINATED' and r ne 'SUSPENDED' and r ne ''}" />
<c:set var="canUsers"
       value="${r eq 'ADMIN' or r eq 'SYS_ADMIN' or r eq 'HR_ADMIN' or r eq 'DEPT_MANAGER'}" />
<c:set var="canDivDashboard"
       value="${r eq 'DIV_LEADER' or r eq 'DEPT_MANAGER' or r eq 'ADMIN' or r eq 'SYS_ADMIN'}" />
<c:set var="canHR"
       value="${r eq 'HR_ADMIN' or r eq 'HR_STAFF' or r eq 'ADMIN' or r eq 'SYS_ADMIN'}" />
<c:set var="canSettings"
       value="${r eq 'ADMIN' or r eq 'SYS_ADMIN' or r eq 'HR_ADMIN' or r eq 'DEPT_MANAGER'}" />

<div class="sidebar">
  <div style="padding:14px 16px;border-bottom:1px solid var(--bd);display:flex;align-items:center;gap:10px">
    <div style="width:28px;height:28px;border-radius:8px;background:var(--pri)"></div>
    <div style="line-height:1">
      <div class="brand">Admin</div>
      <div class="muted" style="font-size:12px">
        Department: ${viewDepartment}
        · Role: <strong><c:out value="${r != '' ? r : 'Guest'}"/></strong>
      </div>
    </div>
  </div>

  <nav style="padding:10px">
    <!-- Dashboard -->
    <c:if test="${canDashboard}">
      <a href="${ctx}/admin" class="btn" style="width:100%;margin-bottom:8px">📊 Dashboard</a>
    </c:if>

    <!-- Division Dashboard -->
    <c:if test="${canDivDashboard}">
      <a href="${ctx}/admin/div" class="btn" style="width:100%;margin-bottom:8px">🏢 Division Dashboard</a>
    </c:if>

    <!-- Requests -->
    <c:if test="${canRequests}">
      <a href="${ctx}/request/list" class="btn" style="width:100%;margin-bottom:8px">📄 Requests</a>
    </c:if>

    <!-- HR -->
    <c:if test="${canHR}">
      <a href="${ctx}/admin/hr" class="btn" style="width:100%;margin-bottom:8px">🧑‍💼 HR</a>
    </c:if>

    <!-- Users -->
    <c:if test="${canUsers}">
      <a href="${ctx}/admin/users" class="btn" style="width:100%;margin-bottom:8px">👥 Users</a>
    </c:if>

    <!-- Settings -->
    <c:if test="${canSettings}">
      <a href="${ctx}/admin/settings" class="btn" style="width:100%;margin-bottom:8px">⚙️ Settings</a>
    </c:if>

    <a href="${ctx}/" class="btn" style="width:100%;">🏠 Home</a>
  </nav>
</div>
