<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>Chi tiết yêu cầu #${r.id}</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
<%@ include file="/WEB-INF/views/common/_header.jsp" %>

  <style>
    :root{
      --bg:#f7f7f8; --card:#fff; --b:#e5e7eb; --muted:#6b7280;
      --ok:#10b981; --warn:#f59e0b; --no:#ef4444; --info:#3b82f6;
    }
    *{box-sizing:border-box}
    body{font-family:system-ui,Segoe UI,Roboto,Helvetica,Arial,sans-serif;background:var(--bg);margin:0;color:#0f172a}
    a{color:#111827;text-decoration:none}
    .wrap{max-width:1000px;margin:22px auto;padding:0 16px}
    .card{background:var(--card);border:1px solid var(--b);border-radius:16px;box-shadow:0 2px 6px rgba(0,0,0,.04);padding:18px;margin-bottom:18px}

    .topbar{display:flex;align-items:center;gap:8px;justify-content:space-between;margin-bottom:12px}
    .breadcrumbs{font-size:13px;color:var(--muted)}
    .breadcrumbs a{color:inherit}
    .title{display:flex;align-items:center;gap:10px;flex-wrap:wrap}
    .id {font-weight:600}
    .badge{display:inline-flex;align-items:center;gap:6px;padding:6px 10px;border-radius:999px;font-size:12px;border:1px solid var(--b);}
    .status-APPROVED{background:#ecfdf5;border-color:var(--ok);color:#065f46}
    .status-REJECTED{background:#fef2f2;border-color:var(--no);color:#991b1b}
    .status-INPROGRESS{background:#eff6ff;border-color:var(--info);color:#1e40af}
    .status-CANCELLED{background:#f3f4f6;border-color:#9ca3af;color:#374151}

    .muted{color:var(--muted)}
    .grid{display:grid;grid-template-columns:1fr 1fr;gap:12px}
    @media (max-width:720px){ .grid{grid-template-columns:1fr} }

    .kv{display:flex;gap:8px}
    .kv b{min-width:140px}
    .mono{font-family:ui-monospace,SFMono-Regular,Menlo,Monaco,Consolas,"Liberation Mono","Courier New",monospace}

    .actions{display:flex;flex-wrap:wrap;gap:8px;margin-top:10px}
    .btn{display:inline-flex;align-items:center;gap:6px;padding:10px 12px;border-radius:12px;border:1px solid var(--b);background:#fff;cursor:pointer}
    .btn:hover{box-shadow:0 1px 0 rgba(0,0,0,.06)}
    .btn-primary{background:#111827;color:#fff;border-color:#111827}
    .btn-danger{border-color:var(--no);color:var(--no);background:#fff}
    .btn-icon{width:36px;height:36px;justify-content:center}

    .chip{display:inline-block;padding:3px 8px;border:1px dashed var(--b);border-radius:999px;font-size:12px;color:var(--muted)}

    /* Timeline */
    .timeline{list-style:none;padding:0;margin:0}
    .timeline li{border-left:2px solid var(--b);margin-left:12px;padding-left:12px;padding-bottom:12px;position:relative}
    .timeline li::before{content:'';width:10px;height:10px;border-radius:50%;background:#fff;border:2px solid var(--info);position:absolute;left:-7px;top:4px}
    .tag{font-size:11px;padding:2px 6px;border-radius:999px;border:1px solid var(--b);color:#334155;background:#f8fafc}
    .note{margin-top:4px;padding:8px 10px;border:1px solid var(--b);border-radius:10px;background:#fafafa}

    /* Toast */
    .toast{position:fixed;right:16px;bottom:16px;background:#111827;color:#fff;padding:10px 14px;border-radius:12px;opacity:0;transform:translateY(6px);transition:all .25s}
    .toast.show{opacity:1;transform:none}

    /* Tooltip */
    .tip{position:relative}
    .tip:hover .tiptext{opacity:1;transform:translateY(-2px)}
    .tiptext{position:absolute;left:50%;transform:translate(-50%,0);bottom:calc(100% + 8px);opacity:.0;background:#111827;color:#fff;padding:6px 8px;border-radius:6px;white-space:nowrap;font-size:12px;pointer-events:none;transition:all .18s}

    /* Minimal modal sheet */
    dialog{border:none;border-radius:16px;box-shadow:0 20px 80px rgba(0,0,0,.18);padding:0}
    .modal{padding:18px 18px 12px}
    .modal .row{margin-top:10px}
    textarea{width:100%;min-height:110px;border:1px solid var(--b);border-radius:12px;padding:10px;font:inherit}

    /* Print */
    @media print{
      .topbar,.actions,.btn,.toast,dialog{display:none !important}
      .card{box-shadow:none;border:none}
      body{background:#fff}
    }
  </style>
</head>
<body>
<div class="wrap">

  <!-- Breadcrumbs + quick actions -->
  <div class="topbar">
    <div class="breadcrumbs">
      <a href="${pageContext.request.contextPath}/">Trang chủ</a> ›
      <a href="${pageContext.request.contextPath}/request/list">Danh sách</a> ›
      Chi tiết
    </div>
    <div class="actions">
      <button class="btn btn-icon tip" id="copyLinkBtn" aria-label="Copy link" title="Copy link (Ctrl+C)">
        🔗
        <span class="tiptext">Sao chép liên kết</span>
      </button>
      <button class="btn btn-icon tip" id="printBtn" aria-label="In" title="In (P)">
        🖨️
        <span class="tiptext">In (phím P)</span>
      </button>
    </div>
  </div>

  <div class="card">
    <div class="title">
      <h2 style="margin:0">Chi tiết yêu cầu <span class="id mono">#${r.id}</span></h2>
      <span class="badge status-${r.status}">
        <c:choose>
          <c:when test="${r.status eq 'INPROGRESS'}">⏳ Đang xử lý</c:when>
          <c:when test="${r.status eq 'APPROVED'}">✅ Đã duyệt</c:when>
          <c:when test="${r.status eq 'REJECTED'}">❌ Từ chối</c:when>
          <c:otherwise>🛑 Đã hủy</c:otherwise>
        </c:choose>
      </span>
      <c:if test="${not empty param.msg}">
        <span class="chip">Cập nhật: <c:out value='${param.msg}'/></span>
      </c:if>
    </div>

    <div class="muted" style="margin-top:6px">Người tạo: <c:out value='${r.createdByName}'/></div>

    <div class="grid" style="margin-top:12px">
      <div class="kv">
        <b>Tiêu đề:</b>
        <span>
          <c:choose>
            <c:when test="${not empty r.title}"><c:out value='${r.title}'/></c:when>
            <c:otherwise><span class="muted">(không có)</span></c:otherwise>
          </c:choose>
        </span>
      </div>

      <div class="kv">
        <b>Khoảng thời gian:</b>
        <span>
          <c:choose>
            <c:when test="${not empty r.startDateUtil}">
              <fmt:formatDate value="${r.startDateUtil}" pattern="dd/MM/yyyy"/>
            </c:when>
            <c:otherwise>—</c:otherwise>
          </c:choose>
          –
          <c:choose>
            <c:when test="${not empty r.endDateUtil}">
              <fmt:formatDate value="${r.endDateUtil}" pattern="dd/MM/yyyy"/>
            </c:when>
            <c:otherwise>—</c:otherwise>
          </c:choose>
          <c:if test="${r.days gt 0}">
            &nbsp;<span class="chip"><fmt:formatNumber value="${r.days}" maxFractionDigits="1"/> ngày</span>
          </c:if>
        </span>
      </div>

      <c:if test="${not empty r.leaveTypeName}">
        <div class="kv">
          <b>Loại nghỉ:</b>
          <span><c:out value='${r.leaveTypeName}'/></span>
        </div>
      </c:if>

      <div style="grid-column:1/-1" class="kv">
        <b>Lý do:</b>
        <div><c:out value='${r.reason}'/></div>
      </div>

      <c:if test="${not empty r.managerNote}">
        <div style="grid-column:1/-1" class="kv">
          <b>Ghi chú quản lý:</b>
          <div class="note" style="background:#fbfafa"><c:out value='${r.managerNote}'/></div>
        </div>
      </c:if>

    <c:if test="${not empty r.attachmentUrl or not empty r.attachmentPath or not empty r.attachmentName}">
  <div style="grid-column:1/-1" class="kv">
    <b>Tệp đính kèm:</b>
    <div>
      📎 <c:out value='${empty r.attachmentName ? "file" : r.attachmentName}'/>
      <c:choose>
        <c:when test="${not empty r.attachmentUrl}">
          <a class="btn" style="padding:4px 8px;margin-left:8px"
             href="${r.attachmentUrl}" target="_blank" rel="noopener">Tải xuống</a>
        </c:when>
        <c:when test="${not empty r.attachmentPath}">
          <a class="btn" style="padding:4px 8px;margin-left:8px"
             href="${pageContext.request.contextPath}/files/${r.attachmentPath}">Tải xuống</a>
        </c:when>
      </c:choose>
    </div>
  </div>
</c:if>

    </div>

    <!-- Hành động -->
    <div class="actions">
      <a class="btn" href="${pageContext.request.contextPath}/request/list">← Quay lại danh sách</a>

      <c:if test="${not empty sessionScope.user
                   and sessionScope.user.roleCode eq 'MANAGER'
                   and r.status eq 'INPROGRESS'}">
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/request/approve?id=${r.id}">Duyệt / Từ chối</a>
      </c:if>

      <c:if test="${not empty sessionScope.user
                   and sessionScope.user.userId == r.createdBy
                   and r.status eq 'INPROGRESS'}">
        <button class="btn btn-danger" id="openCancel">Hủy yêu cầu</button>
      </c:if>
    </div>
  </div>

  <!-- LỊCH SỬ -->
  <div class="card" id="history">
    <h3 style="margin-top:0">Lịch sử xử lý</h3>
    <ul class="timeline">
      <c:forEach var="h" items="${r.history}">
        <li>
          <div style="display:flex;gap:8px;align-items:center;flex-wrap:wrap">
            <span class="tag"><c:out value='${h.action}'/></span>
            <span class="muted">
              <fmt:formatDate value="${h.actedAt}" pattern="dd/MM/yyyy HH:mm"/> • <c:out value='${h.actedByName}'/>
            </span>
          </div>
          <c:if test="${not empty h.note}">
            <div class="note"><c:out value='${h.note}'/></div>
          </c:if>
        </li>
      </c:forEach>
      <c:if test="${empty r.history}">
        <li><span class="muted">Chưa có lịch sử.</span></li>
      </c:if>
    </ul>
  </div>
</div>

<!-- Cancel modal (native dialog) -->
<dialog id="cancelDlg">
  <form method="post" action="${pageContext.request.contextPath}/request/cancel" class="modal">
    <h3 style="margin:0">Xác nhận hủy yêu cầu #${r.id}</h3>
    <input type="hidden" name="id" value="${r.id}">
    <div class="row muted">Bạn chỉ có thể hủy khi trạng thái còn <b>INPROGRESS</b>.</div>
    <div class="row">
      <label for="note"><b>Lý do hủy (tuỳ chọn)</b></label>
      <textarea name="note" id="note" placeholder="Ví dụ: Đổi kế hoạch cá nhân..."></textarea>
    </div>
    <div class="row" style="display:flex;gap:8px;justify-content:flex-end">
      <button type="button" class="btn" id="closeCancel">Đóng</button>
      <button type="submit" class="btn btn-danger">Xác nhận hủy</button>
    </div>
  </form>
</dialog>

<div class="toast" id="toast">Đã sao chép liên kết!</div>

<script>
  // Toast helper
  const toast = msg => {
    const el = document.getElementById('toast');
    el.textContent = msg || 'Thao tác thành công!';
    el.classList.add('show');
    setTimeout(()=>el.classList.remove('show'), 1800);
  };

  // Copy link
  document.getElementById('copyLinkBtn')?.addEventListener('click', async () => {
    try {
      await navigator.clipboard.writeText(location.href);
      toast('Đã sao chép liên kết!');
    } catch(e){ toast('Không thể sao chép.'); }
  });

  // Print
  document.getElementById('printBtn')?.addEventListener('click', () => window.print());

  // Keyboard shortcuts: B(back), P(print), H(history)
  window.addEventListener('keydown', (e)=>{
    if (e.target.tagName === 'TEXTAREA' || e.target.tagName === 'INPUT') return;
    if (e.key === 'p' || e.key === 'P'){ e.preventDefault(); window.print(); }
    if (e.key === 'b' || e.key === 'B'){ history.back(); }
    if (e.key === 'h' || e.key === 'H'){ document.getElementById('history')?.scrollIntoView({behavior:'smooth'}); }
  });

  // Cancel dialog
  const dlg = document.getElementById('cancelDlg');
  document.getElementById('openCancel')?.addEventListener('click', ()=> dlg.showModal());
  document.getElementById('closeCancel')?.addEventListener('click', ()=> dlg.close());

  // Show toast if ?msg=...
  const url = new URL(location.href);
  const msg = url.searchParams.get('msg');
  if (msg) setTimeout(()=>toast(msg), 100);
</script>

<%@ include file="/WEB-INF/views/common/_footer.jsp" %>

</body>
</html>
