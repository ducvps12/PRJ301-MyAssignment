<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>Duyệt đơn nghỉ phép</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <style>
    body{font-family:system-ui,Segoe UI,Roboto,Helvetica,Arial,sans-serif;margin:24px}
    .card{max-width:720px;margin:auto;padding:20px;border:1px solid #e5e7eb;border-radius:12px;background:#fff}
    .row{display:grid;grid-template-columns:160px 1fr;gap:10px;align-items:start;margin:10px 0}
    textarea{width:100%;min-height:120px;padding:10px;border:1px solid #d1d5db;border-radius:8px;resize:vertical}
    .btn{padding:10px 14px;border:none;border-radius:8px;cursor:pointer}
    .btn-approve{background:#16a34a;color:#fff}
    .btn-reject{background:#dc2626;color:#fff}
    .btn-secondary{background:#e5e7eb}
    .info strong{display:inline-block;width:160px;color:#374151}
    .msg{font-size:14px;margin:6px 0}
    .msg.ok{color:#166534}
    .msg.error{color:#b91c1c}
  </style>
</head>
<body>
  <%@ include file="/WEB-INF/views/common/_header.jsp" %>

  <div class="card">
    <h2 style="margin-top:0">Duyệt đơn nghỉ phép</h2>

    <c:if test="${not empty message}">
      <div class="msg ok"><c:out value="${message}"/></div>
    </c:if>
    <c:if test="${not empty error}">
      <div class="msg error"><c:out value="${error}"/></div>
    </c:if>

    <c:if test="${not empty requestItem}">
      <div class="info">
        <p><strong>ID:</strong> #<c:out value="${requestItem.id}"/></p>
        <p><strong>Tiêu đề:</strong> <c:out value="${requestItem.title}"/></p>
        <p><strong>Người tạo:</strong> <c:out value="${requestItem.requester.fullName}"/></p>
        <p><strong>Khoảng thời gian:</strong>
          <fmt:formatDate value="${requestItem.startDate}" pattern="dd/MM/yyyy"/>
          –
          <fmt:formatDate value="${requestItem.endDate}" pattern="dd/MM/yyyy"/>
        </p>
        <p><strong>Loại:</strong> <c:out value="${requestItem.leaveType}"/></p>
        <p><strong>Lý do:</strong> <c:out value="${requestItem.reason}"/></p>
        <p><strong>Trạng thái hiện tại:</strong> <b><c:out value="${requestItem.status}"/></b></p>
      </div>

      <hr style="margin:20px 0">

      <form method="post" action="${pageContext.request.contextPath}/request/approve">
        <input type="hidden" name="id" value="${requestItem.id}">
        <div class="row">
          <label for="note">Ghi chú phản hồi</label>
          <textarea id="note" name="note" placeholder="Ghi chú (tuỳ chọn)"><c:out value="${param.note}"/></textarea>
        </div>

        <div class="row" style="grid-template-columns:1fr 1fr;gap:10px;margin-top:16px">
          <button type="submit" name="action" value="approve" class="btn btn-approve">✔ Duyệt</button>
          <button type="submit" name="action" value="reject"  class="btn btn-reject">✖ Từ chối</button>
        </div>
      </form>

      <div style="margin-top:16px">
        <a href="${pageContext.request.contextPath}/request/list" class="btn btn-secondary">← Quay lại danh sách</a>
      </div>
    </c:if>

    <c:if test="${empty requestItem}">
      <p style="color:#6b7280">Không tìm thấy đơn nghỉ phép.</p>
    </c:if>
  </div>

  <%@ include file="/WEB-INF/views/common/_footer.jsp" %>
</body>
</html>
