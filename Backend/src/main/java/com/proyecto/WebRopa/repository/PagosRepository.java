package com.proyecto.WebRopa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proyecto.WebRopa.entity.Pagos;

public interface PagosRepository extends JpaRepository<Pagos, Long> {
    
    List<Pagos> findByPedidoId(Long pedidoId);
}
