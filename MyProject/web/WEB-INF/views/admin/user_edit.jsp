<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp" %>
<%@ include file="/WEB-INF/views/common/_header.jsp" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!-- đồng bộ tên attribute: servlet có thể set 'u' hoặc 'user' -->
<c:set var="u" value="${empty u ? user : u}" />

<style>
    :root {
        --bg: #f4f5fb;
        --card: #ffffff;
        --stroke: rgba(15,23,42,0.06);
        --ink: #0f172a;
        --muted: #6b7280;
        --pri: #6366f1;
        --pri-light: rgba(99,102,241,0.12);
        --danger: #ef4444;
        --success: #10b981;
        --radius: 20px;
    }
    body {
        background: radial-gradient(circle at 0 0, rgba(99,102,241,0.15), transparent 45%), var(--bg);
    }
    .page-shell {
        max-width: 1050px;
        margin: 1.5rem auto 3rem;
        padding: 0 1.2rem;
    }
    .page-title-bar {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 1rem;
        margin-bottom: 1.1rem;
    }
    .page-title h1 {
        font-size: 1.6rem;
        font-weight: 700;
        display: flex;
        align-items: center;
        gap: .5rem;
    }
    .badge-soft {
        background: rgba(99,102,241,0.12);
        color: #3730a3;
        font-weight: 500;
        padding: .2rem .6rem;
        border-radius: 999px;
        font-size: .7rem;
    }
    .user-edit-layout {
        display: grid;
        grid-template-columns: 300px 1fr;
        gap: 1.5rem;
        align-items: flex-start;
    }
    @media (max-width: 900px) {
        .user-edit-layout { grid-template-columns: 1fr; }
    }
    .card {
        background: var(--card);
        border: 1px solid var(--stroke);
        border-radius: 18px;
        box-shadow: 0 15px 35px rgba(15,23,42,0.02);
    }
    .card-header {
        padding: 1rem 1.25rem .35rem;
        border-bottom: 1px solid rgba(15,23,42,0.03);
    }
    .card-body {
        padding: 1rem 1.25rem 1.25rem;
    }
    .profile-box {
        text-align: center;
        padding: 1.15rem 1rem 1rem;
    }
    .avatar-circle {
        width: 88px;
        height: 88px;
        border-radius: 999px;
        margin: 0 auto .9rem;
        display: grid;
        place-items: center;
        font-size: 2.4rem;
        font-weight: 700;
        background: radial-gradient(circle at 10% 20%, rgba(99,102,241,.8), rgba(59,130,246,1));
        color: #fff;
        box-shadow: 0 10px 25px rgba(99,102,241,.25);
    }
    .profile-name {
        font-weight: 700;
        font-size: 1.1rem;
    }
    .profile-role {
        color: var(--muted);
        font-size: .8rem;
        margin-bottom: .6rem;
    }
    .muted-line {
        font-size: .73rem;
        color: var(--muted);
    }
    .quick-actions {
        display: flex;
        gap: .5rem;
        margin-top: .85rem;
        flex-wrap: wrap;
        justify-content: center;
    }
    .tag-btn {
        background: rgba(15,23,42,0.03);
        border: 1px solid rgba(15,23,42,0.02);
        border-radius: 999px;
        padding: .3rem .7rem .35rem;
        font-size: .65rem;
        cursor: pointer;
        transition: all .18s;
    }
    .tag-btn:hover {
        background: rgba(99,102,241,0.12);
        color: #172554;
    }

    /* form controls */
    .form-row {
        display: flex;
        gap: 1rem;
    }
    .form-group {
        margin-bottom: .9rem;
        width: 100%;
    }
    .form-group label {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-weight: 600;
        color: #111827;
        margin-bottom: .3rem;
        font-size: .8rem;
    }
    .input {
        width: 100%;
        border: 1px solid rgba(15,23,42,0.08);
        border-radius: 13px;
        padding: .5rem .6rem .5rem 2.2rem;
        height: 38px;
        font-size: .8rem;
        transition: all .15s;
        background: #f9fafb;
    }
    .input:focus {
        outline: none;
        border-color: rgba(99,102,241,0.4);
        background: #fff;
        box-shadow: 0 0 0 3px rgba(99,102,241,0.10);
    }
    .input-wrap {
        position: relative;
    }
    .input-icon {
        position: absolute;
        left: .7rem;
        top: 50%;
        transform: translateY(-50%);
        font-size: .9rem;
        color: #94a3b8;
    }
    select.input {
        padding-left: 2.2rem;
        background-image: linear-gradient(45deg, transparent 50%, #94a3b8 55%), linear-gradient(135deg, #94a3b8 45%, transparent 55%);
        background-position: calc(100% - 18px) 52%, calc(100% - 13px) 52%;
        background-size: 5px 5px, 5px 5px;
        background-repeat: no-repeat;
    }

    .status-chip {
        display: inline-flex;
        align-items: center;
        gap: .35rem;
        border-radius: 999px;
        font-size: .65rem;
        padding: .25rem .6rem;
    }
    .status-active {
        background: rgba(16,185,129,.12);
        color: #166534;
    }
    .status-inactive {
        background: rgba(239,68,68,.12);
        color: #b91c1c;
    }

    .form-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 1rem;
        margin-top: .4rem;
    }
    .btn {
        display: inline-flex;
        align-items: center;
        gap: .4rem;
        border: none;
        background: #e2e8f0;
        color: #0f172a;
        border-radius: 11px;
        padding: .45rem .85rem .5rem;
        font-weight: 600;
        font-size: .78rem;
        cursor: pointer;
        transition: .14s;
        text-decoration: none;
    }
    .btn:hover { filter: brightness(.97); }
    .btn-primary {
        background: linear-gradient(135deg, #6366f1, #4f46e5);
        color: #fff;
        box-shadow: 0 10px 25px rgba(99,102,241,0.25);
    }
    .btn-danger {
        background: rgba(239,68,68,1);
        color: #fff;
    }
    .btn-ghost {
        background: transparent;
        color: #475569;
    }

    .scroll-hint {
        font-size: .7rem;
        color: #94a3b8;
    }

    /* mini timeline / audit */
    .mini-timeline {
        list-style: none;
        padding-left: 0;
        margin: .5rem 0 0;
    }
    .mini-timeline li {
        display: flex;
        gap: .6rem;
        margin-bottom: .5rem;
        align-items: flex-start;
    }
    .mini-dot {
        width: 7px;
        height: 7px;
        background: #6366f1;
        border-radius: 999px;
        margin-top: .35rem;
    }
    .mini-txt {
        font-size: .68rem;
        line-height: 1.1rem;
    }
    .mini-txt strong { font-weight: 600; }

    .toast {
        position: fixed;
        top: 1.1rem;
        right: 1rem;
        background: #111827;
        color: #fff;
        padding: .6rem 1rem .7rem;
        border-radius: .7rem;
        font-size: .78rem;
        box-shadow: 0 10px 30px rgba(0,0,0,.12);
        display: none;
        z-index: 9999;
    }
</style>

<div class="page-shell">
    <div class="page-title-bar">
        <div class="page-title">
            <h1>
                <span>🧑‍💻 Sửa thông tin người dùng</span>
                <span class="badge-soft">ID #<c:out value="${u.id}" /></span>
            </h1>
            <p class="muted-line">Chỉnh sửa hồ sơ, phân quyền, trạng thái & phòng ban. Thay đổi sẽ có hiệu lực ngay.</p>
        </div>
        <div>
            <a href="${ctx}/admin/users" class="btn btn-ghost">← Về danh sách</a>
        </div>
    </div>

    <div class="user-edit-layout">
        <!-- SIDE PROFILE / ACTIONS -->
        <div class="card">
            <div class="profile-box">
                <div class="avatar-circle" id="avatarCircle">
                    <!-- sẽ generate từ JS nếu không có -->
                    <c:out value="${empty u.fullName ? (empty u.username ? 'U' : fn:substring(u.username,0,1)) : fn:substring(u.fullName,0,1)}" />
                </div>
                <div class="profile-name">
                    <c:out value="${empty u.fullName ? u.username : u.fullName}" />
                </div>
                <div class="profile-role">Role hiện tại:
                    <strong><c:out value="${u.role}" /></strong>
                </div>
                <c:choose>
                    <c:when test="${u.status == 1 || u.status == 'ACTIVE'}">
                        <span class="status-chip status-active">● Đang hoạt động</span>
                    </c:when>
                    <c:otherwise>
                        <span class="status-chip status-inactive">● Đã khóa</span>
                    </c:otherwise>
                </c:choose>

                <div class="quick-actions">
                    <button type="button" class="tag-btn" onclick="fillLeader()">Gán LEADER</button>
                    <button type="button" class="tag-btn" onclick="fillDept('IT')">Phòng IT</button>
                    <button type="button" class="tag-btn" onclick="fillDept('HR')">Phòng HR</button>
                    <button type="button" class="tag-btn" onclick="genRandomPass()">Tạo mật khẩu</button>
                </div>
            </div>
            <div class="card-body">
                <h4 style="font-size:.8rem;font-weight:600;margin-bottom:.4rem;">Hoạt động gần đây</h4>
                <ul class="mini-timeline">
                    <li>
                        <div class="mini-dot"></div>
                        <div class="mini-txt"><strong>Đăng nhập:</strong> ${empty u.lastLogin ? 'Chưa có' : u.lastLogin}</div>
                    </li>
                    <li>
                        <div class="mini-dot"></div>
                        <div class="mini-txt">Tạo lúc: ${empty u.createdAt ? '—' : u.createdAt}</div>
                    </li>
                    <li>
                        <div class="mini-dot"></div>
                        <div class="mini-txt">Cập nhật bởi Admin lúc: ${empty u.updatedAt ? '—' : u.updatedAt}</div>
                    </li>
                </ul>

                <hr style="border:none;border-top:1px solid rgba(15,23,42,.04);margin:.8rem 0 .7rem;">

                <p class="muted-line">Hành động nhanh</p>
                <div style="display:flex;gap:.5rem;flex-wrap:wrap;">
                    <a href="${ctx}/admin/users/reset?id=${u.id}" class="btn btn-danger" title="Reset mật khẩu về mặc định">
                        🔑 Reset pass
                    </a>
                    <c:choose>
                        <c:when test="${u.status == 1 || u.status == 'ACTIVE'}">
                            <a href="${ctx}/admin/users/deactivate?id=${u.id}" class="btn btn-ghost" style="color:#b91c1c">
                                🚫 Khóa tài khoản
                            </a>
                        </c:when>
                        <c:otherwise>
                            <a href="${ctx}/admin/users/activate?id=${u.id}" class="btn btn-primary" style="gap:.35rem;">
                                ✅ Kích hoạt lại
                            </a>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>

        <!-- MAIN FORM -->
        <form method="post"
              action="${ctx}/admin/users/edit"
              class="card"
              id="userEditForm"
              autocomplete="off">
            <div class="card-header">
                <h3 style="font-size:1rem;font-weight:700;margin-bottom:.15rem;">Thông tin cơ bản</h3>
                <p class="scroll-hint">Điền đủ các trường cần thiết để đảm bảo phân quyền chính xác.</p>
            </div>
            <div class="card-body">

                <input type="hidden" name="id" value="${u.id}"/>

                <div class="form-group">
                    <label for="fullName">Họ và tên
                        <span style="color:#ef4444">*</span>
                        <span id="nameLen" class="muted-line"></span>
                    </label>
                    <div class="input-wrap">
                        <span class="input-icon">👤</span>
                        <input class="input" id="fullName" name="fullName"
                               value="${u.fullName}" maxlength="80" required>
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label for="email">Email <span style="color:#ef4444">*</span></label>
                        <div class="input-wrap">
                            <span class="input-icon">📧</span>
                            <input class="input" id="email" name="email" type="email"
                                   value="${u.email}" required>
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="username">Username</label>
                        <div class="input-wrap">
                            <span class="input-icon">👨‍💼</span>
                            <input class="input" id="username" name="username" value="${u.username}" disabled>
                        </div>
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label for="role">Role / Quyền <span style="color:#ef4444">*</span></label>
                        <div class="input-wrap">
                            <span class="input-icon">🛡️</span>
                            <select class="input" name="role" id="role">
                                <option value="STAFF" <c:if test="${u.role=='STAFF'}">selected</c:if>>Nhân viên (STAFF)</option>
                                <option value="TEAM_LEAD" <c:if test="${u.role=='TEAM_LEAD'}">selected</c:if>>Trưởng nhóm (TEAM_LEAD)</option>
                                <option value="DIV_LEADER" <c:if test="${u.role=='DIV_LEADER'}">selected</c:if>>Trưởng phòng (DIV_LEADER)</option>
                                <option value="HR" <c:if test="${u.role=='HR'}">selected</c:if>>Nhân sự (HR)</option>
                                <option value="ADMIN" <c:if test="${u.role=='ADMIN'}">selected</c:if>>Quản trị (ADMIN)</option>
                            </select>
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="department">Phòng ban</label>
                        <div class="input-wrap">
                            <span class="input-icon">🏢</span>
                            <input class="input" id="department" name="department"
                                   value="${u.department}" placeholder="VD: IT, HR, Sale...">
                        </div>
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group" style="max-width:200px;">
                        <label for="status">Trạng thái</label>
                        <div class="input-wrap">
                            <span class="input-icon">📶</span>
                            <select class="input" name="status" id="status">
                                <option value="1" <c:if test="${u.status==1 || u.status=='ACTIVE'}">selected</c:if>>Đang hoạt động</option>
                                <option value="0" <c:if test="${u.status==0 || u.status=='INACTIVE'}">selected</c:if>>Tạm khóa</option>
                            </select>
                        </div>
                    </div>
                    <div class="form-group">
                        <label>Ghi chú nội bộ</label>
                        <div class="input-wrap">
                            <span class="input-icon">📝</span>
                            <input class="input" id="note" name="note"
                                   placeholder="Ví dụ: thử việc, sắp nghỉ, chuyển team...">
                        </div>
                    </div>
                </div>

                <div class="form-footer">
                    <div style="display:flex;gap:.4rem;flex-wrap:wrap;">
                        <button type="submit" class="btn btn-primary" id="submitBtn">
                            💾 Lưu thay đổi
                        </button>
                        <a href="${ctx}/admin/users" class="btn btn-ghost">Hủy</a>
                    </div>
                    <div class="muted-line">⌘+S (hoặc Ctrl+S) để lưu nhanh</div>
                </div>

            </div>
        </form>
    </div>
</div>

<div class="toast" id="toast">Đã lưu!</div>

<script>
    // Đếm ký tự tên
    const nameInput = document.getElementById('fullName');
    const nameLen = document.getElementById('nameLen');
    if (nameInput && nameLen) {
        const updateLen = () => {
            nameLen.textContent = nameInput.value.length + "/80";
        };
        nameInput.addEventListener('input', updateLen);
        updateLen();
    }

    // Ctrl+S để submit
    document.addEventListener('keydown', function (e) {
        if ((e.metaKey || e.ctrlKey) && e.key === 's') {
            e.preventDefault();
            const form = document.getElementById('userEditForm');
            if (form) form.submit();
        }
    });

    // Tự generate avatar từ tên
    (function () {
        const av = document.getElementById('avatarCircle');
        const fullName = document.getElementById('fullName');
        if (!av || !fullName) return;
        const gen = () => {
            const v = fullName.value.trim();
            if (!v) { av.textContent = 'U'; return; }
            const parts = v.split(/\s+/);
            let letters = parts[0].charAt(0).toUpperCase();
            if (parts.length > 1) {
                letters += parts[parts.length - 1].charAt(0).toUpperCase();
            }
            av.textContent = letters;
        };
        fullName.addEventListener('input', gen);
    })();

    // show toast sau redirect ?updated=1
    (function () {
        const params = new URLSearchParams(window.location.search);
        const toast = document.getElementById('toast');
        if (params.get('updated') === '1' && toast) {
            toast.style.display = 'block';
            setTimeout(() => toast.style.display = 'none', 2500);
        }
    })();

    // quick actions
    function fillLeader() {
        document.getElementById('role').value = 'DIV_LEADER';
        showToast('Đã gán quyền Trưởng phòng');
    }
    function fillDept(dep) {
        document.getElementById('department').value = dep;
        showToast('Đã chọn phòng ' + dep);
    }
    function genRandomPass() {
        const pass = Math.random().toString(36).substring(2, 10);
        navigator.clipboard?.writeText(pass);
        showToast('Mật khẩu tạm: ' + pass + ' (đã copy)');
    }
    function showToast(msg) {
        const t = document.getElementById('toast');
        if (!t) return;
        t.textContent = msg;
        t.style.display = 'block';
        setTimeout(() => { t.style.display = 'none'; }, 2300);
    }

    // validate basic
    const form = document.getElementById('userEditForm');
    if (form) {
        form.addEventListener('submit', function (e) {
            const email = document.getElementById('email').value.trim();
            if (!email.includes('@')) {
                e.preventDefault();
                showToast('Email không hợp lệ');
            }
        });
    }
</script>

<%@ include file="/WEB-INF/views/common/_footer.jsp" %>
