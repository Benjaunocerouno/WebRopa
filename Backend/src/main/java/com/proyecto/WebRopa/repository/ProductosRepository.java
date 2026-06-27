package com.proyecto.WebRopa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proyecto.WebRopa.entity.Productos;
import java.util.List;
import java.util.Optional;

public interface ProductosRepository extends JpaRepository<Productos, Long> {
    long countByCategoriaId(Long categoriaId);
    List<Productos> findByEmpresaId(Long empresaId);
    Optional<Productos> findByIdAndEmpresaId(Long id, Long empresaId);
}
