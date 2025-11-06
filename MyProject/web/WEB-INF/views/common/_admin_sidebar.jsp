<%-- YÊU CẦU: đã include /WEB-INF/views/common/_taglibs.jsp trước block này
     (c, fmt, fn dùng URI jakarta.tags.* cho Tomcat 10) --%>

<%-- ===== Chuẩn hoá role & set cờ ===== --%>
<c:set var="u" value="${sessionScope.currentUser}" />
<c:set var="rawRole"
       value="${empty u ? '' : (not empty u.roleCode ? u.roleCode : (not empty u.role ? u.role : ''))}" />
<c:set var="R" value="${fn:toUpperCase(fn:trim(rawRole))}" />

<c:set var="isAdmin"      value="${R eq 'ADMIN'}"/>
<c:set var="isSysAdmin"   value="${R eq 'SYS_ADMIN'}"/>
<c:set var="isHRAdmin"    value="${R eq 'HR_ADMIN'}"/>
<c:set var="isHRStaff"    value="${R eq 'HR_STAFF'}"/>
<c:set var="isDeptMgr"    value="${R eq 'DEPT_MANAGER'}"/>
<c:set var="isDivLead"    value="${R eq 'DIV_LEADER'}"/>
<c:set var="isTeamLead"   value="${R eq 'TEAM_LEAD'}"/>
<c:set var="isStaff"      value="${R eq 'STAFF' or R eq 'PROBATION' or R eq 'INTERN'}"/>
<c:set var="isRestricted" value="${R eq 'SUSPENDED' or R eq 'UNDER_REVIEW' or R eq 'TERMINATED'}"/>

