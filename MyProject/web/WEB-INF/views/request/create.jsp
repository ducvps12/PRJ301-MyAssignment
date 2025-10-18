<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>Tạo đơn nghỉ phép</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <style>
    body{font-family:system-ui,-apple-system,Segoe UI,Roboto,Helvetica,Arial,sans-serif;background:#f7f7f8;margin:0}
    .wrap{max-width:720px;margin:40px auto;padding:0 16px}
    .card{background:#fff;border:1px solid #e5e7eb;border-radius:12px;box-shadow:0 2px 10px rgba(0,0,0,.04);padding:20px}
    h2{margin:0 0 16px 0}
    label{display:block;font-weight:600;margin-top:12px;margin-bottom:6px}
    input[type="text"], input[type="date"], textarea{
      width:100%;padding:10px 12px;border:1px solid #d1d5db;border-radius:8px;background:#fff
    }
    textarea{min-height:110px;resize:vertical}
    .row{display:grid;grid-template-columns:1fr 1fr;gap:12px}
    .actions{margin-top:16px;display:flex;gap:8px}
    .btn{padding:10px 14px;border:none;border-radius:8px;cursor:pointer;font-weight:600}
    .btn-primary{background:#1a73e8;color:#fff}
    .btn-muted{background:#eef2f7;color:#111}
    .alert{padding:10px 12px;border-radius:8px;margin-bottom:12px}
    .alert-error{background:#fde8e8;border:1px solid #fecaca;color:#b91c1c}
    .alert-ok{background:#e7f5ff;border:1px solid #b6e0fe;color:#0b65c2}
    small.muted{color:#6b7280}
  </style>
</head>
<body>
  <div class="wrap">
    <h2>Tạo đơn nghỉ phép</h2>

    <div class="card">
      <!-- Thông báo -->
      <c:if test="${not empty error}">
        <div class="alert alert-error">${error}</div>
      </c:if>
      <c:if test="${not empty message}">
        <div class="alert alert-ok">${message}</div>
      </c:if>

      <!-- Form -->
      <form method="post" action="${pageContext.request.contextPath}/request/create">
        <!-- CSRF (server đã set sessionScope._csrf trong AuthServlet) -->
        <input type="hidden" name="_csrf" value="${sessionScope._csrf}"/>

        <!-- (Tùy chọn) Title chỉ để mô tả ngắn, sẽ được gộp vào reason ở server -->
        <label for="title">Tiêu đề (tùy chọn)</label>
        <input id="title" name="title" type="text"
               placeholder="VD: Nghỉ phép cá nhân"
               value="${fn:escapeXml(param.title)}"/>

        <label for="reason">Lý do <span style="color:#b91c1c">*</span></label>
        <textarea id="reason" name="reason" required
                  placeholder="Mô tả lý do xin nghỉ (bắt buộc)">${fn:escapeXml(param.reason)}</textarea>
        <small class="muted">Vui lòng không nhập thông tin nhạy cảm.</small>

        <div class="row">
          <div>
            <label for="start_date">Từ ngày <span style="color:#b91c1c">*</span></label>
            <input id="start_date" name="start_date" type="date" required
                   value="${fn:escapeXml(param.start_date)}"/>
          </div>
          <div>
            <label for="end_date">Đến ngày <span style="color:#b91c1c">*</span></label>
            <input id="end_date" name="end_date" type="date" required
                   value="${fn:escapeXml(param.end_date)}"/>
          </div>
        </div>

        <div class="actions">
          <button class="btn btn-primary" type="submit">Gửi đơn</button>
          <a class="btn btn-muted" href="${pageContext.request.contextPath}/request/list">Quay lại</a>
        </div>
      </form>

      <small class="muted">Trạng thái mặc định sau khi gửi: <b>Inprogress</b>.</small>
    </div>
  </div>

  <script>
    // Chặn chọn end < start (UI-only, server vẫn phải validate)
    (function(){
      const s = document.getElementById('start_date');
      const e = document.getElementById('end_date');
      function sync(){
        if (s.value) e.min = s.value;
        if (e.value) s.max = e.value;
      }
      s.addEventListener('change', sync);
      e.addEventListener('change', sync);
      sync();
    })();
  </script>
</body>
</html>
