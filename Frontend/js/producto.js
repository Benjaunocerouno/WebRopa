/* ================================================================
   producto.js — Lógica de la página de detalle de producto (PDP)
================================================================ */

'use strict';

let pdpProduct = null;
let pdpSelectedColor = null;
let pdpSelectedSize = null;
let pdpSelectedVariant = null;
let pdpQuantity = 1;

document.addEventListener('DOMContentLoaded', async () => {
  const urlParams = new URLSearchParams(window.location.search);
  const productId = parseInt(urlParams.get('id'));

  if (!productId) {
    window.location.href = 'index.html';
    return;
  }

  try {
    const res = await fetch(`${CONFIG.API_URL}/productos/${productId}`);
    if (!res.ok) throw new Error('Producto no encontrado');
    
    pdpProduct = await res.json();
    
    if (!pdpProduct) {
      document.getElementById('pdp-name').textContent = 'Producto no encontrado';
      return;
    }

    renderProductDetails();
    
    const resAll = await fetch(`${CONFIG.API_URL}/productos`);
    if (resAll.ok) {
      const allProducts = await resAll.json();
      renderSimilarProducts(allProducts);
    }
  } catch (error) {
    console.error(error);
    document.getElementById('pdp-name').textContent = 'Error cargando el producto';
  }
});

function renderProductDetails() {
  document.getElementById('pdp-name-crumb').textContent = pdpProduct.nombre;
  document.getElementById('pdp-category-link').textContent = pdpProduct.categoria?.nombre || 'Catálogo';
  
  document.getElementById('pdp-main-image').src = pdpProduct.imagen_url;
  document.getElementById('pdp-name').textContent = pdpProduct.nombre;
  document.getElementById('pdp-price').textContent = `S/ ${pdpProduct.precio.toFixed(2)}`;
  document.getElementById('pdp-description').textContent = pdpProduct.descripcion || 'Sin descripción detallada.';

  // Variantes
  if (!pdpProduct.variantes || pdpProduct.variantes.length === 0) {
    document.getElementById('pdp-variants-container').style.display = 'none';
    pdpSelectedVariant = { id: 'default', stock: 100 }; // Fake variant so logic works
    updateStockInfo(pdpSelectedVariant.stock);
    updateQuantityControls();
    return;
  }

  const colorMap = new Map();
  pdpProduct.variantes.forEach(v => {
    if (!colorMap.has(v.color)) {
      colorMap.set(v.color, v.hex);
    }
  });

  const colorsContainer = document.getElementById('pdp-color-options');
  colorsContainer.innerHTML = '';
  document.getElementById('pdp-selected-color').textContent = 'Selecciona';
  document.getElementById('pdp-size-options').innerHTML = '';
  document.getElementById('pdp-selected-size').style.display = 'none';

  colorMap.forEach((hex, colorName) => {
    const btn = document.createElement('button');
    btn.className = 'color-btn';
    btn.style.backgroundColor = hex;
    btn.setAttribute('aria-label', `Color ${colorName}`);
    btn.addEventListener('click', () => selectColor(colorName));
    colorsContainer.appendChild(btn);
  });

  updateStockInfo(null);
}

function selectColor(colorName) {
  pdpSelectedColor = colorName;
  pdpSelectedSize = null;
  pdpSelectedVariant = null;
  document.getElementById('pdp-selected-color').textContent = colorName;

  const colorBtns = document.getElementById('pdp-color-options').querySelectorAll('.color-btn');
  colorBtns.forEach(btn => btn.classList.remove('is-selected'));
  const selectedBtn = Array.from(colorBtns).find(b => b.getAttribute('aria-label') === `Color ${colorName}`);
  if (selectedBtn) selectedBtn.classList.add('is-selected');

  const variantsForColor = pdpProduct.variantes.filter(v => v.color === colorName);
  if (variantsForColor.length > 0 && variantsForColor[0].imagen) {
    document.getElementById('pdp-main-image').src = variantsForColor[0].imagen;
  }

  const sizeContainer = document.getElementById('pdp-size-options');
  sizeContainer.innerHTML = '';
  document.getElementById('pdp-selected-size').textContent = 'Selecciona';
  document.getElementById('pdp-selected-size').style.display = 'inline';

  variantsForColor.forEach(v => {
    const btn = document.createElement('button');
    btn.className = 'size-btn';
    btn.textContent = v.talla;
    if (v.stock <= 0) {
      btn.disabled = true;
    } else {
      btn.addEventListener('click', () => selectSize(v.talla, btn));
    }
    sizeContainer.appendChild(btn);
  });

  updateQuantityControls();
  updateStockInfo(null);
}

function selectSize(sizeName, btnEl) {
  pdpSelectedSize = sizeName;
  document.getElementById('pdp-selected-size').textContent = sizeName;
  
  const sizeBtns = document.getElementById('pdp-size-options').querySelectorAll('.size-btn');
  sizeBtns.forEach(btn => btn.classList.remove('is-selected'));
  btnEl.classList.add('is-selected');

  pdpSelectedVariant = pdpProduct.variantes.find(v => v.color === pdpSelectedColor && v.talla === pdpSelectedSize);
  
  pdpQuantity = 1;
  updateQuantityControls();
  updateStockInfo(pdpSelectedVariant.stock);
}

