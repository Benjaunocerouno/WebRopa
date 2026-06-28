package com.proyecto.WebRopa.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyecto.WebRopa.entity.Devoluciones;
import com.proyecto.WebRopa.entity.PedidosItems;
import com.proyecto.WebRopa.service.IDevolucionesService;
import com.proyecto.WebRopa.service.IPedidosItemsService;

@RestController
@RequestMapping("/api")
public class DevolucionesController {

    private final IDevolucionesService serviceDevoluaciones;
    private final IPedidosItemsService servicePedidosItems;

    public DevolucionesController(
            IDevolucionesService serviceDevoluaciones,
            IPedidosItemsService servicePedidosItems) {
        this.serviceDevoluaciones = serviceDevoluaciones;
        this.servicePedidosItems = servicePedidosItems;
    }

    // ── Ver todas (admin) ────────────────────────────
    @GetMapping("/devoluciones")
    public List<Devoluciones> listarTodos() {
        return serviceDevoluaciones.buscarTodos();
    }

    // ── Ver devoluciones de un pedido ────────────────
    @GetMapping("/devoluciones/pedido/{pedidoId}")
    public ResponseEntity<?> listarPorPedido(@PathVariable Long pedidoId) {
        List<Devoluciones> devs = serviceDevoluaciones.buscarPorPedidoId(pedidoId);
        if (devs.isEmpty()) {
            return ResponseEntity.badRequest().body("No hay devoluciones para ese pedido");
        }
        return ResponseEntity.ok(devs);
    }

