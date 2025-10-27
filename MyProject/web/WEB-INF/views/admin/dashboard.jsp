<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>

<%-- set page flag for active menu --%>
<c:set var="page" value="dashboard"/>

<%@ include file="/WEB-INF/views/admin/_admin_header.jsp" %>
<div class="ad-layout">
  <%@ include file="/WEB-INF/views/admin/_admin_sidebar.jsp" %>

  <main class="ad-content">
    <div class="ad-container">

      <h2 class="ad-title">Admin Dashboard</h2>
      <div class="muted" style="margin-bottom:16px">
        Department view: <strong><c:out value="${viewDepartment != null ? viewDepartment : '—'}"/></strong>
      </div>

      <c:if test="${not empty statsError}">
        <div class="ad-alert ad-alert-error">⚠ <c:out value="${statsError}"/></div>
      </c:if>

      <!-- KPI -->
      <div class="ad-kpis">
        <div class="ad-card kpi">
          <div class="kpi-title">Đơn chờ duyệt</div>
          <div class="kpi-number"><c:out value="${kpis != null ? kpis.pendingAll : 0}"/></div>
        </div>
        <div class="ad-card kpi">
          <div class="kpi-title">Đơn duyệt trong tháng</div>
          <div class="kpi-number"><c:out value="${kpis != null ? kpis.approvedThisMonth : 0}"/></div>
        </div>
        <div class="ad-card kpi">
          <div class="kpi-title">Tổng đơn trong tháng</div>
          <div class="kpi-number"><c:out value="${kpis != null ? kpis.totalThisMonth : 0}"/></div>
        </div>
        <div class="ad-card kpi">
          <div class="kpi-title">Tỷ lệ duyệt 30 ngày</div>
          <div class="kpi-number"><fmt:formatNumber value="${kpis != null ? kpis.approvalRate30d : 0}" maxFractionDigits="1"/>%</div>
        </div>
      </div>

      <!-- Today on leave -->
      <div class="ad-card" style="margin-top:18px;">
        <div class="ad-card-header">Nhân sự đang nghỉ hôm nay</div>
        <div class="ad-table-wrap">
          <table class="ad-table">
            <thead>
              <tr><th>Họ tên</th><th>Phòng</th><th>Từ</th><th>Đến</th><th>Số ngày</th></tr>
            </thead>
            <tbody>
              <c:forEach var="t" items="${todayOnLeave}">
                <tr>
                  <td><c:out value="${t.requester}"/></td>
                  <td><c:out value="${t.department}"/></td>
                  <td><fmt:formatDate value="${t.startDate}" pattern="dd/MM/yyyy"/></td>
                  <td><fmt:formatDate value="${t.endDate}" pattern="dd/MM/yyyy"/></td>
                  <td><c:out value="${t.days}"/></td>
                </tr>
              </c:forEach>
              <c:if test="${empty todayOnLeave}">
                <tr><td colspan="5" class="empty">Không có ai đang nghỉ hôm nay</td></tr>
              </c:if>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Recent -->
      <div class="ad-card" style="margin-top:18px;">
        <div class="ad-card-header">
          Đơn nghỉ mới nhất
          <a href="${pageContext.request.contextPath}/request/list" class="ad-btn sm">Tới danh sách</a>
        </div>
        <div class="ad-table-wrap">
          <table class="ad-table">
            <thead>
              <tr><th>#</th><th>Nội dung</th><th>Từ</th><th>Đến</th><th>Người tạo</th><th>Phòng</th><th>Trạng thái</th></tr>
            </thead>
            <tbody>
              <c:forEach var="r" items="${recentRequests}">
                <c:set var="st" value="${empty r.status ? '' : fn:toUpperCase(r.status)}"/>
                <tr>
                  <td>#<c:out value="${r.id}"/></td>
                  <td><c:out value="${r.title}"/></td>
                  <td><fmt:formatDate value="${r.startDate}" pattern="dd/MM/yyyy"/></td>
                  <td><fmt:formatDate value="${r.endDate}" pattern="dd/MM/yyyy"/></td>
                  <td><c:out value="${r.requester}"/></td>
                  <td><c:out value="${r.department}"/></td>
                  <td>
                    <span class="badge
                      <c:if test='${st eq "PENDING"}'>is-pending</c:if>
                      <c:if test='${st eq "APPROVED"}'>is-approved</c:if>
                      <c:if test='${st eq "REJECTED"}'>is-rejected</c:if>"><c:out value="${st}"/></span>
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
  </main>
</div>
<%@ include file="/WEB-INF/views/admin/_admin_footer.jsp" %>
