<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<jsp:useBean id="now" class="java.util.Date" />

<footer class="footer-elite" id="appFooter">
  <div class="footer-top">
    <div class="container">
      <div class="col brand">
        <div class="logo">LM</div>
        <div class="info">
          <h3>LeaveMgmt System</h3>
          <p>Giải pháp quản lý nghỉ phép thông minh cho doanh nghiệp hiện đại. Dễ dùng, nhanh chóng, và tối ưu trải nghiệm quản lý nhân sự.</p>
          <div class="social">
            <a href="#" class="social-icon" title="Facebook"><i class="fa fa-facebook"></i></a>
            <a href="#" class="social-icon" title="Instagram"><i class="fa fa-instagram"></i></a>
            <a href="#" class="social-icon" title="YouTube"><i class="fa fa-youtube-play"></i></a>
            <a href="#" class="social-icon" title="GitHub"><i class="fa fa-github"></i></a>
            <a href="#" class="social-icon" title="LinkedIn"><i class="fa fa-linkedin"></i></a>
          </div>
        </div>
      </div>

      <div class="col links">
        <h4>Chức năng</h4>
        <ul>
          <li><a href="${pageContext.request.contextPath}/request/list">Danh sách đơn</a></li>
          <li><a href="${pageContext.request.contextPath}/request/agenda">Lịch làm việc</a></li>
          <li><a href="${pageContext.request.contextPath}/admin">Bảng điều khiển</a></li>
          <li><a href="${pageContext.request.contextPath}/admin/users">Quản lý nhân viên</a></li>
        </ul>
      </div>

      <div class="col support">
        <h4>Hỗ trợ</h4>
        <ul>
          <li><a href="${pageContext.request.contextPath}/support/guide">Tài liệu sử dụng</a></li>
        <li><a href="${pageContext.request.contextPath}/support/faq">Câu hỏi thường gặp</a></li>
    
        <li><a href="${pageContext.request.contextPath}/support/contact">Liên hệ quản trị</a></li>
      </ul>
      </div>

      <div class="col contact">
        <h4>Liên hệ</h4>
        <ul>
          <li><i class="fa fa-envelope"></i> support@leavemgmt.com</li>
          <li><i class="fa fa-phone"></i> 0353.519.845</li>
          <li><i class="fa fa-map-marker"></i> FPT University, Hòa Lạc, Hà Nội</li>
        </ul>
        <button id="scrollTopBtn" title="Lên đầu trang">⬆ Top</button>
      </div>
    </div>
  </div>

  <div class="footer-bottom">
    <div class="container">
      <div class="left">
        © <fmt:formatDate value="${now}" pattern="yyyy"/> <strong>LeaveMgmt</strong>. All Rights Reserved.
      </div>
      <div class="right">
        <a href="${pageContext.request.contextPath}/support/privacy">Chính sách bảo mật</a>
        <a href="${pageContext.request.contextPath}/support/terms">Điều khoản</a>
        <button id="themeToggle" title="Đổi theme">🌓</button>
      </div>
    </div>
  </div>
</footer>

<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">

<style>
/* ==== FOOTER ELITE STYLE ==== */
:root {
  --brand:#007bff;
  --accent:#00c2ff;
  --dark:#090a0f;
  --light:#fafafa;
  --muted:#9ca3af;
  --grad:linear-gradient(135deg,#007bff,#00c2ff,#1dd3b0);
}

.footer-elite{
  color:var(--light);
  background:var(--dark);
  font-family:'Segoe UI',Roboto,Arial,sans-serif;
  position:relative;
  overflow:hidden;
}
.footer-elite::before{
  content:"";
  position:absolute;
  inset:0;
  background:radial-gradient(circle at top right,rgba(0,194,255,0.15),transparent 70%);
  pointer-events:none;
}
.footer-elite .container{
  max-width:1200px;
  margin:auto;
  padding:40px 24px;
  display:grid;
  grid-template-columns:repeat(auto-fit,minmax(240px,1fr));
  gap:40px;
}
.footer-elite h3,.footer-elite h4{
  color:#fff;
  font-weight:600;
  margin-bottom:14px;
  position:relative;
}
.footer-elite h4::after{
  content:"";
  position:absolute;
  width:40px;height:2px;
  background:var(--accent);
  left:0;bottom:-6px;
  border-radius:2px;
}
.footer-elite p{color:var(--muted);font-size:14px;line-height:1.6;}
.footer-elite ul{list-style:none;padding:0;margin:0;}
.footer-elite ul li{margin-bottom:8px;color:var(--muted);font-size:14px;}
.footer-elite ul li a{
  color:var(--muted);
  text-decoration:none;
  transition:all .3s;
}
.footer-elite ul li a:hover{
  color:var(--accent);
  padding-left:4px;
}

/* brand block */
.footer-elite .logo{
  width:50px;height:50px;
  border-radius:12px;
  background:var(--grad);
  display:flex;align-items:center;justify-content:center;
  font-weight:800;font-size:18px;
  color:#fff;margin-bottom:10px;
  box-shadow:0 0 10px rgba(0,194,255,0.4);
}
.footer-elite .social{
  display:flex;gap:10px;margin-top:12px;
}
.footer-elite .social-icon{
  width:36px;height:36px;border-radius:50%;
  background:rgba(255,255,255,0.08);
  color:#fff;display:flex;align-items:center;justify-content:center;
  font-size:16px;transition:all .3s;
  border:1px solid rgba(255,255,255,0.1);
}
.footer-elite .social-icon:hover{
  background:var(--grad);
  transform:translateY(-3px);
  box-shadow:0 0 10px rgba(0,194,255,0.4);
}

/* Scroll top button */
#scrollTopBtn{
  background:var(--grad);
  color:#fff;
  border:none;
  border-radius:8px;
  padding:8px 14px;
  margin-top:12px;
  cursor:pointer;
  font-size:14px;
  transition:all .3s;
}
#scrollTopBtn:hover{opacity:.85;transform:translateY(-2px);}

