<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi" data-theme="${sessionScope.theme != null ? sessionScope.theme : 'light'}">
<head>
  <meta charset="UTF-8">
  <title>${param.title != null ? param.title : 'Admin'}</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <style>
    :root{ --sidebar-w:240px; --sidebar-mini-w:72px; }
    .sidebar{ position:fixed; inset:0 auto 0 0; width:var(--sidebar-w); z-index:30; }
    .main{ margin-left:var(--sidebar-w); min-height:100vh; padding:16px; }
    body.sidebar-mini .sidebar{ width:var(--sidebar-mini-w); }
    body.sidebar-mini .main{ margin-left:var(--sidebar-mini-w); }
  </style>
</head>
<body>
  <jsp:include page="/WEB-INF/views/common/_admin_header.jsp"/>
  <jsp:include page="/WEB-INF/views/common/_admin_sidebar.jsp"/>

  <main class="main">
    <jsp:include page="${param.content}"/>
  </main>

  <jsp:include page="/WEB-INF/views/common/_admin_footer.jsp"/>

  <script>
    // Toggle mini sidebar (giữ trạng thái)
    (function(){
      if(localStorage.getItem('sidebar-mini')==='true') document.body.classList.add('sidebar-mini');
      document.querySelectorAll('[data-toggle-sidebar]').forEach(b=>{
        b.addEventListener('click', ()=>{
          document.body.classList.toggle('sidebar-mini');
          localStorage.setItem('sidebar-mini', document.body.classList.contains('sidebar-mini'));
        });
      });
    })();
  </script>
</body>
</html>
