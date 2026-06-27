package com.proyecto.WebRopa.service;

import java.util.List;
import java.util.Optional;

import com.proyecto.WebRopa.entity.Pedidos;

public interface IPedidosService {
    
    List<Pedidos> buscarTodos();
    Optional<Pedidos> buscarId(Long id);
    void guardar(Pedidos pedido);
    void modificar(Pedidos pedido);
    void eliminar(Long id);
}