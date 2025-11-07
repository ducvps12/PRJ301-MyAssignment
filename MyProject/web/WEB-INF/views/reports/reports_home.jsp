<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!-- DÙNG BỘ AUDIT (đúng path) -->
  <%-- Header & Sidebar RIÊNG CHO AUDIT --%>
  <jsp:include page="/WEB-INF/views/audit/_audit_sidebar.jsp" />
  <jsp:include page="/WEB-INF/views/audit/_audit_header.jsp" />

<style>
  :root{ --h:64px; --sbw:220px; }
  body.admin .admin-main{
    margin:0; padding:18px;
    padding-top:calc(var(--h) + 12px);
    margin-left:var(--sbw);
    max-width:1200px;
  }
  @media(max-width:1100px){ body.admin .admin-main{ margin-left:0 } }

  .cards{display:grid;grid-template-columns:repeat(auto-fit,minmax(260px,1fr));gap:14px}
  .card{background:var(--card,#fff);border:1px solid var(--bd,#e5e7eb);border-radius:14px;padding:16px}
  .card h3{margin:0 0 6px}
  .muted{color:var(--muted,#64748b)}
  .btn{display:inline-block;border:1px solid var(--bd,#e5e7eb);border-radius:10px;padding:8px 12px;background:#fff}
</style>

<body class="admin">
<main class="admin-main">
  <h2 style="margin:0 0 10px">Bộ báo cáo</h2>
  <p class="muted" style="margin:0 0 16px">Chọn loại báo cáo bạn muốn xem.</p>

  <div class="cards">
    <div class="card">
      <h3>Đơn nghỉ – Theo ngày</h3>
      <p class="muted">Số đơn phát sinh theo ngày, kèm phân bổ phòng ban & trạng thái.</p>
      <a class="btn" href="${ctx}/admin/reports/requests/daily">Mở báo cáo →</a>
    </div>

    <div class="card">
      <h3>Đơn nghỉ – Theo tháng</h3>
      <p class="muted">Tổng hợp theo từng tháng (có thể mở rộng từ daily).</p>
      <a class="btn" href="${ctx}/admin/reports/requests/monthly">Mở báo cáo →</a>
    </div>
  </div>
</main>

<jsp:include page="/WEB-INF/views/audit/_audit_footer.jsp"/>

<script>
  (function(){
    const h  = document.querySelector('.admin-header, .ad-header')?.offsetHeight || 64;
    const sb = document.querySelector('.admin-sidebar, .ad-sidebar, #sidebar')?.offsetWidth || 220;
    document.documentElement.style.setProperty('--h',  h + 'px');
    document.documentElement.style.setProperty('--sbw', sb + 'px');
  })();
</script>
</body>
