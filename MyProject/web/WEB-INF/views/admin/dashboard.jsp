<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@include file="/WEB-INF/views/common/_taglibs.jsp"%>
<%@include file="/WEB-INF/views/common/_header.jsp"%>

<div class="container" style="max-width:1100px; margin:24px auto;">
  <h2 style="margin:0 0 16px 0;">Admin Dashboard</h2>

  <!-- Cards -->
  <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:14px;">
    <div class="card kpi">
      <div class="kpi-title">Tổng người dùng</div>
      <div class="kpi-number">${stats.totalUsers}</div>
    </div>
    <div class="card kpi">
      <div class="kpi-title">Đơn nghỉ (tổng)</div>
      <div class="kpi-number">${stats.totalRequests}</div>
    </div>
    <div class="card kpi">
      <div class="kpi-title">Đơn chờ duyệt</div>
      <div class="kpi-number">${stats.pendingRequests}</div>
    </div>
    <div class="card kpi">
      <div class="kpi-title">Phòng ban</div>
      <div class="kpi-number">${stats.totalDepartments}</div>
    </div>
  </div>

  <!-- Recent requests -->
  <div class="card" style="margin-top:18px;">
    <div class="card-header">
      Đơn nghỉ mới nhất
      <a href="${pageContext.request.contextPath}/admin/requests" class="btn small">Xem tất cả</a>
    </div>
    <div class="table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>Title</th><th>Từ</th><th>Đến</th><th>Người tạo</th><th>Trạng thái</th>
          </tr>
        </thead>
        <tbody>
        <c:forEach var="r" items="${recentRequests}">
          <tr>
            <td>${r.title}</td>
            <td><fmt:formatDate value="${r.fromDate}" pattern="dd/MM/yyyy"/></td>
            <td><fmt:formatDate value="${r.toDate}" pattern="dd/MM/yyyy"/></td>
            <td>${r.createdByName}</td>
            <td>
              <span class="badge ${r.status}">${r.status}</span>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty recentRequests}">
          <tr><td colspan="5" style="text-align:center;opacity:.7;">Chưa có dữ liệu</td></tr>
        </c:if>
        </tbody>
      </table>
    </div>
  </div>

  <!-- Top active users -->
  <div class="card" style="margin-top:18px;">
    <div class="card-header">Người dùng hoạt động gần đây</div>
    <div class="table-wrap">
      <table class="table">
        <thead>
          <tr><th>#</th><th>Họ tên</th><th>Email</th><th>Role</th><th>Last Login</th></tr>
        </thead>
        <tbody>
        <c:forEach var="u" items="${recentUsers}" varStatus="vs">
          <tr>
            <td>${vs.index + 1}</td>
            <td>${u.fullName}</td>
            <td>${u.email}</td>
            <td>${u.roleNames}</td>
            <td><fmt:formatDate value="${u.lastLogin}" pattern="dd/MM/yyyy HH:mm"/></td>
          </tr>
        </c:forEach>
        <c:if test="${empty recentUsers}">
          <tr><td colspan="5" style="text-align:center;opacity:.7;">Chưa có dữ liệu</td></tr>
        </c:if>
        </tbody>
      </table>
    </div>
  </div>
</div>

<style>
  .card{background:#fff;border:1px solid #e5e7eb;border-radius:12px;box-shadow:0 1px 2px rgba(0,0,0,.04)}
  .card-header{padding:12px 16px;border-bottom:1px solid #f0f1f3;display:flex;justify-content:space-between;align-items:center;font-weight:600}
  .kpi{padding:16px}
  .kpi-title{font-size:13px;color:#6b7280}
  .kpi-number{font-size:28px;font-weight:700;margin-top:6px}
  .table-wrap{overflow:auto}
  .table{width:100%;border-collapse:collapse}
  .table th,.table td{padding:10px 12px;border-bottom:1px solid #f3f4f6;text-align:left}
  .btn.small{font-size:12px;padding:6px 10px;border:1px solid #e5e7eb;border-radius:8px;text-decoration:none}
  .badge{display:inline-block;padding:4px 10px;border-radius:999px;font-size:12px;border:1px solid #e5e7eb}
  .badge.Inprogress{background:#fff7ed}
  .badge.Approved{background:#ecfeff}
  .badge.Rejected{background:#fef2f2}
  @media(max-width:900px){.container{padding:0 12px} .kpi-number{font-size:22px} .container>div:first-child{grid-template-columns:repeat(2,1fr)}}
</style>

<%@include file="/WEB-INF/views/common/_footer.jsp"%>
