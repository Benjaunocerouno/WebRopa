/* ================================================================
   auth.service.js — Servicio de Autenticación
   Funciones para login, registro, sesión y token.
   ================================================================ */

'use strict';

const AuthService = {
  /**
   * Inicia sesión con correo y contraseña.
   * @param {string} correo
   * @param {string} password
   * @returns {Promise<object>} Datos del usuario con token
   */
  async login(correo, password) {
    const res = await fetch(`${CONFIG.API_URL}/usuarios/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ correo, password }),
    });

    if (!res.ok) throw new Error('Credenciales incorrectas.');
    return await res.json();
  },

  /**
   * Registra un nuevo usuario.
   * @param {object} datos - { nombre, correo, password, telefono }
   * @returns {Promise<object>} Usuario creado
   */
  async registro(datos) {
    const res = await fetch(`${CONFIG.API_URL}/usuarios/registro`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(datos),
    });

    if (!res.ok) {
      const errText = await res.text();
      throw new Error(errText || 'Error al crear la cuenta.');
    }
    return await res.json();
  },

  /**
   * Obtiene los datos del usuario autenticado.
   * @param {string} token
   * @returns {Promise<object>} Datos del usuario
   */
  async obtenerMe(token) {
    const res = await fetch(`${CONFIG.API_URL}/usuarios/me`, {
      headers: { 'Authorization': `Bearer ${token}` },
    });
    if (!res.ok) throw new Error('No autenticado');
    return await res.json();
  },

  /**
   * Obtiene el token guardado en localStorage.
   * @returns {string|null}
   */
  getToken() {
    try {
      return JSON.parse(localStorage.getItem(CONFIG.TOKEN_KEY))?.token || null;
    } catch {
      return null;
    }
  },

  /**
   * Obtiene los datos de sesión guardados.
   * @returns {object|null}
   */
  getSession() {
    try {
      return JSON.parse(localStorage.getItem(CONFIG.TOKEN_KEY));
    } catch {
      return null;
    }
  },
};
