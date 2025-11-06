<%@ page contentType="text/html; charset=UTF-8" %>
<!doctype html><html lang="vi"><head>
<meta charset="utf-8"><title>Trạng thái dịch vụ · LeaveMgmt</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css?v=6">
<style>.wrap{max-width:800px;margin:24px auto;padding:0 12px}.ok{color:#16a34a}.warn{color:#ca8a04}</style>
</head><body>
<jsp:include page="/WEB-INF/views/common/_admin_header.jsp"/>
<div class="wrap">
  <h1>Trạng thái dịch vụ</h1>
  <ul>
    <li>Ứng dụng web: <strong class="ok">Hoạt động</strong></li>
    <li>Cơ sở dữ liệu: <strong class="ok">Hoạt động</strong></li>
    <li>Dịch vụ email: <strong class="ok">Hoạt động</strong></li>
    <li>Bảo trì kế tiếp: <strong class="warn">CN 02:00–03:00</strong></li>
  </ul>
</div>
</body></html>
