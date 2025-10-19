<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8"><title>403 – Không có quyền</title>
<style>
  body{font-family:system-ui,Segoe UI,Roboto,Arial,sans-serif;background:#fafafa;margin:0}
  .wrap{max-width:720px;margin:60px auto;padding:0 16px}
  .card{background:#fff;border:1px solid #e5e7eb;border-radius:12px;box-shadow:0 2px 12px rgba(0,0,0,.04);padding:24px}
  .title{font-size:22px;margin:0 0 8px}
  .muted{color:#6b7280;margin:0 0 16px}
  .btn{display:inline-block;padding:10px 16px;border-radius:10px;border:1px solid #e5e7eb;text-decoration:none}
</style>
</head>
<body>
  <div class="wrap">
    <div class="card">
      <h1 class="title">403 – Bạn không có quyền</h1>
      <p class="muted"><%= request.getAttribute("message")!=null ? request.getAttribute("message") : "Vui lòng liên hệ quản trị viên nếu bạn cần cấp quyền." %></p>
      <a class="btn" href="<%= request.getContextPath() %>/">Quay lại trang chủ</a>
    </div>
  </div>
</body>
</html>
