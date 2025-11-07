<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp"%>

<%-- ===== Layout: header + sidebar (chống double-include) ===== --%>
<c:if test="${empty requestScope.__AUDIT_LAYOUT_INCLUDED}">
  <c:set var="__AUDIT_LAYOUT_INCLUDED" scope="request" value="1"/>
  <%@ include file="/WEB-INF/views/audit/_audit_header.jsp" %>
  <%@ include file="/WEB-INF/views/audit/_audit_sidebar.jsp" %>
</c:if>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<style>
  :root{
    --bg:#f7f9fc; --card:#fff; --ink:#0f172a; --muted:#64748b; --bd:#e5e7eb;
    --pri:#0f766e; --pri-ink:#fff; --pill:#e0f2fe; --pill-ink:#0369a1;
  }
  @media (prefers-color-scheme: dark){
    :root{ --bg:#0b1220; --card:#0f172a; --ink:#e5e7eb; --muted:#94a3b8; --bd:#1f2937; --pill:#0b3b52; --pill-ink:#7dd3fc;}
  }
  body{ background:var(--bg) }
  .container.settings-page{ padding:20px 24px 96px; }
  .settings-head{ display:flex; gap:16px; align-items:center; justify-content:space-between; margin-bottom:12px;}
  .settings-actions{ display:flex; gap:8px; align-items:center; }
  .search{ width:280px; border:1px solid var(--bd); border-radius:10px; padding:8px 10px; background:#fff; }
  .badge-ok{ color:#16a34a; font-weight:600; }
  .table-wrap{ background:var(--card); border:1px solid var(--bd); border-radius:14px; overflow:hidden; }
  table.settings{ width:100%; border-collapse:separate; border-spacing:0; }
  .settings thead th{ text-align:left; background:#f4f4f5; color:#4b5563; font-size:12px; padding:10px 12px; position:sticky; top:0; z-index:1; }
  .settings tbody td{ padding:8px 12px; border-top:1px solid var(--bd); vertical-align:middle; }
  .row-alt:nth-child(odd) td{ background:rgba(2,6,23,.02); }
  .pill{ display:inline-block; padding:2px 10px; background:var(--pill); color:var(--pill-ink); border-radius:999px; font-size:12px }
  .input-sm, select.input-sm, textarea.input-sm{
    width:100%; padding:7px 9px; border:1px solid var(--bd); border-radius:8px; background:#fff; color:#111827; font-size:13px;
  }
  .td-narrow{ width:120px }
  .td-keys  { width:220px }
  .btn{ border:none; border-radius:10px; padding:9px 14px; cursor:pointer; font-weight:600 }
  .btn-primary{ background:var(--pri); color:var(--pri-ink) }
  .btn-secondary{ background:#e5e7eb; color:#111827 }
  .sticky-save{
    position:fixed; right:24px; bottom:20px; display:flex; gap:8px; align-items:center;
    background:var(--card); border:1px solid var(--bd); padding:8px 10px; border-radius:12px; box-shadow:0 6px 24px rgba(2,6,23,.08);
  }
  .note{ color:var(--muted); font-size:12px }
  .new-setting{ margin-top:18px; background:var(--card); border:1px solid var(--bd); border-radius:14px; padding:14px 16px; }
  .switch{ display:inline-flex; align-items:center; gap:8px }
  .switch input{ display:none }
  .switch .track{ width:40px; height:22px; background:#d1d5db; border-radius:999px; position:relative; transition:.2s }
  .switch .thumb{ width:18px; height:18px; border-radius:999px; background:#fff; position:absolute; top:2px; left:2px; transition:.2s; box-shadow:0 1px 3px rgba(0,0,0,.2) }
  .switch input:checked + .track{ background:#22c55e }
  .switch input:checked + .track .thumb{ left:20px }
  .kbd{ font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas,"Liberation Mono","Courier New", monospace; font-size:12px; padding:1px 6px; border:1px solid var(--bd); border-radius:6px; background:#fff }
</style>

<div class="main-body">
  <div class="container settings-page">

    <div class="settings-head">
      <div>
        <h2 style="margin:0;">Cấu hình hệ thống</h2>
        <div class="note">Chỉnh các thông số dạng <span class="kbd">key</span> – <span class="kbd">value</span>. Gõ để tìm nhanh.</div>
      </div>
      <div class="settings-actions">
        <input class="search" id="q" placeholder="Tìm theo key / mô tả / nhóm (Ctrl+/)"/>
        <c:if test="${param.ok == '1'}"><span class="badge-ok">Đã lưu cấu hình ✔</span></c:if>
      </div>
    </div>

    <form id="form-save" method="post" action="${ctx}/admin/settings">
      <input type="hidden" name="csrf_token" value="${sessionScope.csrf_token}"/>
      <input type="hidden" name="action" value="save"/>

      <div class="table-wrap">
        <table class="settings" id="settingsTable">
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
          <c:forEach var="s" items="${settings}" varStatus="st">
            <tr class="row-alt" data-key="${fn:toLowerCase(s.key)}"
                data-desc="${fn:toLowerCase(s.description)}"
                data-group="${fn:toLowerCase(s.groupName)}">
              <td class="td-keys">
                <strong>${s.key}</strong>
              </td>

              <td>
                <c:choose>
                  <c:when test="${s.dataType == 'bool'}">
                    <label class="switch">
                      <input type="checkbox" name="val_${s.id}" value="1" <c:if test="${s.value == '1' || s.value == 'true'}">checked</c:if> />
                      <span class="track"><span class="thumb"></span></span>
                    </label>
                  </c:when>

                  <c:when test="${s.dataType == 'int'}">
                    <input class="input-sm" type="number" step="1" name="val_${s.id}" value="${s.value}"/>
                  </c:when>

                  <c:when test="${s.dataType == 'json'}">
                    <textarea class="input-sm" name="val_${s.id}" rows="3" spellcheck="false">${s.value}</textarea>
                  </c:when>

                  <c:when test="${s.dataType == 'html'}">
                    <textarea class="input-sm" name="val_${s.id}" rows="4">${s.value}</textarea>
                  </c:when>

                  <c:otherwise>
                    <input class="input-sm" type="text" name="val_${s.id}" value="${s.value}"/>
                  </c:otherwise>
                </c:choose>
              </td>

              <td>
                <c:choose>
                  <c:when test="${not empty s.groupName}">
                    <span class="pill">${s.groupName}</span>
                  </c:when>
                  <c:otherwise>
                    <span class="pill" style="background:#e5e7eb;color:#374151;">(none)</span>
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

      <div class="sticky-save">
        <button type="submit" class="btn btn-primary">💾 Lưu thay đổi</button>
        <button type="button" class="btn btn-secondary" id="btnReset">Hoàn tác</button>
      </div>
    </form>

    <form method="post" action="${ctx}/admin/settings" class="new-setting" id="form-new">
      <input type="hidden" name="csrf_token" value="${sessionScope.csrf_token}"/>
      <input type="hidden" name="action" value="create"/>

      <h4 style="margin:0 0 10px">Thêm cấu hình mới</h4>
      <div style="display:grid;grid-template-columns:220px 1fr 140px 1fr;gap:10px">
        <div>
          <label class="note">Key</label>
          <input class="input-sm" name="new_key" placeholder="vd: site_phone" required/>
        </div>
        <div>
          <label class="note">Giá trị</label>
          <input class="input-sm" name="new_value"/>
        </div>
        <div>
          <label class="note">Nhóm</label>
          <input class="input-sm" name="new_group" placeholder="System/Mail/HR"/>
        </div>
        <div>
          <label class="note">Kiểu</label>
          <select name="new_type" class="input-sm">
            <option value="string">string</option>
            <option value="int">int</option>
            <option value="bool">bool</option>
            <option value="json">json</option>
            <option value="html">html</option>
          </select>
        </div>
      </div>
      <div style="margin-top:10px">
        <label class="note">Mô tả</label>
        <input class="input-sm" name="new_desc"/>
      </div>
      <div style="margin-top:12px">
        <button class="btn btn-secondary" type="submit">➕ Thêm</button>
      </div>
    </form>

  </div>
</div>

<%@ include file="/WEB-INF/views/common/_admin_footer.jsp" %>

<script>
  // Tìm nhanh
  const q = document.getElementById('q');
  const tbody = document.querySelector('#settingsTable tbody');
  const rows = [...tbody.querySelectorAll('tr')];
  function applyFilter(){
    const v = (q.value || '').toLowerCase().trim();
    rows.forEach(tr=>{
      const ok = !v ||
        tr.dataset.key.includes(v) ||
        tr.dataset.desc.includes(v) ||
        tr.dataset.group.includes(v);
      tr.style.display = ok ? '' : 'none';
    });
  }
  q.addEventListener('input', applyFilter);
  document.addEventListener('keydown', e=>{ if(e.ctrlKey && e.key === '/'){ q.focus(); q.select(); e.preventDefault(); } });

  // Hoàn tác form
  const btnReset = document.getElementById('btnReset');
  btnReset.addEventListener('click', ()=> document.getElementById('form-save').reset());

  // Với input bool, khi submit nếu unchecked → gửi 0
  document.getElementById('form-save').addEventListener('submit', (ev)=>{
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
