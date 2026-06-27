package com.proyecto.WebRopa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proyecto.WebRopa.entity.Categorias;
import java.util.List;
import java.util.Optional;

public interface CategoriasRepository extends JpaRepository<Categorias, Long> {
    List<Categorias> findByEmpresaId(Long empresaId);
    Optional<Categorias> findByIdAndEmpresaId(Long id, Long empresaId);
}
