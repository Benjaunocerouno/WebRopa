package com.proyecto.WebRopa.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyecto.WebRopa.entity.Pedidos;
import com.proyecto.WebRopa.entity.EnvioDomicilio;
import com.proyecto.WebRopa.entity.Usuarios;
import com.proyecto.WebRopa.service.IPedidosService;
import com.proyecto.WebRopa.service.IEnvioDomicilioService;
import com.proyecto.WebRopa.service.IUsuariosService;

@RestController
@RequestMapping("/api")
public class EnvioDomicilioController {

    private final IEnvioDomicilioService serviceEnvio;
    private final IPedidosService servicePedidos;
    private final IUsuariosService serviceUsuarios;

    public EnvioDomicilioController(IEnvioDomicilioService serviceEnvio, IPedidosService servicePedidos, IUsuariosService serviceUsuarios) {
        this.serviceEnvio = serviceEnvio;
        this.servicePedidos = servicePedidos;
        this.serviceUsuarios = serviceUsuarios;
    }

    // ── GET todos ────────────────────────────────────
    @GetMapping("/enviodomicilio")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PEDIDOS_GESTIONAR')")
    public List<EnvioDomicilio> listarTodos() {
        return serviceEnvio.buscarTodos();
    }

    // ── GET por id ───────────────────────────────────
    @GetMapping("/enviodomicilio/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PEDIDOS_GESTIONAR')")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        Optional<EnvioDomicilio> envio = serviceEnvio.buscarId(id);
        if (!envio.isPresent()) {
            return ResponseEntity.badRequest().body("El envío no existe");
        }
        return ResponseEntity.ok(envio.get());
    }

    // ── GET por pedidoId ──────────────────────────────
    @GetMapping("/enviodomicilio/pedido/{pedidoId}")
    public ResponseEntity<?> buscarPorPedidoId(@PathVariable Long pedidoId) {
        Optional<EnvioDomicilio> envio = serviceEnvio.buscarPorPedidoId(pedidoId);
        if (!envio.isPresent()) {
            return ResponseEntity.badRequest().body("El envío no existe para este pedido");
        }
        return ResponseEntity.ok(envio.get());
    }

    // ── POST ─────────────────────────────────────────
    @PostMapping("/enviodomicilio")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PEDIDOS_GESTIONAR')")
    public ResponseEntity<?> guardar(@RequestBody EnvioDomicilio envio) {
        if (envio.getPedido() == null || envio.getPedido().getId() == null) {
            return ResponseEntity.badRequest().body("Debe especificar el pedido");
        }
        if (envio.getCodigo_seguimiento() == null || envio.getCodigo_seguimiento().isEmpty()) {
            return ResponseEntity.badRequest().body("Debe especificar el código de seguimiento");
        }

        Optional<Pedidos> pedidoOpt = servicePedidos.buscarId(envio.getPedido().getId());
        if (!pedidoOpt.isPresent()) {
            return ResponseEntity.badRequest().body("El pedido especificado no existe");
        }

        serviceEnvio.guardar(envio);
        return ResponseEntity.ok(envio);
    }

    // ── PUT ──────────────────────────────────────────
    @PutMapping("/enviodomicilio")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PEDIDOS_GESTIONAR')")
    public ResponseEntity<?> modificar(@RequestBody EnvioDomicilio envio) {
        if (envio.getId() == null) {
            return ResponseEntity.badRequest().body("Debe especificar el id del envío");
        }
        Optional<EnvioDomicilio> existenteOpt = serviceEnvio.buscarId(envio.getId());
        if (!existenteOpt.isPresent()) {
            return ResponseEntity.badRequest().body("El envío no existe");
        }
        EnvioDomicilio existente = existenteOpt.get();

        // Actualizar campos permitidos
        if (envio.getEstado() != null) {
            existente.setEstado(envio.getEstado());
        }
        if (envio.getFecha_envio() != null) {
            existente.setFecha_envio(envio.getFecha_envio());
        }
        if (envio.getFecha_entrega() != null) {
            existente.setFecha_entrega(envio.getFecha_entrega());
        }

        // Si cambia a EN_CAMINO y no tiene fecha_envio, setearla
        if (existente.getEstado() == EnvioDomicilio.Estado.EN_CAMINO && existente.getFecha_envio() == null) {
            existente.setFecha_envio(java.time.LocalDateTime.now());
        }

        // Si cambia a ENTREGADO y no tiene fecha_entrega, setearla
        if (existente.getEstado() == EnvioDomicilio.Estado.ENTREGADO && existente.getFecha_entrega() == null) {
            existente.setFecha_entrega(java.time.LocalDateTime.now());
        }

        // Si se actualiza el empleado que despacha
        if (envio.getDespachado_por() != null && envio.getDespachado_por().getId() != null) {
            Optional<Usuarios> empleadoOpt = serviceUsuarios.buscarId(envio.getDespachado_por().getId());
            if (!empleadoOpt.isPresent()) {
                return ResponseEntity.badRequest().body("El empleado especificado no existe");
            }
            Usuarios empleado = empleadoOpt.get();
            if (empleado.getRol() == null || 
               (!empleado.getRol().getNombre().equalsIgnoreCase("EMPLEADO") && 
                !empleado.getRol().getNombre().equalsIgnoreCase("ADMIN"))) {
                return ResponseEntity.badRequest().body("El usuario especificado no tiene rol de empleado/admin");
            }
            existente.setDespachado_por(empleado);
        }

        try {
            serviceEnvio.modificar(existente);
            return ResponseEntity.ok(existente);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ── DELETE (borrado lógico -> DEVUELTO) ───────────
    @DeleteMapping("/enviodomicilio/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PEDIDOS_GESTIONAR')")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        Optional<EnvioDomicilio> existente = serviceEnvio.buscarId(id);
        if (!existente.isPresent()) {
            return ResponseEntity.badRequest().body("El envío no existe");
        }
        if (existente.get().getEstado() == EnvioDomicilio.Estado.ENTREGADO) {
            return ResponseEntity.badRequest().body("No se puede cancelar un envío que ya fue completado");
        }
        serviceEnvio.eliminar(id);
        return ResponseEntity.ok("Envío marcado como DEVUELTO");
    }
}
