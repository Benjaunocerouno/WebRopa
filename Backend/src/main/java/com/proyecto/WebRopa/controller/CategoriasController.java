package com.proyecto.WebRopa.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyecto.WebRopa.entity.Categorias;
import com.proyecto.WebRopa.repository.ProductosRepository;
import com.proyecto.WebRopa.service.ICategoriasService;

@RestController
@RequestMapping("/api")
public class CategoriasController {

    private final ICategoriasService serviceCategorias;

    public CategoriasController(ICategoriasService serviceCategorias, ProductosRepository productosRepository) {
        this.serviceCategorias = serviceCategorias;
    }

    @GetMapping("/categorias")
    public List<Categorias> listarTodos() {
        return serviceCategorias.buscarTodos();
    }

    @GetMapping("/categorias/{id}")
    public Optional<Categorias> buscarPorId(@PathVariable Long id) {
        return serviceCategorias.buscarId(id);
    }

    @PostMapping("/categorias")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PRODUCTOS_CREAR')")
    public ResponseEntity<?> guardar(@RequestBody Categorias categoria) {
        if (categoria.getNombre() == null || categoria.getNombre().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El nombre de la categoría no puede estar vacío");
        }

        if (categoria.getNombre().matches(".*\\d.*")) {
            return ResponseEntity.badRequest().body("El nombre de la categoría no puede contener números");
        }

        boolean nombreExiste = serviceCategorias.buscarTodos().stream()
                .anyMatch(c -> c.getNombre().equalsIgnoreCase(categoria.getNombre().trim()));
        if (nombreExiste) {
            return ResponseEntity.badRequest().body("Ya existe una categoría con ese nombre");
        }

        categoria.setNombre(categoria.getNombre().trim());
        serviceCategorias.guardar(categoria);
        return ResponseEntity.ok(categoria);
    }

    @PutMapping("/categorias")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PRODUCTOS_EDITAR')")
    public Categorias modificar(@RequestBody Categorias categoria) {
        serviceCategorias.modificar(categoria);
        return categoria;
    }

    @DeleteMapping("/categorias/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PRODUCTOS_ELIMINAR')")
    public String eliminar(@PathVariable Long id) {

        // Verificar si la categoría existe
        Optional<Categorias> categoria = serviceCategorias.buscarId(id);
        if (!categoria.isPresent()) {
            return "La categoría no existe";
        }

        serviceCategorias.eliminar(id);
        return "Categoría, junto a sus productos y variantes, han sido eliminados correctamente";
    }
}