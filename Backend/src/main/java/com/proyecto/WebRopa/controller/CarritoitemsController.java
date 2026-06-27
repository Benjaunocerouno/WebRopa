package com.proyecto.WebRopa.controller;

import com.proyecto.WebRopa.entity.Carritositems;
import com.proyecto.WebRopa.service.ICarritos_itemsService;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CarritoitemsController {
    
    private final ICarritos_itemsService serviceCarritositems;

    public CarritoitemsController(ICarritos_itemsService serviceCarritositems) {
        this.serviceCarritositems = serviceCarritositems;
    }

    @GetMapping("/carritoitems")
    public List<Carritositems> ListarTodos() {
        return serviceCarritositems.buscarTodos();
    }

    @PostMapping("/carritoitems")
    public ResponseEntity<?> guardar(@RequestBody Carritositems carritositem) {

        if (carritositem.getCarrito() == null || carritositem.getCarrito().getId() == null) {
            return ResponseEntity.badRequest().body("Debe especificar un carrito");
        }

        if (carritositem.getVariante() == null || carritositem.getVariante().getId() == null) {
            return ResponseEntity.badRequest().body("Debe especificar una variante");
        }

        if (carritositem.getCantidad() == null || carritositem.getCantidad() <= 0) {
            return ResponseEntity.badRequest().body("La cantidad debe ser mayor a 0");
        }

        // ── Verificar que no exista ya esa variante en el carrito ──
        Optional<Carritositems> itemExistente = serviceCarritositems
            .buscarPorCarritoYVariante(
                carritositem.getCarrito().getId(),
                carritositem.getVariante().getId()
            );
        if (itemExistente.isPresent()) {
            return ResponseEntity.badRequest()
                .body("Esa variante ya está en el carrito, usa PUT para modificar la cantidad");
        }

        serviceCarritositems.guardar(carritositem);
        Optional<Carritositems> resultado = serviceCarritositems.buscarId(carritositem.getId());
        return ResponseEntity.ok(resultado.get());
    }

    @PutMapping("/carritoitems")
    public ResponseEntity<?> modificar(@RequestBody Carritositems carritositem) {

        if (carritositem.getId() == 0) {
            return ResponseEntity.badRequest().body("Debe especificar el id del item");
        }

        Optional<Carritositems> existente = serviceCarritositems.buscarId(carritositem.getId());
        if (!existente.isPresent()) {
            return ResponseEntity.badRequest().body("El item no existe");
        }

        if (carritositem.getCantidad() == null || carritositem.getCantidad() <= 0) {
            return ResponseEntity.badRequest().body("La cantidad debe ser mayor a 0");
        }

        serviceCarritositems.modificar(carritositem);
        return ResponseEntity.ok(carritositem);
    }

    @DeleteMapping("/carritoitems/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {

        Optional<Carritositems> existente = serviceCarritositems.buscarId(id);
        if (!existente.isPresent()) {
            return ResponseEntity.badRequest().body("El item no existe");
        }

        serviceCarritositems.eliminar(id);
        return ResponseEntity.ok("Item eliminado");
    }

    @GetMapping("/carritoitems/{id}")
    public ResponseEntity<?> buscarId(@PathVariable Long id) {

        Optional<Carritositems> existente = serviceCarritositems.buscarId(id);
        if (!existente.isPresent()) {
            return ResponseEntity.badRequest().body("El item no existe");
        }

        return ResponseEntity.ok(existente.get());
    }
}
