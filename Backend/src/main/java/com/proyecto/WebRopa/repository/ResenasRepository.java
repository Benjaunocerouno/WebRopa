package com.proyecto.WebRopa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proyecto.WebRopa.entity.Resenas;

public interface ResenasRepository extends JpaRepository<Resenas, Long> {
    
    // Spring Boot crea automáticamente la consulta SQL con este nombre
    boolean existsByUsuarioIdAndProductoId(Long usuarioId, Long productoId);
}