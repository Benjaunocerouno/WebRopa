package com.proyecto.WebRopa.service;

import java.util.List;
import java.util.Optional;

import com.proyecto.WebRopa.entity.Registros;

public interface IRegistrosService {
    List<Registros> buscarTodos(); //metodo para mostrar todos los registros
    void guardar(Registros registro); //guarda un registro en la base de datos
    void modificar(Registros registro); //modifica un registro en la base de datos
    Optional<Registros> buscarId(Integer id); //busca un registro por su id
    void eliminar(Integer id); //elimina un registro por su id
}
