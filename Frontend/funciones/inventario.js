/* ==========================================================
   CONFIGURACIÓN Y UTILIDADES
   ========================================================== */
const API_URL = CONFIG.API_URL;
const SESSION_KEY = 'tenant_session';

const COLOR_MAP = {
    'negro': '#000000',
    'black': '#000000',
    'blanco': '#ffffff',
    'white': '#ffffff',
    'rojo': '#ff0000',
    'red': '#ff0000',
    'verde': '#00ff00',
    'green': '#00ff00',
    'azul': '#0000ff',
    'blue': '#0000ff',
    'amarillo': '#ffff00',
    'yellow': '#ffff00',
    'gris': '#808080',
    'gray': '#808080',
    'grey': '#808080',
    'rosa': '#ffc0cb',
    'pink': '#ffc0cb',
    'marrón': '#8b4513',
    'marron': '#8b4513',
    'brown': '#8b4513',
    'naranja': '#ffa500',
    'orange': '#ffa500',
    'morado': '#800080',
    'purple': '#800080',
    'violeta': '#ee82ee',
    'violet': '#ee82ee',
    'celeste': '#87ceeb',
    'sky blue': '#87ceeb',
    'beige': '#f5f5dc',
    'crema': '#fffdd0',
    'cream': '#fffdd0',
    'dorado': '#ffd700',
    'gold': '#ffd700',
    'plateado': '#c0c0c0',
    'silver': '#c0c0c0',
    'turquesa': '#40e0d0',
    'turquoise': '#40e0d0',
    'burdeos': '#800020',
    'burgundy': '#800020',
    'mostaza': '#e1ad01',
    'mustard': '#e1ad01',
    'marfil': '#fffff0',
    'ivory': '#fffff0',
    'azul marino': '#000080',
    'navy': '#000080',
    'coral': '#ff7f50',
    'fucsia': '#ff00ff',
    'fuchsia': '#ff00ff',
    'lila': '#c8a2c8',
    'lilac': '#c8a2c8',
    'salmon': '#fa8072',
    'salmón': '#fa8072',
    'caqui': '#f0e68c',
    'khaki': '#f0e68c',
    'oliva': '#808000',
    'olive': '#808000'
};

let toastTimeout;
function showToast(msg, error = false) {
    const t = document.getElementById('toast');
    t.textContent = msg;
    t.style.backgroundColor = error ? 'var(--alerta)' : 'var(--cafe-oscuro)';
    t.classList.add('visible');
    clearTimeout(toastTimeout);
    toastTimeout = setTimeout(() => t.classList.remove('visible'), 3000);
}

function getToken() {
    try { return JSON.parse(localStorage.getItem(SESSION_KEY)).token; } catch { return null; }
}
function getSessionData() {
    try { return JSON.parse(localStorage.getItem(SESSION_KEY)); } catch { return null; }
}

function genSKU() {
    const letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    const num = Math.floor(Math.random() * 900) + 100;
    const l1 = letters.charAt(Math.floor(Math.random() * 26));
    const l2 = letters.charAt(Math.floor(Math.random() * 26));
    return `VAR-${num}-${l1}${l2}`;
}

async function fetchAuth(endpoint, method = 'GET', body = null) {
    const t = getToken();
    if (!t) {
        document.getElementById('login-overlay').classList.remove('oculto');
        throw new Error("No autenticado");
    }
    const opts = { method, headers: { 'Authorization': 'Bearer ' + t, 'Content-Type': 'application/json' }, cache: 'no-store' };
    if (body) opts.body = JSON.stringify(body);

    const res = await fetch(API_URL + endpoint, opts);
    if (!res.ok) {
        let errText = 'Error en petición';
        try {
            const rawText = await res.text();
            try { const eJson = JSON.parse(rawText); errText = eJson.message || eJson.error || rawText || errText; }
            catch { errText = rawText || errText; }
        } catch {}
        throw new Error(errText);
    }
    if (method === 'GET' || res.headers.get('content-type')?.includes('json')) return await res.json();
    return await res.text();
}

/* ==========================================================
   NAVEGACIÓN Y AUTH
   ========================================================== */
document.getElementById('menu-navegacion').addEventListener('click', (e) => {
    const btn = e.target.closest('.menu-item');
    if (!btn) return;
    document.querySelectorAll('.menu-item').forEach(b => b.classList.remove('activo'));
    btn.classList.add('activo');
    document.querySelectorAll('.panel').forEach(p => p.classList.remove('activo'));
    document.getElementById('panel-' + btn.dataset.panel).classList.add('activo');

    if (btn.dataset.panel === 'productos') cargarProductos();
    if (btn.dataset.panel === 'categorias') cargarCategorias();
    if (btn.dataset.panel === 'pedidos') cargarPedidos();
    if (btn.dataset.panel === 'cupones') cargarCupones();
    if (btn.dataset.panel === 'proveedores') cargarProveedores();
    if (btn.dataset.panel === 'alertas') cargarAlertas();
});

async function aplicarUsuario(datos) {
    const nombre = datos.nombre || 'Usuario';
    document.getElementById('usuario-nombre').textContent = nombre;
    document.getElementById('usuario-rol').textContent = datos.rol || '--';
    document.getElementById('avatar-iniciales').textContent = nombre.substring(0, 2).toUpperCase();
    document.getElementById('sidebar-empresa-id').textContent = datos.empresa_id || 'Global';
    document.getElementById('footer-usuario').textContent = datos.correo || '';

    if (datos.empresa_id) {
        try {
            const emp = await fetchAuth(`/empresas/${datos.empresa_id}`);
            const empNombre = emp.nombre_comercial || emp.razon_social || `Empresa #${datos.empresa_id}`;
            document.getElementById('sidebar-empresa-nombre').textContent = empNombre;
            const dashName = document.getElementById('dash-empresa-nombre');
            if (dashName) dashName.textContent = empNombre;
        } catch (e) {
            console.error("Error al obtener la empresa:", e);
            document.getElementById('sidebar-empresa-nombre').textContent = "Mantenimiento Admin";
        }
    } else {
        document.getElementById('sidebar-empresa-nombre').textContent = "Global (SuperAdmin)";
        const dashName = document.getElementById('dash-empresa-nombre');
        if (dashName) dashName.textContent = "Catálogo Global";
    }
}

