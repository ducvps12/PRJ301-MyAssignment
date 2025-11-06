<%-- _admin_footer.jsp (FINAL)
  - KHÔNG khai báo taglib ở partial này.
  - Trang ngoài đã include /WEB-INF/views/common/_taglibs.jsp (jakarta.tags.*)
--%>

<c:set var="ctx"  value="${empty ctx ? pageContext.request.contextPath : ctx}" />
<c:set var="user" value="${sessionScope.user}" />
<c:set var="dbOK" value="${requestScope.dbOK}" />
<c:set var="dbMs" value="${requestScope.dbMs}" />

<footer class="admin-footer" role="contentinfo" aria-label="Phần chân trang quản trị">
  <div class="wrap">
    <div class="left">
      <strong>Admin</strong>
      <span class="sep">•</span>

      <c:choose>
        <c:when test="${not empty user}">
          <span>
            Đăng nhập:
            <b><c:out value="${user.fullName}"/></b>
            <small class="muted">(<c:out value="${user.role}"/>)</small>
          </span>
        </c:when>
        <c:otherwise>
          <span class="muted">Chưa đăng nhập</span>
        </c:otherwise>
      </c:choose>

      <span class="sep">•</span>
      <span>Mode: <b><c:out value="${initParam['app.mode']}"/></b></span>

      <c:if test="${dbOK ne null}">
        <span class="sep">•</span>
        <span>
          DB:
          <span class="badge ${dbOK ? 'ok' : 'no'}"><c:out value="${dbOK ? 'OK' : 'DOWN'}"/></span>
          <small class="muted">(<c:out value="${dbMs}"/> ms)</small>
        </span>
      </c:if>
    </div>

    <div class="right">
      <span class="muted">© <span id="y"></span> LeaveMgmt</span>
      <span class="sep">•</span>
      <a href="#top" class="btn-top" aria-label="Lên đầu trang">↑ Top</a>
    </div>
  </div>
</footer>

<style>
  :root{
    --bd:#e5e7eb; --tx:#111827; --muted:#6b7280; --card:#fff;
  }
  @media (prefers-color-scheme: dark){
    :root{ --bd:#1f2937; --tx:#e5e7eb; --muted:#94a3b8; --card:#0f172a; }
  }

  .admin-footer{border-top:1px solid var(--bd);background:var(--card);margin-top:24px;color:var(--tx)}
  .admin-footer .wrap{max-width:1200px;margin:0 auto;padding:12px 16px;display:flex;gap:12px;align-items:center;justify-content:space-between;flex-wrap:wrap}
  .admin-footer .left,.admin-footer .right{display:flex;gap:10px;align-items:center;flex-wrap:wrap}
  .muted{color:var(--muted)}
  .sep{color:var(--bd)}
  .badge{padding:2px 8px;border-radius:999px;font-size:12px;border:1px solid var(--bd)}
  .badge.ok{background:#ecfdf5;color:#065f46;border-color:#a7f3d0}
  .badge.no{background:#fef2f2;color:#991b1b;border-color:#fecaca}
  .btn-top{border:1px solid var(--bd);border-radius:10px;padding:6px 10px;text-decoration:none;color:inherit;background:transparent}
  .btn-top:focus{outline:2px solid #2563eb;outline-offset:2px}
</style>

<script>
  (function(){
    // Năm hiện tại
    var y = document.getElementById('y');
    if (y) y.textContent = new Date().getFullYear();

    // Scroll to top mượt
    document.querySelectorAll('.btn-top').forEach(function(a){
      a.addEventListener('click', function(e){
        if (this.getAttribute('href') === '#top') {
          e.preventDefault();
          window.scrollTo({top:0, behavior:'smooth'});
        }
      });
    });
  })();
</script>
