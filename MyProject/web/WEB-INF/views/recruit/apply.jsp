<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp"%>
<c:set var="cp" value="${pageContext.request.contextPath}"/>

<div class="wrap">
  <h2>Ứng tuyển: ${job.title}</h2>
  <form method="post" action="${cp}/recruit/apply" class="panel" style="max-width:640px">
    <input type="hidden" name="jobId" value="${job.id}"/>
    <div><label>Họ tên</label><input name="fullName" required class="input"/></div>
    <div><label>Email</label><input name="email" type="email" required class="input"/></div>
    <div><label>Phone</label><input name="phone" class="input"/></div>
    <div><label>Link CV (Drive, v.v.)</label><input name="resumeUrl" class="input"/></div>
    <div><label>Ghi chú</label><textarea name="note" rows="4" class="input"></textarea></div>
    <button class="btn">Gửi hồ sơ</button>
    <a class="btn" href="${cp}/recruit/jobs">Hủy</a>
  </form>
</div>
