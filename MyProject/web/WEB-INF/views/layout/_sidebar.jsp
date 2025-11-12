<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
  String cp = request.getContextPath();
%>
  <aside class="sidebar">
    <nav>
      <a href="<%=cp%>/">Trang chủ</a>
      <a href="<%=cp%>/request/list">Đơn của tôi</a>
      <a href="<%=cp%>/request/new">Tạo đơn</a>
      <a href="<%=cp%>/request/approvals">Phê duyệt</a>
    </nav>
  </aside>
  <main class="content">
