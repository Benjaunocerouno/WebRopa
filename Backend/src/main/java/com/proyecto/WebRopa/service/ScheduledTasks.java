package com.proyecto.WebRopa.service;

import com.proyecto.WebRopa.entity.Pedidos;
import com.proyecto.WebRopa.entity.PedidosItems;
import com.proyecto.WebRopa.entity.Variantes;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ScheduledTasks {

    private final IPedidosService pedidosService;
    private final IPedidosItemsService pedidosItemsService;
    private final IVariantesService variantesService;

    public ScheduledTasks(IPedidosService pedidosService, IPedidosItemsService pedidosItemsService, IVariantesService variantesService) {
        this.pedidosService = pedidosService;
        this.pedidosItemsService = pedidosItemsService;
        this.variantesService = variantesService;
    }

    // Ejecutar cada 1 minuto
    @Scheduled(fixedRate = 60000)
    public void cancelarPedidosNoPagados() {
        System.out.println("[Scheduled] Verificando pedidos no pagados...");

        // Buscar todos los pedidos
        List<Pedidos> pedidos = pedidosService.buscarTodos();
        
        // Filtrar pedidos PENDIENTES, no pagados, que tengan más de 10 minutos de creados
        LocalDateTime hace10Minutos = LocalDateTime.now().minusMinutes(10);
        
        List<Pedidos> pedidosExpirados = pedidos.stream()
                .filter(p -> p.getEstado() == Pedidos.Estado.PENDIENTE 
                          && !p.isPago_confirmado() 
                          && p.getFecha_creacion().isBefore(hace10Minutos))
                .collect(Collectors.toList());

        for (Pedidos p : pedidosExpirados) {
            System.out.println("[Scheduled] Cancelando pedido #" + p.getId() + " por tiempo expirado (10 min).");
            
            // 1. Cambiar estado a CANCELADO
            p.setEstado(Pedidos.Estado.CANCELADO);
            pedidosService.guardar(p);
            
            // 2. Liberar Stock de Variantes
            List<PedidosItems> items = pedidosItemsService.buscarPorPedidoId(p.getId());
            for (PedidosItems item : items) {
                Variantes variante = item.getVariante();
                if (variante != null) {
                    variante.setStock(variante.getStock() + item.getCantidad());
                    variantesService.guardar(variante);
                    System.out.println("  - Stock liberado para variante " + variante.getSku() + " (+" + item.getCantidad() + ")");
                }
            }
        }
    }
}
