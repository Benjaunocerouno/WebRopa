/* ================================================================
   app.js — WebRopa Lógica Global
   Alcance: estado del carrito, sesión de usuario, modal de auth,
            drawer del carrito, toasts, helpers compartidos
   ================================================================ */

'use strict';

// ── Configuración de la API ──────────────────────────────────
// La URL se define en config/config.js → CONFIG.API_URL
const API_BASE = CONFIG.API_URL;

// ── Estado global de la aplicación ──────────────────────────
const AppState = {
  user: null,      // { token, clientId, name, email } | null
  cart: [],        // [{ product: {...}, quantity: Number }]
  tenantId: null,  // ID de la empresa seleccionada o null
};

// ── Helpers: localStorage ────────────────────────────────────
const Storage = {
  get: (key)      => { try { return JSON.parse(localStorage.getItem(key)); } catch { return null; } },
  set: (key, val) => localStorage.setItem(key, JSON.stringify(val)),
  del: (key)      => localStorage.removeItem(key),
};

// ── API helper ───────────────────────────────────────────────
async function apiFetch(path, options = {}) {
  const headers = { 'Content-Type': 'application/json', ...options.headers };
  if (CONFIG.HARDCODED_TOKEN) {
    headers['Authorization'] = `Bearer ${CONFIG.HARDCODED_TOKEN}`;
  } else if (AppState.user?.token) {
    headers['Authorization'] = `Bearer ${AppState.user.token}`;
  }
  const res = await fetch(`${API_BASE}${path}`, { ...options, headers });
  return res;
}


/* ================================================================
   TOASTS — Notificaciones emergentes
================================================================ */
const toastArea = document.getElementById('toast-area');

/**
 * Muestra un toast de notificación.
 * @param {string} message - Mensaje a mostrar
 * @param {'success'|'error'|'info'} type - Tipo de toast
 * @param {number} duration - Duración en ms (default: 3000)
 */
function showToast(message, type = 'info', duration = 3000) {
  if (!toastArea) return; // Si no hay área de toasts (ej. portal), no hacer nada
  const toast = document.createElement('div');
  toast.className = `toast toast--${type}`;
  toast.setAttribute('role', 'alert');

  const icons = {
    success: '✓',
    error:   '✕',
    info:    '♦',
  };

  toast.textContent = `${icons[type] || '♦'} ${message}`;
  toastArea.appendChild(toast);

  // Auto-remove
  setTimeout(() => {
    toast.classList.add('is-leaving');
    toast.addEventListener('animationend', () => toast.remove(), { once: true });
  }, duration);
}


/* ================================================================
   SESIÓN / AUTENTICACIÓN
================================================================ */

/** Carga la sesión guardada en localStorage al inicio */
function loadSession() {
  const saved = Storage.get('webropa_user');
  if (saved) {
    AppState.user = saved;
    updateUserUI(saved);
  }
}

/** Guarda la sesión y actualiza la UI */
function saveSession(userData) {
  AppState.user = userData;
  Storage.set('webropa_user', userData);
  updateUserUI(userData);
}

/** Cierra sesión */
function logout() {
  AppState.user = null;
  Storage.del('webropa_user');
  updateUserUI(null);
  showToast('Hasta pronto 👋', 'info');
}

/**
 * Actualiza la UI del navbar según el estado de sesión.
 * @param {object|null} user
 */
function updateUserUI(user) {
  const displayName   = document.getElementById('user-display-name');
  const guestView     = document.getElementById('dropdown-guest-view');
  const userView      = document.getElementById('dropdown-user-view');

  if (!displayName || !guestView || !userView) return;

  if (user) {
    const firstName = user.name?.split(' ')[0] || 'Tú';
    displayName.textContent = firstName;
    guestView.style.display = 'none';
    userView.style.display  = 'block';
  } else {
    displayName.textContent = 'Entrar';
    guestView.style.display = 'block';
    userView.style.display  = 'none';
  }

  // Update mis pedidos link dynamically
  const navMisPedidos = document.getElementById('nav-mis-pedidos');
  if (navMisPedidos) {
    navMisPedidos.href = AppState.tenantId ? `mis-pedidos.html?empresa=${AppState.tenantId}` : 'mis-pedidos.html';
  }
}


