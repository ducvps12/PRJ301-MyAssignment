<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://jakarta.ee/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://jakarta.ee/jsp/jstl/fmt" %>
<!doctype html><html lang="vi"><head>
<meta charset="utf-8"><title>Support Tickets · Admin</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css?v=6">
<style>
.wrap{max-width:1100px;margin:18px auto;padding:0 12px}
table{width:100%;border-collapse:collapse;background:#fff;border-radius:12px;overflow:hidden}
th,td{padding:.6rem .8rem;border-bottom:1px solid #e5e7eb;font-size:14px} th{text-align:left;background:#f8fafc}
.filters{display:flex;gap:.5rem;align-items:end;flex-wrap:wrap;margin:.5rem 0}
.chip{display:inline-block;padding:.15rem .5rem;border:1px solid #e5e7eb;border-radius:999px}
</style>
</head><body>
<jsp:include page="/WEB-INF/views/common/_admin_header.jsp"/>
<div class="wrap">
  <h2>Support Tickets</h2>

  <form method="get" class="filters">
    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
    <label>Status<br>
      <select name="status">
        <option value="">-- All --</option>
        <c:forEach var="op" items="${['OPEN','INPROGRESS','RESOLVED','CLOSED']}">
          <option value="${op}" <c:if test="${op==status}">selected</c:if>>${op}</option>
        </c:forEach>
      </select>
    </label>
    <label>Từ khoá<br>
      <input type="text" name="q" value="${param.q}" placeholder="title, email, user...">
    </label>
    <label>Size<br><input type="number" min="10" max="100" name="size" value="${result.size}"></label>
    <button class="btn">Lọc</button>
  </form>

  <table>
    <thead><tr>
      <th>#</th><th>Thời gian</th><th>Người gửi</th><th>Email</th><th>Tiêu đề</th><th>Trạng thái</th><th></th>
    </tr></thead>
    <tbody>
      <c:forEach items="${result.items}" var="t">
        <tr>
          <td>${t.id}</td>
          <td><fmt:formatDate value="${t.createdAt}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
          <td><c:out value="${t.userName}"/> <span class="muted">(#<c:out value="${t.userId}"/>)</span></td>
          <td><c:out value="${t.email}"/></td>
          <td><c:out value="${t.title}"/></td>
          <td><c:set var="s" value="${t.status}"/><jsp:include page="/WEB-INF/views/admin/_ticket_status.jspf"/></td>
          <td><a class="btn" href="${pageContext.request.contextPath}/admin/support?view=detail&id=${t.id}">Xem</a></td>
        </tr>
      </c:forEach>
      <c:if test="${empty result.items}">
        <tr><td colspan="7" style="text-align:center;opacity:.7">Không có ticket</td></tr>
      </c:if>
    </tbody>
  </table>

  <div style="margin:.5rem 0">
    Trang ${result.page}/${result.totalPages} — Tổng ${result.total}
    <c:if test="${result.page>1}">
      <a href="?page=${result.page-1}&size=${result.size}&status=${status}&q=${param.q}">« Trước</a>
    </c:if>
    <c:if test="${result.page<result.totalPages}">
      <a href="?page=${result.page+1}&size=${result.size}&status=${status}&q=${param.q}">Sau »</a>
    </c:if>
  </div>
</div>
</body></html>
