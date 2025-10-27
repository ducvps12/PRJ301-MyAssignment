<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<title>404 – Trang không tồn tại</title>
<style>
  body {
    font-family: system-ui, Segoe UI, Roboto, sans-serif;
    background: #f9fafb;
    color: #333;
    margin: 0;
  }
  .wrap {
    max-width: 640px;
    margin: 100px auto;
    text-align: center;
    padding: 20px;
  }
  h1 {
    font-size: 64px;
    margin: 0;
    color: #e11d48;
  }
  h2 {
    margin-top: 8px;
    font-weight: 600;
  }
  p {
    color: #666;
    margin: 12px 0 24px;
  }
  a.btn {
    display: inline-block;
    padding: 10px 18px;
    border-radius: 8px;
    background: #2563eb;
    color: #fff;
    text-decoration: none;
  }
  a.btn:hover {
    background: #1e40af;
  }
</style>
</head>
<body>
  <div class="wrap">
    <h1>404</h1>
    <h2>Không tìm thấy trang yêu cầu</h2>
    <p>Trang bạn đang truy cập không tồn tại hoặc đã bị xóa.<br>
       Vui lòng kiểm tra lại đường dẫn hoặc quay về trang chính.</p>
    <a href="${pageContext.request.contextPath}/" class="btn">Về trang chủ</a>
  </div>
</body>
</html>
