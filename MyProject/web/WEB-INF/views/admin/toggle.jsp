<%@page contentType="text/html; charset=UTF-8"%>
<%@include file="/WEB-INF/views/common/_taglibs.jsp"%>
<%@include file="/WEB-INF/views/common/_header.jsp"%>
<c:set var="cp" value="${pageContext.request.contextPath}" />

<div class="container" style="max-width:800px;margin:24px auto;">
  <h2>Kết quả cập nhật trạng thái</h2>
  <p>Đã cập nhật <strong>${count}</strong> tài khoản.</p>
  <c:if test="${not empty ids}">
    <div class="card" style="padding:12px;margin-top:10px">
      <div class="muted">IDs:</div>
      <div>${fn:join(ids, ', ')}</div>
    </div>
  </c:if>
  <div style="margin-top:16px">
    <a href="${cp}/admin/users" class="btn">Quay về danh sách</a>
  </div>
</div>

<%@include file="/WEB-INF/views/common/_footer.jsp"%>
