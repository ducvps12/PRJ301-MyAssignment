<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%-- Context & CSRF (Csrf.protect(req) đã set) --%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="csrfParam" value="${empty requestScope.csrfParam ? '_csrf' : requestScope.csrfParam}" />
<c:set var="csrfToken" value="${requestScope.csrfToken}" />

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1"/>
  <title>Cấu hình hệ thống</title>
  <style>
    :root{
      --h:64px;         /* chiều cao header */
      --sbw:240px;      /* rộng sidebar */
      --bg:#f7f9fc; --card:#fff; --ink:#0f172a; --muted:#64748b; --bd:#e5e7eb;
      --pri:#0f766e; --pri-ink:#fff; --pill:#e0f2fe; --pill-ink:#0369a1;
    }
    *{box-sizing:border-box}
    body{margin:0;background:var(--bg);color:var(--ink);
         font:14px/1.45 system-ui,Segoe UI,Roboto,Arial}
    a{color:inherit;text-decoration:none}

    /* ===== Header trắng ===== */
    .topbar{
      position:fixed; inset:0 0 auto 0; height:var(--h);
      display:flex; align-items:center; justify-content:space-between;
      padding:0 20px;
      background:#fff; color:#0f172a;
      border-bottom:1px solid #e5e7eb; box-shadow:0 0.5px 0 #e5e7eb;
      z-index:10;
    }
    .brand{font-weight:700}
    .topbar .btn-outline{
      display:inline-flex; align-items:center; gap:8px;
      height:36px; padding:0 12px; border-radius:10px;
      border:1px solid #e5e7eb; background:#fff; color:#0f172a;
    }
    .topbar .btn-outline:hover{ border-color:#cbd5e1; background:#f8fafc; }

    /* ===== Sidebar tự thân ===== */
    .sidebar{
      position:fixed; top:var(--h); bottom:0; left:0; width:var(--sbw);
      background:#111827; color:#cbd5e1; padding:12px 10px; overflow:auto;
    }
    .nav h4{margin:8px 10px 6px;font-size:12px;opacity:.7}
    .nav a{display:block;padding:10px 12px;border-radius:8px;margin:4px 6px}
    .nav a.active, .nav a:hover{background:#1f2937;color:#fff}

    /* ===== Content ===== */
    .app{padding-top:var(--h); padding-left:var(--sbw); min-height:100vh}
    .wrap{padding:20px 24px 96px}
    .head{display:flex; align-items:center; justify-content:space-between; margin-bottom:12px}
    .note{color:var(--muted); font-size:12px}
    .search{width:clamp(180px, 28vw, 280px); border:1px solid var(--bd);
            border-radius:10px; padding:8px 10px; background:#fff}

    /* ===== Bảng + chống tràn ===== */
    .table-wrap{background:var(--card); border:1px solid var(--bd);
                border-radius:14px; overflow:auto}
    table{width:100%; border-collapse:separate; border-spacing:0;
          table-layout:fixed; min-width:980px}
    th{background:#f4f4f5; color:#4b5563; font-size:12px; text-align:left;
       padding:10px 12px; position:sticky; top:0; z-index:1}
    td{padding:8px 12px; border-top:1px solid var(--bd); vertical-align:middle}
    tr:nth-child(odd) td{background:rgba(2,6,23,.02)}
    .td-keys{width:220px}
    .td-narrow{width:120px}
    th,td{overflow:hidden; text-overflow:ellipsis; white-space:nowrap}
    td:nth-child(5){white-space:normal; word-break:break-word; line-height:1.35}

    .pill{display:inline-block; padding:2px 10px; background:var(--pill);
          color:var(--pill-ink); border-radius:999px; font-size:12px}

    .input, select.input, textarea.input{
      width:100%; padding:7px 9px; border:1px solid var(--bd); border-radius:8px;
      background:#fff; color:#111827; font-size:13px;
    }
    td:nth-child(2) .input, td:nth-child(2) textarea.input, td:nth-child(2) select.input{
      max-width:560px;
    }

    .btn{border:none; border-radius:10px; padding:9px 14px; cursor:pointer; font-weight:600}
    .btn-primary{background:var(--pri); color:var(--pri-ink)}
    .btn-secondary{background:#e5e7eb; color:#111827}

    .sticky{
      position:fixed; right:12px; bottom:14px; display:flex; gap:8px; align-items:center;
      background:var(--card); border:1px solid var(--bd); padding:8px 10px;
      border-radius:12px; box-shadow:0 6px 24px rgba(2,6,23,.08);
      max-width:calc(100vw - 32px)
    }
    @supports (right: env(safe-area-inset-right)){
      .sticky{ right:max(12px, env(safe-area-inset-right)) }
    }

    /* Switch */
    .switch{display:inline-flex; align-items:center; gap:8px}
    .switch input{display:none}
    .track{width:40px; height:22px; background:#d1d5db; border-radius:999px;
           position:relative; transition:.2s}
    .thumb{width:18px; height:18px; border-radius:999px; background:#fff;
           position:absolute; top:2px; left:2px; transition:.2s; box-shadow:0 1px 3px rgba(0,0,0,.2)}
    .switch input:checked + .track{background:#22c55e}
    .switch input:checked + .track .thumb{left:20px}

    /* Footer */
    .footer{padding:14px 18px; color:#64748b; font-size:12px}
    @media (max-width:1200px){
      .td-keys{width:180px}
      th:nth-child(4), td:nth-child(4), th:nth-child(6), td:nth-child(6){display:none}
    }
  </style>
</head>
<body>

  <!-- HEADER trắng -->
  <header class="topbar">
    <div class="brand">LeaveMgmt • Admin</div>
    <nav style="display:flex; gap:10px; align-items:center">
      <a href="${ctx}/admin/settings" class="btn-outline">Cấu hình</a>
      <a href="${ctx}/logout" class="btn-outline">Đăng xuất</a>
    </nav>
  </header>

  <!-- SIDEBAR -->
  <aside class="sidebar">
    <div class="nav">
      <h4>Điều hướng</h4>
      <a href="${ctx}/admin/dashboard">Tổng quan</a>
      <a href="${ctx}/admin/users">Người dùng</a>
      <a href="${ctx}/admin/leaves">Đơn nghỉ</a>
      <a href="${ctx}/admin/audit">Audit Log</a>
      <a href="${ctx}/admin/settings" class="active">Cấu hình</a>
    </div>
  </aside>

  <!-- CONTENT -->
  <main class="app">
    <div class="wrap">

      <div class="head">
        <div>
          <h2 style="margin:0">Cấu hình hệ thống</h2>
          <div class="note">Chỉnh các thông số dạng <code>key</code> – <code>value</code>. Gõ để tìm nhanh.</div>
        </div>
        <div>
          <input class="search" id="q" placeholder="Tìm theo key / mô tả / nhóm (Ctrl+/)" />
          <c:if test="${param.ok == '1'}">
            <span style="color:#16a34a;font-weight:600">Đã lưu cấu hình ✔</span>
          </c:if>
        </div>
      </div>

      <%-- ===== FORM LƯU ===== --%>
      <form id="form-save" method="post" action="${ctx}/admin/settings">
        <input type="hidden" name="${csrfParam}" value="${csrfToken}"/>
        <input type="hidden" name="action" value="save"/>

        <div class="table-wrap">
          <table id="settingsTable" aria-label="Bảng cấu hình">
            <thead>
              <tr>
                <th class="td-keys">Key</th>
                <th>Giá trị</th>
                <th class="td-narrow">Nhóm</th>
                <th class="td-narrow">Kiểu</th>
                <th>Mô tả</th>
                <th class="td-narrow">Trạng thái</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach var="s" items="${settings}">
                <tr data-key="${fn:toLowerCase(s.key)}"
                    data-desc="${fn:toLowerCase(s.description)}"
                    data-group="${fn:toLowerCase(s.groupName)}">
                  <td class="td-keys"><strong>${s.key}</strong></td>
                  <td>
                    <c:choose>
                      <c:when test="${s.dataType == 'bool'}">
                        <label class="switch" title="Bật/Tắt">
                          <input type="checkbox" name="val_${s.id}" value="1"
                                 <c:if test="${s.value == '1' || s.value == 'true'}">checked</c:if> />
                          <span class="track"><span class="thumb"></span></span>
                        </label>
                      </c:when>
                      <c:when test="${s.dataType == 'int'}">
                        <input class="input" type="number" step="1" name="val_${s.id}" value="${s.value}" />
                      </c:when>
                      <c:when test="${s.dataType == 'json'}">
                        <textarea class="input" name="val_${s.id}" rows="3" spellcheck="false">${s.value}</textarea>
                      </c:when>
                      <c:when test="${s.dataType == 'html'}">
                        <textarea class="input" name="val_${s.id}" rows="4">${s.value}</textarea>
                      </c:when>
                      <c:otherwise>
                        <input class="input" type="text" name="val_${s.id}" value="${s.value}" />
                      </c:otherwise>
                    </c:choose>
                  </td>
                  <td>
                    <c:choose>
                      <c:when test="${not empty s.groupName}">
                        <span class="pill">${s.groupName}</span>
                      </c:when>
                      <c:otherwise>
                        <span class="pill" style="background:#e5e7eb;color:#374151">(none)</span>
                      </c:otherwise>
                    </c:choose>
                  </td>
                  <td><span class="note">${s.dataType}</span></td>
                  <td><span class="note">${s.description}</span></td>
                  <td>
                    <c:choose>
                      <c:when test="${s.active}"><span class="note">ON</span></c:when>
                      <c:otherwise><span class="note" style="color:#ef4444">OFF</span></c:otherwise>
                    </c:choose>
                  </td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </div>

        <div class="sticky">
          <button type="submit" class="btn btn-primary">💾 Lưu thay đổi</button>
          <button type="button" class="btn btn-secondary" id="btnReset">Hoàn tác</button>
        </div>
      </form>

      <%-- ===== FORM TẠO MỚI ===== --%>
      <form method="post" action="${ctx}/admin/settings"
            style="margin-top:18px;background:var(--card);border:1px solid var(--bd);border-radius:14px;padding:14px 16px;">
        <input type="hidden" name="${csrfParam}" value="${csrfToken}"/>
        <input type="hidden" name="action" value="create"/>

        <h4 style="margin:0 0 10px">Thêm cấu hình mới</h4>
        <div style="display:grid;grid-template-columns:220px 1fr 140px 1fr;gap:10px">
          <div><label class="note">Key</label><input class="input" name="new_key" placeholder="vd: site_phone" required/></div>
          <div><label class="note">Giá trị</label><input class="input" name="new_value"/></div>
          <div><label class="note">Nhóm</label><input class="input" name="new_group" placeholder="System/Mail/HR"/></div>
          <div><label class="note">Kiểu</label>
            <select name="new_type" class="input">
              <option value="string">string</option>
              <option value="int">int</option>
              <option value="bool">bool</option>
              <option value="json">json</option>
              <option value="html">html</option>
            </select>
          </div>
        </div>
        <div style="margin-top:10px"><label class="note">Mô tả</label><input class="input" name="new_desc"/></div>
        <div style="margin-top:12px"><button class="btn btn-secondary" type="submit">➕ Thêm</button></div>
      </form>

      <div class="footer">© <script>document.write(new Date().getFullYear())</script> LeaveMgmt Admin</div>
    </div>
  </main>

<script>
  // Tìm nhanh
  const q = document.getElementById('q');
  const rows = [...document.querySelectorAll('#settingsTable tbody tr')];
  function applyFilter(){
    const v = (q.value || '').toLowerCase().trim();
    rows.forEach(tr=>{
      const ok = !v || tr.dataset.key.includes(v) || tr.dataset.desc.includes(v) || tr.dataset.group.includes(v);
      tr.style.display = ok ? '' : 'none';
    });
  }
  q?.addEventListener('input', applyFilter);
  document.addEventListener('keydown', e=>{
    if(e.ctrlKey && e.key === '/'){ q?.focus(); q?.select(); e.preventDefault(); }
  });

  // Hoàn tác form
  document.getElementById('btnReset')?.addEventListener('click', ()=>{
    document.getElementById('form-save').reset();
  });

  // Với checkbox bool, nếu unchecked thì gửi 0
  document.getElementById('form-save')?.addEventListener('submit', (ev)=>{
    const form = ev.target;
    form.querySelectorAll('input[type=checkbox][name^="val_"]').forEach(cb=>{
      if(!cb.checked){
        const hidden = document.createElement('input');
        hidden.type = 'hidden';
        hidden.name = cb.name; hidden.value = '0';
        form.appendChild(hidden);
      }
    });
  });
</script>
</body>
</html>