document.getElementById('btn-ingresar').addEventListener('click', async () => {
    const correo = document.getElementById('login-correo').value;
    const password = document.getElementById('login-password').value;
    const errEl = document.getElementById('login-error');

    try {
        const res = await fetch(API_URL + '/usuarios/login', {
            method: 'POST', headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ correo, password })
        });
        if (!res.ok) throw new Error('Credenciales inválidas o sin acceso al panel');
        const data = await res.json();

        if (data.rol !== 'ADMIN') {
            throw new Error('Acceso denegado: Solo usuarios con el rol ADMIN pueden ingresar a este panel.');
        }

        localStorage.setItem(SESSION_KEY, JSON.stringify(data));
        document.getElementById('login-overlay').classList.add('oculto');
        await aplicarUsuario(data);
        showToast(`Bienvenido ${data.nombre}`);
        // Cargar dashboard defaults
        cargarCategoriasParaSelect();
        cargarProveedoresParaSelect();
        cargarProductos();
    } catch (e) {
        errEl.textContent = e.message;
        errEl.classList.add('visible');
    }
});

document.getElementById('btn-cerrar-sesion').addEventListener('click', async () => {
    const s = getSessionData();
    if (s && s.token) {
        try {
            await fetch(CONFIG.API_URL + '/usuarios/logout', {
                method: 'POST',
                headers: { 'Authorization': 'Bearer ' + s.token }
            });
        } catch (e) { console.error("Error logging out", e); }
    }
    localStorage.removeItem(SESSION_KEY);
    document.getElementById('login-overlay').classList.remove('oculto');

    // Limpiar campos del login
    document.getElementById('login-correo').value = '';
    document.getElementById('login-password').value = '';
    document.getElementById('login-error').classList.remove('visible');
});

// Check inicial
(async () => {
    const s = getSessionData();
    if (s && s.token) {
        if (s.rol !== 'ADMIN') {
            localStorage.removeItem(SESSION_KEY);
            document.getElementById('login-overlay').classList.remove('oculto');
            const errEl = document.getElementById('login-error');
            errEl.textContent = 'Acceso denegado: Tu rol no es ADMIN.';
            errEl.classList.add('visible');
        } else {
            document.getElementById('login-overlay').classList.add('oculto');
            await aplicarUsuario(s);
            cargarCategoriasParaSelect();
            cargarProveedoresParaSelect();
            cargarProductos();
        }
    }
})();

/* ==========================================================
   CATEGORÍAS
   ========================================================== */
async function cargarCategorias() {
    const tbody = document.getElementById('tabla-categorias');
    try {
        const cats = await fetchAuth('/categorias');
        tbody.innerHTML = cats.map(c => `
      <tr>
        <td>${c.id}</td>
        <td><strong>${c.nombre}</strong></td>
        <td>${c.empresa ? c.empresa.id : 'Global'}</td>
        <td>
          <button class="btn btn-peligro btn-mini" onclick="eliminarCategoria(${c.id}, '${c.nombre}')">Eliminar</button>
        </td>
      </tr>
    `).join('');
        if (cats.length === 0) tbody.innerHTML = '<tr><td colspan="3" style="text-align:center; padding: 20px;">No hay categorías creadas.</td></tr>';
    } catch (e) { tbody.innerHTML = `<tr><td colspan="3" style="color:var(--alerta)">Error: ${e.message}</td></tr>`; }
}

async function cargarCategoriasParaSelect() {
    const sel = document.getElementById('prod-cat');
    try {
        const cats = await fetchAuth('/categorias');
        sel.innerHTML = '<option value="">-- Selecciona Categoría --</option>' + cats.map(c => `<option value="${c.id}">${c.nombre}</option>`).join('');
    } catch (e) {
        sel.innerHTML = '<option value="">Error al cargar categorías</option>';
    }
}

async function cargarProveedoresParaSelect() {
    const sel = document.getElementById('prod-prov');
    if (!sel) return;
    try {
        const provs = await fetchAuth('/proveedores');
        sel.innerHTML = '<option value="">-- Selecciona Proveedor --</option>' + provs.map(p => `<option value="${p.id}">${p.razonSocial} (${p.ruc})</option>`).join('');
    } catch (e) {
        sel.innerHTML = '<option value="">Error al cargar proveedores</option>';
    }
}

document.getElementById('btn-guardar-cat').addEventListener('click', async () => {
    const nombre = document.getElementById('cat-nombre').value.trim();
    if (!nombre) return showToast('El nombre no puede estar vacío', true);

    const sesion = getSessionData();
    const miEmpresaId = sesion?.empresa_id;

    try {
        const payload = { nombre };
        if (miEmpresaId) {
            payload.empresa = { id: parseInt(miEmpresaId) };
        }
        await fetchAuth('/categorias', 'POST', payload);
        showToast('Categoría creada con éxito');
        document.getElementById('cat-nombre').value = '';
        cargarCategorias();
        cargarCategoriasParaSelect(); // Actualizar select de productos
    } catch (e) { showToast(e.message, true); }
});

async function eliminarCategoria(id, nombre) {
    if (!confirm(`¿Estás seguro de eliminar la categoría "${nombre}"? ESTO ELIMINARÁ TODOS SUS PRODUCTOS EN CASCADA.`)) return;
    try {
        const msg = await fetchAuth(`/categorias/${id}`, 'DELETE');
        showToast(msg || 'Categoría eliminada');
        cargarCategorias();
        cargarCategoriasParaSelect();
    } catch (e) { showToast(e.message, true); }
}

/* ==========================================================
   PRODUCTOS
   ========================================================== */
