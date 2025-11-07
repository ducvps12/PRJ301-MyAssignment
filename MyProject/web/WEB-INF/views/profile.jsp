<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@include file="/WEB-INF/views/common/_taglibs.jsp"%>

<!DOCTYPE html>
<html lang="vi" data-theme="${sessionScope.theme != null ? sessionScope.theme : 'auto'}">
<head>
  <meta charset="UTF-8"/>
  <title>Thông tin cá nhân · LeaveMgmt</title>
  <meta name="viewport" content="width=device-width, initial-scale=1"/>
  <meta name="color-scheme" content="light dark"/>
  <style>
    :root{
      --bg:#f6f8fb; --bg2:#eef2ff; --card:#fff; --tx:#0f172a; --muted:#64748b; --bd:#e5e7eb;
      --pri:#2563eb; --pri-ink:#fff; --ok:#16a34a; --no:#dc2626; --ring:#93c5fd; --shadow:0 12px 28px rgba(2,6,23,.08);
    }
    @media (prefers-color-scheme: dark){
      :root{ --bg:#0b1220; --bg2:#0b1220; --card:#0f172a; --tx:#e5e7eb; --muted:#94a3b8; --bd:#1f2a37; --pri:#3b82f6; --pri-ink:#0b1220; --ring:#60a5fa; --shadow:0 12px 28px rgba(0,0,0,.35); }
    }
    *{box-sizing:border-box}
    body{margin:0;font:14px/1.5 system-ui,Segoe UI,Roboto,Arial;background:var(--bg);color:var(--tx)}
    .wrap{max-width:1100px;margin:24px auto;padding:0 16px}
    .titlebar{display:flex;align-items:center;gap:12px;margin:16px 0 20px}
    .titlebar h1{font-size:20px;margin:0}
    .muted{color:var(--muted)}
    .card{background:var(--card);border:1px solid var(--bd);border-radius:16px;box-shadow:var(--shadow)}
    .profile{display:grid;grid-template-columns:240px 1fr;gap:24px;padding:24px}
    @media (max-width:860px){.profile{grid-template-columns:1fr}}
    .avatar{width:120px;height:120px;border-radius:999px;background:linear-gradient(135deg,var(--bg2),var(--bg));border:1px solid var(--bd);display:flex;align-items:center;justify-content:center;font-weight:700;overflow:hidden}
    .avatar img{width:100%;height:100%;object-fit:cover;display:block}
    .big{font-size:36px}
    label{display:block;font-weight:600;margin:4px 0 6px}
    .info form{display:grid;grid-template-columns:1fr 1fr;gap:16px}
    .info form .full{grid-column:1/-1}
    input,select,textarea{width:100%;padding:10px 12px;border:1px solid var(--bd);border-radius:12px;background:transparent;color:inherit;outline:none}
    input:focus,select:focus,textarea:focus{border-color:var(--ring);box-shadow:0 0 0 3px color-mix(in oklab, var(--ring) 30%, transparent)}
    .row{display:flex;gap:10px;align-items:center;flex-wrap:wrap}
    .chip{display:inline-flex;gap:6px;align-items:center;padding:6px 10px;border-radius:999px;border:1px solid var(--bd);font-size:12px}
    .chip.ok{background:color-mix(in oklab, var(--ok) 12%, transparent);border-color:color-mix(in oklab, var(--ok) 50%, var(--bd))}
    .chipline{display:flex;gap:8px;flex-wrap:wrap;margin-top:8px}
    .actions{margin-top:6px;display:flex;gap:10px}
    .btn{appearance:none;border:none;border-radius:12px;padding:10px 14px;font-weight:600;cursor:pointer}
    .btn.pri{background:var(--pri);color:var(--pri-ink)}
    .btn.ghost{background:transparent;border:1px solid var(--bd)}
    .alert{margin:16px 0;padding:10px 12px;border-radius:12px;border:1px solid var(--bd)}
    .alert.ok{border-color:color-mix(in oklab, var(--ok) 50%, var(--bd));background:color-mix(in oklab, var(--ok) 10%, transparent)}
    .alert.no{border-color:color-mix(in oklab, var(--no) 50%, var(--bd));background:color-mix(in oklab, var(--no) 10%, transparent)}
  </style>
</head>
<body>

  <jsp:include page="/WEB-INF/views/common/_header.jsp"/>

  <main class="wrap">
    <div class="titlebar">
      <h1>Thông tin cá nhân</h1>
      <span class="muted">Quản lý hồ sơ người dùng</span>
    </div>

    <!-- ===== Data flags ===== -->
    <c:set var="u" value="${me}"/>
    <c:set var="isAdmin" value="${sessionScope.currentUser != null && sessionScope.currentUser.role == 'ADMIN'}"/>
    <c:set var="canEditSelf" value="${not empty sessionScope.currentUser}"/>
    <c:set var="canEditFK"   value="${isAdmin}"/>

    <c:set var="s" value="${empty u ? '' : u.status}"/>
    <c:set var="isActive" value="${s == 'ACTIVE' or s == '1' or fn:toLowerCase(s) == 'true'}"/>

    <!-- fallback birthday string -->
    <c:if test="${empty uBirthdayStr and not empty u.birthdayDate}">
      <fmt:formatDate value="${u.birthdayDate}" var="uBirthdayStr" pattern="yyyy-MM-dd"/>
    </c:if>

    <div class="card profile">
      <!-- Left -->
      <div>
        <div class="avatar big" aria-label="Avatar">
          <c:choose>
            <c:when test="${not empty u.avatarUrl}">
              <img src="${u.avatarUrl}" alt="avatar"/>
            </c:when>
            <c:otherwise>
              <c:choose>
                <c:when test="${not empty u.fullName}">${fn:substring(u.fullName,0,1)}</c:when>
                <c:when test="${not empty u.username}">${fn:substring(u.username,0,1)}</c:when>
                <c:otherwise>?</c:otherwise>
              </c:choose>
            </c:otherwise>
          </c:choose>
        </div>

        <div class="muted" style="margin-top:12px">
          <div class="row">
            <span class="chip">ID: #<c:out value="${u.id}"/></span>
            <span class="chip">Tài khoản: <c:out value="${u.username}"/></span>
          </div>
          <div class="chipline">
            <span class="chip ${isActive ? 'ok' : ''}">
              Trạng thái: <strong><c:out value="${isActive ? 'ACTIVE' : (empty s ? 'UNKNOWN' : s)}"/></strong>
            </span>
            <c:if test="${not empty uCreatedAtDate}">
              <span class="chip">Tạo lúc:
                <fmt:formatDate value="${uCreatedAtDate}" pattern="dd/MM/yyyy HH:mm"/>
              </span>
            </c:if>
          </div>

          <c:if test="${not empty positionLabel}">
            <div class="chipline" style="margin-top:6px">
              <span class="chip" title="Vị trí trong tổ chức">
                <svg width="14" height="14" viewBox="0 0 24 24" aria-hidden="true" style="margin-right:6px">
                  <path fill="currentColor" d="M10 3h4v4h-4V3m6 18v-5h4v5h-4M4 21v-9h4v9H4m6 0v-9h4v9h-4z"/>
                </svg>
                <c:out value="${positionLabel}"/>
              </span>
            </div>
          </c:if>
        </div>
      </div>

      <!-- Right (form) -->
      <div class="info">
        <form method="post" action="${pageContext.request.contextPath}/profile" autocomplete="on">
          <input type="hidden" name="_token" value="${sessionScope.csrfToken}"/>

          <div class="full">
            <label>Họ và tên</label>
            <input type="text" name="fullName" value="<c:out value='${u.fullName}'/>" ${canEditSelf ? '' : 'readonly'} required/>
          </div>

          <div>
            <label>Email</label>
            <input type="email" name="email" value="<c:out value='${u.email}'/>" ${canEditSelf ? '' : 'readonly'}/>
          </div>
          <div>
            <label>Điện thoại</label>
            <input name="phone" value="<c:out value='${u.phone}'/>" ${canEditSelf ? '' : 'readonly'}/>
          </div>

          <!-- Department -->
          <div>
            <label>Phòng ban</label>
            <c:choose>
              <c:when test="${canEditFK}">
                <select name="departmentId">
                  <option value="">— Không đổi / Trống —</option>
                  <c:forEach var="d" items="${depts}">
                    <option value="${d.id}" ${d.id == uDeptId ? 'selected' : ''}>
                      <c:out value="${d.code}"/> - <c:out value="${d.name}"/>
                    </option>
                  </c:forEach>
                </select>
              </c:when>
              <c:otherwise>
                <input value="<c:out value='${u.departmentName}'/>" readonly/>
              </c:otherwise>
            </c:choose>
          </div>

          <!-- Role -->
          <div>
            <label>Chức vụ (role)</label>
            <c:choose>
              <c:when test="${canEditFK}">
                <select name="roleId">
                  <option value="">— Không đổi / Trống —</option>
                  <c:forEach var="r" items="${roles}">
                    <option value="${r.id}" ${r.id == uRoleId ? 'selected' : ''}>
                      <c:out value="${r.code}"/> - <c:out value="${r.name}"/>
                    </option>
                  </c:forEach>
                </select>
              </c:when>
              <c:otherwise>
                <input value="<c:out value='${empty u.roleName ? u.role : u.roleName}'/>" readonly/>
              </c:otherwise>
            </c:choose>
          </div>

          <!-- Division -->
          <div>
            <label>Khối/Division</label>
            <c:choose>
              <c:when test="${canEditFK}">
                <select name="divisionId">
                  <option value="">— Không đổi / Trống —</option>
                  <c:forEach var="dv" items="${divisions}">
                    <option value="${dv.id}" ${dv.id == uDivisionId ? 'selected' : ''}>
                      <c:out value="${dv.name}"/>
                    </option>
                  </c:forEach>
                </select>
              </c:when>
              <c:otherwise>
                <input value="<c:out value='${u.divisionName}'/>" readonly/>
              </c:otherwise>
            </c:choose>
          </div>

          <!-- Manager -->
          <div>
            <label>Quản lý trực tiếp</label>
            <c:choose>
              <c:when test="${canEditFK}">
                <select name="managerId">
                  <option value="">— Không đổi / Trống —</option>
                  <c:forEach var="m" items="${managers}">
                    <option value="${m.id}" ${m.id == uManagerId ? 'selected' : ''}>
                      <c:out value="${m.fullName}"/>
                    </option>
                  </c:forEach>
                </select>
              </c:when>
              <c:otherwise>
                <input value="<c:out value='${u.managerName}'/>" readonly/>
              </c:otherwise>
            </c:choose>
          </div>

          <div class="full">
            <label>Địa chỉ</label>
            <input name="address" value="<c:out value='${u.address}'/>" ${canEditSelf ? '' : 'readonly'}/>
          </div>

          <div>
            <label>Ngày sinh</label>
            <input type="date" name="birthday" value="${uBirthdayStr}" ${canEditSelf ? '' : 'readonly'}/>
          </div>

          <div>
            <label>Avatar URL</label>
            <input name="avatarUrl" value="<c:out value='${u.avatarUrl}'/>" ${canEditSelf ? '' : 'readonly'}/>
          </div>

          <div class="full">
            <label>Giới thiệu</label>
            <textarea name="bio" rows="3" ${canEditSelf ? '' : 'readonly'}><c:out value="${u.bio}"/></textarea>
          </div>

          <div class="full actions">
            <c:if test="${canEditSelf}">
              <button class="btn pri" type="submit">Cập nhật</button>
              <a class="btn ghost" href="${pageContext.request.contextPath}/request/list">Quay lại danh sách</a>
            </c:if>
            <c:if test="${!canEditSelf}">
              <a class="btn ghost" href="${pageContext.request.contextPath}/login">Đăng nhập để chỉnh sửa</a>
            </c:if>
          </div>

          <c:if test="${not empty ok}">
            <div class="alert ok full"><c:out value="${ok}"/></div>
          </c:if>
          <c:if test="${not empty error}">
            <div class="alert no full"><c:out value="${error}"/></div>
          </c:if>
          <c:if test="${param.ok == '1'}">
            <div class="alert ok full">Cập nhật thành công!</div>
          </c:if>
        </form>
      </div>
    </div>

    <div class="muted" style="margin:18px 4px">
      Lưu ý: Role/Phòng ban/Division/Manager chỉ admin có thể thay đổi. Người dùng thường chỉ xem.
    </div>
  </main>

  <jsp:include page="/WEB-INF/views/common/_footer.jsp"/>
</body>
</html>

