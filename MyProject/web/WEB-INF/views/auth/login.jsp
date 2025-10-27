<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@include file="/WEB-INF/views/common/_taglibs.jsp"%>
<!DOCTYPE html>
<html lang="vi" data-theme="${sessionScope.theme != null ? sessionScope.theme : 'auto'}">
<head>
  <meta charset="UTF-8">
  <title>Đăng nhập · LeaveMgmt</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <meta name="color-scheme" content="light dark">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/login.css?v=1">
</head>
<body>
  <!-- Hiệu ứng icon HR bay -->
  <div class="floaters" aria-hidden="true">
    <span>👥</span><span>📝</span><span>✅</span><span>⏱️</span><span>🗂️</span>
    <span>👥</span><span>📝</span><span>✅</span><span>⏱️</span><span>🗂️</span>
  </div>

  <div class="wrap" role="main">
    <div class="brand" aria-hidden="true">
      <div class="logo"></div>
      <div class="title">LeaveMgmt</div>
    </div>

    <div class="actions">
      <!-- server-side theme switch (giữ nguyên endpoint /theme của bạn) -->
      <form method="post" action="${pageContext.request.contextPath}/theme">
        <input type="hidden" name="redirect" value="${pageContext.request.requestURI}" />
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

      <form id="loginForm" method="post" action="${pageContext.request.contextPath}/login" novalidate>
        <!-- CSRF -->
        <input type="hidden" name="_csrf" value="${sessionScope._csrf}" />

        <div class="input-wrap">
          <label for="username">Tên đăng nhập</label>
          <input id="username" name="username" class="input"
                 value="${fn:escapeXml(param.username)}"
                 autocomplete="username" required aria-required="true"
                 inputmode="text" spellcheck="false" />
        </div>

        <div class="input-wrap pw">
          <label for="password">Mật khẩu</label>
          <input id="password" name="password" class="input" type="password"
                 autocomplete="current-password" required aria-required="true" />
          <button type="button" class="toggle" id="btnTogglePw" aria-label="Hiện/ẩn mật khẩu">👁️</button>
        </div>

        <div class="row">
          <label class="check">
            <input type="checkbox" name="remember" <c:if test="${param.remember == 'on'}">checked</c:if> />
            Nhớ tôi
          </label>
          <a class="link" href="${pageContext.request.contextPath}/forgot">Quên mật khẩu?</a>
        </div>

        <button class="btn" id="btnSubmit" type="submit">Đăng nhập</button>

        <p class="help" id="formHelp" aria-live="polite"></p>

        <div class="meta">
          <span>© <fmt:formatDate value="${now}" pattern="yyyy"/></span>
          <span>·</span>
          <a class="link" href="${pageContext.request.contextPath}/">Trang chủ</a>
        </div>
      </form>
    </div>

  </div>

  <script>
    (function(){
      const form = document.getElementById('loginForm');
      const btn  = document.getElementById('btnSubmit');
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
        user.setAttribute('aria-invalid','false');
        pass.setAttribute('aria-invalid','false');
        help.textContent = '';

        if (!user.value.trim()){
          ok = false;
          user.setAttribute('aria-invalid','true');
          help.textContent = 'Vui lòng nhập tên đăng nhập.';
          user.focus();
        } else if (!pass.value){
          ok = false;
          pass.setAttribute('aria-invalid','true');
          help.textContent = 'Vui lòng nhập mật khẩu.';
          pass.focus();
        }

        if (!ok){
          e.preventDefault();
          return;
        }

        btn.disabled = true;
        btn.textContent = 'Đang đăng nhập...';
      });

      if (!user.value) user.focus();

      // Khôi phục nút nếu back/forward cache
      window.addEventListener('pageshow', function(event){
        if (event.persisted) {
          btn.disabled = false;
          btn.textContent = 'Đăng nhập';
        }
      });
    })();
  </script>
</body>
</html>
