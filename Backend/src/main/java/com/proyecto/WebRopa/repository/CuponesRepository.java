package com.proyecto.WebRopa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proyecto.WebRopa.entity.Cupones;
import java.util.List;
import java.util.Optional;

public interface CuponesRepository extends JpaRepository<Cupones, Long> {
    List<Cupones> findByEmpresaId(Long empresaId);
    Optional<Cupones> findByIdAndEmpresaId(Long id, Long empresaId);
}
