<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp" %>

<c:set var="cp" value="${pageContext.request.contextPath}" />

<style>
  :root{--bd:#e5e7eb;--ink:#0f172a;--muted:#64748b;--pri:#2563eb;--bg:#f7f7f8}
  body{background:var(--bg)}
  .wrap{max-width:1000px;margin:16px auto;padding:0 16px}
  .panel{background:#fff;border:1px solid var(--bd);border-radius:14px;padding:14px}
  .toolbar{display:flex;gap:8px;flex-wrap:wrap;align-items:center;margin-bottom:10px}
  .input{border:1px solid var(--bd);border-radius:10px;padding:8px 10px;background:#fff}
  .btn{border:1px solid var(--pri);background:var(--pri);color:#fff;padding:8px 12px;border-radius:10px;cursor:pointer}
  .btn.ghost{background:#fff;color:var(--pri)}
  .muted{color:var(--muted)}
  .list{margin:8px 0;padding-left:20px}
  .list li{margin:8px 0}
  .item-date{font-weight:600}
  .foot{display:flex;justify-content:space-between;align-items:center;margin-top:10px}
  .pager a{padding:6px 10px;border:1px solid var(--bd);border-radius:8px;margin-left:4px;text-decoration:none;color:var(--ink);background:#fff}
  .pager .on{background:var(--pri);color:#fff;border-color:var(--pri)}
</style>

<main class="wrap">
  <div class="panel">
    <h2 style="margin:6px 0 10px">Reports</h2>

    <!-- Toolbar lọc -->
    <form class="toolbar" method="get" action="${cp}/work">
      <select class="input" name="type">
        <c:set var="t" value="${empty type ? '' : type}" />
        <option value=""           ${empty t ? 'selected' : ''}>All types</option>
        <option value="DAILY"      ${t=='DAILY' ? 'selected' : ''}>DAILY</option>
        <option value="WEEKLY"     ${t=='WEEKLY' ? 'selected' : ''}>WEEKLY</option>
        <option value="MONTHLY"    ${t=='MONTHLY' ? 'selected' : ''}>MONTHLY</option>
      </select>
      <input class="input" type="date" name="from" value="${from}" />
      <input class="input" type="date" name="to"   value="${to}" />
      <button class="btn" type="submit">Lọc</button>
      <a class="btn ghost" href="${cp}/work/todos">Xem TODOs</a>
    </form>

    <p class="muted" style="margin:4px 0 10px">
      Type: <strong>${empty type ? 'ALL' : type}</strong>,
      From: <strong>${from}</strong>,
      To: <strong>${to}</strong>
    </p>

    <!-- Danh sách báo cáo -->
    <c:choose>
      <c:when test="${empty reports}">
        <p class="muted">Chưa có báo cáo trong khoảng thời gian này.</p>
      </c:when>
      <c:otherwise>
        <ul class="list">
          <c:forEach var="r" items="${reports}">
            <li>
              <span class="item-date">
                <fmt:formatDate value="${r.workDate}" pattern="yyyy-MM-dd"/>
              </span>
              — <span class="muted">${empty r.type ? 'DAILY' : r.type}</span>
              — <c:out value="${empty r.content ? '' : fn:substring(r.content,0,140)}"/>...
            </li>
          </c:forEach>
        </ul>
      </c:otherwise>
    </c:choose>

    <!-- Footer -->
    <div class="foot">
      <a href="${cp}/work">« Quay lại Work</a>

      <!-- Phân trang cơ bản (nếu có biến page/size) -->
      <c:if test="${not empty page}">
        <div class="pager">
          <c:set var="p" value="${page}" />
          <c:set var="prev" value="${p-1 < 1 ? 1 : p-1}" />
          <a href="${cp}/work?type=${type}&from=${from}&to=${to}&page=${prev}">« Trước</a>
          <a class="on" href="${cp}/work?type=${type}&from=${from}&to=${to}&page=${p}">${p}</a>
          <a href="${cp}/work?type=${type}&from=${from}&to=${to}&page=${p+1}">Sau »</a>
        </div>
      </c:if>
    </div>
  </div>
</main>
