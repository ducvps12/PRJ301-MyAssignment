<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8" />
  <title>Admin · LeaveMgmt</title>
  <meta name="viewport" content="width=device-width,initial-scale=1" />
  <style>
    :root{
      --bg:#f7f9fc; --card:#fff; --tx:#0f172a; --muted:#64748b; --bd:#e5e7eb;
      --pri:#2563eb; --ok:#16a34a; --warn:#f59e0b; --err:#dc2626;
    }
    @media (prefers-color-scheme: dark){
      :root{
        --bg:#0b1220; --card:#0f172a; --tx:#e5e7eb; --muted:#9aa5b1; --bd:#1f2937;
        --pri:#60a5fa; --ok:#22c55e; --warn:#fbbf24; --err:#f87171;
      }
    }
    *{box-sizing:border-box}
    body{margin:0;font-family:system-ui,Segoe UI,Roboto,Arial,sans-serif;background:var(--bg);color:var(--tx)}
    a{color:inherit;text-decoration:none}
    .layout{display:grid;grid-template-columns:240px 1fr;min-height:100vh}
    .sidebar{background:var(--card);border-right:1px solid var(--bd)}
    .main{display:flex;flex-direction:column}
    .topbar{height:56px;background:var(--card);border-bottom:1px solid var(--bd);display:flex;align-items:center;justify-content:space-between;padding:0 16px;position:sticky;top:0;z-index:10}
    .brand{font-weight:700}
    .content{padding:20px}
    .muted{color:var(--muted)}
    .kpis{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:12px}
    .card{background:var(--card);border:1px solid var(--bd);border-radius:12px;padding:16px}
    .card h3{margin:0 0 6px 0;font-size:14px;color:var(--muted);font-weight:600}
    .card .num{font-size:22px;font-weight:700}
    .table{width:100%;border-collapse:separate;border-spacing:0;border:1px solid var(--bd);border-radius:12px;overflow:hidden;background:var(--card)}
    .table th,.table td{padding:10px 12px;border-bottom:1px solid var(--bd);font-size:14px}
    .table th{background:rgba(0,0,0,.03);text-align:left;color:var(--muted);font-weight:600}
    .status{padding:4px 8px;border-radius:999px;font-size:12px;border:1px solid var(--bd)}
    .status.APPROVED{background:rgba(34,197,94,.12);border-color:rgba(34,197,94,.3)}
    .status.PENDING{background:rgba(245,158,11,.12);border-color:rgba(245,158,11,.3)}
    .status.REJECTED{background:rgba(239,68,68,.12);border-color:rgba(239,68,68,.3)}
    .grid{display:grid;grid-template-columns:2fr 1fr;gap:12px}
    @media (max-width: 980px){
      .layout{grid-template-columns:1fr}
      .sidebar{position:fixed;inset:0 auto 0 0;width:260px;transform:translateX(-100%);transition:.2s;z-index:20}
      .sidebar.open{transform:none}
      .content{padding:14px}
      .kpis{grid-template-columns:repeat(2,minmax(0,1fr))}
      .grid{grid-template-columns:1fr}
    }
    .btn{display:inline-flex;align-items:center;gap:8px;border:1px solid var(--bd);padding:8px 12px;border-radius:10px;background:var(--card)}
    .pill{padding:6px 10px;border:1px solid var(--bd);border-radius:999px;background:var(--card);font-size:12px}
  </style>
  <script>
    function toggleSidebar(){ document.querySelector('.sidebar')?.classList.toggle('open'); }
  </script>
</head>
<body>
<div class="layout">
