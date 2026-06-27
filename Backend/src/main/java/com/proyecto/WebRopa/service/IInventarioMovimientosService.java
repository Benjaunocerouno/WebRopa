package com.proyecto.WebRopa.service;

import java.util.List;
import java.util.Optional;
import com.proyecto.WebRopa.entity.InventarioMovimientos;

public interface IInventarioMovimientosService {
    List<InventarioMovimientos> buscarTodos();
    Optional<InventarioMovimientos> buscarId(Long id);
    void guardar(InventarioMovimientos movimiento);
    void modificar(InventarioMovimientos movimiento);
    void eliminar(Long id);
}