<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<footer class="admin-footer" role="contentinfo" aria-label="Phần chân trang quản trị">
  <div class="wrap">
    <div class="left">
      <strong>Admin</strong>
      <span class="sep">•</span>
      <c:choose>
        <c:when test="${not empty sessionScope.user}">
          <span>Đăng nhập: <b>${sessionScope.user.fullName}</b> <small class="muted">(${sessionScope.user.role})</small></span>
        </c:when>
        <c:otherwise><span class="muted">Chưa đăng nhập</span></c:otherwise>
      </c:choose>
      <span class="sep">•</span>
      <span>Mode: <b>${initParam['app.mode']}</b></span>
      <c:if test="${not empty requestScope.dbOK}">
        <span class="sep">•</span>
        <span>
          DB: 
          <span class="badge ${dbOK ? 'ok' : 'no'}">${dbOK ? 'OK' : 'DOWN'}</span>
          <small class="muted">(${dbMs} ms)</small>
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
  .admin-footer{border-top:1px solid #e5e7eb;background:#fff;margin-top:24px}
  .admin-footer .wrap{max-width:1200px;margin:0 auto;padding:12px 16px;display:flex;gap:12px;align-items:center;justify-content:space-between;flex-wrap:wrap}
  .admin-footer .left,.admin-footer .right{display:flex;gap:10px;align-items:center;flex-wrap:wrap}
  .muted{color:#6b7280}
  .sep{color:#d1d5db}
  .badge{padding:2px 8px;border-radius:999px;font-size:12px;border:1px solid #e5e7eb}
  .badge.ok{background:#ecfdf5;color:#065f46;border-color:#a7f3d0}
  .badge.no{background:#fef2f2;color:#991b1b;border-color:#fecaca}
  .btn-top{border:1px solid #e5e7eb;border-radius:10px;padding:6px 10px;text-decoration:none;color:#111827;background:#fff}
  .btn-top:focus{outline:2px solid #2563eb;outline-offset:2px}
</style>

<script>
  // Năm hiện tại + scroll to top mượt
  (function(){
    var y = document.getElementById('y');
    if (y) y.textContent = new Date().getFullYear();
    document.querySelectorAll('.btn-top').forEach(function(a){
      a.addEventListener('click', function(e){
        if (location.hash === '#top' || this.getAttribute('href') === '#top') {
          e.preventDefault(); window.scrollTo({top:0, behavior:'smooth'});
        }
      });
    });
  })();
</script>
