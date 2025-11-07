<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp"%>
<c:set var="cp" value="${pageContext.request.contextPath}"/>

<jsp:include page="/WEB-INF/views/portal/_portal_header.jsp"/>
<jsp:include page="/WEB-INF/views/portal/_portal_sidebar.jsp"/>

<div class="wrap">
  <h2>Ứng viên – ${job.title}</h2>
  <table class="table" id="appsTbl">
    <thead>
      <tr><th>ID</th><th>Họ tên</th><th>Email</th><th>Phone</th><th>Stage</th><th>Score</th><th>Hồ sơ</th><th>Hành động</th></tr>
    </thead>
    <tbody>
    <c:forEach var="a" items="${apps}">
      <tr data-id="${a.id}">
        <td>${a.id}</td>
        <td>${a.fullName}</td>
        <td>${a.email}</td>
        <td>${a.phone}</td>
        <td>${a.stage}</td>
        <td>${empty a.score ? '-' : a.score}</td>
        <td><c:if test="${not empty a.resumeUrl}"><a href="${a.resumeUrl}" target="_blank">CV</a></c:if></td>
        <td>
          <form method="post" action="${cp}/recruit/move" class="inline">
            <input type="hidden" name="id" value="${a.id}"/>
            <select name="next">
              <c:forEach var="st" items="${fn:split('NEW,SCREEN,INTERVIEW,OFFER,HIRED,REJECT',',')}">
                <option value="${st}" ${st==a.stage?'selected':''}>${st}</option>
              </c:forEach>
            </select>
            <input type="number" name="score" min="0" max="100" value="${a.score}"/>
            <input type="text" name="note" placeholder="Ghi chú"/>
            <button class="btn">Cập nhật</button>
          </form>
        </td>
      </tr>
    </c:forEach>
    </tbody>
  </table>
  <p><a class="btn" href="${cp}/recruit/jobs">← Quay lại Jobs</a></p>
</div>

<jsp:include page="/WEB-INF/views/portal/_portal_footer.jsp"/>
