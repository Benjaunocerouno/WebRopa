package com.proyecto.WebRopa.service;

import com.proyecto.WebRopa.entity.Notificaciones;
import com.proyecto.WebRopa.entity.Pedidos;
import com.proyecto.WebRopa.entity.PedidosItems;
import com.proyecto.WebRopa.entity.RecojoTienda;
import com.proyecto.WebRopa.entity.Variantes;
import com.proyecto.WebRopa.util.DiasHabilesHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ScheduledTasks {

    private final IPedidosService pedidosService;
    private final IPedidosItemsService pedidosItemsService;
    private final IVariantesService variantesService;
    private final IRecojoTiendaService recojoTiendaService;
    private final IConfiguracionDespachoService configService;
    private final IPedidoEstadoService pedidoEstadoService;
    private final INotificacionesService notificacionesService;

    public ScheduledTasks(
            IPedidosService pedidosService,
            IPedidosItemsService pedidosItemsService,
            IVariantesService variantesService,
            IRecojoTiendaService recojoTiendaService,
            IConfiguracionDespachoService configService,
            IPedidoEstadoService pedidoEstadoService,
            INotificacionesService notificacionesService) {
        this.pedidosService = pedidosService;
        this.pedidosItemsService = pedidosItemsService;
        this.variantesService = variantesService;
        this.recojoTiendaService = recojoTiendaService;
        this.configService = configService;
        this.pedidoEstadoService = pedidoEstadoService;
        this.notificacionesService = notificacionesService;
    }

    // ── Cancelar pedidos PENDIENTES sin pago (cada 1 min) ───────────
    @Scheduled(fixedRate = 60000)
    public void cancelarPedidosNoPagados() {
        System.out.println("[Scheduled] Verificando pedidos no pagados...");

        LocalDateTime hace10Minutos = LocalDateTime.now().minusMinutes(10);

        List<Pedidos> pedidosExpirados = pedidosService.buscarTodos().stream()
                .filter(p -> p.getEstado() == Pedidos.Estado.PENDIENTE
                          && !p.isPago_confirmado()
                          && p.getFecha_creacion().isBefore(hace10Minutos))
                .collect(Collectors.toList());

        for (Pedidos p : pedidosExpirados) {
            System.out.println("[Scheduled] Cancelando pedido #" + p.getId() + " por tiempo expirado (10 min).");

            p.setEstado(Pedidos.Estado.CANCELADO);
            pedidosService.guardar(p);

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

    // ── Job A: LISTO_PARA_RECOGER → NO_RECOGIDO (cada 15 min) ──────
    // Transiciona cuando fecha_disponible + días hábiles de recojo ya venció.
    @Scheduled(fixedRate = 900000)
    public void transicionarNoRecogido() {
        System.out.println("[Scheduled] Verificando pedidos LISTO_PARA_RECOGER vencidos...");

        LocalDateTime ahora = LocalDateTime.now();

        List<Pedidos> candidatos = pedidosService.buscarTodos().stream()
                .filter(p -> p.getEstado() == Pedidos.Estado.LISTO_PARA_RECOGER
                          && p.getEmpresa() != null)
                .collect(Collectors.toList());

        for (Pedidos p : candidatos) {
            Optional<RecojoTienda> recojoOpt = recojoTiendaService.buscarPorPedidoId(p.getId());
            if (!recojoOpt.isPresent()) continue;

            RecojoTienda recojo = recojoOpt.get();
            if (recojo.getFecha_disponible() == null) continue;

            int diasRecojo = configService.obtenerOCrearDefault(p.getEmpresa().getId())
                    .getDiasHabilesParaRecojo();
            LocalDateTime limite = DiasHabilesHelper.agregarDiasHabiles(recojo.getFecha_disponible(), diasRecojo);

            if (ahora.isAfter(limite)) {
                System.out.println("[Scheduled] Pedido #" + p.getId() + " → NO_RECOGIDO (plazo de recojo vencido).");
                p.setEstado(Pedidos.Estado.NO_RECOGIDO);
                pedidoEstadoService.aplicarEfectosNoRecogido(p); // establece fecha_no_recogido + notifica
                pedidosService.guardar(p);
            }
        }
    }

    // ── Job B: NO_RECOGIDO → CANCELADO (cada 15 min) ────────────────
    // Cancela cuando fecha_no_recogido + días hábiles de cancelación ya venció.
    @Scheduled(fixedRate = 900000)
    public void cancelarNoRecogidos() {
        System.out.println("[Scheduled] Verificando pedidos NO_RECOGIDO para cancelar...");

        LocalDateTime ahora = LocalDateTime.now();

        List<Pedidos> candidatos = pedidosService.buscarTodos().stream()
                .filter(p -> p.getEstado() == Pedidos.Estado.NO_RECOGIDO
                          && p.getFecha_no_recogido() != null
                          && p.getEmpresa() != null)
                .collect(Collectors.toList());

        for (Pedidos p : candidatos) {
            int diasCancelar = configService.obtenerOCrearDefault(p.getEmpresa().getId())
                    .getDiasHabilesParaCancelarNoRecogido();
            LocalDateTime limite = DiasHabilesHelper.agregarDiasHabiles(p.getFecha_no_recogido(), diasCancelar);

            if (ahora.isAfter(limite)) {
                System.out.println("[Scheduled] Pedido #" + p.getId() + " → CANCELADO (no recogido en plazo).");
                p.setEstado(Pedidos.Estado.CANCELADO);
                pedidoEstadoService.aplicarEfectosCancelado(p,
                        "Pedido cancelado automáticamente por no ser recogido en plazo");
                pedidosService.guardar(p);

                // Notificación de cancelación (dedup en el service)
                notificacionesService.crear(
                        p.getUsuario(),
                        p,
                        Notificaciones.Tipo.NO_RECOGIDO_CANCELADO,
                        "Tu pedido #" + p.getId() + " ha sido cancelado por no ser recogido. "
                                + "Se iniciará tu reembolso a la brevedad."
                );
            }
        }
    }
}
