<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<!--
NeMark Group – Corporate Landing (JSP under WEB-INF)

Hướng dẫn thay nhanh:
- Đổi tên công ty: tìm/replace 'NeMark Group'
- Màu: primary #2563EB, secondary #0EA5E9 (Tailwind utilities)
- Ảnh: đã gắn sẵn link mẫu từ Unsplash/Pexels (miễn phí) – có comment nguồn ngay trên mỗi ảnh.
  Bạn có thể tải về và đặt tại /assets/..., sau đó đổi src cho phù hợp.

Gợi ý nguồn ảnh (đã dùng trong trang):
- Hero (văn phòng): Unsplash – Scott Graham, "man working on laptop in office" https://unsplash.com/photos/5fNmWej4tAA
- Team (meeting): Unsplash – Campaign Creators, https://unsplash.com/photos/gMsnXqILjp4
- Cloud/Data center: Pexels – Manuel Geissinger, https://www.pexels.com/photo/close-up-photo-of-mining-rig-325229/
- Case retail: Unsplash – Luke Chesser (analytics), https://unsplash.com/photos/12b-0t2X0b8
- Case F&B: Unsplash – Blake Wisz (POS), https://unsplash.com/photos/tE6th1h6Bfk
- Case logistics: Unsplash – Elevate, https://unsplash.com/photos/1K9T5YiZ2WU
- Avatars: Unsplash portraits – https://unsplash.com/images/people/portraits

