
(function(){
  const btn = document.getElementById('adMenuBtn');
  const sb  = document.getElementById('adSidebar');
  if(btn && sb){
    btn.addEventListener('click', ()=> sb.classList.toggle('is-open'));
  }
})();
