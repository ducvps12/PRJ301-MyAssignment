<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp" %>
<!DOCTYPE html>
<html lang="vi" data-theme="${sessionScope.theme != null ? sessionScope.theme : 'auto'}">
<head>
  <meta charset="UTF-8">
  <title>Audit Log · LeaveMgmt</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css?v=9"/>

  <style>
    /* ===== Layout fix: bù header (fixed) + sidebar (fixed) ===== */
    :root{ --h:64px; --sbw:220px; }
    body.admin .admin-main{
      margin:0; padding:16px;
      padding-top:calc(var(--h) + 12px);
      margin-left:var(--sbw);
    }
    @media(max-width:1100px){ body.admin .admin-main{ margin-left:0 } }

    /* ===== Audit page ===== */
    .audit h2{margin:0 0 .8rem}
    .audit .filters{display:flex;gap:.5rem;flex-wrap:wrap;align-items:flex-end}
    .audit .filters>*{display:flex;flex-direction:column}
    .audit .filters .grow{flex:1 1 280px}
    .audit .filters input,.audit .filters select{
      height:38px;padding:0 .6rem;border:1px solid var(--bd);border-radius:.6rem;background:#fff
    }
    .audit .filters label{font-size:12px;color:var(--muted);margin-bottom:4px}
    .audit .filters .btn{height:38px;line-height:38px;padding:0 14px}

    .toolbar{display:flex;justify-content:space-between;align-items:center;margin:.6rem 0}
    .toolbar .muted{font-size:13px}
    .pagination{display:flex;gap:.35rem;flex-wrap:wrap}
    .pagination a{padding:.35rem .6rem;border:1px solid #e5e7eb;border-radius:.5rem;text-decoration:none}
    .pagination a.active{background:var(--subtle);font-weight:600}

    .audit .table{margin-top:.75rem;background:var(--card);border:1px solid var(--bd);border-radius:.8rem;overflow:auto;box-shadow:var(--shadow)}
    .audit table{width:100%;min-width:980px;border-collapse:separate;border-spacing:0}
    .audit th,.audit td{padding:.65rem .8rem;border-bottom:1px solid #eef2f7;font-size:14px;vertical-align:top}
    .audit thead th{position:sticky;top:calc(var(--h) + 16px);z-index:2;background:var(--card);text-align:left}
    .audit tbody tr:hover{background:rgba(148,163,184,.08)}
    .nowrap{white-space:nowrap}
    .ua{max-width:520px;overflow:hidden;white-space:nowrap;text-overflow:ellipsis}

    .chip{display:inline-block;padding:.18rem .55rem;border-radius:999px;font-size:12px;border:1px solid #e5e7eb}
    .chip.OK{background:#ecfdf5;border-color:#bbf7d0}
  </style>
</head>
<body class="admin">

  <!-- Header & Sidebar (fixed) -->
  <jsp:include page="/WEB-INF/views/common/_admin_header.jsp"/>

  <main class="admin-main">
    <div class="container audit">
      <h2>Audit Log</h2>

      <!-- ===== Filters ===== -->
      <form class="filters" method="get" action="">
        <input type="hidden" name="csrf" value="${sessionScope.csrf}"/>

        <div>
          <label>User ID</label>
          <input type="number" name="userId" value="${param.userId}"/>
        </div>
        <div>
          <label>Action</label>
          <input name="action" placeholder="APPROVE_REQUEST / LOGOUT ..." value="${fn:escapeXml(param.action)}"/>
        </div>
        <div>
          <label>Từ ngày</label>
          <input type="date" name="from" value="${param.from}"/>
        </div>
        <div>
          <label>Đến ngày</label>
          <input type="date" name="to" value="${param.to}"/>
        </div>
        <div class="grow">
          <label>Tìm nhanh</label>
          <input name="q" placeholder="note, IP, user agent..." value="${fn:escapeXml(param.q)}"/>
        </div>
        <div>
          <label>Size</label>
          <input id="sizeBox" type="number" min="10" max="100" name="size" value="${empty result ? 20 : result.size}"/>
        </div>
        <div><button class="btn" type="submit" title="Lọc (Enter)">Lọc</button></div>

        <c:url var="csvUrl" value="">
          <c:param name="userId" value="${param.userId}"/>
          <c:param name="action" value="${param.action}"/>
          <c:param name="q" value="${param.q}"/>
          <c:param name="from" value="${param.from}"/>
          <c:param name="to" value="${param.to}"/>
          <c:param name="size" value="${empty result ? 20 : result.size}"/>
          <c:param name="page" value="${empty result ? 1 : result.page}"/>
          <c:param name="export" value="csv"/>
        </c:url>
        <div><a class="btn" href="${csvUrl}" title="Xuất CSV">↯ Export CSV</a></div>
      </form>

      <!-- ===== Toolbar ===== -->
      <div class="toolbar">
        <div class="muted">
          Tổng: <strong>${empty result ? 0 : (empty result.totalItems ? result.total : result.totalItems)}</strong> bản ghi
        </div>
        <div class="pagination">
          <c:if test="${not empty result}">
            <c:forEach begin="1" end="${result.totalPages}" var="p">
              <c:url var="pageUrl" value="">
                <c:param name="userId" value="${param.userId}"/>
                <c:param name="action" value="${param.action}"/>
                <c:param name="q" value="${param.q}"/>
                <c:param name="from" value="${param.from}"/>
                <c:param name="to" value="${param.to}"/>
                <c:param name="size" value="${result.size}"/>
                <c:param name="page" value="${p}"/>
              </c:url>
              <a href="${pageUrl}" class="${p==result.page?'active':''}">${p}</a>
            </c:forEach>
          </c:if>
        </div>
      </div>

      <!-- ===== Table ===== -->
      <div class="table">
        <table>
          <thead>
          <tr>
            <th class="nowrap">Thời gian</th>
            <th class="nowrap">Người dùng</th>
            <th>Action</th>
            <th class="nowrap">Đối tượng</th>
            <th>Ghi chú</th>
            <th class="nowrap">IP</th>
            <th>User-Agent</th>
          </tr>
          </thead>
          <tbody>
          <c:choose>
            <c:when test="${empty result || empty result.items}">
              <tr><td colspan="7" class="muted" style="text-align:center">Không có bản ghi.</td></tr>
            </c:when>
            <c:otherwise>
              <c:forEach items="${result.items}" var="a">
                <tr>
<td class="muted nowrap">
  <c:choose>
    <c:when test="${a.createdAt ne null}">
      <fmt:formatDate value="${a.createdAt}" pattern="yyyy-MM-dd'T'HH:mm:ss" var="iso"/>
      <time datetime="${iso}">
        <fmt:formatDate value="${a.createdAt}" pattern="yyyy-MM-dd HH:mm:ss"/>
      </time>
    </c:when>
    <c:otherwise>-</c:otherwise>
  </c:choose>
</td>



                  <td class="nowrap"><c:out value="${a.userName}"/> <span class="muted">(#<c:out value="${a.userId}"/>)</span></td>
                  <td><span class="chip OK"><c:out value="${a.action}"/></span></td>
                  <td class="nowrap">
                    <c:out value="${a.entityType}"/>
                    <c:if test="${a.entityId != null}"> #<c:out value="${a.entityId}"/></c:if>
                  </td>
                  <td><c:out value="${a.note}"/></td>
                  <td class="muted nowrap"><c:out value="${a.ipAddr}"/></td>
                  <td class="ua muted" title="${a.userAgent}">
                    <span class="ua-text"><c:out value="${a.userAgent}"/></span>
                    <button type="button" class="btn" style="margin-left:6px;padding:0 .5rem;height:28px" data-copy=".ua-text">Copy</button>
                  </td>
                </tr>
              </c:forEach>
            </c:otherwise>
          </c:choose>
          </tbody>
        </table>
      </div>
    </div>
  </main>

  <script>
    // Tự đo header/aside để set --h/--sbw (né hard-code)
    (function(){
      const h  = document.querySelector('.ad-header, .admin-header')?.offsetHeight || 64;
      const sb = document.querySelector('.ad-sidebar, .admin-sidebar, #sidebar')?.offsetWidth || 220;
      document.documentElement.style.setProperty('--h',  h + 'px');
      document.documentElement.style.setProperty('--sbw', sb + 'px');
    })();

    // Lưu nhớ input size
    (function(){
      const key='audit_size'; const box=document.getElementById('sizeBox');
      if(!box) return;
      if(!box.value && localStorage.getItem(key)) box.value = localStorage.getItem(key);
      box.addEventListener('change', ()=> localStorage.setItem(key, box.value));
    })();

    // Copy nhanh User-Agent (và có thể dùng cho ô khác nếu thêm data-copy)
    document.body.addEventListener('click', async (e)=>{
      const btn = e.target.closest('button[data-copy]');
      if(!btn) return;
      const sel = btn.getAttribute('data-copy');
      const el  = btn.closest('td')?.querySelector(sel);
      if(el){
        try{ await navigator.clipboard.writeText(el.textContent.trim()); btn.textContent='Copied'; setTimeout(()=>btn.textContent='Copy',1200); }
        catch{ alert('Không thể copy'); }
      }
    });

    // Phím tắt: / để focus ô tìm nhanh
    document.addEventListener('keydown', (e)=>{
      if(e.key==='/' && !/input|textarea/i.test(e.target.tagName)){
        e.preventDefault();
        const q=document.querySelector('input[name="q"]'); q?.focus();
      }
    });
  </script>
</body>
</html>
