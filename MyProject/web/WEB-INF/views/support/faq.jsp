<%@ page contentType="text/html; charset=UTF-8" %>
<!doctype html><html lang="vi"><head>
<meta charset="utf-8"><title>FAQ · LeaveMgmt</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css?v=6">
<style>.wrap{max-width:900px;margin:24px auto;padding:0 12px} details{border:1px solid #e5e7eb;border-radius:10px;padding:10px 14px;margin:10px 0;background:#fff}</style>
</head><body>
<jsp:include page="/WEB-INF/views/common/_admin_header.jsp"/>
<div class="wrap">
  <h1>Câu hỏi thường gặp</h1>

  <details open><summary><strong>1. Không gửi được đơn nghỉ?</strong></summary>
    <p>Hãy kiểm tra ngày bắt đầu/kết thúc, loại nghỉ và file đính kèm (nếu có) &lt; 5MB. Nếu vẫn lỗi, dùng nút <em>Trợ giúp</em> để gửi báo cáo kèm thông tin kỹ thuật.</p>
  </details>

  <details><summary><strong>2. Tôi không thấy quyền phê duyệt?</strong></summary>
    <p>Chỉ vai trò <em>MANAGER / DIV_LEADER</em> mới có menu phê duyệt. Liên hệ quản trị để cấp quyền.</p>
  </details>

  <details><summary><strong>3. Làm sao xuất CSV log?</strong></summary>
    <p>Trang <em>Admin → Audit</em> có nút <strong>Export CSV</strong>. Bạn có thể lọc trước khi xuất.</p>
  </details>
</div>
</body></html>
