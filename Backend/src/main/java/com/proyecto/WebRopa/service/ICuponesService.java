package com.proyecto.WebRopa.service;

import java.util.List;
import java.util.Optional;

import com.proyecto.WebRopa.entity.Cupones;

public interface ICuponesService {
    List<Cupones> buscarTodos();
    void guardar(Cupones cupon);
    void modificar(Cupones cupon);
    Optional<Cupones> buscarId(Long id);
    void eliminar(Long id);
}
