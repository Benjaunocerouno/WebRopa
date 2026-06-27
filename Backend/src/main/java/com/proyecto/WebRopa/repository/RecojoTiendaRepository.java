package com.proyecto.WebRopa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proyecto.WebRopa.entity.RecojoTienda;
import java.util.List;
import java.util.Optional;

public interface RecojoTiendaRepository extends JpaRepository<RecojoTienda, Long> {
    List<RecojoTienda> findByEmpresaId(Long empresaId);
    Optional<RecojoTienda> findByIdAndEmpresaId(Long id, Long empresaId);
    Optional<RecojoTienda> findByPedidoId(Long pedidoId);
    Optional<RecojoTienda> findByPedidoIdAndEmpresaId(Long pedidoId, Long empresaId);
}
