<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://jakarta.ee/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://jakarta.ee/jsp/jstl/fmt" %>
<!doctype html><html lang="vi"><head>
<meta charset="utf-8"><title>Ticket #${ticket.id}</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css?v=6">
<style>.wrap{max-width:900px;margin:18px auto;padding:0 12px}.box{background:#fff;border:1px solid #e5e7eb;border-radius:12px;padding:12px}</style>
</head><body>
<jsp:include page="/WEB-INF/views/common/_admin_header.jsp"/>
<div class="wrap">
  <a href="${pageContext.request.contextPath}/admin/support" class="btn">← Danh sách</a>
  <h2>Ticket #${ticket.id}</h2>
  <div class="box">
    <p><b>Thời gian:</b> <fmt:formatDate value="${ticket.createdAt}" pattern="yyyy-MM-dd HH:mm:ss"/></p>
    <p><b>Người gửi:</b> <c:out value="${ticket.userName}"/> <span class="muted">(#<c:out value="${ticket.userId}"/>)</span></p>
    <p><b>Email:</b> <c:out value="${ticket.email}"/></p>
    <p><b>Tiêu đề:</b> <c:out value="${ticket.title}"/></p>
    <p><b>Nội dung:</b><br><pre style="white-space:pre-wrap"><c:out value="${ticket.body}"/></pre></p>
    <c:if test="${not empty ticket.techJson}">
      <p><b>Tech info:</b><br><pre style="white-space:pre-wrap;background:#0b1220;color:#cbd5e1;border-radius:10px;padding:10px"><c:out value="${ticket.techJson}"/></pre></p>
    </c:if>
    <p><b>Trạng thái:</b> <c:set var="s" value="${ticket.status}"/><jsp:include page="/WEB-INF/views/admin/_ticket_status.jspf"/></p>
  </div>

  <h3>Cập nhật</h3>
  <form method="post" action="${pageContext.request.contextPath}/admin/support">
    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
    <input type="hidden" name="action" value="update">
    <input type="hidden" name="id" value="${ticket.id}">
    <label>Status</label><br>
    <select name="status">
      <c:forEach var="op" items="${['OPEN','INPROGRESS','RESOLVED','CLOSED']}">
        <option value="${op}" <c:if test="${op==ticket.status}">selected</c:if>>${op}</option>
      </c:forEach>
    </select>
    <br><label>Ghi chú</label><br>
    <textarea name="note" rows="3" placeholder="Thêm ghi chú xử lý...">${ticket.note}</textarea>
    <br><button class="btn">Lưu</button>
  </form>
</div>
</body></html>
