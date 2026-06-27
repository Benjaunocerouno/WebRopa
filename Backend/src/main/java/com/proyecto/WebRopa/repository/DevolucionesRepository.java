package com.proyecto.WebRopa.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.proyecto.WebRopa.entity.Devoluciones;

public interface DevolucionesRepository extends JpaRepository<Devoluciones, Long> {
    List<Devoluciones> findByPedidoId(Long pedidoId);
    List<Devoluciones> findByEstado(Devoluciones.Estado estado);
}