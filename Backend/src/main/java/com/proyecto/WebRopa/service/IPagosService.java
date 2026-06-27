package com.proyecto.WebRopa.service;

import java.util.List;
import java.util.Optional;

import com.proyecto.WebRopa.entity.Pagos;

public interface IPagosService {
    List<Pagos> buscarPorPedidoId(Long pedidoId);
    List<Pagos> buscarTodos();
    Optional<Pagos> buscarId(Long id);
    void guardar(Pagos pago);
    void cambiarEstado(Long id, Pagos.Estado nuevoEstado);
    Pagos modificar(Pagos pagoUpdate);
}
