package com.proyecto.WebRopa.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proyecto.WebRopa.entity.ConfiguracionDespacho;

public interface ConfiguracionDespachoRepository extends JpaRepository<ConfiguracionDespacho, Long> {
    Optional<ConfiguracionDespacho> findByEmpresaId(Long empresaId);
}
