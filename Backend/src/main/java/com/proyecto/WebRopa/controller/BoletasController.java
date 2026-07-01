package com.proyecto.WebRopa.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyecto.WebRopa.entity.Boletas;
import com.proyecto.WebRopa.entity.Pedidos;
import com.proyecto.WebRopa.service.IBoletasService;
import com.proyecto.WebRopa.service.IPedidosService;

@RestController
@RequestMapping("/api")
public class BoletasController {

    private final IBoletasService serviceBoletas;
    private final IPedidosService servicePedidos;

    public BoletasController(IBoletasService serviceBoletas, IPedidosService servicePedidos) {
        this.serviceBoletas = serviceBoletas;
        this.servicePedidos = servicePedidos;
    }

    // ── GET todos ────────────────────────────────────
    @GetMapping("/boletas")
    public List<Boletas> listarTodos() {
        return serviceBoletas.buscarTodos();
    }

    // ── GET por id ───────────────────────────────────
    @GetMapping("/boletas/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        Optional<Boletas> boleta = serviceBoletas.buscarId(id);
        if (!boleta.isPresent()) {
            return ResponseEntity.badRequest().body("La boleta no existe");
        }
        return ResponseEntity.ok(boleta.get());
    }

    // ── GET boletas por pedido (comprador) ───────────
    @GetMapping("/boletas/pedido/{pedidoId}")
    public ResponseEntity<?> buscarPorPedido(@PathVariable Long pedidoId) {
        return ResponseEntity.ok(serviceBoletas.buscarPorPedidoId(pedidoId));
    }

    // ── POST ─────────────────────────────────────────
    @PostMapping("/boletas")
    public ResponseEntity<?> guardar(@RequestBody Boletas boleta) {
        if (boleta.getPedido() == null || boleta.getPedido().getId() == null) {
            return ResponseEntity.badRequest().body("Debe especificar el pedido");
        }
        if (boleta.getNumero_boleta() == null || boleta.getNumero_boleta().isEmpty()) {
            return ResponseEntity.badRequest().body("Debe especificar el número de boleta");
        }
        if (boleta.getNombre_cliente() == null || boleta.getNombre_cliente().isEmpty()) {
            return ResponseEntity.badRequest().body("Debe especificar el nombre del cliente");
        }
        serviceBoletas.guardar(boleta);
        return ResponseEntity.ok(boleta);
    }

    // ── PUT (solo dni y nombre) ───────────────────────
    @PutMapping("/boletas")
    public ResponseEntity<?> modificar(@RequestBody Boletas boleta) {
        if (boleta.getId() == null) {
            return ResponseEntity.badRequest().body("Debe especificar el id");
        }
        Optional<Boletas> existente = serviceBoletas.buscarId(boleta.getId());
        if (!existente.isPresent()) {
            return ResponseEntity.badRequest().body("La boleta no existe");
        }
        try {
            serviceBoletas.modificar(boleta);
            return ResponseEntity.ok("Boleta actualizada");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ── DELETE (borrado lógico → ANULADA) ────────────
    @DeleteMapping("/boletas/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        Optional<Boletas> existente = serviceBoletas.buscarId(id);
        if (!existente.isPresent()) {
            return ResponseEntity.badRequest().body("La boleta no existe");
        }
        serviceBoletas.eliminar(id);
        return ResponseEntity.ok("Boleta anulada correctamente");
    }

    // ── Generar boletas faltantes para pedidos pagados ─
    @org.springframework.transaction.annotation.Transactional
    @PostMapping("/boletas/sincronizar")
    public ResponseEntity<?> sincronizar() {
        int generadas = 0;
        for (Pedidos pedido : servicePedidos.buscarTodos()) {
            if (!pedido.isPago_confirmado()) continue;
            if (!serviceBoletas.buscarPorPedidoId(pedido.getId()).isEmpty()) continue;

            String nombreCliente = pedido.getUsuario() != null ? pedido.getUsuario().getNombre() : null;
            if (nombreCliente == null || nombreCliente.isBlank()) nombreCliente = "Cliente No Registrado";

            Boletas boleta = new Boletas();
            boleta.setPedido(pedido);
            boleta.setNombre_cliente(nombreCliente);
            boleta.setNumero_boleta("BOL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            boleta.setSubtotal(pedido.getSubtotal());
            boleta.setIgv(pedido.getSubtotal() * 0.18);
            boleta.setTotal(pedido.getTotal());
            serviceBoletas.guardar(boleta);
            generadas++;
        }
        return ResponseEntity.ok(java.util.Map.of("generadas", generadas));
    }
}