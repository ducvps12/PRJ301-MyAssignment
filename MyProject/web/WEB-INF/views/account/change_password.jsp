<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp" %>
<!DOCTYPE html>
<html lang="vi" data-theme="${sessionScope.theme != null ? sessionScope.theme : 'auto'}">
<head>
  <meta charset="UTF-8">
  <title>Đổi mật khẩu</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/form.css?v=1">
  <style>
    .cp-card{max-width:520px;margin:24px auto;padding:24px;border-radius:16px;background:var(--card,#fff);box-shadow:0 6px 24px rgba(0,0,0,.08)}
    .cp-card h2{margin:0 0 12px;font-size:20px}
    .cp-card .row{margin:12px 0}
    .cp-card input{width:100%;padding:10px 12px;border:1px solid var(--line,#e5e7eb);border-radius:12px}
    .cp-card .actions{display:flex;gap:12px;justify-content:flex-end;margin-top:16px}
    .msg-error{background:#fee2e2;color:#991b1b;padding:10px 12px;border-radius:12px;margin-bottom:12px}
  </style>
</head>
<body>
<jsp:include page="/WEB-INF/views/common/_header.jsp"/>

<div class="wrap">
  <div class="cp-card">
    <h2>Đổi mật khẩu</h2>
    <c:if test="${not empty err}">
      <div class="msg-error">${err}</div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/account/change-password" autocomplete="off">
      <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"/>

      <div class="row">
        <label>Mật khẩu hiện tại</label>
        <input type="password" name="current" required />
      </div>
      <div class="row">
        <label>Mật khẩu mới</label>
        <input id="pw1" type="password" name="pass1" minlength="8" required />
      </div>
      <div class="row">
        <label>Nhập lại mật khẩu mới</label>
        <input id="pw2" type="password" name="pass2" minlength="8" required />
      </div>
      <div class="actions">
        <a class="btn" href="${pageContext.request.contextPath}/request/list">Hủy</a>
        <button class="btn btn-primary" type="submit">Lưu thay đổi</button>
      </div>
    </form>
  </div>
</div>

<jsp:include page="/WEB-INF/views/common/_footer.jsp"/>
<script>
  // cảnh báo nhanh khi retype sai
  const p1=document.getElementById('pw1'), p2=document.getElementById('pw2');
  [p1,p2].forEach(i=>i.addEventListener('input',()=>{
    p2.setCustomValidity(p1.value===p2.value?'':'Mật khẩu nhập lại không khớp');
  }));
</script>
</body>
</html>
