<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<jsp:useBean id="now" class="java.util.Date" />

<footer class="app-footer" role="contentinfo">
  <div class="foot-wrap">
    <div class="foot-brand">
      <div class="logo" aria-hidden="true">LM</div>
      <div>
        <div class="title">
          <strong>LeaveMgmt</strong> <span class="muted">· Admin Console</span>
        </div>
        <div class="muted small">© <fmt:formatDate value="${now}" pattern="yyyy"/> All rights reserved.</div>
      </div>
    </div>

    <nav class="foot-links" aria-label="Liên kết">
      <a href="${pageContext.request.contextPath}/request/list">Requests</a>
      <a href="${pageContext.request.contextPath}/request/agenda">Agenda</a>
      <a href="${pageContext.request.contextPath}/admin">Dashboard</a>
      <a href="${pageContext.request.contextPath}/admin/users">Users</a>
    </nav>

    <div class="foot-actions">
      <button id="btnTheme" class="btn small" type="button" title="Đổi theme (light/dark)">🌓</button>
      <button id="btnTop" class="btn small" type="button" title="Lên đầu trang">↑ Top</button>
    </div>
  </div>

  <div class="foot-meta">
    <span class="muted small">
      <c:if test="${not empty applicationScope.appVersion}">
        v${applicationScope.appVersion}
      </c:if>
      <c:if test="${not empty applicationScope.buildDate}">
        · build ${applicationScope.buildDate}
      </c:if>
    </span>
    <span class="muted small hide-on-mobile">Phím tắt: “/” tìm kiếm · “R” làm mới · “A” chọn tất cả</span>
  </div>
</footer>


<style>
  /* ===== Footer */
  .app-footer{
    margin-top:24px;border-top:1px solid var(--bd);
    padding:16px 0;color:var(--text);background:transparent
  }
  .app-footer .foot-wrap{
    max-width:1200px;margin:0 auto;padding:0 16px;
    display:grid;grid-template-columns:1fr auto auto;gap:12px;align-items:center
  }
  .app-footer .foot-brand{display:flex;gap:10px;align-items:center}
  .app-footer .logo{
    width:36px;height:36px;border-radius:10px;
    display:flex;align-items:center;justify-content:center;
    background:var(--row);border:1px solid var(--bd);font-weight:800
  }
  .app-footer .title{line-height:1}
  .app-footer .muted{color:var(--muted)}
  .app-footer .small{font-size:12px}

  .app-footer .foot-links{display:flex;gap:12px;flex-wrap:wrap;justify-content:center}
  .app-footer .foot-links a{
    text-decoration:none;color:var(--text);border:1px solid var(--bd);
    padding:6px 10px;border-radius:10px;background:var(--card);
  }
  .app-footer .foot-links a:hover{box-shadow:0 0 0 3px var(--ring)}

  .app-footer .foot-actions{display:flex;gap:8px;justify-content:flex-end}
  .app-footer .btn{
    display:inline-flex;align-items:center;gap:6px;padding:8px 12px;
    border:1px solid var(--bd);border-radius:10px;background:var(--card);
    color:var(--text);text-decoration:none;cursor:pointer;min-height:36px
  }
  .app-footer .btn.small{font-size:12px;padding:6px 10px}

  .app-footer .foot-meta{
    max-width:1200px;margin:8px auto 0; padding:0 16px;
    display:flex;gap:12px;align-items:center;justify-content:space-between
  }

  /* Responsive */
  @media (max-width: 768px){
    .app-footer .foot-wrap{
      grid-template-columns:1fr;gap:10px;text-align:center
    }
    .app-footer .foot-actions{justify-content:center}
    .app-footer .foot-brand{justify-content:center}
    .app-footer .hide-on-mobile{display:none}
  }

  /* Respect reduced motion */
  @media (prefers-reduced-motion: reduce){
    .app-footer *{transition:none !important;animation:none !important}
  }
</style>

<script>
(function(){
  // Theme toggle – lưu localStorage("theme")
  var btnTheme = document.getElementById('btnTheme');
  if(btnTheme){
    btnTheme.addEventListener('click', function(){
      var root = document.documentElement;
      var cur = root.getAttribute('data-theme') || 'light';
      var next = cur === 'light' ? 'dark' : 'light';
      root.setAttribute('data-theme', next);
      try{ localStorage.setItem('theme', next); }catch(_){}
    });
    // load theme đã lưu
    try{
      var saved = localStorage.getItem('theme');
      if(saved){ document.documentElement.setAttribute('data-theme', saved); }
    }catch(_){}
  }

  // Scroll to top
  var btnTop = document.getElementById('btnTop');
  if(btnTop){
    btnTop.addEventListener('click', function(){
      window.scrollTo({top:0, behavior:'smooth'});
    });
  }
})();
</script>
