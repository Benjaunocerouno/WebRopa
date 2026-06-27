package com.proyecto.WebRopa.service;

import java.util.List;
import java.util.Optional;

import com.proyecto.WebRopa.entity.RecojoTienda;

public interface IRecojoTiendaService {
    List<RecojoTienda> buscarTodos();
    Optional<RecojoTienda> buscarId(Long id);
    Optional<RecojoTienda> buscarPorPedidoId(Long pedidoId);
    void guardar(RecojoTienda recojo);
    void modificar(RecojoTienda recojo);
    void eliminar(Long id);
}