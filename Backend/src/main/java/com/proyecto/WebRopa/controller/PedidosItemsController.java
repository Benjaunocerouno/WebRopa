package com.proyecto.WebRopa.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyecto.WebRopa.entity.Cupones;
import com.proyecto.WebRopa.entity.Pedidos;
import com.proyecto.WebRopa.entity.PedidosItems;
import com.proyecto.WebRopa.entity.Variantes;
import com.proyecto.WebRopa.service.IPedidosItemsService;
import com.proyecto.WebRopa.service.IPedidosService;
import com.proyecto.WebRopa.service.IVariantesService;

@RestController
@RequestMapping("/api")
public class PedidosItemsController {

    private final IPedidosItemsService servicePedidosItems;
    private final IPedidosService servicePedidos;
    private final IVariantesService serviceVariantes;

    public PedidosItemsController(IPedidosItemsService servicePedidosItems, IPedidosService servicePedidos, IVariantesService serviceVariantes) {
        this.servicePedidosItems = servicePedidosItems;
        this.servicePedidos = servicePedidos;
        this.serviceVariantes = serviceVariantes;
    }

    // Ver todos los items de un pedido
    @GetMapping("/pedidoitems/pedido/{pedidoId}")
    public ResponseEntity<?> buscarPorPedido(@PathVariable Long pedidoId) {
        List<PedidosItems> items = servicePedidosItems.buscarPorPedidoId(pedidoId);
        if (items.isEmpty()) {
            return ResponseEntity.badRequest().body("No hay items para ese pedido");
        }
        return ResponseEntity.ok(items);
    }

    // Ver un item específico
    @GetMapping("/pedidoitems/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        Optional<PedidosItems> item = servicePedidosItems.buscarId(id);
        if (!item.isPresent()) {
            return ResponseEntity.badRequest().body("El item no existe");
        }
        return ResponseEntity.ok(item.get());
    }

    // ── Agregar nuevo ítem al pedido (POST) ──────────
    @PostMapping("/pedidoitems")
    public ResponseEntity<?> agregarItem(@RequestBody PedidosItems item) {
        if (item.getPedido() == null || item.getPedido().getId() == null) {
            return ResponseEntity.badRequest().body("Debe especificar el pedido");
        }
        if (item.getVariante() == null || item.getVariante().getId() == null) {
            return ResponseEntity.badRequest().body("Debe especificar la variante");
        }
        if (item.getCantidad() == null || item.getCantidad() <= 0) {
            return ResponseEntity.badRequest().body("La cantidad debe ser mayor a 0");
        }

        Optional<Pedidos> pedidoOpt = servicePedidos.buscarId(item.getPedido().getId());
        if (!pedidoOpt.isPresent()) {
            return ResponseEntity.badRequest().body("El pedido no existe");
        }
        Pedidos pedido = pedidoOpt.get();

        // Regla: Solo si está en PENDIENTE
        if (pedido.getEstado() != Pedidos.Estado.PENDIENTE) {
            return ResponseEntity.badRequest()
                .body("Solo se pueden agregar ítems a pedidos en estado PENDIENTE");
        }

        Optional<Variantes> varianteOpt = serviceVariantes.buscarId(item.getVariante().getId());
        if (!varianteOpt.isPresent()) {
            return ResponseEntity.badRequest().body("La variante no existe");
        }
        Variantes variante = varianteOpt.get();

        if (variante.getStock() < item.getCantidad()) {
            return ResponseEntity.badRequest().body("Stock insuficiente de la variante seleccionada");
        }

        // Descontar stock
        variante.setStock(variante.getStock() - item.getCantidad());
        serviceVariantes.guardar(variante);

        // Congelar precio y guardar
        item.setPrecio_unitario(variante.getProducto().getPrecio());
        servicePedidosItems.guardar(item);

        recalcularTotales(pedido);

        return ResponseEntity.ok(item);
    }

