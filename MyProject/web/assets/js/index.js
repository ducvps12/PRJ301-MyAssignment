            (() => {
                const ctx = '<%=request.getContextPath()%>';
                const root = document.documentElement;
                const themeBtn = document.getElementById('themeToggle');
                const themeIcon = document.getElementById('themeIcon');
                const toast = document.getElementById('toast');

                /* Theme */
                (function bootTheme() {
                    const saved = localStorage.getItem('theme');
                    if (saved)
                        root.setAttribute('data-theme', saved);
                    syncIcon();
                })();
                function syncIcon() {
                    const d = (root.getAttribute('data-theme') || 'light') === 'dark';
                    themeIcon.className = d ? 'bi bi-sun' : 'bi bi-moon-stars';
                }
                themeBtn?.addEventListener('click', () => {
                    const next = (root.getAttribute('data-theme') === 'light') ? 'dark' : 'light';
                    root.setAttribute('data-theme', next);
                    localStorage.setItem('theme', next);
                    syncIcon();
                    tip('Đã chuyển sang giao diện ' + (next === 'dark' ? 'tối' : 'sáng'));
                });

                /* Toast helper */
                function tip(msg, ms) {
                    toast.textContent = msg;
                    toast.classList.add('show');
                    setTimeout(() => toast.classList.remove('show'), ms || 1600);
                }

                /* Counters */
                const counters = document.querySelectorAll('.metric .num');
                const io = ('IntersectionObserver' in window) ? new IntersectionObserver(es => {
                    es.forEach(e => {
                        if (e.isIntersecting) {
                            animateNum(e.target, parseInt(e.target.getAttribute('data-count') || '0', 10));
                            io.unobserve(e.target);
                        }
                    });
                }, {threshold: .4}) : null;
                counters.forEach(el => io ? io.observe(el) : animateNum(el, parseInt(el.getAttribute('data-count') || '0', 10)));
                function animateNum(el, to) {
                    let cur = 0;
                    const dur = 1200;
                    const t0 = performance.now();
                    const step = (t) => {
                        const p = Math.min(1, (t - t0) / dur);
                        cur = Math.floor(to * (0.5 - Math.cos(Math.PI * p) / 2));
                        el.textContent = cur.toLocaleString('vi-VN');
                        if (p < 1)
                            requestAnimationFrame(step);
                    };
                    requestAnimationFrame(step);
                }

                /* Illustration parallax */
                (function () {
                    const wrap = document.querySelector('.illus');
                    const ring = document.getElementById('ringBg');
                    if (!wrap || !ring)
                        return;
                    wrap.addEventListener('pointermove', e => {
                        const r = wrap.getBoundingClientRect();
                        const x = (e.clientX - r.left) / r.width - .5;
                        const y = (e.clientY - r.top) / r.height - .5;
                        ring.style.transform = 'rotateX(' + (-y * 6) + 'deg) rotateY(' + (x * 6) + 'deg)';
                    });
                    wrap.addEventListener('pointerleave', () => ring.style.transform = '');
                })();

                /* Copy demo accounts */
                document.getElementById('copyDemo')?.addEventListener('click', () => {
                    const txt = 'Employee\nuser: d.staff\npass: 123456\n\nManager\nuser: a.lead\npass: 123456';
                    if (navigator.clipboard?.writeText) {
                        navigator.clipboard.writeText(txt).then(() => tip('Đã sao chép thông tin demo.'));
                    }
                });

                /* Keyboard shortcuts + Command Palette */
                let goHeld = false;
                document.addEventListener('keydown', e => {
                    const k = (e.key || '').toLowerCase();
                    if ((e.ctrlKey || e.metaKey) && k === 'k') {
                        e.preventDefault();
                        openCmd();
                        return;
                    }
                    if (k === 'd' && !e.ctrlKey && !e.metaKey) {
                        e.preventDefault();
                        themeBtn?.click();
                        return;
                    }
                    if (k === '?') {
                        e.preventDefault();
                        openKbd();
                        return;
                    }
                    if (k === 'g') {
                        goHeld = true;
                        return;
                    }
                    if (goHeld) {
                        if (k === 'c') {
                            location.href = ctx + '/request/create';
                        }
                        if (k === 'm') {
                            location.href = ctx + '/request/list?scope=mine';
                        }
                        if (k === 't') {
                            location.href = ctx + '/request/list?scope=team';
                        }
                        goHeld = false;
                    }
                });
                document.addEventListener('keyup', e => {
                    if ((e.key || '').toLowerCase() === 'g')
                        goHeld = false;
                });

                const cmdOverlay = document.getElementById('cmdOverlay');
                const cmdInput = document.getElementById('cmdInput');
                const cmdList = document.getElementById('cmdList');
                document.getElementById('openCmd')?.addEventListener('click', openCmd);
                function openCmd() {
                    cmdOverlay.classList.add('open');
                    cmdInput.value = '';
                    renderCmd('');
                    setTimeout(() => cmdInput.focus(), 0);
                }
                function closeCmd() {
                    cmdOverlay.classList.remove('open');
                }
                cmdOverlay?.addEventListener('click', e => {
                    if (e.target === cmdOverlay)
                        closeCmd();
                });
                cmdInput?.addEventListener('keydown', e => {
                    const items = [...cmdList.querySelectorAll('li')];
                    const cur = items.findIndex(li => li.getAttribute('aria-selected') === 'true');
                    if (e.key === 'Escape') {
                        closeCmd();
                        return;
                    }
                    if (e.key === 'ArrowDown') {
                        e.preventDefault();
                        setSel(Math.min(items.length - 1, cur + 1));
                        return;
                    }
                    if (e.key === 'ArrowUp') {
                        e.preventDefault();
                        setSel(Math.max(0, cur - 1));
                        return;
                    }
                    if (e.key === 'Enter') {
                        e.preventDefault();
                        const it = items[Math.max(0, cur)];
                        it && it.click();
                        return;
                    }
                });
                cmdInput?.addEventListener('input', e => renderCmd(e.target.value || ''));
                const actions = [
                    {icon: 'bi bi-file-plus', text: 'Tạo đơn', url: ctx + '/request/create', k: 'tao don'},
                    {icon: 'bi bi-list-task', text: 'Đơn của tôi', url: ctx + '/request/list?scope=mine', k: 'don cua toi'},
                    {icon: 'bi bi-people', text: 'Đơn cấp dưới', url: ctx + '/request/list?scope=team', k: 'don cap duoi'},
                    {icon: 'bi bi-calendar-week', text: 'Agenda', url: ctx + '/agenda', k: 'agenda lich'},
                    {icon: 'bi bi-palette', text: 'Đổi theme', run: () => themeBtn?.click(), k: 'theme dark light'},
                ];
                function renderCmd(q) {
                    const qq = normalize(q);
                    const rs = actions.map(a => ({a, score: score(normalize(a.text + ' ' + a.k), qq)}))
                            .filter(x => qq ? x.score > -1 : true)
                            .sort((x, y) => y.score - x.score).slice(0, 8);
                    cmdList.innerHTML = rs.map((x, i) =>
                        '<li role="option" aria-selected="' + (i === 0 ? 'true' : 'false') + '">' +
                                '<i class="' + x.a.icon + '"></i><span>' + x.a.text + '</span>' +
                                '</li>'
                    ).join('');
                    [...cmdList.children].forEach((li, i) => {
                        li.addEventListener('mouseenter', () => setSel(i));
                        li.addEventListener('click', () => {
                            closeCmd();
                            const a = rs[i].a;
                            if (a.run)
                                a.run();
                            else if (a.url)
                                location.href = a.url;
                        });
                    });
                }
                function setSel(i) {
                    [...cmdList.children].forEach((li, idx) => li.setAttribute('aria-selected', idx === i ? 'true' : 'false'));
                }
                function normalize(s) {
                    return (s || '').toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, '');
                }
                function score(text, q) {
                    if (!q)
                        return 0;
                    let p = 0, i = 0;
                    for (const ch of q) {
                        const j = text.indexOf(ch, i); if (j < 0)
                            return -1;
                        p += 1 - (j - i > 3 ? .5 : .1 * (j - i));
                        i = j + 1;
                    }
                    return p;
                }

                /* DB toast if down */
                const dbToast = document.getElementById('dbToast');
                if (dbToast) {
                    dbToast.classList.add('show');
                    setTimeout(() => dbToast.classList.remove('show'), 4000);
                }

                /* Greeting */
                (function greet() {
                    const el = document.getElementById('greet');
                    if (!el)
                        return;
                    const h = new Date().getHours();
                    let msg = 'Xin chào, chúc bạn một ngày hiệu quả!';
                    if (h < 10)
                        msg = 'Chào buổi sáng 🌤️ – Bắt đầu ngày mới thật hăng hái!';
                    else if (h < 14)
                        msg = 'Chúc buổi trưa dễ chịu 🕛 – Nghỉ ngơi một chút nhé.';
                    else if (h < 18)
                        msg = 'Buổi chiều năng suất nhé ☕ – Cùng xử lý các request.';
                    else
                        msg = 'Tối an yên ✨ – Tổng kết ngày làm việc nào.';
                    el.textContent = msg;
                })();

                /* Hero particles */
                (function particles() {
                    const canvas = document.getElementById('heroCanvas');
                    if (!canvas)
                        return;
                    const ctx2 = canvas.getContext('2d');
                    const DPR = Math.min(2, window.devicePixelRatio || 1);
                    let W = 0, H = 0, dots = [];
                    function resize() {
                        const r = canvas.getBoundingClientRect();
                        W = r.width;
                        H = r.height;
                        canvas.width = W * DPR;
                        canvas.height = H * DPR;
                        ctx2.setTransform(DPR, 0, 0, DPR, 0, 0);
                        dots = Array.from({length: 48}, () => ({x: Math.random() * W, y: Math.random() * H, vx: (Math.random() - .5) * .3, vy: (Math.random() - .5) * .3, r: Math.random() * 2 + .6, a: .35 + .35 * Math.random()}));
                    }
                    function step() {
                        ctx2.clearRect(0, 0, W, H);
                        dots.forEach(d => {
                            d.x += d.vx;
                            d.y += d.vy;
                            if (d.x < 0 || d.x > W)
                                d.vx *= -1;
                            if (d.y < 0 || d.y > H)
                                d.vy *= -1;
                            ctx2.beginPath();
                            ctx2.arc(d.x, d.y, d.r, 0, Math.PI * 2);
                            ctx2.fillStyle = `rgba(37,99,235,\${d.a})`;
                            ctx2.fill();
                        });
                        requestAnimationFrame(step);
                    }
                    new ResizeObserver(resize).observe(document.querySelector('.hero'));
                    resize();
                    step();
                })();

                /* Keyboard modal */
                const kbdModal = document.getElementById('kbdModal');
                const openKbdBtn = document.getElementById('openKbd');
                const closeKbdBtn = document.getElementById('closeKbd');
                function openKbd() {
                    kbdModal.classList.add('open');
                }
                function closeKbd() {
                    kbdModal.classList.remove('open');
                }
                openKbdBtn?.addEventListener('click', openKbd);
                closeKbdBtn?.addEventListener('click', closeKbd);
                kbdModal?.addEventListener('click', e => {
                    if (e.target === kbdModal)
                        closeKbd();
                });

                /* Dynamic background gradient animation */
                (function () {
                    const body = document.body;
                    let step = 0;
                    let hue = 200;
                    const speed = 0.15;
                    const gradLayer = document.createElement('div');
                    gradLayer.id = 'bg-anim';
                    gradLayer.style.position = 'fixed';
                    gradLayer.style.inset = 0;
                    gradLayer.style.zIndex = '-1';
                    gradLayer.style.background = 'radial-gradient(circle at 30% 40%, #2563eb33, transparent 60%), radial-gradient(circle at 70% 60%, #8b5cf633, transparent 60%)';
                    gradLayer.style.transition = 'opacity .8s ease';
                    body.prepend(gradLayer);

                    body.addEventListener('pointermove', e => {
                        const x = e.clientX / window.innerWidth - .5;
                        const y = e.clientY / window.innerHeight - .5;
gradLayer.style.transform = `translate(${x * 24}px, ${y * 18}px)`; // KHÔNG scale
                    });
                    body.addEventListener('pointerleave', () => gradLayer.style.transform = '');

                    function loop() {
                        step += speed;
                        hue = (hue + 0.15) % 360;
                        const h1 = hue;
                        const h2 = (hue + 60) % 360;
                        const color1 = `hsla(\${h1}, 70%, 60%, .25)`;
                        const color2 = `hsla(\${h2}, 70%, 60%, .25)`;
                        gradLayer.style.background = `
                radial-gradient(1000px 600px at 20% 30%, \${color1}, transparent 70%),
                radial-gradient(1000px 600px at 80% 70%, \${color2}, transparent 70%)`;
                        requestAnimationFrame(loop);
                    }
                    loop();

                    const observer = new MutationObserver(() => {
                        const theme = root.getAttribute('data-theme') || 'light';
                        gradLayer.style.opacity = (theme === 'dark' ? '1' : '.8');
                    });
                    observer.observe(root, {attributes: true, attributeFilter: ['data-theme']});
                })();
            })();
      