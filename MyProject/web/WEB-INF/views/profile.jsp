<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp" %>
<!DOCTYPE html>
<html lang="vi" data-theme="${sessionScope.theme != null ? sessionScope.theme : 'auto'}">
<head>
  <meta charset="UTF-8">
  <title>Thông tin cá nhân · LeaveMgmt</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <meta name="color-scheme" content="light dark">
  <style>
    :root{
      --bg:#f6f8fb; --bg2:#eef2ff; --card:#ffffff; --tx:#0f172a; --muted:#64748b; --bd:#e5e7eb;
      --pri:#2563eb; --pri-ink:#fff; --ok:#16a34a; --warn:#d97706; --no:#dc2626; --ring:#93c5fd;
      --shadow:0 12px 28px rgba(2,6,23,.08);
    }
    @media (prefers-color-scheme: dark){
      :root{
        --bg:#0b1220; --bg2:#0b1220; --card:#0f172a; --tx:#e5e7eb; --muted:#94a3b8; --bd:#1f2a37;
        --pri:#3b82f6; --pri-ink:#0b1220; --ring:#60a5fa; --shadow:0 12px 28px rgba(0,0,0,.35);
      }
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
    .avatar{width:120px;height:120px;border-radius:999px;background:linear-gradient(135deg,var(--bg2),var(--bg));
      border:1px solid var(--bd);display:flex;align-items:center;justify-content:center;font-weight:700;overflow:hidden}
    .avatar img{width:100%;height:100%;object-fit:cover;display:block}
    .big{font-size:36px}
    .info form{display:grid;grid-template-columns:1fr 1fr;gap:16px}
    .info form .full{grid-column:1/-1}
    label{display:block;font-weight:600;margin:4px 0 6px}
    input,select,textarea{width:100%;padding:10px 12px;border:1px solid var(--bd);border-radius:12px;background:transparent;color:inherit;outline:none}
    input:focus,select:focus,textarea:focus{border-color:var(--ring);box-shadow:0 0 0 3px color-mix(in oklab, var(--ring) 30%, transparent)}
    .row{display:flex;gap:10px;align-items:center;flex-wrap:wrap}
    .chip{display:inline-flex;align-items:center;gap:6px;padding:6px 10px;border-radius:999px;border:1px solid var(--bd);font-size:12px}
    .chip.ok{background:color-mix(in oklab, var(--ok) 12%, transparent);border-color:color-mix(in oklab, var(--ok) 50%, var(--bd))}
    .actions{margin-top:6px;display:flex;gap:10px}
    .btn{appearance:none;border:none;border-radius:12px;padding:10px 14px;font-weight:600;cursor:pointer}
    .btn.pri{background:var(--pri);color:var(--pri-ink)}
    .btn.ghost{background:transparent;border:1px solid var(--bd)}
    .alert{margin:16px 0;padding:10px 12px;border-radius:12px;border:1px solid var(--bd)}
    .alert.ok{border-color:color-mix(in oklab, var(--ok) 50%, var(--bd));background:color-mix(in oklab, var(--ok) 10%, transparent)}
    .alert.no{border-color:color-mix(in oklab, var(--no) 50%, var(--bd));background:color-mix(in oklab, var(--no) 10%, transparent)}
    .readonly input, .readonly select, .readonly textarea{background:color-mix(in oklab, var(--bg) 80%, transparent)}
  </style>
</head>
<body>

  <jsp:include page="/WEB-INF/views/common/_header.jsp"/>

  <main class="wrap">
    <div class="titlebar">
      <h1>Thông tin cá nhân</h1>
      <span class="muted">Quản lý hồ sơ người dùng</span>
    </div>

    <c:set var="u" value="${me}"/>
    <c:set var="canEdit" value="${canEdit}"/>
    <c:set var="ro" value="${!canEdit ? 'readonly' : ''}"/>
    <c:set var="isAdmin" value="${sessionScope.currentUser != null && sessionScope.currentUser.role == 'ADMIN'}"/>

    <c:set var="s" value="${u.status}"/>
    <c:set var="isActive" value="${s == 'ACTIVE' or s == '1' or fn:toLowerCase(s) == 'true'}"/>

    <div class="card profile ${!canEdit ? 'readonly' : ''}">
      <div>
        <div class="avatar big">
          <c:choose>
            <c:when test="${not empty u.avatarUrl}">
              <img src="${u.avatarUrl}" alt="avatar">
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
            <span class="chip">ID: #${u.id}</span>
            <span class="chip">Tài khoản: ${u.username}</span>
          </div>
          <div class="row" style="margin-top:8px">
            <span class="chip ${isActive ? 'ok' : ''}">
              Trạng thái: <strong><c:out value="${isActive ? 'ACTIVE' : (empty s ? 'UNKNOWN' : s)}"/></strong>
            </span>
            <c:if test="${not empty u.createdAtDate}">
              <span class="chip">Tạo lúc: <fmt:formatDate value="${u.createdAtDate}" pattern="dd/MM/yyyy HH:mm"/></span>
            </c:if>
          </div>
        </div>
      </div>

      <div class="info">
        <form method="post" action="${pageContext.request.contextPath}/profile" autocomplete="on">
          <!-- CSRF -->
          <input type="hidden" name="_token" value="${sessionScope.csrfToken}"/>

          <div class="full">
            <label>Họ và tên</label>
            <input type="text" name="fullName" value="${u.fullName}" ${ro} required/>
          </div>

          <div>
            <label>Email</label>
            <input type="email" name="email" value="${u.email}" ${ro}/>
          </div>
          <div>
            <label>Điện thoại</label>
            <input name="phone" value="${u.phone}" ${ro}/>
          </div>

          <!-- DEPARTMENT (FK: có cả tên & id) -->
          <div>
            <label>Phòng ban</label>
            <select name="departmentId" ${!canEdit ? 'disabled' : ''}>
              <option value="">— Không đổi / Trống —</option>
              <c:forEach var="d" items="${depts}">
                <option value="${d.id}" ${u.departmentId == d.id ? 'selected' : ''}>
                  ${d.code} - ${d.name}
                </option>
              </c:forEach>
            </select>
          </div>

          <!-- ROLE (FK role_id) -->
          <div>
            <label>Chức vụ (role)</label>
            <c:choose>
              <c:when test="${canEdit && isAdmin}">
                <select name="roleId">
                  <option value="">— Không đổi / Trống —</option>
                  <c:forEach var="r" items="${roles}">
                    <option value="${r.id}" ${u.roleId == r.id ? 'selected' : ''}>
                      ${r.code} - ${r.name}
                    </option>
                  </c:forEach>
                </select>
              </c:when>
              <c:otherwise>
                <input value="${u.role}" readonly/>
              </c:otherwise>
            </c:choose>
          </div>

          <!-- DIVISION (FK) -->
          <div>
            <label>Khối/Division</label>
            <select name="divisionId" ${!canEdit ? 'disabled' : ''}>
              <option value="">— Không đổi / Trống —</option>
              <c:forEach var="dv" items="${divisions}">
                <option value="${dv.id}" ${u.divisionId == dv.id ? 'selected' : ''}>${dv.name}</option>
              </c:forEach>
            </select>
          </div>

          <!-- MANAGER (FK Users.id) -->
          <div>
            <label>Quản lý trực tiếp</label>
            <select name="managerId" ${!canEdit ? 'disabled' : ''}>
              <option value="">— Không đổi / Trống —</option>
              <c:forEach var="m" items="${managers}">
                <option value="${m.id}" ${u.managerId == m.id ? 'selected' : ''}>${m.fullName}</option>
              </c:forEach>
            </select>
          </div>

          <div class="full">
            <label>Địa chỉ</label>
            <input name="address" value="${u.address}" ${ro}/>
          </div>

          <div>
            <label>Ngày sinh</label>
            <c:choose>
              <c:when test="${not empty u.birthday}">
                <input type="date" name="birthday" value="${u.birthday}" ${ro}/>
              </c:when>
              <c:when test="${not empty u.birthdayDate}">
                <input type="date" name="birthday"
                       value="<fmt:formatDate value='${u.birthdayDate}' pattern='yyyy-MM-dd'/>" ${ro}/>
              </c:when>
              <c:otherwise>
                <input type="date" name="birthday" ${ro}/>
              </c:otherwise>
            </c:choose>
          </div>

          <div>
            <label>Lương cơ bản (decimal)</label>
            <input type="number" step="0.01" name="salary" value="${u.salary}" ${ro}/>
          </div>

          <div>
            <label>Avatar URL</label>
            <input name="avatarUrl" value="${u.avatarUrl}" ${ro}/>
          </div>

          <div class="full">
            <label>Giới thiệu</label>
            <textarea name="bio" rows="3" ${ro}>${u.bio}</textarea>
          </div>

          <div class="full actions">
            <c:if test="${canEdit}">
              <button class="btn pri" type="submit">Cập nhật</button>
              <a class="btn ghost" href="${pageContext.request.contextPath}/request/list">Quay lại danh sách</a>
            </c:if>
            <c:if test="${!canEdit}">
              <a class="btn ghost" href="${pageContext.request.contextPath}/login">Đăng nhập để chỉnh sửa</a>
            </c:if>
          </div>

          <c:if test="${not empty ok}">
            <div class="alert ok full">${ok}</div>
          </c:if>
          <c:if test="${not empty error}">
            <div class="alert no full">${error}</div>
          </c:if>
          <c:if test="${param.ok == '1'}">
            <div class="alert ok full">Cập nhật thành công!</div>
          </c:if>
        </form>
      </div>
    </div>

    <div class="muted" style="margin:18px 4px">
      Lưu ý: Form gửi các trường FK: <code>departmentId</code>, <code>roleId</code>, <code>divisionId</code>, <code>managerId</code>.
      Nếu để trống sẽ không cập nhật cột đó (DAO cần dùng <i>CASE WHEN ? IS NULL THEN col ELSE ? END</i> hoặc bỏ set cột).
    </div>
  </main>

  <jsp:include page="/WEB-INF/views/common/_footer.jsp"/>
</body>
</html>