    // ── Modificar cantidad de un ítem (PUT) ──────────
    @PutMapping("/pedidoitems")
    public ResponseEntity<?> modificarCantidad(@RequestBody PedidosItems itemUpdate) {
        if (itemUpdate.getId() == null) {
            return ResponseEntity.badRequest().body("Debe especificar el id del ítem");
        }
        if (itemUpdate.getCantidad() == null || itemUpdate.getCantidad() <= 0) {
            return ResponseEntity.badRequest().body("La cantidad debe ser mayor a 0");
        }

        Optional<PedidosItems> existenteOpt = servicePedidosItems.buscarId(itemUpdate.getId());
        if (!existenteOpt.isPresent()) {
            return ResponseEntity.badRequest().body("El ítem no existe");
        }
        
        PedidosItems existente = existenteOpt.get();
        Pedidos pedido = existente.getPedido();

        // Regla: No permitir si ya fue pagado/tiene boleta
        if (pedido.isPago_confirmado()) {
            return ResponseEntity.badRequest()
                .body("No se puede modificar un pedido que ya ha sido pagado o tiene boleta generada");
        }

        Variantes variante = existente.getVariante();
        int diferencia = itemUpdate.getCantidad() - existente.getCantidad();

        // Ajustar stock según diferencia
        if (diferencia > 0 && variante.getStock() < diferencia) {
            return ResponseEntity.badRequest().body("Stock insuficiente para aumentar la cantidad");
        }

        variante.setStock(variante.getStock() - diferencia);
        serviceVariantes.guardar(variante);

        existente.setCantidad(itemUpdate.getCantidad());
        servicePedidosItems.guardar(existente);

        recalcularTotales(pedido);

        return ResponseEntity.ok(existente);
    }

    // ── Eliminar ítem (DELETE) ───────────────────────
    @DeleteMapping("/pedidoitems/{id}")
    public ResponseEntity<?> eliminarItem(@PathVariable Long id) {
        Optional<PedidosItems> existenteOpt = servicePedidosItems.buscarId(id);
        if (!existenteOpt.isPresent()) {
            return ResponseEntity.badRequest().body("El ítem no existe");
        }
        
        PedidosItems existente = existenteOpt.get();
        Pedidos pedido = existente.getPedido();

        // Regla: No permitir si ya fue pagado/tiene boleta
        if (pedido.isPago_confirmado()) {
            return ResponseEntity.badRequest()
                .body("No se puede eliminar un ítem de un pedido que ya ha sido pagado");
        }

        // Restaurar stock
        Variantes variante = existente.getVariante();
        variante.setStock(variante.getStock() + existente.getCantidad());
        serviceVariantes.guardar(variante);

        servicePedidosItems.eliminar(id);

        recalcularTotales(pedido);

        return ResponseEntity.ok("Ítem eliminado y pedido recalculado");
    }

    // ── Helper: Recalcular Totales ───────────────────
    private void recalcularTotales(Pedidos pedido) {
        List<PedidosItems> items = servicePedidosItems.buscarPorPedidoId(pedido.getId());
        
        double subtotal = 0.0;
        for (PedidosItems i : items) {
            subtotal += i.getCantidad() * i.getPrecio_unitario();
        }
        pedido.setSubtotal(subtotal);

        // Recalcular descuento del cupón dinámicamente según el nuevo subtotal
        double descuento = 0.0;
        if (pedido.getCupon() != null) {
            if (pedido.getCupon().getTipo() == Cupones.Tipo.PORCENTAJE) {
                descuento = subtotal * (pedido.getCupon().getValor() / 100.0);
            } else {
                descuento = pedido.getCupon().getValor();
            }
        } else {
            descuento = pedido.getDescuento();
        }

        pedido.setDescuento(descuento);
        
        double total = subtotal - descuento;
        // Asegurarse de que el total no quede negativo si el descuento fijo es mayor al subtotal
        pedido.setTotal(total < 0 ? 0 : total);

        servicePedidos.guardar(pedido);
    }
}