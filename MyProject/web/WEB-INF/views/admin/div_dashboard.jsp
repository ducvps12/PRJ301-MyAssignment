<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/common/_header.jsp" %>
<div class="wrap">
  <h2>Division Dashboard – ${sessionScope.currentUser.department}</h2>

  <p>Xin chào, ${sessionScope.currentUser.fullName}</p>
  <p>Tổng số yêu cầu nghỉ phép trong division: ${stats.totalRequests}</p>
  <p>Đã duyệt tháng này: ${stats.approvedThisMonth}</p>
  <p>Đang chờ xử lý: ${stats.pendingCount}</p>
</div>
<%@ include file="/WEB-INF/views/common/_footer.jsp" %>
