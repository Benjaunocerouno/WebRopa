package com.proyecto.WebRopa.service;

import java.util.List;
import java.util.Optional;

import com.proyecto.WebRopa.entity.CuponUsos;

public interface ICuponUsosService {
    List<CuponUsos> buscarTodos();
        Optional<CuponUsos> buscarPorCuponYUsuario(Long cuponId, Long usuarioId);
        Optional<CuponUsos> buscarId(Long id);
        void registrarUso(CuponUsos cuponUsos);
        void eliminar(Long id);
        void actualizarNota(Long id, String notaAdmin);
}
