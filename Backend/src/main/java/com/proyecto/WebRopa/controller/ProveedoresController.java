package com.proyecto.WebRopa.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyecto.WebRopa.entity.Proveedores;
import com.proyecto.WebRopa.service.IProveedoresService;

@RestController
@RequestMapping("/api/proveedores")
public class ProveedoresController {

    private final IProveedoresService serviceProveedores;

    public ProveedoresController(IProveedoresService serviceProveedores) {
        this.serviceProveedores = serviceProveedores;
    }

    @GetMapping
    public ResponseEntity<List<Proveedores>> listar() {
        List<Proveedores> lista = serviceProveedores.buscarTodos();
        return new ResponseEntity<>(lista, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Proveedores> obtenerPorId(@PathVariable Long id) {
        Optional<Proveedores> proveedor = serviceProveedores.buscarId(id);
        return proveedor.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Proveedores> crear(@RequestBody Proveedores proveedor) {
        serviceProveedores.guardar(proveedor);
        return new ResponseEntity<>(proveedor, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Proveedores> actualizar(@PathVariable Long id, @RequestBody Proveedores proveedor) {
        Optional<Proveedores> proveedorExistente = serviceProveedores.buscarId(id);

        if (proveedorExistente.isPresent()) {
            proveedor.setId(id);
            
            // Forzar que el RUC no pueda ser modificado (se mantiene el original)
            proveedor.setRuc(proveedorExistente.get().getRuc());

            if (proveedor.getEmpresa() == null) {
                proveedor.setEmpresa(proveedorExistente.get().getEmpresa());
            }

            serviceProveedores.modificar(proveedor);
            return new ResponseEntity<>(proveedor, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        Optional<Proveedores> proveedorExistente = serviceProveedores.buscarId(id);
        if (proveedorExistente.isPresent()) {
            serviceProveedores.eliminar(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<java.util.Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("message", ex.getMessage()));
    }
}