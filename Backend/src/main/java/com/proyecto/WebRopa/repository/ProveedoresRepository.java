package com.proyecto.WebRopa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proyecto.WebRopa.entity.Proveedores;
import java.util.List;
import java.util.Optional;

public interface ProveedoresRepository extends JpaRepository<Proveedores, Long> {
    List<Proveedores> findByEmpresaId(Long empresaId);

    Optional<Proveedores> findByIdAndEmpresaId(Long id, Long empresaId);

    Optional<Proveedores> findByRucAndEmpresaId(String ruc, Long empresaId);

    Optional<Proveedores> findByRazonSocialAndEmpresaId(String razonSocial, Long empresaId);

    Optional<Proveedores> findByCorreoAndEmpresaId(String correo, Long empresaId);
}