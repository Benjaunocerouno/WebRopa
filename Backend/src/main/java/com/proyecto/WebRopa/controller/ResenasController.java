package com.proyecto.WebRopa.controller;

import java.util.List;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.proyecto.WebRopa.entity.Productos;
import com.proyecto.WebRopa.entity.Resenas;
import com.proyecto.WebRopa.service.IProductosService;
import com.proyecto.WebRopa.service.IResenasService;

@RestController
@RequestMapping("/api/resenas") // Simplificamos la ruta base para todos los endpoints
public class ResenasController {

    private final IResenasService serviceResenas;
    private final IProductosService serviceProductos;

    public ResenasController(IResenasService serviceResenas, IProductosService serviceProductos) {
        this.serviceResenas = serviceResenas;
        this.serviceProductos = serviceProductos;
    }

    // 1. GET Todos
    @GetMapping
    public List<Resenas> listarTodos() {
        return serviceResenas.buscarTodos();
    }

    // 2. GET por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        Optional<Resenas> resena = serviceResenas.buscarId(id);
        if (!resena.isPresent()) {
            return ResponseEntity.badRequest().body("La reseña no existe");
        }
        return ResponseEntity.ok(resena.get());
    }

    // 3. POST
    @PostMapping
    public ResponseEntity<?> guardar(@RequestBody Resenas resena) {
        if (resena.getUsuario() == null || resena.getUsuario().getId() == null || 
            resena.getProducto() == null || resena.getProducto().getId() == null) {
            return ResponseEntity.badRequest().body("Debe especificar el ID del usuario y del producto.");
        }
        
        Optional<Productos> productoOpt = serviceProductos.buscarId(resena.getProducto().getId());
        // Si tu entidad usa un campo estado="ACTIVO", puedes agregar: || !productoOpt.get().getEstado().equals("ACTIVO")
        if (!productoOpt.isPresent()) {
            return ResponseEntity.badRequest().body("El producto no existe o se encuentra inactivo.");
        }

        try {
            serviceResenas.guardar(resena);
            return ResponseEntity.ok("Reseña creada con éxito. Pendiente de aprobación.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 4. PUT
    @PutMapping
    public ResponseEntity<?> modificar(@RequestBody Resenas resena) {
        if (resena.getId() == null) {
            return ResponseEntity.badRequest().body("Debe especificar el ID de la reseña a modificar.");
        }
        Optional<Resenas> existente = serviceResenas.buscarId(resena.getId());
        if (!existente.isPresent()) {
            return ResponseEntity.badRequest().body("La reseña no existe.");
        }
        
        try {
            serviceResenas.modificar(resena);
            return ResponseEntity.ok("Reseña actualizada con éxito. Requiere nueva aprobación.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 5. DELETE (Borrado Físico)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        Optional<Resenas> existente = serviceResenas.buscarId(id);
        if (!existente.isPresent()) {
            return ResponseEntity.badRequest().body("La reseña no existe.");
        }
        serviceResenas.eliminar(id);
        return ResponseEntity.ok("Reseña eliminada correctamente.");
    }

    // 6. PUT - Aprobar reseña (Solo Admin)
    @PutMapping("/{id}/aprobar")
    public ResponseEntity<?> aprobar(@PathVariable Long id) {
        Optional<Resenas> existente = serviceResenas.buscarId(id);
        if (!existente.isPresent()) {
            return ResponseEntity.badRequest().body("La reseña no existe.");
        }
        
        try {
            serviceResenas.aprobar(id);
            return ResponseEntity.ok("Reseña aprobada y visible para el público.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}