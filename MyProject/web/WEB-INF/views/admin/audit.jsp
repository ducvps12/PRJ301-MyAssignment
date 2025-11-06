<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp" %>

<!DOCTYPE html>
<html lang="vi" data-theme="${sessionScope.theme != null ? sessionScope.theme : 'auto'}">
<head>
  <meta charset="UTF-8">
  <title>Audit Log · LeaveMgmt</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">

  <!-- BUMP version để tránh cache -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css?v=8">

  <!-- CSS nhỏ chỉ dành riêng cho trang Audit -->
  <style>
    .audit .filters{display:flex;gap:.5rem;flex-wrap:wrap;align-items:end}
    .audit .filters>*{display:flex;flex-direction:column}
    .audit .filters .grow{flex:1 1 240px}
    .audit .filters input,.audit .filters select{
      padding:.45rem .6rem;border:1px solid var(--bd);border-radius:.6rem;background:#fff
    }
    .audit .filters .btn{height:38px;line-height:38px;padding:0 14px}

    .audit .table{margin-top:.75rem;background:var(--card);border-radius:.8rem;overflow:auto;box-shadow:var(--shadow);padding-bottom:2px}
    .audit table{width:100%;min-width:960px;border-collapse:collapse}
    .audit th,.audit td{padding:.6rem .8rem;border-bottom:1px solid #eef2f7;font-size:14px}
    .audit th{background:var(--subtle);text-align:left}

    .chip{display:inline-block;padding:.2rem .5rem;border-radius:999px;font-size:12px;border:1px solid #e5e7eb}
    .chip.OK{background:#ecfdf5;border-color:#bbf7d0}
    .muted{color:var(--muted)}
    .toolbar{display:flex;justify-content:space-between;align-items:center;margin:.5rem 0}
    .pagination{display:flex;gap:.3rem}
    .pagination a{padding:.35rem .6rem;border:1px solid #e5e7eb;border-radius:.5rem;text-decoration:none}
    .ua{max-width:520px;overflow:hidden;white-space:nowrap;text-overflow:ellipsis}
  </style>
</head>

<body class="admin">
  <%-- Header cố định --%>
  <jsp:include page="/WEB-INF/views/common/_admin_header.jsp"/>

  <%-- Sidebar cố định. Nếu header của bạn đã tự include sidebar thì HÃY XÓA dòng dưới. --%>
  <jsp:include page="/WEB-INF/views/common/_admin_sidebar.jsp"/>

  <%-- TẤT CẢ nội dung bắt buộc nằm trong admin-main để né header/aside --%>
  <main class="admin-main">
    <div class="container audit">
      <h2>Audit Log</h2>

      <form class="filters" method="get" action="">
        <input type="hidden" name="csrf" value="${sessionScope.csrf}"/>

        <div>
          <label>User ID</label><br>
          <input type="number" name="userId" value="${param.userId}"/>
        </div>
        <div>
          <label>Action</label><br>
          <input name="action" placeholder="APPROVE_REQUEST / LOGOUT ..." value="${fn:escapeXml(param.action)}"/>
        </div>
        <div>
          <label>Từ ngày</label><br>
          <input type="date" name="from" value="${param.from}"/>
        </div>
        <div>
          <label>Đến ngày</label><br>
          <input type="date" name="to" value="${param.to}"/>
        </div>
        <div class="grow">
          <label>Tìm nhanh</label><br>
          <input name="q" placeholder="note, IP, user agent..." value="${fn:escapeXml(param.q)}"/>
        </div>
        <div>
          <label>Size</label><br>
          <input type="number" min="10" max="100" name="size" value="${empty result ? 20 : result.size}"/>
        </div>
        <div><button class="btn" type="submit">Lọc</button></div>

        <c:url var="csvUrl" value="">
          <c:param name="userId" value="${param.userId}"/>
          <c:param name="action" value="${param.action}"/>
          <c:param name="q" value="${param.q}"/>
          <c:param name="from" value="${param.from}"/>
          <c:param name="to" value="${param.to}"/>
          <c:param name="size" value="${empty result ? 20 : result.size}"/>
          <c:param name="page" value="${empty result ? 1 : result.page}"/>
          <c:param name="export" value="csv"/>
        </c:url>
        <div><a class="btn" href="${csvUrl}">↯ Export CSV</a></div>
      </form>

      <div class="toolbar">
        <div class="muted">
          Tổng: <strong>${empty result ? 0 : (empty result.totalItems ? result.total : result.totalItems)}</strong> bản ghi
        </div>
        <div class="pagination">
          <c:if test="${not empty result}">
            <c:forEach begin="1" end="${result.totalPages}" var="p">
              <c:url var="pageUrl" value="">
                <c:param name="userId" value="${param.userId}"/>
                <c:param name="action" value="${param.action}"/>
                <c:param name="q" value="${param.q}"/>
                <c:param name="from" value="${param.from}"/>
                <c:param name="to" value="${param.to}"/>
                <c:param name="size" value="${result.size}"/>
                <c:param name="page" value="${p}"/>
              </c:url>
              <a href="${pageUrl}" style="${p==result.page?'background:var(--subtle);font-weight:600':''}">${p}</a>
            </c:forEach>
          </c:if>
        </div>
      </div>

      <div class="table">
        <table>
          <thead>
          <tr>
            <th>Thời gian</th>
            <th>Người dùng</th>
            <th>Action</th>
            <th>Đối tượng</th>
            <th>Ghi chú</th>
            <th>IP</th>
            <th>User-Agent</th>
          </tr>
          </thead>
          <tbody>
          <c:choose>
            <c:when test="${empty result || empty result.items}">
              <tr><td colspan="7" class="muted" style="text-align:center">Không có bản ghi.</td></tr>
            </c:when>
            <c:otherwise>
              <c:forEach items="${result.items}" var="a">
                <tr>
                  <td class="muted">
                    <c:choose>
                      <c:when test="${a.createdAt ne null}">
                        <fmt:formatDate value="${a.createdAt}" pattern="yyyy-MM-dd HH:mm:ss"/>
                      </c:when>
                      <c:otherwise>-</c:otherwise>
                    </c:choose>
                  </td>
                  <td><c:out value="${a.userName}"/> <span class="muted">(#<c:out value="${a.userId}"/>)</span></td>
                  <td><span class="chip OK"><c:out value="${a.action}"/></span></td>
                  <td>
                    <c:out value="${a.entityType}"/>
                    <c:if test="${a.entityId != null}"> #<c:out value="${a.entityId}"/></c:if>
                  </td>
                  <td><c:out value="${a.note}"/></td>
                  <td class="muted"><c:out value="${a.ipAddr}"/></td>
                  <td class="ua muted" title="${a.userAgent}"><c:out value="${a.userAgent}"/></td>
                </tr>
              </c:forEach>
            </c:otherwise>
          </c:choose>
          </tbody>
        </table>
      </div>
    </div>
  </main>
</body>
</html>
