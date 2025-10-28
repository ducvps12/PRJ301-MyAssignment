<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<%
  com.acme.leavemgmt.model.User u = (com.acme.leavemgmt.model.User) request.getAttribute("u");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<title>Chi tiết người dùng #${u.id}</title>
<meta name="viewport" content="width=device-width, initial-scale=1">
<style>
  :root{--bg:#f7f7f8;--card:#fff;--b:#e5e7eb;--m:#6b7280;--ok:#10b981;--no:#ef4444;--info:#3b82f6}
  body{font-family:system-ui,Segoe UI,Roboto,Arial,sans-serif;background:var(--bg);margin:0;color:#111827}
  .wrap{max-width:920px;margin:24px auto;padding:0 16px}
  .card{background:var(--card);border:1px solid var(--b);border-radius:14px;padding:18px}
  .grid{display:grid;grid-template-columns:180px 1fr;gap:10px 18px}
  .muted{color:var(--m)}
  .badge{display:inline-block;padding:2px 8px;border-radius:999px;border:1px solid var(--b);font-size:12px}
  .admin{border-color:#fde68a;background:#fef9c3}
  .lead{border-color:#bfdbfe;background:#eff6ff}
  .staff{border-color:#e5e7eb;background:#f9fafb}
  .row{margin:8px 0}
  .btn{border:1px solid var(--b);background:#fff;border-radius:10px;padding:8px 12px;cursor:pointer}
  .btn:hover{background:#f3f4f6}
  .btn-no{border-color:var(--no);color:#b91c1c}
  .btn-ok{border-color:var(--ok);color:#065f46}
  .actions{display:flex;gap:10px;margin-top:14px}
</style>
</head>
<body>
<div class="wrap">
  <h2>Người dùng #${u.id}</h2>
  <div class="card">
    <div class="grid">
      <div class="muted">Username</div><div>${u.username}</div>
      <div class="muted">Họ tên</div><div>${u.fullName}</div>
      <div class="muted">Email</div><div>${u.email}</div>
      <div class="muted">Role</div>
      <div>
<c:set var="rc" value="${u.roleCode}" />
<span class="badge ${rc == 'ADMIN' 
                     ? 'admin' 
                     : (fn:endsWith(rc, '_LEAD') or fn:endsWith(rc, '_LEADER') 
                        ? 'lead' 
                        : 'staff')}">
  ${u.roleCode}
</span>

      </div>
      <div class="muted">Phòng ban</div><div>${u.department}</div>
      <div class="muted">Trạng thái</div>
      <div>
        <span class="badge ${u.active ? 'admin' : 'staff'}">${u.statusText}</span>
      </div>
    </div>

    <div class="actions">
      <form method="post" action="${pageContext.request.contextPath}/admin/users/toggle">
        <input type="hidden" name="id" value="${u.id}">
        <button class="btn ${u.active ? 'btn-no' : 'btn-ok'}" type="submit">
          ${u.active ? 'Vô hiệu hóa' : 'Kích hoạt'}
        </button>
      </form>

      <form method="post" action="${pageContext.request.contextPath}/admin/users/resetpw"
            onsubmit="return confirm('Reset mật khẩu về mặc định cho user #${u.id}?');">
        <input type="hidden" name="id" value="${u.id}">
        <button class="btn" type="submit">Reset mật khẩu</button>
      </form>

      <a class="btn" href="${pageContext.request.contextPath}/admin/users/edit?id=${u.id}">Sửa</a>
      <a class="btn" href="${pageContext.request.contextPath}/admin/users">Quay lại danh sách</a>
    </div>
  </div>
</div>
</body>
</html>
