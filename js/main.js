(function () {
    // Aplica el tema guardado cuanto antes para evitar parpadeos.
    try {
        var saved = localStorage.getItem('peseta-theme');
        if (saved === 'light' || saved === 'dark') {
            document.documentElement.setAttribute('data-theme', saved);
        }
    } catch (e) { /* localStorage no disponible: se usa el tema del sistema */ }
})();

document.addEventListener('DOMContentLoaded', function () {
    var root = document.documentElement;
    var toggle = document.getElementById('theme-toggle');

    function currentTheme() {
        var attr = root.getAttribute('data-theme');
        if (attr === 'light' || attr === 'dark') return attr;
        return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    }

    if (toggle) {
        toggle.addEventListener('click', function () {
            var next = currentTheme() === 'dark' ? 'light' : 'dark';
            root.setAttribute('data-theme', next);
            try { localStorage.setItem('peseta-theme', next); } catch (e) { /* no-op */ }
        });
    }

    // Revelado suave al entrar en viewport.
    var items = document.querySelectorAll('.reveal');
    if (!('IntersectionObserver' in window)) {
        items.forEach(function (el) { el.classList.add('in'); });
        return;
    }
    var observer = new IntersectionObserver(function (entries) {
        entries.forEach(function (entry) {
            if (entry.isIntersecting) {
                entry.target.classList.add('in');
                observer.unobserve(entry.target);
            }
        });
    }, { threshold: 0.14, rootMargin: '0px 0px -8% 0px' });
    items.forEach(function (el) { observer.observe(el); });
});
