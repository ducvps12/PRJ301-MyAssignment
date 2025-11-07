<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp"%>
<jsp:useBean id="now" class="java.util.Date" />
<%
  try { com.acme.leavemgmt.util.Csrf.addToken(request); } catch (Throwable ignore) {}
%>

<c:set var="cp" value="${pageContext.request.contextPath}" />
<c:set var="currentUser" value="${sessionScope.currentUser}" />
<c:set var="page" value="home" />

<%@ include file="/WEB-INF/views/common/_user_header.jsp" %>

<style>
  :root{ --bg:#f7f9fc; --card:#fff; --tx:#0f172a; --muted:#64748b; --bd:#e5e7eb;
         --pri:#2563eb; --ok:#16a34a; --warn:#f59e0b; --err:#dc2626; --vio:#7c3aed; }
  @media (prefers-color-scheme: dark){
    :root{ --bg:#0b1220; --card:#0f172a; --tx:#e5e7eb; --muted:#94a3b8; --bd:#1e293b; }
  }
  body{background:var(--bg)}
  .wrap{max-width:1200px;margin:18px auto;padding:0 16px}
  .greet{display:flex;align-items:center;gap:12px}
  .greet .avatar{width:44px;height:44px;border-radius:50%;background:#dbeafe;display:grid;place-items:center;font-weight:800;color:#1e3a8a}
  .divider{height:1px;background:var(--bd);margin:14px 0}
  .panel{background:var(--card);border:1px solid var(--bd);border-radius:16px;padding:14px}
  .cards{display:grid;gap:12px;grid-template-columns:repeat(2,1fr)}
  @media(min-width:900px){ .cards{grid-template-columns:repeat(4,1fr)} }
  .card{background:var(--card);border:1px solid var(--bd);border-radius:16px;padding:14px}
  .card h4{margin:0 0 6px;color:var(--muted);font-size:13px}
  .metric{font-size:22px;font-weight:900}
  .pill{display:inline-flex;align-items:center;gap:6px;padding:6px 10px;border-radius:999px;font-size:12px;border:1px solid var(--bd);color:var(--muted)}
  .pill.ok{background:#ecfdf5;border-color:#bbf7d0;color:#065f46}
  .pill.warn{background:#fff7ed;border-color:#fed7aa;color:#7c2d12}
  .pill.err{background:#fef2f2;border-color:#fecaca;color:#7f1d1d}
  .btn{appearance:none;border:none;padding:10px 14px;border-radius:12px;background:var(--pri);color:#fff;font-weight:800;cursor:pointer;text-decoration:none;display:inline-flex;align-items:center;gap:8px}
  .btn.ghost{background:transparent;color:var(--pri);border:1px solid var(--pri)}
  .btn.muted{background:#e5e7eb;color:#374151}
  @media (prefers-color-scheme: dark){ .btn.muted{background:#1f2937;color:#e5e7eb}}
  .grid{display:grid;gap:16px}
  @media(min-width:900px){ .grid{grid-template-columns:1.3fr 1fr} }
  table{width:100%;border-collapse:collapse}
  th,td{padding:10px 12px;border-bottom:1px solid var(--bd);text-align:left}
  th{font-size:12px;color:var(--muted);text-transform:uppercase}
  .status{padding:4px 8px;border-radius:999px;font-size:12px;border:1px solid var(--bd)}
  .s-pending{background:#fff7ed;color:#9a3412;border-color:#fed7aa}
  .s-approved{background:#ecfdf5;color:#065f46;border-color:#bbf7d0}
  .s-rejected{background:#fef2f2;color:#7f1d1d;border-color:#fecaca}
  .empty{color:var(--muted);font-style:italic}
  .limited{border:1px dashed #f59e0b;background:#fffbeb}
  @media (prefers-color-scheme: dark){ .limited{background:#1f2937;border-color:#f59e0b} }
</style>

<%-- empStatus: ưu tiên accState do servlet set; nếu không có thì
     fallback theo cờ status (so sánh chuỗi để tránh ép kiểu số). --%>
<c:set var="empStatus"
       value="${not empty requestScope.accState
               ? requestScope.accState
               : (not empty currentUser and currentUser.status ne '1' ? 'SUSPENDED' : '')}" />

<%-- roleCode: lấy role nếu có, tránh NPE --%>
<c:set var="roleCode" value="${currentUser != null ? (currentUser.role != null ? currentUser.role : '') : ''}" />

<%-- LIMITED nếu: chưa set role, hoặc empStatus thuộc nhóm hạn chế, hoặc role là PROBATION/INTERN --%>
<c:set var="isLimited"
       value="${empty roleCode
                or empStatus=='OFFBOARDING'
                or empStatus=='SUSPENDED'
                or empStatus=='UNDER_REVIEW'
                or empStatus=='TERMINATED'
                or roleCode=='PROBATION'
                or roleCode=='INTERN'}" />

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
      <div style="font-size:13px;color:var(--muted)">Chúc một ngày tốt lành 👋</div>
      <div style="font-size:22px;font-weight:900">
        <c:out value="${currentUser != null ? currentUser.fullName : 'User'}"/>
        <span style="color:var(--muted);font-weight:600">
          • <c:out value="${not empty roleCode ? roleCode : 'N/A'}"/>
        </span>
      </div>
    </div>
    <div style="margin-left:auto"><span class="pill">IP: <c:out value="${pageContext.request.remoteAddr}"/></span></div>
  </div>

  <div class="divider"></div>

  <!-- LIMITED MODE banner -->
  <c:if test="${isLimited}">
    <div class="panel limited" role="alert" aria-live="polite">
      <h3 style="margin:0 0 6px">Tài khoản đang ở chế độ giới hạn</h3>
      <p style="margin:0 0 8px;color:#7c2d12">
        Bạn hiện chưa được cấp đầy đủ quyền sử dụng hệ thống (hoặc đang thuộc trạng thái nhân sự:
        <b><c:out value="${empty empStatus ? 'CHƯA CẤU HÌNH' : empStatus}"/></b>).
      </p>
      <ul style="margin:0 0 10px 18px;color:var(--muted)">
        <li>Không truy cập danh sách đơn, không phê duyệt.</li>
        <li>Vẫn có thể gửi yêu cầu nghỉ phép để HR xem xét (nếu đơn vị cho phép).</li>
      </ul>
      <div style="display:flex;gap:8px;flex-wrap:wrap">
        <a class="btn" href="${cp}/request/create">＋ Tạo đơn nghỉ phép</a>
        <a class="btn ghost" href="${cp}/help/roles">Quyền & Trạng thái</a>
        <a class="btn muted"
           href="mailto:hradmin@company.local?subject=Yeu%20cau%20cap%20quyen%20tai%20khoan&body=Username:%20${currentUser != null ? currentUser.username : ''}">
          Liên hệ HR
        </a>
        <a class="btn muted"
           href="mailto:manager@company.local?subject=De%20nghi%20kich%20hoat%20quyen&body=Username:%20${currentUser != null ? currentUser.username : ''}">
          Liên hệ Quản lý
        </a>
      </div>
    </div>

    <div class="divider"></div>
  </c:if>

  <!-- KPI + nội dung chỉ hiển thị nếu KHÔNG limited -->
  <c:if test="${not isLimited}">
    <div class="cards" aria-label="Các chỉ số nhanh">
      <div class="card">
        <h4>Phép năm còn</h4>
        <div class="metric"><c:out value="${empty myBalances.AL ? 0 : myBalances.AL}"/> ngày</div>
        <div class="pill">Cập nhật:
          <fmt:formatDate value="${requestScope.now != null ? requestScope.now : now}" pattern="dd/MM/yyyy HH:mm"/>
        </div>
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

    <div class="panel">
      <h3>Lối tắt</h3>
      <div style="display:flex;gap:10px;flex-wrap:wrap">
        <a class="btn" href="${cp}/request/create">＋ Tạo đơn nghỉ phép</a>
        <!-- Trỏ về trang "Đơn của tôi" -->
        <a class="btn ghost" href="${cp}/request/my">📄 Đơn của tôi</a>
        <a class="btn ghost" href="${cp}/agenda">📅 Agenda phòng</a>
        <a class="btn ghost" href="${cp}/notifications">🔔 Thông báo</a>
        <c:if test="${roleCode == 'TEAM_LEAD' || roleCode == 'DIV_LEADER' || roleCode == 'HR_ADMIN' || roleCode == 'MANAGER'}">
          <a class="btn" style="background:var(--vio)" href="${cp}/approve/inbox">✅ Hộp thư duyệt</a>
        </c:if>
      </div>
    </div>

    <div class="divider"></div>

    <div class="grid">
      <!-- Đơn gần đây -->
      <div class="panel">
        <h3>Đơn gần đây</h3>
        <c:choose>
          <c:when test="${empty recentRequests}">
            <div class="empty">Chưa có dữ liệu.</div>
          </c:when>
          <c:otherwise>
            <table>
              <thead><tr><th>#</th><th>Loại</th><th>Từ</th><th>Đến</th><th>Trạng thái</th><th></th></tr></thead>
              <tbody>
              <c:forEach items="${recentRequests}" var="r">
                <tr>
                  <td>#<c:out value="${r.id}"/></td>
                  <td><c:out value="${r.type}"/></td>
                  <td><fmt:formatDate value="${r.startDate}" pattern="dd/MM/yyyy"/></td>
                  <td><fmt:formatDate value="${r.endDate}" pattern="dd/MM/yyyy"/></td>
                  <td>
                    <c:set var="st" value="${fn:toLowerCase(r.status)}"/>
                    <span class="status ${st=='approved'?'s-approved':(st=='rejected'?'s-rejected':'s-pending')}">
                      <c:out value="${r.status}"/>
                    </span>
                  </td>
                  <td><a class="pill" href="${cp}/request/view?id=${r.id}">Xem</a></td>
                </tr>
              </c:forEach>
              </tbody>
            </table>
          </c:otherwise>
        </c:choose>
      </div>

      <!-- Thông báo -->
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
                    <div class="pill"><fmt:formatDate value="${n.createdAt}" pattern="dd/MM HH:mm"/></div>
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
  </c:if>

  <!-- Nếu LIMITED: chỉ để lại lối tắt tối thiểu -->
  <c:if test="${isLimited}">
    <div class="panel">
      <h3>Lối tắt</h3>
      <div style="display:flex;gap:10px;flex-wrap:wrap">
        <a class="btn" href="${cp}/request/create">＋ Tạo đơn nghỉ phép</a>
        <a class="btn ghost" href="${cp}/notifications">🔔 Thông báo</a>
        <a class="btn ghost" href="${cp}/help">❓ Trợ giúp</a>
      </div>
    </div>
  </c:if>
</div>

<script>
  // Phím tắt nhanh
  document.addEventListener('keydown', e => {
    if (e.target.closest('input,textarea')) return;
    if (e.key.toLowerCase() === 'n') location.href='${cp}/request/create';
    if (e.key.toLowerCase() === 'l') location.href='${cp}/request/my';
  });
</script>
