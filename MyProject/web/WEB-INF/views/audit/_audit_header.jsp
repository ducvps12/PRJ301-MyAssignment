<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp" %>
<c:set var="cp" value="${pageContext.request.contextPath}" />

<style>
  :root{
    --h:64px; --sbw:220px;
    --bg:#f7f9fc; --card:#fff; --tx:#0f172a; --muted:#64748b; --bd:#e5e7eb;
    --pri:#2563eb; --subtle:#f1f5ff; --shadow:0 10px 30px rgba(2,6,23,.06);
  }
  @media (prefers-color-scheme: dark){
    :root{ --bg:#0b1220; --card:#0f172a; --tx:#e5e7eb; --muted:#94a3b8; --bd:#1f2937; --subtle:#0f1a3a }
  }
  *{box-sizing:border-box}
  body.admin{margin:0;background:var(--bg);color:var(--tx);font-family:system-ui,Segoe UI,Roboto,Helvetica,Arial,sans-serif}
  a{color:inherit;text-decoration:none}

  /* Header cố định */
  .au-header{position:fixed;left:var(--sbw);right:0;top:0;height:var(--h);z-index:20;
    display:flex;align-items:center;justify-content:space-between;gap:12px;
    background:linear-gradient(180deg,rgba(255,255,255,.92),rgba(255,255,255,.75));
    border-bottom:1px solid var(--bd);backdrop-filter:blur(8px);padding:0 16px}
  .au-tools{display:flex;gap:8px;flex-wrap:wrap}
  .btn{display:inline-flex;align-items:center;gap:8px;border:1px solid var(--bd);background:var(--card);
    padding:8px 12px;border-radius:10px;cursor:pointer}
  .btn.icon{width:38px;height:38px;justify-content:center;padding:0}
  .chip{display:inline-flex;align-items:center;border:1px solid var(--bd);padding:2px 8px;border-radius:999px;font-size:12px;color:var(--muted)}
</style>

<header class="au-header admin-header">
  <div class="au-tools">
    <button class="btn icon" onclick="window.toggleSidebar?.()" title="Sidebar">☰</button>
    <span class="chip">Audit Log</span>
  </div>
  <div class="au-tools">
    <button class="btn icon" onclick="location.reload()" title="Refresh">⟳</button>
    <a class="btn" href="${cp}/logout">Đăng xuất</a>
  </div>
</header>

<script>
  // Header đo kích thước để set --h, --sbw (sẽ được sidebar cập nhật nốt)
  addEventListener('DOMContentLoaded', ()=> {
    const h = document.querySelector('.au-header')?.offsetHeight || 64;
    document.documentElement.style.setProperty('--h', h+'px');
  });
</script>
