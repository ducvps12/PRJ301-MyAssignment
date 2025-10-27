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
        <title>LeaveMgmt · Quản lý nghỉ phép nhanh – gọn – đúng quy trình</title>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <meta name="color-scheme" content="light dark">
        <meta name="description" content="Hệ thống quản lý nghỉ phép, agenda phòng ban, phân quyền duyệt 2 bước. Trải nghiệm nhanh, trực quan, an toàn.">

        <!-- Bootstrap + Icons -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">

        <style>
            html, body {
  overflow-x: hidden !important; /* không cho xuất hiện thanh ngang */
}

#bg-anim {
  position: fixed;
  inset: 0;
  z-index: -1;
  pointer-events: none; /* không bắt sự kiện chuột */
  will-change: transform;
  contain: paint; /* chặn tràn */
}

            :root{
                --bg:#0f1b24;
                --card:#0f172a;
                --text:#e8edf3;
                --muted:#9ab0c2;
                --brand:#60a5fa;
                --brand-2:#c4b5fd;
                --accent:#67e8f9;
                --orange:#ff7a19;
                --orange-2:#ff9b2b;
                --ring:rgba(96,165,250,.35);
                --border:#1f2a44;
                --shadow:0 18px 42px rgba(0,0,0,.55);
                --radius:16px;
                --ease:cubic-bezier(.22,.61,.36,1);
            }
            [data-theme="dark"] {
                --bg:#0f1b24;
                --card:#0f172a;
                --text:#e8edf3;
                --muted:#9ab0c2;
                --brand:#60a5fa;
                --brand-2:#c4b5fd;
                --accent:#67e8f9;
                --orange:#ff7a19;
                --orange-2:#ff9b2b;
                --ring:rgba(96,165,250,.35);
                --border:#1f2a44;
                --shadow:0 18px 42px rgba(0,0,0,.55);
            }
            [data-theme="light"] {
                --bg:#f9fafb;
                --card:#ffffff;
                --text:#1f2937;
                --muted:#6b7280;
                --brand:#3b82f6;
                --brand-2:#a855f7;
                --accent:#06b6d4;
                --orange:#f97316;
                --orange-2:#fb923c;
                --ring:rgba(59,130,246,.35);
                --border:#e5e7eb;
                --shadow:0 10px 30px rgba(0,0,0,.1);
            }
            html,body{
                background:var(--bg);
                color:var(--text);
                scroll-behavior:smooth;
                font-family:system-ui,Segoe UI,Roboto,sans-serif;
                overflow-x:hidden;
            }
            a{
                color:var(--brand);
                text-decoration:none;
                transition:color .2s;
            }
            a:hover{
                color:var(--brand-2);
            }
            .brand-grad{
                background:linear-gradient(90deg,var(--brand),var(--brand-2) 60%,var(--accent));
                -webkit-background-clip:text;
                background-clip:text;
                color:transparent
            }
            .orange-grad{
                background:linear-gradient(90deg,var(--orange),var(--orange-2));
                -webkit-background-clip:text;
                background-clip:text;
                color:transparent
            }
            .text-grad{
                background:linear-gradient(90deg,var(--brand),var(--accent));
                -webkit-background-clip:text;
                background-clip:text;
                color:transparent;
            }

            .navbar{
                background:rgba(var(--card-rgb),.55);
                backdrop-filter:saturate(1.2) blur(10px);
                border-bottom:1px solid var(--border);
                transition:background .3s;
            }
            .ring{
                transition:box-shadow .15s ease, transform .05s
            }
            .ring:focus-visible{
                outline:0;
                box-shadow:0 0 0 .20rem var(--ring)
            }
            .btn:active{
                transform:translateY(1px)
            }
            .hover{
                transition:transform .18s ease,box-shadow .18s ease
            }
            .hover:hover{
                transform:translateY(-3px);
                box-shadow:var(--shadow)
            }

            /* ---------- Announcement ---------- */
            .announce{
                background:var(--card);
                color:var(--text);
                border-bottom:1px solid var(--border)
            }
            .announce .badge{
                background:var(--orange);
                color:var(--bg);
                font-weight:800
            }
            .ticker{
                position:relative;
                overflow:hidden;
                height:1.6rem
            }
            .ticker-inner{
                display:inline-block;
                padding-left:100%;
                white-space:nowrap;
                animation:marquee 18s linear infinite;
                opacity:.95
            }
            @keyframes marquee{
                from{
                    transform:translateX(0)
                }
                to{
                    transform:translateX(-100%)
                }
            }

            /* ---------- HERO ---------- */
            .hero{
                position:relative;
                overflow:hidden;
                background:var(--bg);
                padding:100px 0 80px;
                border-bottom:1px solid var(--border);
            }
            #heroCanvas{
                position:absolute;
                inset:0;
                z-index:0;
                opacity:.6;
                pointer-events:none;
            }
            .blob{
                position:absolute;
                width:600px;
                height:600px;
                border-radius:50%;
                filter:blur(100px);
                opacity:.15;
                transition:transform .3s ease;
            }
            .blob.b1{
                background:var(--brand);
                top:-200px;
                left:-200px;
            }
            .blob.b2{
                background:var(--accent);
                bottom:-200px;
                right:-200px;
            }
            .hero h1{
                font-size:clamp(36px,5.5vw,64px);
                font-weight:900;
                line-height:1.05;
                letter-spacing:-.5px;
                margin:0
            }
            .u-underline{
                position:relative;
                display:inline-block;
                padding-bottom:.12em
            }
            .u-underline::after{
                content:"";
                position:absolute;
                left:0;
                right:0;
                bottom:-.02em;
                height:.18em;
                background:linear-gradient(90deg,var(--orange),var(--orange-2));
                border-radius:999px;
                transform:scaleX(0);
                transform-origin:left center;
                animation:uline .9s var(--ease) .25s forwards;
            }
            @keyframes uline{
                to{
                    transform:scaleX(1)
                }
            }
            .lead.mini{
                color:var(--muted)
            }
            .alert{
                transition:all .3s;
            }

            /* Illustration + chips */
            .illus{
                position:relative;
                min-height:380px;
                display:grid;
                place-items:center
            }
            .ring-bg{
                width:clamp(220px,40vw,420px);
                height:clamp(220px,40vw,420px);
                border-radius:28px;
                background:radial-gradient(60% 60% at 50% 40%,rgba(255,255,255,.06),transparent 70%);
                box-shadow:inset 0 0 0 8px rgba(255,255,255,.06),0 24px 60px rgba(0,0,0,.35);
                display:grid;
                place-items:center;
                transition:transform .3s var(--ease)
            }
            .main-icon{
                width:min(60%,280px);
                aspect-ratio:1/1;
                border-radius:22%;
                background:linear-gradient(135deg,var(--brand-2) 0%, var(--brand) 100%);
                border:8px solid var(--card);
                box-shadow:0 18px 36px rgba(0,0,0,.55);
                display:grid;
                place-items:center;
                color:var(--text);
                font-size:clamp(58px,8vw,108px)
            }
            .hr-chip{
                position:absolute;
                display:flex;
                align-items:center;
                gap:.38rem;
                padding:.42rem .7rem;
                border-radius:999px;
                font-weight:700;
                font-size:.92rem;
                color:var(--bg);
                background:var(--card);
                border:1px solid var(--border);
                box-shadow:var(--shadow);
                animation:floatY 7s ease-in-out infinite
            }
            .hr-chip i{
                font-size:1rem
            }
            .chip-blue{
                background:linear-gradient(135deg,#d6e8ff,#eef6ff)
            }
            .chip-orange{
                background:linear-gradient(135deg,#ffd9b0,#fff2e6)
            }
            .chip-violet{
                background:linear-gradient(135deg,#eadcff,#f5eeff)
            }
            @keyframes floatY{
                0%,100%{
                    transform:translateY(0)
                }
                50%{
                    transform:translateY(-12px)
                }
            }
            .chip-1{
                top:10%;
                right:6%;
                animation-delay:0s
            }
            .chip-2{
                top:36%;
                right:2%;
                animation-delay:1.5s
            }
            .chip-3{
                bottom:14%;
                left:8%;
                animation-delay:3s
            }
            .chip-4{
                top:16%;
                left:4%;
                animation-delay:4.5s
            }

            /* Cards / quick / metrics */
            .card{
                background:var(--card);
                border:1px solid var(--border);
                box-shadow:var(--shadow);
                border-radius:var(--radius);
                transition:box-shadow .3s, transform .3s;
            }
            .quick{
                min-height:120px;
                display:flex;
                align-items:center;
                padding:18px 20px
            }
            .mini-muted{
                color:var(--muted)
            }
            .legend-dot{
                width:10px;
                height:10px;
                border-radius:999px;
                display:inline-block;
                margin-right:.4rem
            }
            .dot-green{
                background:#22c55e;
                animation:pulseG 1.6s ease-in-out infinite
            }
            .dot-red{
                background:#ef4444;
                animation:pulseR 1.6s ease-in-out infinite
            }
            @keyframes pulseG{
                0%,100%{
                    opacity:.6;
                    transform:scale(.9)
                }
                50%{
                    opacity:1;
                    transform:scale(1.3)
                }
            }
            @keyframes pulseR{
                0%,100%{
                    opacity:.7;
                    transform:scale(.9)
                }
                50%{
                    opacity:1;
                    transform:scale(1.25)
                }
            }
            .status-badge{
                padding:.25rem .55rem;
                border-radius:999px;
                border:1px solid var(--border);
                font-weight:600;
                display:inline-flex;
                align-items:center;
                gap:.35rem
            }

            .metric{
                padding:20px;
                border-radius:var(--radius);
                border:1px solid var(--border);
                background:var(--card)
            }
            .metric .num{
                font-size:clamp(28px,5vw,42px);
                font-weight:800;
                line-height:1
            }
            .metric .lbl{
                color:var(--muted);
                font-weight:600
            }

            /* HR feature icons row */
            .hr-icons .card{
                background:linear-gradient(180deg,rgba(255,255,255,.06),transparent);
                text-align:center;
                padding:18px 10px
            }
            .hr-icons i{
                display:grid;
                place-items:center;
                width:56px;
                height:56px;
                margin:0 auto 10px;
                border-radius:14px;
                font-size:24px;
                color:#fff
            }
            .hr-icons .lbl{
                font-weight:700
            }

            /* Highlights */
            .hi{
                padding:1.25rem;
                border-radius:var(--radius);
                background:var(--card);
                border:1px solid var(--border);
                box-shadow:var(--shadow);
            }
            .hi i{
                font-size:1.75rem;
                padding:.5rem;
                border-radius:.75rem;
                background:rgba(var(--brand-rgb),.1);
            }

            /* Testimonials */
            .testi p{
                font-style:italic;
                color:var(--text);
            }
            .avatar{
                width:28px;
                height:28px;
                border-radius:50%;
                background:var(--brand);
                color:var(--card);
                display:grid;
                place-items:center;
                font-weight:bold;
            }

            /* Overlays */
            .cmd-overlay{
                position:fixed;
                inset:0;
                background:rgba(0,0,0,.45);
                display:none;
                place-items:center;
                z-index:1050;
                padding:16px
            }
            .cmd-overlay.open{
                display:grid
            }
            .cmd{
                width:min(680px,92vw);
                background:var(--card);
                border:1px solid var(--border);
                border-radius:14px;
                box-shadow:var(--shadow);
                overflow:hidden
            }
            .cmd input{
                width:100%;
                border:0;
                outline:0;
                padding:14px 14px 12px;
                background:transparent;
                color:var(--text)
            }
            .cmd .hint{
                padding:0 14px 8px;
                color:var(--muted);
                font-size:13px
            }
            .cmd ul{
                list-style:none;
                margin:0;
                padding:0;
                max-height:48vh;
                overflow:auto;
                border-top:1px solid var(--border)
            }
            .cmd li{
                display:flex;
                align-items:center;
                gap:10px;
                padding:10px 14px;
                cursor:pointer;
                transition:background .2s;
            }
            .cmd li:hover,
            .cmd li[aria-selected="true"]{
                background:rgba(var(--brand-rgb),.12)
            }
            .kbd{
                border:1px solid var(--border);
                border-bottom-width:2px;
                padding:.15rem .45rem;
                border-radius:.35rem;
                background:var(--card);
                color:var(--text);
                font-weight:600
            }

            .footer{
                border-top:1px solid var(--border)
            }
            .toast-lite{
                position:fixed;
                right:14px;
                bottom:14px;
                display:none;
                background:var(--card);
                color:var(--text);
                padding:10px 12px;
                border-radius:10px;
                box-shadow:var(--shadow);
                font-size:13px;
                z-index:1060;
            }
            .toast-lite.show{
                display:block
            }
            .fade-in{
                animation:fade .35s ease both
            }
            @keyframes fade{
                from{
                    opacity:0;
                    transform:translateY(6px)
                }
                to{
                    opacity:1;
                    transform:translateY(0)
                }
            }

            /* Additional enhancements */
            .status-floating{
                position:fixed;
                bottom:20px;
                left:20px;
                background:var(--card);
                padding:8px 12px;
                border-radius:999px;
                border:1px solid var(--border);
                box-shadow:var(--shadow);
                z-index:1050;
                display:flex;
                align-items:center;
                gap:6px;
                color:var(--muted);
            }
            .faq .accordion-item{
                background:var(--card);
                border:1px solid var(--border);
                border-radius:var(--radius);
                margin-bottom:1rem;
            }
            .faq .accordion-button{
                background:var(--card);
                color:var(--text);
            }
            .faq .accordion-button:not(.collapsed){
                box-shadow:none;
                background:var(--card);
            }
            .faq .accordion-body{
                background:var(--card);
                color:var(--muted);
            }

            /* Dynamic background */
            #bg-anim{
                position:fixed;
                inset:0;
                z-index:-1;
                pointer-events:none;
                transition:transform .5s ease;
            }
            /* ===== Base chip ===== */
            .hr-chip{
                display:inline-flex;
                align-items:center;
                gap:.5rem;
                padding:.4rem .8rem;
                border-radius:999px;
                font-weight:600;
                line-height:1;
                user-select:none;
                white-space:nowrap;
                box-shadow:0 2px 8px rgba(2,6,23,.08);
                border:1px solid transparent;
            }
            .hr-chip .bi{
                opacity:.9
            }

            /* ===== LIGHT THEME ===== */
            html[data-theme="light"] .chip-blue{
                color:#0b3b87;
                background:rgba(59,130,246,.14);
                border-color:rgba(59,130,246,.35);
            }
            html[data-theme="light"] .chip-orange{
                color:#7a3e00;
                background:rgba(245,158,11,.18);
                border-color:rgba(245,158,11,.38);
            }

            /* ===== DARK THEME ===== */
            html[data-theme="dark"] .chip-blue{
                color:#e8f1ff;
                background:rgba(37,99,235,.22);
                border-color:rgba(147,197,253,.35);
            }
            html[data-theme="dark"] .chip-orange{
                color:#fff6e5;
                background:rgba(245,158,11,.22);
                border-color:rgba(251,191,36,.40);
            }



        </style>
    </head>
    <body class="fade-in">

        <!-- ============== ANNOUNCE BAR ============== -->
        <section class="announce py-1">
            <div class="container d-flex align-items-center gap-2 overflow-hidden">
                <span class="badge rounded-pill">Thông báo</span>
                <div class="ticker flex-grow-1" aria-live="polite">
                    <div class="ticker-inner">
                        Tip: Nhấn <b>Ctrl/Cmd + K</b> mở Command Palette · Duyệt đơn nhanh tại <b>Requests → Team</b> · Agenda tô màu trạng thái đi làm / nghỉ / remote ·
                    </div>
                </div>
                <a class="btn btn-outline-primary btn-sm rounded-pill ms-2" href="${pageContext.request.contextPath}/request/list">Vào Requests</a>
            </div>
        </section>

        <!-- ============== NAVBAR ============== -->
        <nav class="navbar navbar-expand-lg sticky-top">
            <div class="container">
                <a class="navbar-brand fw-bold brand-grad" href="${pageContext.request.contextPath}/">LeaveMgmt</a>
                <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#nav"><span class="navbar-toggler-icon"></span></button>
                <div class="collapse navbar-collapse" id="nav">
                    <ul class="navbar-nav me-auto">
                        <li class="nav-item"><a class="nav-link" href="#quick">Tính năng</a></li>
                        <li class="nav-item"><a class="nav-link" href="#highlights">Nổi bật</a></li>
                        <li class="nav-item"><a class="nav-link" href="#metrics">Số liệu</a></li>
                        <li class="nav-item"><a class="nav-link" href="#faq">FAQ</a></li>
                    </ul>
                    <div class="d-flex align-items-center gap-2">
                        <c:choose>
                            <c:when test="${requestScope.dbOK}">
                                <span class="status-badge mini-muted" title="Database connected"><span class="legend-dot dot-green"></span>DB OK · <c:out value="${requestScope.dbMs}"/> ms</span>
                            </c:when>
                            <c:otherwise>
                                <span class="status-badge mini-muted" title="Database NOT reachable" style="border-color:#ffb3b3"><span class="legend-dot dot-red"></span>DB FAIL</span>
                            </c:otherwise>
                        </c:choose>
                        <button id="themeToggle" class="btn btn-sm btn-outline-secondary ring" title="Dark/Light (D)"><i id="themeIcon" class="bi bi-sun"></i></button>
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
            </div>
        </nav>

        <!-- ============== HERO ============== -->
        <section class="hero position-relative">
            <canvas id="heroCanvas" aria-hidden="true"></canvas>
            <span class="blob b1" aria-hidden="true"></span>
            <span class="blob b2" aria-hidden="true"></span>
            <div class="container">
                <div class="row g-4 align-items-center">
                    <div class="col-lg-7">
                        <p class="mini-muted mb-1" id="greet">Xin chào, chúc bạn một ngày hiệu quả!</p>
                        <h1>Giá trị <span class="orange-grad">HRM</span> &amp; <span class="u-underline">Nghỉ phép</span> gọn – đúng quy trình</h1>
                        <p class="lead mini mt-3">Flow chuẩn: <strong>Create → List → Review</strong>. RBAC 3 vai trò, Agenda phòng ban, phím tắt & command palette.</p>
                        <c:if test="${not empty sessionScope.user}">
                            <div class="alert" style="background:var(--card);border:1px solid var(--border)">
                                Xin chào, <strong>${sessionScope.user.fullName}</strong>
                                <c:if test="${sessionScope.isManager}"> · <span class="badge text-bg-primary">MANAGER</span></c:if>
                                <c:if test="${sessionScope.isDivisionLeader}"> · <span class="badge text-bg-info">DIVISION LEADER</span></c:if>
                                </div>
                        </c:if>
                        <div class="d-flex flex-wrap gap-2 mt-3">
                            <c:choose>
                                <c:when test="${empty sessionScope.user}">
                                    <a class="btn btn-lg btn-primary ring" href="${pageContext.request.contextPath}/login"><i class="bi bi-rocket-takeoff me-1"></i>Bắt đầu ngay</a>
                                    <a class="btn btn-lg btn-outline-light ring" href="#quick">Tính năng</a>
                                </c:when>
                                <c:otherwise>
                                    <a class="btn btn-lg btn-primary ring" href="${pageContext.request.contextPath}/request/create"><i class="bi bi-plus-circle me-1"></i>Tạo đơn</a>
                                    <a class="btn btn-lg btn-outline-light ring" href="${pageContext.request.contextPath}/request/list?scope=mine"><i class="bi bi-list-ul me-1"></i>Đơn của tôi</a>
                                </c:otherwise>
                            </c:choose>
                            <button class="btn btn-lg btn-outline-light ring" id="openCmd"><i class="bi bi-terminal me-1"></i>Command (Ctrl/Cmd + K)</button>
                            <button class="btn btn-lg btn-outline-light ring" id="openKbd"><i class="bi bi-keyboard me-1"></i>Phím tắt</button>
                        </div>
                    </div>

                    <div class="col-lg-5">
                        <div class="illus">
                            <div class="ring-bg" id="ringBg">
                                <div class="main-icon"><i class="bi bi-people-fill"></i></div>
                            </div>


                            <div class="hr-chip chip-blue chip-1"><i class="bi bi-clipboard-check"></i> Nghỉ phép</div>
                            <div class="hr-chip chip-orange chip-2"><i class="bi bi-clock-history"></i> Chấm công</div>
                            <div class="hr-chip chip-violet chip-3"><i class="bi bi-shield-lock"></i> Phân quyền</div>
                            <div class="hr-chip chip-blue chip-4"><i class="bi bi-cash-coin"></i> Lương thưởng</div>


                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- ============== QUICK ============== -->
        <section id="quick" class="py-5">
            <div class="container">
                <div class="d-flex align-items-center justify-content-between mb-3">
                    <h2 class="h4 fw-bold mb-0">Truy cập nhanh</h2>
                    <span class="mini-muted">Tip: <span class="kbd">G</span> + <span class="kbd">C/M/T</span> để chuyển trang nhanh</span>
                </div>
                <div class="row g-3">
                    <div class="col-sm-6 col-xl-3"><a class="text-decoration-none" href="${pageContext.request.contextPath}/request/create"><div class="card quick hover"><div><h5 class="mb-1"><i class="bi bi-file-plus me-1"></i>Tạo đơn</h5><small class="mini-muted">Khởi tạo <span class="status-badge">Inprogress</span></small></div></div></a></div>
                    <div class="col-sm-6 col-xl-3"><a class="text-decoration-none" href="${pageContext.request.contextPath}/request/list?scope=mine"><div class="card quick hover"><div><h5 class="mb-1"><i class="bi bi-list-task me-1"></i>Đơn của tôi</h5><small class="mini-muted">Theo dõi trạng thái</small></div></div></a></div>
                                        <c:if test="${sessionScope.isManager || sessionScope.isDivisionLeader}">
                        <div class="col-sm-6 col-xl-3"><a class="text-decoration-none" href="${pageContext.request.contextPath}/request/list?scope=team"><div class="card quick hover"><div><h5 class="mb-1"><i class="bi bi-people me-1"></i>Đơn cấp dưới</h5><small class="mini-muted">Duyệt / Từ chối</small></div></div></a></div>
                                        </c:if>
                    <div class="col-sm-6 col-xl-3"><a class="text-decoration-none" href="${pageContext.request.contextPath}/agenda"><div class="card quick hover"><div><h5 class="mb-1"><i class="bi bi-calendar-week me-1"></i>Agenda</h5><small class="mini-muted">Lịch phòng ban</small></div></div></a></div>
                </div>
            </div>
        </section>

        <!-- ============== HR FEATURES ROW ============== -->
        <section class="py-4">
            <div class="container">
                <h2 class="h5 fw-bold mb-3">Bộ tính năng Quản lý Nhân Sự</h2>
                <div class="row g-3 hr-icons">
                    <div class="col-6 col-md-4 col-xl-2"><div class="card hover"><i class="bi bi-clipboard-check" style="background:#6366f1"></i><div class="lbl">Nghỉ phép</div><div class="mini-muted">Tạo/duyệt nhanh</div></div></div>
                    <div class="col-6 col-md-4 col-xl-2"><div class="card hover"><i class="bi bi-clock-history" style="background:#06b6d4"></i><div class="lbl">Chấm công</div><div class="mini-muted">Giờ làm rõ ràng</div></div></div>
                    <div class="col-6 col-md-4 col-xl-2"><div class="card hover"><i class="bi bi-people-fill" style="background:#f97316"></i><div class="lbl">Phòng ban</div><div class="mini-muted">Org chart</div></div></div>
                    <div class="col-6 col-md-4 col-xl-2"><div class="card hover"><i class="bi bi-shield-lock" style="background:#14b8a6"></i><div class="lbl">Phân quyền</div><div class="mini-muted">RBAC 3 vai trò</div></div></div>
                    <div class="col-6 col-md-4 col-xl-2"><div class="card hover"><i class="bi bi-cash-coin" style="background:#10b981"></i><div class="lbl">Lương thưởng</div><div class="mini-muted">Tổng hợp</div></div></div>
                    <div class="col-6 col-md-4 col-xl-2"><div class="card hover"><i class="bi bi-briefcase" style="background:#eab308"></i><div class="lbl">Tuyển dụng</div><div class="mini-muted">Pipeline</div></div></div>
                </div>
            </div>
        </section>

        <!-- ============== HIGHLIGHTS ============== -->
        <section id="highlights" class="py-5">
            <div class="container">
                <h2 class="h4 fw-bold mb-3">Điểm nổi bật</h2>
                <div class="row g-3">
                    <div class="col-md-4">
                        <div class="hi hover">
                            <div class="d-flex align-items-start gap-3">
                                <i class="bi bi-diagram-3 text-grad"></i>
                                <div>
                                    <h5 class="mb-1">RBAC linh hoạt</h5>
                                    <p class="mini-muted mb-0">Phân quyền Employee/Manager/Division Leader gắn trực tiếp vào luồng duyệt.</p>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="hi hover">
                            <div class="d-flex align-items-start gap-3">
                                <i class="bi bi-shield-check text-grad"></i>
                                <div>
                                    <h5 class="mb-1">Bảo mật & Audit</h5>
                                    <p class="mini-muted mb-0">Log hành động, lịch sử duyệt, thông báo rõ ràng theo người dùng & phòng ban.</p>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="hi hover">
                            <div class="d-flex align-items-start gap-3">
                                <i class="bi bi-speedometer2 text-grad"></i>
                                <div>
                                    <h5 class="mb-1">Hiệu năng & UX</h5>
                                    <p class="mini-muted mb-0">Phím tắt, command palette, counters động, lazy UI, dark/light tự nhớ.</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Testimonials -->
                <h2 class="h4 fw-bold mt-5 mb-3">Khách hàng nói gì</h2>
                <div class="row g-3">
                    <div class="col-lg-6">
                        <div class="card p-3 hover">
                            <div class="testi">
                                <p class="mb-2">“Từ khi dùng LeaveMgmt, team giảm 70% thời gian chờ duyệt và ai cũng nắm rõ lịch vắng mặt.”</p>
                                <div class="d-flex align-items-center gap-2 mini-muted">
                                    <span class="avatar">A</span><span>Anh Khoa · Trưởng nhóm Dev</span>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-lg-6">
                        <div class="card p-3 hover">
                            <div class="testi">
                                <p class="mb-2">“Command Palette đúng cứu tinh. Ctrl+K một phát là nhảy tới trang cần luôn.”</p>
                                <div class="d-flex align-items-center gap-2 mini-muted">
                                    <span class="avatar">B</span><span>Bảo Trâm · HR</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- ============== METRICS ============== -->
        <section id="metrics" class="py-4">
            <div class="container">
                <h2 class="h4 fw-bold mb-3">Số liệu nổi bật</h2>
                <div class="row g-3">
                    <div class="col-6 col-lg-3"><div class="metric hover"><div class="num" data-count="1280">0</div><div class="lbl">Đơn đã xử lý</div></div></div>
                    <div class="col-6 col-lg-3"><div class="metric hover"><div class="num" data-count="98">0</div><div class="lbl">Mức hài lòng (%)</div></div></div>
                    <div class="col-6 col-lg-3"><div class="metric hover"><div class="num" data-count="3">0</div><div class="lbl">Bước duyệt tối đa</div></div></div>
                    <div class="col-6 col-lg-3"><div class="metric hover"><div class="num" data-count="60">0</div><div class="lbl">Khởi tạo &lt; 60s</div></div></div>
                </div>
            </div>
        </section>

        <!-- ============== FAQ ============== -->
        <section id="faq" class="py-5">
            <div class="container">
                <h2 class="h4 fw-bold mb-3">FAQ</h2>
                <div class="accordion" id="faqAcc">
                    <div class="accordion-item">
                        <h2 class="accordion-header"><button class="accordion-button" type="button" data-bs-toggle="collapse" data-bs-target="#f1">Ai có thể duyệt đơn?</button></h2>
                        <div id="f1" class="accordion-collapse collapse show" data-bs-parent="#faqAcc"><div class="accordion-body">Manager hoặc Division Leader theo phân quyền và tuyến báo cáo.</div></div>
                    </div>
                    <div class="accordion-item">
                        <h2 class="accordion-header"><button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#f2">Trạng thái đơn?</button></h2>
                        <div id="f2" class="accordion-collapse collapse" data-bs-parent="#faqAcc"><div class="accordion-body">Inprogress → Approved/Rejected, kèm lý do, thời gian và người xử lý.</div></div>
                    </div>
                    <div class="accordion-item">
                        <h2 class="accordion-header"><button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#f3">Agenda hiển thị?</button></h2>
                        <div id="f3" class="accordion-collapse collapse" data-bs-parent="#faqAcc"><div class="accordion-body">Tô màu theo trạng thái đi làm / nghỉ / remote, lọc theo phòng ban.</div></div>
                    </div>
                </div>
            </div>
        </section>

        <!-- ============== CTA ============== -->
        <section class="py-5">
            <div class="container">
                <div class="card p-4 p-md-5 text-center hover">
                    <h3 class="fw-bold mb-2">Sẵn sàng tối ưu quy trình nghỉ phép?</h3>
                    <p class="mini-muted mb-3">Bắt đầu ngay — chỉ vài phút để làm quen.</p>
                    <c:choose>
                        <c:when test="${empty sessionScope.user}">
                            <a class="btn btn-lg btn-primary ring" href="${pageContext.request.contextPath}/login"><i class="bi bi-person-plus me-1"></i>Đăng nhập</a>
                        </c:when>
                        <c:otherwise>
                            <a class="btn btn-lg btn-primary ring" href="${pageContext.request.contextPath}/request/create"><i class="bi bi-plus-circle me-1"></i>Tạo đơn</a>
                        </c:otherwise>
                    </c:choose>
                    <button id="copyDemo" class="btn btn-lg btn-outline-light ring ms-2"><i class="bi bi-clipboard"></i> Sao chép tài khoản demo</button>
                </div>
            </div>
        </section>

        <footer class="footer py-4">
            <div class="container d-flex flex-wrap justify-content-between align-items-center gap-2">
                <span>© <fmt:formatDate value="<%=new java.util.Date()%>" pattern="yyyy"/> LeaveMgmt</span>
                <span class="mini-muted">FALL 2025 · JSP/Servlet + JDBC · v2.1</span>
            </div>
        </footer>

        <c:if test="${not requestScope.dbOK}">
            <div class="toast-lite show" id="dbToast">Database disconnected</div>
        </c:if>
        <div id="toast" class="toast-lite" role="status" aria-live="polite"></div>

        <!-- Command Palette -->
        <div class="cmd-overlay" id="cmdOverlay" role="dialog" aria-modal="true">
            <div class="cmd">
                <input id="cmdInput" type="text" placeholder="Gõ để tìm hành động… (vd: tạo đơn, agenda, users)" aria-label="Command input" autocomplete="off">
                <div class="hint">↑/↓ để chọn, Enter để mở · Esc để đóng</div>
                <ul id="cmdList" role="listbox"></ul>
            </div>
        </div>

        <!-- Keyboard Modal -->
        <div id="kbdModal" class="cmd-overlay" role="dialog" aria-modal="true" aria-labelledby="kbdTitle">
            <div class="cmd" style="max-width:520px">
                <div class="p-3">
                    <h5 id="kbdTitle"><i class="bi bi-keyboard"></i> Phím tắt</h5>
                    <ul class="mb-3">
                        <li><span class="kbd">D</span> — Bật/tắt Dark mode</li>
                        <li><span class="kbd">G</span> + <span class="kbd">C</span> — Mở Tạo đơn</li>
                        <li><span class="kbd">G</span> + <span class="kbd">M</span> — Đơn của tôi</li>
                        <li><span class="kbd">G</span> + <span class="kbd">T</span> — Đơn cấp dưới</li>
                        <li><span class="kbd">Ctrl/Cmd</span> + <span class="kbd">K</span> — Command Palette</li>
                        <li><span class="kbd">?</span> — Mở bảng này</li>
                    </ul>
                    <button id="closeKbd" class="btn btn-secondary ring">Đóng</button>
                </div>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
        <script>
            (() => {
                const ctx = '<%=request.getContextPath()%>';
                const root = document.documentElement;
                const themeBtn = document.getElementById('themeToggle');
                const themeIcon = document.getElementById('themeIcon');
                const toast = document.getElementById('toast');

                /* Theme */
                (function bootTheme() {
                    const saved = localStorage.getItem('theme');
                    if (saved)
                        root.setAttribute('data-theme', saved);
                    syncIcon();
                })();
                function syncIcon() {
                    const d = (root.getAttribute('data-theme') || 'light') === 'dark';
                    themeIcon.className = d ? 'bi bi-sun' : 'bi bi-moon-stars';
                }
                themeBtn?.addEventListener('click', () => {
                    const next = (root.getAttribute('data-theme') === 'light') ? 'dark' : 'light';
                    root.setAttribute('data-theme', next);
                    localStorage.setItem('theme', next);
                    syncIcon();
                    tip('Đã chuyển sang giao diện ' + (next === 'dark' ? 'tối' : 'sáng'));
                });

                /* Toast helper */
                function tip(msg, ms) {
                    toast.textContent = msg;
                    toast.classList.add('show');
                    setTimeout(() => toast.classList.remove('show'), ms || 1600);
                }

                /* Counters */
                const counters = document.querySelectorAll('.metric .num');
                const io = ('IntersectionObserver' in window) ? new IntersectionObserver(es => {
                    es.forEach(e => {
                        if (e.isIntersecting) {
                            animateNum(e.target, parseInt(e.target.getAttribute('data-count') || '0', 10));
                            io.unobserve(e.target);
                        }
                    });
                }, {threshold: .4}) : null;
                counters.forEach(el => io ? io.observe(el) : animateNum(el, parseInt(el.getAttribute('data-count') || '0', 10)));
                function animateNum(el, to) {
                    let cur = 0;
                    const dur = 1200;
                    const t0 = performance.now();
                    const step = (t) => {
                        const p = Math.min(1, (t - t0) / dur);
                        cur = Math.floor(to * (0.5 - Math.cos(Math.PI * p) / 2));
                        el.textContent = cur.toLocaleString('vi-VN');
                        if (p < 1)
                            requestAnimationFrame(step);
                    };
                    requestAnimationFrame(step);
                }

                /* Illustration parallax */
                (function () {
                    const wrap = document.querySelector('.illus');
                    const ring = document.getElementById('ringBg');
                    if (!wrap || !ring)
                        return;
                    wrap.addEventListener('pointermove', e => {
                        const r = wrap.getBoundingClientRect();
                        const x = (e.clientX - r.left) / r.width - .5;
                        const y = (e.clientY - r.top) / r.height - .5;
                        ring.style.transform = 'rotateX(' + (-y * 6) + 'deg) rotateY(' + (x * 6) + 'deg)';
                    });
                    wrap.addEventListener('pointerleave', () => ring.style.transform = '');
                })();

                /* Copy demo accounts */
                document.getElementById('copyDemo')?.addEventListener('click', () => {
                    const txt = 'Employee\nuser: d.staff\npass: 123456\n\nManager\nuser: a.lead\npass: 123456';
                    if (navigator.clipboard?.writeText) {
                        navigator.clipboard.writeText(txt).then(() => tip('Đã sao chép thông tin demo.'));
                    }
                });

                /* Keyboard shortcuts + Command Palette */
                let goHeld = false;
                document.addEventListener('keydown', e => {
                    const k = (e.key || '').toLowerCase();
                    if ((e.ctrlKey || e.metaKey) && k === 'k') {
                        e.preventDefault();
                        openCmd();
                        return;
                    }
                    if (k === 'd' && !e.ctrlKey && !e.metaKey) {
                        e.preventDefault();
                        themeBtn?.click();
                        return;
                    }
                    if (k === '?') {
                        e.preventDefault();
                        openKbd();
                        return;
                    }
                    if (k === 'g') {
                        goHeld = true;
                        return;
                    }
                    if (goHeld) {
                        if (k === 'c') {
                            location.href = ctx + '/request/create';
                        }
                        if (k === 'm') {
                            location.href = ctx + '/request/list?scope=mine';
                        }
                        if (k === 't') {
                            location.href = ctx + '/request/list?scope=team';
                        }
                        goHeld = false;
                    }
                });
                document.addEventListener('keyup', e => {
                    if ((e.key || '').toLowerCase() === 'g')
                        goHeld = false;
                });

                const cmdOverlay = document.getElementById('cmdOverlay');
                const cmdInput = document.getElementById('cmdInput');
                const cmdList = document.getElementById('cmdList');
                document.getElementById('openCmd')?.addEventListener('click', openCmd);
                function openCmd() {
                    cmdOverlay.classList.add('open');
                    cmdInput.value = '';
                    renderCmd('');
                    setTimeout(() => cmdInput.focus(), 0);
                }
                function closeCmd() {
                    cmdOverlay.classList.remove('open');
                }
                cmdOverlay?.addEventListener('click', e => {
                    if (e.target === cmdOverlay)
                        closeCmd();
                });
                cmdInput?.addEventListener('keydown', e => {
                    const items = [...cmdList.querySelectorAll('li')];
                    const cur = items.findIndex(li => li.getAttribute('aria-selected') === 'true');
                    if (e.key === 'Escape') {
                        closeCmd();
                        return;
                    }
                    if (e.key === 'ArrowDown') {
                        e.preventDefault();
                        setSel(Math.min(items.length - 1, cur + 1));
                        return;
                    }
                    if (e.key === 'ArrowUp') {
                        e.preventDefault();
                        setSel(Math.max(0, cur - 1));
                        return;
                    }
                    if (e.key === 'Enter') {
                        e.preventDefault();
                        const it = items[Math.max(0, cur)];
                        it && it.click();
                        return;
                    }
                });
                cmdInput?.addEventListener('input', e => renderCmd(e.target.value || ''));
                const actions = [
                    {icon: 'bi bi-file-plus', text: 'Tạo đơn', url: ctx + '/request/create', k: 'tao don'},
                    {icon: 'bi bi-list-task', text: 'Đơn của tôi', url: ctx + '/request/list?scope=mine', k: 'don cua toi'},
                    {icon: 'bi bi-people', text: 'Đơn cấp dưới', url: ctx + '/request/list?scope=team', k: 'don cap duoi'},
                    {icon: 'bi bi-calendar-week', text: 'Agenda', url: ctx + '/agenda', k: 'agenda lich'},
                    {icon: 'bi bi-palette', text: 'Đổi theme', run: () => themeBtn?.click(), k: 'theme dark light'},
                ];
                function renderCmd(q) {
                    const qq = normalize(q);
                    const rs = actions.map(a => ({a, score: score(normalize(a.text + ' ' + a.k), qq)}))
                            .filter(x => qq ? x.score > -1 : true)
                            .sort((x, y) => y.score - x.score).slice(0, 8);
                    cmdList.innerHTML = rs.map((x, i) =>
                        '<li role="option" aria-selected="' + (i === 0 ? 'true' : 'false') + '">' +
                                '<i class="' + x.a.icon + '"></i><span>' + x.a.text + '</span>' +
                                '</li>'
                    ).join('');
                    [...cmdList.children].forEach((li, i) => {
                        li.addEventListener('mouseenter', () => setSel(i));
                        li.addEventListener('click', () => {
                            closeCmd();
                            const a = rs[i].a;
                            if (a.run)
                                a.run();
                            else if (a.url)
                                location.href = a.url;
                        });
                    });
                }
                function setSel(i) {
                    [...cmdList.children].forEach((li, idx) => li.setAttribute('aria-selected', idx === i ? 'true' : 'false'));
                }
                function normalize(s) {
                    return (s || '').toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, '');
                }
                function score(text, q) {
                    if (!q)
                        return 0;
                    let p = 0, i = 0;
                    for (const ch of q) {
                        const j = text.indexOf(ch, i); if (j < 0)
                            return -1;
                        p += 1 - (j - i > 3 ? .5 : .1 * (j - i));
                        i = j + 1;
                    }
                    return p;
                }

                /* DB toast if down */
                const dbToast = document.getElementById('dbToast');
                if (dbToast) {
                    dbToast.classList.add('show');
                    setTimeout(() => dbToast.classList.remove('show'), 4000);
                }

                /* Greeting */
                (function greet() {
                    const el = document.getElementById('greet');
                    if (!el)
                        return;
                    const h = new Date().getHours();
                    let msg = 'Xin chào, chúc bạn một ngày hiệu quả!';
                    if (h < 10)
                        msg = 'Chào buổi sáng 🌤️ – Bắt đầu ngày mới thật hăng hái!';
                    else if (h < 14)
                        msg = 'Chúc buổi trưa dễ chịu 🕛 – Nghỉ ngơi một chút nhé.';
                    else if (h < 18)
                        msg = 'Buổi chiều năng suất nhé ☕ – Cùng xử lý các request.';
                    else
                        msg = 'Tối an yên ✨ – Tổng kết ngày làm việc nào.';
                    el.textContent = msg;
                })();

                /* Hero particles */
                (function particles() {
                    const canvas = document.getElementById('heroCanvas');
                    if (!canvas)
                        return;
                    const ctx2 = canvas.getContext('2d');
                    const DPR = Math.min(2, window.devicePixelRatio || 1);
                    let W = 0, H = 0, dots = [];
                    function resize() {
                        const r = canvas.getBoundingClientRect();
                        W = r.width;
                        H = r.height;
                        canvas.width = W * DPR;
                        canvas.height = H * DPR;
                        ctx2.setTransform(DPR, 0, 0, DPR, 0, 0);
                        dots = Array.from({length: 48}, () => ({x: Math.random() * W, y: Math.random() * H, vx: (Math.random() - .5) * .3, vy: (Math.random() - .5) * .3, r: Math.random() * 2 + .6, a: .35 + .35 * Math.random()}));
                    }
                    function step() {
                        ctx2.clearRect(0, 0, W, H);
                        dots.forEach(d => {
                            d.x += d.vx;
                            d.y += d.vy;
                            if (d.x < 0 || d.x > W)
                                d.vx *= -1;
                            if (d.y < 0 || d.y > H)
                                d.vy *= -1;
                            ctx2.beginPath();
                            ctx2.arc(d.x, d.y, d.r, 0, Math.PI * 2);
                            ctx2.fillStyle = `rgba(37,99,235,\${d.a})`;
                            ctx2.fill();
                        });
                        requestAnimationFrame(step);
                    }
                    new ResizeObserver(resize).observe(document.querySelector('.hero'));
                    resize();
                    step();
                })();

                /* Keyboard modal */
                const kbdModal = document.getElementById('kbdModal');
                const openKbdBtn = document.getElementById('openKbd');
                const closeKbdBtn = document.getElementById('closeKbd');
                function openKbd() {
                    kbdModal.classList.add('open');
                }
                function closeKbd() {
                    kbdModal.classList.remove('open');
                }
                openKbdBtn?.addEventListener('click', openKbd);
                closeKbdBtn?.addEventListener('click', closeKbd);
                kbdModal?.addEventListener('click', e => {
                    if (e.target === kbdModal)
                        closeKbd();
                });

                /* Dynamic background gradient animation */
                (function () {
                    const body = document.body;
                    let step = 0;
                    let hue = 200;
                    const speed = 0.15;
                    const gradLayer = document.createElement('div');
                    gradLayer.id = 'bg-anim';
                    gradLayer.style.position = 'fixed';
                    gradLayer.style.inset = 0;
                    gradLayer.style.zIndex = '-1';
                    gradLayer.style.background = 'radial-gradient(circle at 30% 40%, #2563eb33, transparent 60%), radial-gradient(circle at 70% 60%, #8b5cf633, transparent 60%)';
                    gradLayer.style.transition = 'opacity .8s ease';
                    body.prepend(gradLayer);

                    body.addEventListener('pointermove', e => {
                        const x = e.clientX / window.innerWidth - .5;
                        const y = e.clientY / window.innerHeight - .5;
gradLayer.style.transform = `translate(${x * 24}px, ${y * 18}px)`; // KHÔNG scale
                    });
                    body.addEventListener('pointerleave', () => gradLayer.style.transform = '');

                    function loop() {
                        step += speed;
                        hue = (hue + 0.15) % 360;
                        const h1 = hue;
                        const h2 = (hue + 60) % 360;
                        const color1 = `hsla(\${h1}, 70%, 60%, .25)`;
                        const color2 = `hsla(\${h2}, 70%, 60%, .25)`;
                        gradLayer.style.background = `
                radial-gradient(1000px 600px at 20% 30%, \${color1}, transparent 70%),
                radial-gradient(1000px 600px at 80% 70%, \${color2}, transparent 70%)`;
                        requestAnimationFrame(loop);
                    }
                    loop();

                    const observer = new MutationObserver(() => {
                        const theme = root.getAttribute('data-theme') || 'light';
                        gradLayer.style.opacity = (theme === 'dark' ? '1' : '.8');
                    });
                    observer.observe(root, {attributes: true, attributeFilter: ['data-theme']});
                })();
            })();
        </script>
    </body>
</html>