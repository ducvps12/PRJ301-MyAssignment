<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp" %>
<jsp:include page="/WEB-INF/views/audit/_audit_header.jsp"/>
<jsp:include page="/WEB-INF/views/audit/_audit_sidebar.jsp"/>


<c:set var="cp" value="${pageContext.request.contextPath}"/>

<style>
  .r-wrap{padding:18px;max-width:1300px;margin:0 auto}
  .grid{display:grid;grid-template-columns:2fr 1fr;gap:14px}
  @media(max-width:1100px){.grid{grid-template-columns:1fr}}
  .card{background:var(--card,#fff);border:1px solid var(--bd,#e5e7eb);border-radius:14px;padding:14px}
  .toolbar{display:flex;gap:8px;align-items:center;flex-wrap:wrap;margin:0 0 10px}
  .input{border:1px solid var(--bd,#e5e7eb);border-radius:10px;padding:8px 10px;background:#fff}
  .btn{border:1px solid var(--bd,#e5e7eb);border-radius:10px;padding:8px 12px;background:#fff}
  .muted{color:var(--muted,#64748b)}
  table{width:100%;border-collapse:separate;border-spacing:0}
  th,td{padding:8px 10px;border-bottom:1px solid var(--bd,#e5e7eb);text-align:left}
</style>

<main class="r-wrap">
  <h2 style="margin:0 0 10px">Báo cáo đơn nghỉ – Theo ngày</h2>

  <form class="toolbar" method="get" action="">
    <label class="muted">Từ</label>
    <input class="input" type="date" name="from" value="${from}"/>
    <label class="muted">Đến</label>
    <input class="input" type="date" name="to" value="${to}"/>
    <label class="muted">Phòng ban (ID)</label>
    <input class="input" type="number" name="deptId" value="${param.deptId}"/>
    <label class="muted">Trạng thái</label>
    <select class="input" name="status">
      <option value="">-- All --</option>
<c:set var="statusList" value="PENDING,APPROVED,REJECTED,CANCELLED"/>
<c:forEach var="s" items="${fn:split(statusList, ',')}">
  <option value="${s}" <c:if test="${param.status eq s}">selected</c:if>>${s}</option>
</c:forEach>

    </select>
    <button class="btn" type="submit">Áp dụng</button>

    <c:url var="csvUrl" value="">
      <c:param name="from" value="${from}"/><c:param name="to" value="${to}"/>
      <c:param name="deptId" value="${param.deptId}"/><c:param name="status" value="${param.status}"/>
      <c:param name="export" value="csv"/>
    </c:url>
    <a class="btn" href="${csvUrl}">↯ Export CSV</a>
  </form>

  <div class="grid">
    <div class="card">
      <h3 style="margin:0 0 8px">Đơn phát sinh theo ngày</h3>
      <canvas id="line" width="1200" height="360"></canvas>
    </div>

    <div class="card">
      <h3 style="margin:0 0 8px">Theo trạng thái</h3>
      <canvas id="donut" width="600" height="260"></canvas>
    </div>

    <div class="card">
      <h3 style="margin:0 0 8px">Theo phòng ban</h3>
      <canvas id="bar" width="1200" height="320"></canvas>
    </div>

    <div class="card">
      <h3 style="margin:0 0 8px">Bảng dữ liệu</h3>
      <table>
        <thead><tr><th>Ngày</th><th>Số đơn</th></tr></thead>
        <tbody>
        <c:forEach var="e" items="${daily}">
          <tr><td>${e.key}</td><td>${e.value}</td></tr>
        </c:forEach>
        </tbody>
      </table>
    </div>
  </div>
</main>

<jsp:include page="/WEB-INF/views/audit/_audit_footer.jsp"/>

<script>
  // ----- dữ liệu từ server -----
  const dailyLabels=[<c:forEach var="e" items="${daily}" varStatus="s">${s.first?'':','}"${e.key}"</c:forEach>];
  const dailyCounts=[<c:forEach var="e" items="${daily}" varStatus="s">${s.first?'':','}${e.value}</c:forEach>];
  const byStatus   =[<c:forEach var="e" items="${byStatus}" varStatus="s">${s.first?'':','}["${e.key}",${e.value}]</c:forEach>];
  const byDept     =[<c:forEach var="e" items="${byDept}" varStatus="s">${s.first?'':','}["${e.key}",${e.value}]</c:forEach>];

  // ----- vẽ chart canvas mini -----
  function drawLine(ctx, labels, counts){
    const w=ctx.canvas.width,h=ctx.canvas.height,p=40,max=Math.max(1,...counts);
    const step=(h-2*p)/max, dx=(w-2*p)/Math.max(1,(counts.length-1));
    ctx.clearRect(0,0,w,h); ctx.strokeStyle='#cbd5e1'; ctx.beginPath(); ctx.moveTo(p,p); ctx.lineTo(p,h-p); ctx.lineTo(w-p,h-p); ctx.stroke();
    ctx.strokeStyle='#2563eb'; ctx.lineWidth=2; ctx.beginPath();
    counts.forEach((v,i)=>{const x=p+i*dx, y=h-p-v*step; i?ctx.lineTo(x,y):ctx.moveTo(x,y)}); ctx.stroke();
    ctx.fillStyle='#2563eb'; counts.forEach((v,i)=>{const x=p+i*dx, y=h-p-v*step; ctx.beginPath(); ctx.arc(x,y,3,0,6.28); ctx.fill()});
    ctx.fillStyle='#64748b'; ctx.font='12px system-ui';
    const skip=Math.ceil(labels.length/6);
    labels.forEach((lb,i)=>{ if(i%skip) return; ctx.fillText(lb,p+i*dx-18,h-p+14); });
  }
  function drawDonut(ctx,pairs){
    const sum=pairs.reduce((s,[,v])=>s+v,0)||1, cx=ctx.canvas.width/2, cy=ctx.canvas.height/2, r=Math.min(cx,cy)-10, ri=r*.55;
    let a=-Math.PI/2; const cols=['#16a34a','#f59e0b','#ef4444','#7c3aed','#2563eb','#10b981'];
    pairs.forEach(([k,v],i)=>{ const a2=a+2*Math.PI*(v/sum); ctx.beginPath(); ctx.moveTo(cx,cy); ctx.arc(cx,cy,r,a,a2); ctx.closePath(); ctx.fillStyle=cols[i%cols.length]; ctx.fill(); a=a2; });
    ctx.globalCompositeOperation='destination-out'; ctx.beginPath(); ctx.arc(cx,cy,ri,0,6.28); ctx.fill(); ctx.globalCompositeOperation='source-over';
  }
  function drawBar(ctx,pairs){
    const w=ctx.canvas.width,h=ctx.canvas.height,p=40,max=Math.max(1,...pairs.map(p=>p[1])), step=(h-2*p)/max, bw=(w-2*p)/Math.max(1,pairs.length);
    ctx.clearRect(0,0,w,h); ctx.strokeStyle='#cbd5e1'; ctx.beginPath(); ctx.moveTo(p,p); ctx.lineTo(p,h-p); ctx.lineTo(w-p,h-p); ctx.stroke();
    pairs.forEach(([k,v],i)=>{ const x=p+i*bw+8, y=h-p-v*step; ctx.fillStyle='#7c3aed'; ctx.fillRect(x,y,bw-16,v*step);
      ctx.save(); ctx.translate(x+(bw-16)/2,h-p+12); ctx.rotate(-Math.PI/4); ctx.fillStyle='#64748b'; ctx.font='12px system-ui'; ctx.fillText(k,0,0); ctx.restore(); });
  }

  drawLine(document.getElementById('line').getContext('2d'), dailyLabels, dailyCounts);
  drawDonut(document.getElementById('donut').getContext('2d'), byStatus);
  drawBar(document.getElementById('bar').getContext('2d'), byDept);
</script>
