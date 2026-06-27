package com.proyecto.WebRopa.service;

import java.util.List;
import java.util.Optional;
import com.proyecto.WebRopa.entity.Productos;

public interface IProductosService {
    List<Productos> buscarPorEmpresa(Long empresaId);
    List<Productos> buscarTodos();
    void guardar(Productos producto);
    void modificar(Productos producto);
    Optional<Productos> buscarId(Long id);
    void eliminar(Long id);
}