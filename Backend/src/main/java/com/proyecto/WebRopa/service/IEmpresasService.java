package com.proyecto.WebRopa.service;
import java.util.List;
import java.util.Optional;
import com.proyecto.WebRopa.entity.Empresas;

public interface IEmpresasService {
    List<Empresas> buscarTodos();
    void guardar(Empresas empresa);
    void modificar(Empresas empresa);
    Optional<Empresas> buscarId(Long id);
    void eliminar(Long id);
}