document.getElementById('btn-toggle-nuevo-prod').addEventListener('click', () => {
    const form = document.getElementById('form-producto-card');
    form.style.display = form.style.display === 'none' ? 'block' : 'none';
    if (form.style.display === 'block' && document.getElementById('contenedor-variantes-temp').children.length === 0) {
        addVarianteRow(); // Añadir una fila por defecto
    }
});
document.getElementById('btn-cancelar-prod').addEventListener('click', () => {
    document.getElementById('form-producto-card').style.display = 'none';
});

// Filas dinámicas de variantes para la creación del producto
let varianteRowIndex = 0;
function addVarianteRow() {
    const container = document.getElementById('contenedor-variantes-temp');
    const div = document.createElement('div');
    div.className = 'variante-item';
    div.id = `var-row-${varianteRowIndex}`;
    const autoSku = genSKU();
    div.innerHTML = `
    <div class="campo" style="flex:1"><label class="etiqueta-campo">SKU</label><input type="text" class="v-sku" value="${autoSku}" disabled style="background:#eee"></div>
    <div class="campo" style="flex:1"><label class="etiqueta-campo">Color</label><input type="text" class="v-color" placeholder="Ej. Negro"></div>
    <div class="campo" style="flex:0.8"><label class="etiqueta-campo">Hex Color</label><input type="color" class="v-hex" value="#5c4a3d" style="width: 100%; height: 38px; padding: 2px; cursor: pointer; border-radius: 12px; border: 1.5px solid var(--borde-trazo); background: var(--crema);"></div>
    <div class="campo" style="flex:0.5"><label class="etiqueta-campo">Talla</label><input type="text" class="v-talla" placeholder="Ej. M"></div>
    <div class="campo" style="flex:0.5"><label class="etiqueta-campo">Stock</label><input type="number" class="v-stock" value="10"></div>
    <div class="campo" style="flex:0.5"><label class="etiqueta-campo">Stock Crítico</label><input type="number" class="v-critico" value="2"></div>
    <div class="campo" style="flex:1.5"><label class="etiqueta-campo">Imagen URL</label><input type="text" class="v-img" placeholder="https://..."></div>
    <button class="btn-quitar-variante" style="margin-bottom: 2px;" onclick="document.getElementById('var-row-${varianteRowIndex}').remove()">✕</button>
  `;
    container.appendChild(div);

    // Auto-sync color
    const colorInput = div.querySelector('.v-color');
    const hexInput = div.querySelector('.v-hex');
    colorInput.addEventListener('input', () => {
        const key = colorInput.value.trim().toLowerCase();
        if (typeof COLOR_MAP !== 'undefined' && COLOR_MAP[key]) hexInput.value = COLOR_MAP[key];
    });

    varianteRowIndex++;
}
document.getElementById('btn-add-variante-row').addEventListener('click', addVarianteRow);

// Guardar Producto + Variantes
document.getElementById('btn-guardar-prod').addEventListener('click', async () => {
    const nombre = document.getElementById('prod-nombre').value.trim();
    const catId = document.getElementById('prod-cat').value;
    const provId = document.getElementById('prod-prov').value;
    const precio = document.getElementById('prod-precio').value;
    const desc = document.getElementById('prod-desc').value;
    const img = document.getElementById('prod-img').value;

    if (!nombre || !catId || !precio) return showToast('Nombre, categoría y precio son obligatorios', true);

    const btn = document.getElementById('btn-guardar-prod');
    btn.disabled = true;
    btn.textContent = 'Guardando...';

    const sesion = getSessionData();
    const miEmpresaId = sesion?.empresa_id;

    try {
        // 1. Guardar producto
        const payloadProd = {
            nombre,
            precio: parseFloat(precio),
            descripcion: desc,
            imagen_url: img,
            categoria: { id: parseInt(catId) }
        };
        if (provId) {
            payloadProd.proveedor = { id: parseInt(provId) };
        }
        if (miEmpresaId) {
            payloadProd.empresa = { id: parseInt(miEmpresaId) };
        }
        const prodCreado = await fetchAuth('/productos', 'POST', payloadProd);

        // 2. Guardar variantes
        const rows = document.querySelectorAll('.variante-item');
        for (const row of rows) {
            const vSku = row.querySelector('.v-sku').value;
            const vColor = row.querySelector('.v-color').value || 'Default';
            const vHex = row.querySelector('.v-hex').value || '#000000';
            const vTalla = row.querySelector('.v-talla').value || 'U';
            const vStock = parseInt(row.querySelector('.v-stock').value) || 0;
            const vCritico = parseInt(row.querySelector('.v-critico').value) || 0;
            const vImg = row.querySelector('.v-img').value || '';

            const payloadVar = {
                sku: vSku, color: vColor, talla: vTalla, stock: vStock, stock_critico: vCritico, imagen: vImg, hex: vHex,
                producto: { id: prodCreado.id }
            };
            if (miEmpresaId) {
                payloadVar.empresa = { id: parseInt(miEmpresaId) };
            }
            await fetchAuth('/variantes', 'POST', payloadVar);
        }

        showToast(`Producto ${prodCreado.nombre} y sus variantes creados exitosamente`);

        // Resetear form
        document.getElementById('prod-nombre').value = '';
        document.getElementById('prod-cat').value = '';
        document.getElementById('prod-prov').value = '';
        document.getElementById('prod-precio').value = '';
        document.getElementById('prod-desc').value = '';
        document.getElementById('prod-img').value = '';
        document.getElementById('contenedor-variantes-temp').innerHTML = '';
        document.getElementById('form-producto-card').style.display = 'none';

        cargarProductos();
    } catch (e) {
        showToast(e.message, true);
    } finally {
        btn.disabled = false;
        btn.textContent = 'Guardar Producto y Variantes';
    }
});

