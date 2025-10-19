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
  <meta name="color-scheme" content="light dark">

  <!-- Bootstrap -->
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">

  <style>
    /* ---------------- Core palette + Bootstrap mapping --------------- */
    :root{
      /* App tokens */
      --bg:#f6f8fb; --card:#ffffff; --text:#1f2937; --muted:#6b7280;
      --brand:#0d6efd; --accent:#6610f2; --ring:rgba(13,110,253,.25);
      --border:#e5e7eb; --hero-l:#eef4ff; --hero-r:#ecfff9;
      --shadow:0 10px 25px rgba(13,110,253,.08);

      /* Bootstrap tokens */
      --bs-body-bg: var(--bg);
      --bs-body-color: var(--text);
      --bs-border-color: var(--border);
      --bs-link-color: var(--brand);
      --bs-link-hover-color: #0b5ed7;
      --bs-heading-color: var(--text);
      --bs-card-bg: var(--card);
      --bs-card-color: var(--text);
    }
    [data-theme="dark"]{
      --bg:#0f1320; --card:#12172a; --text:#e5e7eb; --muted:#9aa3b2;
      --brand:#66a2ff; --accent:#b693ff; --ring:rgba(102,162,255,.3);
      --border:#1e2438; --hero-l:#0b1222; --hero-r:#0c1e1b;
      --shadow:0 14px 34px rgba(0,0,0,.45);

      --bs-body-bg: var(--bg);
      --bs-body-color: var(--text);
      --bs-border-color: var(--border);
      --bs-link-color: var(--brand);
      --bs-link-hover-color: #8ab8ff;
      --bs-heading-color: var(--text);
      --bs-card-bg: var(--card);
      --bs-card-color: var(--text);
    }

    html,body{background:var(--bs-body-bg);color:var(--bs-body-color)}
    .navbar{backdrop-filter:saturate(1.2) blur(4px);}
    .brand-grad{
      background:linear-gradient(90deg,var(--brand),var(--accent));
      -webkit-background-clip:text;background-clip:text;color:transparent
    }
    .hero{
      background:
        radial-gradient(1200px 600px at 0% 0%,var(--hero-l) 0,transparent 60%),
        radial-gradient(1200px 600px at 100% 0%,var(--hero-r) 0,transparent 60%);
      border-bottom:1px solid var(--bs-border-color);
    }

    .card{background:var(--bs-card-bg);border:1px solid var(--bs-border-color);box-shadow:var(--shadow);}
    .hover{transition:transform .18s ease,box-shadow .18s ease;}
    .hover:hover{transform:translateY(-3px);box-shadow:0 18px 40px rgba(13,110,253,.18);}
    [data-theme="dark"] .hover:hover{box-shadow:0 24px 56px rgba(0,0,0,.55);}

    .quick{min-height:120px;display:flex;align-items:center;padding:18px 20px}
    .kbd{border:1px solid var(--bs-border-color);border-bottom-width:2px;padding:.2rem .45rem;border-radius:.35rem;background:var(--card);color:var(--text)}
    .legend-dot{width:10px;height:10px;border-radius:999px;display:inline-block;margin-right:.4rem;}
    .dot-green{background:#15c26b;animation:pulseG 1.6s infinite ease-in-out;}
    .dot-red{background:#e55353;animation:pulseR 1.6s infinite ease-in-out;}
    @keyframes pulseG{0%{opacity:.6;transform:scale(.9)}50%{opacity:1;transform:scale(1.3)}100%{opacity:.6;transform:scale(.9)}}
    @keyframes pulseR{0%{opacity:.7;transform:scale(.9)}50%{opacity:1;transform:scale(1.25)}100%{opacity:.7;transform:scale(.9)}}
    .status-badge{padding:.25rem .5rem;border-radius:999px;border:1px solid var(--bs-border-color);font-weight:600;display:flex;align-items:center;gap:.3rem;}
    .mini-muted{font-size:.95rem;color:var(--muted)}
    .divider{height:1px;background:var(--bs-border-color);margin:1.2rem 0}
    .pill{border-radius:999px;padding:.25rem .6rem;border:1px solid var(--bs-border-color);}

    .fade-in{animation:fade .35s ease both}@keyframes fade{from{opacity:0;transform:translateY(6px)}to{opacity:1;transform:translateY(0)}}
    .status-floating{position:fixed;bottom:16px;right:16px;z-index:9999;background:var(--card);border:1px solid var(--bs-border-color);box-shadow:var(--shadow);border-radius:999px;padding:.4rem .9rem;font-size:.9rem;}

    /* Focus ring đẹp + accessible */
    .ring{transition:box-shadow .15s ease, transform .05s}
    .ring:focus-visible{outline:0;box-shadow:0 0 0 .2rem var(--ring)}
    .btn:active{transform:translateY(1px)}

    /* -------- Bootstrap utility dark-safe overrides -------- */
    [data-theme="dark"] .bg-light{background-color:var(--card)!important}
    [data-theme="dark"] .text-dark{color:var(--bs-body-color)!important}
    [data-theme="dark"] .btn-outline-secondary{
      color:var(--bs-body-color);border-color:var(--bs-border-color);background:transparent
    }
    [data-theme="dark"] .btn-outline-secondary:hover{
      background:rgba(255,255,255,.06);border-color:#3a4a6b;color:var(--bs-body-color)
    }
    [data-theme="dark"] .btn-outline-primary{color:var(--brand);border-color:var(--brand)}
    [data-theme="dark"] .btn-outline-primary:hover{background:rgba(102,162,255,.12)}

    /* Keyboard help modal */
    .kbd-modal{position:fixed;inset:0;display:none;place-items:center;background:rgba(0,0,0,.45);z-index:1050;padding:16px}
    .kbd-modal.open{display:grid}
    .kbd-sheet{max-width:520px;width:100%;background:var(--card);border:1px solid var(--bs-border-color);border-radius:14px;box-shadow:var(--shadow);padding:18px}
    .kbd-sheet h5{margin:0 0 8px}
    .kbd-sheet li{margin:.2rem 0}
    /* Toast */
    .toast-lite{position:fixed;right:14px;bottom:14px;display:none;background:#111827;color:#fff;padding:10px 12px;border-radius:10px;box-shadow:var(--shadow);font-size:13px}
    .toast-lite.show{display:block}
  </style>
</head>
<body class="fade-in">

<!-- NAV -->
<nav class="navbar navbar-expand-lg sticky-top border-bottom" style="background:var(--card)">
  <div class="container">
    <a class="navbar-brand fw-bold brand-grad" href="${pageContext.request.contextPath}/">LeaveMgmt</a>

    <div class="d-flex align-items-center gap-2 ms-auto">
      <!-- DB status -->
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
        <i id="themeIcon" class="bi bi-moon-stars"></i>
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
        <p class="lead mini-muted mb-4">Flow demo chuẩn: <strong>Create → List → Review</strong>. RBAC 3 vai trò (Employee / Manager / Division Leader).</p>

        <c:if test="${not empty sessionScope.user}">
          <div class="alert" style="background:var(--card);border:1px solid var(--bs-border-color)">
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
          <button class="btn btn-lg btn-outline-primary ring" id="openKbd"><i class="bi bi-keyboard me-1"></i>Phím tắt</button>
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

<!-- QUICK -->
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
            <h5 class="card-title mb-3"><i class="bi bi-info-circle"></i> Mẹo sử dụng</h5>
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
            <button id="copyDemo" class="btn btn-sm btn-outline-primary mt-3 ring"><i class="bi bi-clipboard"></i> Sao chép tài khoản demo</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</section>

<footer class="py-4 border-top">
  <div class="container d-flex flex-wrap justify-content-between">
    <span>© <fmt:formatDate value="<%=new java.util.Date()%>" pattern="yyyy"/> LeaveMgmt</span>
    <span class="mini-muted">FALL 2025 · JSP/Servlet + JDBC · v1.3</span>
  </div>
</footer>

<!-- Floating mini notice -->
<c:if test="${not requestScope.dbOK}">
  <div class="status-floating"><span class="legend-dot dot-red"></span> Database disconnected</div>
</c:if>

<!-- Keyboard Help Modal -->
<div id="kbdModal" class="kbd-modal" role="dialog" aria-modal="true" aria-labelledby="kbdTitle">
  <div class="kbd-sheet">
    <h5 id="kbdTitle"><i class="bi bi-keyboard"></i> Phím tắt</h5>
    <ul class="mb-3">
      <li><span class="kbd">D</span> — Bật/tắt Dark mode</li>
      <li><span class="kbd">G</span> + <span class="kbd">C</span> — Mở Tạo đơn</li>
      <li><span class="kbd">G</span> + <span class="kbd">M</span> — Đơn của tôi</li>
      <li><span class="kbd">G</span> + <span class="kbd">T</span> — Đơn cấp dưới</li>
      <li><span class="kbd">?</span> — Mở bảng này</li>
    </ul>
    <div class="d-flex gap-2">
      <button id="closeKbd" class="btn btn-secondary ring">Đóng</button>
    </div>
  </div>
</div>

<div id="toast" class="toast-lite"></div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
  (function(){
    const root = document.documentElement;
    const icon = document.getElementById('themeIcon');
    const toggleBtn = document.getElementById('themeToggle');
    const toast = document.getElementById('toast');
    const ctx = '<%=request.getContextPath()%>';

    // ---------- Theme boot: prefer saved, else system ----------
    const saved = localStorage.getItem('theme');
    if(saved){ root.setAttribute('data-theme', saved); }
    else{
      try{
        if(window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches){
          root.setAttribute('data-theme', 'dark');
        }
      }catch(_){}
    }
    syncThemeIcon();

    function showToast(msg, ms){
      toast.textContent = msg;
      toast.classList.add('show');
      setTimeout(function(){ toast.classList.remove('show'); }, ms || 1600);
    }
    function currentTheme(){ return root.getAttribute('data-theme') || 'light'; }
    function setTheme(next){
      root.setAttribute('data-theme', next);
      localStorage.setItem('theme', next);
      syncThemeIcon();
      showToast('Đã chuyển sang giao diện ' + (next==='dark'?'tối':'sáng') + '.');
    }
    function syncThemeIcon(){
      if(!icon) return;
      var isDark = currentTheme()==='dark';
      icon.className = isDark ? 'bi bi-sun' : 'bi bi-moon-stars';
      toggleBtn.setAttribute('aria-label', isDark ? 'Chuyển sang Light' : 'Chuyển sang Dark');
    }

    toggleBtn && toggleBtn.addEventListener('click', function(){
      var next = currentTheme()==='light' ? 'dark' : 'light';
      setTheme(next);
    });

    // ---------- Keyboard shortcuts ----------
    var goHeld = false;
    document.addEventListener('keydown', function(e){
      var k = (e.key || '').toLowerCase();
      if(k==='d' && !e.ctrlKey && !e.metaKey){
        // theme
        e.preventDefault();
        toggleBtn && toggleBtn.click();
        return;
      }
      if(k==='?'){
        e.preventDefault();
        openKbd();
        return;
      }
      if(k==='g'){ goHeld = true; return; }
      if(goHeld){
        if(k==='c'){ location.href = ctx + '/request/create'; }
        if(k==='m'){ location.href = ctx + '/request/list?scope=mine'; }
        if(k==='t'){ location.href = ctx + '/request/list?scope=team'; }
        goHeld = false;
      }
    });
    document.addEventListener('keyup', function(e){
      if((e.key||'').toLowerCase()==='g') goHeld = false;
    });

    // ---------- Keyboard help modal ----------
    var kbdModal = document.getElementById('kbdModal');
    var openBtn = document.getElementById('openKbd');
    var closeBtn = document.getElementById('closeKbd');
    function openKbd(){ kbdModal.classList.add('open'); }
    function closeKbd(){ kbdModal.classList.remove('open'); }
    openBtn && openBtn.addEventListener('click', openKbd);
    closeBtn && closeBtn.addEventListener('click', closeKbd);
    kbdModal && kbdModal.addEventListener('click', function(ev){ if(ev.target===kbdModal) closeKbd(); });

    // ---------- Copy demo accounts ----------
    var copyBtn = document.getElementById('copyDemo');
    copyBtn && copyBtn.addEventListener('click', function(){
      var txt = 'Employee\\nuser: emp_e\\npass: 123456\\n\\nManager\\nuser: manager_it\\npass: 123456';
      if(navigator.clipboard && navigator.clipboard.writeText){
        navigator.clipboard.writeText(txt).then(function(){ showToast('Đã sao chép thông tin demo.'); });
      }else{
        // Fallback
        var ta = document.createElement('textarea');
        ta.value = txt; document.body.appendChild(ta); ta.select();
        try{ document.execCommand('copy'); showToast('Đã sao chép thông tin demo.'); }catch(_){}
        document.body.removeChild(ta);
      }
    });

    // ---------- Respect reduced motion ----------
    try{
      if(window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches){
        var styles = document.createElement('style');
        styles.textContent = '.fade-in, .hover{animation:none!important;transition:none!important}';
        document.head.appendChild(styles);
      }
    }catch(_){}
  })();
</script>
</body>
</html>
