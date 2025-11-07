<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp"%>

<c:set var="cp"   value="${pageContext.request.contextPath}"/>
<c:set var="page" value="portal.home"/>

<jsp:include page="/WEB-INF/views/portal/_portal_header.jsp"/>
<jsp:include page="/WEB-INF/views/portal/_portal_sidebar.jsp"/>

<%-- ====== Data (có thể null) ====== --%>
<c:set var="site"  value="${requestScope.site}" />
<c:set var="kpi"   value="${requestScope.kpi}" />
<c:set var="news"  value="${requestScope.news}" />      <%-- [{title,href,thumb,ts}] --%>
<c:set var="media" value="${requestScope.media}" />     <%-- [{title,href,src,author,source}] --%>
<c:set var="acts"  value="${requestScope.recentActivities}" />
<c:set var="clock" value="${requestScope.clock}" />

<style>
  :root{
    --sbw:280px; --ph-h:64px;
    --bd:#e5e7eb; --ink:#0f172a; --muted:#64748b; --card:#fff; --bg:#f7f8fb; --pri:#2563eb;
  }
  @media (prefers-color-scheme: dark){
    :root{ --bd:#1f2937; --ink:#e5e7eb; --muted:#94a3b8; --card:#0b1220; --bg:#070c14; }
  }
  body{background:var(--bg)}
  .with-psb{ margin-left:var(--sbw); padding-top:var(--ph-h); transition:margin-left .25s ease; }
  @media(max-width:1100px){ .with-psb{ margin-left:0 } }
  .wrap{max-width:1200px;margin:0 auto;padding:16px}

  /* ===== Hero ===== */
  .hero{position:relative;border-radius:18px;overflow:hidden;border:1px solid var(--bd);background:var(--card)}
  .hero .img{height:min(32vw,300px);background-size:cover;background-position:center}
  .hero .overlay{position:absolute;inset:0;background:linear-gradient(180deg,rgba(2,6,23,.12),rgba(2,6,23,.35));pointer-events:none}
  .hero .content{position:absolute;inset:auto 16px 16px 16px;display:flex;gap:16px;align-items:end;justify-content:space-between}
  .hero .title{font-weight:800;font-size:clamp(18px,3vw,26px);color:#fff;text-shadow:0 2px 12px rgba(0,0,0,.35)}
  .hero .muted{color:#e5e7eb}

  /* ===== KPI ===== */
  .cards{display:grid;gap:12px;grid-template-columns:repeat(2,1fr)}
  @media(min-width:900px){.cards{grid-template-columns:repeat(4,1fr)}}
  .card{background:var(--card);border:1px solid var(--bd);border-radius:14px;padding:14px}
  .card h4{margin:0 0 6px;color:var(--muted);font-size:12px;letter-spacing:.3px}
  .metric{font-size:22px;font-weight:800}

  /* ===== Tiles ===== */
  .tiles{display:grid;grid-template-columns:repeat(6,minmax(140px,1fr));gap:12px}
  @media(max-width:900px){.tiles{grid-template-columns:repeat(2,minmax(160px,1fr))}}
  .tile{display:flex;gap:12px;align-items:center;padding:14px;border:1px solid var(--bd);
        border-radius:14px;background:var(--card);text-decoration:none;color:inherit}
  .tile .ic{width:36px;height:36px;border-radius:10px;border:1px solid var(--bd);display:grid;place-items:center}
  .tile .ic svg{width:18px;height:18px;color:var(--muted)}
  .tile:hover{border-color:color-mix(in oklab,var(--pri) 35%, var(--bd))}

  /* ===== 2 cols ===== */
  .grid{display:grid;gap:16px}
  @media(min-width:1000px){.grid{grid-template-columns:1.2fr 1fr}}
  .panel{background:var(--card);border:1px solid var(--bd);border-radius:14px;padding:14px}

  /* ===== Table ===== */
  .table-wrap{overflow:auto;border-radius:10px;border:1px solid var(--bd)}
  table{width:100%;border-collapse:collapse;background:var(--card)}
  th,td{padding:10px 12px;border-bottom:1px solid var(--bd);text-align:left}
  th{font-size:12px;color:var(--muted)}

  /* ===== Media Gallery ===== */
  .gallery{display:grid;grid-template-columns:repeat(3,1fr);gap:12px}
  @media(max-width:900px){.gallery{grid-template-columns:repeat(2,1fr)}}

  .media{position:relative;border:1px solid var(--bd);border-radius:12px;overflow:hidden;background:var(--card)}
  .media .ph{aspect-ratio:16/9;background:#e5e7eb;position:relative}
  .media img{width:100%;height:100%;object-fit:cover;display:block;transition:transform .35s ease}
  .media:hover img{transform:scale(1.035)}
  .media .grad{position:absolute;inset:auto 0 0 0;height:40%;background:linear-gradient(180deg,transparent,rgba(0,0,0,.65))}
  .media .txt{position:absolute;left:10px;bottom:10px;right:10px;color:#fff;font-weight:700;text-shadow:0 1px 8px rgba(0,0,0,.45)}
  .media .src{position:absolute;top:8px;left:8px;font-size:11px;color:#fff;background:rgba(0,0,0,.5);padding:3px 6px;border-radius:999px}

  /* ===== News ===== */
  .news{display:grid;grid-template-columns:1fr 1fr;gap:12px}
  @media(max-width:900px){.news{grid-template-columns:1fr}}
  .news .item{display:grid;grid-template-columns:120px 1fr;gap:10px;border:1px solid var(--bd);
              border-radius:12px;background:var(--card);overflow:hidden}
  .news .thumb{width:100%;height:100%;background:#e5e7eb}
  .news .thumb picture, .news .thumb img{width:100%;height:100%;object-fit:cover;display:block}
  .news .meta{padding:10px}
  .meta .t{font-weight:700}
  .meta .d{color:var(--muted);font-size:12px;margin-top:4px}

  /* ===== Buttons ===== */
  .btn{padding:10px 14px;border:1px solid var(--bd);border-radius:12px;background:var(--card);
       text-decoration:none;color:inherit;font-weight:600}
  .btn.pri{background:linear-gradient(180deg,#2563eb,#1d4ed8);color:#fff;border-color:transparent}
  .muted{color:var(--muted)}
</style>

<main id="main" class="with-psb">
  <div class="wrap">

    <%-- ===== HERO (dùng ảnh mặc định đẹp nếu chưa cấu hình) ===== --%>
    <c:url var="heroDefault" value="/assets/hero-business.jpg"/>
    <%-- Hero mặc định từ Pexels (16:9, office teamwork) --%>
    <c:set var="heroFallback" value="https://images.pexels.com/photos/3184360/pexels-photo-3184360.jpeg?auto=compress&cs=tinysrgb&w=1600&h=600&fit=crop"/>
    <c:set var="hero" value="${empty site.heroUrl ? heroFallback : site.heroUrl}"/>

    <section class="hero" aria-label="Tổng quan">
      <div class="img" style="background-image:url('<c:out value="${hero}"/>')"></div>
      <div class="overlay"></div>
      <div class="content">
        <div>
          <div class="title">
            <c:out value="${empty site.heroTitle ? 'Welcome to LeaveMgmt Portal' : site.heroTitle}"/>
          </div>
          <div class="muted">
            <c:out value="${empty site.heroSubtitle ? 'Nền tảng quản trị nhân sự & vận hành nội bộ.' : site.heroSubtitle}"/>
          </div>
        </div>
        <div style="display:flex;gap:8px;flex-wrap:wrap">
          <a class="btn pri" href="${cp}/request/create">Tạo yêu cầu</a>
          <a class="btn" href="${cp}/work">Xem báo cáo</a>
        </div>
      </div>
    </section>

    <!-- ===== KPIs ===== -->
    <section class="cards" aria-label="Chỉ số nhanh" style="margin-top:14px">
      <div class="card"><h4>Phép năm còn</h4><div class="metric"><c:out value="${empty kpi.AL ? 0 : kpi.AL}"/> ngày</div></div>
      <div class="card"><h4>Đơn đang chờ</h4><div class="metric"><c:out value="${empty kpi.pending ? 0 : kpi.pending}"/></div></div>
      <div class="card"><h4>Đi muộn (tháng)</h4><div class="metric"><c:out value="${empty kpi.late ? 0 : kpi.late}"/> lần</div></div>
      <div class="card"><h4>Ước tính NET</h4><div class="metric"><fmt:formatNumber value="${empty kpi.net ? 0 : kpi.net}" pattern="#,##0"/> ₫</div></div>
    </section>

    <!-- ===== Lối tắt module ===== -->
    <section style="margin:14px 0">
      <nav class="tiles" aria-label="Lối tắt">
        <a class="tile" href="${cp}/request/list"><div class="ic">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M4 6h16M4 12h16M4 18h10"/></svg></div>
          <div><b>Requests</b><div class="muted" style="font-size:12px">Tạo/duyệt nhanh</div></div></a>
        <a class="tile" href="${cp}/attendance"><div class="ic">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><circle cx="12" cy="12" r="9"/><path d="M12 7v6l4 2"/></svg></div>
          <div><b>Chấm công</b><div class="muted" style="font-size:12px">Giờ làm rõ ràng</div></div></a>
        <a class="tile" href="${cp}/work"><div class="ic">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M3 12l4 4 7-8 7 10"/><path d="M21 21H3"/></svg></div>
          <div><b>Báo cáo</b><div class="muted" style="font-size:12px">Daily / Weekly</div></div></a>
        <a class="tile" href="${cp}/work/todos"><div class="ic">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M9 11l3 3L22 4"/><path d="M3 7h5M3 12h5M3 17h5"/></svg></div>
          <div><b>Việc HR</b><div class="muted" style="font-size:12px">To-do</div></div></a>
        <a class="tile" href="${cp}/payroll"><div class="ic">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M3 7h18v10H3z"/><circle cx="12" cy="12" r="2"/></svg></div>
          <div><b>Lương</b><div class="muted" style="font-size:12px">Tổng hợp</div></div></a>
        <a class="tile" href="${cp}/recruit/job"><div class="ic">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M4 7h16v10H4z"/><path d="M8 7V5h8v2"/></svg></div>
          <div><b>Tuyển dụng</b><div class="muted" style="font-size:12px">Pipeline</div></div></a>
      </nav>
    </section>

    <!-- ===== 2 cột: Hoạt động & Chấm công nhanh ===== -->
    <section class="grid">
      <div class="panel">
        <h3 style="margin:0 0 10px">Hoạt động gần đây</h3>
        <div class="table-wrap">
          <table aria-describedby="recentCaption">
            <caption id="recentCaption" class="muted" style="text-align:left;padding:8px 12px">Nhật ký</caption>
            <thead><tr><th>Loại</th><th>Nội dung</th><th>Thời gian</th></tr></thead>
            <tbody>
            <c:forEach items="${acts}" var="a">
              <tr><td><c:out value="${a.type}"/></td><td><c:out value="${a.title}"/></td><td><fmt:formatDate value="${a.time}" pattern="dd/MM HH:mm"/></td></tr>
            </c:forEach>
            <c:if test="${empty acts}"><tr><td colspan="3" class="muted">— Chưa có dữ liệu —</td></tr></c:if>
            </tbody>
          </table>
        </div>
      </div>

      <div class="panel">
        <h3 style="margin:0 0 10px">Chấm công nhanh</h3>
        <form method="post" action="${cp}/attendance/clock" style="display:flex;gap:10px;flex-wrap:wrap" autocomplete="off">
          <input type="hidden" name="csrf" value="${requestScope.csrf}"/>
          <button class="btn pri"  name="action" value="in"  <c:if test="${clock != null && !clock.inAllowed}">disabled</c:if>>Check-in</button>
          <button class="btn"      name="action" value="out" <c:if test="${clock != null && !clock.outAllowed}">disabled</c:if>>Check-out</button>
          <span class="muted" style="margin-left:auto">Hôm nay:
            <b><c:out value="${empty requestScope.todaySummary ? '--:--' : requestScope.todaySummary}"/></b>
          </span>
        </form>
        <c:if test="${not empty requestScope.clockHint}">
          <div class="muted" style="margin-top:8px;font-size:12px"><c:out value="${requestScope.clockHint}"/></div>
        </c:if>
      </div>
    </section>

    <!-- ===== Media / Thư viện ảnh nội bộ (có fallback ảnh Pexels) ===== -->
    <section class="panel" style="margin-top:16px">
      <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:8px">
        <h3 style="margin:0">Thư viện nội bộ</h3>
        <a class="btn" href="${cp}/media">Xem tất cả</a>
      </div>

      <c:choose>
        <c:when test="${not empty media}">
          <div class="gallery">
            <c:forEach items="${media}" var="m" varStatus="st">
              <c:if test="${st.index < 6}">
                <a class="media" href="${empty m.href ? m.src : m.href}" target="_blank" rel="noopener">
                  <div class="ph">
                    <img loading="lazy" src="${m.src}" alt="${fn:escapeXml(m.title)}"/>
                    <span class="src"><c:out value="${empty m.source ? 'Media' : m.source}"/></span>
                    <div class="grad"></div>
                    <div class="txt"><c:out value="${m.title}"/></div>
                  </div>
                </a>
              </c:if>
            </c:forEach>
          </div>
        </c:when>

        <c:otherwise>
          <%-- Fallback 6 ảnh từ Pexels (office/team work) --%>
          <div class="gallery">
            <a class="media" href="https://www.pexels.com/photo/coworkers-having-a-meeting-3184360/" target="_blank" rel="noopener">
              <div class="ph">
                <img loading="lazy"
                     src="https://images.pexels.com/photos/3184360/pexels-photo-3184360.jpeg?auto=compress&cs=tinysrgb&w=900&h=506&fit=crop"
                     alt="Team meeting in modern office"/>
                <span class="src">Pexels</span><div class="grad"></div><div class="txt">Team Meeting</div>
              </div>
            </a>
            <a class="media" href="https://www.pexels.com/photo/photo-of-people-having-meeting-3184325/" target="_blank" rel="noopener">
              <div class="ph">
                <img loading="lazy"
                     src="https://images.pexels.com/photos/3184325/pexels-photo-3184325.jpeg?auto=compress&cs=tinysrgb&w=900&h=506&fit=crop"
                     alt="HR discussion around table"/>
                <span class="src">Pexels</span><div class="grad"></div><div class="txt">HR Discussion</div>
              </div>
            </a>
            <a class="media" href="https://www.pexels.com/photo/people-working-in-the-office-4344878/" target="_blank" rel="noopener">
              <div class="ph">
                <img loading="lazy"
                     src="https://images.pexels.com/photos/4344878/pexels-photo-4344878.jpeg?auto=compress&cs=tinysrgb&w=900&h=506&fit=crop"
                     alt="Daily standup & teamwork"/>
                <span class="src">Pexels</span><div class="grad"></div><div class="txt">Daily Standup</div>
              </div>
            </a>
            <a class="media" href="https://www.pexels.com/photo/people-in-the-office-5324881/" target="_blank" rel="noopener">
              <div class="ph">
                <img loading="lazy"
                     src="https://images.pexels.com/photos/5324881/pexels-photo-5324881.jpeg?auto=compress&cs=tinysrgb&w=900&h=506&fit=crop"
                     alt="Working at open office"/>
                <span class="src">Pexels</span><div class="grad"></div><div class="txt">Open Office</div>
              </div>
            </a>
            <a class="media" href="https://www.pexels.com/photo/woman-in-white-long-sleeve-shirt-sitting-beside-woman-in-blue-and-white-striped-long-sleeve-3184465/" target="_blank" rel="noopener">
              <div class="ph">
                <img loading="lazy"
                     src="https://images.pexels.com/photos/3184465/pexels-photo-3184465.jpeg?auto=compress&cs=tinysrgb&w=900&h=506&fit=crop"
                     alt="One-on-one review"/>
                <span class="src">Pexels</span><div class="grad"></div><div class="txt">1-on-1 Review</div>
              </div>
            </a>
            <a class="media" href="https://www.pexels.com/photo/woman-in-black-blazer-holding-tablet-computer-3184632/" target="_blank" rel="noopener">
              <div class="ph">
                <img loading="lazy"
                     src="https://images.pexels.com/photos/3184632/pexels-photo-3184632.jpeg?auto=compress&cs=tinysrgb&w=900&h=506&fit=crop"
                     alt="Project planning on whiteboard"/>
                <span class="src">Pexels</span><div class="grad"></div><div class="txt">Project Planning</div>
              </div>
            </a>
          </div>
        </c:otherwise>
      </c:choose>
    </section>

    <!-- ===== News / Tin nội bộ ===== -->
    <section class="panel" style="margin-top:16px">
      <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:8px">
        <h3 style="margin:0">Tin nội bộ</h3>
        <a class="btn" href="${cp}/news">Xem tất cả</a>
      </div>

      <div class="news">
        <c:forEach items="${news}" var="n" varStatus="st">
          <c:if test="${st.index < 4}">
            <a class="item" href="${n.href}">
              <div class="thumb">
                <c:choose>
                  <c:when test="${not empty n.thumb}">
                    <picture>
                      <source srcset="${n.thumb}&auto=compress&cs=tinysrgb&w=480&h=300&fit=crop" media="(max-width: 600px)">
                      <img loading="lazy" src="${n.thumb}" alt="${fn:escapeXml(n.title)}"/>
                    </picture>
                  </c:when>
                  <c:otherwise><div style="width:100%;height:100%;background:#e5e7eb"></div></c:otherwise>
                </c:choose>
              </div>
              <div class="meta">
                <div class="t"><c:out value="${n.title}"/></div>
                <div class="d"><fmt:formatDate value="${n.ts}" pattern="dd/MM/yyyy"/></div>
              </div>
            </a>
          </c:if>
        </c:forEach>

        <c:if test="${empty news}">
          <%-- Fallback 2 tin mẫu (có thể xoá) --%>
          <a class="item" href="#">
            <div class="thumb">
              <img loading="lazy" src="https://images.pexels.com/photos/6476261/pexels-photo-6476261.jpeg?auto=compress&cs=tinysrgb&w=480&h=300&fit=crop" alt="Chính sách HR mới"/>
            </div>
            <div class="meta"><div class="t">Cập nhật chính sách HR tháng này</div><div class="d"><fmt:formatDate value="<%= new java.util.Date() %>" pattern="dd/MM/yyyy"/></div></div>
          </a>
          <a class="item" href="#">
            <div class="thumb">
              <img loading="lazy" src="https://images.pexels.com/photos/3184291/pexels-photo-3184291.jpeg?auto=compress&cs=tinysrgb&w=480&h=300&fit=crop" alt="Đào tạo kỹ năng teamwork"/>
            </div>
            <div class="meta"><div class="t">Workshop “Teamwork & Ownership” tuần tới</div><div class="d"><fmt:formatDate value="<%= new java.util.Date() %>" pattern="dd/MM/yyyy"/></div></div>
          </a>
        </c:if>
      </div>
    </section>

    <jsp:include page="/WEB-INF/views/portal/_portal_footer.jsp"/>
  </div>
</main>