    // ── Ver devoluciones por estado (admin) ──────────
    @GetMapping("/devoluciones/estado/{estado}")
    public ResponseEntity<?> listarPorEstado(@PathVariable String estado) {
        try {
            Devoluciones.Estado estadoEnum = Devoluciones.Estado.valueOf(estado.toUpperCase().trim());
            List<Devoluciones> devs = serviceDevoluaciones.buscarPorEstado(estadoEnum);
            return ResponseEntity.ok(devs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body("Estado inválido. Valores: SOLICITADA, APROBADA, RECHAZADA, REEMBOLSADA");
        }
    }

    // ── Ver una devolución específica ────────────────
    @GetMapping("/devoluciones/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        Optional<Devoluciones> dev = serviceDevoluaciones.buscarId(id);
        if (!dev.isPresent()) {
            return ResponseEntity.badRequest().body("La devolución no existe");
        }
        return ResponseEntity.ok(dev.get());
    }

    // ── Crear devolución (cliente o empleado) ────────
    @PostMapping("/devoluciones")
    public ResponseEntity<?> crear(@RequestBody Devoluciones devolucion) {

        if (devolucion.getPedido() == null || devolucion.getPedido().getId() == null) {
            return ResponseEntity.badRequest().body("Debe especificar el pedido");
        }

        if (devolucion.getPedidoItem() == null || devolucion.getPedidoItem().getId() == null) {
            return ResponseEntity.badRequest().body("Debe especificar el item del pedido");
        }

        if (devolucion.getMotivo() == null) {
            return ResponseEntity.badRequest()
                .body("Debe especificar el motivo: DEFECTUOSO, TALLA_INCORRECTA, COLOR_INCORRECTO, OTRO");
        }

        if (devolucion.getCantidad_devuelta() == null || devolucion.getCantidad_devuelta() <= 0) {
            return ResponseEntity.badRequest().body("La cantidad devuelta debe ser mayor a 0");
        }

        // Verificar que el item existe y calcular monto automáticamente
        Optional<PedidosItems> item = servicePedidosItems.buscarId(devolucion.getPedidoItem().getId());
        if (!item.isPresent()) {
            return ResponseEntity.badRequest().body("El item del pedido no existe");
        }

        // Verificar que no devuelva más de lo que compró
        if (devolucion.getCantidad_devuelta() > item.get().getCantidad()) {
            return ResponseEntity.badRequest()
                .body("La cantidad devuelta no puede ser mayor a la comprada (" 
                      + item.get().getCantidad() + ")");
        }

        // Calcular monto automáticamente
        double monto = item.get().getPrecio_unitario() * devolucion.getCantidad_devuelta();
        devolucion.setMonto_reembolso(monto);
        devolucion.setEstado(Devoluciones.Estado.SOLICITADA);

        serviceDevoluaciones.guardar(devolucion);
        return ResponseEntity.ok(devolucion);
    }

    // ── Actualizar estado (admin) ────────────────────
    // Transiciones válidas:
    //   SOLICITADA → APROBADA   (admin aprueba la solicitud)
    //   SOLICITADA → RECHAZADA  (admin rechaza la solicitud)
    //   APROBADA   → REEMBOLSADA (admin confirma que el dinero fue devuelto manualmente)
    @PutMapping("/devoluciones/{id}")
    public ResponseEntity<?> actualizarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        Optional<Devoluciones> devOpt = serviceDevoluaciones.buscarId(id);
        if (!devOpt.isPresent()) {
            return ResponseEntity.badRequest().body("La devolución no existe");
        }

        String nuevoEstadoStr = body.get("estado");
        if (nuevoEstadoStr == null) {
            return ResponseEntity.badRequest().body("Debe especificar el estado");
        }

        try {
            Devoluciones.Estado nuevoEstado = Devoluciones.Estado.valueOf(nuevoEstadoStr.toUpperCase().trim());
            Devoluciones.Estado estadoActual = devOpt.get().getEstado();

            switch (nuevoEstado) {
                case APROBADA -> {
                    if (estadoActual != Devoluciones.Estado.SOLICITADA) {
                        return ResponseEntity.badRequest()
                            .body("Solo se puede aprobar una devolución en estado SOLICITADA");
                    }
                    serviceDevoluaciones.aprobarDevolucion(id);
                    return ResponseEntity.ok("Devolución aprobada — pendiente de reembolso físico");
                }
                case RECHAZADA -> {
                    if (estadoActual != Devoluciones.Estado.SOLICITADA) {
                        return ResponseEntity.badRequest()
                            .body("Solo se puede rechazar una devolución en estado SOLICITADA");
                    }
                    serviceDevoluaciones.rechazarDevolucion(id);
                    return ResponseEntity.ok("Devolución rechazada");
                }
                case REEMBOLSADA -> {
                    if (estadoActual != Devoluciones.Estado.APROBADA) {
                        return ResponseEntity.badRequest()
                            .body("Solo se puede marcar como reembolsada una devolución en estado APROBADA");
                    }
                    serviceDevoluaciones.reembolsarDevolucion(id);
                    return ResponseEntity.ok("Devolución reembolsada y pago marcado como REEMBOLSADO");
                }
                default -> {
                    return ResponseEntity.badRequest()
                        .body("Transición inválida. Válidas: SOLICITADA→APROBADA, SOLICITADA→RECHAZADA, APROBADA→REEMBOLSADA");
                }
            }

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body("Estado inválido. Valores aceptados: APROBADA, RECHAZADA, REEMBOLSADA");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ── Reembolsar todas las devoluciones de un pedido cancelado ──
    // Útil cuando un admin cancela un pedido ya pagado: procesa todas las
    // devoluciones pendientes (SOLICITADA o APROBADA) en un solo paso.
    @PutMapping("/devoluciones/pedido/{pedidoId}/reembolsar-todo")
    public ResponseEntity<?> reembolsarTodoPorPedido(@PathVariable Long pedidoId) {
        try {
            serviceDevoluaciones.reembolsarTodasPorPedido(pedidoId);
            return ResponseEntity.ok("Todas las devoluciones del pedido marcadas como REEMBOLSADA y pago actualizado");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ── Cancelar devolución (cliente se arrepiente) ──
    @DeleteMapping("/devoluciones/{id}")
    public ResponseEntity<?> cancelar(@PathVariable Long id) {
        Optional<Devoluciones> dev = serviceDevoluaciones.buscarId(id);
        if (!dev.isPresent()) {
            return ResponseEntity.badRequest().body("La devolución no existe");
        }
        if (dev.get().getEstado() != Devoluciones.Estado.SOLICITADA) {
            return ResponseEntity.badRequest()
                .body("Solo se pueden cancelar devoluciones en estado SOLICITADA");
        }
        serviceDevoluaciones.eliminar(id);
        return ResponseEntity.ok("Devolución cancelada");
    }
}