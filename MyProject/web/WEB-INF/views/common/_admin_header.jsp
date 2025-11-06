<%-- _admin_header.jsp (FINAL, drop-in)
  - KHÔNG khai báo taglib ở partial này (layout đã include /common/_taglibs.jsp).
  - Sử dụng biến `cp` (contextPath) được c:set ở layout ngoài.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%-- Chuẩn hóa user (ưu tiên currentUser, fallback user) --%>
<c:set var="u"
       value="${not empty sessionScope.currentUser ? sessionScope.currentUser : sessionScope.user}" />
<c:set var="uName"
       value="${not empty u ? (u.full_name != null ? u.full_name : (u.fullName != null ? u.fullName : 'Guest')) : 'Guest'}" />
<c:set var="uEmail"
       value="${not empty u ? (u.email != null ? u.email : '') : ''}" />
<c:set var="uRole"
       value="${not empty u ? (u.role != null ? u.role : '') : ''}" />

<div class="main">
  <!-- ======= TOPBAR / HEADER ======= -->
  <style>
    /* Tokens kế thừa từ layout */
    :root{
      --bg:#f7f9fc; --card:#fff; --tx:#0f172a; --muted:#64748b; --bd:#e5e7eb; --pri:#2563eb;
    }
    @media (prefers-color-scheme: dark){
      :root{ --bg:#0b1220; --card:#0f172a; --tx:#e5e7eb; --muted:#94a3b8; --bd:#1f2937; }
    }

    .topbar{
      position: sticky; top: 0; z-index: 40;
      background: var(--card);
      border-bottom: 1px solid var(--bd);
      padding: 10px 14px;
      display:flex; align-items:center; gap:12px; justify-content:space-between; flex-wrap:wrap;
    }
    .h-left,.h-right{display:flex; align-items:center; gap:10px}
    .brand{font-weight:800}
    .brand a{color:var(--tx); text-decoration:none}
    .btn{
      border:1px solid var(--bd); background:var(--card); color:var(--tx);
      border-radius:10px; padding:6px 10px; cursor:pointer;
    }
    .btn:focus{outline:2px solid var(--pri); outline-offset:2px}

    .search{
      display:flex; align-items:center; gap:8px;
      border:1px solid var(--bd); background:var(--card); border-radius:10px;
      padding:6px 10px; min-width:280px
    }
    .search input{border:0; background:transparent; outline:none; width:100%}
    .kbd{border:1px solid var(--bd); padding:0 6px; border-radius:6px; font-size:12px; color:var(--muted)}

    .breadcrumbs{display:flex; align-items:center; gap:6px; color:var(--muted); font-size:13px}
    .breadcrumbs a{color:inherit; text-decoration:none}
    .breadcrumbs .now{color:var(--tx); font-weight:600}

    .dd{position:relative}
    .dd-menu{
      position:absolute; right:0; top:calc(100% + 8px); min-width:260px; background:var(--card);
      border:1px solid var(--bd); border-radius:12px; padding:8px;
      box-shadow:0 10px 30px rgba(0,0,0,.08); display:none; z-index:30
    }
    .dd.open .dd-menu{display:block}
    .dd-menu a{display:block; padding:8px 10px; border-radius:8px; color:var(--tx); text-decoration:none}
    .dd-menu a:hover{background:rgba(0,0,0,.03)}
    .u-chip{display:flex; align-items:center; gap:8px; border:1px solid var(--bd); border-radius:999px; background:var(--card); padding:6px 10px; cursor:pointer}
    .u-ava{width:26px; height:26px; border-radius:50%; background:#c7d2fe; display:inline-block}

    @media (max-width:980px){
      .search{display:none}
      .breadcrumbs{display:none}
    }
  </style>

  <div class="topbar" role="banner">
    <div class="h-left">
      <button class="btn" title="Mở/đóng menu" id="btnSidebar">☰</button>
      <div class="brand"><a href="${cp}/admin">LeaveMgmt Admin</a></div>

      <!-- Breadcrumbs: truyền List<String> vào request.setAttribute("bc", ...) -->
      <c:if test="${not empty bc}">
        <nav class="breadcrumbs" aria-label="Breadcrumb">
          <c:forEach var="b" items="${bc}" varStatus="s">
            <c:choose>
              <c:when test="${s.last}">
                <span class="now"><c:out value="${b}"/></span>
              </c:when>
              <c:otherwise>
                <a href="javascript:history.back()"><c:out value="${b}"/></a><span>/</span>
              </c:otherwise>
            </c:choose>
          </c:forEach>
        </nav>
      </c:if>
    </div>

    <div class="search" role="search">
      <input id="globalSearch" type="search" placeholder="Tìm người, đơn nghỉ, phòng ban…" aria-label="Tìm kiếm" />
      <span class="kbd" aria-hidden="true">/</span>
    </div>

    <div class="h-right">
      <button class="btn" title="Làm mới" id="btnReload">⟳</button>
      <button class="btn" title="In trang" id="btnPrint">🖨</button>
      <button class="btn" id="btnTheme" title="Dark / Light">🌓</button>

      <div class="dd" id="ddNotif">
        <button class="btn" type="button" aria-haspopup="true" aria-expanded="false" aria-controls="menuNotif">🔔</button>
        <div class="dd-menu" id="menuNotif" role="menu" aria-label="Thông báo">
          <div style="font-weight:700;padding:6px 6px 8px">Thông báo</div>
          <div style="height:1px;background:var(--bd);margin:6px 0"></div>

          <c:forEach var="n" items="${notifications}">
            <a role="menuitem" href="${cp}/admin/notifications#${n.id}">
              <b><c:out value="${n.title}"/></b><br>
              <small class="muted"><fmt:formatDate value="${n.createdAt}" pattern="dd/MM/yyyy HH:mm"/></small>
            </a>
          </c:forEach>
          <c:if test="${empty notifications}">
            <div class="muted" style="padding:6px 10px">Không có thông báo mới.</div>
          </c:if>

          <div style="height:1px;background:var(--bd);margin:6px 0"></div>
          <a role="menuitem" href="${cp}/admin/notifications">Xem tất cả →</a>
        </div>
      </div>

      <div class="dd" id="ddUser">
        <button class="u-chip" type="button" aria-haspopup="true" aria-expanded="false" aria-controls="menuUser">
          <span class="u-ava" aria-hidden="true"></span>
          <div style="line-height:1.1">
            <b><c:out value="${uName}"/></b>
            <div class="muted" style="font-size:12px"><c:out value="${uRole}"/></div>
          </div>
        </button>
        <div class="dd-menu" id="menuUser" style="min-width:220px" role="menu" aria-label="Tài khoản">
          <div style="padding:6px 8px">
            <b><c:out value="${uName}"/></b><br>
            <small class="muted"><c:out value="${uEmail}"/></small>
          </div>
          <div style="height:1px;background:var(--bd);margin:6px 0"></div>
          <a role="menuitem" href="${cp}/account/profile">Hồ sơ</a>
          <a role="menuitem" href="${cp}/account/security">Bảo mật</a>
          <div style="height:1px;background:var(--bd);margin:6px 0"></div>
          <a role="menuitem" href="${cp}/logout">Đăng xuất</a>
        </div>
      </div>
    </div>
  </div>
  <!-- ======= /TOPBAR ======= -->

  <script>
    (function(){
      // Fallback toggleSidebar nếu app chưa định nghĩa
      if (typeof window.toggleSidebar !== 'function') {
        window.toggleSidebar = function(){
          document.documentElement.classList.toggle('sidebar-collapsed');
        };
      }

      // Sidebar toggle
      var btnSidebar = document.getElementById('btnSidebar');
      if (btnSidebar) btnSidebar.addEventListener('click', toggleSidebar);

      // Actions
      var btnReload = document.getElementById('btnReload');
      if (btnReload) btnReload.addEventListener('click', function(){ location.reload(); });

      var btnPrint = document.getElementById('btnPrint');
      if (btnPrint) btnPrint.addEventListener('click', function(){ window.print(); });

      // Theme toggle (cycle: auto -> light -> dark)
      var html = document.documentElement;
      var btnTheme = document.getElementById('btnTheme');
      function applyTheme(v){
        // v: 'auto' | 'light' | 'dark'
        if (!v) v = 'auto';
        html.setAttribute('data-theme', v);
        try{ localStorage.setItem('theme', v); }catch(_){}
      }
      function nextTheme(curr){
        if (curr === 'auto') return 'light';
        if (curr === 'light') return 'dark';
        return 'auto';
      }
      // init theme
      try{
        var saved = localStorage.getItem('theme');
        if (saved) applyTheme(saved);
      }catch(_){}
      if (btnTheme){
        btnTheme.addEventListener('click', function(){
          var curr = html.getAttribute('data-theme') || 'auto';
          applyTheme(nextTheme(curr));
        });
      }

      // Global search: '/' để focus, Enter để submit đến /admin/search?q=
      var gSearch = document.getElementById('globalSearch');
      document.addEventListener('keydown', function(e){
        if (e.key === '/' && !/input|textarea|select/i.test(document.activeElement.tagName)) {
          e.preventDefault(); if (gSearch) gSearch.focus();
        }
        if (e.key === 'Escape') closeAllDropdowns();
      });
      if (gSearch){
        gSearch.addEventListener('keydown', function(e){
          if (e.key === 'Enter'){
            var q = (gSearch.value || '').trim();
            if (q) window.location.href = '${cp}/admin/search?q=' + encodeURIComponent(q);
          }
        });
      }

      // Dropdowns (Notif/User)
      function toggleDd(id){
        var dd = document.getElementById(id);
        if (!dd) return;
        dd.classList.toggle('open');
        var btn = dd.querySelector('button[aria-haspopup="true"]');
        if (btn) btn.setAttribute('aria-expanded', dd.classList.contains('open') ? 'true':'false');
      }
      function closeAllDropdowns(){
        document.querySelectorAll('.dd.open').forEach(function(el){
          el.classList.remove('open');
          var btn = el.querySelector('button[aria-haspopup="true"]');
          if (btn) btn.setAttribute('aria-expanded', 'false');
        });
      }
      ['ddNotif','ddUser'].forEach(function(id){
        var dd = document.getElementById(id); if (!dd) return;
        var btn = dd.querySelector('button[aria-haspopup="true"]');
        if (btn) btn.addEventListener('click', function(e){
          e.stopPropagation();
          // close others then open this
          document.querySelectorAll('.dd').forEach(function(x){ if (x !== dd) x.classList.remove('open'); });
          toggleDd(id);
        });
      });
      document.addEventListener('click', function(e){
        if (!e.target.closest('.dd')) closeAllDropdowns();
      }, {passive:true});
    })();
  </script>

  <!-- Content trang sẽ tiếp tục ở dưới -->
  <div class="content">
