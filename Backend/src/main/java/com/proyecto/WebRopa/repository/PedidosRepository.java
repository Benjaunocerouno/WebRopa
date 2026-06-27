package com.proyecto.WebRopa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proyecto.WebRopa.entity.Pedidos;
import java.util.List;
import java.util.Optional;

public interface PedidosRepository extends JpaRepository<Pedidos, Long> {
    List<Pedidos> findByEmpresaId(Long empresaId);
    Optional<Pedidos> findByIdAndEmpresaId(Long id, Long empresaId);
}
