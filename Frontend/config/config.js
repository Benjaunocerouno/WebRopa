// ============================
// CONFIGURACIÓN DEL FRONTEND
// ============================


const CONFIG = {

    API_URL: 'http://shop.spring.informaticapp.com:2100/api',

    HARDCODED_TOKEN: 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlYzRhNTVjZTU2MjRkNzhjOWNlMTFlOWY2NDQ0YzNlMjJhNDZiMmIzMzA3ZDliNjU0ZjczZDUzMGYzYmU0ODkwIiwicGVybWlzb3MiOltdLCJyb2wiOiJBUEkiLCJpYXQiOjE3ODI0OTIxODMsImV4cCI6NDkzNjA5MjE4M30.G4PerWY4M51Jxpvl14MtEurPj_pOvHQjElVQ4nDm1MM',

    // Claves de localStorage
    TOKEN_KEY: 'webropa_user',
    TENANT_SESSION_KEY: 'tenant_session',
};

// ============================
// FUNCIONES GLOBALES COMPARTIDAS
// ============================

// Búsqueda genérica para todas las tablas
document.addEventListener('input', function (e) {
    if (e.target.matches('.buscador-generico')) {
        const targetId = e.target.dataset.target;
        if (!targetId) return;

        const tabla = document.getElementById(targetId);
        if (!tabla) return;

        let filas;
        // Buscar el tbody o directamente los tr dentro de la tabla
        const tbody = tabla.tagName === 'TBODY' ? tabla : tabla.querySelector('tbody') || tabla;
        filas = tbody.querySelectorAll('tr');

        // Función para normalizar texto (quitar acentos)
        const normalizar = (texto) => {
            return texto.normalize("NFD").replace(/[\u0300-\u036f]/g, "").toLowerCase();
        };

        const termino = normalizar(e.target.value);

        filas.forEach(fila => {
            // Ignorar filas de carga o vacías (suelen tener un td con colspan)
            if (fila.cells.length === 1 && fila.cells[0].colSpan > 1) return;
            // Si tiene clase 'no-filtrable', ignorarla
            if (fila.classList.contains('no-filtrable')) return;

            const textoFila = normalizar(fila.textContent);
            fila.style.display = textoFila.includes(termino) ? '' : 'none';
        });
    }
});
