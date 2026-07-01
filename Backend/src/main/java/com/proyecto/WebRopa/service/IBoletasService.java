package com.proyecto.WebRopa.service;

import java.util.List;
import java.util.Optional;

import com.proyecto.WebRopa.entity.Boletas;

public interface IBoletasService {
    List<Boletas> buscarTodos();
    Optional<Boletas> buscarId(Long id);
    List<Boletas> buscarPorPedidoId(Long pedidoId);
    void guardar(Boletas boleta);
    void modificar(Boletas boleta);
    void eliminar(Long id);
}