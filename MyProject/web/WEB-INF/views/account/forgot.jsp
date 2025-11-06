<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%
  com.acme.leavemgmt.util.Csrf.addToken(request);
  String cpath = request.getContextPath();
%>
<!doctype html>
<html lang="vi" data-theme="${sessionScope.theme != null ? sessionScope.theme : 'auto'}">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <meta name="color-scheme" content="light dark">
  <title>Quên mật khẩu · LeaveMgmt</title>

  <!-- Giữ CSS cũ (nếu có) -->
  <link rel="stylesheet" href="<%=cpath%>/assets/css/login.css?v=3">

  <style>
    /* ========== v2 Design tokens ========== */
    :root{
      --bg: #f6f7fb; --card:#fff; --text:#0f172a; --muted:#6b7280;
      --pri:#2563eb; --pri-600:#1d4ed8; --pri-700:#1e40af;
      --ok:#16a34a; --warn:#d97706; --err:#dc2626; --info:#0ea5e9;
      --border:#e5e7eb; --radius:16px;
      --shadow:0 10px 28px rgba(2,6,23,.08), 0 2px 10px rgba(2,6,23,.05);
      --ring: rgba(37,99,235,.38);
    }
    @media (prefers-color-scheme: dark){
      :root{
        --bg:#0b1220; --card:#0f172a; --text:#e5e7eb; --muted:#94a3b8;
        --border:#1f2737; --shadow:0 10px 30px rgba(0,0,0,.35);
        --ring:rgba(37,99,235,.55);
      }
    }
    html,body{height:100%}
    body{
      margin:0; color:var(--text);
      background:
        radial-gradient(900px 500px at -10% -10%, rgba(37,99,235,.08), transparent 60%),
        radial-gradient(700px 350px at 110% 0%, rgba(99,102,241,.08), transparent 60%),
        var(--bg);
      font: 15.8px/1.55 system-ui,-apple-system,Segoe UI,Roboto,Helvetica,Arial,"Apple Color Emoji","Segoe UI Emoji";
    }

    /* Layout */
    .page{display:grid; grid-template-columns: 280px 1fr; gap:24px; padding:24px; min-height:100%}
    .sidebar, .header, .card{
      background:var(--card); border:1px solid var(--border); border-radius:var(--radius); box-shadow:var(--shadow);
    }
    .sidebar{position:sticky; top:24px; height:calc(100vh - 48px); overflow:auto}
    .main{display:flex; flex-direction:column}
    .header{display:flex; align-items:center; justify-content:space-between; padding:12px 16px; margin-bottom:24px}
    .hdr-left{display:flex; gap:10px; align-items:center}
    .hdr-right{display:flex; gap:8px; align-items:center}

    /* Sidebar */
    .sb-brand{display:flex; gap:12px; align-items:center; padding:16px 16px 8px}
    .logo{width:42px; height:42px; border-radius:12px; display:grid; place-items:center;
      background:linear-gradient(135deg,#60a5fa, #6366f1); color:#fff; font-weight:800}
    .sb-sec{padding:8px 12px}
    .sb-sec h6{margin:8px 8px 6px; font-size:12px; color:var(--muted); text-transform:uppercase; letter-spacing:.06em}
    .sb-nav{list-style:none; margin:0; padding:0}
    .sb-nav a{display:flex; gap:10px; align-items:center; padding:10px 12px; color:var(--text); text-decoration:none; border-radius:10px}
    .sb-nav a:hover{background:color-mix(in oklab, var(--pri) 12%, transparent)}
    .sb-tip{margin:12px; padding:12px; border-radius:12px; border:1px dashed color-mix(in oklab, var(--pri) 35%, transparent);
      background:color-mix(in oklab, var(--pri) 6%, var(--card)); color:var(--muted); font-size:13px}

    /* Common UI */
    .btn{appearance:none; border:none; border-radius:12px; padding:11px 14px; font-weight:700; cursor:pointer}
    .btn-pri{background:var(--pri); color:#fff; box-shadow:0 8px 18px rgba(37,99,235,.25)}
    .btn-pri:hover{background:var(--pri-600)}
    .btn-pri:active{background:var(--pri-700)}
    .btn-ghost{background:var(--card); border:1px solid var(--border); color:var(--text)}
    .chip{padding:6px 10px; border:1px solid var(--border); border-radius:999px; color:var(--muted); background:var(--card)}
    .icon-btn{width:38px; height:38px; border:1px solid var(--border); background:var(--card); border-radius:10px; display:grid; place-items:center; cursor:pointer}
    .muted{color:var(--muted)}
    .link{color:var(--pri); text-decoration:none}
    .link:hover{text-decoration:underline}
    .divider{display:flex; gap:12px; align-items:center; color:var(--muted); margin:16px 0}
    .divider:before,.divider:after{content:""; flex:1; height:1px; background:var(--border)}
    .kbd{font:600 12px/1.2 ui-monospace, SFMono-Regular, Menlo, Consolas, "Liberation Mono", monospace; padding:.2em .45em; border:1px solid var(--border); border-radius:6px; background:var(--card)}

    /* Card form */
    .card{padding:22px; max-width:680px}
    .card h2{margin:6px 0 8px}
    .lead{margin-top:0; color:var(--muted)}
    .form-row{display:flex; flex-direction:column; gap:6px; margin-top:12px}
    label{font-weight:700}
    input[type="email"]{
      padding:12px; border-radius:12px; border:1px solid var(--border); background:var(--card); color:var(--text);
      outline:none; transition:border-color .15s, box-shadow .15s;
    }
    input[type="email"]:focus{border-color:var(--pri); box-shadow:0 0 0 4px var(--ring)}
    .hint{font-size:12.5px; color:var(--muted)}
    .suggest{margin-top:6px; font-size:13.5px}
    .suggest a{cursor:pointer}

    /* Stepper + breadcrumb */
    .crumbs{display:flex; gap:6px; font-size:13px; color:var(--muted); margin-bottom:6px}
    .crumbs a{color:inherit; text-decoration:none}
    .stepper{display:flex; gap:10px; margin:10px 0 16px}
    .step{display:flex; gap:8px; align-items:center}
    .step .dot{width:8px; height:8px; border-radius:50%; background:var(--border)}
    .step.active .dot{background:var(--pri)}
    .step.done .dot{background:var(--ok)}

    /* Toasts */
    .toasts{position:fixed; right:16px; bottom:18px; display:flex; flex-direction:column; gap:10px; z-index:60}
    .toast{background:var(--card); border:1px solid var(--border); border-radius:12px; box-shadow:var(--shadow); padding:12px 14px; min-width:280px}
    .toast.ok{border-color:color-mix(in oklab, var(--ok) 45%, var(--border))}
    .toast.err{border-color:color-mix(in oklab, var(--err) 45%, var(--border))}
    .toast.warn{border-color:color-mix(in oklab, var(--warn) 45%, var(--border))}
    .t-title{font-weight:800}
    .t-msg{color:var(--muted); font-size:14px}

    /* Footer */
    .footer{margin-top:18px; color:var(--muted)}

    /* Confetti canvas */
    #confetti{position:fixed; inset:0; pointer-events:none; z-index:55}

    /* Responsive */
    @media (max-width: 980px){
      .page{grid-template-columns:1fr; gap:14px; padding:14px}
      .sidebar{position:relative; height:auto}
      .hdr-right .chip{display:none}
    }
  </style>
</head>
<body>
  <canvas id="confetti"></canvas>

  <div class="page">
    <!-- ===== Sidebar ===== -->
    <aside class="sidebar" aria-label="Điều hướng công khai">
      <div class="sb-brand">
        <div class="logo">LM</div>
        <div>
          <div style="font-weight:900">LeaveMgmt</div>
          <div class="muted" style="font-size:12px">Hệ thống đơn nghỉ phép</div>
        </div>
      </div>

      <div class="sb-sec">
        <h6>Hỗ trợ nhanh</h6>
        <ul class="sb-nav">
          <li><a href="<%=cpath%>/login">🔐 Đăng nhập</a></li>
          <li><a id="supportMail">🛟 Liên hệ hỗ trợ</a></li>
          <li><a id="openShortcuts">⌨️ Phím tắt</a></li>
          <li><a id="toggleTheme">🌓 Chuyển giao diện</a></li>
        </ul>
        <div class="sb-tip">
          Mẹo: nhấn <span class="kbd">Alt</span> + <span class="kbd">K</span> để focus ô email. Khuyến nghị đổi mật khẩu ngay sau khi đăng nhập.
        </div>
      </div>

      <div class="sb-sec">
        <h6>Hỏi đáp</h6>
        <details><summary>Không nhận được email?</summary>
          <div class="muted">Kiểm tra Spam/Promotions. Thêm domain công ty vào whitelist. Nếu vẫn không được, báo IT.</div>
        </details>
        <details><summary>Gõ sai email?</summary>
          <div class="muted">Email phải trùng với hồ sơ nhân sự. Nếu đổi gần đây, nhờ HR cập nhật.</div>
        </details>
      </div>
    </aside>

    <!-- ===== Main ===== -->
    <div class="main">
      <header class="header">
        <div class="hdr-left">
          <span class="chip">Công khai</span>
          <nav class="crumbs">
            <a href="<%=cpath%>/">Trang chủ</a> › <a href="<%=cpath%>/login">Đăng nhập</a> › <span>Quên mật khẩu</span>
          </nav>
        </div>
        <div class="hdr-right">
          <span class="chip" id="netChip">Trạng thái: Online</span>
          <button class="icon-btn" id="btnLang" title="Ngôn ngữ">🌐</button>
          <a class="btn btn-pri" href="<%=cpath%>/login">Đăng nhập</a>
        </div>
      </header>

      <section class="card" aria-labelledby="ttl">
        <div class="stepper" aria-hidden="true">
          <div class="step active"><span class="dot"></span><span>Nhập email</span></div>
          <div class="step"><span class="dot"></span><span>Kiểm tra hộp thư</span></div>
          <div class="step"><span class="dot"></span><span>Đổi mật khẩu</span></div>
        </div>

        <h2 id="ttl">Quên mật khẩu</h2>
        <p class="lead">Nhập email đăng ký, chúng tôi sẽ gửi <strong>mật khẩu tạm thời</strong> tới email của bạn.</p>

        <!-- server messages (SSR) -->
        <c:if test="${not empty error}">
          <div class="toast err" role="alert">
            <div class="t-title">Lỗi</div><div class="t-msg">${error}</div>
          </div>
        </c:if>
        <c:if test="${not empty message}">
          <div class="toast ok" role="status">
            <div class="t-title">Thành công</div><div class="t-msg">${message}</div>
          </div>
        </c:if>

        <form id="forgotForm" method="post" action="<%=cpath%>/forgot" novalidate>
          <input type="hidden" name="csrf" value="${sessionScope.csrf}">
          <div class="form-row">
            <label for="email">Email công ty</label>
            <input id="email" name="email" type="email" autocomplete="email" inputmode="email"
                   placeholder="you@company.com" required aria-required="true">
            <div class="hint">Dùng email đã đăng ký trong hệ thống HR.</div>
            <div class="suggest" id="emailSuggest" hidden></div>
          </div>

          <div class="divider"><span> </span></div>

          <div class="row" style="display:flex; gap:10px; align-items:center">
            <button id="submitBtn" class="btn btn-pri" type="submit">Gửi mật khẩu mới</button>
            <button class="btn btn-ghost" type="button" id="btnClear">Xoá</button>
            <a class="link" href="<%=cpath%>/login">← Quay lại đăng nhập</a>
          </div>

          <div class="divider"><span>hoặc</span></div>

          <div class="row" style="display:flex; gap:10px; align-items:center">
            <button class="icon-btn" type="button" id="btnSupport" title="Email hỗ trợ">🛟</button>
            <span class="muted">Gặp vấn đề? Bấm để gửi email hỗ trợ.</span>
          </div>

          <p class="hint" style="margin-top:12px">
            * Bảo mật: sau khi nhận mật khẩu tạm, hãy đăng nhập và đổi mật khẩu ngay tại <em>Tài khoản → Đổi mật khẩu</em>.
          </p>
        </form>

        <footer class="footer">
          © <script>document.write(new Date().getFullYear())</script> LeaveMgmt · v2.0 ·
          <a class="link" href="<%=cpath%>/">Trang chủ</a> ·
          <a class="link" href="#">Chính sách quyền riêng tư</a>
        </footer>
      </section>
    </div>
  </div>

  <!-- Toast stack (CSR) -->
  <div class="toasts" id="toasts" aria-live="polite" aria-atomic="true"></div>

  <script>
    // ===== Helpers =====
    const $ = sel => document.querySelector(sel);
    const on = (el, ev, cb) => el && el.addEventListener(ev, cb);
    const toast = (kind, title, msg, t=4200) => {
      const host = $('#toasts'); if(!host) return;
      const el = document.createElement('div');
      el.className = 'toast ' + (kind||'');
      el.innerHTML = `<div class="t-title">${title||''}</div><div class="t-msg">${msg||''}</div>`;
      host.appendChild(el);
      setTimeout(()=>{ el.style.opacity='0'; el.style.transform='translateY(8px)'; }, t);
      setTimeout(()=> host.removeChild(el), t+350);
    };

    // ===== Theme toggle =====
    function toggleTheme(){
      const cur = document.documentElement.getAttribute('data-theme');
      const next = cur === 'dark' ? 'light' : 'dark';
      document.documentElement.setAttribute('data-theme', next);
      try{ localStorage.setItem('lgmt_theme', next);}catch{}
    }
    (function initTheme(){ try{ const t = localStorage.getItem('lgmt_theme'); if(t) document.documentElement.setAttribute('data-theme', t);}catch{} })();
    on($('#toggleTheme'), 'click', toggleTheme);

    // ===== Shortcuts =====
    on($('#openShortcuts'), 'click', ()=> toast('', 'Phím tắt', 'Alt+K: focus email · Enter: gửi form · Esc: xoá'));
    on(document, 'keydown', e=>{
      if(e.altKey && e.key.toLowerCase()==='k'){ e.preventDefault(); $('#email')?.focus(); }
      if(e.key==='Escape'){ $('#btnClear')?.click(); }
    });

    // ===== Online/Offline detector =====
    const netChip = $('#netChip');
    function setNet(){ if(!netChip) return;
      const o = navigator.onLine; netChip.textContent = 'Trạng thái: ' + (o?'Online':'Offline'); netChip.className='chip'; if(!o) netChip.style.borderColor='var(--warn)'; }
    on(window, 'online', setNet); on(window, 'offline', setNet); setNet();

    // ===== Support mail =====
    const supportAddr = 'support@example.com';
    on($('#supportMail'), 'click', ()=> window.location.href = 'mailto:'+supportAddr+'?subject=Quen%20mat%20khau%20-%20LeaveMgmt');
    on($('#btnSupport'), 'click', ()=> $('#supportMail').click());

    // ===== Email helpers: auto save & typo suggestion =====
    const emailInput = $('#email');
    (function restoreEmail(){ try{ const v = localStorage.getItem('lgmt_forgot_email'); if(v) emailInput.value=v; }catch{} })();
    on(emailInput, 'input', ()=>{ try{ localStorage.setItem('lgmt_forgot_email', emailInput.value.trim()); }catch{}; suggestEmail(); });

    const commonDomains = ['gmail.com','outlook.com','hotmail.com','yahoo.com','icloud.com','live.com','proton.me'];
    function suggestEmail(){
      const wrap = $('#emailSuggest'); if(!wrap) return;
      const v = emailInput.value.trim();
      const m = v.match(/@([^@]+)$/); if(!m){ wrap.hidden = true; return; }
      const typed = m[1].toLowerCase();
      const map = {'gmal.com':'gmail.com','gmaill.com':'gmail.com','gmial.com':'gmail.com','hotnail.com':'hotmail.com','outlok.com':'outlook.com','yahho.com':'yahoo.com'};
      let sug = map[typed] || '';
      if(!sug){
        let best = '', bestDist = 9;
        commonDomains.forEach(d=>{
          const dist = levenshtein(typed,d);
          if(dist < bestDist){ bestDist = dist; best = d; }
        });
        if(bestDist===1 || (bestDist===2 && typed.length>6)) sug = best;
      }
      if(sug){
wrap.innerHTML = 'Có phải bạn muốn dùng <a id="applySug"><strong>' + 
  v.replace(/@.*/, String.fromCharCode(64) + sug) + 
  '</strong></a>?';
        wrap.hidden = false;
        on($('#applySug'), 'click', ()=>{ emailInput.value = v.replace(/@.*/, '@'+sug); wrap.hidden = true; emailInput.focus(); });
      }else wrap.hidden = true;
    }
    function levenshtein(a,b){
      const dp=Array(a.length+1).fill(0).map((_,i)=>[i]);
      for(let j=0;j<=b.length;j++) dp[0][j]=j;
      for(let i=1;i<=a.length;i++) for(let j=1;j<=b.length;j++){
        dp[i][j]=Math.min(dp[i-1][j]+1, dp[i][j-1]+1, dp[i-1][j-1]+(a[i-1]==b[j-1]?0:1));
      }
      return dp[a.length][b.length];
    }

    // ===== Form UX: validate + cooldown + confetti on success =====
    const form = $('#forgotForm'), btn = $('#submitBtn');
    on($('#btnClear'),'click', ()=>{ emailInput.value=''; localStorage.removeItem('lgmt_forgot_email'); emailInput.focus(); toast('', 'Đã xoá', 'Bạn có thể nhập lại email.'); });

    on(form,'submit', (e)=>{
      const email = emailInput.value.trim();
      const ok = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(email);
      if(!ok){ e.preventDefault(); emailInput.focus(); emailInput.setAttribute('aria-invalid','true'); toast('err','Email không hợp lệ','Hãy nhập đúng định dạng.'); return; }

      // Cooldown 8s để chống bấm liên tục
      btn.disabled = true;
      const old = btn.textContent; let t=8; btn.textContent = `Đang gửi… (${t}s)`;
      const iv = setInterval(()=>{ t--; btn.textContent = `Đang gửi… (${t}s)`; if(t<=0){ clearInterval(iv); btn.disabled=false; btn.textContent=old; }},1000);
    });

    // Hiển thị server messages dạng toast + confetti
    (function enhanceServerMsg(){
      const okMsg = document.querySelector('.toast.ok .t-msg');
      const errMsg = document.querySelector('.toast.err .t-msg');
      if(okMsg){ toast('ok','Thành công', okMsg.textContent); confetti(); }
      if(errMsg){ toast('err','Lỗi', errMsg.textContent, 6000); }
    })();

    // ===== I18n stub =====
    on($('#btnLang'),'click', ()=>{
      toast('', 'Ngôn ngữ', 'Bản v2 hỗ trợ vi/en (stub). Tuỳ biến sau.');
    });

    // ===== Confetti (nhẹ, thuần JS) =====
    function confetti(){
      const cvs = $('#confetti'); if(!cvs) return;
      const ctx = cvs.getContext('2d'); let w=window.innerWidth,h=window.innerHeight; cvs.width=w; cvs.height=h;
      const N=120, P=[]; for(let i=0;i<N;i++){ P.push({x:Math.random()*w,y:Math.random()*-h,vx:(Math.random()-.5)*2,vy:2+Math.random()*2,s:4+Math.random()*4}); }
      let t=0, raf;
      function draw(){
        ctx.clearRect(0,0,w,h);
        P.forEach(p=>{ p.x+=p.vx; p.y+=p.vy; if(p.y>h+20){ p.y=-10; p.x=Math.random()*w; } ctx.fillStyle=`hsl(${(p.x+p.y+t)%360} 90% 60%)`; ctx.fillRect(p.x,p.y,p.s,p.s); });
        t+=2; raf=requestAnimationFrame(draw); if(t>500) cancelAnimationFrame(raf);
      } draw();
      setTimeout(()=> cancelAnimationFrame(raf), 4000);
    }

    // Autofocus
    emailInput?.focus();
  </script>
</body>
</html>