// Listar Productos y Variantes (Acordeón)
let lastProducts = [];
async function cargarProductos() {
    const tbody = document.getElementById('tabla-productos');
    const verInactivos = document.getElementById('chk-ver-inactivos')?.checked || false;
    try {
        const prods = await fetchAuth(`/productos?incluirInactivos=${verInactivos}`);
        lastProducts = prods;

        tbody.innerHTML = prods.map(p => {
            const vars = p.variantes || [];
            const varCount = vars.length;
            const isInactive = p.estado === 'INACTIVO';
            const rowStyle = isInactive ? 'style="opacity:0.65; background-color:rgba(92,74,61,0.04);"' : '';
            const statusBadge = isInactive ? ' <span class="pildora estado-fallo" style="font-size:0.65rem;">Deshabilitado</span>' : '';
            
            const btnAccion = isInactive
                ? `<button class="btn btn-primario btn-mini" onclick="event.stopPropagation(); activarProducto(${p.id}, '${p.nombre.replace(/'/g, "\\'")}')">Habilitar</button>`
                : `<button class="btn btn-peligro btn-mini" onclick="event.stopPropagation(); eliminarProducto(${p.id}, '${p.nombre.replace(/'/g, "\\'")}')">Deshabilitar</button>`;

            return `
        <tr class="fila-principal" ${rowStyle} onclick="toggleVariantes(${p.id})">
          <td>${p.id}</td>
          <td><img src="${p.imagen_url || 'https://via.placeholder.com/40x50'}" class="img-min"></td>
          <td>
            <strong>${p.nombre}</strong>${statusBadge}<br>
            <span style="font-size:0.75rem; color:var(--cafe); opacity:0.8">${p.proveedor ? 'Prov: ' + p.proveedor.razonSocial : 'Sin proveedor'}</span>
          </td>
          <td>S/. ${p.precio.toFixed(2)}</td>
          <td>${p.categoria ? p.categoria.nombre : '-'}</td>
          <td><span class="pildora rol-almacenero">${varCount} variante(s)</span></td>
          <td>
            <button class="btn btn-secundario btn-mini" onclick="event.stopPropagation(); toggleVariantes(${p.id})">Ver ▾</button>
            ${btnAccion}
          </td>
        </tr>
        <tr id="vars-prod-${p.id}" style="display:none;">
          <td colspan="7" style="padding:0;">
            <div class="variantes-lista-expandida" style="padding: 15px 20px;">
              <div style="display:flex; justify-content:space-between; margin-bottom:10px; align-items: center;">
                <h4 style="margin:0; font-size:0.9rem; color:var(--cafe-oscuro)">Variantes de ${p.nombre}</h4>
                <button class="btn btn-primario btn-mini" onclick="openAddVariantModal(${p.id}, '${p.nombre.replace(/'/g, "\\'")}')">+ Añadir Variante</button>
              </div>
              ${vars.length === 0 ? '<p style="font-size:0.8rem; color:var(--cafe); margin:0;">No hay variantes registradas.</p>' : `
              <table>
                <thead><tr><th>SKU</th><th>Img</th><th>Color</th><th>Talla</th><th>Stock</th><th>Acción</th></tr></thead>
                <tbody>
                  ${vars.map(v => {
                    const isVarInactive = v.estado === 'INACTIVO';
                    const varRowStyle = isVarInactive ? 'style="opacity:0.65; background-color:rgba(92,74,61,0.02);"' : '';
                    const varStatus = isVarInactive ? ' <small style="color:var(--alerta)">(Deshabilitada)</small>' : '';
                    
                    const btnVarAccion = isVarInactive
                        ? `<button class="btn btn-primario btn-mini" onclick="activarVariante(${v.id}, '${v.sku}')">Habilitar</button>`
                        : `<button class="btn btn-peligro btn-mini" onclick="eliminarVariante(${v.id}, '${v.sku}')">Deshabilitar</button>`;
                    
                    return `
                    <tr ${varRowStyle}>
                      <td class="mono">${v.sku}</td>
                      <td>${v.imagen ? `<img src="${v.imagen}" style="width:25px;height:25px;object-fit:cover;border-radius:4px;">` : '-'}</td>
                      <td>
                        <span style="display:inline-block; width:12px; height:12px; border-radius:50%; background-color:${v.hex || '#000000'}; border:1px solid var(--borde-trazo); vertical-align:middle; margin-right:6px;"></span>
                        ${v.color}${varStatus} <span style="font-size:0.75rem; color:var(--cafe); opacity:0.7">(${v.hex || '#000000'})</span>
                      </td>
                      <td>${v.talla}</td>
                      <td><span class="pildora ${v.stock <= v.stock_critico ? 'estado-fallo' : 'estado-ok'}">${v.stock}</span></td>
                      <td>${btnVarAccion}</td>
                    </tr>
                    `;
                  }).join('')}
                </tbody>
              </table>
              `}
            </div>
          </td>
        </tr>
      `;
        }).join('');

        if (prods.length === 0) tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding:20px;">No hay productos registrados.</td></tr>';
    } catch (e) { tbody.innerHTML = `<tr><td colspan="7" style="color:var(--alerta)">Error: ${e.message}</td></tr>`; }
}

function toggleVariantes(prodId) {
    const tr = document.getElementById(`vars-prod-${prodId}`);
    if (tr) tr.style.display = tr.style.display === 'none' ? 'table-row' : 'none';
}

async function eliminarProducto(id, nombre) {
    if (!confirm(`¿Deshabilitar producto "${nombre}" y todas sus variantes?`)) return;
    try {
        const msg = await fetchAuth(`/productos/${id}`, 'DELETE');
        showToast(msg || 'Producto deshabilitado');
        cargarProductos();
    } catch (e) { showToast(e.message, true); }
}

async function eliminarVariante(id, sku) {
    if (!confirm(`¿Deshabilitar variante SKU: ${sku}?`)) return;
    try {
        const msg = await fetchAuth(`/variantes/${id}`, 'DELETE');
        showToast(msg || 'Variante deshabilitada');
        cargarProductos();
    } catch (e) { showToast(e.message, true); }
}

async function activarProducto(id, nombre) {
    if (!confirm(`¿Desea volver a habilitar el producto "${nombre}"?`)) return;
    try {
        await fetchAuth(`/productos/${id}/activar`, 'PUT');
        showToast('Producto habilitado con éxito');
        cargarProductos();
    } catch (e) { showToast(e.message, true); }
}

