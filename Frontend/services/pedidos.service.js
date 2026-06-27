/* ================================================================
   pedidos.service.js — Servicio de Pedidos, Pagos y Carrito
   Funciones para consumir los endpoints de pedidos y checkout.
   ================================================================ */

'use strict';

const PedidosService = {
  /**
   * Obtiene los pedidos de un usuario.
   * @param {number|string} usuarioId
   * @param {string} token
   * @returns {Promise<Array>}
   */
  async listarPorUsuario(usuarioId, token) {
    const res = await fetch(`${CONFIG.API_URL}/pedidos/usuario/${usuarioId}`, {
      headers: { 'Authorization': `Bearer ${token}` },
    });
    if (!res.ok) throw new Error('Error al obtener pedidos');
    return await res.json();
  },

  /**
   * Obtiene los items de un pedido.
   * @param {number|string} pedidoId
   * @param {string} token
   * @returns {Promise<Array>}
   */
  async obtenerItems(pedidoId, token) {
    const res = await fetch(`${CONFIG.API_URL}/pedidoitems/pedido/${pedidoId}`, {
      headers: { 'Authorization': `Bearer ${token}` },
    });
    if (!res.ok) throw new Error('Error al obtener items del pedido');
    return await res.json();
  },

  /**
   * Obtiene el recojo en tienda de un pedido.
   * @param {number|string} pedidoId
   * @param {string} token
   * @returns {Promise<object|null>}
   */
  async obtenerRecojo(pedidoId, token) {
    const res = await fetch(`${CONFIG.API_URL}/recojotienda/pedido/${pedidoId}`, {
      headers: { 'Authorization': `Bearer ${token}` },
    });
    if (!res.ok) return null;
    return await res.json();
  },
};
