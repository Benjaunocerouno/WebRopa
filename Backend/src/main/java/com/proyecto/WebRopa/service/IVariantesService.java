package com.proyecto.WebRopa.service;

import java.util.List;
import java.util.Optional;

import com.proyecto.WebRopa.entity.Variantes;

public interface IVariantesService {
    List<Variantes> buscarTodos();
    void guardar(Variantes variante);
    void modificar(Variantes variante);
    Optional<Variantes> buscarId(Long id);
    void eliminar(Long id);
}
