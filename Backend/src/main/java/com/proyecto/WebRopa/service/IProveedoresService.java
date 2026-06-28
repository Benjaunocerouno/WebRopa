package com.proyecto.WebRopa.service;

import java.util.List;
import java.util.Optional;
import com.proyecto.WebRopa.entity.Proveedores;

public interface IProveedoresService {
    List<Proveedores> buscarTodos();

    void guardar(Proveedores proveedor);

    void modificar(Proveedores proveedor);

    Optional<Proveedores> buscarId(Long id);

    void eliminar(Long id);
}