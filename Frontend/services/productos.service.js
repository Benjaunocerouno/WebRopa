/* ================================================================
   productos.service.js — Servicio de Productos y Categorías
   Funciones para consumir los endpoints de productos.
   ================================================================ */

'use strict';

const ProductosService = {
  /**
   * Obtiene todos los productos.
   * @returns {Promise<Array>}
   */
  async listarTodos() {
    const res = await fetch(`${CONFIG.API_URL}/productos`);
    if (!res.ok) throw new Error('Error al obtener productos');
    return await res.json();
  },

  /**
   * Obtiene un producto por ID.
   * @param {number} id
   * @returns {Promise<object>}
   */
  async obtenerPorId(id) {
    const res = await fetch(`${CONFIG.API_URL}/productos/${id}`);
    if (!res.ok) throw new Error('Producto no encontrado');
    return await res.json();
  },

  /**
   * Obtiene los productos de una empresa.
   * @param {number|string} empresaId
   * @param {string|null} token - Token opcional para autenticación
   * @returns {Promise<Array>}
   */
  async listarPorEmpresa(empresaId, token = null) {
    const headers = {};
    if (token) headers['Authorization'] = `Bearer ${token}`;

    const res = await fetch(`${CONFIG.API_URL}/productos/empresa/${empresaId}`, { headers });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return await res.json();
  },

  /**
   * Obtiene todas las categorías.
   * @returns {Promise<Array>}
   */
  async listarCategorias() {
    const res = await fetch(`${CONFIG.API_URL}/categorias`);
    if (!res.ok) throw new Error('Error al obtener categorías');
    return await res.json();
  },
};
