<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>Danh sách đơn nghỉ phép</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <style>
    body{font-family:system-ui,Segoe UI,Roboto,Helvetica,Arial,sans-serif;margin:24px}
    .wrap{max-width:1000px;margin:auto}
    .toolbar{display:flex;gap:10px;flex-wrap:wrap;align-items:center;margin-bottom:12px}
    .card{border:1px solid #e5e7eb;border-radius:12px;overflow:hidden}
    table{width:100%;border-collapse:collapse}
    th,td{padding:10px 12px;border-top:1px solid #eef2f7}
    th{background:#f8fafc;text-align:left;font-weight:600}
    .actions a,.btn{display:inline-block;padding:8px 12px;border-radius:8px;border:1px solid #e5e7eb;text-decoration:none}
    .btn-primary{background:#2563eb;color:#fff;border-color:#2563eb}
    .pill{display:inline-block;padding:2px 8px;border-radius:999px;font-size:12px;font-weight:600}
    .pill.NEW{background:#e0f2fe;color:#075985}
    .pill.INPROGRESS{background:#fff7ed;color:#9a3412}
    .pill.APPROVED{background:#dcfce7;color:#14532d}
    .pill.REJECTED{background:#fee2e2;color:#7f1d1d}
    .pill.CANCELLED{background:#e5e7eb;color:#4b5563}
    .pagination{display:flex;gap:6px;justify-content:flex-end;padding:10px}
    .pagination a{padding:6px 10px;border:1px solid #e5e7eb;border-radius:8px;text-decoration:none}
    .muted{color:#6b7280}
    input[type="text"], select{padding:8px 10px;border:1px solid #d1d5db;border-radius:8px}
  </style>
</head>
<body>
  <%@ include file="/WEB-INF/views/common/_header.jsp" %>

  <div class="wrap">
    <div class="toolbar">
      <a class="btn btn-primary" href="${pageContext.request.contextPath}/request/create">+ Tạo đơn</a>
      <a class="btn" href="${pageContext.request.contextPath}/request/agenda">📅 Xem Agenda</a>

      <form method="get" action="${pageContext.request.contextPath}/request/list" class="toolbar" style="margin-left:auto">
        <select name="status">
          <option value="">-- Tất cả trạng thái --</option>
          <option value="PENDING"   ${param.status == 'PENDING'   ? 'selected' : ''}>Chờ duyệt</option>
          <option value="APPROVED"  ${param.status == 'APPROVED'  ? 'selected' : ''}>Đã duyệt</option>
          <option value="REJECTED"  ${param.status == 'REJECTED'  ? 'selected' : ''}>Từ chối</option>
          <option value="CANCELLED" ${param.status == 'CANCELLED' ? 'selected' : ''}>Đã hủy</option>
        </select>
        <select name="mine">
          <option value="">Của mọi người</option>
          <option value="1" ${param.mine == '1' ? 'selected' : ''}>Chỉ của tôi</option>
        </select>
        <input type="text" name="q" value="${fn:escapeXml(param.q)}" placeholder="Tìm lý do/người tạo...">
        <button class="btn" type="submit">Lọc</button>
      </form>
    </div>

    <c:if test="${not empty requestScope.message}">
      <div class="pill APPROVED" style="margin:8px 0">${requestScope.message}</div>
    </c:if>
    <c:if test="${not empty requestScope.error}">
      <div class="pill REJECTED" style="margin:8px 0">${requestScope.error}</div>
    </c:if>

    <div class="card">
      <table>
        <thead>
          <tr>
            <th style="width:64px">ID</th>
            <th>Nội dung (reason)</th>
            <th>Người tạo (ID)</th>
            <th>Khoảng thời gian</th>
            <th>Trạng thái</th>
            <th style="width:210px">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="r" items="${requests}">
            <tr>
              <td>#${r.id}</td>
              <td>${fn:escapeXml(r.reason)}</td>
              <td>#${r.createdBy}</td>
             <td>
  <c:choose>
    <c:when test="${not empty r.startDate}">
      <%-- r.startDate / r.endDate là LocalDate -> toString() = yyyy-MM-dd --%>
      <fmt:parseDate value="${r.startDate}" pattern="yyyy-MM-dd" var="sd"/>
      <fmt:parseDate value="${r.endDate}"   pattern="yyyy-MM-dd" var="ed"/>

      <fmt:formatDate value="${sd}" pattern="dd/MM/yyyy"/> –
      <fmt:formatDate value="${ed}" pattern="dd/MM/yyyy"/>
    </c:when>
    <c:otherwise>—</c:otherwise>
  </c:choose>
</td>

              <td>
                <span class="pill ${fn:toUpperCase(r.status)}">
                  ${fn:toUpperCase(r.status)}
                </span>
              </td>
              <td class="actions">
                <a href="${pageContext.request.contextPath}/request/detail?id=${r.id}">Xem</a>

                <c:if test="${sessionScope.role == 'MANAGER'}">
                  <c:if test="${fn:toLowerCase(r.status) == 'pending'}">
                    <a href="${pageContext.request.contextPath}/request/approve?id=${r.id}&ok=1">Duyệt</a>
                    <a href="${pageContext.request.contextPath}/request/approve?id=${r.id}&ok=0">Từ chối</a>
                  </c:if>
                </c:if>

                <c:if test="${fn:toLowerCase(r.status) == 'pending' && r.createdBy == sessionScope.userId}">
                  <a href="${pageContext.request.contextPath}/request/cancel?id=${r.id}">Hủy</a>
                </c:if>
              </td>
            </tr>
          </c:forEach>

          <c:if test="${empty requests}">
            <tr><td colspan="6" class="muted" style="text-align:center;padding:24px">Không có dữ liệu.</td></tr>
          </c:if>
        </tbody>
      </table>

      <c:if test="${totalPages > 1}">
        <div class="pagination">
          <c:url var="baseUrl" value="/request/list">
            <c:param name="q" value="${param.q}" />
            <c:param name="status" value="${param.status}" />
            <c:param name="mine" value="${param.mine}" />
          </c:url>

          <c:forEach var="p" begin="1" end="${totalPages}">
            <c:choose>
              <c:when test="${p == page}">
                <span class="btn" style="background:#f3f4f6">${p}</span>
              </c:when>
              <c:otherwise>
                <a href="${baseUrl}&page=${p}">${p}</a>
              </c:otherwise>
            </c:choose>
          </c:forEach>
        </div>
      </c:if>
    </div>
  </div>

  <%@ include file="/WEB-INF/views/common/_footer.jsp" %>
</body>
</html>