async function activarVariante(id, sku) {
    if (!confirm(`¿Desea volver a habilitar la variante SKU: ${sku}?`)) return;
    try {
        await fetchAuth(`/variantes/${id}/activar`, 'PUT');
        showToast('Variante habilitada con éxito');
        cargarProductos();
    } catch (e) { showToast(e.message, true); }
}

// Modal añadir una variante
function openAddVariantModal(prodId, prodNombre) {
    document.getElementById('modal-var-prod-id').value = prodId;
    document.getElementById('modal-var-prod-nombre').textContent = prodNombre;
    document.getElementById('modal-var-sku').value = genSKU();
    document.getElementById('modal-var-color').value = '';
    document.getElementById('modal-var-talla').value = '';
    document.getElementById('modal-var-img').value = '';
    document.getElementById('modal-add-variante').classList.remove('oculto');
}

// Auto-sync: color nombre → hex en el modal
document.getElementById('modal-var-color').addEventListener('input', () => {
    const key = document.getElementById('modal-var-color').value.trim().toLowerCase();
    if (COLOR_MAP[key]) document.getElementById('modal-var-hex').value = COLOR_MAP[key];
});

document.getElementById('btn-save-single-variant').addEventListener('click', async () => {
    const prodId = document.getElementById('modal-var-prod-id').value;
    const sesion = getSessionData();
    const miEmpresaId = sesion?.empresa_id;

    const payload = {
        sku: document.getElementById('modal-var-sku').value,
        color: document.getElementById('modal-var-color').value || 'Default',
        talla: document.getElementById('modal-var-talla').value || 'U',
        stock: parseInt(document.getElementById('modal-var-stock').value) || 0,
        stock_critico: parseInt(document.getElementById('modal-var-critico').value) || 0,
        hex: document.getElementById('modal-var-hex').value || '#000',
        imagen: document.getElementById('modal-var-img').value || '',
        producto: { id: parseInt(prodId) }
    };

    if (miEmpresaId) {
        payload.empresa = { id: parseInt(miEmpresaId) };
    }

    try {
        await fetchAuth('/variantes', 'POST', payload);
        showToast('Variante añadida');
        document.getElementById('modal-add-variante').classList.add('oculto');
        await cargarProductos(); // refescar tabla para ver la variante

        // Dejar la vista expandida abierta para este producto
        setTimeout(() => {
            const tr = document.getElementById(`vars-prod-${prodId}`);
            if (tr) tr.style.display = 'table-row';
        }, 200);

    } catch (e) { showToast(e.message, true); }
});

/* ==========================================================
   PEDIDOS (REAL)
   ========================================================== */
async function cargarPedidos() {
    const tbody = document.getElementById('tabla-pedidos');
    tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding:20px;">Cargando pedidos...</td></tr>';
    try {
        const pedidos = await fetchAuth('/pedidos');
        if (pedidos.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding:20px;">No hay pedidos registrados en tu boutique.</td></tr>';
            return;
        }

        tbody.innerHTML = pedidos.map(p => {
            const fecha = p.fecha_creacion || 'Desconocida';
            const total = p.total.toFixed(2);
            const cliNombre = p.usuario ? p.usuario.nombre : 'Cliente Desconocido';
            const cliCorreo = p.usuario ? p.usuario.correo : '';
            const pago = p.pago_confirmado
                ? `<span class="pildora estado-ok">Confirmado</span>`
                : `<span class="pildora estado-fallo">Pendiente</span>`;

            const estados = ['PENDIENTE', 'CONFIRMADO', 'EN_PREPARACION', 'LISTO_PARA_RECOGER', 'RECOGIDO', 'CANCELADO'];
            const selectEstado = `
        <select onchange="cambiarEstadoPedido(${p.id}, this.value)" style="padding:4px 8px; font-size:0.8rem; border-radius:8px; border:1px solid var(--borde-trazo); background:var(--crema);">
          ${estados.map(est => `<option value="${est}" ${p.estado === est ? 'selected' : ''}>${est}</option>`).join('')}
        </select>
      `;

            return `
        <tr class="fila-principal" onclick="toggleItemsPedido(${p.id})">
          <td>${p.id}</td>
          <td>
            <strong>${cliNombre}</strong><br>
            <span style="font-size:0.75rem; color:var(--cafe); opacity:0.8">${cliCorreo}</span>
          </td>
          <td>${fecha}</td>
          <td>${pago}</td>
          <td style="font-weight:600;">S/. ${total}</td>
          <td>${selectEstado}</td>
          <td>
            <button class="btn btn-secundario btn-mini" onclick="event.stopPropagation(); toggleItemsPedido(${p.id})">Ver Detalle</button>
          </td>
        </tr>
        <tr id="items-pedido-${p.id}" style="display:none;">
          <td colspan="7" style="padding:0;">
            <div class="variantes-lista-expandida" style="padding: 15px 25px;">
              <h4 style="margin:0 0 10px; font-size:0.9rem; color:var(--cafe-oscuro)">Detalle de Ítems del Pedido #${p.id}</h4>
              <div id="items-pedido-loader-${p.id}">Cargando ítems...</div>
            </div>
          </td>
        </tr>
      `;
        }).join('');
    } catch (e) {
        tbody.innerHTML = `<tr><td colspan="7" style="color:var(--alerta); text-align:center;">Error: ${e.message}</td></tr>`;
    }
}

