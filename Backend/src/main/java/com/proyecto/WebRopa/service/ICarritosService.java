package com.proyecto.WebRopa.service;

import java.util.List;
import java.util.Optional;
import com.proyecto.WebRopa.entity.Carritos;

public interface ICarritosService {
    List<Carritos> buscarTodos();
    void guardar(Carritos carrito);
    Optional<Carritos> buscarPorUsuarioId(Long usuarioId);
    Optional<Carritos> buscarId(Long id);
    void reactivarCarrito(Long id);
    void eliminar(Long id);
}