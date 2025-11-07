<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp"%>
<c:set var="cp" value="${pageContext.request.contextPath}"/>

<jsp:include page="/WEB-INF/views/portal/_portal_header.jsp"/>
<jsp:include page="/WEB-INF/views/portal/_portal_sidebar.jsp"/>

<div class="wrap">
  <h2>Vị trí đang tuyển</h2>
  <table class="table">
    <thead><tr><th>ID</th><th>Vị trí</th><th>Địa điểm</th><th>Trạng thái</th><th></th></tr></thead>
    <tbody>
    <c:forEach var="j" items="${jobs}">
      <tr>
        <td>${j.id}</td>
        <td>${j.title}</td>
        <td>${j.location}</td>
        <td><span class="badge">${j.status}</span></td>
        <td>
          <a class="btn" href="${cp}/recruit/apps?jobId=${j.id}">Ứng viên</a>
          <a class="btn" href="${cp}/recruit/apply?jobId=${j.id}">Ứng tuyển</a>
        </td>
      </tr>
    </c:forEach>
    </tbody>
  </table>
</div>

<jsp:include page="/WEB-INF/views/portal/_portal_footer.jsp"/>
