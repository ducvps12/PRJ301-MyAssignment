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
 <!-- CSS đã tách riêng -->
 <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/index.css?v=1"><!-- comment -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/js/index.js?v=1"><!-- comment -->

      
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

        <!-- ============== PARTNERS / LOGO STRIP ============== -->
        <section class="py-5">
            <div class="container">
                <div class="text-center mb-4">
                    <h2 class="h4 fw-bold mb-2">Được tin tưởng bởi các đội nhóm</h2>
                    <p class="mini-muted mb-0">Các logo minh họa — chỉ nhằm mục đích demo UI</p>
                </div>
                <div class="logo-strip">
                    <img loading="lazy" alt="Microsoft" data-src="https://cdn.simpleicons.org/microsoft/60a5fa" width="120" height="32">
                    <img loading="lazy" alt="Google" data-src="https://cdn.simpleicons.org/google/60a5fa" width="120" height="32">
                    <img loading="lazy" alt="GitHub" data-src="https://cdn.simpleicons.org/github/60a5fa" width="120" height="32">
                    <img loading="lazy" alt="Slack" data-src="https://cdn.simpleicons.org/slack/60a5fa" width="120" height="32">
                    <img loading="lazy" alt="Atlassian" data-src="https://cdn.simpleicons.org/atlassian/60a5fa" width="120" height="32">
                    <img loading="lazy" alt="Notion" data-src="https://cdn.simpleicons.org/notion/60a5fa" width="120" height="32">
                    <img loading="lazy" alt="Trello" data-src="https://cdn.simpleicons.org/trello/60a5fa" width="120" height="32">
                    <img loading="lazy" alt="Azure" data-src="https://cdn.simpleicons.org/microsoftazure/60a5fa" width="120" height="32">
                </div>
            </div>
        </section>

        <!-- ============== IMAGE GALLERY ============== -->
        <section id="gallery" class="py-5" style="background: linear-gradient(135deg, rgba(var(--card-rgb), .25), transparent);">
            <div class="container">
                <div class="text-center mb-5">
                    <h2 class="h3 fw-bold mb-2">Thư viện hình ảnh</h2>
                    <p class="mini-muted">Văn phòng · team · hoạt động nhân sự · minh hoạ quy trình</p>
                </div>
                <div class="gallery-grid">
                    <figure class="gallery-item hover"><img alt="office" data-src="https://images.unsplash.com/photo-1521737604893-d14cc237f11d?auto=format&fit=crop&w=1200&q=60"></figure>
                    <figure class="gallery-item hover"><img alt="team" data-src="https://images.unsplash.com/photo-1551836022-d5d88e9218df?auto=format&fit=crop&w=1200&q=60"></figure>
                    <figure class="gallery-item hover"><img alt="meeting" data-src="https://images.unsplash.com/photo-1529336953121-a9d1b3a5bc8b?auto=format&fit=crop&w=1200&q=60"></figure>
                    <figure class="gallery-item hover"><img alt="calendar" data-src="https://images.unsplash.com/photo-1518085250887-2f903c200fee?auto=format&fit=crop&w=1200&q=60"></figure>
                    <figure class="gallery-item hover"><img alt="hr" data-src="https://images.unsplash.com/photo-1552581234-26160f608093?auto=format&fit=crop&w=1200&q=60"></figure>
                    <figure class="gallery-item hover"><img alt="laptop" data-src="https://images.unsplash.com/photo-1518779578993-ec3579fee39f?auto=format&fit=crop&w=1200&q=60"></figure>
                    <figure class="gallery-item hover"><img alt="approval" data-src="https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?auto=format&fit=crop&w=1200&q=60"></figure>
                    <figure class="gallery-item hover"><img alt="report" data-src="https://images.unsplash.com/photo-1519389950473-47ba0277781c?auto=format&fit=crop&w=1200&q=60"></figure>
                    <figure class="gallery-item hover"><img alt="workspace" data-src="https://images.unsplash.com/photo-1487015307662-6ce6210680f1?auto=format&fit=crop&w=1200&q=60"></figure>
                    <figure class="gallery-item hover"><img alt="whiteboard" data-src="https://images.unsplash.com/photo-1557800636-894a64c1696f?auto=format&fit=crop&w=1200&q=60"></figure>
                    <figure class="gallery-item hover"><img alt="dashboard" data-src="https://images.unsplash.com/photo-1553729459-efe14ef6055d?auto=format&fit=crop&w=1200&q=60"></figure>
                    <figure class="gallery-item hover"><img alt="analytics" data-src="https://images.unsplash.com/photo-1556157382-97eda2d62296?auto=format&fit=crop&w=1200&q=60"></figure>
                </div>
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
        
        
       <script defer src="${pageContext.request.contextPath}/assets/js/index.js"></script>

    </body>
</html>