<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@include file="/WEB-INF/views/common/_taglibs.jsp"%>
<%@include file="/WEB-INF/views/common/_header.jsp"%>

<div class="container" style="max-width:1100px; margin:24px auto;">
  <h2 style="margin:0 0 16px 0;">Quản lý Người dùng</h2>

  <!-- Search / Filter -->
  <form method="get" action="${pageContext.request.contextPath}/admin/users" class="card"
        style="padding:12px;display:grid;grid-template-columns:1fr 200px 120px;gap:10px;align-items:center;">
    <input type="text" name="q" value="${fn:escapeXml(param.q)}" placeholder="Tìm theo tên, email, username…" class="input">
    <select name="status" class="input">
      <option value="">-- Tất cả trạng thái --</option>
      <option value="ACTIVE"   ${param.status == 'ACTIVE'   ? 'selected':''}>ACTIVE</option>
      <option value="INACTIVE" ${param.status == 'INACTIVE' ? 'selected':''}>INACTIVE</option>
    </select>
    <button class="btn">Lọc</button>
  </form>

  <!-- Table -->
  <div class="card" style="margin-top:12px;">
    <div class="card-header">Danh sách người dùng</div>
    <div class="table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>#</th><th>Họ tên</th><th>Email</th><th>Username</th><th>Role</th><th>Phòng ban</th><th>Trạng thái</th><th style="width:220px;">Thao tác</th>
          </tr>
        </thead>
        <tbody>
        <c:forEach var="u" items="${page.data}" varStatus="vs">
          <tr>
            <td>${(page.pageIndex-1)*page.pageSize + vs.index + 1}</td>
            <td>${u.fullName}</td>
            <td>${u.email}</td>
            <td>${u.username}</td>
            <td>${u.role}</td>
            <td>${u.department}</td>
            <td><span class="badge ${u.status}">${u.status}</span></td>
            <td>
              <a class="btn small" href="${pageContext.request.contextPath}/admin/users/detail?id=${u.id}">Xem</a>
              <a class="btn small" href="${pageContext.request.contextPath}/admin/users/edit?id=${u.id}">Sửa</a>

              <!-- Toggle status -->
              <form method="post" action="${pageContext.request.contextPath}/admin/users/toggle" style="display:inline">
                <input type="hidden" name="csrf" value="${csrf}">
                <input type="hidden" name="id" value="${u.id}">
                <input type="hidden" name="q" value="${fn:escapeXml(param.q)}">
                <input type="hidden" name="status" value="${param.status}">
                <input type="hidden" name="page" value="${page.pageIndex}">
                <input type="hidden" name="size" value="${page.pageSize}">
                <button class="btn small" onclick="return confirm('Xác nhận thay đổi trạng thái?')">
                  <c:choose>
                    <c:when test="${u.status=='ACTIVE'}">Vô hiệu</c:when>
                    <c:otherwise>Kích hoạt</c:otherwise>
                  </c:choose>
                </button>
              </form>

              <!-- Reset password (tuỳ chọn) -->
              <form method="post" action="${pageContext.request.contextPath}/admin/users/resetpw" style="display:inline">
                <input type="hidden" name="csrf" value="${csrf}">
                <input type="hidden" name="id" value="${u.id}">
                <input type="hidden" name="q" value="${fn:escapeXml(param.q)}">
                <input type="hidden" name="status" value="${param.status}">
                <input type="hidden" name="page" value="${page.pageIndex}">
                <input type="hidden" name="size" value="${page.pageSize}">
                <button class="btn small" onclick="return confirm('Reset mật khẩu về 123456?')">Reset PW</button>
              </form>
            </td>
          </tr>
        </c:forEach>

        <c:if test="${empty page.data}">
          <tr><td colspan="8" style="text-align:center;opacity:.7;">Không có dữ liệu</td></tr>
        </c:if>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <div style="display:flex;justify-content:center;gap:6px;padding:12px;">
      <c:forEach begin="1" end="${page.totalPages}" var="p">
        <a class="btn small ${p==page.pageIndex?'active':''}"
           href="${pageContext.request.contextPath}/admin/users?page=${p}&size=${page.pageSize}&q=${fn:escapeXml(param.q)}&status=${param.status}">
           ${p}
        </a>
      </c:forEach>
    </div>
  </div>
</div>

<style>
  .input{padding:10px 12px;border:1px solid #e5e7eb;border-radius:10px;width:100%}
  .btn{display:inline-block;padding:8px 12px;border:1px solid #e5e7eb;border-radius:10px;background:#fff;text-decoration:none}
  .btn.small{font-size:12px;padding:6px 10px}
  .btn.active{background:#111827;color:#fff;border-color:#111827}
  .badge{display:inline-block;padding:2px 8px;border-radius:20px;border:1px solid #e5e7eb}
  .badge.ACTIVE{background:#ecfdf5;border-color:#a7f3d0}
  .badge.INACTIVE{background:#fef2f2;border-color:#fecaca}
</style>

<%@include file="/WEB-INF/views/common/_footer.jsp"%>
