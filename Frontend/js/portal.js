/* ================================================================
   portal.js — Lógica para la página portal.html (Directorio Central)
   ================================================================ */

'use strict';

const PortalState = {
  companies: [],
  products: [],
};

// Asignar tamaño visual masonry
function assignVisualSize(products) {
  const sizes = ['tall', 'medium', 'medium', 'short', 'medium', 'tall'];
  return products.map((p, i) => ({ ...p, size: p.size || sizes[i % sizes.length] }));
}

// Cargar y pintar directorio de empresas
async function loadCompanies() {
  const grid = document.getElementById('company-directory-grid');
  if (!grid) return;
  
  try {
    const res = await fetch(`${API_BASE}/empresas`);
    if (!res.ok) throw new Error('Error al obtener empresas');
    const empresas = await res.json();
    PortalState.companies = empresas;

    if (empresas.length === 0) {
      grid.innerHTML = '<p style="color:var(--color-text-muted);">No hay boutiques registradas.</p>';
      return;
    }

    grid.innerHTML = empresas.map(emp => `
      <a href="index.html?empresa=${emp.id}" class="portal-company-card">
        <span class="portal-company-card__logo">🛍️</span>
        <strong class="portal-company-card__name">${emp.nombre_comercial || emp.razon_social}</strong>
        <span class="portal-company-card__action">Visitar Boutique →</span>
      </a>
    `).join('');
  } catch (e) {
    console.error(e);
    grid.innerHTML = '<p style="color:red; text-align:center;">Error al cargar las boutiques. Asegúrate de que el servidor esté activo.</p>';
  }
}

// Cargar y pintar todos los productos
async function loadProducts() {
  const grid = document.getElementById('productos-masonry-grid');
  if (!grid) return;

  try {
    const res = await fetch(`${API_BASE}/productos`);
    if (!res.ok) throw new Error('Error al obtener productos');
    const products = await res.json();
    
    if (products.length === 0) {
      grid.innerHTML = '<div style="grid-column: 1/-1; text-align:center; color:var(--color-text-muted); padding:3rem;">No hay productos registrados.</div>';
      return;
    }

    PortalState.products = assignVisualSize(products);
    grid.innerHTML = ''; // Limpiar skeletons
    
    PortalState.products.forEach(product => {
      const card = document.createElement('div');
      card.className = 'product-card';
      
      const sizeClass = product.size === 'tall'  ? 'product-card--tall'
                      : product.size === 'short' ? 'product-card--short'
                      :                            'product-card--medium';
      card.classList.add(sizeClass);

      const price = parseFloat(product.precio).toFixed(2);
      const imgSrc = product.imagen_url || `https://picsum.photos/seed/${product.id}/400/500`;
      const storeId = product.empresa?.id || '';
      const storeName = product.empresa ? (product.empresa.nombre_comercial || product.empresa.razon_social) : 'Boutique';

      card.innerHTML = `
        <div class="product-card__img-wrap">
          <img
            class="product-card__img"
            src="${imgSrc}"
            alt="${product.nombre}"
            loading="lazy"
            onerror="this.src='https://picsum.photos/seed/${product.id + 100}/400/500'"
          />
        </div>
        <div class="product-card__info">
          <span style="font-size:0.75rem; color:var(--color-text-muted); text-transform:uppercase; font-weight:600; display:block; margin-bottom:0.25rem;">${storeName}</span>
          <h3 class="product-card__name">${product.nombre}</h3>
          <p class="product-card__price">S/. ${price}</p>
          <button
            class="product-card__add-btn product-card__add-btn--portal"
            style="background: var(--color-bg-secondary); border: 1px solid var(--color-border); color: var(--color-text); cursor: pointer; opacity: 1; transform: translateY(0);"
          >
            Ver Boutique →
          </button>
        </div>
      `;

      // Evento: redirigir a la tienda al hacer clic en el botón de ver tienda
      card.querySelector('.product-card__add-btn').addEventListener('click', (e) => {
        e.stopPropagation();
        if (storeId) {
          window.location.href = `index.html?empresa=${storeId}`;
        }
      });

      // Evento: click en la tarjeta redirige al storefront
      card.addEventListener('click', () => {
        if (storeId) {
          window.location.href = `index.html?empresa=${storeId}`;
        }
      });

      grid.appendChild(card);
    });
  } catch (e) {
    console.error(e);
    grid.innerHTML = '<div style="grid-column: 1/-1; text-align:center; color:red; padding:3rem;">Error al cargar el catálogo de productos.</div>';
  }
}

// Inicialización
document.addEventListener('DOMContentLoaded', () => {
  loadCompanies();
  loadProducts();
});
