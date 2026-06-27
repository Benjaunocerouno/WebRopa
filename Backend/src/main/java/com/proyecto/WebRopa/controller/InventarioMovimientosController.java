package com.proyecto.WebRopa.controller;

import java.util.List;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.proyecto.WebRopa.entity.InventarioMovimientos;
import com.proyecto.WebRopa.service.IInventarioMovimientosService;

@RestController
@RequestMapping("/api/inventario-movimientos")
public class InventarioMovimientosController {

    private final IInventarioMovimientosService serviceMovimientos;

    public InventarioMovimientosController(IInventarioMovimientosService serviceMovimientos) {
        this.serviceMovimientos = serviceMovimientos;
    }

    @GetMapping
    public List<InventarioMovimientos> listarTodos() { return serviceMovimientos.buscarTodos(); }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        Optional<InventarioMovimientos> movimiento = serviceMovimientos.buscarId(id);
        return movimiento.isPresent() ? ResponseEntity.ok(movimiento.get()) : ResponseEntity.badRequest().body("No encontrado");
    }

    @PostMapping
    public ResponseEntity<?> guardar(@RequestBody InventarioMovimientos movimiento) {
        if (movimiento.getVariante() == null || movimiento.getVariante().getId() == null || movimiento.getCantidad() == 0) {
            return ResponseEntity.badRequest().body("Debe especificar el ID de la variante y una cantidad distinta de 0");
        }
        if (movimiento.getTipo_movimiento() == null) {
            return ResponseEntity.badRequest().body("Debe especificar el tipo de movimiento (ej. INGRESO_COMPRA, SALIDA_VENTA, AJUSTE_MANUAL)");
        }
        
        try {
            serviceMovimientos.guardar(movimiento);
            return ResponseEntity.ok("Movimiento de inventario registrado y stock actualizado.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modificar(@PathVariable Long id, @RequestBody InventarioMovimientos movimiento) {
        movimiento.setId(id);
        try {
            serviceMovimientos.modificar(movimiento);
            return ResponseEntity.ok("Observación del movimiento actualizada");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            serviceMovimientos.eliminar(id);
            return ResponseEntity.ok("Movimiento anulado correctamente y stock revertido.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}