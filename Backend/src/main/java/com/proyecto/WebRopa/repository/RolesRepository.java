package com.proyecto.WebRopa.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.proyecto.WebRopa.entity.Roles;

public interface RolesRepository extends JpaRepository<Roles, Long> {
    Optional<Roles> findByNombre(String nombre);
}
