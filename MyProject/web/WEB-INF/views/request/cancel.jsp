<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>Hủy yêu cầu #${r.id}</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <style>
    body{font-family:system-ui,Segoe UI,Roboto,Helvetica,Arial,sans-serif;background:#f7f7f8;margin:0}
    .wrap{max-width:720px;margin:32px auto;padding:0 16px}
    .card{background:#fff;border:1px solid #e5e7eb;border-radius:14px;box-shadow:0 1px 4px rgba(0,0,0,.04);padding:18px}
    textarea,input,button{width:100%;box-sizing:border-box;font:inherit}
    textarea{min-height:120px;padding:10px;border:1px solid #e5e7eb;border-radius:10px}
    .row{margin-top:12px}
    .btn{padding:10px 14px;border-radius:10px;border:1px solid #e5e7eb;background:#fff;cursor:pointer}
    .danger{background:#fee2e2;border-color:#ef4444;color:#991b1b}
    .muted{color:#6b7280}
  </style>
</head>
<body>
<div class="wrap">
  <div class="card">
    <h2 style="margin-top:0">Hủy yêu cầu #${r.id}</h2>
    <div class="muted">Tiêu đề: ${fn:escapeXml(r.title)}</div>
    <div class="muted">Trạng thái hiện tại: ${r.status}</div>

    <c:if test="${not empty error}">
      <div style="margin-top:10px;color:#b91c1c;background:#fee2e2;border:1px solid #fecaca;padding:8px;border-radius:8px">
        ${error}
      </div>
    </c:if>

    <c:choose>
      <c:when test="${!(sessionScope.user.userId == r.createdBy && r.status eq 'INPROGRESS')}">
        <p class="row">Bạn không thể hủy yêu cầu này.</p>
        <a class="btn" href="${pageContext.request.contextPath}/request/detail?id=${r.id}">Quay lại</a>
      </c:when>
      <c:otherwise>
        <form method="post" action="${pageContext.request.contextPath}/request/cancel">
          <input type="hidden" name="id" value="${r.id}">
          <div class="row">
            <label>Lý do hủy (tuỳ chọn)</label>
            <textarea name="note" placeholder="Ví dụ: Đổi kế hoạch cá nhân..."></textarea>
          </div>
          <div class="row" style="display:flex;gap:8px">
            <button class="btn danger" type="submit">Xác nhận hủy</button>
            <a class="btn" href="${pageContext.request.contextPath}/request/detail?id=${r.id}">Huỷ bỏ</a>
          </div>
        </form>
      </c:otherwise>
    </c:choose>
  </div>
</div>
</body>
</html>