/* ================================================================
   MODAL DE AUTH (Login / Registro)
================================================================ */
const authModalOverlay = document.getElementById('auth-modal-overlay');
const authModalClose   = document.getElementById('auth-modal-close');
const tabLogin         = document.getElementById('tab-login');
const tabRegister      = document.getElementById('tab-register');
const panelLogin       = document.getElementById('panel-login');
const panelRegister    = document.getElementById('panel-register');

/** Abre el modal de auth. @param {'login'|'register'} tab */
function openAuthModal(tab = 'login') {
  authModalOverlay.hidden = false;
  document.body.style.overflow = 'hidden';
  switchAuthTab(tab);

  // Focus trap inicial
  setTimeout(() => {
    const firstInput = authModalOverlay.querySelector('input');
    if (firstInput) firstInput.focus();
  }, 100);
}

/** Cierra el modal de auth */
function closeAuthModal() {
  authModalOverlay.hidden = true;
  document.body.style.overflow = '';
  // Limpiar formularios y mensajes de estado
  document.getElementById('form-login').reset();
  document.getElementById('form-register').reset();
  clearFieldErrors(document.getElementById('form-login'));
  clearFieldErrors(document.getElementById('form-register'));
  setStatusMsg('login-status', '', '');
  setStatusMsg('register-status', '', '');
}

/** Cambia entre tabs de login y registro */
function switchAuthTab(tab) {
  const isLogin = tab === 'login';
  tabLogin.classList.toggle('auth-tab--active', isLogin);
  tabRegister.classList.toggle('auth-tab--active', !isLogin);
  tabLogin.setAttribute('aria-selected', String(isLogin));
  tabRegister.setAttribute('aria-selected', String(!isLogin));
  panelLogin.hidden    = !isLogin;
  panelRegister.hidden = isLogin;
}

// Eventos del modal
if (authModalClose) authModalClose.addEventListener('click', closeAuthModal);

if (authModalOverlay) {
  authModalOverlay.addEventListener('click', (e) => {
    if (e.target === authModalOverlay) closeAuthModal();
  });
}

document.addEventListener('keydown', (e) => {
  if (authModalOverlay && !authModalOverlay.hidden && e.key === 'Escape') closeAuthModal();
});

if (tabLogin) tabLogin.addEventListener('click', () => switchAuthTab('login'));
if (tabRegister) tabRegister.addEventListener('click', () => switchAuthTab('register'));

// Botones del navbar que abren el modal
document.getElementById('btn-open-login')?.addEventListener('click', () => {
  closeUserDropdown();
  openAuthModal('login');
});
document.getElementById('btn-open-register')?.addEventListener('click', () => {
  closeUserDropdown();
  openAuthModal('register');
});

document.getElementById('btn-go-login-after-reg')?.addEventListener('click', () => {
  switchAuthTab('login');
  document.getElementById('credentials-reveal').hidden = true;
  document.getElementById('panel-register').querySelector('.auth-form').style.display = '';
});

// Botón de logout
document.getElementById('btn-logout')?.addEventListener('click', () => {
  closeUserDropdown();
  logout();
});


