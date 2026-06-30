package com.proyecto.WebRopa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proyecto.WebRopa.entity.Sucursales;
import java.util.List;
import java.util.Optional;

public interface SucursalesRepository extends JpaRepository<Sucursales, Long> {
    List<Sucursales> findByEmpresaId(Long empresaId);
    Optional<Sucursales> findByIdAndEmpresaId(Long id, Long empresaId);
}
