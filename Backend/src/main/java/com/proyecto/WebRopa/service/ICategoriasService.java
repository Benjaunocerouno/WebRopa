package com.proyecto.WebRopa.service;

import java.util.List;
import java.util.Optional;
import com.proyecto.WebRopa.entity.Categorias;

public interface ICategoriasService {
    List<Categorias> buscarTodos();
    void guardar(Categorias categoria);
    void modificar(Categorias categoria);
    Optional<Categorias> buscarId(Long id);
    void eliminar(Long id);
}