/* ── Formulario de Login ─────────────────────────────────── */
const formLogin = document.getElementById('form-login');
if (formLogin) {
  formLogin.addEventListener('submit', async (e) => {
  e.preventDefault();
  const form = e.target;
  if (!validateForm(form)) return;

  const correo   = form.correo.value.trim();
  const password = form.password.value.trim();

  const submitBtn = document.getElementById('btn-login-submit');
  submitBtn.textContent = 'Verificando...';
  submitBtn.disabled = true;

  try {
    const res = await fetch(`${API_BASE}/usuarios/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ correo: correo, password: password }),
    });

    if (!res.ok) throw new Error('Credenciales incorrectas.');

    const data = await res.json();
    const token = data.token;
    const name = data.nombre;
    const email = data.correo;
    const clientId = data.id;

    saveSession({ token, clientId, name, email });
    closeAuthModal();
    showToast(`¡Bienvenida de vuelta, ${name.split(' ')[0]}! ♡`, 'success');
  } catch (err) {
    setStatusMsg('login-status', err.message, 'error');
  } finally {
    submitBtn.textContent = 'Entrar ✦';
    submitBtn.disabled = false;
  }
  });
}


/* ── Formulario de Registro ──────────────────────────────── */
const formRegister = document.getElementById('form-register');
if (formRegister) {
  formRegister.addEventListener('submit', async (e) => {
  e.preventDefault();
  const form = e.target;
  if (!validateForm(form)) return;

  const nombres   = form.nombres.value.trim();
  const correo    = form.correo.value.trim();
  const password  = form.password.value.trim();
  const telefono  = form.telefono.value.trim();

  const submitBtn = document.getElementById('btn-register-submit');
  submitBtn.textContent = 'Creando cuenta...';
  submitBtn.disabled = true;

  try {
    const payload = { nombre: nombres, correo: correo, password: password, telefono: telefono };

    const res = await fetch(`${API_BASE}/usuarios/registro`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });

    if (!res.ok) {
      const errText = await res.text();
      throw new Error(errText || 'Error al crear la cuenta.');
    }

    const data = await res.json();

    // Ocultar formulario y pedir que inicie sesión
    form.style.display = 'none';
    showToast('Cuenta creada con éxito 🎉, ahora inicia sesión.', 'success');
    setTimeout(() => {
      switchAuthTab('login');
      form.style.display = '';
    }, 2000);
  } catch (err) {
    setStatusMsg('register-status', err.message, 'error');
  } finally {
    submitBtn.textContent = 'Unirme ♡';
    submitBtn.disabled = false;
  }
  });
}


/* ── Botones de copiar credenciales ─────────────────────── */
document.querySelectorAll('.copy-btn').forEach(btn => {
  btn.addEventListener('click', () => {
    const targetId = btn.getAttribute('data-target');
    const text = document.getElementById(targetId)?.textContent || '';
    navigator.clipboard.writeText(text)
      .then(() => showToast('Copiado al portapapeles', 'info'))
      .catch(() => showToast('No se pudo copiar', 'error'));
  });
});


/* ================================================================
   USER DROPDOWN (Navbar)
================================================================ */
const userMenuTrigger = document.getElementById('user-menu-trigger');
const userDropdown    = document.getElementById('user-dropdown');

if (userMenuTrigger && userDropdown) {
  userMenuTrigger.addEventListener('click', (e) => {
    e.stopPropagation();
    const isOpen = userDropdown.classList.toggle('is-open');
    userMenuTrigger.setAttribute('aria-expanded', String(isOpen));
    userDropdown.setAttribute('aria-hidden', String(!isOpen));
  });
}

document.addEventListener('click', () => closeUserDropdown());

function closeUserDropdown() {
  if (userDropdown && userMenuTrigger) {
    userDropdown.classList.remove('is-open');
    userMenuTrigger.setAttribute('aria-expanded', 'false');
    userDropdown.setAttribute('aria-hidden', 'true');
  }
}


/* ================================================================
   CARRITO — Estado y lógica
================================================================ */
const cartDrawer        = document.getElementById('cart-drawer');
const cartDrawerOverlay = document.getElementById('cart-drawer-overlay');
const cartItemsList     = document.getElementById('cart-items-list');
const cartEmptyState    = document.getElementById('cart-empty-state');
const cartFooter        = document.getElementById('cart-footer');
const cartCountBadge    = document.getElementById('cart-count-badge');

/** Carga el carrito desde localStorage segmentado por tenant */
function loadCart() {
  const allCarts = Storage.get('webropa_carts') || {};
  if (AppState.tenantId && allCarts[AppState.tenantId]) {
    AppState.cart = allCarts[AppState.tenantId];
  } else {
    AppState.cart = [];
  }
  renderCart();
}

/** Guarda el carrito en localStorage segmentado por tenant */
function persistCart() {
  if (!AppState.tenantId) return; // No hay carrito sin tienda
  const allCarts = Storage.get('webropa_carts') || {};
  allCarts[AppState.tenantId] = AppState.cart;
  Storage.set('webropa_carts', allCarts);
}

/**
 * Agrega un producto al carrito.
 * @param {object} product - Producto a agregar
 * @param {number} qty - Cantidad (default: 1)
 */
function addToCart(product, qty = 1) {
  const existing = AppState.cart.find(item => item.product.id === product.id);
  if (existing) {
    existing.quantity += qty;
  } else {
    AppState.cart.push({ product, quantity: qty });
  }
  persistCart();
  renderCart();
  showToast(`"${product.nombre}" añadido a tu bolsita ♡`, 'success');
}

/**
 * Actualiza la cantidad de un item del carrito.
 * @param {number|string} productId
 * @param {number} delta - +1 o -1
 */
function updateCartQty(productId, delta) {
  const item = AppState.cart.find(i => i.product.id == productId);
  if (!item) return;
  item.quantity += delta;
  if (item.quantity <= 0) {
    AppState.cart = AppState.cart.filter(i => i.product.id != productId);
  }
  persistCart();
  renderCart();
}

/**
 * Elimina un item del carrito.
 * @param {number|string} productId
 */
function removeFromCart(productId) {
  AppState.cart = AppState.cart.filter(i => i.product.id != productId);
  persistCart();
  renderCart();
  showToast('Producto eliminado', 'info');
}

/** Renderiza el carrito en el drawer */
function renderCart() {
  const totalQty = AppState.cart.reduce((sum, i) => sum + i.quantity, 0);
  const subtotal = AppState.cart.reduce((sum, i) => sum + (i.product.precio * i.quantity), 0);

  // Badge del carrito
  if (cartCountBadge) {
    cartCountBadge.textContent = totalQty;
    cartCountBadge.classList.toggle('is-visible', totalQty > 0);
  }

  // Estado vacío vs lleno
  const isEmpty = AppState.cart.length === 0;
  if (cartEmptyState) cartEmptyState.style.display = isEmpty ? 'flex' : 'none';
  if (cartFooter) cartFooter.hidden = isEmpty;

  if (cartItemsList) {
    // Limpiar items anteriores (conservar empty state)
    cartItemsList.querySelectorAll('.cart-item').forEach(el => el.remove());

    if (!isEmpty) {
      // Renderizar cada item
      AppState.cart.forEach(({ product, quantity }) => {
        const itemEl = document.createElement('div');
        itemEl.className = 'cart-item';
        itemEl.innerHTML = `
          <img
            class="cart-item__img"
            src="${product.imagen_url || 'https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=200&q=80'}"
            alt="${product.nombre}"
            onerror="this.src='https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=200&q=80'"
            loading="lazy"
          />
          <div class="cart-item__info">
            <span class="cart-item__name">${product.nombre}</span>
            <span class="cart-item__price">S/. ${(product.precio * quantity).toFixed(2)}</span>
            <div class="cart-item__qty">
              <button class="qty-btn" data-action="minus" data-id="${product.id}" aria-label="Reducir cantidad">−</button>
              <span class="qty-value" aria-label="Cantidad: ${quantity}">${quantity}</span>
              <button class="qty-btn" data-action="plus" data-id="${product.id}" aria-label="Aumentar cantidad">+</button>
            </div>
          </div>
          <button class="cart-item__remove" data-id="${product.id}" aria-label="Eliminar ${product.nombre} del carrito">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" aria-hidden="true">
              <polyline points="3 6 5 6 21 6"/>
              <path d="M19 6 L18 20 C18 21 17 22 16 22 H8 C7 22 6 21 6 20 L5 6"/>
              <path d="M10 11 V17 M14 11 V17"/>
              <path d="M9 6 V4 C9 3 10 2 11 2 H13 C14 2 15 3 15 4 V6"/>
            </svg>
          </button>
        `;

        // Eventos de cantidad y eliminar
        itemEl.querySelector('[data-action="minus"]').addEventListener('click', () => updateCartQty(product.id, -1));
        itemEl.querySelector('[data-action="plus"]').addEventListener('click', () => updateCartQty(product.id, +1));
        itemEl.querySelector('.cart-item__remove').addEventListener('click', () => removeFromCart(product.id));

        cartItemsList.appendChild(itemEl);
      });
    }
  }

  // Actualizar totales
  const subtotalEl = document.getElementById('cart-subtotal');
  const totalEl = document.getElementById('cart-total');
  if (subtotalEl) subtotalEl.textContent = `S/. ${subtotal.toFixed(2)}`;
  if (totalEl) totalEl.textContent = `S/. ${subtotal.toFixed(2)}`;
}

/* Cart drawer: abrir / cerrar */
function openCartDrawer() {
  cartDrawer.classList.add('is-open');
  cartDrawerOverlay.classList.add('is-open');
  cartDrawer.setAttribute('aria-hidden', 'false');
  document.getElementById('cart-trigger-btn').setAttribute('aria-expanded', 'true');
  document.body.style.overflow = 'hidden';
}

function closeCartDrawer() {
  cartDrawer.classList.remove('is-open');
  cartDrawerOverlay.classList.remove('is-open');
  cartDrawer.setAttribute('aria-hidden', 'true');
  document.getElementById('cart-trigger-btn').setAttribute('aria-expanded', 'false');
  document.body.style.overflow = '';
}

if (document.getElementById('cart-trigger-btn')) {
  document.getElementById('cart-trigger-btn').addEventListener('click', openCartDrawer);
}
if (document.getElementById('cart-drawer-close')) {
  document.getElementById('cart-drawer-close').addEventListener('click', closeCartDrawer);
}
if (cartDrawerOverlay) {
  cartDrawerOverlay.addEventListener('click', closeCartDrawer);
}

document.addEventListener('keydown', (e) => {
  if (e.key === 'Escape' && cartDrawer.classList.contains('is-open')) closeCartDrawer();
});

/* Checkout button */
const btnCheckout = document.getElementById('btn-checkout');
if (btnCheckout) {
  btnCheckout.addEventListener('click', () => {
    if (!AppState.user) {
      closeCartDrawer();
      if (typeof authModalOverlay !== 'undefined' && authModalOverlay) {
        openAuthModal('login');
        showToast('Inicia sesión para finalizar tu compra', 'info');
      } else {
        // Fallback si no hay modal de login en esta página
        showToast('Debes iniciar sesión. Redirigiendo...', 'info');
        setTimeout(() => window.location.href = 'index.html', 1500);
      }
      return;
    }
    // Redirigir a la página de pago
    window.location.href = AppState.tenantId ? `checkout.html?empresa=${AppState.tenantId}` : 'checkout.html';
  });
}


/* ================================================================
   VALIDACIÓN DE FORMULARIOS
================================================================ */

/**
 * Valida todos los campos requeridos de un formulario.
 * @param {HTMLFormElement} form
 * @returns {boolean} isValid
 */
function validateForm(form) {
  let isValid = true;
  clearFieldErrors(form);

  form.querySelectorAll('[required]').forEach(input => {
    const val = input.value.trim();
    if (!val) {
      showFieldError(input, 'Este campo es obligatorio');
      isValid = false;
    } else if (input.type === 'email' && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(val)) {
      showFieldError(input, 'Ingresa un correo válido');
      isValid = false;
    }
  });

  return isValid;
}

function showFieldError(input, msg) {
  input.classList.add('is-invalid');
  const errEl = input.closest('.form-field')?.querySelector('.form-field__error');
  if (errEl) errEl.textContent = msg;
}

function clearFieldErrors(form) {
  form.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
  form.querySelectorAll('.form-field__error').forEach(el => { el.textContent = ''; });
}

function setStatusMsg(elId, msg, type) {
  const el = document.getElementById(elId);
  if (!el) return;
  el.textContent = msg;
  el.className = `auth-form__hint${type ? ' is-' + type : ''}`;
}


/* ================================================================
   SEARCH (Navbar)
================================================================ */
const searchInput = document.getElementById('search-input');

if (searchInput) {
  let searchTimeout;
  searchInput.addEventListener('input', () => {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
      // home.js escucha este evento para filtrar la grilla
      document.dispatchEvent(new CustomEvent('webropa:search', {
        detail: { query: searchInput.value.trim().toLowerCase() }
      }));
    }, 300);
  });
}


/* ================================================================
   QUICK ADD MODAL (Variantes)
================================================================ */
const qaOverlay = document.getElementById('quick-add-modal-overlay');
const qaCloseBtn = document.getElementById('quick-add-modal-close');
const qaCancelBtn = document.getElementById('qa-cancel-btn');
const qaSubmitBtn = document.getElementById('qa-submit-btn');

let qaCurrentProduct = null;
let qaSelectedColor = null;
let qaSelectedSize = null;

function closeQuickAddModal() {
  if(qaOverlay) qaOverlay.hidden = true;
  document.body.style.overflow = '';
}

if (qaCloseBtn) qaCloseBtn.addEventListener('click', closeQuickAddModal);
if (qaCancelBtn) qaCancelBtn.addEventListener('click', closeQuickAddModal);
if (qaOverlay) qaOverlay.addEventListener('click', (e) => {
  if (e.target === qaOverlay) closeQuickAddModal();
});

window.openQuickAddModal = function(product) {
  if (!product.variantes || product.variantes.length === 0) {
    showToast('Sin stock', 'error');
    return;
  }

  qaCurrentProduct = product;
  qaSelectedColor = null;
  qaSelectedSize = null;

  // Render product info
  document.getElementById('qa-product-name').textContent = product.nombre;
  document.getElementById('qa-product-price').textContent = `S/ ${product.precio.toFixed(2)}`;
  document.getElementById('qa-product-img').src = product.imagen_url || '';
  
  // Extraer colores únicos
  const colorMap = new Map();
  product.variantes.forEach(v => {
    if (!colorMap.has(v.color)) {
      colorMap.set(v.color, v.hex);
    }
  });

  const colorsContainer = document.getElementById('qa-color-options');
  colorsContainer.innerHTML = '';
  document.getElementById('qa-selected-color-name').textContent = 'Selecciona';
  
  document.getElementById('qa-size-options').innerHTML = '';
  document.getElementById('qa-selected-size-name').style.display = 'none';
  if (qaSubmitBtn) qaSubmitBtn.disabled = true;

  colorMap.forEach((hex, colorName) => {
    const btn = document.createElement('button');
    btn.className = 'color-btn';
    btn.style.backgroundColor = hex;
    btn.setAttribute('aria-label', `Color ${colorName}`);
    btn.addEventListener('click', () => selectQaColor(colorName));
    colorsContainer.appendChild(btn);
  });

  if(qaOverlay) {
    qaOverlay.hidden = false;
    document.body.style.overflow = 'hidden';
  }
};

function selectQaColor(colorName) {
  qaSelectedColor = colorName;
  qaSelectedSize = null;
  document.getElementById('qa-selected-color-name').textContent = colorName;

  // Highlight selected color
  const colorBtns = document.getElementById('qa-color-options').querySelectorAll('.color-btn');
  colorBtns.forEach(btn => btn.classList.remove('is-selected'));
  const selectedBtn = Array.from(colorBtns).find(b => b.getAttribute('aria-label') === `Color ${colorName}`);
  if (selectedBtn) selectedBtn.classList.add('is-selected');

  // Change image to first variant of this color
  const variantsForColor = qaCurrentProduct.variantes.filter(v => v.color === colorName);
  if (variantsForColor.length > 0 && variantsForColor[0].imagen) {
    document.getElementById('qa-product-img').src = variantsForColor[0].imagen;
  }

  // Render sizes
  const sizeContainer = document.getElementById('qa-size-options');
  sizeContainer.innerHTML = '';
  document.getElementById('qa-selected-size-name').textContent = 'Selecciona';
  document.getElementById('qa-selected-size-name').style.display = 'inline';

  variantsForColor.forEach(v => {
    const btn = document.createElement('button');
    btn.className = 'size-btn';
    btn.textContent = v.talla;
    if (v.stock <= 0) {
      btn.disabled = true;
    } else {
      btn.addEventListener('click', () => selectQaSize(v.talla, btn));
    }
    sizeContainer.appendChild(btn);
  });

  if (qaSubmitBtn) qaSubmitBtn.disabled = true;
}

function selectQaSize(sizeName, btnEl) {
  qaSelectedSize = sizeName;
  document.getElementById('qa-selected-size-name').textContent = sizeName;
  
  const sizeBtns = document.getElementById('qa-size-options').querySelectorAll('.size-btn');
  sizeBtns.forEach(btn => btn.classList.remove('is-selected'));
  btnEl.classList.add('is-selected');

  if (qaSubmitBtn) qaSubmitBtn.disabled = false;
}

if (qaSubmitBtn) {
  qaSubmitBtn.addEventListener('click', () => {
    if (!qaCurrentProduct || !qaSelectedColor || !qaSelectedSize) return;
    
    // Buscar la variante específica
    const variant = qaCurrentProduct.variantes.find(v => v.color === qaSelectedColor && v.talla === qaSelectedSize);
    
    // Clonar producto para el carrito y añadir info de variante
    const cartProduct = {
      ...qaCurrentProduct,
      id: `${qaCurrentProduct.id}-${variant.id}`, // ID único para el item en el carrito
      nombre: `${qaCurrentProduct.nombre} (${qaSelectedColor}, ${qaSelectedSize})`,
      imagen_url: variant.imagen || qaCurrentProduct.imagen_url
    };
    
    addToCart(cartProduct);
    closeQuickAddModal();
  });
}


/* ================================================================
   INICIALIZACIÓN
================================================================ */
document.addEventListener('DOMContentLoaded', () => {
  // Detectar tenant desde la URL
  const urlParams = new URLSearchParams(window.location.search);
  AppState.tenantId = urlParams.get('empresa') || null;

  // Redirigir al portal si estamos en la tienda (index.html) sin especificar empresa
  const isIndexPage = window.location.pathname === '/' || window.location.pathname.endsWith('index.html');
  if (isIndexPage && !AppState.tenantId) {
    window.location.replace('portal.html');
    return;
  }

  // Ajustar visibilidad de botones e interfaz
  const cartBtn = document.getElementById('cart-trigger-btn');
  const backPortalBtn = document.getElementById('btn-back-to-portal-container');

  if (AppState.tenantId) {
    if (cartBtn) cartBtn.style.display = '';
    if (backPortalBtn) backPortalBtn.style.display = '';
  } else {
    if (cartBtn) cartBtn.style.display = 'none';
    if (backPortalBtn) backPortalBtn.style.display = 'none';
  }

  loadSession();
  loadCart();
});
