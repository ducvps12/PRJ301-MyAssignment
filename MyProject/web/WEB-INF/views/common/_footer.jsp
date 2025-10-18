<footer class="app-footer">
  <div>© <fmt:formatDate value="<%= new java.util.Date() %>" pattern="yyyy"/> LeaveMgmt  Admin Console</div>
</footer>
<style>
  .app-footer{margin-top:24px;padding:16px;text-align:center;color:var(--muted)}
  .container{max-width:1100px;margin:24px auto;padding:0 16px}
  .card{background:#fff;border:1px solid var(--bd);border-radius:12px;box-shadow:0 1px 2px rgba(0,0,0,.04)}
  .card-header{padding:12px 16px;border-bottom:1px solid #f0f1f3;display:flex;justify-content:space-between;align-items:center;font-weight:600}
  .table{width:100%;border-collapse:collapse}
  .table th,.table td{padding:10px 12px;border-bottom:1px solid #f3f4f6;text-align:left}
  .btn{display:inline-block;padding:8px 12px;border:1px solid var(--bd);border-radius:10px;background:#fff;text-decoration:none;color:#111827}
  .btn.small{font-size:12px;padding:6px 10px}
  .btn.active{background:#111827;color:#fff;border-color:#111827}
  .input{padding:10px 12px;border:1px solid var(--bd);border-radius:10px;width:100%}
  .badge{display:inline-block;padding:4px 10px;border-radius:999px;font-size:12px;border:1px solid var(--bd)}
  .badge.Inprogress{background:#fff7ed}
  .badge.Approved{background:#ecfeff}
  .badge.Rejected{background:#fef2f2}
</style>