/* bottom */
.footer-bottom{
  border-top:1px solid rgba(255,255,255,0.08);
  background:#0b0c11;
  padding:12px 0;
}
.footer-bottom .container{
  max-width:1200px;
  margin:auto;
  display:flex;
  align-items:center;
  justify-content:space-between;
  flex-wrap:wrap;
  padding:0 20px;
}
.footer-bottom .left{font-size:13px;color:var(--muted);}
.footer-bottom .right{display:flex;align-items:center;gap:16px;}
.footer-bottom .right a{
  color:var(--muted);text-decoration:none;font-size:13px;
  transition:color .3s;
}
.footer-bottom .right a:hover{color:var(--accent);}
#themeToggle{
  background:rgba(255,255,255,0.05);
  border:1px solid rgba(255,255,255,0.1);
  border-radius:8px;
  padding:4px 10px;
  color:#fff;
  cursor:pointer;
  font-size:16px;
  transition:all .3s;
}
#themeToggle:hover{background:var(--grad);}

/* glow effect hover */
.footer-elite a:hover, .footer-elite button:hover{
  text-shadow:0 0 8px rgba(0,194,255,0.6);
}

/* Animation subtle gradient line */
.footer-elite h4::after{
  animation:slideLine 2s infinite alternate;
}
@keyframes slideLine{
  from{width:20px;}
  to{width:60px;}
}

/* RESPONSIVE */
@media(max-width:600px){
  .footer-elite .container{text-align:center;}
  .footer-elite h4::after{left:50%;transform:translateX(-50%);}
  .footer-bottom .container{flex-direction:column;gap:8px;text-align:center;}
}
</style>

<script>
(function(){
  // Scroll to top
  const scrollBtn = document.getElementById("scrollTopBtn");
  scrollBtn.addEventListener("click",()=>window.scrollTo({top:0,behavior:"smooth"}));

  // Theme toggle with persistence
  const themeBtn = document.getElementById("themeToggle");
  themeBtn.addEventListener("click",()=>{
    const root = document.documentElement;
    const cur = root.getAttribute("data-theme") || "light";
    const next = cur==="light"?"dark":"light";
    root.setAttribute("data-theme",next);
    localStorage.setItem("theme",next);
  });
  try{
    const saved = localStorage.getItem("theme");
    if(saved){document.documentElement.setAttribute("data-theme",saved);}
  }catch(_){}

  // Subtle particle background animation
  const footer = document.getElementById("appFooter");
  const particleCount = 20;
  for(let i=0;i<particleCount;i++){
    const dot=document.createElement("span");
    dot.className="footer-particle";
    dot.style.left=Math.random()*100+"%";
    dot.style.top=Math.random()*100+"%";
    dot.style.animationDelay=(Math.random()*5)+"s";
    footer.appendChild(dot);
  }
})();
</script>

<style>
.footer-particle{
  position:absolute;
  width:6px;height:6px;
  background:var(--accent);
  border-radius:50%;
  opacity:0.15;
  animation:floatDot 6s linear infinite;
}
@keyframes floatDot{
  0%{transform:translateY(0);opacity:.2;}
  50%{transform:translateY(-20px);opacity:.6;}
  100%{transform:translateY(0);opacity:.2;}
}
</style>
