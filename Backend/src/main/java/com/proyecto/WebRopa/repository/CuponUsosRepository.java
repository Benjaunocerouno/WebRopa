package com.proyecto.WebRopa.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proyecto.WebRopa.entity.CuponUsos;

public interface CuponUsosRepository extends JpaRepository<CuponUsos, Long> {
    Optional<CuponUsos> findByCuponIdAndUsuarioId(Long cuponId, Long usuarioId);
}
