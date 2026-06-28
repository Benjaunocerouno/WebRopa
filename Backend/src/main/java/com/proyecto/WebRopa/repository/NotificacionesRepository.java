package com.proyecto.WebRopa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proyecto.WebRopa.entity.Notificaciones;

public interface NotificacionesRepository extends JpaRepository<Notificaciones, Long> {
    List<Notificaciones> findByUsuarioIdOrderByFechaDesc(Long usuarioId);
    boolean existsByPedidoIdAndTipo(Long pedidoId, Notificaciones.Tipo tipo);
}
