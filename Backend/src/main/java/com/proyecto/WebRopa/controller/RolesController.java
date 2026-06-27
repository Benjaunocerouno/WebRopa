package com.proyecto.WebRopa.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyecto.WebRopa.entity.Roles;
import com.proyecto.WebRopa.service.IRolesServices;

@RestController
@RequestMapping("/api")
public class RolesController {

    private final IRolesServices serviceRoles;

    public RolesController(IRolesServices serviceRoles) {
        this.serviceRoles = serviceRoles;
    }

    @GetMapping("/roles")
    public List<Roles> listarTodos() {
        return serviceRoles.buscarTodos();
    }

    @GetMapping("/roles/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        Optional<Roles> rol = serviceRoles.buscarId(id);
        if (!rol.isPresent()) {
            return ResponseEntity.badRequest().body("El rol no existe");
        }
        return ResponseEntity.ok(rol.get());
    }

    @PostMapping("/roles")
    public ResponseEntity<?> guardar(@RequestBody Roles rol) {
        serviceRoles.guardar(rol);
        return ResponseEntity.ok(rol);
    }

    @PutMapping("/roles")
    public ResponseEntity<?> modificar(@RequestBody Roles rolUpdate) {
        if (rolUpdate.getId() == null) {
            return ResponseEntity.badRequest().body("Debe especificar el id del rol");
        }
        Optional<Roles> existente = serviceRoles.buscarId(rolUpdate.getId());
        if (!existente.isPresent()) {
            return ResponseEntity.badRequest().body("El rol no existe");
        }
        serviceRoles.modificar(rolUpdate);
        return ResponseEntity.ok(rolUpdate);
    }

    @DeleteMapping("/roles/{id}")
    public String eliminar(@PathVariable Long id) {
        serviceRoles.eliminar(id);
        return "Rol eliminado correctamente";
    }
}