<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.io.*" %>
<%
  String mode = getServletContext().getInitParameter("app.mode");
  Throwable ex = (Throwable) request.getAttribute("jakarta.servlet.error.exception");
  Integer code = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
  String msg = (String) request.getAttribute("jakarta.servlet.error.message");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<title>500 – Lỗi hệ thống</title>
<style>
  body{font-family:system-ui,Segoe UI,Roboto,Arial,sans-serif;background:#fafafa;margin:0}
  .wrap{max-width:820px;margin:60px auto;padding:0 16px}
  .card{background:#fff;border:1px solid #e5e7eb;border-radius:12px;box-shadow:0 2px 12px rgba(0,0,0,.04);padding:24px}
  .title{font-size:22px;margin:0 0 8px}
  .muted{color:#6b7280}
  pre{overflow:auto;background:#0b1020;color:#f0f3f8;padding:12px;border-radius:8px}
  .btn{display:inline-block;padding:10px 16px;border-radius:10px;border:1px solid #e5e7eb;text-decoration:none}
</style>
</head>
<body>
  <div class="wrap">
    <div class="card">
      <h1 class="title">Có lỗi xảy ra (500)</h1>
      <p class="muted">Mã lỗi: <b><%= code %></b></p>
      <p><b><%= (msg!=null?msg:"Đã có sự cố trong quá trình xử lý.") %></b></p>
      <a class="btn" href="${pageContext.request.contextPath}/">Về trang chủ</a>

      <% if ("dev".equalsIgnoreCase(mode) && ex != null) { %>
        <h3>Chi tiết (DEV only)</h3>
        <pre><%
          StringWriter sw = new StringWriter();
          ex.printStackTrace(new PrintWriter(sw));
          out.print(sw.toString());
        %></pre>
      <% } %>
    </div>
  </div>
</body>
</html>
