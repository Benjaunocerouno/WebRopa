package com.proyecto.WebRopa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyecto.WebRopa.entity.Carritos;
import com.proyecto.WebRopa.entity.Usuarios;
import com.proyecto.WebRopa.service.ICarritosService;
import com.proyecto.WebRopa.service.IUsuariosService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class CarritosController {


    private final ICarritosService serviceCarritos;
    private final IUsuariosService serviceUsuarios;

    public CarritosController(ICarritosService serviceCarritos, IUsuariosService serviceUsuarios) {
        this.serviceCarritos = serviceCarritos;
        this.serviceUsuarios = serviceUsuarios;
    }


    @GetMapping("/carritos")
    public List<Carritos> listarTodos() {
        return serviceCarritos.buscarTodos();
    }

    // Ver el carrito de un usuario
    @GetMapping("/carritos/usuario/{usuarioId}")
    public ResponseEntity<?> verCarrito(@PathVariable Long usuarioId) {

        Optional<Carritos> carrito = serviceCarritos.buscarPorUsuarioId(usuarioId);

        if (!carrito.isPresent()) {
            return ResponseEntity.badRequest()
                    .body("El usuario no tiene un carrito asignado");
        }

        return ResponseEntity.ok(carrito.get());
    }

    // Crear carrito para un usuario (se llama al registrarse)
    @PostMapping("/carritos/usuario/{usuarioId}")
    public ResponseEntity<?> crearCarrito(@PathVariable Long usuarioId) {

        // Verificar que el usuario existe
        Optional<Usuarios> usuario = serviceUsuarios.buscarId(usuarioId);
        if (!usuario.isPresent()) {
            return ResponseEntity.badRequest().body("El usuario no existe");
        }

        // Verificar que no tenga ya un carrito
        Optional<Carritos> carritoExistente = serviceCarritos.buscarPorUsuarioId(usuarioId);
        if (carritoExistente.isPresent()) {
            return ResponseEntity.badRequest().body("El usuario ya tiene un carrito");
        }

        Carritos carrito = new Carritos();
        carrito.setUsuario(usuario.get());
        serviceCarritos.guardar(carrito);

        return ResponseEntity.ok(carrito);
    }

    // Reactivar carrito inactivo
    @PutMapping("/carritos/{id}/reactivar")
    public ResponseEntity<?> reactivarCarrito(@PathVariable Long id) {
        try {
            serviceCarritos.reactivarCarrito(id);
            return ResponseEntity.ok("Carrito reactivado correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @DeleteMapping("/carritos/{id}")
    public ResponseEntity<?> eliminarCarrito(@PathVariable Long id) {
        Optional<Carritos> carrito = serviceCarritos.buscarId(id);
        if (!carrito.isPresent()) {
            return ResponseEntity.badRequest().body("El carrito no existe");
        }
        serviceCarritos.eliminar(id);
        return ResponseEntity.ok("Carrito eliminado");
    }
}