async function toggleItemsPedido(pedidoId) {
    const tr = document.getElementById(`items-pedido-${pedidoId}`);
    if (!tr) return;

    const isHidden = tr.style.display === 'none';
    tr.style.display = isHidden ? 'table-row' : 'none';

    if (isHidden) {
        const loader = document.getElementById(`items-pedido-loader-${pedidoId}`);
        if (loader && loader.textContent === 'Cargando ítems...') {
            try {
                const items = await fetchAuth(`/pedidoitems/pedido/${pedidoId}`);
                if (!items || items.length === 0) {
                    loader.innerHTML = '<span style="color:var(--cafe); font-size:0.85rem;">No hay ítems registrados para este pedido.</span>';
                    return;
                }

                loader.innerHTML = `
          <table style="width: 100%; font-size: 0.8rem; background: rgba(92,74,61,0.02); border-radius: 8px; border: 1px solid var(--borde-trazo);">
            <thead>
              <tr style="background: rgba(92,74,61,0.05);">
                <th style="padding: 6px;">Producto</th>
                <th style="padding: 6px;">SKU</th>
                <th style="padding: 6px;">Color</th>
                <th style="padding: 6px; text-align: center;">Talla</th>
                <th style="padding: 6px; text-align: right;">Precio Unit.</th>
                <th style="padding: 6px; text-align: center;">Cant.</th>
                <th style="padding: 6px; text-align: right;">Total</th>
              </tr>
            </thead>
            <tbody>
              ${items.map(item => {
                    const prodNombre = item.variante?.producto?.nombre || 'Producto';
                    const sku = item.variante?.sku || '-';
                    const color = item.variante?.color || '-';
                    const talla = item.variante?.talla || '-';
                    const precio = item.precio_unitario.toFixed(2);
                    const cant = item.cantidad;
                    const total = (item.precio_unitario * item.cantidad).toFixed(2);
                    return `
                  <tr>
                    <td style="padding: 6px;"><strong>${prodNombre}</strong></td>
                    <td style="padding: 6px;" class="mono">${sku}</td>
                    <td style="padding: 6px;">${color}</td>
                    <td style="padding: 6px; text-align: center;">${talla}</td>
                    <td style="padding: 6px; text-align: right;">S/. ${precio}</td>
                    <td style="padding: 6px; text-align: center;">${cant}</td>
                    <td style="padding: 6px; text-align: right;">S/. ${total}</td>
                  </tr>
                `;
                }).join('')}
            </tbody>
          </table>
        `;
            } catch (err) {
                loader.innerHTML = `<span style="color:var(--alerta)">Error cargando detalles: ${err.message}</span>`;
            }
        }
    }
}

async function cambiarEstadoPedido(pedidoId, nuevoEstado) {
    try {
        const pedido = await fetchAuth(`/pedidos/${pedidoId}`);
        pedido.estado = nuevoEstado;
        await fetchAuth('/pedidos', 'PUT', pedido);
        showToast(`Pedido #${pedidoId} actualizado a ${nuevoEstado}`);
        cargarPedidos();
    } catch (err) {
        showToast(`Error: ${err.message}`, true);
        cargarPedidos();
    }
}

/* ==========================================================
   CUPONES (REAL)
   ========================================================== */
const btnToggleCupon = document.getElementById('btn-toggle-nuevo-cupon');
const cardCupon = document.getElementById('form-cupon-card');
const btnCancelCupon = document.getElementById('btn-cancelar-cupon');

if (btnToggleCupon) {
    btnToggleCupon.addEventListener('click', () => {
        cardCupon.style.display = cardCupon.style.display === 'none' ? 'block' : 'none';
    });
}
if (btnCancelCupon) {
    btnCancelCupon.addEventListener('click', () => {
        cardCupon.style.display = 'none';
    });
}

async function cargarCupones() {
    const tbody = document.getElementById('tabla-cupones');
    tbody.innerHTML = '<tr><td colspan="8" style="text-align:center; padding:20px;">Cargando cupones...</td></tr>';
    try {
        const cupones = await fetchAuth('/cupones');
        if (cupones.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" style="text-align:center; padding:20px;">No hay cupones activos en tu boutique.</td></tr>';
            return;
        }

        tbody.innerHTML = cupones.map(c => {
            const tipoVal = c.tipo === 'PORCENTAJE' ? `${c.valor}%` : `S/. ${c.valor.toFixed(2)}`;
            const minCompra = c.minimo_compra ? `S/. ${c.minimo_compra.toFixed(2)}` : 'S/. 0.00';
            return `
        <tr>
          <td>${c.id}</td>
          <td><strong class="mono" style="font-size:0.95rem; color:var(--cafe-oscuro)">${c.codigo}</strong></td>
          <td><span class="pildora ${c.tipo === 'PORCENTAJE' ? 'rol-catalogo' : 'rol-vendedor'}">${c.tipo}</span></td>
          <td style="font-weight:600;">${tipoVal}</td>
          <td>${minCompra}</td>
          <td><span class="pildora rol-almacenero">${c.usos_actuales} / ${c.usos_maximos}</span></td>
          <td><span class="pildora estado-ok">${c.estado}</span></td>
          <td>
            <button class="btn btn-peligro btn-mini" onclick="eliminarCupon(${c.id}, '${c.codigo}')">Eliminar</button>
          </td>
        </tr>
      `;
        }).join('');
    } catch (e) {
        tbody.innerHTML = `<tr><td colspan="8" style="color:var(--alerta); text-align:center;">Error: ${e.message}</td></tr>`;
    }
}

document.getElementById('btn-guardar-cupon')?.addEventListener('click', async () => {
    const codigo = document.getElementById('cup-codigo').value.trim();
    const tipo = document.getElementById('cup-tipo').value;
    const valor = document.getElementById('cup-valor').value;
    const minimo = document.getElementById('cup-minimo').value;
    const usos = document.getElementById('cup-usos-max').value;

    if (!codigo || !valor || !usos) {
        return showToast('Código, valor y límite de usos son obligatorios', true);
    }

    const sesion = getSessionData();
    const miEmpresaId = sesion?.empresa_id;

    try {
        const payload = {
            codigo: codigo.toUpperCase(),
            tipo: tipo,
            valor: parseFloat(valor),
            minimo_compra: minimo ? parseFloat(minimo) : 0.0,
            usos_maximos: parseInt(usos),
            usos_actuales: 0,
            estado: 'ACTIVO'
        };

        if (miEmpresaId) {
            payload.empresa = { id: parseInt(miEmpresaId) };
        }

        await fetchAuth('/cupones', 'POST', payload);
        showToast('Cupón creado con éxito');

        // Resetear form
        document.getElementById('cup-codigo').value = '';
        document.getElementById('cup-valor').value = '';
        document.getElementById('cup-minimo').value = '0.00';
        document.getElementById('cup-usos-max').value = '100';
        cardCupon.style.display = 'none';

        cargarCupones();
    } catch (e) {
        showToast(e.message, true);
    }
});

