<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://jakarta.ee/jsp/jstl/core" %>
<!doctype html><html lang="vi"><head>
<meta charset="utf-8"><title>Tài liệu sử dụng · LeaveMgmt</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css?v=6">
<style>.wrap{max-width:900px;margin:24px auto;padding:0 12px} h2{margin:8px 0}</style>
</head><body>
<jsp:include page="/WEB-INF/views/common/_admin_header.jsp"/>
<div class="wrap">
  <h1>Tài liệu sử dụng</h1>
  <ol>
    <li><a href="#request">Tạo & theo dõi đơn nghỉ</a></li>
    <li><a href="#approve">Phê duyệt dành cho quản lý</a></li>
    <li><a href="#calendar">Lịch & ngày nghỉ</a></li>
    <li><a href="#faq">Câu hỏi thường gặp</a></li>
  </ol>

  <h2 id="request">1. Tạo & theo dõi đơn nghỉ</h2>
  <p>Vào <strong>Đơn nghỉ &rarr; Tạo đơn</strong>, điền loại nghỉ, thời gian, lý do. Theo dõi trạng thái tại trang Danh sách.</p>

  <h2 id="approve">2. Phê duyệt cho quản lý</h2>
  <p>Truy cập <strong>Quản lý &rarr; Duyệt đơn</strong>. Bạn có thể <em>Approve/Reject</em> kèm ghi chú, hệ thống ghi lại Audit Log.</p>

  <h2 id="calendar">3. Lịch & ngày nghỉ</h2>
  <p>Xem lịch phòng ban, hiển thị ngày lễ, cuối tuần. Các đơn đã duyệt sẽ hiện trên lịch.</p>

  <h2 id="faq">4. FAQ</h2>
  <p>Xem thêm tại <a href="${pageContext.request.contextPath}/support/faq">Câu hỏi thường gặp</a>.</p>
</div>
</body></html>
