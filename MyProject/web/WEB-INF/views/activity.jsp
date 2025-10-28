<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="cp" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<title>Lịch sử hoạt động</title>
<meta name="viewport" content="width=device-width, initial-scale=1" />
<style>
  :root{--bg:#f7f7f8;--card:#fff;--b:#e5e7eb;--muted:#6b7280}
  body{font-family:system-ui,Segoe UI,Roboto,Arial,sans-serif;background:var(--bg);margin:0}
  .wrap{max-width:1100px;margin:20px auto;padding:0 16px}
  .card{background:var(--card);border:1px solid var(--b);border-radius:14px;overflow:hidden}
  .toolbar{display:flex;gap:12px;align-items:center;justify-content:space-between;padding:12px;border-bottom:1px solid var(--b)}
  .btn{border:1px solid var(--b);background:#fff;padding:8px 12px;border-radius:10px;text-decoration:none}
  .muted{color:var(--muted)}
  table{width:100%;border-collapse:collapse}
  th,td{padding:10px;border-bottom:1px solid var(--b);text-align:left;vertical-align:top}
  .pager{display:flex;gap:8px;justify-content:flex-end;padding:12px}
</style>
</head>
<body>
<div class="wrap">
  <h2>Lịch sử hoạt động</h2>

  <div class="card">
    <div class="toolbar">
      <div class="muted">
        <c:choose>
          <c:when test="${scope == 'admin'}">Quản trị</c:when>
          <c:otherwise>Của tôi</c:otherwise>
        </c:choose>
        • Tổng: <strong>${pg.total}</strong>
        <c:if test="${scope == 'admin' && not empty userFilter}"> • userId=<code>${userFilter}</code></c:if>
      </div>

      <c:if test="${scope == 'admin'}">
        <form method="get" action="${cp}/admin/activity" style="display:flex;gap:8px;align-items:center">
          <label class="muted">userId</label>
          <input type="number" name="userId" value="${param.userId}" style="padding:8px;border:1px solid var(--b);border-radius:10px"/>
          <label class="muted">size</label>
          <input type="number" name="size" value="${pg.size}" style="padding:8px;border:1px solid var(--b);border-radius:10px"/>
          <button class="btn" type="submit">Lọc</button>
          <a class="btn" href="${cp}/admin/activity">Xóa lọc</a>
        </form>
      </c:if>
    </div>

    <table>
      <thead>
        <tr>
          <th>#</th><th>Thời gian</th><th>userId</th>
          <th>Hành động</th><th>Đối tượng</th><th>Ghi chú</th><th>IP / UA</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="r" items="${pg.items}">
          <tr>
            <td>${r.id}</td>
            <td><fmt:formatDate value="${r.createdAt}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
            <td><c:out value="${r.userId}"/></td>
            <td><c:out value="${r.action}"/></td>
            <td>
              <strong><c:out value="${r.entityType}"/></strong>
              <div class="muted">ID: <c:out value="${r.entityId}"/></div>
            </td>
            <td style="max-width:460px"><c:out value="${r.note}"/></td>
            <td style="max-width:320px">
              <div><code><c:out value="${r.ip}"/></code></div>
              <div class="muted"><c:out value="${r.ua}"/></div>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty pg.items}">
          <tr><td colspan="7" class="muted" style="padding:14px">Không có bản ghi.</td></tr>
        </c:if>
      </tbody>
    </table>

    <div class="pager">
      <c:set var="prev" value="${pg.page - 1}" />
      <c:set var="next" value="${pg.page + 1}" />
      <c:choose>
        <c:when test="${scope == 'admin'}">
          <a class="btn" href="?page=1&size=${pg.size}&userId=${param.userId}" ${pg.page==1?"disabled":""}>« Đầu</a>
          <a class="btn" href="?page=${prev}&size=${pg.size}&userId=${param.userId}" ${pg.page==1?"disabled":""}>‹ Trước</a>
          <span>Trang <strong>${pg.page}</strong></span>
          <a class="btn" href="?page=${next}&size=${pg.size}&userId=${param.userId}" ${(pg.page*pg.size>=pg.total)?"disabled":""}>Tiếp ›</a>
        </c:when>
        <c:otherwise>
          <a class="btn" href="${cp}/activity?page=1&size=${pg.size}" ${pg.page==1?"disabled":""}>« Đầu</a>
          <a class="btn" href="${cp}/activity?page=${prev}&size=${pg.size}" ${pg.page==1?"disabled":""}>‹ Trước</a>
          <span>Trang <strong>${pg.page}</strong></span>
          <a class="btn" href="${cp}/activity?page=${next}&size=${pg.size}" ${(pg.page*pg.size>=pg.total)?"disabled":""}>Tiếp ›</a>
        </c:otherwise>
      </c:choose>
    </div>
  </div>
</div>
</body>
</html>
