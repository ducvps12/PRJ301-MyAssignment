<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/views/common/_admin_header.jsp" %>
<%@ include file="/WEB-INF/views/common/_admin_sidebar.jsp" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<style>
  .settings-page{
    padding:20px 24px 60px;
  }
  .settings-head{
    display:flex;
    justify-content:space-between;
    align-items:center;
    margin-bottom:16px;
  }
  .settings-table{
    width:100%;
    border-collapse:separate;
    border-spacing:0 6px;
  }
  .settings-table th{
    text-align:left;
    padding:10px 12px;
    background:#f4f4f5;
    font-size:13px;
    color:#4b5563;
  }
  .settings-table td{
    background:#fff;
    padding:6px 12px;
    border-bottom:1px solid #eee;
    vertical-align:middle;
  }
  .settings-table tr:hover td{
    background:#f9fafb;
  }
  .input-sm{
    width:100%;
    padding:6px 8px;
    border:1px solid #d1d5db;
    border-radius:6px;
    font-size:13px;
  }
  .pill{
    display:inline-block;
    padding:2px 10px;
    background:#e0f2fe;
    color:#0369a1;
    border-radius:9999px;
    font-size:12px;
  }
  .btn-primary{
    background:#0f766e;
    color:#fff;
    border:none;
    border-radius:6px;
    padding:7px 14px;
    cursor:pointer;
    font-weight:500;
  }
  .btn-secondary{
    background:#e5e7eb;
    color:#111827;
    border:none;
    border-radius:6px;
    padding:7px 14px;
    cursor:pointer;
  }
  .new-setting{
    margin-top:28px;
    background:#fff;
    padding:14px 16px;
    border-radius:12px;
    border:1px solid #e5e7eb;
  }
  .badge-ok{color:#16a34a;font-weight:500;}
</style>

<div class="main-body">
  <div class="container settings-page">
    <div class="settings-head">
      <div>
        <h2 style="margin:0;">Cấu hình hệ thống</h2>
        <p style="margin:4px 0 0;color:#6b7280;font-size:13px;">
          Sửa nhanh các thông số giống panel PHP (key – value).
        </p>
      </div>
      <c:if test="${param.ok == '1'}">
        <span class="badge-ok">Đã lưu cấu hình ✔</span>
      </c:if>
    </div>

    <form method="post" action="${ctx}/admin/settings">
      <input type="hidden" name="csrf_token" value="${sessionScope.csrf_token}" />
      <input type="hidden" name="action" value="save" />

      <table class="settings-table">
        <thead>
          <tr>
            <th style="width:180px;">Key</th>
            <th>Giá trị</th>
            <th style="width:110px;">Nhóm</th>
            <th style="width:110px;">Kiểu</th>
            <th>Mô tả</th>
            <th style="width:80px;">#</th>
          </tr>
        </thead>
        <tbody>
        <c:forEach var="s" items="${settings}">
          <tr>
            <td>
              <strong>${s.key}</strong>
            </td>
            <td>
              <input class="input-sm"
                     type="text"
                     name="val_${s.id}"
                     value="${s.value}"/>
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
            <td>${s.dataType}</td>
            <td style="font-size:12px;color:#6b7280;">${s.description}</td>
            <td>
              <c:if test="${!s.active}">
                <span style="color:#ef4444;font-size:12px;">OFF</span>
              </c:if>
            </td>
          </tr>
        </c:forEach>
        </tbody>
      </table>

      <div style="margin-top:14px;">
        <button type="submit" class="btn-primary">💾 Lưu thay đổi</button>
      </div>
    </form>

    <!-- form tạo mới -->
    <form method="post" action="${ctx}/admin/settings" class="new-setting">
      <input type="hidden" name="csrf_token" value="${sessionScope.csrf_token}" />
      <input type="hidden" name="action" value="create" />
      <h4 style="margin-top:0;margin-bottom:10px;">Thêm cấu hình mới</h4>
      <div style="display:grid;grid-template-columns:200px 1fr 130px 1fr;gap:10px;">
        <div>
          <label>Key</label>
          <input class="input-sm" name="new_key" placeholder="vd: site_phone" required />
        </div>
        <div>
          <label>Giá trị</label>
          <input class="input-sm" name="new_value" />
        </div>
        <div>
          <label>Nhóm</label>
          <input class="input-sm" name="new_group" placeholder="System/Mail/HR" />
        </div>
        <div>
          <label>Kiểu</label>
          <select name="new_type" class="input-sm">
            <option value="string">string</option>
            <option value="int">int</option>
            <option value="bool">bool</option>
            <option value="json">json</option>
            <option value="html">html</option>
          </select>
        </div>
      </div>
      <div style="margin-top:10px;">
        <label>Mô tả</label>
        <input class="input-sm" name="new_desc" />
      </div>
      <div style="margin-top:12px;">
        <button class="btn-secondary" type="submit">➕ Thêm</button>
      </div>
    </form>

  </div>
</div>

<%@ include file="/WEB-INF/views/common/_admin_footer.jsp" %>
