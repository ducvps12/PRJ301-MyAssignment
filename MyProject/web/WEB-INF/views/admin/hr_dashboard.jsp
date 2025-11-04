<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi" data-theme="${sessionScope.theme != null ? sessionScope.theme : 'auto'}">
<head>
  <meta charset="UTF-8">
  <title>HR Dashboard · LeaveMgmt</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <meta name="color-scheme" content="light dark">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/hr_admin.css?v=5">
</head>
<body>
  <fmt:setLocale value="vi_VN"/>
  <fmt:setTimeZone value="Asia/Ho_Chi_Minh"/>

  <!-- ===== App Shell ===== -->
  <div class="app">
    <!-- Sidebar -->
    <aside class="sidebar" id="sidebar">
      <div class="brand">
        <div class="logo">👔</div>
        <div class="brand-text">
          <strong>LeaveMgmt</strong>
          <small>HR Console</small>
        </div>
        <button class="sidebar-toggle" id="sidebarToggle" title="Thu gọn (Ctrl+B)">⟷</button>
      </div>

      <nav class="nav">
        <a class="nav-item active" href="${pageContext.request.contextPath}/admin/hr">🏠 Dashboard</a>
        <a class="nav-item" href="${pageContext.request.contextPath}/admin/users">👥 Nhân sự</a>
        <a class="nav-item" href="${pageContext.request.contextPath}/admin/requests">📝 Yêu cầu nghỉ</a>
        <a class="nav-item" href="${pageContext.request.contextPath}/admin/divisions">🏢 Phòng ban</a>
        <div class="nav-section">Tiện ích</div>
        <button class="nav-item btn-link" id="btnExportCsv">⬇️ Export “Nghỉ hôm nay”</button>
        <button class="nav-item btn-link" id="btnRefresh">🔄 Làm mới</button>
      </nav>

      <div class="sidebar-footer">
        <div class="mini-kpis">
          <div class="mini-kpi">
            <div class="label">Tổng NS</div>
            <div class="val">${totalEmployees}</div>
          </div>
          <div class="mini-kpi">
            <div class="label">Đang nghỉ</div>
            <div class="val">${onLeaveToday}</div>
          </div>
          <div class="mini-kpi">
            <div class="label">Intern</div>
            <div class="val">${interns}</div>
          </div>
          <div class="mini-kpi">
            <div class="label">HĐ ≤30d</div>
            <div class="val">${contractEndingSoon}</div>
          </div>
        </div>
        <div class="muted">© <fmt:formatDate value="<%= new java.util.Date() %>" pattern="yyyy"/> LeaveMgmt</div>
      </div>
    </aside>

    <!-- Main -->
    <main class="main">
      <!-- Header -->
      <header class="header">
        <div class="left">
          <h1>HR Dashboard</h1>
          <span class="chip">Admin</span>
          <span class="chip chip-muted">${empty sessionScope.currentUser ? 'Chưa đăng nhập' : sessionScope.currentUser.username}</span>
          <span class="chip chip-mode">Mode: <strong>dev</strong></span>
        </div>
        <div class="right">
          <div class="search">
            <input id="searchInput" type="search" placeholder="Tìm nhân sự đang nghỉ…" autocomplete="off">
            <button id="searchClear" title="Xóa">✕</button>
          </div>
          <button id="themeToggle" class="icon-btn" title="Đổi theme (Ctrl+J)">🌓</button>
          <a class="icon-btn" title="Lên đầu trang" href="#top">↑</a>
        </div>
      </header>

      <!-- KPI Cards -->
      <section class="cards">
        <div class="card kpi">
          <div class="kpi-label">Tổng nhân sự</div>
          <div class="kpi-value">${totalEmployees}</div>
          <div class="kpi-foot">Tăng sự gắn kết & hiệu suất</div>
        </div>
        <div class="card kpi">
          <div class="kpi-label">Đang nghỉ hôm nay</div>
          <div class="kpi-value">${onLeaveToday}</div>
          <div class="kpi-foot"><span class="dot dot-green"></span> Hệ thống chạy ổn định</div>
        </div>
        <div class="card kpi">
          <div class="kpi-label">Intern</div>
          <div class="kpi-value">${interns}</div>
          <div class="kpi-foot">Chương trình thực tập</div>
        </div>
        <div class="card kpi">
          <div class="kpi-label">Sắp hết HĐ (≤30d)</div>
          <div class="kpi-value">${contractEndingSoon}</div>
          <div class="kpi-foot">Cần gia hạn/đánh giá</div>
        </div>
      </section>

      <!-- Table: On leave today -->
      <section class="panel">
        <div class="panel-head">
          <h2>Nghỉ hôm nay</h2>
          <div class="actions">
            <select id="pageSize">
              <option value="5">5 hàng</option>
              <option value="10" selected>10 hàng</option>
              <option value="20">20 hàng</option>
            </select>
            <button class="btn" id="btnSortName">Sắp xếp theo tên</button>
            <button class="btn" id="btnSortFrom">Sắp xếp theo ngày</button>
          </div>
        </div>

        <div class="table-wrap">
          <table class="table" id="leaveTable">
            <thead>
              <tr>
                <th data-key="name">Nhân sự</th>
                <th data-key="division">Phòng ban</th>
                <th data-key="from">Từ</th>
                <th data-key="to">Đến</th>
              </tr>
            </thead>
            <tbody id="leaveTbody" data-empty-text="Hôm nay không có ai nghỉ.">
              <c:forEach items="${todayLeaves}" var="r">
                <tr>
                  <td data-col="name"><c:out value="${r.fullName}"/></td>
                  <td data-col="division"><c:out value="${empty r.divisionName ? '—' : r.divisionName}"/></td>
                  <td data-col="from">
                    <c:choose>
                      <c:when test="${not empty r.startDate}">
                        <fmt:formatDate value="${r.startDate}" pattern="dd/MM/yyyy"/>
                      </c:when>
                      <c:otherwise>—</c:otherwise>
                    </c:choose>
                  </td>
                  <td data-col="to">
                    <c:choose>
                      <c:when test="${not empty r.endDate}">
                        <fmt:formatDate value="${r.endDate}" pattern="dd/MM/yyyy"/>
                      </c:when>
                      <c:otherwise>—</c:otherwise>
                    </c:choose>
                  </td>
                </tr>
              </c:forEach>
            </tbody>
          </table>

          <!-- Skeleton khi chưa có dữ liệu (được ẩn đi bằng JS khi có tbody > 0) -->
          <div class="skeleton" id="skeleton" aria-hidden="true">
            <div class="sk-row"></div>
            <div class="sk-row"></div>
            <div class="sk-row"></div>
          </div>
        </div>

        <div class="pager">
          <button class="btn" id="prevPage">‹ Trước</button>
          <span id="pageInfo">Trang 1/1</span>
          <button class="btn" id="nextPage">Sau ›</button>
        </div>
      </section>

      <!-- Footer -->
      <footer class="footer">
        <div>© <fmt:formatDate value="<%= new java.util.Date() %>" pattern="yyyy"/> LeaveMgmt • Made with ❤️</div>
        <div class="foot-right">
          <a href="#" class="link">Điều khoản</a>
          <a href="#" class="link">Bảo mật</a>
          <a href="#" class="link" id="toTop">↑ Top</a>
        </div>
      </footer>
    </main>
  </div>

  <script>
    // Dữ liệu thô từ server -> JS (chỉ những field cần thiết)
    window.__LEAVES__ = (function () {
      const rows = [];
      <%-- inject từng hàng thành object nhẹ --%>
      <c:forEach items="${todayLeaves}" var="r">
        rows.push({
          name: "<c:out value='${r.fullName}'/>",
          division: "<c:out value='${empty r.divisionName ? "-" : r.divisionName}'/>",
          from: "<c:choose><c:when test='${not empty r.startDate}'><fmt:formatDate value='${r.startDate}' pattern='dd/MM/yyyy'/></c:when><c:otherwise>-</c:otherwise></c:choose>",
          to: "<c:choose><c:when test='${not empty r.endDate}'><fmt:formatDate value='${r.endDate}' pattern='dd/MM/yyyy'/></c:when><c:otherwise>-</c:otherwise></c:choose>"
        });
      </c:forEach>
      return rows;
    })();
  </script>
  <script src="${pageContext.request.contextPath}/assets/js/admin-hr.js?v=5"></script>
</body>
</html>
