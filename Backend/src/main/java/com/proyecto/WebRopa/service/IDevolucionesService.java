package com.proyecto.WebRopa.service;

import java.util.List;
import java.util.Optional;
import com.proyecto.WebRopa.entity.Devoluciones;

public interface IDevolucionesService {
    List<Devoluciones> buscarTodos();
    List<Devoluciones> buscarPorPedidoId(Long pedidoId);
    List<Devoluciones> buscarPorEstado(Devoluciones.Estado estado);
    Optional<Devoluciones> buscarId(Long id);
    void guardar(Devoluciones devolucion);
    void aprobarDevolucion(Long id);
    void rechazarDevolucion(Long id);
    void reembolsarDevolucion(Long id);
    void reembolsarTodasPorPedido(Long pedidoId);
    void eliminar(Long id);
}