function updateStockInfo(stock) {
  const stockEl = document.getElementById('pdp-stock-text');
  const addBtn = document.getElementById('pdp-add-to-cart');

  stockEl.classList.remove('is-in-stock', 'is-low-stock', 'is-out-of-stock');

  if (stock === null) {
    stockEl.textContent = 'Selecciona tus opciones';
    addBtn.disabled = true;
  } else if (stock === 0) {
    stockEl.textContent = 'Sin stock';
    stockEl.classList.add('is-out-of-stock');
    addBtn.disabled = true;
  } else if (stock < 10) {
    stockEl.textContent = `¡Solo quedan ${stock} unidades!`;
    stockEl.classList.add('is-low-stock');
    addBtn.disabled = false;
  } else {
    stockEl.textContent = `En stock (+10)`;
    stockEl.classList.add('is-in-stock');
    addBtn.disabled = false;
  }
}

function updateQuantityControls() {
  const input = document.getElementById('pdp-qty-input');
  const btnMinus = document.getElementById('pdp-qty-minus');
  const btnPlus = document.getElementById('pdp-qty-plus');

  input.value = pdpQuantity;

  if (!pdpSelectedVariant || pdpSelectedVariant.stock === 0) {
    btnMinus.disabled = true;
    btnPlus.disabled = true;
  } else {
    btnMinus.disabled = pdpQuantity <= 1;
    btnPlus.disabled = pdpQuantity >= pdpSelectedVariant.stock;
  }
}

// Event Listeners for Qty
document.getElementById('pdp-qty-minus').addEventListener('click', () => {
  if (pdpQuantity > 1) {
    pdpQuantity--;
    updateQuantityControls();
  }
});

document.getElementById('pdp-qty-plus').addEventListener('click', () => {
  if (pdpSelectedVariant && pdpQuantity < pdpSelectedVariant.stock) {
    pdpQuantity++;
    updateQuantityControls();
  }
});

// Event Listener for Add to Cart
document.getElementById('pdp-add-to-cart').addEventListener('click', () => {
  if (!pdpProduct || !pdpSelectedVariant) return;

  const isDefault = pdpSelectedVariant.id === 'default';

  const cartProduct = {
    ...pdpProduct,
    id: isDefault ? pdpProduct.id : `${pdpProduct.id}-${pdpSelectedVariant.id}`,
    nombre: isDefault ? pdpProduct.nombre : `${pdpProduct.nombre} (${pdpSelectedColor}, ${pdpSelectedSize})`,
    imagen_url: pdpSelectedVariant.imagen || pdpProduct.imagen_url
  };
  
  addToCart(cartProduct, pdpQuantity);
  openCartDrawer(); // Open drawer directly since we're in PDP
});

/* ================================================================
   CARRUSEL DE PRODUCTOS SIMILARES
================================================================ */
function renderSimilarProducts(allProducts) {
  const track = document.getElementById('similar-products-track');
  
  if (!allProducts) return;

  // Usar los productos reales excluyendo el actual
  const similars = allProducts.filter(p => p.id !== pdpProduct?.id).slice(0, 10);

  // Aplicar un diseño más limpio para la sección de similares
  similars.forEach(product => {
    const card = document.createElement('div');
    card.className = 'product-card';
    card.style.cursor = 'pointer';
    
    const imgSrc = product.imagen_url;
    const price  = parseFloat(product.precio).toFixed(2);
    
    card.innerHTML = `
      <div class="product-card__img-wrap" style="aspect-ratio: 3/4; overflow: hidden; border-radius: var(--radius-md);">
        <img class="product-card__img" src="${imgSrc}" alt="${product.nombre}" loading="lazy" style="width: 100%; height: 100%; object-fit: cover; transition: transform 0.3s ease;" onmouseover="this.style.transform='scale(1.05)'" onmouseout="this.style.transform='scale(1)'" />
      </div>
      <div class="product-card__info" style="padding: 1rem 0; text-align: center;">
        <h3 class="product-card__name" style="font-size: 1.1rem; font-weight: 500; margin-bottom: 0.3rem;">${product.nombre}</h3>
        <p class="product-card__price" style="font-size: 1.1rem; color: var(--color-brown); font-weight: 600;">S/. ${price}</p>
      </div>
    `;

    card.addEventListener('click', () => {
      window.location.href = `producto.html?id=${product.id}`;
    });
    
    track.appendChild(card);
  });

  // Carrusel Nav
  const btnPrev = document.getElementById('carousel-prev');
  const btnNext = document.getElementById('carousel-next');

  btnPrev.addEventListener('click', () => {
    track.scrollBy({ left: -300, behavior: 'smooth' });
  });

  btnNext.addEventListener('click', () => {
    track.scrollBy({ left: 300, behavior: 'smooth' });
  });
}
