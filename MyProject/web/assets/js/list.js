/* list.js — v20251027
   UX nâng cao: auto-filter + debounce, chips nhanh, sticky header, copy ID,
   bulk select, CSV export, theme/density toggle, phím tắt, toast, loading, …
*/

(function () {
  const qs = (s, el=document) => el.querySelector(s);
  const qsa = (s, el=document) => Array.from(el.querySelectorAll(s));
  const on = (el, ev, fn, opts) => el && el.addEventListener(ev, fn, opts);
  const debounce = (fn, ms=350) => { let t; return (...args)=>{ clearTimeout(t); t=setTimeout(()=>fn(...args), ms); }; };

  const html = document.documentElement;
  const body = document.body;
  const form = qs('#filterForm');
  const table = qs('#reqTable');
  const toast = qs('#toast');
  const loading = qs('#loading');
  const themeToggle = qs('#themeToggle');
  const densityToggle = qs('#densityToggle');

  const isManager = !!qs('#bulkForm');

  /* -------------------- UTILITIES -------------------- */
  const showLoading = (onoff) => {
    if (!loading) return;
    if (onoff) loading.removeAttribute('hidden');
    else loading.setAttribute('hidden','');
  };

  const showToast = (msg, timeout=2200) => {
    if (!toast) return alert(msg);
    toast.textContent = msg;
    toast.classList.add('show');
    setTimeout(()=> toast.classList.remove('show'), timeout);
  };

  const currentCtx = () => (document.body.getAttribute('data-cp') || window.contextPath || ''); // optional

  const serialize = (formEl) => {
    const p = new URLSearchParams(new FormData(formEl));
    // Trim q
    if (p.get('q')) p.set('q', p.get('q').trim());
    return p.toString();
  };

  const goto = (url) => { window.location.href = url; };

  const updateURLKeepScroll = (href) => {
    // dùng khi chỉ muốn pushState nhưng vẫn submit thật ra server-render
    // ở đây ta submit thật (để server phân trang/lọc), nên không dùng pushState
  };

  const ctxUrl = (path) => {
    const ctx = (window.__ctx || (window.contextPath || (qs('base')?.href || '')));
    if (!ctx) return path;
    return path.startsWith('/') ? (ctx + path) : (ctx + '/' + path);
  };

  /* -------------------- AUTOSUBMIT FILTERS -------------------- */
  if (form) {
    // submit helper
    const doSubmit = () => {
      showLoading(true);
      form.submit();
    };

    // change on selects & date inputs
    qsa('select, input[type="date"]', form).forEach(el=>{
      on(el, 'change', doSubmit);
    });

    // search input debounce
    const q = qs('#q', form);
    if (q) {
      on(q, 'keydown', (e)=> {
        // "/" to focus handled below; here just enter submits immediately
        if (e.key === 'Enter') { doSubmit(); }
      });
      on(q, 'input', debounce(()=> { doSubmit(); }, 450));
    }

    // sort & size also trigger submit (already covered by 'change')

    // quick chips
    qsa('.chip[data-quick]').forEach(chip=>{
      on(chip, 'click', ()=>{
        const v = chip.getAttribute('data-quick');
        // reset relevant fields
        const from = qs('#from', form), to = qs('#to', form);
        const status = qs('#statusSel', form);
        const mine = qs('#mineSel', form);
        const sort = qs('#sortSel', form);
        if (from) from.value = '';
        if (to) to.value = '';
        if (sort) sort.value = '';
        if (v === 'week') {
          // set from = first day of current week (Mon) to today
          const now = new Date();
          const day = (now.getDay()+6)%7; // Mon=0
          const start = new Date(now); start.setDate(now.getDate()-day);
          const toISO = d => d.toISOString().slice(0,10);
          if (from) from.value = toISO(start);
          if (to) to.value = toISO(now);
          if (status) status.value = '';
        } else if (v === 'month') {
          const now = new Date();
          const start = new Date(now.getFullYear(), now.getMonth(), 1);
          const toISO = d => d.toISOString().slice(0,10);
          if (from) from.value = toISO(start);
          if (to) to.value = toISO(now);
          if (status) status.value = '';
        } else if (v === 'pending') {
          if (status) status.value = 'PENDING';
        } else if (v === 'approved') {
          if (status) status.value = 'APPROVED';
        } else if (v === 'mine') {
          if (mine) mine.value = '1';
        } else if (v === 'clear') {
          qsa('input[type="date"]', form).forEach(i=> i.value='');
          if (status) status.value = '';
          if (mine) mine.value = '';
          if (q) q.value = '';
          if (sort) sort.value = '';
        }
        // visual
        qsa('.chip[data-quick]').forEach(c=> c.classList.toggle('active', c===chip));
        doSubmit();
      });
    });

    // Export CSV
    const btnCsv = qs('#btnExportCsv');
    if (btnCsv) {
      on(btnCsv, 'click', ()=>{
        const params = serialize(form);
        // endpoint đề xuất: /request/export (GET) – bạn handle trên server đọc y hệt bộ lọc
        const url = ctxUrl('/request/export') + (params ? ('?' + params) : '');
        window.open(url, '_blank');
        showToast('Đang xuất CSV theo bộ lọc hiện tại…');
      });
    }

    // Refresh
    const btnRefresh = qs('#btnRefresh');
    if (btnRefresh) {
      on(btnRefresh, 'click', (e)=> {
        e.preventDefault();
        goto(btnRefresh.getAttribute('href'));
      });
    }
  }

  /* -------------------- THEME & DENSITY -------------------- */
  const applyTheme = (t) => { html.setAttribute('data-theme', t); localStorage.setItem('theme', t); };
  const toggleTheme = () => {
    const cur = html.getAttribute('data-theme') || 'light';
    applyTheme(cur === 'light' ? 'dark' : 'light');
    showToast('Đã đổi giao diện');
  };
  on(themeToggle, 'click', toggleTheme);
  // init theme from localStorage (ưu tiên sessionScope của JSP sẵn có, nên chỉ sync nếu không set)
  const memTheme = localStorage.getItem('theme');
  if (memTheme && !document.documentElement.hasAttribute('data-theme-from-server')) {
    applyTheme(memTheme);
  }

  const applyDensity = (dense) => {
    body.classList.toggle('dense', !!dense);
    localStorage.setItem('dense', dense ? '1' : '0');
  };
  on(densityToggle, 'click', ()=> {
    const d = !body.classList.contains('dense');
    applyDensity(d);
    showToast(d ? 'Chế độ cô đọng' : 'Chế độ thông thoáng');
  });
  if (localStorage.getItem('dense') === '1') applyDensity(true);

  /* -------------------- TABLE INTERACTIONS -------------------- */
  if (table) {
    // Expand/collapse long reason by click
    qsa('[data-expand]', table).forEach(el=>{
      on(el, 'click', ()=> el.classList.toggle('expanded'));
      el.title ||= 'Nhấn để xem đầy đủ';
    });

    // Copy ID
    qsa('[data-copy]', table).forEach(btn=>{
      on(btn, 'click', ()=>{
        const text = btn.getAttribute('data-copy');
        navigator.clipboard.writeText(text).then(()=>{
          showToast(`Đã copy ${text}`);
        });
      });
    });
  }

  /* -------------------- BULK SELECT -------------------- */
  if (isManager) {
    const bulkForm = qs('#bulkForm');
    const bulkAction = qs('#bulkAction');
    const bulkSubmit = qs('#bulkSubmit');
    const chkAll = qs('#chkAll');
    const rowChks = qsa('.rowChk');
    const selCount = qs('#selCount');
    const selCount2 = qs('#selCount2');
    const bulkbar = qs('#bulkbar');
    const selAllPage = qs('#selAllPage');
    const selNone = qs('#selNone');
    const gotoTop = qs('#gotoTop');

    const updateSel = () => {
      const n = rowChks.filter(c=>c.checked).length;
      if (selCount) selCount.textContent = n;
      if (selCount2) selCount2.textContent = n;
      bulkSubmit && (bulkSubmit.disabled = !(n>0 && bulkAction && bulkAction.value));
      if (chkAll) chkAll.checked = n>0 && n===rowChks.length;
    };
    rowChks.forEach(c=> on(c, 'change', updateSel));
    on(bulkAction, 'change', updateSel);
    on(chkAll, 'change', ()=> {
      rowChks.forEach(c=> c.checked = chkAll.checked);
      updateSel();
    });
    on(selAllPage, 'click', ()=> { rowChks.forEach(c=> c.checked = true); updateSel(); });
    on(selNone, 'click', ()=> { rowChks.forEach(c=> c.checked = false); updateSel(); });
    on(gotoTop, 'click', ()=> window.scrollTo({top:0, behavior:'smooth'}));
    updateSel();

    // confirm before submit
    on(bulkForm, 'submit', (e)=>{
      if (!bulkAction || !bulkAction.value) { e.preventDefault(); showToast('Chọn thao tác trước đã'); return; }
      const ok = confirm('Xác nhận thực hiện thao tác hàng loạt?');
      if (!ok) { e.preventDefault(); return; }
      showLoading(true);
    });
  }

  /* -------------------- APPROVE / REJECT MODALS -------------------- */
  const approveDlg = qs('#approveDlg');
  const rejectDlg = qs('#rejectDlg');

  qsa('[data-open-approve]').forEach(btn=>{
    on(btn, 'click', ()=>{
      const id = btn.getAttribute('data-id');
      const input = qs('#approveId', approveDlg);
      if (input) input.value = id;
      approveDlg.showModal();
    });
  });
  qsa('[data-open-reject]').forEach(btn=>{
    on(btn, 'click', ()=>{
      const id = btn.getAttribute('data-id');
      const input = qs('#rejectId', rejectDlg);
      if (input) input.value = id;
      rejectDlg.showModal();
    });
  });
  qsa('[data-close]').forEach(btn=>{
    on(btn, 'click', (e)=>{
      e.preventDefault();
      const dlg = btn.closest('dialog');
      dlg?.close();
    });
  });

  // submit hooks to show loading
  qsa('dialog form').forEach(f=>{
    on(f, 'submit', ()=> showLoading(true));
  });

  /* -------------------- CANCEL (creator) -------------------- */
  const cancelForm = qs('#cancelForm');
  qsa('[data-cancel]').forEach(a=>{
    on(a, 'click', (e)=>{
      e.preventDefault();
      const id = a.getAttribute('data-id');
      if (!id) return;
      if (!confirm('Xác nhận hủy đơn này?')) return;
      const input = qs('#cancelId', cancelForm);
      input.value = id;
      showLoading(true);
      cancelForm.submit();
    });
  });

  /* -------------------- KEYBOARD SHORTCUTS -------------------- */
  on(document, 'keydown', (e)=>{
    // ignore when typing in inputs (except global shortcuts)
    const isTyping = ['INPUT','TEXTAREA','SELECT'].includes(e.target.tagName);
    const key = e.key.toLowerCase();

    // Focus search: "/"
    if (e.key === '/' && !isTyping) {
      e.preventDefault();
      qs('#q')?.focus();
      return;
    }
    // Refresh: R
    if (key === 'r' && !isTyping) {
      e.preventDefault();
      qs('#btnRefresh')?.click();
      return;
    }
    // Theme: T
    if (key === 't' && !isTyping) {
      e.preventDefault();
      themeToggle?.click();
      return;
    }
    // Density: D
    if (key === 'd' && !isTyping) {
      e.preventDefault();
      densityToggle?.click();
      return;
    }
    // Go top: G
    if (key === 'g' && !isTyping) {
      e.preventDefault();
      window.scrollTo({top:0, behavior:'smooth'});
      return;
    }
    // Select all (manager): A
    if (key === 'a' && isManager && !isTyping) {
      e.preventDefault();
      const chk = qs('#chkAll');
      chk && (chk.checked = !chk.checked);
      chk?.dispatchEvent(new Event('change', {bubbles:true}));
      return;
    }
    // Pagination with , and .
    const prev = qs('.pagination a[aria-label="Trang trước"]');
    const next = qs('.pagination a[aria-label="Trang sau"]');
    if (!isTyping && (e.key === ',' || e.key === '.')) {
      e.preventDefault();
      if (e.key === ',' && prev) prev.click();
      if (e.key === '.' && next) next.click();
    }
  });

  /* -------------------- INIT HINT -------------------- */
  window.addEventListener('load', ()=>{
    // Nếu có message/error từ server, hiện toast nhẹ để người dùng thấy
    const ok = qs('.msg.ok'); const no = qs('.msg.no');
    if (ok) showToast(ok.textContent.trim());
    if (no) showToast(no.textContent.trim(), 3500);
  });
})();
