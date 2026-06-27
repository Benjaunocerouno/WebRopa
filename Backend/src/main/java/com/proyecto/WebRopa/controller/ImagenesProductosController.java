package com.proyecto.WebRopa.controller;

import java.util.List;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.proyecto.WebRopa.entity.ImagenesProductos;
import com.proyecto.WebRopa.service.IImagenesProductosService;

@RestController
@RequestMapping("/api/imagenes-producto")
public class ImagenesProductosController {

    private final IImagenesProductosService serviceImagenes;

    public ImagenesProductosController(IImagenesProductosService serviceImagenes) {
        this.serviceImagenes = serviceImagenes;
    }

    @GetMapping
    public List<ImagenesProductos> listarTodos() { return serviceImagenes.buscarTodos(); }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        Optional<ImagenesProductos> imagen = serviceImagenes.buscarId(id);
        return imagen.isPresent() ? ResponseEntity.ok(imagen.get()) : ResponseEntity.badRequest().body("No encontrada");
    }

    @PostMapping
    public ResponseEntity<?> guardar(@RequestBody ImagenesProductos imagen) {
        serviceImagenes.guardar(imagen);
        return ResponseEntity.ok("Imagen guardada");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modificar(@PathVariable Long id, @RequestBody ImagenesProductos imagen) {
        imagen.setId(id);
        try {
            serviceImagenes.modificar(imagen);
            return ResponseEntity.ok("Imagen actualizada");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        Optional<ImagenesProductos> existente = serviceImagenes.buscarId(id);
        if (!existente.isPresent()) {
            return ResponseEntity.badRequest().body("La imagen no existe o ya fue eliminada");
        }
        serviceImagenes.eliminar(id);
        return ResponseEntity.ok("Imagen eliminada");
    }
}