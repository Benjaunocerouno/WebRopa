package com.proyecto.WebRopa.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyecto.WebRopa.entity.Pedidos;
import com.proyecto.WebRopa.entity.RecojoTienda;
import com.proyecto.WebRopa.entity.Usuarios;
import com.proyecto.WebRopa.service.IPedidosService;
import com.proyecto.WebRopa.service.IRecojoTiendaService;
import com.proyecto.WebRopa.service.IUsuariosService;

@RestController
@RequestMapping("/api")
public class RecojoTiendaController {

    private final IRecojoTiendaService serviceRecojoTienda;
    private final IPedidosService servicePedidos;
    private final IUsuariosService serviceUsuarios;

    public RecojoTiendaController(IRecojoTiendaService serviceRecojoTienda, IPedidosService servicePedidos, IUsuariosService serviceUsuarios) {
        this.serviceRecojoTienda = serviceRecojoTienda;
        this.servicePedidos = servicePedidos;
        this.serviceUsuarios = serviceUsuarios;
    }

    // ── GET todos ────────────────────────────────────
    @GetMapping("/recojotienda")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PEDIDOS_GESTIONAR')")
    public List<RecojoTienda> listarTodos() {
        return serviceRecojoTienda.buscarTodos();
    }

    // ── GET por id ───────────────────────────────────
    @GetMapping("/recojotienda/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PEDIDOS_GESTIONAR')")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        Optional<RecojoTienda> recojo = serviceRecojoTienda.buscarId(id);
        if (!recojo.isPresent()) {
            return ResponseEntity.badRequest().body("El recojo no existe");
        }
        return ResponseEntity.ok(recojo.get());
    }

    // ── GET por pedidoId ──────────────────────────────
    @GetMapping("/recojotienda/pedido/{pedidoId}")
    public ResponseEntity<?> buscarPorPedidoId(@PathVariable Long pedidoId) {
        Optional<RecojoTienda> recojo = serviceRecojoTienda.buscarPorPedidoId(pedidoId);
        if (!recojo.isPresent()) {
            return ResponseEntity.badRequest().body("El recojo no existe para este pedido");
        }
        return ResponseEntity.ok(recojo.get());
    }

    // ── POST ─────────────────────────────────────────
    @PostMapping("/recojotienda")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PEDIDOS_GESTIONAR')")
    public ResponseEntity<?> guardar(@RequestBody RecojoTienda recojo) {
        if (recojo.getPedido() == null || recojo.getPedido().getId() == null) {
            return ResponseEntity.badRequest().body("Debe especificar el pedido");
        }
        if (recojo.getCodigo_recojo() == null || recojo.getCodigo_recojo().isEmpty()) {
            return ResponseEntity.badRequest().body("Debe especificar el código de recojo");
        }

        Optional<Pedidos> pedidoOpt = servicePedidos.buscarId(recojo.getPedido().getId());
        if (!pedidoOpt.isPresent()) {
            return ResponseEntity.badRequest().body("El pedido especificado no existe");
        }

        serviceRecojoTienda.guardar(recojo);
        return ResponseEntity.ok(recojo);
    }

    // ── PUT ──────────────────────────────────────────
    @PutMapping("/recojotienda")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PEDIDOS_GESTIONAR')")
    public ResponseEntity<?> modificar(@RequestBody RecojoTienda recojo) {
        if (recojo.getId() == null) {
            return ResponseEntity.badRequest().body("Debe especificar el id");
        }
        Optional<RecojoTienda> existenteOpt = serviceRecojoTienda.buscarId(recojo.getId());
        if (!existenteOpt.isPresent()) {
            return ResponseEntity.badRequest().body("El recojo no existe");
        }

        RecojoTienda existente = existenteOpt.get();

        // Si se proporciona atendido_por, validar que sea empleado o admin
        if (recojo.getAtendido_por() != null && recojo.getAtendido_por().getId() != null) {
            Optional<Usuarios> empleadoOpt = serviceUsuarios.buscarId(recojo.getAtendido_por().getId());
            if (!empleadoOpt.isPresent()) {
                return ResponseEntity.badRequest().body("El empleado especificado no existe");
            }
            Usuarios empleado = empleadoOpt.get();
            if (empleado.getRol() == null ||
               (!empleado.getRol().getNombre().equalsIgnoreCase("EMPLEADO") &&
                !empleado.getRol().getNombre().equalsIgnoreCase("ADMIN"))) {
                return ResponseEntity.badRequest()
                    .body("El usuario especificado no tiene rol de empleado");
            }
            existente.setAtendido_por(empleado);
        }

        if (recojo.getFecha_disponible() != null) existente.setFecha_disponible(recojo.getFecha_disponible());
        if (recojo.getFecha_recogido() != null)  existente.setFecha_recogido(recojo.getFecha_recogido());
        if (recojo.getCodigo_recojo() != null)   existente.setCodigo_recojo(recojo.getCodigo_recojo());

        serviceRecojoTienda.modificar(existente);
        return ResponseEntity.ok(existente);
    }

    // ── DELETE ───────────────────────────────────────
    @DeleteMapping("/recojotienda/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PEDIDOS_GESTIONAR')")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        Optional<RecojoTienda> existente = serviceRecojoTienda.buscarId(id);
        if (!existente.isPresent()) {
            return ResponseEntity.badRequest().body("El recojo no existe");
        }
        if (existente.get().getPedido().getEstado() == Pedidos.Estado.RECOGIDO) {
            return ResponseEntity.badRequest()
                .body("No se puede eliminar el recojo de un pedido ya completado");
        }
        serviceRecojoTienda.eliminar(id);
        return ResponseEntity.ok("Recojo eliminado");
    }
}