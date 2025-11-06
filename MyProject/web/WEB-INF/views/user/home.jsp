<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%
    // CSRF (nếu bạn đang dùng util của dự án)
    try { com.acme.leavemgmt.util.Csrf.addToken(request); } catch (Throwable ignore) {}
%>
<c:set var="cp" value="${pageContext.request.contextPath}" />

<%-- Header + Sidebar dùng chung của dự án --%>
<%@ include file="/WEB-INF/views/common/_admin_header.jsp" %>
<%@ include file="/WEB-INF/views/common/_admin_sidebar.jsp" %>

<style>
  :root{
    --bg:#f7f9fc; --card:#fff; --tx:#0f172a; --muted:#64748b; --bd:#e5e7eb;
    --pri:#2563eb; --ok:#16a34a; --warn:#f59e0b; --err:#dc2626; --vio:#7c3aed;
  }
  @media (prefers-color-scheme: dark){
    :root{ --bg:#0b1220; --card:#0f172a; --tx:#e5e7eb; --muted:#94a3b8; --bd:#1e293b; }
  }
  body{background:var(--bg);}
  .wrap{max-width:1200px;margin:20px auto;padding:0 16px}
  .greet{display:flex;align-items:center;gap:12px}
  .greet .avatar{width:44px;height:44px;border-radius:50%;background:#dbeafe;display:grid;place-items:center;font-weight:700;color:#1e3a8a}
  .grid{display:grid;gap:16px}
  @media(min-width:900px){ .grid{grid-template-columns:1.3fr 1fr} }

  .cards{display:grid;gap:12px;grid-template-columns:repeat(2,1fr)}
  @media(min-width:900px){ .cards{grid-template-columns:repeat(4,1fr)} }
  .card{background:var(--card);border:1px solid var(--bd);border-radius:16px;padding:14px}
  .card h4{margin:0 0 6px 0;font-size:14px;color:var(--muted);font-weight:600}
  .metric{font-size:22px;font-weight:800;color:var(--tx)}
  .pill{display:inline-flex;align-items:center;gap:6px;padding:6px 10px;border-radius:999px;font-size:12px;border:1px solid var(--bd);color:var(--muted)}
  .pill.ok{background:#ecfdf5;border-color:#bbf7d0;color:#065f46}
  .pill.warn{background:#fff7ed;border-color:#fed7aa;color:#7c2d12}
  .pill.err{background:#fef2f2;border-color:#fecaca;color:#7f1d1d}

  .quick{display:flex;gap:12px;flex-wrap:wrap}
  .btn{appearance:none;border:none;padding:10px 14px;border-radius:12px;background:var(--pri);color:#fff;font-weight:700;cursor:pointer}
  .btn.ghost{background:transparent;color:var(--pri);border:1px solid var(--pri)}
  .btn.light{background:#eef2ff;color:#3730a3}
  .btn.full{width:100%}

  table{width:100%;border-collapse:collapse}
  th,td{padding:10px 12px;border-bottom:1px solid var(--bd);text-align:left}
  th{font-size:12px;color:var(--muted);font-weight:700;text-transform:uppercase}
  td .status{padding:4px 8px;border-radius:999px;font-size:12px;border:1px solid var(--bd)}
  .s-pending{background:#fff7ed;color:#9a3412;border-color:#fed7aa}
  .s-approved{background:#ecfdf5;color:#065f46;border-color:#bbf7d0}
  .s-rejected{background:#fef2f2;color:#7f1d1d;border-color:#fecaca}

  .panel{background:var(--card);border:1px solid var(--bd);border-radius:16px;padding:14px}
  .panel h3{margin:0 0 12px 0}
  .inline-help{font-size:12px;color:var(--muted)}
  .spark{height:44px;width:100%;display:block}
  .divider{height:1px;background:var(--bd);margin:12px 0}
  .empty{color:var(--muted);font-style:italic}
</style>

<div class="wrap">
  <!-- Greeting -->
  <div class="greet">
    <div class="avatar">
      <c:choose>
        <c:when test="${not empty currentUser && not empty currentUser.fullName}">
          ${fn:substring(currentUser.fullName,0,1)}
        </c:when>
        <c:otherwise>U</c:otherwise>
      </c:choose>
    </div>
    <div>
      <div style="font-size:14px;color:var(--muted)">
        Chúc một ngày tốt lành 👋
      </div>
      <div style="font-size:22px;font-weight:900">
        <c:out value="${currentUser.fullName}"/> • <span style="color:var(--muted);font-weight:600"><c:out value="${currentUser.role}"/></span>
      </div>
    </div>
    <div style="margin-left:auto"><span class="pill">IP: <c:out value="${request.remoteAddr}"/></span></div>
  </div>

  <div class="divider"></div>

  <!-- Cards -->
  <div class="cards">
    <div class="card">
      <h4>Phép năm còn</h4>
      <div class="metric">
        <c:out value="${empty myBalances.AL ? 0 : myBalances.AL}"/> ngày
      </div>
      <canvas id="spk-balance" class="spark"></canvas>
      <div class="inline-help">Cập nhật: <fmt:formatDate value="${now}" pattern="dd/MM/yyyy HH:mm"/></div>
    </div>
    <div class="card">
      <h4>Đơn đang chờ duyệt</h4>
      <div class="metric"><c:out value="${empty myPendingCount ? 0 : myPendingCount}"/></div>
      <div class="pill warn">Cần theo dõi</div>
    </div>
    <div class="card">
      <h4>Đơn đã duyệt</h4>
      <div class="metric"><c:out value="${empty myApprovedCount ? 0 : myApprovedCount}"/></div>
      <div class="pill ok">Ổn định</div>
    </div>
    <div class="card">
      <h4>Đơn bị từ chối</h4>
      <div class="metric"><c:out value="${empty myRejectedCount ? 0 : myRejectedCount}"/></div>
      <div class="pill err">Xem lý do</div>
    </div>
  </div>

  <div class="divider"></div>

  <!-- Quick actions -->
  <div class="panel">
    <h3>Lối tắt</h3>
    <div class="quick">
      <form action="${cp}/request/create" method="get">
        <button class="btn">＋ Tạo đơn nghỉ phép</button>
      </form>
      <a class="btn light" href="${cp}/request/list">📄 Đơn của tôi</a>
      <a class="btn ghost" href="${cp}/agenda">📅 Agenda phòng</a>
      <a class="btn ghost" href="${cp}/notifications">🔔 Thông báo</a>

      <c:if test="${currentUser.role == 'TEAM_LEAD' || currentUser.role == 'DIV_LEADER' || currentUser.role == 'HR_ADMIN' || currentUser.role == 'MANAGER'}">
        <a class="btn" style="background:var(--vio)" href="${cp}/approve/inbox">✅ Hộp thư duyệt</a>
      </c:if>
    </div>
  </div>

  <div class="divider"></div>

  <!-- 2 cột: Đơn gần đây + Việc cần duyệt -->
  <div class="grid">
    <!-- Recent requests -->
    <div class="panel">
      <h3>Đơn gần đây</h3>
      <c:choose>
        <c:when test="${empty recentRequests}">
          <div class="empty">Chưa có dữ liệu.</div>
        </c:when>
        <c:otherwise>
          <table>
            <thead>
              <tr>
                <th>#</th><th>Loại</th><th>Từ</th><th>Đến</th><th>Trạng thái</th><th></th>
              </tr>
            </thead>
            <tbody>
            <c:forEach items="${recentRequests}" var="r">
              <tr>
                <td><c:out value="${r.id}"/></td>
                <td><c:out value="${r.type}"/></td>
                <td><fmt:formatDate value="${r.startDate}" pattern="dd/MM"/></td>
                <td><fmt:formatDate value="${r.endDate}" pattern="dd/MM"/></td>
                <td>
                  <c:choose>
                    <c:when test="${r.status == 'APPROVED'}"><span class="status s-approved">Đã duyệt</span></c:when>
                    <c:when test="${r.status == 'REJECTED'}"><span class="status s-rejected">Từ chối</span></c:when>
                    <c:otherwise><span class="status s-pending">Chờ xử lý</span></c:otherwise>
                  </c:choose>
                </td>
                <td><a class="pill" href="${cp}/request/detail?id=${r.id}">Xem</a></td>
              </tr>
            </c:forEach>
            </tbody>
          </table>
        </c:otherwise>
      </c:choose>
    </div>

    <!-- Approvals inbox (if manager) -->
    <div class="panel">
      <h3>Việc cần duyệt</h3>
      <c:choose>
        <c:when test="${empty approveInbox}">
          <div class="empty">Không có mục cần duyệt.</div>
        </c:when>
        <c:otherwise>
          <table>
            <thead>
              <tr><th>#</th><th>Nhân viên</th><th>Khoảng nghỉ</th><th></th></tr>
            </thead>
            <tbody>
              <c:forEach items="${approveInbox}" var="a">
                <tr>
                  <td><c:out value="${a.id}"/></td>
                  <td><c:out value="${a.userName}"/></td>
                  <td><fmt:formatDate value="${a.startDate}" pattern="dd/MM"/> – <fmt:formatDate value="${a.endDate}" pattern="dd/MM"/></td>
                  <td><a class="pill ok" href="${cp}/approve/review?id=${a.id}">Duyệt</a></td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </c:otherwise>
      </c:choose>
    </div>
  </div>

  <div class="divider"></div>

  <!-- Notifications -->
  <div class="panel">
    <h3>Thông báo</h3>
    <c:choose>
      <c:when test="${empty notifications}">
        <div class="empty">Chưa có thông báo.</div>
      </c:when>
      <c:otherwise>
        <ul style="margin:0;padding:0;list-style:none">
          <c:forEach items="${notifications}" var="n">
            <li class="card" style="display:flex;justify-content:space-between;align-items:center;margin:8px 0">
              <div>
                <div style="font-weight:700"><c:out value="${n.title}"/></div>
                <div style="color:var(--muted);font-size:13px"><c:out value="${n.body}"/></div>
              </div>
              <div style="text-align:right">
                <div class="inline-help"><fmt:formatDate value="${n.createdAt}" pattern="dd/MM HH:mm"/></div>
                <c:if test="${not empty n.linkUrl}">
                  <a class="pill" href="${n.linkUrl}">Mở</a>
                </c:if>
              </div>
            </li>
          </c:forEach>
        </ul>
      </c:otherwise>
    </c:choose>
  </div>
</div>

<script>
  // Mini sparkline (không dùng thư viện)
  (function(){
    const el = document.getElementById('spk-balance');
    if(!el) return;
    const ctx = el.getContext('2d');
    const w = el.width = el.clientWidth;
    const h = el.height = el.clientHeight;

    // dữ liệu giả/hoặc server có thể đẩy: arr số ngày còn lại theo tháng
    const data = (window.serverBalanceSeries || [8,7.5,7,7,6.5,6,5,4.5,4,3.5,3,2.5]);
    const max = Math.max(...data), min = Math.min(...data);

    ctx.clearRect(0,0,w,h);
    ctx.lineWidth = 2;
    ctx.strokeStyle = getComputedStyle(document.documentElement).getPropertyValue('--pri').trim();
    ctx.beginPath();
    data.forEach((v,i)=>{
      const x = (i/(data.length-1))*w;
      const y = h - ((v-min)/(max-min||1))*h;
      i?ctx.lineTo(x,y):ctx.moveTo(x,y);
    });
    ctx.stroke();

    // fill nhẹ
    const grd = ctx.createLinearGradient(0,0,0,h);
    grd.addColorStop(0,'rgba(37,99,235,.15)');
    grd.addColorStop(1,'rgba(37,99,235,0)');
    ctx.lineTo(w,h); ctx.lineTo(0,h); ctx.closePath();
    ctx.fillStyle = grd; ctx.fill();
  })();

  // Phím tắt nộp đơn: nhấn "n" để mở form tạo đơn
  document.addEventListener('keydown',e=>{
    if(e.key.toLowerCase()==='n'){ location.href = '${cp}/request/create'; }
  });
</script>