<aside class="sb" id="sidebar" aria-label="Main navigation">
  <div class="brand">
    <div class="meta">
      <b>Admin Panel</b>
      <small>Department: <c:out value="${viewDepartment}"/> · Role: <c:out value="${R != '' ? R : 'GUEST'}"/></small>
    </div>
    <div class="actions">
      <button class="btn" id="btnExpandAll" title="Mở/đóng toàn bộ">Expand</button>
      <button class="btn" id="btnMini" title="Thu gọn sidebar">Mini</button>
    </div>
  </div>

  <nav role="navigation">

    <!-- ADMIN -->
    <c:if test="${isAdmin}">
      <div class="sec">Tổng quan</div>
      <a class="mb8" href="${ctx}/admin">Dashboard</a>
      <div class="divider"></div>
      <details data-key="sb-ops">
        <summary>Vận hành hệ thống</summary>
        <div class="group">
          <a href="${ctx}/admin/users">Tài khoản</a>
          <a href="${ctx}/admin/notifications">Thông báo</a>
          <a href="${ctx}/admin/support">Hỗ trợ</a>
          <a href="${ctx}/admin/settings">Cấu hình (Sys_Settings)</a>
        </div>
      </details>
      <details data-key="sb-audit">
        <summary>Giám sát & báo cáo</summary>
        <div class="group">
          <a href="${ctx}/admin/audit">Nhật ký hệ thống</a>
          <a href="${ctx}/admin/reports">Bộ báo cáo</a>
        </div>
      </details>
      <div class="divider"></div>
      <a href="${ctx}/">Trang chủ</a>
    </c:if>

    <!-- SYS_ADMIN -->
    <c:if test="${isSysAdmin}">
      <div class="sec">Tổng quan</div>
      <a class="mb8" href="${ctx}/admin">Dashboard</a>
      <a class="mb8" href="${ctx}/admin/div">Division Dashboard</a>
      <div class="divider"></div>
      <details data-key="sb-ops">
        <summary>Vận hành hệ thống</summary>
        <div class="group">
          <a href="${ctx}/admin/users">Tài khoản</a>
          <a href="${ctx}/admin/notifications">Thông báo</a>
          <a href="${ctx}/admin/support">Hỗ trợ</a>
          <a href="${ctx}/admin/settings">Cấu hình (Sys_Settings)</a>
        </div>
      </details>
      <details data-key="sb-audit">
        <summary>Giám sát & báo cáo</summary>
        <div class="group">
          <a href="${ctx}/admin/audit">Nhật ký hệ thống</a>
          <a href="${ctx}/admin/reports">Bộ báo cáo</a>
        </div>
      </details>
      <div class="divider"></div>
      <a href="${ctx}/">Trang chủ</a>
    </c:if>

    <!-- HR -->
    <c:if test="${isHRAdmin or isHRStaff}">
      <div class="sec">Tổng quan</div>
      <a class="mb8" href="${ctx}/admin">Dashboard</a>
      <div class="divider"></div>
      <details data-key="sb-org">
        <summary>Tổ chức & nhân sự</summary>
        <div class="group">
          <a href="${ctx}/admin/hr/employees">Nhân sự (Users)</a>
          <c:if test="${isHRAdmin}">
            <a href="${ctx}/admin/hr/roles">Vai trò (Roles)</a>
            <a href="${ctx}/admin/hr/role-history">Lịch sử vai trò</a>
            <a href="${ctx}/admin/hr/departments">Phòng ban</a>
            <a href="${ctx}/admin/hr/divisions">Khối/nhóm</a>
            <a href="${ctx}/admin/hr/titles">Chức danh</a>
            <a href="${ctx}/admin/hr/employment-statuses">Trạng thái làm việc</a>
          </c:if>
        </div>
      </details>
      <details data-key="sb-leave-master">
        <summary>Danh mục nghỉ phép</summary>
        <div class="group">
          <a href="${ctx}/admin/hr/leave-types">Loại nghỉ</a>
          <a href="${ctx}/admin/hr/approve-rules">Quy tắc duyệt</a>
          <a href="${ctx}/admin/hr/user-leave-balances">Tồn phép</a>
          <a href="${ctx}/admin/hr/holidays">Ngày nghỉ</a>
        </div>
      </details>
      <details data-key="sb-audit">
        <summary>Báo cáo</summary>
        <div class="group">
          <a href="${ctx}/admin/reports">Bộ báo cáo</a>
        </div>
      </details>
      <div class="divider"></div>
      <a href="${ctx}/">Trang chủ</a>
    </c:if>

    <!-- LÃNH ĐẠO -->
    <c:if test="${isDivLead or isDeptMgr or isTeamLead}">
      <div class="sec">Tổng quan</div>
      <a class="mb8" href="${ctx}/admin">Dashboard</a>
      <a class="mb8" href="${ctx}/admin/div">Division Dashboard</a>
      <div class="divider"></div>
      <details data-key="sb-requests">
        <summary>Đơn nghỉ phép</summary>
        <div class="group">
          <a href="${ctx}/request/list">Tất cả đơn</a>
          <a href="${ctx}/request/list?me=1">Đơn của tôi</a>
          <a href="${ctx}/admin/requests/pending">Đang chờ duyệt</a>
          <a href="${ctx}/admin/requests?status=APPROVED">Đã duyệt</a>
          <a href="${ctx}/admin/requests?status=REJECTED">Từ chối</a>
          <a href="${ctx}/request/new">Tạo đơn mới</a>
        </div>
      </details>
      <details data-key="sb-audit">
        <summary>Báo cáo</summary>
        <div class="group">
          <a href="${ctx}/admin/reports/requests/daily">Theo ngày</a>
          <a href="${ctx}/admin/reports/requests/monthly">Theo tháng</a>
        </div>
      </details>
      <div class="divider"></div>
      <a href="${ctx}/">Trang chủ</a>
    </c:if>

    <!-- NHÂN VIÊN -->
    <c:if test="${isStaff}">
      <div class="sec">Tổng quan</div>
      <a class="mb8" href="${ctx}/admin">Dashboard</a>
      <div class="divider"></div>
      <details data-key="sb-requests">
        <summary>Đơn nghỉ phép</summary>
        <div class="group">
          <a href="${ctx}/request/list?me=1">Đơn của tôi</a>
          <a href="${ctx}/request/new">Tạo đơn mới</a>
        </div>
      </details>
      <div class="divider"></div>
      <a href="${ctx}/">Trang chủ</a>
    </c:if>

    <!-- RESTRICTED -->
    <c:if test="${isRestricted}">
      <div class="sec">Tài khoản</div>
      <a class="mb8" href="${ctx}/profile">Hồ sơ của tôi</a>
      <div class="divider"></div>
      <a href="${ctx}/">Trang chủ</a>
    </c:if>

    <!-- GUEST -->
    <c:if test="${empty R}">
      <div class="sec">Chung</div>
      <a class="mb8" href="${ctx}/login">Đăng nhập</a>
      <a href="${ctx}/">Trang chủ</a>
    </c:if>

  </nav>
</aside>
