/* ================================================================
   empresas.service.js — Servicio de Empresas
   Funciones para consumir los endpoints de empresas.
   ================================================================ */

'use strict';

const EmpresasService = {
  /**
   * Obtiene todas las empresas.
   * @returns {Promise<Array>}
   */
  async listarTodas() {
    const res = await fetch(`${CONFIG.API_URL}/empresas`);
    if (!res.ok) throw new Error('Error al obtener empresas');
    return await res.json();
  },

  /**
   * Obtiene una empresa por ID.
   * @param {number|string} id
   * @returns {Promise<object>}
   */
  async obtenerPorId(id) {
    const res = await fetch(`${CONFIG.API_URL}/empresas/${id}`);
    if (!res.ok) throw new Error('Empresa no encontrada');
    return await res.json();
  },
};
