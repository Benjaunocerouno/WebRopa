package com.proyecto.WebRopa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proyecto.WebRopa.entity.PedidosItems;
import java.util.List;

public interface PedidosItemsRepository extends JpaRepository<PedidosItems, Long> {
    
    // Para obtener todos los items de un pedido específico
    List<PedidosItems> findByPedidoId(Long pedidoId);
}