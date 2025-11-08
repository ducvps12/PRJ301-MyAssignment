<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp" %>
<c:set var="cp" value="${pageContext.request.contextPath}"/>

<style>
  .wrap{max-width:1100px;margin:16px auto;padding:0 16px}
  .panel{background:#fff;border:1px solid #e5e7eb;border-radius:14px;padding:14px;margin-bottom:16px}
  .muted{color:#64748b}
  .toolbar{display:flex;gap:8px;align-items:end;flex-wrap:wrap;margin:8px 0}
  .input, select, textarea{border:1px solid #e5e7eb;border-radius:10px;padding:8px 10px}
  .btn{border:1px solid #0ea5e9;background:#0ea5e9;color:#fff;border-radius:10px;padding:8px 12px;cursor:pointer}
  .btn.ghost{background:#fff;color:#0ea5e9}
  table{width:100%;border-collapse:collapse}
  th,td{border-bottom:1px solid #e5e7eb;padding:10px;text-align:left;vertical-align:top}
  th{background:#f8fafc;font-weight:600}
  .chips{display:inline-flex;gap:6px;flex-wrap:wrap}
  .chip{padding:.2rem .55rem;border-radius:999px;border:1px solid #e5e7eb;background:#f8fafc;font-size:12px}
  .actions form{display:inline}
  .right{margin-left:auto}
  .pager{display:flex;gap:6px;justify-content:flex-end;margin-top:10px}
  .pager a{padding:6px 10px;border:1px solid #e5e7eb;border-radius:8px;text-decoration:none;color:#111;background:#fff}
  .pager .on{background:#0ea5e9;color:#fff;border-color:#0ea5e9}
</style>

<main class="wrap">
  <!-- Filter -->
  <div class="panel">
    <h2 style="margin-bottom:8px">Todos</h2>
    <form class="toolbar" method="get" action="${cp}/work/todos">
      <div>
        <label class="muted">Status</label><br/>
        <select name="status">
          <option value="">-- All --</option>
          <c:forEach var="s" items="${['OPEN','DOING','DONE','HOLD','CANCEL']}">
            <option value="${s}" ${s==status?'selected':''}>${s}</option>
          </c:forEach>
        </select>
      </div>
      <div>
        <label class="muted">Assignee (userId)</label><br/>
        <input class="input" type="text" name="assignee" value="${assignee}"/>
      </div>
      <div>
        <label class="muted">&nbsp;</label><br/>
        <button class="btn" type="submit">Apply</button>
        <a class="btn ghost" href="${cp}/work/todos">Reset</a>
      </div>
      <span class="muted right">Page: ${page}</span>
    </form>
  </div>

  <!-- List -->
  <div class="panel">
    <c:choose>
      <c:when test="${empty todos}">
        <p class="muted">Không có công việc nào với bộ lọc hiện tại.</p>
      </c:when>
      <c:otherwise>
        <table>
          <thead>
          <tr>
            <th style="width:80px">#ID</th>
            <th>Title</th>
            <th style="width:140px">Due</th>
            <th style="width:110px">Priority</th>
            <th style="width:110px">Status</th>
            <th style="width:220px">Actions</th>
          </tr>
          </thead>
          <tbody>
          <c:forEach var="t" items="${todos}">
            <tr>
              <td>${t.id}</td>
              <td>
                <div><strong><c:out value="${t.title}"/></strong></div>
                <c:if test="${not empty t.tags}">
                  <div class="chips">
                    <c:forTokens var="g" items="${t.tags}" delims=",">
                      <span class="chip"><c:out value="${fn:trim(g)}"/></span>
                    </c:forTokens>
                  </div>
                </c:if>
                <c:if test="${not empty t.note}">
                  <div class="muted" style="margin-top:4px">
                    <c:out value="${t.note}"/>
                  </div>
                </c:if>
              </td>
              <td>
                <c:choose>
                  <c:when test="${not empty t.due_date || not empty t.dueDate}">
                    <fmt:formatDate value="${empty t.dueDate ? t.due_date : t.dueDate}" pattern="yyyy-MM-dd"/>
                  </c:when>
                  <c:otherwise><span class="muted">—</span></c:otherwise>
                </c:choose>
              </td>
              <td>${empty t.priority ? 'NORMAL' : t.priority}</td>
              <td>${empty t.status ? 'OPEN' : t.status}</td>
              <td class="actions">
                <form method="post" action="${cp}/work" style="margin-right:6px">
                  <input type="hidden" name="act" value="setTodoStatus"/>
                  <input type="hidden" name="id" value="${t.id}"/>
                  <input type="hidden" name="status" value="OPEN"/>
                  <button class="btn ghost" type="submit">OPEN</button>
                </form>
                <form method="post" action="${cp}/work" style="margin-right:6px">
                  <input type="hidden" name="act" value="setTodoStatus"/>
                  <input type="hidden" name="id" value="${t.id}"/>
                  <input type="hidden" name="status" value="DOING"/>
                  <button class="btn ghost" type="submit">DOING</button>
                </form>
                <form method="post" action="${cp}/work">
                  <input type="hidden" name="act" value="setTodoStatus"/>
                  <input type="hidden" name="id" value="${t.id}"/>
                  <input type="hidden" name="status" value="DONE"/>
                  <button class="btn" type="submit">DONE</button>
                </form>
              </td>
            </tr>
          </c:forEach>
          </tbody>
        </table>

        <!-- Pager cơ bản -->
        <c:if test="${not empty page}">
          <div class="pager">
            <c:set var="p" value="${page}"/>
            <c:set var="prev" value="${p-1 < 1 ? 1 : p-1}"/>
            <a href="${cp}/work/todos?status=${status}&assignee=${assignee}&page=${prev}">« Trước</a>
            <a class="on" href="${cp}/work/todos?status=${status}&assignee=${assignee}&page=${p}">${p}</a>
            <a href="${cp}/work/todos?status=${status}&assignee=${assignee}&page=${p+1}">Sau »</a>
          </div>
        </c:if>
      </c:otherwise>
    </c:choose>
  </div>

  <!-- Add new -->
  <div class="panel">
    <h3 style="margin-bottom:8px">Add Todo</h3>
    <form method="post" action="${cp}/work" class="js-post">
      <input type="hidden" name="act" value="addTodo"/>

      <div class="toolbar" style="align-items:flex-start">
        <div style="flex:1;min-width:260px">
          <label class="muted">Title *</label><br/>
          <input class="input" name="title" required placeholder="Viết tiêu đề công việc…" style="width:100%"/>
        </div>

        <div>
          <label class="muted">Assignee (userId)</label><br/>
          <input class="input" type="text" name="assignee" value="${assignee}" style="width:160px"/>
        </div>

        <div>
          <label class="muted">Due</label><br/>
          <input class="input" type="date" name="due" style="width:160px"/>
        </div>

        <div>
          <label class="muted">Priority</label><br/>
          <select name="priority">
            <c:forEach var="p" items="${['LOW','NORMAL','HIGH']}">
              <option value="${p}" ${p=='NORMAL'?'selected':''}>${p}</option>
            </c:forEach>
          </select>
        </div>

        <div style="flex:1;min-width:220px">
          <label class="muted">Tags (comma)</label><br/>
          <input class="input" name="tags" placeholder="backend,urgent,release" style="width:100%"/>
        </div>
      </div>

      <div style="margin:8px 0">
        <label class="muted">Note</label><br/>
        <textarea class="input" name="note" rows="3" style="width:100%" placeholder="Mô tả nhanh công việc…"></textarea>
      </div>

      <button class="btn" type="submit">Create</button>
      <a class="btn ghost" href="${cp}/work/todos">Cancel</a>
    </form>
  </div>
</main>

<script>
  // Chặn double-submit cho các form POST
  document.querySelectorAll('form.js-post, td.actions form').forEach(f=>{
    f.addEventListener('submit', e=>{
      const btn = f.querySelector('button[type=submit]');
      if (btn){ btn.disabled = true; btn.textContent = 'Đang gửi...'; }
    });
  });
</script>
