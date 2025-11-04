
(function () {
  const $ = (q, root=document) => root.querySelector(q);
  const $$ = (q, root=document) => Array.from(root.querySelectorAll(q));

  // ===== Theme toggle (persist localStorage) =====
  const themeToggle = $('#themeToggle');
  function getTheme(){ return localStorage.getItem('theme') || 'auto'; }
  function setTheme(mode){
    localStorage.setItem('theme', mode);
    document.documentElement.setAttribute('data-theme', mode === 'auto' ? (matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light') : mode);
  }
  setTheme(getTheme());
  themeToggle?.addEventListener('click', ()=>{
    const cur = getTheme();
    const next = cur === 'light' ? 'dark' : (cur === 'dark' ? 'auto' : 'light');
    setTheme(next);
    themeToggle.title = `Theme: ${next}`;
  });
  // Shortcut Ctrl+J
  document.addEventListener('keydown', e=>{
    if((e.ctrlKey||e.metaKey) && e.key.toLowerCase()==='j'){ e.preventDefault(); themeToggle?.click(); }
  });

  // ===== Sidebar toggle
  const app = $('.app');
  $('#sidebarToggle')?.addEventListener('click', ()=> app.classList.toggle('collapsed'));
  // Shortcut Ctrl+B
  document.addEventListener('keydown', e=>{
    if((e.ctrlKey||e.metaKey) && e.key.toLowerCase()==='b'){ e.preventDefault(); app.classList.toggle('collapsed'); }
  });

  // ===== Scroll top
  $('#toTop')?.addEventListener('click', e=>{ e.preventDefault(); window.scrollTo({top:0,behavior:'smooth'}); });

  // ===== Table interactions
  const data = Array.isArray(window.__LEAVES__) ? window.__LEAVES__ : [];
  const tbody = $('#leaveTbody');
  const pageInfo = $('#pageInfo');
  const pageSizeSel = $('#pageSize');
  const btnPrev = $('#prevPage'), btnNext = $('#nextPage');
  const btnSortName = $('#btnSortName'), btnSortFrom = $('#btnSortFrom');
  const searchInput = $('#searchInput'), searchClear = $('#searchClear');
  const skeleton = $('#skeleton');

  let state = {
    page: 1,
    pageSize: parseInt(pageSizeSel?.value || '10', 10),
    sortKey: 'name',
    sortDir: 'asc',
    q: ''
  };

  function normalize(s){ return (s||'').toString().toLowerCase().normalize('NFKD').replace(/[\u0300-\u036f]/g,''); }

  function filterData(){
    const q = normalize(state.q);
    return data.filter(r => normalize(r.name).includes(q) || normalize(r.division).includes(q));
  }

  function sortData(rows){
    const {sortKey, sortDir} = state;
    const dir = sortDir === 'asc' ? 1 : -1;
    return rows.slice().sort((a,b)=>{
      const va = (a[sortKey] || '').toString();
      const vb = (b[sortKey] || '').toString();
      return va.localeCompare(vb, 'vi') * dir;
    });
  }

  function paginate(rows){
    const total = rows.length;
    const pages = Math.max(1, Math.ceil(total / state.pageSize));
    state.page = Math.min(state.page, pages);
    const start = (state.page - 1) * state.pageSize;
    return { slice: rows.slice(start, start + state.pageSize), total, pages };
  }

  function render(){
    // skeleton off
    skeleton && (skeleton.style.display = 'none');

    const filtered = filterData();
    const sorted = sortData(filtered);
    const {slice, total, pages} = paginate(sorted);

    tbody.innerHTML = '';
    if (total === 0) {
      const tr = document.createElement('tr');
      tr.innerHTML = `<td colspan="4" class="muted">${tbody.dataset.emptyText || 'Không có dữ liệu.'}</td>`;
      tbody.appendChild(tr);
    } else {
      slice.forEach(r=>{
        const tr = document.createElement('tr');
        tr.innerHTML = `
          <td data-col="name">${r.name}</td>
          <td data-col="division">${r.division === '-' ? '—' : r.division}</td>
          <td data-col="from">${r.from}</td>
          <td data-col="to">${r.to}</td>
        `;
        tbody.appendChild(tr);
      });
    }

    pageInfo.textContent = `Trang ${state.page}/${pages}`;
    btnPrev.disabled = (state.page <= 1);
    btnNext.disabled = (state.page >= pages);
  }

  // Bindings
  pageSizeSel?.addEventListener('change', ()=>{
    state.pageSize = parseInt(pageSizeSel.value,10);
    state.page = 1;
    render();
  });
  btnPrev?.addEventListener('click', ()=>{ state.page=Math.max(1, state.page-1); render(); });
  btnNext?.addEventListener('click', ()=>{ state.page=state.page+1; render(); });

  btnSortName?.addEventListener('click', ()=>{
    state.sortKey = 'name';
    state.sortDir = (state.sortDir==='asc' && state.sortKey==='name') ? 'desc':'asc';
    render();
  });
  btnSortFrom?.addEventListener('click', ()=>{
    state.sortKey = 'from';
    state.sortDir = (state.sortDir==='asc' && state.sortKey==='from') ? 'desc':'asc';
    render();
  });

  searchInput?.addEventListener('input', ()=>{
    state.q = searchInput.value.trim();
    state.page = 1;
    render();
  });
  searchClear?.addEventListener('click', ()=>{
    searchInput.value=''; state.q=''; state.page=1; render();
  });

  // Export CSV
  $('#btnExportCsv')?.addEventListener('click', ()=>{
    const rows = filterData();
    const header = ['Nhân sự','Phòng ban','Từ','Đến'];
    const body = rows.map(r => [r.name, r.division, r.from, r.to]);
    const csv = [header].concat(body).map(a => a.map(x => `"${String(x).replace(/"/g,'""')}"`).join(',')).join('\r\n');
    const blob = new Blob([csv], {type:'text/csv;charset=utf-8;'});
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url; a.download = 'nghi_hom_nay.csv';
    document.body.appendChild(a); a.click(); a.remove();
    URL.revokeObjectURL(url);
  });

  // Fake refresh (client) – nếu bạn muốn server refresh, gắn link tới /admin/hr
  $('#btnRefresh')?.addEventListener('click', ()=> { location.reload(); });

  // Hide skeleton after first render
  render();
})();
