<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ include file="/WEB-INF/views/admin/_admin_header.jsp" %>
<%@ include file="/WEB-INF/views/admin/_admin_sidebar.jsp" %>

<div class="main">
  <div class="topbar">
    <button class="btn" onclick="toggleSidebar()">☰</button>
    <div class="muted">View: <span class="pill">${viewDepartment}</span></div>
    <a class="btn" href="${pageContext.request.contextPath}/logout">Đăng xuất</a>
  </div>

  <div class="content">
    <h2 style="margin:0 0 8px">Admin Dashboard</h2>
    <div class="muted" style="margin-bottom:14px">Tổng quan tình hình nghỉ phép</div>

    <!-- KPI -->
    <div class="kpis" style="margin-bottom:14px">
      <div class="card">
        <h3>Đang chờ duyệt</h3>
        <div class="num">${kpis.pendingAll}</div>
      </div>
      <div class="card">
        <h3>Đã duyệt trong tháng</h3>
        <div class="num">${kpis.approvedThisMonth}</div>
      </div>
      <div class="card">
        <h3>Tổng đơn trong tháng</h3>
        <div class="num">${kpis.totalThisMonth}</div>
      </div>
      <div class="card">
        <h3>Tỉ lệ duyệt 30 ngày</h3>
        <div class="num">
          <fmt:formatNumber value="${kpis.approvalRate30d}" maxFractionDigits="1"/>%
        </div>
        <div class="muted" style="font-size:12px">Base: ${kpis.approvalBase30d}</div>
      </div>
    </div>

    <div class="grid">
      <!-- Recent requests -->
      <div class="card">
        <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:10px">
          <h3 style="margin:0;color:var(--tx)">Đơn gần đây</h3>
          <a class="pill" href="${pageContext.request.contextPath}/request/list">Xem tất cả →</a>
        </div>
        <table class="table">
          <thead>
          <tr>
            <th>#</th>
            <th>Tiêu đề</th>
            <th>Người tạo</th>
            <th>Phòng ban</th>
            <th>Ngày</th>
            <th>Trạng thái</th>
            <th>Ngày tạo</th>
          </tr>
          </thead>
          <tbody>
          <c:forEach var="r" items="${recentRequests}">
            <tr>
              <td>${r.id}</td>
              <td><a href="${pageContext.request.contextPath}/request/detail?id=${r.id}">${r.title}</a></td>
              <td>${r.requester}</td>
              <td>${r.department}</td>
              <td>
                <fmt:formatDate value="${r.startDate}" pattern="dd/MM"/>–<fmt:formatDate value="${r.endDate}" pattern="dd/MM"/>
                <span class="pill" style="margin-left:6px">${r.days}d</span>
              </td>
              <td><span class="status ${r.status}">${r.status}</span></td>
              <td><fmt:formatDate value="${r.createdAt}" pattern="dd/MM HH:mm"/></td>
            </tr>
          </c:forEach>
          <c:if test="${empty recentRequests}">
            <tr><td colspan="7" class="muted">Chưa có dữ liệu</td></tr>
          </c:if>
          </tbody>
        </table>
      </div>

      <!-- Today on leave -->
      <div class="card">
        <h3 style="margin:0 0 10px 0;color:var(--tx)">Nghỉ hôm nay</h3>
        <c:choose>
          <c:when test="${empty todayOnLeave}">
            <div class="muted">Không có ai nghỉ hôm nay.</div>
          </c:when>
          <c:otherwise>
            <table class="table">
              <thead>
              <tr>
                <th>Nhân sự</th>
                <th>Phòng ban</th>
                <th>Từ</th>
                <th>Đến</th>
                <th>Số ngày</th>
              </tr>
              </thead>
              <tbody>
              <c:forEach var="t" items="${todayOnLeave}">
                <tr>
                  <td>${t.requester}</td>
                  <td>${t.department}</td>
                  <td><fmt:formatDate value="${t.startDate}" pattern="dd/MM"/></td>
                  <td><fmt:formatDate value="${t.endDate}" pattern="dd/MM"/></td>
                  <td>${t.days}</td>
                </tr>
              </c:forEach>
              </tbody>
            </table>
          </c:otherwise>
        </c:choose>
      </div>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/views/admin/_admin_footer.jsp" %>
