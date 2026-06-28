package com.proyecto.WebRopa.service;

import java.util.List;
import java.util.Optional;

import com.proyecto.WebRopa.entity.EnvioDomicilio;

public interface IEnvioDomicilioService {
    List<EnvioDomicilio> buscarTodos();
    Optional<EnvioDomicilio> buscarId(Long id);
    Optional<EnvioDomicilio> buscarPorPedidoId(Long pedidoId);
    void guardar(EnvioDomicilio envio);
    void modificar(EnvioDomicilio envio);
    void eliminar(Long id);
}
