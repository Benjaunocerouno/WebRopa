package com.proyecto.WebRopa.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.proyecto.WebRopa.entity.Boletas;

public interface BoletasRepository extends JpaRepository<Boletas, Long> {

    List<Boletas> findByPedidoId(Long pedidoId);

    @Query("SELECT b FROM Boletas b WHERE b.numero_boleta = ?1")
    Optional<Boletas> findByNumeroboleta(String numeroBoleta);

    Optional<Boletas> findFirstByPedidoId(Long pedidoId);

    List<Boletas> findByPedidoEmpresaId(Long empresaId);

    Optional<Boletas> findByIdAndPedidoEmpresaId(Long id, Long empresaId);
}
