package com.proyecto.WebRopa.service;
import java.util.List;
import java.util.Optional;
import com.proyecto.WebRopa.entity.Permisos;

public interface IPermisosService {
    List<Permisos> buscarTodos();
    void guardar(Permisos permiso);
    void modificar(Permisos permiso);
    Optional<Permisos> buscarId(Long id);
    void eliminar(Long id);
}
