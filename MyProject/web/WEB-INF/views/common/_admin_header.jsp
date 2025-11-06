<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ include file="/WEB-INF/views/common/_admin_sidebar.jsp" %>

<div class="main">
  <!-- ======= TOPBAR / HEADER ======= -->
  <style>
    .h-left,.h-right{display:flex;align-items:center;gap:10px}
    .brand{font-weight:800}
    .search{display:flex;align-items:center;gap:8px;border:1px solid var(--bd);background:var(--card);border-radius:10px;padding:6px 10px;min-width:280px}
    .search input{border:0;background:transparent;outline:none;width:100%}
    .kbd{border:1px solid var(--bd);padding:0 6px;border-radius:6px;font-size:12px;color:var(--muted)}
    .dd{position:relative}
    .dd-menu{position:absolute;right:0;top:calc(100% + 8px);min-width:260px;background:var(--card);
      border:1px solid var(--bd);border-radius:12px;padding:8px;box-shadow:0 10px 30px rgba(0,0,0,.08);display:none;z-index:30}
    .dd.open .dd-menu{display:block}
    .dd-menu a{display:block;padding:8px 10px;border-radius:8px}
    .dd-menu a:hover{background:rgba(0,0,0,.03)}
    .u-chip{display:flex;align-items:center;gap:8px;border:1px solid var(--bd);border-radius:999px;background:var(--card);padding:6px 10px;cursor:pointer}
    .u-ava{width:26px;height:26px;border-radius:50%;background:#c7d2fe;display:inline-block}
    .breadcrumbs{display:flex;align-items:center;gap:6px;color:var(--muted);font-size:13px}
    .breadcrumbs a{color:inherit}
    .breadcrumbs .now{color:var(--tx);font-weight:600}
    @media (max-width:980px){ .search{display:none} }
  </style>

  <div class="topbar">
    <div class="h-left">
      <button class="btn" title="Mở/đóng menu" onclick="toggleSidebar()">☰</button>
      <div class="brand"><a href="${ctx}/admin">LeaveMgmt Admin</a></div>

      <!-- Breadcrumbs: truyền List<String> vào request.setAttribute("bc", ...) -->
      <c:if test="${not empty bc}">
        <div class="breadcrumbs">
          <c:forEach var="b" items="${bc}" varStatus="s">
            <c:choose>
              <c:when test="${s.last}">
                <span class="now">${b}</span>
              </c:when>
              <c:otherwise>
                <a href="javascript:history.back()">${b}</a><span>/</span>
              </c:otherwise>
            </c:choose>
          </c:forEach>
        </div>
      </c:if>
    </div>

    <div class="search">
      <input id="globalSearch" type="search" placeholder="Tìm người, đơn nghỉ, phòng ban…" />
      <span class="kbd">/</span>
    </div>

    <div class="h-right">
      <button class="btn" title="Làm mới" onclick="location.reload()">⟳</button>
      <button class="btn" title="In trang" onclick="window.print()">🖨</button>
      <button class="btn" id="btnTheme" title="Dark / Light">🌓</button>

      <div class="dd" id="ddNotif">
        <button class="btn" title="Thông báo">🔔</button>
        <div class="dd-menu">
          <div style="font-weight:700;padding:6px 6px 8px">Thông báo</div>
          <div style="height:1px;background:var(--bd);margin:6px 0"></div>
          <c:forEach var="n" items="${notifications}">
            <a href="${ctx}/admin/notifications#${n.id}">
              <b>${n.title}</b><br>
              <small class="muted"><fmt:formatDate value="${n.createdAt}" pattern="dd/MM/yyyy HH:mm"/></small>
            </a>
          </c:forEach>
          <c:if test="${empty notifications}">
            <div class="muted" style="padding:6px 10px">Không có thông báo mới.</div>
          </c:if>
          <div style="height:1px;background:var(--bd);margin:6px 0"></div>
          <a href="${ctx}/admin/notifications">Xem tất cả →</a>
        </div>
      </div>

      <div class="dd" id="ddUser">
        <div class="u-chip">
          <span class="u-ava"></span>
          <div style="line-height:1.1">
            <b><c:out value="${sessionScope.currentUser != null ? sessionScope.currentUser.full_name : 'Guest'}"/></b>
            <div class="muted" style="font-size:12px"><c:out value="${sessionScope.currentUser != null ? sessionScope.currentUser.role : ''}"/></div>
          </div>
        </div>
        <div class="dd-menu" style="min-width:220px">
          <div style="padding:6px 8px">
            <b><c:out value="${sessionScope.currentUser.full_name}"/></b><br>
            <small class="muted"><c:out value="${sessionScope.currentUser.email}"/></small>
          </div>
          <div style="height:1px;background:var(--bd);margin:6px 0"></div>
          <a href="${ctx}/account/profile">Hồ sơ</a>
          <a href="${ctx}/account/security">Bảo mật</a>
          <div style="height:1px;background:var(--bd);margin:6px 0"></div>
          <a href="${ctx}/logout">Đăng xuất</a>
        </div>
      </div>
    </div>
  </div>
  <!-- ======= /TOPBAR ======= -->

  <!-- Phần content trang của bạn tiếp tục ở dưới -->
  <div class="content">
    <!-- ... -->
