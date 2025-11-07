<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp"%>

<c:set var="cp"   value="${pageContext.request.contextPath}"/>
<c:set var="page" value="portal.home"/>

<jsp:include page="/WEB-INF/views/portal/_portal_header.jsp"/>
<jsp:include page="/WEB-INF/views/portal/_portal_sidebar.jsp"/>

<style>
  .wrap{max-width:1200px;margin:16px auto;padding:0 16px}
  .panel{background:var(--card,#fff);border:1px solid var(--bd,#e5e7eb);border-radius:16px;padding:14px}
  .cards{display:grid;gap:12px;grid-template-columns:repeat(2,1fr)}
  @media(min-width:900px){.cards{grid-template-columns:repeat(4,1fr)}}
  .card{background:var(--card);border:1px solid var(--bd);border-radius:16px;padding:14px}
  .card h4{margin:0 0 6px;color:var(--muted,#64748b);font-size:13px}
  .metric{font-size:22px;font-weight:900}
  .tiles{display:grid;grid-template-columns:repeat(6,minmax(140px,1fr));gap:12px}
  @media(max-width:900px){.tiles{grid-template-columns:repeat(2,minmax(160px,1fr))}}
  .tile{display:flex;gap:10px;align-items:center;padding:14px;border:1px solid var(--bd);border-radius:16px;background:var(--card);text-decoration:none;color:inherit}
  .tile .ic{font-size:22px;width:36px;height:36px;display:grid;place-items:center;border-radius:12px;background:var(--card-2,#f3f4f6)}
  .grid{display:grid;gap:16px}
  @media(min-width:900px){.grid{grid-template-columns:1.3fr 1fr}}
  table{width:100%;border-collapse:collapse}
  th,td{padding:10px 12px;border-bottom:1px solid var(--bd);text-align:left}
  th{font-size:12px;color:var(--muted)}
  .btn{padding:8px 12px;border:1px solid var(--bd);border-radius:12px;background:var(--card-2,#f9fafb);text-decoration:none;color:inherit}
  .btn.pri{background:linear-gradient(180deg,#22c55e,#16a34a);color:#fff;border-color:transparent}
  .muted{color:var(--muted,#64748b)}
</style>

<main class="with-psb">
  <div class="wrap">

    <!-- Greeting / quick actions -->
    <div class="panel" style="display:flex;align-items:center;gap:12px">
      <div style="width:44px;height:44px;border-radius:50%;background:#dbeafe;display:grid;place-items:center;font-weight:900;color:#1e3a8a">
        <c:out value="${empty sessionScope.currentUser ? 'U' : fn:substring(sessionScope.currentUser.fullName,0,1)}"/>
      </div>
      <div>
        <div class="muted" style="font-size:13px">Chúc một ngày tốt lành 👋</div>
        <div style="font-size:22px;font-weight:900">
          <c:out value="${empty sessionScope.currentUser ? 'User' : sessionScope.currentUser.fullName}"/>
        </div>
      </div>
      <div style="margin-left:auto;display:flex;gap:8px;flex-wrap:wrap">
        <a class="btn" href="${cp}/request/create">＋ Tạo đơn</a>
        <a class="btn" href="${cp}/attendance">🕒 Chấm công</a>
        <a class="btn" href="${cp}/work">📈 Báo cáo</a>
      </div>
    </div>

    <!-- KPIs -->
    <c:set var="k" value="${requestScope.kpi}"/>
    <div class="cards" aria-label="Chỉ số nhanh">
      <div class="card">
        <h4>Phép năm còn</h4>
        <div class="metric">
          <c:out value="${empty k.AL ? 0 : k.AL}"/> ngày
        </div>
      </div>
      <div class="card">
        <h4>Đơn đang chờ</h4>
        <div class="metric"><c:out value="${empty k.pending ? 0 : k.pending}"/></div>
      </div>
      <div class="card">
        <h4>Đi muộn (tháng)</h4>
        <div class="metric"><c:out value="${empty k.late ? 0 : k.late}"/> lần</div>
      </div>
      <div class="card">
        <h4>Ước tính NET</h4>
        <div class="metric">
          <fmt:formatNumber value="${empty k.net ? 0 : k.net}" type="currency"/>
        </div>
      </div>
    </div>

    <!-- Shortcuts -->
    <div class="panel" style="margin:14px 0">
      <div class="tiles" role="navigation" aria-label="Lối tắt module">
        <a class="tile" href="${cp}/request/list">
          <div class="ic">📝</div><div><b>Requests</b><div class="muted" style="font-size:12px">Tạo/duyệt nhanh</div></div>
        </a>
        <a class="tile" href="${cp}/attendance">
          <div class="ic">🕒</div><div><b>Chấm công</b><div class="muted" style="font-size:12px">Giờ làm rõ ràng</div></div>
        </a>
        <a class="tile" href="${cp}/work">
          <div class="ic">📈</div><div><b>Báo cáo</b><div class="muted" style="font-size:12px">Daily/Weekly</div></div>
        </a>
        <a class="tile" href="${cp}/work/todos">
          <div class="ic">✅</div><div><b>Việc HR</b><div class="muted" style="font-size:12px">To-do</div></div>
        </a>
        <a class="tile" href="${cp}/payroll">
          <div class="ic">💸</div><div><b>Lương</b><div class="muted" style="font-size:12px">Tổng hợp</div></div>
        </a>
        <a class="tile" href="${cp}/recruit/job">
          <div class="ic">🧲</div><div><b>Tuyển dụng</b><div class="muted" style="font-size:12px">Pipeline</div></div>
        </a>
      </div>
    </div>

    <!-- 2 columns -->
    <div class="grid">
      <!-- Recent activity -->
      <div class="panel">
        <h3 style="margin:0 0 10px">Hoạt động gần đây</h3>
        <table>
          <thead>
            <tr><th>Loại</th><th>Nội dung</th><th>Thời gian</th></tr>
          </thead>
          <tbody>
            <c:forEach items="${requestScope.recentActivities}" var="a">
  <tr>
    <td><c:out value="${a.type}"/></td>
    <td><c:out value="${a.title}"/></td>
    <td><fmt:formatDate value="${a.time}" pattern="dd/MM HH:mm"/></td>
  </tr>
</c:forEach>

            <c:if test="${empty requestScope.recentActivities}">
              <tr><td colspan="3" class="muted">— Không có dữ liệu —</td></tr>
            </c:if>
          </tbody>
        </table>
      </div>

      <!-- Quick clock -->
      <div class="panel">
        <h3 style="margin:0 0 10px">Chấm công nhanh</h3>

        <c:set var="clock" value="${requestScope.clock}"/>
        <%-- clock.inAllowed / outAllowed là boolean do Servlet set sẵn; todaySummary là chuỗi --%>
        <form method="post" action="${cp}/attendance/clock" style="display:flex;gap:8px;flex-wrap:wrap" autocomplete="off">
          <input type="hidden" name="csrf" value="${requestScope.csrf}"/>
          <button class="btn pri"  name="action" value="in"  <c:if test="${clock != null && !clock.inAllowed}">disabled</c:if>>Check-in</button>
          <button class="btn"      name="action" value="out" <c:if test="${clock != null && !clock.outAllowed}">disabled</c:if>>Check-out</button>
          <span class="muted" style="margin-left:auto">
            Hôm nay: <b><c:out value="${empty requestScope.todaySummary ? '--:--' : requestScope.todaySummary}"/></b>
          </span>
        </form>

        <c:if test="${not empty requestScope.clockHint}">
          <div class="muted" style="margin-top:8px;font-size:12px">
            <c:out value="${requestScope.clockHint}"/>
          </div>
        </c:if>
      </div>
    </div>

    <jsp:include page="/WEB-INF/views/portal/_portal_footer.jsp"/>
  </div>
</main>