async function eliminarCupon(id, codigo) {
    if (!confirm(`¿Estás seguro de eliminar el cupón "${codigo}"?`)) return;
    try {
        const msg = await fetchAuth(`/cupones/${id}`, 'DELETE');
        showToast(msg || 'Cupón eliminado');
        cargarCupones();
    } catch (e) {
        showToast(e.message, true);
    }
}
/* ==========================================================
   PROVEEDORES
   ========================================================== */
const cardProv = document.getElementById('form-proveedor-card');

document.getElementById('btn-toggle-nuevo-prov')?.addEventListener('click', () => {
    limpiarFormProv();
    cardProv.style.display = cardProv.style.display === 'none' ? 'block' : 'none';
    document.getElementById('prov-form-titulo').textContent = 'Registrar Nuevo Proveedor';
});

document.getElementById('btn-cancelar-prov')?.addEventListener('click', () => {
    limpiarFormProv();
    cardProv.style.display = 'none';
});

function limpiarFormProv() {
    document.getElementById('prov-id').value = '';
    document.getElementById('prov-ruc').value = '';
    document.getElementById('prov-razon').value = '';
    document.getElementById('prov-comercial').value = '';
    document.getElementById('prov-contacto').value = '';
    document.getElementById('prov-telefono').value = '';
    document.getElementById('prov-correo').value = '';
    document.getElementById('prov-direccion').value = '';
    
    // Volver a habilitar el RUC en caso de que se haya deshabilitado al editar
    document.getElementById('prov-ruc').disabled = false;
}

async function cargarProveedores() {
    const tbody = document.getElementById('tabla-proveedores');
    tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; padding:20px;">Cargando proveedores...</td></tr>';

    try {
        // Ajusta el endpoint si en tu backend la ruta base es diferente
        const proveedores = await fetchAuth('/proveedores');

        if (proveedores.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; padding:20px;">No hay proveedores registrados.</td></tr>';
            return;
        }

        tbody.innerHTML = proveedores.map(p => `
        <tr>
          <td><strong class="mono">${p.ruc}</strong></td>
          <td>
            <strong>${p.razonSocial}</strong><br>
            <span style="font-size:0.75rem; color:var(--cafe); opacity:0.8">${p.nombreComercial || '-'}</span>
          </td>
          <td>${p.contactoNombre || '-'}</td>
          <td>${p.telefono || '-'}</td>
          <td>${p.correo || '-'}</td>
          <td>
            <button class="btn btn-secundario btn-mini" onclick="editarProveedor(${p.id})">Editar</button>
            <button class="btn btn-peligro btn-mini" onclick="eliminarProveedor(${p.id}, '${p.razonSocial}')">Eliminar</button>
          </td>
        </tr>
      `).join('');
    } catch (e) {
        tbody.innerHTML = `<tr><td colspan="6" style="color:var(--alerta); text-align:center;">Error: ${e.message}</td></tr>`;
    }
}

document.getElementById('btn-guardar-prov')?.addEventListener('click', async () => {
    const id = document.getElementById('prov-id').value;
    const ruc = document.getElementById('prov-ruc').value.trim();
    const razonSocial = document.getElementById('prov-razon').value.trim();
    const nombreComercial = document.getElementById('prov-comercial').value.trim();
    const contactoNombre = document.getElementById('prov-contacto').value.trim();
    const telefono = document.getElementById('prov-telefono').value.trim();
    const correo = document.getElementById('prov-correo').value.trim();
    const direccion = document.getElementById('prov-direccion').value.trim();

    if (!ruc || !razonSocial) {
        return showToast('El RUC y la Razón Social son obligatorios', true);
    }
    if (ruc.length !== 11) {
        return showToast('El RUC debe tener 11 dígitos', true);
    }

    const btn = document.getElementById('btn-guardar-prov');
    btn.disabled = true;
    btn.textContent = 'Guardando...';

    const sesion = getSessionData();
    const miEmpresaId = sesion?.empresa_id;

    try {
        const payload = {
            ruc,
            razonSocial,
            nombreComercial,
            contactoNombre,
            telefono,
            correo,
            direccion,
            estado: 'ACTIVO'
        };

        if (miEmpresaId) {
            payload.empresa = { id: parseInt(miEmpresaId) };
        }

        if (id) {
            // Es una edición
            await fetchAuth(`/proveedores/${id}`, 'PUT', payload);
            showToast('Proveedor actualizado exitosamente');
        } else {
            // Es una creación
            await fetchAuth('/proveedores', 'POST', payload);
            showToast('Proveedor registrado exitosamente');
        }

        limpiarFormProv();
        cardProv.style.display = 'none';
        cargarProveedores();
    } catch (e) {
        showToast(e.message, true);
    } finally {
        btn.disabled = false;
        btn.textContent = 'Guardar Proveedor';
    }
});

async function editarProveedor(id) {
    try {
        const p = await fetchAuth(`/proveedores/${id}`);

        document.getElementById('prov-id').value = p.id;
        document.getElementById('prov-ruc').value = p.ruc;
        document.getElementById('prov-razon').value = p.razonSocial;
        document.getElementById('prov-comercial').value = p.nombreComercial || '';
        document.getElementById('prov-contacto').value = p.contactoNombre || '';
        document.getElementById('prov-telefono').value = p.telefono || '';
        document.getElementById('prov-correo').value = p.correo || '';
        document.getElementById('prov-direccion').value = p.direccion || '';

        document.getElementById('prov-form-titulo').textContent = 'Editar Proveedor';
        
        // Deshabilitar el RUC para que no pueda ser editado
        document.getElementById('prov-ruc').disabled = true;
        
        cardProv.style.display = 'block';

        // Scroll hacia el formulario
        cardProv.scrollIntoView({ behavior: 'smooth', block: 'start' });
    } catch (e) {
        showToast(`Error al cargar datos del proveedor: ${e.message}`, true);
    }
}

