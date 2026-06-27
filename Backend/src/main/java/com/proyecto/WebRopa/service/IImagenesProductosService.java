package com.proyecto.WebRopa.service;

import java.util.List;
import java.util.Optional;
import com.proyecto.WebRopa.entity.ImagenesProductos;

public interface IImagenesProductosService {
    List<ImagenesProductos> buscarTodos();
    Optional<ImagenesProductos> buscarId(Long id);
    void guardar(ImagenesProductos imagen);
    void modificar(ImagenesProductos imagen);
    void eliminar(Long id);
}