<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp" %>
<!DOCTYPE html>
<html lang="vi" data-theme="${sessionScope.theme != null ? sessionScope.theme : 'light'}">
<head>
  <meta charset="UTF-8" />
  <title>Danh sách đơn nghỉ phép</title>
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <meta name="color-scheme" content="light dark" />
  
  <!-- THEME BOOTSTRAP: set data-theme sớm từ localStorage (mặc định light) -->
<%@ include file="/WEB-INF/views/common/_theme_bootstrap.jsp" %>


  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/list.css" />
  <script defer src="${pageContext.request.contextPath}/assets/js/list.js"></script>
</head>
<body>
<%@ include file="/WEB-INF/views/common/_header.jsp" %>

<div class="wrap">

  <!-- KPI nhỏ cho quản lý -->
  <c:if test="${sessionScope.role == 'MANAGER'}">
    <section class="kpi">
      <div class="box">📝 Chờ duyệt <b>${stats.pendingCount}</b></div>
      <div class="box">✅ Duyệt tháng này <b>${stats.approvedThisMonth}</b></div>
      <div class="box">📉 Tỉ lệ duyệt <b>${stats.approvalRate}%</b></div>
      <div class="box tip" title="Nhân sự active trong phòng">👥 Headcount <b>${stats.headcount}</b></div>
    </section>
  </c:if>

  <!-- Thanh công cụ -->
  <div class="toolbar">
    <a class="btn btn-primary" href="${pageContext.request.contextPath}/request/create">+ Tạo đơn</a>
    <a class="btn" href="${pageContext.request.contextPath}/request/agenda">📅 Agenda</a>
    <button class="btn" id="btnExportCsv" type="button" title="Xuất CSV theo bộ lọc">⇩ Xuất CSV</button>
    <a class="btn btn-ghost" href="${pageContext.request.contextPath}/request/list" id="btnRefresh" title="Làm mới (R)">⟲ Làm mới</a>

    <!-- Quick chips -->
    <div class="chips">
      <button class="chip" data-quick="week">Tuần này</button>
      <button class="chip" data-quick="month">Tháng này</button>
      <button class="chip" data-quick="pending">Đang chờ</button>
      <button class="chip" data-quick="approved">Đã duyệt</button>
      <button class="chip" data-quick="mine">Của tôi</button>
      <button class="chip" data-quick="clear">Xóa lọc</button>
    </div>

    <!-- Bộ lọc nâng cao (autosubmit) -->
    <form id="filterForm" method="get" action="${pageContext.request.contextPath}/request/list" class="right">
      <input type="date" id="from" name="from" value="${param.from}" aria-label="Từ ngày">
      <input type="date" id="to"   name="to"   value="${param.to}" aria-label="Đến ngày">

      <select name="status" id="statusSel" aria-label="Trạng thái">
        <option value="">Trạng thái</option>
        <option value="PENDING"   ${param.status == 'PENDING'   ? 'selected':''}>Chờ duyệt</option>
        <option value="APPROVED"  ${param.status == 'APPROVED'  ? 'selected':''}>Đã duyệt</option>
        <option value="REJECTED"  ${param.status == 'REJECTED'  ? 'selected':''}>Từ chối</option>
        <option value="CANCELLED" ${param.status == 'CANCELLED' ? 'selected':''}>Đã hủy</option>
      </select>

      <select name="mine" id="mineSel" aria-label="Phạm vi">
        <option value="">Mọi người</option>
        <option value="1" ${param.mine == '1' ? 'selected' : ''}>Chỉ của tôi</option>
        <c:if test="${sessionScope.role == 'MANAGER'}">
          <option value="team" ${param.mine == 'team' ? 'selected' : ''}>Cấp dưới của tôi</option>
        </c:if>
      </select>

      <input type="text" id="q" name="q" value="${fn:escapeXml(param.q)}" placeholder="Tìm lý do, người tạo… (/)">

      <select name="sort" id="sortSel" aria-label="Sắp xếp">
        <option value="">Sắp xếp</option>
        <option value="created_desc" ${param.sort=='created_desc'?'selected':''}>Mới nhất</option>
        <option value="created_asc"  ${param.sort=='created_asc'?'selected':''}>Cũ nhất</option>
        <option value="from_asc"     ${param.sort=='from_asc'?'selected':''}>Bắt đầu ↑</option>
        <option value="from_desc"    ${param.sort=='from_desc'?'selected':''}>Bắt đầu ↓</option>
      </select>

      <select name="size" id="sizeSel" aria-label="Số bản ghi/trang">
        <c:set var="sizeVal" value="${empty param.size ? 20 : param.size}" />
        <option value="10"  ${sizeVal=='10'?'selected':''}>10</option>
        <option value="20"  ${sizeVal=='20'?'selected':''}>20</option>
        <option value="50"  ${sizeVal=='50'?'selected':''}>50</option>
        <option value="100" ${sizeVal=='100'?'selected':''}>100</option>
      </select>

      <button class="btn hide-when-js" type="submit">Lọc</button>
    </form>
  </div>

  <!-- Thông báo -->
  <c:if test="${not empty requestScope.message}">
    <div class="msg ok">${requestScope.message}</div>
  </c:if>
  <c:if test="${not empty requestScope.error}">
    <div class="msg no">${requestScope.error}</div>
  </c:if>

  <div class="card">
    <header class="card-head">
      <div class="muted">
        Tổng: <b>${totalItems}</b> đơn • Trang <b>${page}</b>/<b>${totalPages}</b> • Mỗi trang <b>${empty param.size ? 20 : param.size}</b>
      </div>
      <div class="card-actions">
        <button id="densityToggle" class="btn btn-ghost" title="Đổi mật độ hiển thị">↕️ Density</button>
        <button id="themeToggle"   class="btn btn-ghost" title="Đổi theme">🌓 Theme</button>
      </div>

      <c:if test="${sessionScope.role == 'MANAGER'}">
        <form id="bulkForm" method="post" action="${pageContext.request.contextPath}/request/bulk" class="bulk">
          <input type="hidden" name="csrf" value="${sessionScope.csrfToken}">
          <span class="muted">Chọn: <b id="selCount">0</b></span>
          <select name="action" id="bulkAction">
            <option value="">Bulk action…</option>
            <option value="approve">Duyệt</option>
            <option value="reject">Từ chối</option>
            <option value="cancel">Hủy</option>
          </select>
          <input name="note" placeholder="Ghi chú (tuỳ chọn)" />
          <button class="btn" type="submit" id="bulkSubmit" disabled>Thực hiện</button>
        </form>
      </c:if>
    </header>

    <!-- Bảng -->
    <div class="table-scroll">
      <table id="reqTable">
        <thead>
        <tr>
          <th class="row-select">
            <c:if test="${sessionScope.role == 'MANAGER'}">
              <input type="checkbox" id="chkAll" title="Chọn tất cả (A)">
            </c:if>
          </th>
          <th style="width:80px">ID</th>
          <th>Nội dung</th>
          <th>Người tạo</th>
          <th>Khoảng thời gian</th>
          <th>Số ngày</th>
          <th>Trạng thái</th>
          <th>Người xử lý</th>
          <th style="width:260px">Thao tác</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="r" items="${requests}">
          <tr id="row${r.id}">
            <td>
              <c:if test="${sessionScope.role == 'MANAGER'}">
                <input type="checkbox" class="rowChk" name="ids" form="bulkForm" value="${r.id}">
              </c:if>
            </td>
            <td>
              <span class="mono">#${r.id}</span>
              <button class="btn-icon tiny" data-copy="#${r.id}" title="Copy ID">⧉</button>
            </td>
            <td title="${fn:escapeXml(r.title)}">
              <div class="cell-reason clamp" data-expand>
                ${fn:escapeXml(r.reason)}
              </div>
            </td>
            <td>#${r.createdBy} <c:if test="${not empty r.createdByName}">– ${r.createdByName}</c:if></td>
            <td>
              <c:choose>
                <c:when test="${not empty r.startDate}">
                  <fmt:parseDate value="${r.startDate}" pattern="yyyy-MM-dd" var="sd"/>
                  <fmt:parseDate value="${r.endDate}"   pattern="yyyy-MM-dd" var="ed"/>
                  <fmt:formatDate value="${sd}" pattern="dd/MM/yyyy"/> – <fmt:formatDate value="${ed}" pattern="dd/MM/yyyy"/>
                </c:when>
                <c:otherwise>—</c:otherwise>
              </c:choose>
            </td>
            <td>
              <c:if test="${not empty r.startDate}">
                <fmt:parseDate value="${r.startDate}" pattern="yyyy-MM-dd" var="sd2"/>
                <fmt:parseDate value="${r.endDate}"   pattern="yyyy-MM-dd" var="ed2"/>
                <c:set var="days" value="${(ed2.time - sd2.time) / (1000*60*60*24) + 1}" />
                ${days}
              </c:if>
            </td>
            <td>
              <span class="pill ${fn:toUpperCase(r.status)}">
                <c:choose>
                  <c:when test="${fn:toUpperCase(r.status)=='PENDING'}">⏳</c:when>
                  <c:when test="${fn:toUpperCase(r.status)=='APPROVED'}">✅</c:when>
                  <c:when test="${fn:toUpperCase(r.status)=='REJECTED'}">⛔</c:when>
                  <c:otherwise>🗑</c:otherwise>
                </c:choose>
                ${fn:toUpperCase(r.status)}
              </span>
            </td>
            <td>
              <c:if test="${not empty r.processedBy}">#${r.processedBy} <c:if test="${not empty r.processedByName}">– ${r.processedByName}</c:if></c:if>
              <c:if test="${empty r.processedBy}"><span class="muted">—</span></c:if>
            </td>
            <td class="table-actions">
              <a class="btn-icon" title="Xem"
                 href="<c:url value='/request/detail'><c:param name='id' value='${r.id}'/></c:url>">Xem</a>

              <c:if test="${sessionScope.role == 'MANAGER' && fn:toLowerCase(r.status) == 'pending'}">
                <button class="btn-icon ok"   data-open-approve data-id="${r.id}" title="Duyệt">Duyệt</button>
                <button class="btn-icon warn" data-open-reject  data-id="${r.id}" title="Từ chối">Từ chối</button>
              </c:if>

              <c:if test="${fn:toLowerCase(r.status) == 'pending' && r.createdBy == sessionScope.userId}">
                <a class="btn-icon danger" href="#" data-cancel data-id="${r.id}" title="Hủy">Hủy</a>
              </c:if>

