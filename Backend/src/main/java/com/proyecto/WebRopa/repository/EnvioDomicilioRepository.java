package com.proyecto.WebRopa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proyecto.WebRopa.entity.EnvioDomicilio;
import java.util.List;
import java.util.Optional;

public interface EnvioDomicilioRepository extends JpaRepository<EnvioDomicilio, Long> {
    List<EnvioDomicilio> findByEmpresaId(Long empresaId);
    Optional<EnvioDomicilio> findByIdAndEmpresaId(Long id, Long empresaId);
    Optional<EnvioDomicilio> findByPedidoId(Long pedidoId);
    Optional<EnvioDomicilio> findByPedidoIdAndEmpresaId(Long pedidoId, Long empresaId);
}
