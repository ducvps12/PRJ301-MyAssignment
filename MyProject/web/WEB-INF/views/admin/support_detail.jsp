<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="csrfParam" value="${empty requestScope.csrfParam ? '_csrf' : requestScope.csrfParam}" />
<c:set var="csrfToken" value="${requestScope.csrfToken}" />

<!DOCTYPE html>
<html lang="vi"><head><meta charset="utf-8"/><meta name="viewport" content="width=device-width, initial-scale=1"/>
<title>Chi tiết hỗ trợ</title>
<style>
:root{--h:64px;--sbw:240px;--bg:#f7f9fc;--card:#fff;--bd:#e5e7eb;--ink:#0f172a;--muted:#64748b;--pri:#0f766e}
*{box-sizing:border-box}body{margin:0;background:var(--bg);color:var(--ink);font:14px/1.45 system-ui,Segoe UI,Roboto}
.topbar{position:fixed;inset:0 0 auto 0;height:var(--h);display:flex;align-items:center;justify-content:space-between;padding:0 20px;background:#fff;border-bottom:1px solid #e5e7eb}
.topbar .btn{border:1px solid #e5e7eb;border-radius:10px;height:36px;padding:0 12px;background:#fff}
.sidebar{position:fixed;top:var(--h);bottom:0;left:0;width:var(--sbw);background:#111827;color:#cbd5e1;padding:12px 10px;overflow:auto}
.nav a{display:block;padding:10px 12px;border-radius:8px;margin:4px 6px}.nav a.active,.nav a:hover{background:#1f2937;color:#fff}
.app{padding-top:var(--h);padding-left:var(--sbw)}
.wrap{padding:20px 24px 96px}
.card{background:var(--card);border:1px solid var(--bd);border-radius:14px;padding:14px 16px}
.msg{border-left:3px solid #e5e7eb;padding:8px 10px;margin:6px 0;background:#fff}
</style></head>
<body>
<header class="topbar">
  <div><strong>LeaveMgmt • Admin</strong></div>
  <nav style="display:flex;gap:10px">
    <a class="btn" href="${ctx}/admin/support">Quay lại</a>
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
  <h2 style="margin:0">Ticket #${t.id} — ${t.subject}</h2>
  <div style="color:#64748b">Từ: ${t.email} • Trạng thái: <strong>${t.status}</strong> • Phân công: ${empty t.assignee ? '(chưa)' : t.assignee}</div>

  <div class="card" style="margin-top:12px">
    <div class="msg"><strong>Mô tả:</strong><div>${t.body}</div></div>
    <c:forEach var="m" items="${t.replies}">
      <div class="msg">${m}</div>
    </c:forEach>
  </div>

  <form class="card" method="post" action="${ctx}/admin/support/detail" style="margin-top:12px">
    <input type="hidden" name="${csrfParam}" value="${csrfToken}"/>
    <input type="hidden" name="action" value="reply"/>
    <input type="hidden" name="id" value="${t.id}"/>
    <textarea name="message" rows="3" placeholder="Phản hồi..." style="width:100%"></textarea>
    <div style="margin-top:8px;display:flex;gap:8px">
      <button class="btn" style="background:#0f766e;color:#fff;border:none;border-radius:10px;padding:9px 14px">Gửi</button>
      <a class="btn" href="${ctx}/admin/support" style="border:1px solid #e5e7eb;border-radius:10px;padding:9px 14px;background:#fff">Về danh sách</a>
    </div>
  </form>
</div></main>
</body></html>