<a class="btn" href="${pageContext.request.contextPath}/request/duplicate?id=${r.id}">
  Nhân bản
</a>
            </td>
          </tr>
        </c:forEach>

        <c:if test="${empty requests}">
          <tr><td colspan="9" class="empty">Không có dữ liệu phù hợp bộ lọc.</td></tr>
        </c:if>
        </tbody>
      </table>
    </div>

    <!-- Phân trang -->
    <c:if test="${totalPages > 1}">
      <div class="pagination">
        <c:url var="baseUrl" value="/request/list">
          <c:param name="q" value="${param.q}" />
          <c:param name="status" value="${param.status}" />
          <c:param name="mine" value="${param.mine}" />
          <c:param name="from" value="${param.from}" />
          <c:param name="to" value="${param.to}" />
          <c:param name="sort" value="${param.sort}" />
          <c:param name="size" value="${empty param.size ? 20 : param.size}" />
        </c:url>

        <c:if test="${page>1}">
          <a class="page" href="${baseUrl}&page=1" aria-label="Về đầu">«</a>
          <a class="page" href="${baseUrl}&page=${page-1}" aria-label="Trang trước">‹</a>
        </c:if>

        <c:forEach var="p" begin="${page-2 < 1 ? 1 : page-2}" end="${page+2 > totalPages ? totalPages : page+2}">
          <c:choose>
            <c:when test="${p == page}">
              <span class="page current" aria-current="page">${p}</span>
            </c:when>
            <c:otherwise>
              <a class="page" href="${baseUrl}&page=${p}">${p}</a>
            </c:otherwise>
          </c:choose>
        </c:forEach>

        <c:if test="${page<totalPages}">
          <a class="page" href="${baseUrl}&page=${page+1}" aria-label="Trang sau">›</a>
          <a class="page" href="${baseUrl}&page=${totalPages}" aria-label="Về cuối">»</a>
        </c:if>
      </div>
    </c:if>
  </div>
