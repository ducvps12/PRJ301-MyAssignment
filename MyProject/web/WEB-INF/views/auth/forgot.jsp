<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp" %>
<c:set var="cp" value="${pageContext.request.contextPath}"/>
<c:set var="csrfParam" value="${requestScope.csrfParam}"/>
<c:set var="csrfToken" value="${requestScope.csrfToken}"/>

<div class="container forgot-page">
  <h1>Quên mật khẩu</h1>

  <div aria-live="polite" aria-atomic="true">
    <c:if test="${not empty requestScope.error}">
      <div class="alert alert-danger">${requestScope.error}</div>
    </c:if>
    <c:if test="${not empty requestScope.message}">
      <div class="alert alert-success">${requestScope.message}</div>
    </c:if>
  </div>

  <!-- Step 1: Nhập email để nhận mã -->
  <form id="reqForm" action="${cp}/forgot" method="post" class="card" style="margin-bottom:16px" autocomplete="on">
    <input type="hidden" name="${csrfParam}" value="${csrfToken}"/>
    <!-- honeypot (anti-bot) -->
    <input type="text" name="website" autocomplete="off" tabindex="-1" style="position:absolute;left:-9999px" aria-hidden="true"/>

    <label for="email1">Email công ty</label>
    <input id="email1" name="email" type="email" class="input" placeholder="you@company.com"
           value="${fn:escapeXml(param.email)}" required autocomplete="email"/>

    <div class="row">
      <button id="btnReq" class="btn btn-primary" type="submit">Gửi mã xác minh</button>
      <small class="muted" style="display:block;margin-top:6px">
        * Chúng tôi luôn hiển thị thông báo chung để bảo vệ quyền riêng tư người dùng.
      </small>
    </div>
  </form>

  <!-- Step 2: Nhập OTP để cấp mật khẩu mới -->
  <form id="otpForm" action="${cp}/forgot/verify" method="post" class="card" autocomplete="off">
    <input type="hidden" name="${csrfParam}" value="${csrfToken}"/>

    <label for="email2">Email</label>
    <input id="email2" name="email" type="email" class="input" placeholder="you@company.com"
           value="${fn:escapeXml(param.email)}" required autocomplete="email"/>

    <label for="otp">Mã xác minh (6 số)</label>
    <input id="otp" name="otp" type="text" inputmode="numeric" pattern="\\d{6}" maxlength="6"
           class="input" autocomplete="one-time-code" required />

    <div class="row" style="display:flex;gap:8px;align-items:center;margin-top:8px">
      <button id="btnOtp" class="btn btn-success" type="submit">Xác minh &amp; cấp mật khẩu mới</button>
      <button id="btnResend" class="btn btn-light" type="button" title="Gửi lại mã">Gửi lại mã</button>
      <small id="cooldownHint" class="muted" style="margin-left:auto"></small>
    </div>
  </form>

  <p class="muted" style="margin-top:10px">
    * Sau khi nhận mật khẩu mới, vui lòng đăng nhập và đổi ngay tại <b>Tài khoản → Đổi mật khẩu</b>.
  </p>
</div>

<style>
  .forgot-page .card{padding:16px;border:1px solid #e5e7eb;border-radius:10px;background:#fff}
  .forgot-page .input{width:100%;padding:10px 12px;border:1px solid #d1d5db;border-radius:8px;margin:6px 0 12px}
  .forgot-page .btn{padding:10px 14px;border-radius:10px;border:1px solid transparent;cursor:pointer}
  .forgot-page .btn-primary{background:#2563eb;color:#fff}
  .forgot-page .btn-success{background:#16a34a;color:#fff}
  .forgot-page .btn-light{background:#f3f4f6;color:#111827;border-color:#e5e7eb}
  .forgot-page .alert{padding:10px 12px;border-radius:8px;margin-bottom:12px}
  .forgot-page .alert-success{background:#ecfdf5;color:#065f46;border:1px solid #a7f3d0}
  .forgot-page .alert-danger{background:#fef2f2;color:#991b1b;border:1px solid #fecaca}
  .forgot-page .muted{color:#6b7280;font-size:12px}
  .forgot-page .row{display:flex;gap:8px;align-items:center;flex-wrap:wrap}
</style>

<script>
(function(){
  const q = s => document.querySelector(s);

  // Autofocus: nếu có otpFocus=1 sau PRG thì focus OTP, không thì ưu tiên email Step1
  const url = new URL(window.location.href);
  if (url.searchParams.get('otpFocus') === '1') {
    q('#otp')?.focus();
  } else {
    (q('#email1')?.value ? q('#email2') : q('#email1'))?.focus();
  }

  // Khóa nút khi submit để chống double-submit
  function lock(btn, txt='Đang xử lý...'){
    if (!btn) return;
    btn.disabled = true;
    btn.dataset._text = btn.innerHTML;
    btn.innerHTML = txt;
    setTimeout(()=>{ btn.disabled=false; btn.innerHTML = btn.dataset._text; }, 8000);
  }
  q('#reqForm')?.addEventListener('submit', () => lock(q('#btnReq')));
  q('#otpForm')?.addEventListener('submit', () => lock(q('#btnOtp')));

  // Chỉ cho phép số trong OTP
  q('#otp')?.addEventListener('input', function(){
    this.value = this.value.replace(/\D+/g,'').slice(0,6);
  });

  // Cooldown cho Gửi lại mã (60s)
  const COOLDOWN_KEY = 'fg_cd';
  const COOLDOWN_MS  = 60000;
  const btnResend   = q('#btnResend');
  const cdHint      = q('#cooldownHint');

  function now(){ return Date.now(); }
  function setCd(){ localStorage.setItem(COOLDOWN_KEY, String(now()+COOLDOWN_MS)); }
  function getCd(){ const v = parseInt(localStorage.getItem(COOLDOWN_KEY)||'0',10); return isNaN(v)?0:v; }

  function tick(){
    const t = getCd();
    const remain = t - now();
    if (remain > 0){
      btnResend.disabled = true;
      cdHint.textContent = 'Chờ ' + Math.ceil(remain/1000) + 's để gửi lại';
      requestAnimationFrame(tick);
    } else {
      btnResend.disabled = false;
      cdHint.textContent = '';
    }
  }
  tick();

  // Gửi lại mã: copy email step 2 sang step 1 nếu trống, set cooldown rồi submit step 1
  btnResend?.addEventListener('click', function(){
    if (btnResend.disabled) return;
    const e1 = q('#email1'), e2 = q('#email2');
    if (e1 && e2 && (!e1.value || e1.value.trim()==='')) e1.value = e2.value;
    setCd(); tick();
    q('#reqForm')?.submit();
  });

})();
</script>
