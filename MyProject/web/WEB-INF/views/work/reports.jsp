<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp" %>
    
<style>
  .wrap{max-width:1000px;margin:16px auto;padding:0 16px}
  .panel{background:#fff;border:1px solid #e5e7eb;border-radius:14px;padding:14px}
  .muted{color:#64748b}
  .list{margin:8px 0;padding-left:20px}
  .list li{margin:6px 0}
</style>

<main class="wrap">
  <div class="panel">
    <h2>Reports</h2>
    <p class="muted">
      Type: <strong>${empty type ? 'ALL' : type}</strong>,
      From: <strong>${from}</strong>,
      To: <strong>${to}</strong>
    </p>

    <c:choose>
      <c:when test="${empty reports}">
        <p class="muted">Chưa có báo cáo trong khoảng thời gian này.</p>
      </c:when>
      <c:otherwise>
        <ul class="list">
          <c:forEach var="r" items="${reports}">
            <li>
              <strong>
                <fmt:formatDate value="${r.workDate}" pattern="yyyy-MM-dd"/>
              </strong>
              — ${empty r.type ? 'DAILY' : r.type}
              — <c:out value="${empty r.content ? '' : fn:substring(r.content,0,80)}"/>...
            </li>
          </c:forEach>
        </ul>
      </c:otherwise>
    </c:choose>

    <p><a href="${pageContext.request.contextPath}/work">« Quay lại Work</a></p>
  </div>
</main>
