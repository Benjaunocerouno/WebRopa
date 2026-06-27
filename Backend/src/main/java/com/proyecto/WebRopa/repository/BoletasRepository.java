package com.proyecto.WebRopa.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.proyecto.WebRopa.entity.Boletas;

public interface BoletasRepository extends JpaRepository<Boletas, Long> {

    List<Boletas> findByPedidoId(Long pedidoId);

    // Para buscarPorNumeroBoleta
    @Query("SELECT b FROM Boletas b WHERE b.numero_boleta = ?1")
    Optional<Boletas> findByNumeroboleta(String numeroBoleta);
    
    // Para buscarPorPedidoId que devuelve Optional (una boleta por pedido)
    Optional<Boletas> findFirstByPedidoId(Long pedidoId);
}
