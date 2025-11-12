<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp" %>
<%
  String cp = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title><c:out value="${empty pageTitle ? 'LeaveMgmt' : pageTitle}"/></title>
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <style>
    body{margin:0;background:#f3f4f6;color:#0f172a;font-family:system-ui,-apple-system,Segoe UI,Roboto,Helvetica,Arial}
    .topbar{height:52px;display:flex;align-items:center;justify-content:space-between;padding:0 16px;background:#111827;color:#fff}
    .topbar a{color:#fff;text-decoration:none}
    .layout{display:grid;grid-template-columns:240px 1fr;min-height:calc(100dvh - 52px)}
    .sidebar{background:#111827;color:#cbd5e1}
    .sidebar a{display:block;color:#cbd5e1;text-decoration:none;padding:10px 14px;border-left:3px solid transparent}
    .sidebar a:hover{background:#1f2937;color:#fff;border-left-color:#22c55e}
    .content{padding:16px 18px}
  </style>
</head>
<body>
  <div class="topbar">
    <div><a href="<%=cp%>">LeaveMgmt</a></div>
    <div><c:out value="${sessionScope.currentUser != null ? sessionScope.currentUser.fullName : 'Guest'}"/></div>
  </div>
  <div class="layout">
    