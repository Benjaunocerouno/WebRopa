package com.proyecto.WebRopa.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyecto.WebRopa.entity.Productos;
import com.proyecto.WebRopa.entity.Variantes;
import com.proyecto.WebRopa.service.IVariantesService;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class VariantesController {
 
    private final IVariantesService serviceVariantes;

    public VariantesController(IVariantesService serviceVariantes) {
        this.serviceVariantes = serviceVariantes;
    }

    @GetMapping("/variantes")
    public List<Variantes> listarTodos(
            @RequestParam(value = "incluirInactivos", required = false, defaultValue = "false") boolean incluirInactivos) {
        List<Variantes> lista = serviceVariantes.buscarTodos();
        if (incluirInactivos) {
            return lista;
        }
        return lista.stream()
                .filter(v -> v.getEstado() == Variantes.Estado.ACTIVO)
                .collect(Collectors.toList());
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

    @PutMapping("/variantes/{id}/activar")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PRODUCTOS_EDITAR')")
    public ResponseEntity<?> activarVariante(@PathVariable Long id) {
        Optional<Variantes> varOpt = serviceVariantes.buscarId(id);
        if (!varOpt.isPresent()) {
            return ResponseEntity.badRequest().body("La variante no existe");
        }
        Variantes variante = varOpt.get();

        // Validación: Impedir activar variante de un producto inactivo
        if (variante.getProducto() != null && variante.getProducto().getEstado() == Productos.Estado.INACTIVO) {
            return ResponseEntity.badRequest()
                    .body("No se puede habilitar esta variante porque el producto principal está deshabilitado. Por favor, habilite el producto primero.");
        }

        variante.setEstado(Variantes.Estado.ACTIVO);
        serviceVariantes.modificar(variante);
        return ResponseEntity.ok(variante);
    }
}