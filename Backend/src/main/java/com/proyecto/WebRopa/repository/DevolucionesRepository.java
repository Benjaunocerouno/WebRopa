package com.proyecto.WebRopa.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.proyecto.WebRopa.entity.Devoluciones;

public interface DevolucionesRepository extends JpaRepository<Devoluciones, Long> {
    List<Devoluciones> findByPedidoId(Long pedidoId);
    List<Devoluciones> findByEstado(Devoluciones.Estado estado);

    List<Devoluciones> findByPedidoEmpresaId(Long empresaId);

    Optional<Devoluciones> findByIdAndPedidoEmpresaId(Long id, Long empresaId);

    List<Devoluciones> findByEstadoAndPedidoEmpresaId(Devoluciones.Estado estado, Long empresaId);
}