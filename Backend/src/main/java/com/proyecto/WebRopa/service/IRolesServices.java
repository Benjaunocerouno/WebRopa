package com.proyecto.WebRopa.service;

import java.util.List;
import java.util.Optional;
import com.proyecto.WebRopa.entity.Roles;

public interface IRolesServices {
    
    List<Roles> buscarTodos();
    Optional<Roles> buscarId(Long id);
    Optional<Roles> buscarPorNombre(String nombre);
    void guardar(Roles rol);
    void modificar(Roles rol);
    void eliminar(Long id);
}
