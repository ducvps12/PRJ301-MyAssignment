<%-- ??t trong <head> m?i trang --%>
<script>
  (function () {
    try {
      var saved = localStorage.getItem('theme')
               || document.documentElement.getAttribute('data-theme')
               || 'light';
      if (saved === 'auto') {
        saved = (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches)
                ? 'dark' : 'light';
      }
      document.documentElement.setAttribute('data-theme', saved);
      // C?p nh?t meta color-scheme n?u có
      var m = document.querySelector('meta[name="color-scheme"]');
      if (m) m.setAttribute('content', saved === 'dark' ? 'dark light' : 'light dark');
    } catch (e) {
      document.documentElement.setAttribute('data-theme', 'light');
    }
  })();
</script>
