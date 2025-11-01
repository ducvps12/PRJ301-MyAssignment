<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%
    com.acme.leavemgmt.model.User u =
            (com.acme.leavemgmt.model.User) request.getAttribute("u");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Chi tiết người dùng #${u.id}</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <style>
        :root {
            --bg: #f7f7f8;
            --card: #fff;
            --b: #e5e7eb;
            --m: #6b7280;
            --ok: #10b981;
            --no: #ef4444;
            --info: #3b82f6;
        }
        body {font-family: system-ui, -apple-system, Segoe UI, Roboto, Arial, sans-serif;
              background: var(--bg); margin: 0; color: #111827;}
        .wrap {max-width: 920px; margin: 24px auto; padding: 0 16px;}
        .card {background: var(--card); border: 1px solid var(--b); border-radius: 14px; padding: 18px;}
        .grid {display: grid; grid-template-columns: 180px 1fr; gap: 10px 18px;}
        .muted {color: var(--m);}
        .badge {display: inline-block; padding: 3px 10px; border-radius: 999px; font-size: 12px;}
        .badge-role-admin {background: #fef9c3; color: #92400e;}
        .badge-role-lead {background: #eff6ff; color: #1d4ed8;}
        .badge-role-staff {background: #f3f4f6; color: #374151;}
        .badge-status-active {background: #d1fae5; color: #065f46;}
        .badge-status-inactive {background: #fee2e2; color: #991b1b;}
        .actions {display: flex; gap: 10px; margin-top: 16px; flex-wrap: wrap;}
        .btn {border: 1px solid var(--b); background: #fff; border-radius: 10px; padding: 8px 12px; cursor: pointer; text-decoration: none; color: inherit;}
        .btn:hover {background: #f3f4f6;}
        .btn-danger {border-color: var(--no); color: #b91c1c;}
        .btn-success {border-color: var(--ok); color: #065f46;}
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
                <c:choose>
                    <c:when test="${u.role == 'ADMIN'}">
                        <span class="badge badge-role-admin">${u.role}</span>
                    </c:when>
                    <c:when test="${u.role == 'DIV_LEADER' || u.role == 'TEAM_LEADER'}">
                        <span class="badge badge-role-lead">${u.role}</span>
                    </c:when>
                    <c:otherwise>
                        <span class="badge badge-role-staff">${u.role}</span>
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="muted">Phòng ban</div><div>${u.department}</div>

            <div class="muted">Trạng thái</div>
            <div>
                <c:choose>
                    <c:when test="${u.status == 1}">
                        <span class="badge badge-status-active">ACTIVE</span>
                    </c:when>
                    <c:otherwise>
                        <span class="badge badge-status-inactive">INACTIVE</span>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <div class="actions">
            <!-- bật/tắt -->
            <form method="post" action="${pageContext.request.contextPath}/admin/users/toggle">
                <input type="hidden" name="id" value="${u.id}"/>
                <c:choose>
                    <c:when test="${u.status == 1}">
                        <button type="submit" class="btn btn-danger">Vô hiệu hóa</button>
                    </c:when>
                    <c:otherwise>
                        <button type="submit" class="btn btn-success">Kích hoạt</button>
                    </c:otherwise>
                </c:choose>
            </form>

            <!-- reset pw -->
            <form method="post" action="${pageContext.request.contextPath}/admin/users/resetpw"
                  onsubmit="return confirm('Reset mật khẩu về mặc định cho user #${u.id}?');">
                <input type="hidden" name="id" value="${u.id}"/>
                <button type="submit" class="btn">Reset mật khẩu</button>
            </form>

            <a class="btn" href="${pageContext.request.contextPath}/admin/users/edit?id=${u.id}">Sửa</a>
            <a class="btn" href="${pageContext.request.contextPath}/admin/users">Quay lại danh sách</a>
        </div>
    </div>
</div>
</body>
</html>
