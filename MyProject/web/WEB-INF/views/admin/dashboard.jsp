<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<%@include file="/WEB-INF/views/common/_header.jsp"%>

<div class="container" style="max-width:1100px; margin:24px auto;">
  <h2 style="margin:0 0 6px 0;">Admin Dashboard</h2>
  <div class="muted" style="margin-bottom:16px">
    Department view:
    <strong>${viewDepartment}</strong>
  </div>

  <!-- KPI cards (khớp keys trong servlet: pendingAll, approvedThisMonth, totalThisMonth, approvalRate30d) -->
  <div class="kpi-grid">
    <div class="card kpi">
      <div class="kpi-title">Đơn chờ duyệt</div>
      <div class="kpi-number">${kpis.pendingAll}</div>
    </div>
    <div class="card kpi">
      <div class="kpi-title">Đơn duyệt trong tháng</div>
      <div class="kpi-number">${kpis.approvedThisMonth}</div>
    </div>
    <div class="card kpi">
      <div class="kpi-title">Tổng đơn trong tháng</div>
      <div class="kpi-number">${kpis.totalThisMonth}</div>
    </div>
    <div class="card kpi">
      <div class="kpi-title">Tỷ lệ duyệt 30 ngày</div>
      <div class="kpi-number">
        <fmt:formatNumber value="${kpis.approvalRate30d}" maxFractionDigits="1"/>%
      </div>
    </div>
  </div>

  <!-- Đang nghỉ hôm nay -->
  <div class="card" style="margin-top:18px;">
    <div class="card-header">Nhân sự đang nghỉ hôm nay</div>
    <div class="table-wrap">
      <table class="table">
        <thead>
        <tr>
          <th>Họ tên</th><th>Phòng</th><th>Từ</th><th>Đến</th><th>Số ngày</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="t" items="${todayOnLeave}">
          <tr>
            <td>${t.requester}</td>
            <td>${t.department}</td>
            <td><fmt:formatDate value="${t.startDate}" pattern="dd/MM/yyyy"/></td>
            <td><fmt:formatDate value="${t.endDate}" pattern="dd/MM/yyyy"/></td>
            <td>${t.days}</td>
          </tr>
        </c:forEach>
        <c:if test="${empty todayOnLeave}">
          <tr><td colspan="5" class="empty">Không có ai đang nghỉ hôm nay</td></tr>
        </c:if>
        </tbody>
      </table>
    </div>
  </div>

  <!-- Đơn mới nhất -->
  <div class="card" style="margin-top:18px;">
    <div class="card-header">
      Đơn nghỉ mới nhất
      <a href="${pageContext.request.contextPath}/request/list" class="btn small">Tới danh sách</a>
    </div>
    <div class="table-wrap">
      <table class="table">
        <thead>
        <tr>
          <th>#</th><th>Nội dung</th><th>Từ</th><th>Đến</th><th>Người tạo</th><th>Phòng</th><th>Trạng thái</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="r" items="${recentRequests}">
          <tr>
            <td>#${r.id}</td>
            <td>${r.title}</td>
            <td><fmt:formatDate value="${r.startDate}" pattern="dd/MM/yyyy"/></td>
            <td><fmt:formatDate value="${r.endDate}" pattern="dd/MM/yyyy"/></td>
            <td>${r.requester}</td>
            <td>${r.department}</td>
            <td>
              <c:set var="st" value="${fn:toUpperCase(r.status)}"/>
              <span class="badge
                <c:if test='${st eq "PENDING"}'>is-pending</c:if>
                <c:if test='${st eq "APPROVED"}'>is-approved</c:if>
                <c:if test='${st eq "REJECTED"}'>is-rejected</c:if>">
                ${st}
              </span>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty recentRequests}">
          <tr><td colspan="7" class="empty">Chưa có dữ liệu</td></tr>
        </c:if>
        </tbody>
      </table>
    </div>
  </div>
</div>

<style>
  .muted{color:#6b7280;font-size:13px}
  .card{background:#fff;border:1px solid #e5e7eb;border-radius:12px;box-shadow:0 1px 2px rgba(0,0,0,.04)}
  .card-header{padding:12px 16px;border-bottom:1px solid #f0f1f3;display:flex;justify-content:space-between;align-items:center;font-weight:600}
  .kpi-grid{display:grid;grid-template-columns:repeat(4,1fr);gap:14px}
  .kpi{padding:16px}
  .kpi-title{font-size:13px;color:#6b7280}
  .kpi-number{font-size:28px;font-weight:700;margin-top:6px}
  .table-wrap{overflow:auto}
  .table{width:100%;border-collapse:collapse}
  .table th,.table td{padding:10px 12px;border-bottom:1px solid #f3f4f6;text-align:left}
  .btn.small{font-size:12px;padding:6px 10px;border:1px solid #e5e7eb;border-radius:8px;text-decoration:none}
  .badge{display:inline-block;padding:4px 10px;border-radius:999px;font-size:12px;border:1px solid #e5e7eb}
  .badge.is-pending{background:#fff7ed}
  .badge.is-approved{background:#ecfeff}
  .badge.is-rejected{background:#fef2f2}
  .empty{text-align:center;opacity:.7}
  @media(max-width:900px){
    .container{padding:0 12px}
    .kpi-number{font-size:22px}
    .kpi-grid{grid-template-columns:repeat(2,1fr)}
  }
</style>

<%@include file="/WEB-INF/views/common/_footer.jsp"%>
