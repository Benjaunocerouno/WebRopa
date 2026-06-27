package com.proyecto.WebRopa.service;

import java.util.List;
import java.util.Optional;
import com.proyecto.WebRopa.entity.PedidosItems;

public interface IPedidosItemsService {
    List<PedidosItems> buscarPorPedidoId(Long pedidoId);
    Optional<PedidosItems> buscarId(Long id);
    void guardar(PedidosItems item);
    void eliminar(Long id);
}