<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi" data-theme="${sessionScope.theme != null ? sessionScope.theme : 'light'}">
<head>
  <meta charset="UTF-8">
  <title>${param.title != null ? param.title : 'Admin'}</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">

  <style>
    /* ===== Tokens (enterprise) ===== */
    :root{
      --bg:#f7f8fb; --card:#fff; --ink:#0f172a; --muted:#64748b; --bd:#e5e7eb;
      --pri:#111827; --accent:#2563eb;
      --sidebar-w:232px; --sidebar-mini-w:72px; --header-h:56px;
      --shadow:0 1px 2px rgba(0,0,0,.05);
    }
    [data-theme="dark"]{
      --bg:#0b1220; --card:#0f172a; --ink:#e5e7eb; --muted:#9ca3af; --bd:#1f2937;
      --pri:#e5e7eb; --accent:#60a5fa; --shadow:0 1px 2px rgba(0,0,0,.6);
    }

    *{box-sizing:border-box; margin:0; padding:0}
    body{background:var(--bg); color:var(--ink);
      font-family:Inter, ui-sans-serif, system-ui, -apple-system, "Segoe UI", Roboto, Arial; line-height:1.55}

    /* ===== Header (no icon) ===== */
    .header{position:fixed; inset:0 0 auto 0; height:var(--header-h); z-index:40;
      display:flex; align-items:center; justify-content:space-between; gap:12px;
      padding:0 16px; background:var(--card); border-bottom:1px solid var(--bd); box-shadow:var(--shadow)}
    .h-left{display:flex; align-items:center; gap:12px}
    .brand{display:flex; align-items:center; gap:10px}
    .brand .mark{width:22px; height:22px; border-radius:4px; background:#111827}
    .brand b{font-weight:700; letter-spacing:.2px}
    .btn-icon{width:34px; height:34px; border:1px solid var(--bd); border-radius:8px; background:var(--card); cursor:pointer}
    .search{min-width:320px; position:relative}
    .search input{width:100%; padding:9px 12px; border:1px solid var(--bd); border-radius:8px; background:var(--card); color:var(--ink)}
    .h-right{display:flex; align-items:center; gap:10px}
    .who{font-size:13px; color:var(--muted)}

    /* ===== Sidebar (text-only) ===== */
    .sidebar{position:fixed; top:var(--header-h); left:0; bottom:0; width:var(--sidebar-w);
      background:var(--card); border-right:1px solid var(--bd); box-shadow:var(--shadow); overflow:auto; z-index:30}
    .sb-sec{padding:18px 16px 6px; font-size:11px; letter-spacing:.06em; color:var(--muted); text-transform:uppercase}
    .nav{padding:6px 8px 16px}
    .nav a{display:block; padding:10px 12px; margin:2px 0; border-radius:8px; color:var(--ink);
      text-decoration:none; border:1px solid transparent; font-weight:500}
    .nav a:hover{background:#eef2f7}
    .nav a.active{background:#edf2ff; border-color:#dbeafe; color:var(--accent)}

    /* Mini mode */
    body.sidebar-mini .sidebar{width:var(--sidebar-mini-w)}
    body.sidebar-mini .nav a{padding:10px 8px; text-align:center}
    body.sidebar-mini .sb-sec{display:none}
    body.sidebar-mini .nav a span{display:none}

    /* ===== Main ===== */
    .main{margin:calc(var(--header-h)) 0 0 var(--sidebar-w); padding:16px; min-height:100vh}
    body.sidebar-mini .main{margin-left:var(--sidebar-mini-w)}

    @media (max-width:1024px){
      .search{min-width:220px}
      .sidebar{width:var(--sidebar-mini-w)}
      .main{margin-left:var(--sidebar-mini-w)}
      .sb-sec{display:none}
      .nav a{padding:10px 8px}
    }
  </style>
</head>
<body>

  <!-- Header -->
  <header class="header">
    <div class="h-left">
      <button class="btn-icon" type="button" id="btnSidebar" title="Thu gọn/mở rộng" data-toggle-sidebar>≡</button>
      <div class="brand">
        <div class="mark"></div>
        <b>LeaveMgmt Admin</b>
      </div>
    </div>
    <div class="h-right">
      <div class="search"><input id="topSearch" type="search" placeholder="Tìm nhanh (Ctrl+/)"></div>
      <span class="who">${sessionScope.currentUser != null ? sessionScope.currentUser.fullName : 'Guest'}</span>
      <a class="btn-icon" href="${pageContext.request.contextPath}/logout" title="Đăng xuất">↗</a>
    </div>
  </header>

  <!-- Sidebar (text only) -->
  <aside class="sidebar">
    <div class="sb-sec">Tổng quan</div>
    <nav class="nav">
      <a href="${pageContext.request.contextPath}/admin/div" class="${param.active eq 'div' ? 'active' : ''}"><span>Division Dashboard</span></a>

      <div class="sb-sec">Đơn nghỉ phép</div>
      <a href="${pageContext.request.contextPath}/request/list" class="${param.active eq 'req_list' ? 'active' : ''}"><span>Danh sách</span></a>
      <a href="${pageContext.request.contextPath}/request/approvals" class="${param.active eq 'req_approvals' ? 'active' : ''}"><span>Phê duyệt</span></a>

      <div class="sb-sec">Báo cáo</div>
      <a href="${pageContext.request.contextPath}/reports/daily" class="${param.active eq 'r_daily' ? 'active' : ''}"><span>Theo ngày</span></a>
      <a href="${pageContext.request.contextPath}/reports/home" class="${param.active eq 'r_month' ? 'active' : ''}"><span>Theo tháng</span></a>

      <div class="sb-sec">Hệ thống</div>
      <a href="${pageContext.request.contextPath}/admin/users" class="${param.active eq 'users' ? 'active' : ''}"><span>Người dùng</span></a>
      <a href="${pageContext.request.contextPath}/settings" class="${param.active eq 'settings' ? 'active' : ''}"><span>Cấu hình</span></a>
    </nav>
  </aside>

  <!-- Main -->
  <main class="main">
    <jsp:include page="${param.content}"/>
  </main>

  <jsp:include page="/WEB-INF/views/common/_admin_footer.jsp"/>

  <script>
    // Giữ trạng thái sidebar
    (function(){
      try{
        if(localStorage.getItem('sidebar-mini')==='true'){ document.body.classList.add('sidebar-mini'); }
        document.querySelectorAll('[data-toggle-sidebar]').forEach(function(b){
          b.addEventListener('click', function(){
            document.body.classList.toggle('sidebar-mini');
            localStorage.setItem('sidebar-mini', document.body.classList.contains('sidebar-mini'));
          });
        });
      }catch(e){}
    })();

    // Phím tắt tìm kiếm
    document.addEventListener('keydown', function(e){
      if((e.ctrlKey||e.metaKey) && e.key === '/'){
        e.preventDefault(); document.getElementById('topSearch')?.focus();
      }
    });

    // Đánh dấu active theo URL (fallback)
    (function(){
      var cur = location.pathname.replace(/\/+$/,'');
      document.querySelectorAll('.nav a[href]').forEach(function(a){
        var href = a.getAttribute('href').replace(/\/+$/,'');
        if(href && cur === href) a.classList.add('active');
      });
    })();
  </script>
</body>
</html>
