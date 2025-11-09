<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="csrfParam" value="${empty requestScope.csrfParam ? '_csrf' : requestScope.csrfParam}" />
<c:set var="csrfToken" value="${requestScope.csrfToken}" />

<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="utf-8"/>
<meta name="viewport" content="width=device-width, initial-scale=1"/>
<title>Thông báo</title>
<style>
:root{ --h:64px; --sbw:240px; --bg:#f7f9fc; --card:#fff; --ink:#0f172a; --bd:#e5e7eb; --muted:#64748b; --pri:#0f766e; }
*{box-sizing:border-box}
body{margin:0;background:var(--bg);color:var(--ink);font:14px/1.45 system-ui,Segoe UI,Roboto}
.topbar{position:fixed;inset:0 0 auto 0;height:var(--h);display:flex;align-items:center;justify-content:space-between;padding:0 20px;background:#fff;border-bottom:1px solid #e5e7eb}
.topbar .btn{border:1px solid #e5e7eb;border-radius:10px;height:36px;padding:0 12px;background:#fff}
.sidebar{position:fixed;top:var(--h);bottom:0;left:0;width:var(--sbw);background:#111827;color:#cbd5e1;padding:12px 10px;overflow:auto}
.nav a{display:block;padding:10px 12px;border-radius:8px;margin:4px 6px}
.nav a.active,.nav a:hover{background:#1f2937;color:#fff}
.app{padding-top:var(--h);padding-left:var(--sbw);min-height:100vh}
.wrap{padding:20px 24px 96px}
h2{margin:0 0 6px}
.note{color:var(--muted);font-size:12px}
.card{background:var(--card);border:1px solid var(--bd);border-radius:14px;padding:14px 16px}
.table{width:100%;border-collapse:separate;border-spacing:0;table-layout:fixed}
.table th,.table td{padding:10px 12px;border-top:1px solid var(--bd)}
.table th{background:#f4f4f5;text-align:left;font-size:12px;color:#4b5563}
.badge{display:inline-block;padding:2px 8px;border-radius:999px;font-size:12px;background:#e5e7eb;color:#111827}
.badge.read{background:#dcfce7;color:#166534}
.btn2{border:none;border-radius:10px;padding:7px 10px;cursor:pointer}
.btn2.pri{background:#0f766e;color:#fff}
.actions{display:flex;gap:6px;flex-wrap:wrap}
input,textarea{width:100%;border:1px solid #e5e7eb;border-radius:10px;padding:8px 10px}
.alert{background:#ecfdf5;border:1px solid #a7f3d0;color:#065f46;border-radius:10px;padding:8px 10px;margin:8px 0;display:inline-flex;gap:8px;align-items:center}
</style>
</head>
<body>
<header class="topbar">
  <div><strong>LeaveMgmt • Admin</strong></div>
  <nav style="display:flex;gap:10px">
    <a class="btn" href="${ctx}/admin/settings">Cấu hình</a>
    <a class="btn" href="${ctx}/logout">Đăng xuất</a>
  </nav>
</header>

<aside class="sidebar">
  <div class="nav">
    <a href="${ctx}/admin/dashboard">Tổng quan</a>
    <a href="${ctx}/admin/users">Người dùng</a>
    <a href="${ctx}/admin/notifications" class="active">Thông báo</a>
    <a href="${ctx}/admin/support">Hỗ trợ</a>
    <a href="${ctx}/admin/settings">Cấu hình</a>
  </div>
</aside>

<main class="app"><div class="wrap">
  <h2>Thông báo</h2>
  <div class="note">Tạo / xoá / đánh dấu đã đọc. Dữ liệu lấy từ bảng <code>Notifications</code>.</div>

  <c:if test="${param.ok == '1'}">
    <div class="alert">✔ Đã lưu thay đổi</div>
  </c:if>

  <!-- Tạo mới -->
  <form class="card" style="margin:12px 0" method="post" action="${ctx}/admin/notifications">
    <input type="hidden" name="${csrfParam}" value="${csrfToken}"/>
    <input type="hidden" name="action" value="create"/>
    <div style="display:grid;grid-template-columns:1fr 1fr;gap:10px">
      <div><input name="title" placeholder="Tiêu đề" required/></div>
      <div><input name="link_url" placeholder="Link (tuỳ chọn): https://..." /></div>
    </div>
    <textarea name="content" rows="3" placeholder="Nội dung..." style="margin-top:8px"></textarea>
    <div style="margin-top:8px"><button class="btn2 pri" type="submit">➕ Tạo</button></div>
  </form>

  <!-- Danh sách -->
  <div class="card">
    <table class="table" aria-label="Danh sách thông báo">
      <thead>
        <tr>
          <th style="width:56px">#</th>
          <th>Tiêu đề &amp; Nội dung</th>
          <th style="width:200px">Link</th>
          <th style="width:120px">Trạng thái</th>
          <th style="width:190px">Thao tác</th>
        </tr>
      </thead>
      <tbody>
      <c:choose>
        <c:when test="${empty items}">
          <tr>
            <td colspan="5"><span class="note">Chưa có thông báo nào.</span></td>
          </tr>
        </c:when>
        <c:otherwise>
          <c:forEach var="n" items="${items}">
            <tr>
              <td><c:out value="${n.id}"/></td>
              <td>
                <div style="font-weight:600"><c:out value="${n.title}"/></div>
                <div class="note"><c:out value="${n.body}"/></div>
                <c:if test="${not empty n.createdAt}">
                  <div class="note">Tạo lúc: <c:out value="${n.createdAt}"/></div>
                </c:if>
              </td>
              <td>
                <c:choose>
                  <c:when test="${not empty n.linkUrl}">
                    <a href="<c:out value='${n.linkUrl}'/>" target="_blank" rel="noopener" style="color:#0f766e">
                      <c:out value="${n.linkUrl}"/>
                    </a>
                  </c:when>
                  <c:otherwise><span class="note">—</span></c:otherwise>
                </c:choose>
              </td>
              <td>
                <span class="badge ${n.read ? 'read' : ''}">
                  <c:out value="${n.read ? 'Đã đọc' : 'Chưa đọc'}"/>
                </span>
              </td>
              <td class="actions">
                <form method="post" action="${ctx}/admin/notifications" style="display:inline">
                  <input type="hidden" name="${csrfParam}" value="${csrfToken}"/>
                  <input type="hidden" name="action" value="mark"/>
                  <input type="hidden" name="id" value="${n.id}"/>
                  <input type="hidden" name="read" value="${n.read ? '0' : '1'}"/>
                  <button class="btn2" type="submit">
                    <c:out value="${n.read ? 'Đánh dấu chưa đọc' : 'Đánh dấu đã đọc'}"/>
                  </button>
                </form>

                <form method="post" action="${ctx}/admin/notifications"
                      onsubmit="return confirm('Xoá thông báo này?')" style="display:inline">
                  <input type="hidden" name="${csrfParam}" value="${csrfToken}"/>
                  <input type="hidden" name="action" value="delete"/>
                  <input type="hidden" name="id" value="${n.id}"/>
                  <button class="btn2" type="submit">🗑 Xoá</button>
                </form>
              </td>
            </tr>
          </c:forEach>
        </c:otherwise>
      </c:choose>
      </tbody>
    </table>
  </div>
</div></main>
</body>
</html>
 