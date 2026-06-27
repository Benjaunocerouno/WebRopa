package com.proyecto.WebRopa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proyecto.WebRopa.entity.Variantes;
import java.util.List;
import java.util.Optional;

public interface VariantesRepository extends JpaRepository<Variantes, Long> {
    List<Variantes> findByEmpresaId(Long empresaId);
    Optional<Variantes> findByIdAndEmpresaId(Long id, Long empresaId);
}
