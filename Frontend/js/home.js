/* ================================================================
   home.js — WebRopa Lógica específica del Home
   Alcance: renderización de productos (masonry), filtros,
            favoritos, integración con la API de productos
   ================================================================ */

'use strict';

// ── Datos de ejemplo para cuando la API no esté disponible ──
const PRODUCTOS_DEMO = [];

// ── Estado local del home ────────────────────────────────────
const HomeState = {
  products: [],          // todos los productos cargados
  filtered: [],          // productos filtrados por tab / búsqueda
  favorites: new Set(),  // ids marcados como favoritos
  activeFilter: 'all',   // tab activo
  searchQuery: '',       // búsqueda actual
};

// ── Referencias al DOM ───────────────────────────────────────
const masonryGrid = document.getElementById('productos-masonry-grid');


/* ================================================================
   RENDER DE PRODUCTOS
================================================================ */

/**
 * Construye el HTML de una tarjeta de producto y la retorna.
 * @param {object} product
 * @returns {HTMLElement}
 */
function buildProductCard(product) {
  const card = document.createElement('div');
  card.className = 'product-card';
  card.setAttribute('data-id', product.id);
  card.setAttribute('data-badge', product.badge || '');

  // Tamaño visual de la tarjeta
  card.classList.add('product-card--medium');

  // Badge de sticker
  const badgeLabels = { nuevo: 'Nuevo ✦', oferta: '🔥 Oferta', favorito: '♡ Favorito' };
  const badgeHTML = product.badge
    ? `<span class="product-badge product-badge--${product.badge}" aria-label="${badgeLabels[product.badge]}">${badgeLabels[product.badge]}</span>`
    : '';

  // Es favorito?
  const isFav = HomeState.favorites.has(product.id);
  const favClass = isFav ? ' is-liked' : '';

  const imgSrc = product.imagen_url || `https://picsum.photos/seed/${product.id}/400/500`;
  const price  = parseFloat(product.precio).toFixed(2);

  // ¿Tiene stock/variantes?
  const hasStock = product.variantes && product.variantes.length > 0;
  const outOfStockHTML = !hasStock ? `<div class="product-card__out-of-stock">Agotado</div>` : '';

  card.innerHTML = `
    <div class="product-card__img-wrap">
      ${badgeHTML}
      ${outOfStockHTML}
      <button
        class="product-card__fav${favClass}"
        data-id="${product.id}"
        aria-label="${isFav ? 'Quitar de favoritos' : 'Agregar a favoritos'}"
        aria-pressed="${isFav}"
      >
        <svg viewBox="0 0 24 24" fill="${isFav ? 'currentColor' : 'none'}" stroke="currentColor" stroke-width="1.5" aria-hidden="true">
          <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
        </svg>
      </button>
      <img
        class="product-card__img"
        src="${imgSrc}"
        alt="${product.nombre}"
        loading="lazy"
        onerror="this.src='https://picsum.photos/seed/${product.id + 100}/400/500'"
      />
    </div>
    <div class="product-card__info">
      <h3 class="product-card__name">${product.nombre}</h3>
      <p class="product-card__price">S/. ${price}</p>
      <button
        class="product-card__add-btn"
        data-id="${product.id}"
        aria-label="Agregar ${product.nombre} al carrito"
      >
        + Agregar al carrito
      </button>
    </div>
  `;

  // Evento: favorito
  card.querySelector('.product-card__fav').addEventListener('click', (e) => {
    e.stopPropagation();
    toggleFavorite(product.id, card);
  });

  // Evento: agregar al carrito (ahora abre el modal de variantes)
  card.querySelector('.product-card__add-btn').addEventListener('click', (e) => {
    e.stopPropagation();
    if (typeof openQuickAddModal === 'function') {
      openQuickAddModal(product);
    } else {
      // Fallback por si no existe
      addToCart(product);
    }
  });

  // Evento: click en la tarjeta para ir al detalle
  card.addEventListener('click', () => {
    window.location.href = `producto.html?id=${product.id}&empresa=${AppState.tenantId}`;
  });

  return card;
}

/**
 * Renderiza la grilla masonry con los productos filtrados.
 */
function renderProducts() {
  if (!masonryGrid) return;
  // Limpiar grilla (quitar skeletons y tarjetas anteriores)
  masonryGrid.innerHTML = '';

  if (HomeState.filtered.length === 0) {
    masonryGrid.innerHTML = `
      <div style="grid-column: 1/-1; text-align:center; padding: 4rem 1rem; color: var(--color-text-muted);">
        <p style="font-family: var(--font-serif); font-size:1.4rem; margin-bottom:0.5rem;">No encontramos nada 🌿</p>
        <p>Prueba con otra búsqueda o categoría.</p>
      </div>
    `;
    return;
  }

  HomeState.filtered.forEach(product => {
    masonryGrid.appendChild(buildProductCard(product));
  });
}

/**
 * Aplica los filtros activos (tab + búsqueda) y re-renderiza.
 */
function applyFilters() {
  let result = [...HomeState.products];

  // Filtro por tab
  if (HomeState.activeFilter !== 'all') {
    result = result.filter(p => p.badge === HomeState.activeFilter);
  }

  // Filtro por búsqueda
  if (HomeState.searchQuery) {
    result = result.filter(p =>
      p.nombre.toLowerCase().includes(HomeState.searchQuery) ||
      (p.descripcion && p.descripcion.toLowerCase().includes(HomeState.searchQuery)) ||
      (p.categoria?.nombre && p.categoria.nombre.toLowerCase().includes(HomeState.searchQuery))
    );
  }

  HomeState.filtered = result;
  renderProducts();
}


