<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="csrfParam"  value="${empty requestScope.csrfParam ? '_csrf' : requestScope.csrfParam}" />
<c:set var="csrfToken" value="${requestScope.csrfToken}" />

<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="utf-8"/>
<meta name="viewport" content="width=device-width, initial-scale=1"/>
<title>Hỗ trợ</title>
<style>
:root{--h:64px;--sbw:240px;--bg:#f7f9fc;--card:#fff;--bd:#e5e7eb;--ink:#0f172a;--pri:#0f766e;--muted:#64748b}
*{box-sizing:border-box}
body{margin:0;background:var(--bg);color:var(--ink);font:14px/1.45 system-ui,Segoe UI,Roboto}
.topbar{position:fixed;inset:0 0 auto 0;height:var(--h);display:flex;align-items:center;justify-content:space-between;padding:0 20px;background:#fff;border-bottom:1px solid #e5e7eb}
.topbar .btn{border:1px solid #e5e7eb;border-radius:10px;height:36px;padding:0 12px;background:#fff}
.sidebar{position:fixed;top:var(--h);bottom:0;left:0;width:var(--sbw);background:#111827;color:#cbd5e1;padding:12px 10px;overflow:auto}
.nav a{display:block;padding:10px 12px;border-radius:8px;margin:4px 6px}
.nav a.active,.nav a:hover{background:#1f2937;color:#fff}
.app{padding-top:var(--h);padding-left:var(--sbw)}
.wrap{padding:20px 24px 96px}
.card{background:var(--card);border:1px solid var(--bd);border-radius:14px;padding:14px 16px}
table{width:100%;border-collapse:separate;border-spacing:0;table-layout:fixed}
th,td{padding:10px 12px;border-top:1px solid var(--bd)}
th{background:#f4f4f5;text-align:left;font-size:12px;color:#4b5563}
.badge{display:inline-block;padding:2px 8px;border-radius:999px;font-size:12px;background:#e5e7eb;color:#111827}
.badge.open{background:#fee2e2;color:#991b1b}
.badge.inprog{background:#e0f2fe;color:#075985}
.badge.closed{background:#dcfce7;color:#166534}
.btn2{border:none;border-radius:10px;padding:7px 10px;cursor:pointer}
.btn2.pri{background:#0f766e;color:#fff}
input,select{height:36px;border:1px solid var(--bd);border-radius:10px;padding:0 10px}
.note{color:var(--muted);font-size:12px}
.alert{background:#eef2ff;border:1px solid #c7d2fe;color:#3730a3;border-radius:10px;padding:8px 10px;margin:8px 0;display:inline-flex;gap:8px}
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
    <a href="${ctx}/admin/notifications">Thông báo</a>
    <a href="${ctx}/admin/support" class="active">Hỗ trợ</a>
    <a href="${ctx}/admin/settings">Cấu hình</a>
  </div>
</aside>

<main class="app"><div class="wrap">
  <h2 style="margin:0">Hỗ trợ</h2>
  <div class="note">Quản lý ticket hỗ trợ của người dùng.</div>

  <c:if test="${param.ok == '1'}"><div class="alert">✔ Đã lưu thay đổi</div></c:if>

  <!-- Bộ lọc -->
  <div class="card" style="margin:12px 0">
    <form method="get" action="${ctx}/admin/support" style="display:flex;gap:10px;flex-wrap:wrap">
      <input name="q" placeholder="Từ khoá..." value="<c:out value='${param.q}'/>"/>
      <select name="status">
        <option value="">-- Trạng thái --</option>
        <option value="OPEN"        <c:if test="${param.status=='OPEN'}">selected</c:if>       >OPEN</option>
        <option value="IN_PROGRESS" <c:if test="${param.status=='IN_PROGRESS'}">selected</c:if>>IN_PROGRESS</option>
        <option value="CLOSED"      <c:if test="${param.status=='CLOSED'}">selected</c:if>     >CLOSED</option>
      </select>
      <button class="btn2 pri" type="submit">Lọc</button>
    </form>
  </div>

  <!-- Danh sách ticket -->
  <div class="card">
    <table aria-label="Danh sách ticket hỗ trợ">
      <thead>
        <tr>
          <th style="width:56px">#</th>
          <th>Chủ đề &amp; Nội dung</th>
          <th style="width:220px">Email</th>
          <th style="width:140px">Trạng thái</th>
          <th style="width:200px">Phân công</th>
          <th style="width:160px">Thao tác</th>
        </tr>
      </thead>
      <tbody>
      <c:choose>
        <c:when test="${empty items}">
          <tr><td colspan="6" class="note">Không có ticket nào.</td></tr>
        </c:when>
        <c:otherwise>
          <c:forEach var="t" items="${items}">
            <tr>
              <td><c:out value="${t.id}"/></td>

              <td>
                <div style="font-weight:600"><c:out value="${t.subject}"/></div>
                <div class="note"><c:out value="${t.body}"/></div>
                <c:if test="${not empty t.createdAt}">
                  <div class="note">Tạo lúc: <c:out value="${t.createdAt}"/></div>
                </c:if>
              </td>

              <td><c:out value="${t.email}"/></td>

              <td>
                <span class="badge ${t.status=='OPEN'?'open':(t.status=='CLOSED'?'closed':'inprog')}">
                  <c:out value="${t.status}"/>
                </span>
              </td>

              <td>
                <form method="post" action="${ctx}/admin/support">
                  <input type="hidden" name="${csrfParam}" value="${csrfToken}"/>
                  <input type="hidden" name="action" value="assign"/>
                  <input type="hidden" name="id" value="<c:out value='${t.id}'/>"/>
                  <input name="assignee" placeholder="Người phụ trách" value="<c:out value='${t.assignee}'/>"/>
                  <button class="btn2" type="submit">Gán</button>
                </form>
              </td>

              <td style="display:flex;gap:6px;flex-wrap:wrap">
                <a class="btn2" href="${ctx}/admin/support/detail?id=<c:out value='${t.id}'/>">Chi tiết</a>

                <form method="post" action="${ctx}/admin/support" style="display:inline">
                  <input type="hidden" name="${csrfParam}" value="${csrfToken}"/>
                  <input type="hidden" name="action" value="status"/>
                  <input type="hidden" name="id" value="<c:out value='${t.id}'/>"/>
                  <input type="hidden" name="status" value="${t.status=='CLOSED'?'OPEN':'CLOSED'}"/>
                  <button class="btn2" type="submit">
                    <c:out value="${t.status=='CLOSED' ? 'Mở lại' : 'Đóng'}"/>
                  </button>
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
