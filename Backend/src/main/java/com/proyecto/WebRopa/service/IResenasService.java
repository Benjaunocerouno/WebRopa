package com.proyecto.WebRopa.service;

import java.util.List;
import java.util.Optional;
import com.proyecto.WebRopa.entity.Resenas;

public interface IResenasService {
    List<Resenas> buscarTodos();
    Optional<Resenas> buscarId(Long id);
    void guardar(Resenas resena);
    void modificar(Resenas resena);
    void eliminar(Long id);
    void aprobar(Long id);
}
