<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@include file="/WEB-INF/views/common/_taglibs.jsp"%>
<jsp:useBean id="now" class="java.util.Date" />
<!DOCTYPE html>
<html lang="vi" data-theme="${sessionScope.theme != null ? sessionScope.theme : 'auto'}">
<head>
  <meta charset="UTF-8">
  <title>Đăng nhập · LeaveMgmt</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <meta name="color-scheme" content="light dark">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/login.css?v=2">
</head>
<body>
  <!-- Hiệu ứng icon HR bay -->
  <div class="floaters" aria-hidden="true">
    <span>👥</span><span>📝</span><span>✅</span><span>⏱️</span><span>🗂️</span>
    <span>👥</span><span>📝</span><span>✅</span><span>⏱️</span><span>🗂️</span>
  </div>

  <!-- GRID 2 CỘT: brand (trái) + cột form (phải) -->
  <div class="wrap" role="main">
    <!-- Cột trái: brand/hero -->
    <div class="brand" aria-hidden="true">
      <div class="logo">
        <img src="https://i.imgur.com/tumQO30.png" alt="LeaveMgmt">
      </div>
      <div class="title">LeaveMgmt</div>
    </div>

    <!-- Cột phải: actions + card -->
    <div class="right-col">
      <div class="actions">
        <!-- server-side theme switch (endpoint /theme giữ nguyên) -->
        <form method="post" action="${pageContext.request.contextPath}/theme">
          <input type="hidden" name="redirect" value="${pageContext.request.requestURI}">
          <button type="submit" class="tiny" title="Đổi theme (sáng/tối)">🌓 Đổi theme</button>
        </form>
      </div>

      <div class="card" aria-labelledby="loginTitle">
        <h1 id="loginTitle">Đăng nhập</h1>
        <p class="sub">Đăng nhập để sử dụng hệ thống quản lý đơn nghỉ phép.</p>

        <!-- Thông báo lỗi -->
        <c:if test="${not empty error}">
          <div class="alert" role="alert">
            <span>⚠️</span>
            <div>
              <strong>Lỗi:</strong>
              <div>${fn:escapeXml(error)}</div>
            </div>
          </div>
        </c:if>

        <!-- FORM: login nội bộ -->
        <form id="loginForm" method="post" action="${pageContext.request.contextPath}/login" novalidate>
          <!-- CSRF -->
          <input type="hidden" name="_csrf" value="${sessionScope._csrf}">

          <div class="input-wrap">
            <label for="username">Tên đăng nhập</label>
            <input id="username" name="username" class="input" type="text"
                   value="${fn:escapeXml(param.username)}"
                   autocomplete="username" required aria-required="true"
                   aria-describedby="formHelp"
                   inputmode="text" spellcheck="false" />
          </div>

          <div class="input-wrap pw">
            <label for="password">Mật khẩu</label>
            <input id="password" name="password" class="input" type="password"
                   autocomplete="current-password" required aria-required="true"
                   aria-describedby="formHelp" />
          </div>

          <div class="row">
            <label class="check">
              <input type="checkbox" name="remember" <c:if test="${param.remember == 'on'}">checked</c:if> />
              Nhớ tôi
            </label>
            <a class="link" href="${pageContext.request.contextPath}/forgot">Quên mật khẩu?</a>
          </div>

          <button class="btn" id="btnSubmit" type="submit">Đăng nhập</button>

          <!-- Divider -->
          <div class="or" role="separator" aria-label="hoặc">
            <span></span> <em>hoặc</em> <span></span>
          </div>

          <!-- Đăng nhập bằng Google (OAuth2/OpenID Connect) -->
          <a class="btn google" href="${pageContext.request.contextPath}/oauth/google/start" aria-label="Đăng nhập bằng Google">
            <img src="https://www.gstatic.com/firebasejs/ui/2.0.0/images/auth/google.svg" alt="" width="18" height="18" style="vertical-align:middle;margin-right:8px;">
            Đăng nhập bằng Google
          </a>

          <p class="help" id="formHelp" aria-live="polite"></p>

          <div class="meta">
            <span>© <fmt:formatDate value="${now}" pattern="yyyy"/></span>
            <span>·</span>
            <a class="link" href="${pageContext.request.contextPath}/">Trang chủ</a>
          </div>
        </form>
      </div>
    </div><!-- /right-col -->
  </div><!-- /wrap -->

  <!-- Khối CSS nhỏ gọn cho divider + nút Google (nếu bạn chưa thêm trong login.css) -->
  <style>
    .or{display:flex;align-items:center;gap:8px;margin:16px 0;color:var(--m,#6b7280)}
    .or span{flex:1;height:1px;background:var(--b,#e5e7eb)}
    .or em{font-style:normal;font-size:.95rem}
    .btn.google{display:inline-flex;align-items:center;justify-content:center;width:100%;
      border:1px solid #dadce0;border-radius:8px;padding:10px 14px;background:#fff;color:#202124;
      font-weight:600;text-decoration:none}
    .btn.google:hover{background:#f8f9fa}
  </style>

  <script>
    (function () {
      const form = document.getElementById('loginForm');
      const btn = document.getElementById('btnSubmit');
      const help = document.getElementById('formHelp');
      const user = document.getElementById('username');
      const pass = document.getElementById('password');
      const btnToggle = document.getElementById('btnTogglePw');

      // Toggle show/hide password
      btnToggle.addEventListener('click', () => {
        const isPw = pass.getAttribute('type') === 'password';
        pass.setAttribute('type', isPw ? 'text' : 'password');
        btnToggle.textContent = isPw ? '🙈' : '👁️';
      });

      // Client validation + chặn spam click
      form.addEventListener('submit', (e) => {
        let ok = true;
        user.setAttribute('aria-invalid', 'false');
        pass.setAttribute('aria-invalid', 'false');
        help.textContent = '';

        if (!user.value.trim()) {
          ok = false;
          user.setAttribute('aria-invalid', 'true');
          help.textContent = 'Vui lòng nhập tên đăng nhập.';
          user.focus();
        } else if (!pass.value) {
          ok = false;
          pass.setAttribute('aria-invalid', 'true');
          help.textContent = 'Vui lòng nhập mật khẩu.';
          pass.focus();
        }

        if (!ok) {
          e.preventDefault();
          return;
        }

        btn.disabled = true;
        btn.textContent = 'Đang đăng nhập...';
      });

      if (!user.value) user.focus();

      // Khôi phục nút nếu back/forward cache
      window.addEventListener('pageshow', function (event) {
        if (event.persisted) {
          btn.disabled = false;
          btn.textContent = 'Đăng nhập';
        }
      });
    })();
  </script>
</body>
</html>
