<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@page import="com.acme.leavemgmt.util.DBConnection"%>
<%
    long _t0 = System.nanoTime();
    boolean _dbOK = false;
    try { _dbOK = DBConnection.ping(); } catch (Throwable ignore) {}
    long _ms = (System.nanoTime() - _t0) / 1_000_000L;
    request.setAttribute("dbOK", _dbOK);
    request.setAttribute("dbMs", _ms);
%>
<!DOCTYPE html>
<html lang="vi" data-theme="light">
<head>
  <meta charset="UTF-8">
  <title>Leave Management – Landing</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">

  <!-- Bootstrap -->
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">

  <style>
    :root{
      --bg:#f6f8fb;--card:#ffffff;--text:#212529;--muted:#6c757d;
      --brand:#0d6efd;--ring:rgba(13,110,253,.2);--shadow:0 10px 25px rgba(13,110,253,.08);
      --border:#e9ecef;--hero-l:#eef4ff;--hero-r:#ecfff9;--accent:#6610f2;
    }
    [data-theme="dark"]{
      --bg:#0f1320;--card:#12172a;--text:#dee2e6;--muted:#9aa3b2;
      --brand:#66a2ff;--ring:rgba(102,162,255,.25);--shadow:0 10px 25px rgba(0,0,0,.35);
      --border:#1e2438;--hero-l:#0b1222;--hero-r:#0c1e1b;--accent:#b693ff;
    }
    html,body{background:var(--bg); color:var(--text)}
    .navbar{backdrop-filter:saturate(1.2) blur(4px);}
    .brand-grad{background:linear-gradient(90deg,var(--brand),var(--accent));-webkit-background-clip:text;background-clip:text;color:transparent}
    .hero{background:radial-gradient(1200px 600px at 0% 0%,var(--hero-l) 0,transparent 60%),radial-gradient(1200px 600px at 100% 0%,var(--hero-r) 0,transparent 60%);border-bottom:1px solid var(--border);}
    .card{background:var(--card);border:1px solid var(--border);box-shadow:var(--shadow);}
    .card.hover{transition:transform .18s ease,box-shadow .18s ease;}
    .card.hover:hover{transform:translateY(-3px);box-shadow:0 18px 40px rgba(13,110,253,.18);}
    .quick{min-height:120px;display:flex;align-items:center;padding:18px 20px}
    .kbd{border:1px solid var(--border);border-bottom-width:2px;padding:.2rem .45rem;border-radius:.35rem;background:var(--card)}
    .legend-dot{width:10px;height:10px;border-radius:999px;display:inline-block;margin-right:.4rem;}
    .dot-green{background:#15c26b;animation:pulseG 1.6s infinite ease-in-out;}
    .dot-red{background:#e55353;animation:pulseR 1.6s infinite ease-in-out;}
    @keyframes pulseG{0%{opacity:.6;transform:scale(.9)}50%{opacity:1;transform:scale(1.3)}100%{opacity:.6;transform:scale(.9)}}
    @keyframes pulseR{0%{opacity:.7;transform:scale(.9)}50%{opacity:1;transform:scale(1.25)}100%{opacity:.7;transform:scale(.9)}}
    .status-badge{padding:.25rem .5rem;border-radius:999px;border:1px solid var(--border);font-weight:600;display:flex;align-items:center;gap:.3rem;}
    .fade-in{animation:fade .35s ease both}@keyframes fade{from{opacity:0;transform:translateY(6px)}to{opacity:1;transform:translateY(0)}}
    .mini-muted{font-size:.9rem;color:var(--muted)}
    .divider{height:1px;background:var(--border);margin:1.2rem 0}
    .pill{border-radius:999px;padding:.25rem .6rem;border:1px solid var(--border);}
    .status-floating{position:fixed;bottom:16px;right:16px;z-index:9999;background:var(--card);border:1px solid var(--border);box-shadow:var(--shadow);border-radius:999px;padding:.4rem .9rem;font-size:.9rem;}
  </style>
</head>
<body class="fade-in">

<!-- NAV -->
<nav class="navbar navbar-expand-lg sticky-top border-bottom" style="background:var(--card)">
  <div class="container">
    <a class="navbar-brand fw-bold brand-grad" href="${pageContext.request.contextPath}/">LeaveMgmt</a>
    <div class="d-flex align-items-center gap-2 ms-auto">

      <!-- ✅ DB status badge -->
      <c:choose>
        <c:when test="${requestScope.dbOK}">
          <span class="status-badge mini-muted" title="Database connected">
            <span class="legend-dot dot-green"></span>
            DB OK · <c:out value="${requestScope.dbMs}"/> ms
          </span>
        </c:when>
        <c:otherwise>
          <span class="status-badge mini-muted" title="Database NOT reachable" style="border-color:#ffb3b3">
            <span class="legend-dot dot-red"></span> DB FAIL
          </span>
        </c:otherwise>
      </c:choose>

      <button id="themeToggle" class="btn btn-sm btn-outline-secondary ring" title="Dark/Light (D)">
        <i class="bi bi-moon-stars"></i>
      </button>

      <c:choose>
        <c:when test="${not empty sessionScope.user}">
          <a class="btn btn-outline-secondary ring" href="${pageContext.request.contextPath}/request/list?scope=mine"><i class="bi bi-list-check me-1"></i>Danh sách</a>
          <a class="btn btn-primary ring" href="${pageContext.request.contextPath}/logout"><i class="bi bi-box-arrow-right me-1"></i>Đăng xuất</a>
        </c:when>
        <c:otherwise>
          <a class="btn btn-primary ring" href="${pageContext.request.contextPath}/login"><i class="bi bi-person-lock me-1"></i>Đăng nhập</a>
        </c:otherwise>
      </c:choose>
    </div>
  </div>
</nav>

<!-- HERO -->
<section class="hero py-5">
  <div class="container">
    <div class="row g-4 align-items-center">
      <div class="col-lg-7">
        <h1 class="display-5 fw-bold mb-3">Quản lý <span class="brand-grad">đơn xin nghỉ</span> nhanh – gọn – đúng quy trình</h1>
        <p class="lead mini-muted mb-4">Flow demo chuẩn: <strong>Create → List → Review</strong>. RBAC 3 vai trò (Employee/Manager/Division Leader).</p>
        <c:if test="${not empty sessionScope.user}">
          <div class="alert" style="background:var(--card);border:1px solid var(--border)">
            Xin chào, <strong>${sessionScope.user.fullName}</strong>
            <c:if test="${sessionScope.isManager}"> · <span class="pill">MANAGER</span></c:if>
            <c:if test="${sessionScope.isDivisionLeader}"> · <span class="pill">DIVISION LEADER</span></c:if>
          </div>
        </c:if>

        <div class="d-flex flex-wrap gap-2">
          <c:choose>
            <c:when test="${empty sessionScope.user}">
              <a class="btn btn-lg btn-primary ring" href="${pageContext.request.contextPath}/login"><i class="bi bi-rocket-takeoff me-1"></i>Bắt đầu ngay</a>
              <a class="btn btn-lg btn-outline-secondary ring" href="#quick">Tính năng</a>
            </c:when>
            <c:otherwise>
              <a class="btn btn-lg btn-primary ring" href="${pageContext.request.contextPath}/request/create"><i class="bi bi-plus-circle me-1"></i>Tạo đơn</a>
              <a class="btn btn-lg btn-outline-secondary ring" href="${pageContext.request.contextPath}/request/list?scope=mine"><i class="bi bi-list-ul me-1"></i>Đơn của tôi</a>
            </c:otherwise>
          </c:choose>
          <a class="btn btn-lg btn-outline-primary ring" href="#help"><i class="bi bi-mortarboard me-1"></i>Hướng dẫn</a>
        </div>
      </div>

      <div class="col-lg-5">
        <div class="card p-3 hover">
          <div class="card-body">
            <h5 class="card-title mb-3"><i class="bi bi-bookmark-check"></i> Hướng dẫn nhanh</h5>
            <ol class="mb-3">
              <li>Đăng nhập bằng tài khoản demo hoặc của bạn.</li>
              <li>Tạo đơn với <b>Từ ngày / Đến ngày / Lý do</b>.</li>
              <li>Mở “Đơn của tôi” để theo dõi trạng thái.</li>
              <li>Quản lý vào “Đơn cấp dưới” để duyệt hoặc từ chối.</li>
            </ol>
            <div class="mini-muted">
              <span class="legend-dot dot-green"></span>Đi làm · 
              <span class="legend-dot dot-red"></span>Nghỉ (trên Agenda)
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</section>

<!-- QUICK + DEMO ACCOUNTS -->
<section id="quick" class="py-5">
  <div class="container">
    <div class="d-flex align-items-center justify-content-between mb-3">
      <h2 class="h4 fw-bold mb-0">Truy cập nhanh</h2>
    </div>
    <div class="row g-3">
      <div class="col-sm-6 col-xl-3"><a class="text-decoration-none" href="${pageContext.request.contextPath}/request/create">
        <div class="card quick hover"><div><h5 class="mb-1"><i class="bi bi-file-plus me-1"></i>Tạo đơn</h5><small class="mini-muted">Khởi tạo trạng thái <span class="status-badge">Inprogress</span></small></div></div></a></div>
      <div class="col-sm-6 col-xl-3"><a class="text-decoration-none" href="${pageContext.request.contextPath}/request/list?scope=mine">
        <div class="card quick hover d-flex justify-content-between"><div><h5 class="mb-1"><i class="bi bi-list-task me-1"></i>Đơn của tôi</h5><small class="mini-muted">Theo dõi trạng thái</small></div></div></a></div>
      <c:if test="${sessionScope.isManager || sessionScope.isDivisionLeader}">
        <div class="col-sm-6 col-xl-3"><a class="text-decoration-none" href="${pageContext.request.contextPath}/request/list?scope=team">
          <div class="card quick hover"><div><h5 class="mb-1"><i class="bi bi-people me-1"></i>Đơn cấp dưới</h5><small class="mini-muted">Duyệt hoặc từ chối</small></div></div></a></div>
      </c:if>
      <div class="col-sm-6 col-xl-3"><a class="text-decoration-none" href="${pageContext.request.contextPath}/agenda">
        <div class="card quick hover"><div><h5 class="mb-1"><i class="bi bi-calendar-week me-1"></i>Agenda</h5><small class="mini-muted">Lịch phòng ban</small></div></div></a></div>
    </div>

    <div class="divider"></div>

    <div class="row g-3">
      <div class="col-lg-7">
        <div class="card p-3 hover">
          <div class="card-body">
            <h5 class="card-title mb-3" id="help"><i class="bi bi-info-circle"></i> Mẹo sử dụng</h5>
            <ul class="mb-0">
              <li>Trạng thái ban đầu là <b>Inprogress</b>.</li>
              <li>Chỉ quản lý/cấp phòng mới được duyệt đơn.</li>
              <li>Dùng phím <span class="kbd">G</span> + <span class="kbd">C</span> để tạo nhanh.</li>
            </ul>
          </div>
        </div>
      </div>
      <div class="col-lg-5">
        <div class="card p-3 hover">
          <div class="card-body">
            <h5 class="card-title mb-3"><i class="bi bi-person-badge"></i> Tài khoản demo</h5>
            <div class="d-flex justify-content-between">
              <div><div class="fw-semibold">Employee</div><div class="mini-muted">user: <code>emp_e</code><br>pass: <code>123456</code></div></div>
              <div><div class="fw-semibold">Manager</div><div class="mini-muted">user: <code>manager_it</code><br>pass: <code>123456</code></div></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</section>

<footer class="py-4 border-top">
  <div class="container d-flex flex-wrap justify-content-between">
    <span>© <fmt:formatDate value="<%=new java.util.Date()%>" pattern="yyyy"/> LeaveMgmt</span>
    <span class="mini-muted">FALL 2025 · JSP/Servlet + JDBC · v1.2</span>
  </div>
</footer>

<!-- Floating mini notice -->
<c:if test="${not requestScope.dbOK}">
  <div class="status-floating"><span class="legend-dot dot-red"></span> Database disconnected</div>
</c:if>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
  const root=document.documentElement,themeToggle=document.getElementById('themeToggle');
  const saved=localStorage.getItem('theme');if(saved)root.setAttribute('data-theme',saved);
  themeToggle?.addEventListener('click',()=>{const c=root.getAttribute('data-theme')||'light';const n=c==='light'?'dark':'light';root.setAttribute('data-theme',n);localStorage.setItem('theme',n);});
  document.addEventListener('keydown',e=>{const ctx='<%=request.getContextPath()%>';if(e.key.toLowerCase()==='d'&&!e.ctrlKey&&!e.metaKey)themeToggle?.click();
  if(e.key.toLowerCase()==='g'){window.__goPressed=true;return;}
  if(window.__goPressed){if(e.key.toLowerCase()==='c')location.href=ctx+'/request/create';if(e.key.toLowerCase()==='m')location.href=ctx+'/request/list?scope=mine';if(e.key.toLowerCase()==='t')location.href=ctx+'/request/list?scope=team';window.__goPressed=false;}});
</script>
</body>
</html>