</div>

<!-- Bulk sticky bar -->
<c:if test="${sessionScope.role == 'MANAGER'}">
  <div class="bulkbar" id="bulkbar">
    <span class="badgeSel">Đã chọn: <b id="selCount2">0</b></span>
    <button class="btn" type="button" id="selAllPage">Chọn tất cả trang</button>
    <button class="btn" type="button" id="selNone">Bỏ chọn</button>
    <button class="btn" type="button" id="gotoTop">Lên đầu</button>
  </div>
</c:if>

<!-- Modal approve/reject -->
<dialog id="approveDlg" class="modal">
  <form method="post" action="${pageContext.request.contextPath}/request/approve">
    <div class="modal-head">Duyệt đơn</div>
    <input type="hidden" name="id" id="approveId">
    <input type="hidden" name="ok" value="1">
    <input type="hidden" name="csrf" value="${sessionScope.csrfToken}">
    <label>Ghi chú (tuỳ chọn)</label>
    <textarea name="note" rows="3" class="w100"></textarea>
    <div class="modal-actions">
      <button type="button" class="btn btn-ghost" data-close>Đóng</button>
      <button class="btn btn-primary" type="submit">Xác nhận duyệt</button>
    </div>
  </form>
</dialog>

<dialog id="rejectDlg" class="modal">
  <form method="post" action="${pageContext.request.contextPath}/request/approve">
    <div class="modal-head">Từ chối đơn</div>
    <input type="hidden" name="id" id="rejectId">
    <input type="hidden" name="ok" value="0">
    <input type="hidden" name="csrf" value="${sessionScope.csrfToken}">
    <label>Lý do từ chối (bắt buộc)</label>
    <textarea name="note" rows="3" required class="w100"></textarea>
    <div class="modal-actions">
      <button type="button" class="btn btn-ghost" data-close>Đóng</button>
      <button class="btn danger" type="submit">Xác nhận từ chối</button>
    </div>
  </form>
</dialog>

<!-- Cancel form -->
<form id="cancelForm" method="post" action="${pageContext.request.contextPath}/request/cancel" class="hide">
  <input type="hidden" name="id" id="cancelId">
  <input type="hidden" name="csrf" value="${sessionScope.csrfToken}">
</form>

<!-- Toast + Loading -->
<div id="toast" class="toast" role="status" aria-live="polite"></div>
<div id="loading" class="loading" hidden>
  <div class="spinner"></div><span>Đang tải…</span>
</div>
<script src="${pageContext.request.contextPath}/assets/js/theme.js"></script>

<%@ include file="/WEB-INF/views/common/_footer.jsp" %>
</body>
</html>
