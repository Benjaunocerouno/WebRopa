/* ================================================================
   api.service.js — Servicio base para consumir la API del backend
   Proporciona el helper apiFetch() que incluye automáticamente
   el token JWT en cada petición.
   ================================================================ */

'use strict';

/**
 * Realiza una petición autenticada a la API del backend.
 * Incluye automáticamente el header Authorization con el token JWT
 * si el usuario tiene sesión activa.
 *
 * @param {string} path - Ruta relativa al API (ej: '/productos')
 * @param {object} options - Opciones de fetch (method, body, headers, etc.)
 * @returns {Promise<Response>}
 */
async function apiFetch(path, options = {}) {
  const headers = { 'Content-Type': 'application/json', ...options.headers };

  // Usar token hardcodeado si existe, de lo contrario buscar en localStorage
  if (CONFIG.HARDCODED_TOKEN) {
    headers['Authorization'] = `Bearer ${CONFIG.HARDCODED_TOKEN}`;
  } else {
    try {
      const saved = JSON.parse(localStorage.getItem(CONFIG.TOKEN_KEY));
      if (saved?.token) {
        headers['Authorization'] = `Bearer ${saved.token}`;
      }
    } catch { /* sin sesión */ }
  }

  const res = await fetch(`${CONFIG.API_URL}${path}`, { ...options, headers });
  return res;
}

/**
 * Realiza una petición autenticada y parsea la respuesta.
 * Lanza error si la respuesta no es OK.
 *
 * @param {string} endpoint - Ruta relativa (ej: '/productos')
 * @param {string} method - Método HTTP (GET, POST, PUT, DELETE)
 * @param {object|null} body - Cuerpo de la petición (se serializa a JSON)
 * @returns {Promise<any>} Datos parseados de la respuesta
 */
async function fetchAuth(endpoint, method = 'GET', body = null) {
  const token = (() => {
    if (CONFIG.HARDCODED_TOKEN) return CONFIG.HARDCODED_TOKEN;
    try { return JSON.parse(localStorage.getItem(CONFIG.TENANT_SESSION_KEY)).token; }
    catch { return null; }
  })();

  if (!token) {
    throw new Error('No autenticado');
  }

  const opts = {
    method,
    headers: {
      'Authorization': 'Bearer ' + token,
      'Content-Type': 'application/json'
    },
    cache: 'no-store'
  };

  if (body) opts.body = JSON.stringify(body);

  const res = await fetch(CONFIG.API_URL + endpoint, opts);

  if (!res.ok) {
      let errMsg = 'Error en la petición';
      try {
        const rawText = await res.text();
        try {
          const eJson = JSON.parse(rawText);
          errMsg = eJson.message || eJson.error || rawText || errMsg;
        } catch {
          errMsg = rawText || errMsg;
        }
      } catch {}
      throw new Error(errMsg);
  }

  if (method === 'GET' || res.headers.get('content-type')?.includes('json')) {
    return await res.json();
  }
  return await res.text();
}
