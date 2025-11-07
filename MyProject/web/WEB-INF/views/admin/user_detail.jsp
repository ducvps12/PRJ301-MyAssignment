<%@ include file="/WEB-INF/views/common/_taglibs.jsp" %>
<%-- cần có fn ở _taglibs.jsp:
    <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
--%>

<c:set var="hasUser" value="${not empty u}" />

<c:set var="empCode"
       value="${ hasUser and not empty u.employmentStatusCode
                ? fn:toUpperCase(u.employmentStatusCode) : '' }" />

<c:set var="isActive"
       value="${ hasUser and (
                   (not empty u.status and (u.status == 1 or u.status eq '1'))
                   or (empCode eq 'ACTIVE')
                ) }" />

<c:set var="roleSafe" value="${ hasUser and not empty u.role ? u.role : '' }" />

<%
    // user được servlet đẩy sang
    com.acme.leavemgmt.model.User u =
            (com.acme.leavemgmt.model.User) request.getAttribute("u");

    // user đang login (nếu muốn ẩn các nút khi tự xem chính mình)
    com.acme.leavemgmt.model.User current =
            (com.acme.leavemgmt.model.User) session.getAttribute("currentUser");

    String ctx = request.getContextPath();
    String csrf = (String) request.getAttribute("csrfToken"); // nếu có
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title><c:choose><c:when test="${not empty u}">Chi tiết người dùng #${u.id}</c:when><c:otherwise>Không tìm thấy người dùng</c:otherwise></c:choose></title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <c:set var="isActive" value="${u.status == 1 or u.status eq '1' or u.employmentStatusCode eq 'ACTIVE' or u.status eq 'ACTIVE'}"/>

    <style>
        /* ====== TOKEN MÀU ====== */
        :root {
            --bg: #f7f7f8;
            --card: #fff;
            --b: #e5e7eb;
            --m: #6b7280;
            --ok: #10b981;
            --no: #ef4444;
            --info: #3b82f6;
            --warn: #f97316;
            --shadow-sm: 0 10px 20px rgba(15, 23, 42, .04);
            --shadow-md: 0 20px 45px rgba(15, 23, 42, .08);
            --radius-lg: 16px;
            --radius-md: 12px;
        }
        body {
            font-family: system-ui, -apple-system, "Segoe UI", Roboto, Arial, sans-serif;
            background: var(--bg);
            margin: 0;
            color: #0f172a;
            min-height: 100vh;
        }

        /* ====== TOPBAR ====== */
        .topbar {
            backdrop-filter: blur(8px);
            background: rgba(247, 247, 248, 0.85);
            border-bottom: 1px solid rgba(226, 232, 240, 0.8);
            position: sticky;
            top: 0;
            z-index: 50;
        }
        .topbar-inner {
            max-width: 1080px;
            margin: 0 auto;
            padding: 12px 16px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 10px;
        }
        .back-btn {
            border: 1px solid rgba(148, 163, 184, 0.6);
            background: #fff;
            border-radius: 999px;
            display: flex;
            align-items: center;
            gap: 6px;
            padding: 6px 14px;
            font-weight: 500;
            cursor: pointer;
            transition: .15s;
        }
        .back-btn:hover { background: #eff6ff; }
        .breadcrumbs {
            display: flex;
            gap: 6px;
            align-items: center;
            color: #94a3b8;
            font-size: 13px;
        }
        .breadcrumbs strong { color: #0f172a; }
        .top-actions { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }

        /* ====== BUTTONS ====== */
        .btn {
            border: 1px solid var(--b);
            background: #fff;
            border-radius: 10px;
            padding: 8px 14px;
            cursor: pointer;
            text-decoration: none;
            color: inherit;
            display: inline-flex;
            gap: 6px;
            align-items: center;
            font-size: 14px;
            transition: .15s;
        }
        .btn:hover { background: #f3f4f6; }
        .btn-primary {
            background: #0f172a;
            border-color: #0f172a;
            color: #fff;
            box-shadow: 0 14px 30px rgba(15, 23, 42, .18);
        }
        .btn-danger { border-color: var(--no); color: #b91c1c; background: rgba(254, 226, 226, .6);}
        .btn-success { border-color: var(--ok); color: #065f46; background: rgba(209, 250, 229, .5);}
        .btn-icon { width: 36px; height: 36px; border-radius: 999px; justify-content: center; }

        /* ====== LAYOUT ====== */
        .page {
            max-width: 1080px;
            margin: 20px auto 24px;
            padding: 0 16px 50px;
            display: grid;
            grid-template-columns: minmax(0, 1.15fr) 360px;
            gap: 18px;
        }
        @media (max-width: 998px) {
            .page { grid-template-columns: 1fr; }
            .topbar-inner { flex-direction: column; align-items: flex-start; }
            .top-actions { justify-content: flex-start; }
        }
        .card {
            background: var(--card);
            border: 1px solid var(--b);
            border-radius: var(--radius-lg);
            box-shadow: var(--shadow-sm);
        }
        .card-header {
            padding: 16px 18px 10px;
            border-bottom: 1px solid rgba(226, 232, 240, .7);
            display: flex;
            gap: 10px;
            align-items: center;
            justify-content: space-between;
        }
        .card-title { font-weight: 600; }
        .card-sub { color: var(--m); font-size: 13px; }
        .card-body { padding: 14px 18px 16px; }

        /* ====== PROFILE ====== */
        .profile-head { display: flex; gap: 16px; align-items: flex-start; }
        .avatar-xxl {
            width: 76px;
            height: 76px;
            border-radius: 28px;
            background: linear-gradient(135deg, #0f172a 20%, #38bdf8);
            color: #fff;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 30px;
            font-weight: 700;
            box-shadow: 0 12px 35px rgba(15, 23, 42, .28);
        }
        .profile-meta { flex: 1; }
        .name-line { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
        .title { font-size: 19px; font-weight: 650; }
        .badge {
            display: inline-flex;
            gap: 4px;
            align-items: center;
            padding: 3px 12px;
            border-radius: 999px;
            font-size: 12px;
            font-weight: 500;
        }
        .badge-role-admin { background: #fef9c3; color: #92400e; border: 1px solid rgba(250, 204, 21, .7); }
        .badge-role-lead { background: #eff6ff; color: #1d4ed8; border: 1px solid rgba(59, 130, 246, .2); }
        .badge-role-staff { background: #f3f4f6; color: #374151; border: 1px solid rgba(148, 163, 184, .28); }
        .badge-status-active { background: #d1fae5; color: #065f46; }
        .badge-status-inactive { background: #fee2e2; color: #991b1b; }
        .presence-dot {
            width: 10px; height: 10px; border-radius: 999px; background: #22c55e;
            box-shadow: 0 0 0 4px rgba(34,197,94,.15);
        }

        /* ====== INFO GRID ====== */
        .info-grid {
            display: grid;
            grid-template-columns: 140px 1fr;
            gap: 8px 12px;
            margin-top: 16px;
        }
        .info-label { color: var(--m); font-size: 13px; }
        .info-value { font-weight: 500; }
        .inline { display: inline-flex; gap: 6px; align-items: center; }

        @media (max-width: 640px) {
            .profile-head { flex-direction: column; align-items: flex-start; }
            .info-grid { grid-template-columns: 1fr; }
        }

        /* ====== TABS ====== */
        .tabs {
            display: flex;
            gap: 6px;
            border-bottom: 1px solid rgba(226, 232, 240, .5);
            margin-bottom: 6px;
        }
        .tab {
            padding: 6px 10px 10px;
            cursor: pointer;
            font-weight: 500;
            color: #64748b;
            border-bottom: 2px solid transparent;
            display: inline-flex; gap: 6px; align-items: center;
        }
        .tab.active { color: #0f172a; border-color: #0f172a; }
        .tab .dot {
            width: 8px; height: 8px;
            border-radius: 999px; background: #22c55e;
        }
        .tab-content { display: none; animation: fade .2s ease; }
        .tab-content.active { display: block; }
        @keyframes fade { from{opacity:0;transform:translateY(4px);} to{opacity:1;transform:none;} }

        /* ====== TIMELINE ====== */
        .timeline { list-style: none; padding: 0; margin: 0; }
        .timeline-item {
            padding: 8px 0 14px 26px;
            border-left: 1px solid rgba(148, 163, 184, .35);
            position: relative;
        }
        .timeline-item::before {
            content: "";
            width: 10px; height: 10px; border-radius: 999px;
            background: #fff; border: 2px solid #38bdf8;
            position: absolute; top: 7px; left: -5px;
        }
        .timeline-title { font-weight: 500; }
        .timeline-meta { color: #94a3b8; font-size: 12px; }

        /* ====== SIDEBAR ====== */
        .side-card { padding: 16px 16px 10px; display: flex; flex-direction: column; gap: 10px; }
        .side-section-title { font-weight: 600; }
        .tag-list { display: flex; gap: 6px; flex-wrap: wrap; }
        .tag {
            background: rgba(15, 23, 42, .03);
            border: 1px solid rgba(148, 163, 184, 0.25);
            border-radius: 999px;
            padding: 3px 10px;
            font-size: 12px;
        }

        /* ====== TOAST ====== */
        .toast {
            position: fixed;
            bottom: 16px;
            right: 16px;
            background: #0f172a;
            color: #fff;
            padding: 10px 14px;
            border-radius: 999px;
            display: none;
            gap: 8px;
            align-items: center;
            box-shadow: var(--shadow-md);
            z-index: 999;
        }

        /* PRINT */
        @media print {
            .topbar, .btn, .card-header, .tabs, #toast { display: none !important; }
            body { background: #fff; }
            .page { display: block; }
            .card { border: none; box-shadow: none; }
        }
    </style>
</head>
<body>

<!-- ====== TOPBAR ====== -->
<div class="topbar">
    <div class="topbar-inner">
        <div style="display:flex;gap:10px;align-items:center;">
            <button class="back-btn" type="button" onclick="window.location.href='<%=ctx%>/admin/users'">
                <span style="font-size:18px;">←</span> Danh sách
            </button>
            <div class="breadcrumbs">
                <span>Admin</span> /
                <a href="<%=ctx%>/admin/users" style="color:#64748b;text-decoration:none;">Users</a> /
                <strong>Chi tiết</strong>
            </div>
        </div>
        <div class="top-actions">
            <button class="btn-icon" type="button" onclick="toggleTheme()" title="Đổi giao diện">🌓</button>
            <button class="btn-icon" type="button" onclick="printUser()" title="In trang">🖨</button>
            <c:if test="${not empty u}">
                <a class="btn" href="<%=ctx%>/admin/users/edit?id=${u.id}">Sửa nhanh</a>
            </c:if>
        </div>
    </div>
</div>

<!-- ====== NẾU KHÔNG CÓ USER ====== -->
<c:if test="${empty u}">
    <div style="max-width:700px;margin:40px auto;text-align:center;">
        <h2>Không tìm thấy người dùng</h2>
        <p class="card-sub">Có thể user đã bị xóa hoặc bạn vào sai link.</p>
        <a class="btn" href="<%=ctx%>/admin/users">← Quay lại danh sách</a>
    </div>
</c:if>

<!-- ====== CÓ USER ====== -->
<c:if test="${not empty u}">
    <div class="page" id="page-wrap">
        <!-- ====== CỘT TRÁI ====== -->
        <div>
            <!-- PROFILE CARD -->
            <div class="card" style="margin-bottom:16px;">
                <div class="card-body">
                    <div class="profile-head">
                        <div class="avatar-xxl">
                            <c:choose>
                                <c:when test="${not empty u.fullName}">
                                    ${fn:toUpperCase(fn:substring(u.fullName,0,1))}
                                </c:when>
                                <c:otherwise>U</c:otherwise>
                            </c:choose>
                        </div>
                        <div class="profile-meta">
                            <div class="name-line">
                                <div class="title">
                                    <c:out value="${empty u.fullName ? u.username : u.fullName}"/>
                                </div>
                                <c:choose>
                                    <c:when test="${u.role == 'ADMIN'}">
                                        <span class="badge badge-role-admin">ADMIN</span>
                                    </c:when>
                                    <c:when test="${u.role == 'DIV_LEADER' || u.role == 'TEAM_LEADER'}">
                                        <span class="badge badge-role-lead">${u.role}</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge badge-role-staff">${u.role}</span>
                                    </c:otherwise>
                                </c:choose>
                               
                                

<c:if test="${isActive}">
  <span class="badge badge-status-active"><span class="presence-dot"></span> Đang hoạt động</span>
</c:if>
<c:if test="${not isActive}">
  <span class="badge badge-status-inactive">Ngưng hoạt động</span>
</c:if>



                            </div>
                            <div class="card-sub" style="margin-top:4px;">
                                Username: <strong>${u.username}</strong>
                                <c:if test="${not empty u.department}">
                                    · <span style="color:#0f172a;">${u.department}</span>
                                </c:if>
                            </div>
                            <div style="margin-top:8px;display:flex;gap:8px;flex-wrap:wrap;">
<button class="btn" type="button"
        data-email="${hasUser and not empty u.email ? u.email : ''}"
        onclick="copyToClipboard(this.getAttribute('data-email'))">
  Copy email
</button>
                                <c:if test="${not empty u.email}">
                                    <a class="btn" href="mailto:${u.email}">Gửi mail</a>
                                </c:if>
                            </div>
                        </div>
                    </div>

                    <!-- GRID THÔNG TIN -->
                    <div class="info-grid">
                        <div class="info-label">Email</div>
                        <div class="info-value inline">
                            <span>${empty u.email ? '—' : u.email}</span>
                        </div>

                        <div class="info-label">Phòng ban</div>
                        <div class="info-value">${empty u.department ? 'Chưa gán' : u.department}</div>

                        <div class="info-label">Trạng thái</div>
                        <div class="info-value">
                          
                            <c:choose>
  <c:when test="${isActive}">
    <span class="badge badge-status-active">ACTIVE</span>
  </c:when>
  <c:otherwise>
    <span class="badge badge-status-inactive">INACTIVE</span>
  </c:otherwise>
</c:choose>



                        </div>

                        <div class="info-label">Role hệ thống</div>
                        <div class="info-value">${u.role}</div>
                    </div>
                </div>
            </div>

            <!-- THẺ TAB -->
            <div class="card">
                <div class="card-header">
                    <div>
                        <div class="card-title">Thông tin & hoạt động</div>
                        <div class="card-sub">Quản lý user này nhanh chóng</div>
                    </div>
                    <div style="display:flex;gap:6px;">
                        <a class="btn" href="<%=ctx%>/admin/users/edit?id=${u.id}">Chỉnh sửa</a>
                    </div>
                </div>
                <div class="card-body">
                    <div class="tabs">
                        <div class="tab active" data-tab="overview">Tổng quan</div>
                        <div class="tab" data-tab="activity">Hoạt động gần đây <span class="dot"></span></div>
                        <div class="tab" data-tab="security">Bảo mật</div>
                    </div>

                    <!-- Tổng quan -->
                    <div class="tab-content active" id="tab-overview">
                        <p class="card-sub">Thông tin cơ bản của nhân sự.</p>
                        <ul style="margin:0;padding-left:18px;">
                            <li>Đã cấp role: <strong>${u.role}</strong></li>
                            <li>Phòng ban: <strong>${empty u.department ? 'Chưa gán' : u.department}</strong></li>
                            <li>Trạng thái:
                            
                                

                                <c:choose>
  <c:when test="${isActive}">
    <span class="badge badge-status-active">ACTIVE</span>
  </c:when>
  <c:otherwise>
    <span class="badge badge-status-inactive">INACTIVE</span>
  </c:otherwise>
</c:choose>


                            </li>
                            <li>Email: <strong>${empty u.email ? '—' : u.email}</strong></li>
                        </ul>
                    </div>

                    <!-- Activity -->
                    <div class="tab-content" id="tab-activity">
                        <ul class="timeline">
                            <li class="timeline-item">
                                <div class="timeline-title">Đăng nhập hệ thống</div>
                                <div class="timeline-meta">~ gần đây · IP 192.168.x.x (demo)</div>
                            </li>
                            <li class="timeline-item">
                                <div class="timeline-title">Xem / yêu cầu nghỉ phép</div>
                                <div class="timeline-meta">Hôm qua · qua module Leave Request</div>
                            </li>
                            <li class="timeline-item">
                                <div class="timeline-title">Được gán vào phòng ban ${empty u.department ? '...' : u.department}</div>
                                <div class="timeline-meta">3 ngày trước</div>
                            </li>
                        </ul>
                    </div>

                    <!-- Security -->
                    <div class="tab-content" id="tab-security">
                        <p class="card-sub">Hành động nhạy cảm</p>
                        <form method="post" action="<%=ctx%>/admin/users/resetpw"
                              onsubmit="return confirm('Reset mật khẩu về mặc định cho user #${u.id}?');">
                            <input type="hidden" name="id" value="${u.id}"/>
                            <c:if test="${not empty csrf}">
                                <input type="hidden" name="_token" value="<%=csrf%>">
                            </c:if>
                            <button type="submit" class="btn">Reset mật khẩu</button>
                        </form>
                        <p style="font-size:12px;color:#94a3b8;margin-top:6px;">
                            Khi reset, hệ thống có thể gửi mail / log audit tùy bạn code.
                        </p>
                    </div>
                </div>
            </div>

            <!-- HÀNH ĐỘNG CHÍNH -->
            <div class="card" style="margin-top:16px;">
                <div class="card-header">
                    <div class="card-title">Hành động</div>
                </div>
                <div class="card-body" style="display:flex;gap:12px;flex-wrap:wrap;">
                    <!-- bật / tắt -->
                    <form method="post" action="<%=ctx%>/admin/users/toggle" onsubmit="return confirmToggle(this)">
                        <input type="hidden" name="id" value="${u.id}">
                        <c:if test="${not empty csrf}">
                            <input type="hidden" name="_token" value="<%=csrf%>">
                        </c:if>
                       
                        

<c:choose>
  <c:when test="${isActive}">
    <button type="submit" class="btn btn-danger">Vô hiệu hóa</button>
  </c:when>
  <c:otherwise>
    <button type="submit" class="btn btn-success">Kích hoạt</button>
  </c:otherwise>
</c:choose>



                    </form>

                    <!-- reset pw -->
                    <form method="post" action="<%=ctx%>/admin/users/resetpw"
                          onsubmit="return confirm('Reset mật khẩu về mặc định cho user #${u.id}?');">
                        <input type="hidden" name="id" value="${u.id}">
                        <c:if test="${not empty csrf}">
                            <input type="hidden" name="_token" value="<%=csrf%>">
                        </c:if>
                        <button type="submit" class="btn">Reset mật khẩu</button>
                    </form>

                    <a class="btn" href="<%=ctx%>/admin/users/edit?id=${u.id}">Sửa thông tin</a>
                    <a class="btn" href="<%=ctx%>/admin/users">Quay lại danh sách</a>
                </div>
            </div>
        </div>

        <!-- ====== CỘT PHẢI ====== -->
        <div style="display:flex;flex-direction:column;gap:16px;">
            <div class="card">
                <div class="side-card">
                    <div class="side-section-title">Tóm tắt</div>
                    <div class="card-sub">ID: <strong>#${u.id}</strong></div>
                    <div class="card-sub">Username: <strong>${u.username}</strong></div>
                    <div class="card-sub">Email: <strong>${empty u.email ? '—' : u.email}</strong></div>
                    <div class="card-sub">Phòng ban: <strong>${empty u.department ? 'Chưa gán' : u.department}</strong></div>
                    <div class="card-sub">Role: <strong>${u.role}</strong></div>
                </div>
            </div>

            <div class="card">
                <div class="side-card">
                    <div class="side-section-title">Tags / Ghi chú nhanh</div>
                    <div class="tag-list">
                        <span class="tag">HR tracking</span>
                        <span class="tag">Chưa nghỉ phép</span>
                        <span class="tag">Đang online</span>
                        <span class="tag">${u.role}</span>
                    </div>
                </div>
            </div>

            <div class="card">
                <div class="side-card">
                    <div class="side-section-title">Liên hệ</div>
                    <div style="display:flex;gap:6px;flex-wrap:wrap;">
                        <c:if test="${not empty u.email}">
                            <a class="btn" href="mailto:${u.email}">Gửi email</a>
                        </c:if>
<button class="btn" type="button"
        data-email="${hasUser and not empty u.email ? u.email : ''}"
        onclick="copyToClipboard(this.getAttribute('data-email'))">
  Copy email
</button>
                    </div>
                    <p class="card-sub">Có thể đồng bộ với HRM / Payroll nếu bạn kết nối.</p>
                </div>
            </div>
        </div>
    </div>
</c:if>

<!-- TOAST -->
<div id="toast" class="toast">
    ✅ Đã copy vào clipboard
</div>

<script>
    // ====== TAB ======
    document.querySelectorAll('.tab').forEach(function (el) {
        el.addEventListener('click', function () {
            document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
            document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
            el.classList.add('active');
            const id = el.getAttribute('data-tab');
            document.getElementById('tab-' + id).classList.add('active');
        });
    });

    // ====== COPY CLIPBOARD ======
    function copyToClipboard(text) {
        if (!text) return;
        navigator.clipboard.writeText(text).then(function () {
            showToast();
        });
    }
    function showToast() {
        const t = document.getElementById('toast');
        t.style.display = 'flex';
        setTimeout(() => { t.style.display = 'none'; }, 1800);
    }

    // ====== CONFIRM Toggle ======
    function confirmToggle(form) {
        const btn = form.querySelector('button');
        const isDeactivate = btn && btn.classList.contains('btn-danger');
        return confirm(isDeactivate ? 'Bạn chắc chắn muốn vô hiệu hóa user này?' : 'Kích hoạt lại user này?');
    }

    // ====== PRINT ======
    function printUser() {
        window.print();
    }

    // ====== DARK MODE MINI ======
    function toggleTheme() {
        const html = document.documentElement;
        const isDark = html.dataset.theme === 'dark';
        if (isDark) {
            html.dataset.theme = '';
            document.body.style.background = 'var(--bg)';
        } else {
            html.dataset.theme = 'dark';
            document.body.style.background = '#0f172a';
        }
        document.querySelectorAll('.card').forEach(c => {
            if (!isDark) {
                c.style.background = 'rgba(15, 23, 42, .35)';
                c.style.color = '#fff';
            } else {
                c.style.background = '#fff';
                c.style.color = '#0f172a';
            }
        });
    }
</script>

</body>
</html>
