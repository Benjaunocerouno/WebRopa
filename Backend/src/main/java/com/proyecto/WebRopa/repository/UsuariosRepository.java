package com.proyecto.WebRopa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proyecto.WebRopa.entity.Usuarios;
import java.util.List;
import java.util.Optional;

public interface UsuariosRepository extends JpaRepository<Usuarios, Long> {
    Optional<Usuarios> findByCorreo(String correo);
    List<Usuarios> findByEmpresaId(Long empresaId);
    Optional<Usuarios> findByIdAndEmpresaId(Long id, Long empresaId);
}