async function eliminarProveedor(id, razonSocial) {
    if (!confirm(`¿Estás seguro de eliminar al proveedor "${razonSocial}"?`)) return;

    try {
        await fetchAuth(`/proveedores/${id}`, 'DELETE');
        showToast('Proveedor eliminado exitosamente');
        cargarProveedores();
    } catch (e) {
        showToast(e.message, true);
    }
}

// Búsqueda en tiempo real para Proveedores
document.getElementById('buscador-prov')?.addEventListener('input', function(e) {
    const termino = e.target.value.toLowerCase();
    const filas = document.querySelectorAll('#tabla-proveedores tr');
    
    filas.forEach(fila => {
        // Evitar filtrar la fila de "Cargando..." o "No hay proveedores..."
        if (fila.cells.length === 1 && fila.cells[0].colSpan > 1) return;
        
        const textoFila = fila.textContent.toLowerCase();
        if (textoFila.includes(termino)) {
            fila.style.display = '';
        } else {
            fila.style.display = 'none';
        }
    });
});

/* ==========================================================
   ALERTAS DE INVENTARIO (REAL)
   ========================================================== */
async function cargarAlertas() {
    const tbody = document.getElementById('tabla-alertas');
    if (!tbody) return;
    tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; padding:20px;">Analizando niveles de stock...</td></tr>';

    try {
        const prods = await fetchAuth('/productos');
        let alertasHTML = '';
        let contadorAlertas = 0;

        prods.forEach(p => {
            const vars = p.variantes || [];
            const prov = p.proveedor;
            
            vars.forEach(v => {
                const stock = v.stock ?? 0;
                const critico = v.stock_critico ?? 2; // valor por defecto si es nulo
                
                if (stock <= critico) {
                    contadorAlertas++;
                    const provText = prov 
                        ? `<strong>${prov.razonSocial}</strong>${prov.contactoNombre ? `<br><small style="color:var(--cafe)">Contacto: ${prov.contactoNombre}</small>` : ''}` 
                        : '<span style="color:var(--alerta)">Sin Proveedor</span>';

                    // Convertir proveedor a JSON string seguro para pasarlo como argumento HTML
                    const provJsonStr = prov ? JSON.stringify({
                        ruc: prov.ruc,
                        razonSocial: prov.razonSocial,
                        contactoNombre: prov.contactoNombre || '',
                        telefono: prov.telefono || '',
                        correo: prov.correo || ''
                    }).replace(/"/g, '&quot;').replace(/'/g, '&#39;') : 'null';

                    alertasHTML += `
                    <tr class="fila-sospechosa">
                      <td class="mono">${v.sku}</td>
                      <td>
                        <strong>${p.nombre}</strong><br>
                        <span style="font-size:0.75rem; color:var(--cafe); opacity:0.8">${provText}</span>
                      </td>
                      <td>
                        <span style="display:inline-block; width:12px; height:12px; border-radius:50%; background-color:${v.hex || '#000000'}; border:1px solid var(--borde-trazo); vertical-align:middle; margin-right:6px;"></span>
                        ${v.color} (${v.talla})
                      </td>
                      <td><span class="pildora estado-fallo">${stock}</span></td>
                      <td>${critico}</td>
                      <td>
                        ${prov 
                          ? `<button class="btn btn-secundario btn-mini" onclick="contactarProveedor(${provJsonStr}, '${p.nombre.replace(/'/g, "\\'")}', '${v.sku}', '${v.color}', '${v.talla}', ${stock}, ${critico})">Contactar Proveedor</button>`
                          : `<span style="font-size:0.8rem; color:var(--cafe)">Asigne un proveedor al producto</span>`
                        }
                      </td>
                    </tr>
                    `;
                }
            });
        });

        if (contadorAlertas === 0) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; color:var(--verde); padding:20px; font-weight: 500;">✓ Todo el inventario se encuentra por encima del stock crítico.</td></tr>';
        } else {
            tbody.innerHTML = alertasHTML;
        }

    } catch (e) {
        tbody.innerHTML = `<tr><td colspan="6" style="color:var(--alerta); text-align:center; padding:20px;">Error al cargar alertas: ${e.message}</td></tr>`;
    }
}

function contactarProveedor(prov, prodNombre, sku, color, talla, stock, critico) {
    if (!prov) {
        showToast('Este producto no tiene proveedor asignado', true);
        return;
    }

    const asunto = `Solicitud de Reabastecimiento - ${prodNombre}`;
    const cuerpo = `Hola ${prov.contactoNombre || 'Proveedor'},\n\nLe escribimos de la boutique para solicitar reabastecimiento del siguiente artículo:\n- Producto: ${prodNombre}\n- SKU: ${sku}\n- Variante: Color ${color}, Talla ${talla}\n- Stock actual: ${stock} unidades (Nivel de reorden: ${critico})\n\nPor favor, confírmenos la disponibilidad y el precio para esta reposición.\n\nAtentamente,\nControl de Inventario.`;

    if (prov.telefono) {
        let tClean = prov.telefono.replace(/\D/g, '');
        if (tClean.length === 9) {
            tClean = '51' + tClean; // Default country code
        }
        window.open(`https://wa.me/${tClean}?text=${encodeURIComponent(cuerpo)}`);
        showToast(`Abriendo chat de WhatsApp para ${prov.razonSocial}`);
    } else if (prov.correo) {
        window.open(`mailto:${prov.correo}?subject=${encodeURIComponent(asunto)}&body=${encodeURIComponent(cuerpo)}`);
        showToast(`Abriendo cliente de correo para ${prov.razonSocial}`);
    } else {
        alert(`No hay teléfono ni correo registrado para ${prov.razonSocial}. \nRUC: ${prov.ruc}\nNombre Comercial: ${prov.nombreComercial || '-'}`);
    }
}