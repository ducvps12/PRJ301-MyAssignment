<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp" %>

<c:set var="ctx" value="${empty ctx ? pageContext.request.contextPath : ctx}" />
<c:set var="u"   value="${not empty sessionScope.currentUser ? sessionScope.currentUser : sessionScope.user}" />
<c:set var="uName" value="${not empty u ? (u.full_name != null ? u.full_name : (u.fullName != null ? u.fullName : '')) : ''}" />
<c:set var="uRole" value="${not empty u && u.role != null ? u.role : ''}" />
<c:set var="dbOK"    value="${requestScope.dbOK}" />
<c:set var="dbMs"    value="${requestScope.dbMs}" />
<c:set var="appMode" value="${initParam['app.mode']}" />
<c:set var="appVer"  value="${initParam['app.version']}" />
<c:set var="buildAt" value="${initParam['app.buildAt']}" />

<c:set var="appModeClass" value="" />
<c:if test="${not empty appMode}">
  <c:choose>
    <c:when test="${appMode == 'DEV' or appMode == 'Dev' or appMode == 'dev'}"><c:set var="appModeClass" value="dev"/></c:when>
    <c:when test="${appMode == 'STAGING' or appMode == 'Staging' or appMode == 'staging'}"><c:set var="appModeClass" value="staging"/></c:when>
    <c:when test="${appMode == 'PROD' or appMode == 'Prod' or appMode == 'prod' or appMode == 'PRODUCTION'}"><c:set var="appModeClass" value="prod"/></c:when>
  </c:choose>
</c:if>

<footer class="admin-footer" role="contentinfo" aria-label="Phần chân trang">
  <div class="wrap">
    <div class="left">
      <strong>Portal</strong><span class="sep" aria-hidden="true">•</span>

      <c:choose>
        <c:when test="${not empty uName}">
          <span>Đăng nhập: <b><c:out value="${uName}"/></b>
            <c:if test="${not empty uRole}">
              <small class="muted">(<c:out value="${uRole}"/>)</small>
            </c:if>
          </span>
        </c:when>
        <c:otherwise><span class="muted">Chưa đăng nhập</span></c:otherwise>
      </c:choose>

      <c:if test="${not empty appMode}">
        <span class="sep" aria-hidden="true">•</span>
        <span>Mode: <span class="badge env ${appModeClass}"><c:out value="${appMode}"/></span></span>
      </c:if>
      <c:if test="${not empty appVer}">
        <span class="sep" aria-hidden="true">•</span>
        <span>Version: <code class="muted"><c:out value="${appVer}"/></code></span>
      </c:if>
      <c:if test="${not empty buildAt}">
        <span class="sep" aria-hidden="true">•</span>
        <span>Build: <small class="muted"><c:out value="${buildAt}"/></small></span>
      </c:if>

      <c:if test="${dbOK ne null}">
        <span class="sep" aria-hidden="true">•</span>
        <span>DB:
          <span class="badge ${dbOK ? 'ok' : 'no'}" aria-live="polite">
            <c:out value="${dbOK ? 'OK' : 'DOWN'}"/>
          </span>
          <c:if test="${not empty dbMs}">
            <small class="muted">(<c:out value="${dbMs}"/> ms)</small>
          </c:if>
        </span>
      </c:if>
    </div>

    <div class="right">
      <span class="muted">© <span id="y"></span> LeaveMgmt</span>
      <span class="sep" aria-hidden="true">•</span>
      <a href="#top" class="btn-top" aria-label="Lên đầu trang" data-visible="false">↑ Top</a>
    </div>
  </div>
</footer>

<style>
  :root{
    --bd:#e5e7eb; --tx:#111827; --muted:#6b7280; --card:#fff; --pri:#2563eb;
  }
  @media (prefers-color-scheme: dark){
    :root{ --bd:#1f2937; --tx:#e5e7eb; --muted:#94a3b8; --card:#0f172a; }
  }

  .admin-footer{
    border-top:1px solid var(--bd);
    background:var(--card);
    color:var(--tx);
    margin-top:24px;
    position:relative;
    z-index:1;
    padding-bottom:env(safe-area-inset-bottom);
  }
  .admin-footer .wrap{
    max-width:1280px; /* đồng bộ header */
    margin:0 auto;
    padding:12px clamp(12px,2.5vw,16px);
    display:flex; align-items:center; justify-content:space-between; gap:12px; flex-wrap:wrap;
  }
  .left,.right{display:flex; align-items:center; gap:10px; flex-wrap:wrap}
  .muted{color:var(--muted)}
  .sep{color:var(--bd)}
  .badge{padding:2px 8px; border-radius:999px; font-size:12px; border:1px solid var(--bd)}
  .badge.ok{background:#ecfdf5; color:#065f46; border-color:#a7f3d0}
  .badge.no{background:#fef2f2; color:#991b1b; border-color:#fecaca}
  .badge.env{background:#eef2ff; border-color:#c7d2fe; color:#1e3a8a}
  .badge.env.dev{background:#ecfeff; border-color:#a5f3fc; color:#155e75}
  .badge.env.staging{background:#fff7ed; border-color:#fed7aa; color:#9a3412}
  .badge.env.prod{background:#f0fdf4; border-color:#bbf7d0; color:#14532d}

  .btn-top{
    border:1px solid var(--bd);
    border-radius:10px;
    padding:6px 10px;
    text-decoration:none;
    color:inherit;
    background:transparent;
    opacity:0; pointer-events:none;
    transition:opacity .2s ease;
  }
  .btn-top[data-visible="true"]{ opacity:1; pointer-events:auto }

  /* In ấn gọn gàng */
  @media print{
    .admin-footer{border-top:none; background:#fff}
    .btn-top{display:none}
  }
</style>

<script>
(function(){
  // Năm hiện tại
  const y=document.getElementById('y'); if(y) y.textContent=new Date().getFullYear();

  // Nút Top – hiện khi kéo xuống
  const btn=document.querySelector('.btn-top');
  if(btn){
    const toggle = ()=>{ btn.dataset.visible = String(window.scrollY > 200); };
    toggle();
    window.addEventListener('scroll', toggle, {passive:true});

    // Click cuộn mượt
    document.addEventListener('click', e=>{
      const a=e.target.closest('.btn-top'); if(!a) return;
      e.preventDefault();
      const opts={top:0,behavior:'smooth'};
      try{ window.scrollTo(opts); }catch(_){ window.scrollTo(0,0); }
    });

    // Hotkey: Alt+↑ về đầu trang
    document.addEventListener('keydown', e=>{
      if(e.altKey && (e.key==='ArrowUp' || e.key==='ArrowLeft')){
        e.preventDefault();
        try{ window.scrollTo({top:0,behavior:'smooth'}); }catch(_){ window.scrollTo(0,0); }
      }
    });

    // Tôn trọng reduced motion
    try{
      const mq = window.matchMedia('(prefers-reduced-motion: reduce)');
      mq.addEventListener?.('change', toggle);
    }catch(_){}
  }
})();
</script>