Ghi chú: Trang dùng Tailwind CDN, không cần build; accessible (focus ring, contrast), toggle dark mode, animation nhẹ.
-->
<html lang="vi" class="scroll-smooth" data-color-scheme="light">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <meta name="color-scheme" content="light dark">
        <title>LeaveMgmt – Cổng nội bộ quản lý nhân sự</title>
        <meta name="description" content="Cổng nội bộ quản lý nhân sự: Nghỉ phép, Chấm công, Phê duyệt, Agenda phòng ban, Báo cáo. Nhanh, an toàn, phân quyền rõ ràng.">

        <meta property="og:type" content="website">
        <meta property="og:title" content="LeaveMgmt – Cổng nội bộ quản lý nhân sự">
        <meta property="og:description" content="Nghỉ phép, Chấm công, Phân quyền duyệt, Agenda phòng ban, Báo cáo.">
        <meta property="og:url" content="/">
        <meta property="og:image" content="/assets/placeholder/og-hero.jpg"><!-- TODO replace -->

        <meta name="twitter:card" content="summary_large_image">
        <meta name="twitter:title" content="LeaveMgmt – Cổng nội bộ quản lý nhân sự">
        <meta name="twitter:description" content="Nghỉ phép, Chấm công, Phân quyền duyệt, Agenda phòng ban, Báo cáo.">
        <meta name="twitter:image" content="/assets/placeholder/og-hero.jpg"><!-- TODO replace -->

        <script type="application/ld+json">
        {
          "@context": "https://schema.org",
          "@type": "Organization",
          "name": "LeaveMgmt",
          "url": "/",
          "logo": "/assets/placeholder/logo-mark.svg",
          "sameAs": ["https://www.facebook.com/yourpage","https://www.linkedin.com/company/yourcompany"],
          "contactPoint": [{"@type":"ContactPoint","telephone":"+84-28-0000-0000","contactType":"customer service","areaServed":"VN","availableLanguage":"Vietnamese"}]
        }
        </script>
        <script type="application/ld+json">
        {
          "@context": "https://schema.org",
          "@type": "WebSite",
          "url": "/",
          "name": "LeaveMgmt",
          "potentialAction": {"@type":"SearchAction","target":"/?q={search_term_string}","query-input":"required name=search_term_string"}
        }
        </script>

        <script src="https://cdn.tailwindcss.com"></script>
        <script>
            tailwind.config = {
                theme: {
                    extend: {
                        colors: { primary: '#2563EB', secondary: '#0EA5E9' },
                        boxShadow: { soft: '0 10px 25px -10px rgba(2,6,23,0.25)' },
                        borderRadius: { '2xl': '1rem', '3xl': '1.25rem' }
                    },
                    container: { center: true, padding: '1rem' }
                },
                darkMode: 'class'
            };
        </script>
        <style>
            .reveal { opacity: 0; transform: translateY(16px); transition: opacity .6s ease, transform .6s ease; }
            .reveal.show { opacity: 1; transform: translateY(0); }
            :focus-visible { outline: 2px solid #0EA5E9; outline-offset: 2px; }
        </style>
    </head>
    <body class="min-h-screen bg-slate-50 text-slate-800 selection:bg-primary/10 selection:text-primary">
        <a href="#hero" class="sr-only focus:not-sr-only focus:absolute focus:top-2 focus:left-2 bg-white text-slate-900 px-3 py-2 rounded-md shadow">Bỏ qua nội dung</a>

        <header class="sticky top-0 z-50 bg-white/80 backdrop-blur border-b border-slate-200">
            <div class="container max-w-7xl">
                <div class="flex items-center justify-between py-3">
                    <a href="#" aria-label="NeMark Group - Trang chủ" class="flex items-center gap-2">
                        <svg width="132" height="24" viewBox="0 0 132 24" fill="none" xmlns="http://www.w3.org/2000/svg" class="shrink-0">
                            <text x="0" y="18" font-size="18" font-family="system-ui,-apple-system,Segoe UI,Roboto,Arial" fill="#2563EB" font-weight="700">NeMark</text>
                            <text x="75" y="18" font-size="18" font-family="system-ui,-apple-system,Segoe UI,Roboto,Arial" fill="#0EA5E9" font-weight="700">Group</text>
                        </svg>
                    </a>
                    <nav aria-label="Chính">
                        <ul class="hidden md:flex items-center gap-8 text-sm text-slate-700">
                            <li><a href="#modules" class="hover:text-primary">Module</a></li>
                            <li><a href="#quick" class="hover:text-primary">Truy cập nhanh</a></li>
                            <li><a href="#process" class="hover:text-primary">Quy trình</a></li>
                            <li><a href="#faq" class="hover:text-primary">FAQ</a></li>
                            <li><a href="#cta" class="hover:text-primary">Hỗ trợ</a></li>
                        </ul>
                    </nav>
                    <div class="flex items-center gap-2">
                        <button id="themeToggle" class="inline-flex items-center gap-2 rounded-xl border border-slate-300 px-3 py-2 text-sm hover:bg-slate-100" type="button" aria-pressed="false" aria-label="Chuyển chế độ sáng/tối">
                            <svg id="iconSun" class="h-4 w-4 text-amber-500 hidden" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><circle cx="12" cy="12" r="4"/><path d="M12 2v2m0 16v2M4.93 4.93l1.41 1.41M17.66 17.66l1.41 1.41M2 12h2m16 0h2M4.93 19.07l1.41-1.41M17.66 6.34l1.41-1.41"/></svg>
                            <svg id="iconMoon" class="h-4 w-4 text-slate-700" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/></svg>
                            <span class="hidden sm:inline">Chế độ</span>
                        </button>
                        <a href="${pageContext.request.contextPath}/login" class="ml-1 inline-flex items-center gap-2 rounded-xl bg-primary px-4 py-2 text-white text-sm font-semibold shadow-soft hover:bg-primary/90">
                            Đăng nhập
                            <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M5 12h14"/><path d="m12 5 7 7-7 7"/></svg>
                        </a>
                    </div>
                </div>
            </div>
        </header>

        <section id="hero" class="relative overflow-hidden">
            <div aria-hidden="true" class="pointer-events-none absolute inset-0 bg-gradient-to-br from-primary/10 via-secondary/10 to-transparent"></div>
            <div class="container max-w-7xl">
                <div class="grid grid-cols-1 lg:grid-cols-12 gap-10 items-center py-16 md:py-24">
                    <div class="lg:col-span-7 reveal">
                        <h1 class="text-3xl md:text-5xl font-extrabold tracking-tight text-slate-900">Cổng nội bộ Quản lý Nhân sự</h1>
                        <p class="mt-4 text-lg text-slate-600 max-w-2xl">Nghỉ phép, Chấm công, Phê duyệt theo vai trò, Agenda phòng ban và báo cáo. Nhanh – gọn – đúng quy trình.</p>
                        <div class="mt-8 flex flex-wrap gap-3">
                            <a href="${pageContext.request.contextPath}/request/create" class="inline-flex items-center gap-2 rounded-2xl bg-primary text-white px-6 py-3 shadow-soft hover:bg-primary/90">Tạo đơn nghỉ<svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M5 12h14"/><path d="m12 5 7 7-7 7"/></svg></a>
                            <a href="${pageContext.request.contextPath}/request/list?scope=mine" class="inline-flex items-center gap-2 rounded-2xl border border-slate-300 px-6 py-3 hover:bg-slate-100">Đơn của tôi<svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="m10 8 6 4-6 4z"/></svg></a>
                        </div>
                        <div class="mt-6 flex flex-wrap items-center gap-6 text-sm text-slate-600">
                            <div class="inline-flex items-center gap-2"><svg class="h-5 w-5 text-secondary" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 6 9 17l-5-5"/></svg>RBAC 3 vai trò</div>
                            <div class="inline-flex items-center gap-2"><svg class="h-5 w-5 text-secondary" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 6 9 17l-5-5"/></svg>Agenda phòng ban</div>
                        </div>
                    </div>
                    <div class="lg:col-span-5 reveal">
                        <picture>
                            <!-- Nguồn gợi ý: Unsplash – Scott Graham https://unsplash.com/photos/5fNmWej4tAA -->
                            <source srcset="https://images.unsplash.com/photo-1522071820081-009f0129c71c?q=80&w=1600&auto=format&fit=crop" type="image/jpeg">
                            <img src="https://images.unsplash.com/photo-1522071820081-009f0129c71c?q=80&w=1600&auto=format&fit=crop" alt="Đội ngũ triển khai giải pháp công nghệ cho doanh nghiệp" class="w-full rounded-3xl shadow-soft">
                        </picture>
                    </div>
                </div>
            </div>
        </section>

        <section id="trust" class="py-10 md:py-12 bg-white border-y border-slate-200">
            <div class="container max-w-7xl">
                <div class="flex items-center justify-between flex-wrap gap-4 reveal text-sm">
                    <span class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-slate-100 text-slate-700"><svg class="h-4 w-4 text-emerald-600" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 6 9 17l-5-5"/></svg>SSO ready</span>
                    <span class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-slate-100 text-slate-700"><svg class="h-4 w-4 text-emerald-600" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 6 9 17l-5-5"/></svg>RBAC 3 vai trò</span>
                    <span class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-slate-100 text-slate-700"><svg class="h-4 w-4 text-emerald-600" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 6 9 17l-5-5"/></svg>Audit log</span>
                    <span class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-slate-100 text-slate-700"><svg class="h-4 w-4 text-emerald-600" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 6 9 17l-5-5"/></svg>ISO 27001</span>
                </div>
            </div>
        </section>

        <section id="solutions" class="py-16 md:py-24">
            <div class="container max-w-7xl">
                <div class="text-center max-w-2xl mx-auto reveal">
                    <h2 class="text-2xl md:text-4xl font-bold text-slate-900">Giải pháp trọng tâm</h2>
                    <p class="mt-3 text-slate-600">Tập trung vào tác động kinh doanh: doanh số, hiệu suất, an toàn dữ liệu.</p>
                </div>
                <div class="mt-10 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                    <article class="reveal rounded-2xl bg-white shadow-soft border border-slate-200 p-6">
                        <div class="h-12 w-12 rounded-xl bg-primary/10 text-primary flex items-center justify-center"><svg class="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 3v18h18"/><path d="m19 9-5 5-4-4-3 3"/></svg></div>
                        <h3 class="mt-4 font-semibold text-slate-900">Website &amp; TMĐT</h3>
                        <p class="mt-2 text-sm text-slate-600">Tối ưu chuyển đổi, thanh toán, tích hợp kho &amp; vận chuyển.</p>
                        <a href="#cta" class="mt-4 inline-flex items-center gap-1 text-primary hover:underline">Tìm hiểu<svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M5 12h14"/><path d="m12 5 7 7-7 7"/></svg></a>
                    </article>
                    <article class="reveal rounded-2xl bg-white shadow-soft border border-slate-200 p-6">
                        <div class="h-12 w-12 rounded-xl bg-primary/10 text-primary flex items-center justify-center"><svg class="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m12 2 8 4-8 4-8-4 8-4Z"/><path d="m20 10-8 4-8-4"/><path d="m20 18-8 4-8-4"/></svg></div>
                        <h3 class="mt-4 font-semibold text-slate-900">CRM/ERP</h3>
                        <p class="mt-2 text-sm text-slate-600">Chuẩn hóa quy trình bán hàng, tồn kho, kế toán theo ngành.</p>
                        <a href="#cta" class="mt-4 inline-flex items-center gap-1 text-primary hover:underline">Tìm hiểu<svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M5 12h14"/><path d="m12 5 7 7-7 7"/></svg></a>
                    </article>
                    <article class="reveal rounded-2xl bg-white shadow-soft border border-slate-200 p-6">
                        <div class="h-12 w-12 rounded-xl bg-primary/10 text-primary flex items-center justify-center"><svg class="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17.5 19a4.5 4.5 0 0 0 0-9 6 6 0 0 0-11.31 1A4 4 0 0 0 6 19Z"/></svg></div>
                        <h3 class="mt-4 font-semibold text-slate-900">Hạ tầng Cloud</h3>
                        <p class="mt-2 text-sm text-slate-600">Kiến trúc linh hoạt, tối ưu chi phí, SRE/DevOps 24/7.</p>
                        <a href="#cta" class="mt-4 inline-flex items-center gap-1 text-primary hover:underline">Tìm hiểu<svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M5 12h14"/><path d="m12 5 7 7-7 7"/></svg></a>
                    </article>
                    <article class="reveal rounded-2xl bg-white shadow-soft border border-slate-200 p-6">
                        <div class="h-12 w-12 rounded-xl bg-primary/10 text-primary flex items-center justify-center"><svg class="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10Z"/></svg></div>
                        <h3 class="mt-4 font-semibold text-slate-900">Bảo mật</h3>
                        <p class="mt-2 text-sm text-slate-600">Kiểm thử xâm nhập, SOC, tuân thủ &amp; đào tạo nhận thức.</p>
                        <a href="#cta" class="mt-4 inline-flex items-center gap-1 text-primary hover:underline">Tìm hiểu<svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M5 12h14"/><path d="m12 5 7 7-7 7"/></svg></a>
                    </article>
                </div>
            </div>
        </section>

        <!-- Modules (nội bộ) -->
        <section id="modules" class="py-16 md:py-24 bg-white">
            <div class="container max-w-7xl">
                <div class="text-center max-w-2xl mx-auto reveal">
                    <h2 class="text-2xl md:text-4xl font-bold text-slate-900">Các module chính</h2>
                    <p class="mt-3 text-slate-600">Phục vụ nhu cầu vận hành nhân sự hàng ngày.</p>
                </div>
                <div class="mt-10 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    <article class="reveal rounded-2xl bg-slate-50 border border-slate-200 p-5 hover:shadow-soft">
                        <div class="h-12 w-12 rounded-xl bg-primary/10 text-primary flex items-center justify-center"><svg class="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 2v4"/><path d="M8 2v4"/><rect x="4" y="4" width="16" height="18" rx="2"/><path d="M8 10h8M8 14h6"/></svg></div>
                        <h3 class="mt-3 font-semibold">Nghỉ phép</h3>
                        <p class="mt-1 text-sm text-slate-600">Tạo đơn, theo dõi trạng thái, lịch sử duyệt.</p>
                        <a href="${pageContext.request.contextPath}/request/create" class="mt-3 inline-flex items-center gap-1 text-primary hover:underline">Tạo đơn<svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M5 12h14"/><path d="m12 5 7 7-7 7"/></svg></a>
                    </article>
                    <article class="reveal rounded-2xl bg-slate-50 border border-slate-200 p-5 hover:shadow-soft">
                        <div class="h-12 w-12 rounded-xl bg-primary/10 text-primary flex items-center justify-center"><svg class="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 3v18h18"/><path d="M19 7 10 16l-3-3"/></svg></div>
                        <h3 class="mt-3 font-semibold">Chấm công</h3>
                        <p class="mt-1 text-sm text-slate-600">Giờ làm, remote/office, báo cáo tổng hợp.</p>
                        <a href="${pageContext.request.contextPath}/activity" class="mt-3 inline-flex items-center gap-1 text-primary hover:underline">Xem hoạt động<svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M5 12h14"/><path d="m12 5 7 7-7 7"/></svg></a>
                    </article>
                    <article class="reveal rounded-2xl bg-slate-50 border border-slate-200 p-5 hover:shadow-soft">
                        <div class="h-12 w-12 rounded-xl bg-primary/10 text-primary flex items-center justify-center"><svg class="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 3h18v6H3z"/><path d="M16 13H3"/><path d="M16 17H3"/><path d="M21 13h-2v8h2z"/></svg></div>
                        <h3 class="mt-3 font-semibold">Phê duyệt</h3>
                        <p class="mt-1 text-sm text-slate-600">Duyệt theo vai trò, ghi nhận lịch sử, thông báo.</p>
                        <a href="${pageContext.request.contextPath}/request/list?scope=team" class="mt-3 inline-flex items-center gap-1 text-primary hover:underline">Duyệt đơn<svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M5 12h14"/><path d="m12 5 7 7-7 7"/></svg></a>
                    </article>
                    <article class="reveal rounded-2xl bg-slate-50 border border-slate-200 p-5 hover:shadow-soft">
                        <div class="h-12 w-12 rounded-xl bg-primary/10 text-primary flex items-center justify-center"><svg class="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15V6a2 2 0 0 0-2-2H5a2 2 0 0 0-2 2v9"/><path d="M7 22h10"/><path d="M12 17v5"/></svg></div>
                        <h3 class="mt-3 font-semibold">Phòng ban &amp; Org</h3>
                        <p class="mt-1 text-sm text-slate-600">Sơ đồ tổ chức, vai trò, phân quyền.</p>
                        <a href="${pageContext.request.contextPath}/admin/divisions" class="mt-3 inline-flex items-center gap-1 text-primary hover:underline">Quản lý phòng ban<svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M5 12h14"/><path d="m12 5 7 7-7 7"/></svg></a>
                    </article>
                    <article class="reveal rounded-2xl bg-slate-50 border border-slate-200 p-5 hover:shadow-soft">
                        <div class="h-12 w-12 rounded-xl bg-primary/10 text-primary flex items-center justify-center"><svg class="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 3v18h18"/><path d="M18 12a6 6 0 1 1-12 0 6 6 0 0 1 12 0z"/></svg></div>
                        <h3 class="mt-3 font-semibold">Agenda &amp; Lịch</h3>
                        <p class="mt-1 text-sm text-slate-600">Tô màu trạng thái đi làm/ nghỉ/ remote.</p>
                        <a href="${pageContext.request.contextPath}/request/agenda" class="mt-3 inline-flex items-center gap-1 text-primary hover:underline">Xem Agenda<svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M5 12h14"/><path d="m12 5 7 7-7 7"/></svg></a>
                    </article>
                </div>
            </div>
        </section>

        <section id="features-1" class="py-16 md:py-24 bg-slate-50">
            <div class="container max-w-7xl">
                <div class="grid grid-cols-1 lg:grid-cols-12 gap-10 items-center">
                    <div class="lg:col-span-5 order-2 lg:order-1 reveal">
                        <picture>
                            <!-- Unsplash – Campaign Creators https://unsplash.com/photos/gMsnXqILjp4 -->
                            <source srcset="https://images.unsplash.com/photo-1551836022-d5d88e9218df?q=80&w=1400&auto=format&fit=crop" type="image/jpeg">
                            <img src="https://images.unsplash.com/photo-1551836022-d5d88e9218df?q=80&w=1400&auto=format&fit=crop" alt="Đội dự án triển khai" class="w-full rounded-3xl shadow-soft">
                        </picture>
                    </div>
                    <div class="lg:col-span-7 order-1 lg:order-2 reveal">
                        <h3 class="text-2xl md:text-3xl font-bold text-slate-900">Luồng làm việc rõ ràng, đo lường theo KPI</h3>
                        <ul class="mt-4 space-y-2 text-slate-700">
                            <li class="flex gap-2"><svg class="h-5 w-5 text-emerald-500 mt-1" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 6 9 17l-5-5"/></svg>Chuẩn hoá mẫu đơn & quy trình duyệt</li>
                            <li class="flex gap-2"><svg class="h-5 w-5 text-emerald-500 mt-1" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 6 9 17l-5-5"/></svg>Theo dõi SLA phê duyệt, nhắc hạn tự động</li>
                            <li class="flex gap-2"><svg class="h-5 w-5 text-emerald-500 mt-1" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 6 9 17l-5-5"/></svg>Báo cáo realtime cho lãnh đạo</li>
                        </ul>
                        <a href="${pageContext.request.contextPath}/request/list" class="mt-6 inline-flex items-center gap-2 rounded-2xl bg-primary text-white px-6 py-3 shadow-soft hover:bg-primary/90">Xem danh sách đơn</a>
                    </div>
                </div>
            </div>
        </section>

        <section id="features-2" class="py-16 md:py-24">
            <div class="container max-w-7xl">
                <div class="grid grid-cols-1 lg:grid-cols-12 gap-10 items-center">
                    <div class="lg:col-span-7 reveal">
                        <h3 class="text-2xl md:text-3xl font-bold text-slate-900">Hạ tầng vững chắc, bảo mật theo chuẩn</h3>
                        <ul class="mt-4 space-y-2 text-slate-700">
                            <li class="flex gap-2"><svg class="h-5 w-5 text-emerald-500 mt-1" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 6 9 17l-5-5"/></svg>Cloud đa vùng – uptime 99.95%</li>
                            <li class="flex gap-2"><svg class="h-5 w-5 text-emerald-500 mt-1" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 6 9 17l-5-5"/></svg>WAF, DDoS, backup 30 ngày</li>
                            <li class="flex gap-2"><svg class="h-5 w-5 text-emerald-500 mt-1" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 6 9 17l-5-5"/></svg>Tuân thủ: ISO 27001 / OWASP ASVS</li>
                        </ul>
                        <a href="#cta" class="mt-6 inline-flex items-center gap-2 rounded-2xl border border-slate-300 px-6 py-3 hover:bg-slate-100">Tài liệu kỹ thuật</a>
                    </div>
                    <div class="lg:col-span-5 reveal">
                        <picture>
                            <!-- Pexels – Manuel Geissinger https://www.pexels.com/photo/close-up-photo-of-mining-rig-325229/ -->
                            <source srcset="https://images.pexels.com/photos/325229/pexels-photo-325229.jpeg?auto=compress&cs=tinysrgb&w=1600" type="image/jpeg">
                            <img src="https://images.pexels.com/photos/325229/pexels-photo-325229.jpeg?auto=compress&cs=tinysrgb&w=1600" alt="Kiến trúc cloud bảo mật" class="w-full rounded-3xl shadow-soft">
                        </picture>
                    </div>
                </div>
            </div>
        </section>

        <section id="process" class="py-16 md:py-24 bg-white">
            <div class="container max-w-7xl">
                <div class="text-center max-w-2xl mx-auto reveal">
                    <h2 class="text-2xl md:text-4xl font-bold text-slate-900">Quy trình làm việc</h2>
                    <p class="mt-3 text-slate-600">Chuẩn nhất quán, theo dõi minh bạch.</p>
                </div>
                <div class="mt-10 grid grid-cols-1 md:grid-cols-3 gap-6">
                    <article class="reveal rounded-2xl bg-slate-50 border border-slate-200 p-6">
                        <img src="https://images.unsplash.com/photo-1522071820081-009f0129c71c?q=80&w=1200&auto=format&fit=crop" alt="Tạo đơn nghỉ" class="w-full h-40 object-cover rounded-xl">
                        <h3 class="mt-4 font-semibold">Bước 1 · Tạo đơn</h3>
                        <p class="mt-2 text-sm text-slate-600">Chọn loại nghỉ, thời gian, người duyệt. Hệ thống kiểm tra số ngày còn lại.</p>
                        <ul class="mt-3 text-sm text-slate-700 space-y-1"><li>Khởi tạo &lt; 60s</li><li>Thông báo realtime</li></ul>
                    </article>
                    <article class="reveal rounded-2xl bg-slate-50 border border-slate-200 p-6">
                        <img src="https://images.unsplash.com/photo-1551836022-d5d88e9218df?q=80&w=1200&auto=format&fit=crop" alt="Phê duyệt" class="w-full h-40 object-cover rounded-xl">
                        <h3 class="mt-4 font-semibold">Bước 2 · Phê duyệt</h3>
                        <p class="mt-2 text-sm text-slate-600">Manager hoặc Division Leader xử lý. Lưu audit đầy đủ.</p>
                        <ul class="mt-3 text-sm text-slate-700 space-y-1"><li>2 cấp duyệt</li><li>Lý do &amp; minh chứng</li></ul>
                    </article>
                    <article class="reveal rounded-2xl bg-slate-50 border border-slate-200 p-6">
                        <img src="https://images.unsplash.com/photo-1519389950473-47ba0277781c?q=80&w=1200&auto=format&fit=crop" alt="Agenda & báo cáo" class="w-full h-40 object-cover rounded-xl">
                        <h3 class="mt-4 font-semibold">Bước 3 · Agenda &amp; Báo cáo</h3>
                        <p class="mt-2 text-sm text-slate-600">Lịch phòng ban rõ ràng, báo cáo KPI theo thời gian thực.</p>
                        <ul class="mt-3 text-sm text-slate-700 space-y-1"><li>Uptime 99.95%</li><li>Minh bạch dữ liệu</li></ul>
                    </article>
                </div>
            </div>
        </section>

        <section id="testimonials" class="py-16 md:py-24 bg-slate-50">
            <div class="container max-w-7xl">
                <div class="text-center max-w-2xl mx-auto reveal"><h2 class="text-2xl md:text-4xl font-bold text-slate-900">Mẹo nhanh &amp; Phím tắt</h2><p class="mt-3 text-slate-600">Tăng tốc thao tác trong hệ thống.</p></div>
                <div class="mt-10 grid grid-cols-1 md:grid-cols-3 gap-6">
                    <figure class="reveal rounded-2xl bg-white border border-slate-200 p-6 shadow-soft"><blockquote class="text-slate-700">G + C: mở Tạo đơn. G + M: Đơn của tôi. G + T: Đơn cấp dưới.</blockquote><figcaption class="mt-4 flex items-center gap-3"><span class="h-10 w-10 rounded-full bg-slate-100 flex items-center justify-center">⌨️</span><div><div class="font-semibold">Phím tắt</div><div class="text-sm text-slate-500">Di chuyển siêu nhanh</div></div></figcaption></figure>
                    <figure class="reveal rounded-2xl bg-white border border-slate-200 p-6 shadow-soft"><blockquote class="text-slate-700">Ctrl/Cmd + K: Command Palette để tìm hành động nhanh.</blockquote><figcaption class="mt-4 flex items-center gap-3"><span class="h-10 w-10 rounded-full bg-slate-100 flex items-center justify-center">⌘</span><div><div class="font-semibold">Command</div><div class="text-sm text-slate-500">Tìm &amp; chạy lệnh</div></div></figcaption></figure>
                    <figure class="reveal rounded-2xl bg-white border border-slate-200 p-6 shadow-soft"><blockquote class="text-slate-700">D: bật/tắt Dark Mode. Lưu trạng thái để lần sau tự nhớ.</blockquote><figcaption class="mt-4 flex items-center gap-3"><span class="h-10 w-10 rounded-full bg-slate-100 flex items-center justify-center">☾</span><div><div class="font-semibold">Giao diện</div><div class="text-sm text-slate-500">Thoải mái mắt</div></div></figcaption></figure>
                </div>
            </div>
        </section>

        <section id="quick" class="py-16 md:py-24 bg-white">
            <div class="container max-w-7xl">
                <div class="text-center max-w-2xl mx-auto reveal"><h2 class="text-2xl md:text-4xl font-bold text-slate-900">Truy cập nhanh</h2><p class="mt-3 text-slate-600">Các thao tác thường dùng.</p></div>
                <div class="mt-10 grid grid-cols-1 md:grid-cols-3 gap-6">
                    <a href="${pageContext.request.contextPath}/request/create" class="reveal rounded-2xl border border-slate-200 bg-slate-50 p-6 block hover:shadow-soft"><h3 class="font-semibold">Tạo đơn nghỉ</h3><p class="mt-2 text-sm text-slate-600">Khởi tạo nhanh &lt; 60s.</p></a>
                    <a href="${pageContext.request.contextPath}/request/list?scope=mine" class="reveal rounded-2xl border border-slate-200 bg-slate-50 p-6 block hover:shadow-soft"><h3 class="font-semibold">Đơn của tôi</h3><p class="mt-2 text-sm text-slate-600">Theo dõi trạng thái &amp; lịch sử.</p></a>
                    <a href="${pageContext.request.contextPath}/request/agenda" class="reveal rounded-2xl border border-slate-200 bg-slate-50 p-6 block hover:shadow-soft"><h3 class="font-semibold">Agenda phòng ban</h3><p class="mt-2 text-sm text-slate-600">Xem lịch vắng mặt.</p></a>
                </div>
            </div>
        </section>

        <section id="faq" class="py-16 md:py-24 bg-slate-50">
            <div class="container max-w-7xl">
                <div class="text-center max-w-2xl mx-auto reveal"><h2 class="text-2xl md:text-4xl font-bold text-slate-900">Câu hỏi thường gặp</h2></div>
                <div class="mt-10 grid grid-cols-1 md:grid-cols-2 gap-6">
                    <details class="reveal group rounded-2xl bg-white border border-slate-200 p-5 open:shadow-soft"><summary class="cursor-pointer font-semibold text-slate-900 flex items-center justify-between">Thời gian triển khai?<svg class="h-5 w-5 text-slate-500 group-open:rotate-180 transition-transform" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m6 9 6 6 6-6"/></svg></summary><p class="mt-2 text-sm text-slate-700">Tùy phạm vi: 2–12 tuần. Chúng tôi làm việc theo sprint minh bạch.</p></details>
                    <details class="reveal group rounded-2xl bg-white border border-slate-200 p-5"><summary class="cursor-pointer font-semibold text-slate-900 flex items-center justify-between">Bảo hành &amp; hỗ trợ?<svg class="h-5 w-5 text-slate-500 group-open:rotate-180 transition-transform" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m6 9 6 6 6-6"/></svg></summary><p class="mt-2 text-sm text-slate-700">Hỗ trợ 3–12 tháng tùy gói, kèm SLA rõ ràng.</p></details>
                    <details class="reveal group rounded-2xl bg-white border border-slate-200 p-5"><summary class="cursor-pointer font-semibold text-slate-900 flex items-center justify-between">Chi phí phát sinh?<svg class="h-5 w-5 text-slate-500 group-open:rotate-180 transition-transform" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m6 9 6 6 6-6"/></svg></summary><p class="mt-2 text-sm text-slate-700">Báo giá minh bạch theo hạng mục &amp; thay đổi phạm vi có kiểm soát.</p></details>
                    <details class="reveal group rounded-2xl bg-white border border-slate-200 p-5"><summary class="cursor-pointer font-semibold text-slate-900 flex items-center justify-between">Bảo mật dữ liệu?<svg class="h-5 w-5 text-slate-500 group-open:rotate-180 transition-transform" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m6 9 6 6 6-6"/></svg></summary><p class="mt-2 text-sm text-slate-700">Tuân thủ ISO 27001, nguyên tắc tối thiểu quyền truy cập.</p></details>
                    <details class="reveal group rounded-2xl bg-white border border-slate-200 p-5"><summary class="cursor-pointer font-semibold text-slate-900 flex items-center justify-between">Có demo/POC?<svg class="h-5 w-5 text-slate-500 group-open:rotate-180 transition-transform" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m6 9 6 6 6-6"/></svg></summary><p class="mt-2 text-sm text-slate-700">Có, POC 2–3 tuần giúp ra quyết định nhanh.</p></details>
                    <details class="reveal group rounded-2xl bg-white border border-slate-200 p-5"><summary class="cursor-pointer font-semibold text-slate-900 flex items-center justify-between">Hình thức thanh toán?<svg class="h-5 w-5 text-slate-500 group-open:rotate-180 transition-transform" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m6 9 6 6 6-6"/></svg></summary><p class="mt-2 text-sm text-slate-700">Chuyển khoản doanh nghiệp, hóa đơn VAT đầy đủ.</p></details>
                </div>
            </div>
        </section>

        <section id="cta" class="py-16 md:py-24 relative overflow-hidden">
            <div aria-hidden="true" class="absolute inset-0 -z-10 bg-gradient-to-br from-primary/10 via-secondary/10 to-transparent"></div>
            <div class="container max-w-5xl">
                <div class="reveal rounded-3xl bg-white shadow-soft border border-slate-200 p-8 md:p-10">
                    <div class="grid grid-cols-1 lg:grid-cols-12 gap-8">
                        <div class="lg:col-span-6">
                            <h3 class="text-2xl md:text-3xl font-bold text-slate-900">Nhận tư vấn miễn phí</h3>
                            <p class="mt-2 text-slate-600">Chia sẻ mục tiêu – chúng tôi đề xuất lộ trình &amp; ngân sách rõ ràng.</p>
                            <ul class="mt-4 space-y-2 text-sm text-slate-700"><li class="flex gap-2"><svg class="h-4 w-4 text-emerald-500 mt-1" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 6 9 17l-5-5"/></svg>Phản hồi trong 24h</li><li class="flex gap-2"><svg class="h-4 w-4 text-emerald-500 mt-1" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 6 9 17l-5-5"/></svg>Cam kết bảo mật thông tin</li></ul>
                        </div>
                        <div class="lg:col-span-6">
                            <form id="contactForm" novalidate class="grid grid-cols-1 gap-4">
                                <div>
                                    <label for="name" class="block text-sm font-medium text-slate-700">Họ và tên</label>
                                    <input id="name" name="name" required autocomplete="name" class="mt-1 w-full rounded-xl border-slate-300 focus:border-secondary focus:ring-secondary" placeholder="Nguyễn Văn A">
                                    <p class="mt-1 text-xs text-rose-600 hidden" id="errName">Vui lòng nhập họ tên.</p>
                                </div>
                                <div>
                                    <label for="email" class="block text-sm font-medium text-slate-700">Email</label>
                                    <input id="email" name="email" type="email" required autocomplete="email" class="mt-1 w-full rounded-xl border-slate-300 focus:border-secondary focus:ring-secondary" placeholder="ban@congty.com">
                                    <p class="mt-1 text-xs text-rose-600 hidden" id="errEmail">Email không hợp lệ.</p>
                                </div>
                                <div>
                                    <label for="phone" class="block text-sm font-medium text-slate-700">Số điện thoại</label>
                                    <input id="phone" name="phone" type="tel" required pattern="^[0-9\\+\\-\\s]{8,}$" class="mt-1 w-full rounded-xl border-slate-300 focus:border-secondary focus:ring-secondary" placeholder="09xx xxx xxx">
                                    <p class="mt-1 text-xs text-rose-600 hidden" id="errPhone">Số điện thoại không hợp lệ.</p>
                                </div>
                                <div>
                                    <label for="need" class="block text-sm font-medium text-slate-700">Nhu cầu</label>
                                    <textarea id="need" name="need" rows="3" required class="mt-1 w-full rounded-xl border-slate-300 focus:border-secondary focus:ring-secondary" placeholder="Mô tả ngắn mục tiêu &amp; hiện trạng..."></textarea>
                                    <p class="mt-1 text-xs text-rose-600 hidden" id="errNeed">Vui lòng mô tả nhu cầu.</p>
                                </div>
                                <button type="submit" class="mt-2 inline-flex items-center justify-center gap-2 rounded-2xl bg-primary text-white px-6 py-3 shadow-soft hover:bg-primary/90">Gửi yêu cầu</button>
                                <p id="formMsg" class="text-sm mt-1 text-emerald-600 hidden">Đã gửi thành công! Chúng tôi sẽ liên hệ sớm.</p>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <footer id="footer" class="bg-slate-900 text-slate-200">
            <div class="container max-w-7xl py-12">
                <div class="grid grid-cols-1 md:grid-cols-4 gap-8">
                    <div>
                        <div class="flex items-center gap-2">
                            <svg width="132" height="24" viewBox="0 0 132 24" fill="none" xmlns="http://www.w3.org/2000/svg"><text x="0" y="18" font-size="18" font-family="system-ui,-apple-system,Segoe UI,Roboto,Arial" fill="#93C5FD" font-weight="700">NeMark</text><text x="75" y="18" font-size="18" font-family="system-ui,-apple-system,Segoe UI,Roboto,Arial" fill="#7DD3FC" font-weight="700">Group</text></svg>
                        </div>
                        <p class="mt-3 text-sm text-slate-400">Giải pháp công nghệ &amp; tăng trưởng cho doanh nghiệp Việt.</p>
                    </div>
                    <div>
                        <h4 class="font-semibold">Công ty</h4>
                        <ul class="mt-3 space-y-2 text-sm text-slate-300">
                            <li><a class="hover:text-white" href="#solutions">Giải pháp</a></li>
                            <li><a class="hover:text-white" href="#services">Dịch vụ</a></li>
                            <li><a class="hover:text-white" href="#pricing">Bảng giá</a></li>
                        </ul>
                    </div>
                    <div>
                        <h4 class="font-semibold">Liên hệ</h4>
                        <ul class="mt-3 space-y-2 text-sm text-slate-300">
                            <li class="flex items-center gap-2"><svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 16.92v1a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6A19.79 19.79 0 0 1 4.09 4.18 2 2 0 0 1 6.09 2h1a2 2 0 0 1 2 1.72c.12.81.3 1.6.57 2.36a2 2 0 0 1-.45 2.11L8 9a16 16 0 0 0 7 7l.81-1.21a2 2 0 0 1 2.11-.45c.76.27 1.55.45 2.36.57A2 2 0 0 1 22 16.92z"/></svg>+84 28 0000 0000</li>
                            <li class="flex items-center gap-2"><svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4h16v16H4z" opacity=".1"/><path d="M22 12H2"/><path d="M7 12l5 5 5-5"/></svg>hello@nemark.vn</li>
                            <li class="flex items-center gap-2"><svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13S3 17 3 10a9 9 0 1 1 18 0Z"/><circle cx="12" cy="10" r="3"/></svg>Q.1, TP.HCM</li>
                        </ul>
                    </div>
                    <div>
                        <h4 class="font-semibold">Mạng xã hội</h4>
                        <div class="mt-3 flex gap-3">
                            <a aria-label="Facebook" href="#" class="p-2 rounded-xl bg-slate-800 hover:bg-slate-700"><svg class="h-4 w-4" viewBox="0 0 24 24" fill="currentColor"><path d="M13 22v-9h3l1-4h-4V7a1 1 0 0 1 1-1h3V2h-3a5 5 0 0 0-5 5v3H6v4h3v9z"/></svg></a>
                            <a aria-label="LinkedIn" href="#" class="p-2 rounded-xl bg-slate-800 hover:bg-slate-700"><svg class="h-4 w-4" viewBox="0 0 24 24" fill="currentColor"><path d="M4.98 3.5C4.98 4.88 3.86 6 2.5 6S0 4.88 0 3.5 1.12 1 2.5 1s2.48 1.12 2.48 2.5zM.5 8.5h4V23h-4V8.5zM8.5 8.5h3.8v2h.05c.53-1 1.83-2 3.77-2 4.03 0 4.78 2.65 4.78 6.1V23h-4v-7.5c0-1.8-.03-4.1-2.5-4.1-2.5 0-2.88 1.95-2.88 4v7.6h-4V8.5z"/></svg></a>
                        </div>
                    </div>
                </div>
                <div class="mt-10 border-t border-slate-800 pt-6 text-xs text-slate-400">&copy; <span id="year"></span> LeaveMgmt. All rights reserved.</div>
            </div>
        </footer>

        <script>
            (function initTheme(){
                const stored = localStorage.getItem('theme');
                const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
                const root = document.documentElement;
                const set = (mode) => {
                    root.classList.toggle('dark', mode === 'dark');
                    document.documentElement.dataset.colorScheme = mode;
                    const sun = document.getElementById('iconSun');
                    const moon = document.getElementById('iconMoon');
                    if (sun && moon) { if (mode === 'dark') { sun.classList.remove('hidden'); moon.classList.add('hidden'); } else { sun.classList.add('hidden'); moon.classList.remove('hidden'); } }
                };
                set(stored || (prefersDark ? 'dark' : 'light'));
                document.getElementById('themeToggle').addEventListener('click', () => {
                    const next = document.documentElement.classList.contains('dark') ? 'light' : 'dark';
                    localStorage.setItem('theme', next); set(next);
                });
            })();

            const io = new IntersectionObserver((entries) => {
                entries.forEach(e => { if (e.isIntersecting) { e.target.classList.add('show'); io.unobserve(e.target); } });
            }, { threshold: 0.12 });
            document.querySelectorAll('.reveal').forEach(el => io.observe(el));

            document.getElementById('year').textContent = new Date().getFullYear();

            const form = document.getElementById('contactForm');
            const fields = {
                name: { el: document.getElementById('name'), err: document.getElementById('errName'), valid: v => v.trim().length > 1 },
                email:{ el: document.getElementById('email'), err: document.getElementById('errEmail'), valid: v => /^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(v) },
                phone:{ el: document.getElementById('phone'), err: document.getElementById('errPhone'), valid: v => /^[0-9+\-\s]{8,}$/.test(v) },
                need: { el: document.getElementById('need'), err: document.getElementById('errNeed'), valid: v => v.trim().length > 5 }
            };
            form.addEventListener('submit', (e) => {
                e.preventDefault();
                let ok = true; for (const k in fields) { const f = fields[k]; const v = f.el.value || ''; const valid = f.valid(v); f.err.classList.toggle('hidden', valid); f.el.classList.toggle('border-rose-500', !valid); ok = ok && valid; }
                if (ok) { document.getElementById('formMsg').classList.remove('hidden'); form.reset(); }
            });
        </script>
    </body>
</html>


