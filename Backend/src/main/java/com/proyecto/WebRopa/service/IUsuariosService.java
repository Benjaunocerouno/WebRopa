package com.proyecto.WebRopa.service;

import java.util.List;
import java.util.Optional;
import com.proyecto.WebRopa.entity.Usuarios;

public interface IUsuariosService {
    List<Usuarios> buscarTodos();
    void guardar(Usuarios usuario);
    void modificar(Usuarios usuario);
    Optional<Usuarios> buscarId(Long id);
    Optional<Usuarios> buscarPorCorreo(String correo);
    void eliminar(Long id);
}