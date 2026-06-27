package com.proyecto.WebRopa.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyecto.WebRopa.entity.Variantes;
import com.proyecto.WebRopa.service.IVariantesService;

@RestController
@RequestMapping("/api")
public class VariantesController {
 
    private final IVariantesService serviceVariantes;

    public VariantesController(IVariantesService serviceVariantes) {
        this.serviceVariantes = serviceVariantes;
    }

    @GetMapping("/variantes")
    public List<Variantes> listarTodos() {
        return serviceVariantes.buscarTodos();
    }

    @GetMapping("/variantes/{id}")
    public Optional<Variantes> buscarPorId(@PathVariable Long id) {
        return serviceVariantes.buscarId(id);
    }
    
    @PostMapping("/variantes")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PRODUCTOS_EDITAR')")
    public ResponseEntity<?> guardar(@RequestBody Variantes variante) {
        
        // Si no mandaron el objeto producto o mandaron sin id
        if (variante.getProducto() == null || variante.getProducto().getId() == null) {
            return ResponseEntity
                    .badRequest()
                    .body("La variante debe estar asociada a un producto");
        }

        serviceVariantes.guardar(variante);
        return ResponseEntity.ok(variante);
    }

    @PutMapping("/variantes")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PRODUCTOS_EDITAR')")
    public Variantes modificar(@RequestBody Variantes variante) {
        serviceVariantes.modificar(variante);
        return variante;
    }

    @DeleteMapping("/variantes/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PRODUCTOS_EDITAR')")
    public String eliminar(@PathVariable Long id) {
        serviceVariantes.eliminar(id);
        return "Variante eliminada";
    }
}