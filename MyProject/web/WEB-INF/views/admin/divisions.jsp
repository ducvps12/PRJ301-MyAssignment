<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="vi" data-theme="${sessionScope.theme != null ? sessionScope.theme : 'auto'}">
<head>
  <meta charset="UTF-8">
  <title>Phòng ban · LeaveMgmt</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/divi_admin.css?v=5">
</head>
<body>
  <div class="app">
    <aside class="sidebar">
      <div class="brand">
        <div class="logo">🏢</div>
        <div class="brand-text"><strong>LeaveMgmt</strong><small>HR Console</small></div>
        <button class="sidebar-toggle" id="sidebarToggle">⟷</button>
      </div>
      <nav class="nav">
        <a class="nav-item" href="${pageContext.request.contextPath}/admin/hr">🏠 Dashboard</a>
        <a class="nav-item" href="${pageContext.request.contextPath}/admin/users">👥 Nhân sự</a>
        <a class="nav-item" href="${pageContext.request.contextPath}/admin/requests">📝 Yêu cầu nghỉ</a>
        <a class="nav-item active" href="${pageContext.request.contextPath}/admin/divisions">🏢 Phòng ban</a>
      </nav>
      <div class="sidebar-footer"><div class="muted">Quản lý cơ cấu tổ chức</div></div>
    </aside>

    <main class="main">
      <header class="header">
        <div class="left">
          <h1>Phòng ban</h1>
          <span class="chip">Quản trị</span>
        </div>
        <div class="right">
          <form method="get" class="search" action="${pageContext.request.contextPath}/admin/divisions">
            <input type="search" name="q" value="${fn:escapeXml(param.q)}" placeholder="Tìm theo mã/tên…">
            <button type="button" id="clearQ">✕</button>
          </form>
          <button id="btnCreate" class="icon-btn" title="Thêm phòng ban">＋</button>
        </div>
      </header>

      <!-- Alerts -->
      <c:if test="${param.msg=='created'}"><div class="card">✅ Đã tạo phòng ban.</div></c:if>
      <c:if test="${param.msg=='updated'}"><div class="card">✅ Đã cập nhật phòng ban.</div></c:if>
      <c:if test="${param.msg=='deleted'}"><div class="card">✅ Đã xóa (ẩn) phòng ban.</div></c:if>
      <c:if test="${param.err=='cannot_delete_has_users'}"><div class="card" style="border-color:#ef4444">⚠️ Không thể xóa vì còn nhân sự thuộc phòng ban này.</div></c:if>

      <section class="panel">
        <div class="panel-head">
          <h2>Danh sách</h2>
          <form method="get" class="actions" action="${pageContext.request.contextPath}/admin/divisions">
            <input type="hidden" name="q" value="${fn:escapeXml(param.q)}">
            <c:set var="sz" value="${empty param.size ? 10 : (param.size + 0)}"/>
            <select name="size" onchange="this.form.submit()">
              <option value="10" ${sz==10 ? 'selected="selected"' : ''}>10 hàng</option>
              <option value="20" ${sz==20 ? 'selected="selected"' : ''}>20 hàng</option>
              <option value="50" ${sz==50 ? 'selected="selected"' : ''}>50 hàng</option>
            </select>
          </form>
        </div>

        <div class="table-wrap">
          <table class="table">
            <thead>
            <tr><th>Mã</th><th>Tên phòng ban</th><th>Trạng thái</th><th style="width:140px;">Thao tác</th></tr>
            </thead>
            <tbody>
            <c:forEach items="${items}" var="d">
              <tr data-id="${d.id}"
                  data-code="${d.code}"
                  data-name="${d.name}"
                  data-status="${d.isActive ? 'ACTIVE' : 'INACTIVE'}">
                <td>${d.code}</td>
                <td>${d.name}</td>
                <td><span class="chip">${d.isActive ? 'ACTIVE' : 'INACTIVE'}</span></td>
                <td>
                  <button type="button" class="btn btn-edit" data-id="${d.id}">Sửa</button>
                  <form method="post" action="${pageContext.request.contextPath}/admin/divisions" style="display:inline"
                        onsubmit="return confirm('Xóa (ẩn) phòng ban này?');">
                    <input type="hidden" name="action" value="delete">
                    <input type="hidden" name="id" value="${d.id}">
                    <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                    <button type="submit" class="btn">Xóa</button>
                  </form>
                </td>
              </tr>
            </c:forEach>

            <c:if test="${empty items}">
              <tr><td colspan="4" class="muted">Không có dữ liệu.</td></tr>
            </c:if>
            </tbody>
          </table>
        </div>

        <!-- Pager -->
        <c:set var="page" value="${empty param.page ? 1 : (param.page + 0)}"/>
        <c:set var="size" value="${empty param.size ? 10 : (param.size + 0)}"/>
        <c:set var="pages" value="${(total/size) + (total%size>0 ? 1 : 0)}"/>
        <div class="pager">
          <a class="btn"
             href="${pageContext.request.contextPath}/admin/divisions?q=${fn:escapeXml(param.q)}&size=${size}&page=${page-1}"
             ${page<=1 ? 'style="pointer-events:none;opacity:.5"' : ''}>‹ Trước</a>
          <span>Trang ${page}/${pages==0?1:pages}</span>
          <a class="btn"
             href="${pageContext.request.contextPath}/admin/divisions?q=${fn:escapeXml(param.q)}&size=${size}&page=${page+1}"
             ${(page>=pages || pages==0) ? 'style="pointer-events:none;opacity:.5"' : ''}>Sau ›</a>
        </div>
      </section>

      <footer class="footer">
        <div>© 2025 LeaveMgmt</div>
        <div class="foot-right"><a class="link" href="${pageContext.request.contextPath}/admin/hr">Trở lại Dashboard</a></div>
      </footer>
    </main>
  </div>

  <!-- Modal Create/Update -->
  <dialog id="divForm" style="border:none;border-radius:16px;padding:0;">
    <form method="post" action="${pageContext.request.contextPath}/admin/divisions" class="card" style="min-width:420px">
      <div class="panel-head" style="border:none;"><h2 id="formTitle">Thêm phòng ban</h2></div>
      <div style="padding:14px">
        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
        <input type="hidden" name="action" value="create" id="action">
        <input type="hidden" name="id" value="" id="id">

        <div style="margin-bottom:10px">
          <label>Mã (unique)</label>
          <input name="code" id="code" required style="width:100%;padding:10px" maxlength="30">
        </div>
        <div style="margin-bottom:10px">
          <label>Tên phòng ban</label>
          <input name="name" id="name" required style="width:100%;padding:10px">
        </div>
        <div style="margin-bottom:10px">
          <label>Trạng thái</label>
          <select name="status" id="status" style="width:100%;padding:10px">
            <option value="ACTIVE">ACTIVE</option>
            <option value="INACTIVE">INACTIVE</option>
          </select>
        </div>
      </div>
      <div style="display:flex;gap:8px;justify-content:flex-end;padding:12px;border-top:1px solid var(--ring)">
        <button type="button" class="btn" id="btnClose">Hủy</button>
        <button type="submit" class="btn">Lưu</button>
      </div>
    </form>
  </dialog>

  <script>
    const $ = (q, r=document)=>r.querySelector(q);
    const app = $('.app'); $('#sidebarToggle')?.addEventListener('click',()=>app.classList.toggle('collapsed'));

    // Clear search
    $('#clearQ')?.addEventListener('click',(e)=>{
      e.preventDefault();
      const form = e.target.closest('form');
      form.querySelector('input[name="q"]').value='';
      form.submit();
    });

    // Modal logic
    const dlg = $('#divForm');
    $('#btnCreate')?.addEventListener('click', ()=>{
      $('#formTitle').textContent='Thêm phòng ban';
      $('#action').value='create';
      $('#id').value='';
      $('#code').value='';
      $('#name').value='';
      $('#status').value='ACTIVE';
      dlg.showModal();
    });
    $('#btnClose')?.addEventListener('click', ()=> dlg.close());

    // Edit
    document.querySelectorAll('.btn-edit').forEach(btn=>{
      btn.addEventListener('click', ()=>{
        const tr = btn.closest('tr');
        $('#formTitle').textContent='Sửa phòng ban';
        $('#action').value='update';
        $('#id').value=tr.dataset.id;
        $('#code').value=tr.dataset.code;
        $('#name').value=tr.dataset.name;
        $('#status').value=tr.dataset.status || 'ACTIVE';
        dlg.showModal();
      });
    });
  </script>
</body>
</html>
