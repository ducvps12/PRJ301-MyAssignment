
(function () {
  const BTN = document.getElementById('globalThemeToggle') || document.getElementById('themeToggle');
  if (!BTN) return;

  const MEDIA = window.matchMedia ? window.matchMedia('(prefers-color-scheme: dark)') : null;

  function applyTheme(mode) {
    let t = mode;
    if (t === 'auto') t = (MEDIA && MEDIA.matches) ? 'dark' : 'light';
    document.documentElement.setAttribute('data-theme', t);

    const m = document.querySelector('meta[name="color-scheme"]');
    if (m) m.setAttribute('content', t === 'dark' ? 'dark light' : 'light dark');
  }
  function getTheme() { return localStorage.getItem('theme') || 'light'; }
  function setTheme(mode) {
    localStorage.setItem('theme', mode);
    applyTheme(mode);
    if (BTN) {
      BTN.dataset.mode = mode;
      BTN.title = 'Theme: ' + mode + ' (Ctrl/Cmd + J)';
      BTN.textContent = mode === 'dark' ? '🌙' : (mode === 'auto' ? '🌓' : '☀️');
    }
  }

  setTheme(getTheme());
  if (MEDIA) MEDIA.addEventListener('change', () => { if (getTheme()==='auto') applyTheme('auto'); });

  BTN.addEventListener('click', () => {
    const cur = getTheme();
    const next = cur === 'light' ? 'dark' : (cur === 'dark' ? 'auto' : 'light');
    setTheme(next);
  });
  document.addEventListener('keydown', (e) => {
    if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase()==='j') { e.preventDefault(); BTN.click(); }
  });
})();
