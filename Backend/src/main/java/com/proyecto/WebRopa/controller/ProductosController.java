package com.proyecto.WebRopa.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyecto.WebRopa.entity.Productos;
import com.proyecto.WebRopa.repository.VariantesRepository;
import com.proyecto.WebRopa.service.IProductosService;

@RestController
@RequestMapping("/api")
public class ProductosController {

    private final IProductosService serviceProductos;

    public ProductosController(IProductosService serviceProductos, VariantesRepository variantesRepository) {
        this.serviceProductos = serviceProductos;
    }

    @GetMapping("/productos")
    public List<Productos> listarTodos() {
        return serviceProductos.buscarTodos();
    }

    @GetMapping("/productos/empresa/{empresaId}")
    public List<Productos> listarPorEmpresa(@PathVariable Long empresaId) {
        return serviceProductos.buscarPorEmpresa(empresaId);
    }

    @GetMapping("/productos/{id}")
    public Optional<Productos> buscarPorId(@PathVariable Long id) {
        return serviceProductos.buscarId(id);
    }

    @PostMapping("/productos")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PRODUCTOS_CREAR')")
    public ResponseEntity<?> guardar(@RequestBody Productos producto) {
        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El nombre del producto es obligatorio");
        }

        boolean nombreExiste = serviceProductos.buscarTodos().stream()
                .anyMatch(p -> p.getNombre().equalsIgnoreCase(producto.getNombre().trim()));
        if (nombreExiste) {
            return ResponseEntity.badRequest().body("Ya existe un producto con ese nombre");
        }

        producto.setNombre(producto.getNombre().trim());
        serviceProductos.guardar(producto);
        return ResponseEntity.ok(producto);
    }

    @PutMapping("/productos")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PRODUCTOS_EDITAR')")
    public Productos modificar(@RequestBody Productos producto) {
        serviceProductos.modificar(producto);
        return producto;
    }

    @DeleteMapping("/productos/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PRODUCTOS_ELIMINAR')")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {

        // Verificar si el producto existe
        Optional<Productos> producto = serviceProductos.buscarId(id);
        if (!producto.isPresent()) {
            return ResponseEntity.badRequest().body("El producto no existe");
        }

        serviceProductos.eliminar(id);
        return ResponseEntity.ok("Producto y sus variantes asociados han sido eliminados correctamente");
    }
}