package com.proyecto.WebRopa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proyecto.WebRopa.entity.InventarioMovimientos;

public interface InventarioMovimientosRepository extends JpaRepository<InventarioMovimientos, Long> {
}