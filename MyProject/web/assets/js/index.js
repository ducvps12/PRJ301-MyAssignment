/* ============================================
   ENHANCED LANDING PAGE JS - PREMIUM FEATURES
   ============================================ */

            (() => {
    'use strict';
    
                const ctx = '<%=request.getContextPath()%>';
                const root = document.documentElement;
                const themeBtn = document.getElementById('themeToggle');
                const themeIcon = document.getElementById('themeIcon');
                const toast = document.getElementById('toast');

    /* ===== THEME MANAGEMENT ===== */
                (function bootTheme() {
        const saved = localStorage.getItem('theme') || 'light';
                        root.setAttribute('data-theme', saved);
                    syncIcon();
                })();

                function syncIcon() {
        const isDark = (root.getAttribute('data-theme') || 'light') === 'dark';
        themeIcon.className = isDark ? 'bi bi-sun' : 'bi bi-moon-stars';
                }

                themeBtn?.addEventListener('click', () => {
        const current = root.getAttribute('data-theme') || 'light';
        const next = current === 'light' ? 'dark' : 'light';
                    root.setAttribute('data-theme', next);
                    localStorage.setItem('theme', next);
                    syncIcon();
        tip('Đã chuyển sang giao diện ' + (next === 'dark' ? 'tối 🌙' : 'sáng ☀️'));
        // Add smooth transition effect
        document.body.style.transition = 'background 0.3s ease, color 0.3s ease';
        setTimeout(() => document.body.style.transition = '', 300);
                });

    /* ===== TOAST NOTIFICATION SYSTEM ===== */
    function tip(msg, ms = 2000) {
        if (!toast) return;
                    toast.textContent = msg;
                    toast.classList.add('show');
        
        // Add success animation
        toast.style.animation = 'slide-in-right 0.3s ease';
        
        setTimeout(() => {
            toast.style.animation = 'slide-out-right 0.3s ease';
            setTimeout(() => {
                toast.classList.remove('show');
            }, 300);
        }, ms);
    }

    // Add CSS for slide-out animation
    if (!document.getElementById('toast-anim-style')) {
        const style = document.createElement('style');
        style.id = 'toast-anim-style';
        style.textContent = `
            @keyframes slide-out-right {
                to {
                    opacity: 0;
                    transform: translateX(100px);
                }
            }
        `;
        document.head.appendChild(style);
    }

    /* ===== ANIMATED COUNTERS WITH INTERSECTION OBSERVER ===== */
                const counters = document.querySelectorAll('.metric .num');
    const counterIO = ('IntersectionObserver' in window) 
        ? new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    const target = entry.target;
                    const finalValue = parseInt(target.getAttribute('data-count') || '0', 10);
                    animateCounter(target, finalValue);
                    counterIO.unobserve(target);
                }
            });
        }, { threshold: 0.5, rootMargin: '50px' })
        : null;

    counters.forEach(el => {
        if (counterIO) {
            counterIO.observe(el);
        } else {
            const finalValue = parseInt(el.getAttribute('data-count') || '0', 10);
            animateCounter(el, finalValue);
        }
    });

    function animateCounter(el, to) {
        let current = 0;
        const duration = 2000;
        const startTime = performance.now();
        const suffix = el.textContent.includes('%') ? '%' : '';
        const prefix = el.textContent.includes('<') ? '< ' : '';
        
        const step = (timestamp) => {
            const elapsed = timestamp - startTime;
            const progress = Math.min(elapsed / duration, 1);
            
            // Easing function for smooth animation
            const easeOutCubic = 1 - Math.pow(1 - progress, 3);
            current = Math.floor(to * easeOutCubic);
            
            el.textContent = prefix + current.toLocaleString('vi-VN') + suffix;
            
            if (progress < 1) {
                            requestAnimationFrame(step);
            } else {
                el.textContent = prefix + to.toLocaleString('vi-VN') + suffix;
                // Add completion animation
                el.style.transform = 'scale(1.1)';
                setTimeout(() => {
                    el.style.transition = 'transform 0.3s ease';
                    el.style.transform = 'scale(1)';
                }, 100);
            }
        };
        
                    requestAnimationFrame(step);
                }

    /* ===== ILLUSTRATION PARALLAX EFFECT ===== */
    (function initParallax() {
                    const wrap = document.querySelector('.illus');
                    const ring = document.getElementById('ringBg');
        if (!wrap || !ring) return;

        let isHovering = false;
        
        wrap.addEventListener('pointerenter', () => isHovering = true);
        wrap.addEventListener('pointerleave', () => {
            isHovering = false;
            ring.style.transform = '';
        });

        wrap.addEventListener('pointermove', (e) => {
            if (!isHovering) return;
            const rect = wrap.getBoundingClientRect();
            const x = (e.clientX - rect.left) / rect.width - 0.5;
            const y = (e.clientY - rect.top) / rect.height - 0.5;
            
            // Enhanced 3D effect
            ring.style.transform = `
                rotateX(${-y * 12}deg) 
                rotateY(${x * 12}deg) 
                translateZ(20px)
            `;
            ring.style.transition = 'transform 0.1s ease-out';
        });

        // Add subtle continuous animation
        let angle = 0;
        setInterval(() => {
            if (!isHovering) {
                angle += 0.5;
                ring.style.transform = `rotateZ(${angle}deg)`;
                ring.style.transition = 'transform 20s linear';
            }
        }, 100);
                })();

    /* ===== COPY DEMO ACCOUNTS ===== */
    document.getElementById('copyDemo')?.addEventListener('click', async () => {
        const txt = 'TÀI KHOẢN DEMO\n\n📋 Employee\nUser: d.staff\nPass: 123456\n\n👔 Manager\nUser: a.lead\nPass: 123456';
        
        try {
                    if (navigator.clipboard?.writeText) {
                await navigator.clipboard.writeText(txt);
                tip('✅ Đã sao chép thông tin tài khoản demo!');
                
                // Add visual feedback
                const btn = document.getElementById('copyDemo');
                btn.style.transform = 'scale(0.95)';
                setTimeout(() => {
                    btn.style.transform = 'scale(1)';
                }, 150);
            } else {
                // Fallback for older browsers
                const textarea = document.createElement('textarea');
                textarea.value = txt;
                document.body.appendChild(textarea);
                textarea.select();
                document.execCommand('copy');
                document.body.removeChild(textarea);
                tip('✅ Đã sao chép!');
            }
        } catch (err) {
            tip('❌ Không thể sao chép. Vui lòng thử lại.');
        }
    });

    /* ===== KEYBOARD SHORTCUTS SYSTEM ===== */
                let goHeld = false;
    let goTimeout;

    document.addEventListener('keydown', (e) => {
        const key = (e.key || '').toLowerCase();
        const isMac = navigator.platform.toUpperCase().indexOf('MAC') >= 0;
        const modKey = isMac ? e.metaKey : e.ctrlKey;

        // Command Palette: Ctrl/Cmd + K
        if (modKey && key === 'k') {
                        e.preventDefault();
                        openCmd();
                        return;
                    }

        // Theme Toggle: D
        if (key === 'd' && !modKey && !e.altKey && !e.shiftKey) {
            // Only trigger if not typing in input
            if (e.target.tagName !== 'INPUT' && e.target.tagName !== 'TEXTAREA') {
                        e.preventDefault();
                        themeBtn?.click();
                        return;
                    }
        }

        // Keyboard shortcuts help: ?
        if (key === '?' && !modKey) {
            if (e.target.tagName !== 'INPUT' && e.target.tagName !== 'TEXTAREA') {
                        e.preventDefault();
                        openKbd();
                        return;
                    }
        }

        // G navigation shortcuts
        if (key === 'g' && !modKey) {
            if (e.target.tagName !== 'INPUT' && e.target.tagName !== 'TEXTAREA') {
                        goHeld = true;
                clearTimeout(goTimeout);
                goTimeout = setTimeout(() => {
                    goHeld = false;
                }, 1000);
                        return;
                    }
        }

                    if (goHeld) {
            switch (key) {
                case 'c':
                    e.preventDefault();
                    window.location.href = ctx + '/request/create';
                    break;
                case 'm':
                    e.preventDefault();
                    window.location.href = ctx + '/request/list?scope=mine';
                    break;
                case 't':
                    e.preventDefault();
                    window.location.href = ctx + '/request/list?scope=team';
                    break;
                        }
                        goHeld = false;
                    }
                });

    document.addEventListener('keyup', (e) => {
        if ((e.key || '').toLowerCase() === 'g') {
                        goHeld = false;
            clearTimeout(goTimeout);
        }
                });

    /* ===== COMMAND PALETTE SYSTEM ===== */
                const cmdOverlay = document.getElementById('cmdOverlay');
                const cmdInput = document.getElementById('cmdInput');
                const cmdList = document.getElementById('cmdList');
    let selectedIndex = 0;

                document.getElementById('openCmd')?.addEventListener('click', openCmd);

                function openCmd() {
                    cmdOverlay.classList.add('open');
                    cmdInput.value = '';
        selectedIndex = 0;
                    renderCmd('');
        setTimeout(() => {
            cmdInput.focus();
            document.body.style.overflow = 'hidden';
        }, 10);
                }

                function closeCmd() {
                    cmdOverlay.classList.remove('open');
        selectedIndex = 0;
        document.body.style.overflow = '';
                }

    cmdOverlay?.addEventListener('click', (e) => {
        if (e.target === cmdOverlay) {
                        closeCmd();
        }
                });

    cmdInput?.addEventListener('keydown', (e) => {
                    const items = [...cmdList.querySelectorAll('li')];
        
                    if (e.key === 'Escape') {
                        closeCmd();
                        return;
                    }

                    if (e.key === 'ArrowDown') {
                        e.preventDefault();
            selectedIndex = Math.min(items.length - 1, selectedIndex + 1);
            updateSelection(items);
            scrollToItem(items[selectedIndex]);
                        return;
                    }

                    if (e.key === 'ArrowUp') {
                        e.preventDefault();
            selectedIndex = Math.max(0, selectedIndex - 1);
            updateSelection(items);
            scrollToItem(items[selectedIndex]);
                        return;
                    }

                    if (e.key === 'Enter') {
                        e.preventDefault();
            const item = items[selectedIndex];
            if (item) item.click();
                        return;
                    }
                });

    cmdInput?.addEventListener('input', (e) => {
        selectedIndex = 0;
        renderCmd(e.target.value || '');
    });

    function updateSelection(items) {
        items.forEach((item, idx) => {
            item.setAttribute('aria-selected', idx === selectedIndex ? 'true' : 'false');
        });
    }

    function scrollToItem(item) {
        if (!item) return;
        item.scrollIntoView({ block: 'nearest', behavior: 'smooth' });
    }

                const actions = [
        { icon: 'bi bi-file-plus', text: 'Tạo đơn nghỉ phép', url: ctx + '/request/create', keywords: 'tao don nghi phep create' },
        { icon: 'bi bi-list-task', text: 'Đơn của tôi', url: ctx + '/request/list?scope=mine', keywords: 'don cua toi mine' },
        { icon: 'bi bi-people', text: 'Đơn cấp dưới', url: ctx + '/request/list?scope=team', keywords: 'don cap duoi team' },
        { icon: 'bi bi-calendar-week', text: 'Agenda phòng ban', url: ctx + '/agenda', keywords: 'agenda lich phong ban' },
        { icon: 'bi bi-palette', text: 'Đổi theme (Dark/Light)', run: () => themeBtn?.click(), keywords: 'theme dark light mode' },
    ];

    function renderCmd(query) {
        const normalizedQuery = normalize(query);
        const results = actions
            .map(action => ({
                action,
                score: calculateScore(normalize(action.text + ' ' + action.keywords), normalizedQuery)
            }))
            .filter(item => normalizedQuery ? item.score > -1 : true)
            .sort((a, b) => b.score - a.score)
            .slice(0, 8);

        if (results.length === 0) {
            cmdList.innerHTML = '<li style="padding: 20px; text-align: center; color: var(--muted);">Không tìm thấy kết quả</li>';
            return;
        }

        cmdList.innerHTML = results.map((result, idx) => `
            <li role="option" aria-selected="${idx === selectedIndex ? 'true' : 'false'}">
                <i class="${result.action.icon}"></i>
                <span>${highlightMatch(result.action.text, normalizedQuery)}</span>
            </li>
        `).join('');

        [...cmdList.children].forEach((li, idx) => {
            li.addEventListener('mouseenter', () => {
                selectedIndex = idx;
                updateSelection([...cmdList.children]);
            });

                        li.addEventListener('click', () => {
                            closeCmd();
                const action = results[idx].action;
                if (action.run) {
                    action.run();
                } else if (action.url) {
                    window.location.href = action.url;
                }
                        });
                    });
                }

    function highlightMatch(text, query) {
        if (!query) return text;
        const normalizedText = normalize(text);
        const regex = new RegExp(`(${query.split('').join('.*?')})`, 'gi');
        return text.replace(regex, '<mark style="background: rgba(96,165,250,.3); padding: 2px 4px; border-radius: 4px;">$1</mark>');
    }

    function normalize(str) {
        return (str || '')
            .toLowerCase()
            .normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '')
            .replace(/đ/g, 'd')
            .replace(/Đ/g, 'D');
    }

    function calculateScore(text, query) {
        if (!query) return 0;
        let score = 0;
        let lastIndex = -1;
        const queryChars = query.split('');

        for (const char of queryChars) {
            const index = text.indexOf(char, lastIndex + 1);
            if (index < 0) return -1;
            const distance = index - lastIndex;
            score += 1 - (distance > 3 ? 0.5 : 0.1 * distance);
            lastIndex = index;
        }

        // Bonus for exact matches
        if (text.includes(query)) {
            score += 5;
        }

        // Bonus for starts with
        if (text.startsWith(query)) {
            score += 10;
        }

        return score;
    }

    /* ===== DATABASE STATUS TOAST ===== */
                const dbToast = document.getElementById('dbToast');
    if (dbToast && dbToast.classList.contains('show')) {
        setTimeout(() => {
            dbToast.style.animation = 'slide-out-right 0.3s ease';
            setTimeout(() => {
                dbToast.classList.remove('show');
            }, 300);
        }, 5000);
    }

    /* ===== DYNAMIC GREETING ===== */
    (function setGreeting() {
                    const el = document.getElementById('greet');
        if (!el) return;

        const hour = new Date().getHours();
        let message = 'Xin chào, chúc bạn một ngày hiệu quả!';
        let emoji = '👋';

        if (hour < 6) {
            message = 'Chào buổi sáng sớm 🌙 – Nghỉ ngơi thêm chút nhé!';
            emoji = '🌙';
        } else if (hour < 10) {
            message = 'Chào buổi sáng 🌤️ – Bắt đầu ngày mới thật hăng hái!';
            emoji = '🌤️';
        } else if (hour < 12) {
            message = 'Buổi sáng năng động ⚡ – Chúc bạn làm việc hiệu quả!';
            emoji = '⚡';
        } else if (hour < 14) {
            message = 'Chúc buổi trưa dễ chịu 🕛 – Nghỉ ngơi một chút nhé.';
            emoji = '🕛';
        } else if (hour < 18) {
            message = 'Buổi chiều năng suất nhé ☕ – Cùng xử lý các request!';
            emoji = '☕';
        } else if (hour < 22) {
            message = 'Buổi tối an yên ✨ – Tổng kết ngày làm việc nào.';
            emoji = '✨';
        } else {
            message = 'Đêm khuya rồi 🌙 – Nhớ nghỉ ngơi đầy đủ nhé!';
            emoji = '🌙';
        }

        // Typewriter effect
        el.textContent = '';
        let i = 0;
        const typeWriter = () => {
            if (i < message.length) {
                el.textContent += message.charAt(i);
                i++;
                setTimeout(typeWriter, 50);
            } else {
                el.innerHTML += ' ' + emoji;
            }
        };
        setTimeout(typeWriter, 500);
                })();

    /* ===== HERO CANVAS PARTICLES ===== */
    (function initParticles() {
                    const canvas = document.getElementById('heroCanvas');
        if (!canvas) return;

        const ctx2d = canvas.getContext('2d');
                    const DPR = Math.min(2, window.devicePixelRatio || 1);
        let width = 0, height = 0;
        let particles = [];
        let animationId;

                    function resize() {
            const rect = canvas.getBoundingClientRect();
            width = rect.width;
            height = rect.height;
            canvas.width = width * DPR;
            canvas.height = height * DPR;
            ctx2d.scale(DPR, DPR);
            
            // Create more particles for better effect
            particles = Array.from({ length: 60 }, () => ({
                x: Math.random() * width,
                y: Math.random() * height,
                vx: (Math.random() - 0.5) * 0.5,
                vy: (Math.random() - 0.5) * 0.5,
                radius: Math.random() * 3 + 1,
                alpha: 0.3 + Math.random() * 0.4,
                color: Math.random() > 0.5 ? '96,165,250' : '196,181,253'
            }));
        }

        function animate() {
            ctx2d.clearRect(0, 0, width, height);

            particles.forEach((particle, i) => {
                // Update position
                particle.x += particle.vx;
                particle.y += particle.vy;

                // Bounce off edges
                if (particle.x < 0 || particle.x > width) particle.vx *= -1;
                if (particle.y < 0 || particle.y > height) particle.vy *= -1;

                // Keep particles in bounds
                particle.x = Math.max(0, Math.min(width, particle.x));
                particle.y = Math.max(0, Math.min(height, particle.y));

                // Draw particle
                ctx2d.beginPath();
                ctx2d.arc(particle.x, particle.y, particle.radius, 0, Math.PI * 2);
                ctx2d.fillStyle = `rgba(${particle.color}, ${particle.alpha})`;
                ctx2d.fill();

                // Draw connections
                particles.slice(i + 1).forEach(other => {
                    const dx = particle.x - other.x;
                    const dy = particle.y - other.y;
                    const distance = Math.sqrt(dx * dx + dy * dy);

                    if (distance < 150) {
                        ctx2d.beginPath();
                        ctx2d.moveTo(particle.x, particle.y);
                        ctx2d.lineTo(other.x, other.y);
                        ctx2d.strokeStyle = `rgba(${particle.color}, ${0.2 * (1 - distance / 150)})`;
                        ctx2d.lineWidth = 1;
                        ctx2d.stroke();
                    }
                });
            });

            animationId = requestAnimationFrame(animate);
        }

        const resizeObserver = new ResizeObserver(() => {
            resize();
        });
        
        const heroSection = document.querySelector('.hero');
        if (heroSection) {
            resizeObserver.observe(heroSection);
        }

                    resize();
        animate();

        // Pause animation when tab is hidden
        document.addEventListener('visibilitychange', () => {
            if (document.hidden) {
                cancelAnimationFrame(animationId);
            } else {
                animate();
            }
        });
                })();

    /* ===== KEYBOARD SHORTCUTS MODAL ===== */
                const kbdModal = document.getElementById('kbdModal');
                const openKbdBtn = document.getElementById('openKbd');
                const closeKbdBtn = document.getElementById('closeKbd');

                function openKbd() {
                    kbdModal.classList.add('open');
        document.body.style.overflow = 'hidden';
                }

                function closeKbd() {
                    kbdModal.classList.remove('open');
        document.body.style.overflow = '';
                }

                openKbdBtn?.addEventListener('click', openKbd);
                closeKbdBtn?.addEventListener('click', closeKbd);
    
    kbdModal?.addEventListener('click', (e) => {
        if (e.target === kbdModal) {
                        closeKbd();
        }
    });

    /* ===== SCROLL ANIMATIONS ===== */
    (function initScrollAnimations() {
        const observerOptions = {
            threshold: 0.1,
            rootMargin: '0px 0px -100px 0px'
        };

        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('visible');
                    observer.unobserve(entry.target);
                }
            });
        }, observerOptions);

        // Add fade-in-section class to sections
        const sections = document.querySelectorAll('section:not(.hero):not(.announce)');
        sections.forEach(section => {
            section.classList.add('fade-in-section');
            observer.observe(section);
        });

        // Animate cards on scroll
        const cards = document.querySelectorAll('.card, .hi, .metric');
        cards.forEach((card, index) => {
            card.style.opacity = '0';
            card.style.transform = 'translateY(30px)';
            card.style.transition = `all 0.6s var(--ease) ${index * 0.1}s`;
            
            const cardObserver = new IntersectionObserver((entries) => {
                entries.forEach(entry => {
                    if (entry.isIntersecting) {
                        entry.target.style.opacity = '1';
                        entry.target.style.transform = 'translateY(0)';
                        cardObserver.unobserve(entry.target);
                    }
                });
            }, { threshold: 0.2 });
            
            cardObserver.observe(card);
        });
    })();

    /* ===== ICON CAROUSEL (auto-rotate 3-5s) ===== */
    (function initIconCarousel(){
        const wrap = document.getElementById('iconCarousel');
        if (!wrap) return;
        const slides = [...wrap.querySelectorAll('.icon-slide')];
        const dots = [...wrap.querySelectorAll('.icon-dots .dot')];
        let idx = 0;
        let timer;
        const delayMs = 4000; // 4s mặc định

        function show(i){
            slides[idx]?.classList.remove('active');
            dots[idx]?.classList.remove('active');
            idx = (i + slides.length) % slides.length;
            slides[idx]?.classList.add('active');
            dots[idx]?.classList.add('active');
        }

        function next(){ show(idx + 1); }

        function play(){
            stop();
            timer = setInterval(next, delayMs);
        }
        function stop(){ if (timer) clearInterval(timer); }

        // init first
        show(0);
        play();

        // pause/resume on hover for professionalism
        wrap.addEventListener('mouseenter', stop);
        wrap.addEventListener('mouseleave', play);

        // click dots
        dots.forEach((d, i)=> d.addEventListener('click', ()=>{ show(i); play(); }));
    })();

    /* ===== NAVBAR SCROLL EFFECT ===== */
    (function initNavbarScroll() {
        const navbar = document.querySelector('.navbar');
        if (!navbar) return;

        let lastScroll = 0;
        window.addEventListener('scroll', () => {
            const currentScroll = window.pageYOffset;
            
            if (currentScroll > 50) {
                navbar.classList.add('scrolled');
            } else {
                navbar.classList.remove('scrolled');
            }

            lastScroll = currentScroll;
        });
    })();

    /* ===== SMOOTH SCROLL FOR ANCHOR LINKS ===== */
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            const href = this.getAttribute('href');
            if (href === '#') return;
            
            const target = document.querySelector(href);
            if (target) {
                e.preventDefault();
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });

    /* ===== CHIP INTERACTIVE ANIMATIONS ===== */
    document.querySelectorAll('.hr-chip').forEach(chip => {
        chip.addEventListener('mouseenter', () => {
            chip.style.animation = 'none';
            setTimeout(() => {
                chip.style.animation = '';
            }, 10);
        });
    });

    /* ===== LAZY LOADING FOR IMAGES ===== */
    if ('IntersectionObserver' in window) {
        const imageObserver = new IntersectionObserver((entries, observer) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    const img = entry.target;
                    if (img.dataset.src) {
                        img.src = img.dataset.src;
                        img.removeAttribute('data-src');
                    }
                    imageObserver.unobserve(img);
                }
            });
        });

        document.querySelectorAll('img[data-src]').forEach(img => {
            imageObserver.observe(img);
        });
    }

    /* ===== PERFORMANCE MONITORING ===== */
    window.addEventListener('load', () => {
        if ('performance' in window) {
            const perfData = window.performance.timing;
            const pageLoadTime = perfData.loadEventEnd - perfData.navigationStart;
            
            if (pageLoadTime < 2000) {
                console.log(`✅ Page loaded in ${pageLoadTime}ms - Excellent!`);
            }
        }
    });

    /* ===== CONSOLE WELCOME MESSAGE ===== */
    console.log('%c🚀 LeaveMgmt - Premium Landing Page', 'color: #60a5fa; font-size: 16px; font-weight: bold;');
    console.log('%cBuilt with ❤️ for optimal UX', 'color: #9ab0c2; font-size: 12px;');

            })();
      