/* ================================================================
   FAVORITOS
================================================================ */

/** Carga los ids favoritos desde localStorage */
function loadFavorites() {
  const saved = JSON.parse(localStorage.getItem('webropa_favorites') || '[]');
  HomeState.favorites = new Set(saved);
}

/** Guarda los favoritos en localStorage */
function persistFavorites() {
  localStorage.setItem('webropa_favorites', JSON.stringify([...HomeState.favorites]));
}

/**
 * Alterna el estado favorito de un producto.
 * @param {number} productId
 * @param {HTMLElement} card - Tarjeta de producto a actualizar visualmente
 */
function toggleFavorite(productId, card) {
  const btn = card.querySelector('.product-card__fav');
  const svg = btn.querySelector('path');

  if (HomeState.favorites.has(productId)) {
    HomeState.favorites.delete(productId);
    btn.classList.remove('is-liked');
    btn.setAttribute('aria-pressed', 'false');
    btn.setAttribute('aria-label', 'Agregar a favoritos');
    svg.setAttribute('fill', 'none');
    showToast('Eliminado de favoritos', 'info');
  } else {
    HomeState.favorites.add(productId);
    btn.classList.add('is-liked');
    btn.setAttribute('aria-pressed', 'true');
    btn.setAttribute('aria-label', 'Quitar de favoritos');
    svg.setAttribute('fill', 'currentColor');
    showToast('Guardado en tus favoritos ♡', 'success');
  }

  persistFavorites();
}


/* ================================================================
   FILTER TABS
================================================================ */
document.querySelectorAll('.filter-tab').forEach(tab => {
  tab.addEventListener('click', () => {
    // Actualizar estado visual de tabs
    document.querySelectorAll('.filter-tab').forEach(t => {
      t.classList.remove('filter-tab--active');
      t.setAttribute('aria-selected', 'false');
    });
    tab.classList.add('filter-tab--active');
    tab.setAttribute('aria-selected', 'true');

    // Aplicar filtro
    HomeState.activeFilter = tab.getAttribute('data-filter');
    applyFilters();
  });
});


/* ================================================================
   BÚSQUEDA (escucha evento de app.js)
================================================================ */
document.addEventListener('webropa:search', (e) => {
  HomeState.searchQuery = e.detail.query;
  applyFilters();
});


/* ================================================================
   CARGA DE PRODUCTOS desde la API
================================================================ */

// ── Carga de Productos desde la API ──

async function loadProducts() {
  if (typeof AppState === 'undefined' || !AppState.tenantId) {
    return; // Redirección ya manejada por app.js
  }

  // Obtener detalles de la empresa para personalizar la tienda
  try {
    const res = await fetch(`${CONFIG.API_URL}/empresas/${AppState.tenantId}`);
    if (res.ok) {
      const emp = await res.json();
      const storeName = emp.nombre_comercial || emp.razon_social;
      
      // Actualizar títulos
      const heroTitle = document.querySelector('.hero__headline');
      if (heroTitle) {
        heroTitle.innerHTML = `Bienvenida a<br/><em>${storeName}</em>`;
      }
      const storeTitle = document.querySelector('.section-title');
      if (storeTitle) {
        storeTitle.textContent = `Colección de ${storeName} ♡`;
      }
    }
  } catch (e) {
    console.warn("No se pudo cargar el nombre de la empresa", e);
  }

  try {
    const headers = {};
    if (typeof AppState !== 'undefined' && AppState.user?.token) {
      headers['Authorization'] = `Bearer ${AppState.user.token}`;
    }

    const endpoint = `${CONFIG.API_URL}/productos/empresa/${AppState.tenantId}`;
    const res = await fetch(endpoint, { headers });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const data = await res.json();
    const productosReales = Array.isArray(data) ? data : [];

    const withBadge = productosReales.map((p, i) => ({
      ...p,
      badge: i < 2 ? 'nuevo' : i < 4 ? 'favorito' : null,
      variantes: p.variantes || []
    }));

    HomeState.products = withBadge;
  } catch (err) {
    console.warn('[WebRopa] Error al cargar productos:', err.message);
    HomeState.products = [];
  }

  HomeState.filtered = [...HomeState.products];
  renderProducts();
}

/**
 * Carga las empresas en el selector del navbar (oculto en la cabecera)
 */
async function loadEmpresasSelector() {
  const select = document.getElementById('tenant-select');
  if (!select) return;
  try {
    const res = await fetch(`${CONFIG.API_URL}/empresas`);
    if (res.ok) {
      const empresas = await res.json();
      empresas.forEach(emp => {
        const opt = document.createElement('option');
        opt.value = emp.id;
        opt.textContent = emp.nombre_comercial || emp.razon_social;
        select.appendChild(opt);
      });
    }
  } catch(e) {
    console.warn("No se pudieron cargar las empresas para el selector", e);
  }
}


/* ================================================================
   INICIALIZACIÓN DEL HOME
================================================================ */
document.addEventListener('DOMContentLoaded', () => {
  loadFavorites();
  loadEmpresasSelector();
  loadProducts();

  // Smooth scroll al CTA del hero
  document.querySelector('.hero__cta-btn')?.addEventListener('click', (e) => {
    e.preventDefault();
    document.getElementById('productos-destacados')?.scrollIntoView({ behavior: 'smooth' });
  });
});
