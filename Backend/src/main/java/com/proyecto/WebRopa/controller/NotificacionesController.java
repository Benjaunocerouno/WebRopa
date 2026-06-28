package com.proyecto.WebRopa.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyecto.WebRopa.entity.Notificaciones;
import com.proyecto.WebRopa.service.INotificacionesService;

@RestController
@RequestMapping("/api")
public class NotificacionesController {

    private final INotificacionesService service;

    public NotificacionesController(INotificacionesService service) {
        this.service = service;
    }

    // ── Listar notificaciones de un usuario (más recientes primero) ──
    @GetMapping("/notificaciones/usuario/{usuarioId}")
    public ResponseEntity<?> listarPorUsuario(@PathVariable Long usuarioId) {
        List<Notificaciones> lista = service.buscarPorUsuarioId(usuarioId);
        return ResponseEntity.ok(lista);
    }

    // ── Marcar una notificación como leída ───────────
    @PutMapping("/notificaciones/{id}/leida")
    public ResponseEntity<?> marcarLeida(@PathVariable Long id) {
        service.marcarComoLeida(id);
        return ResponseEntity.ok("Notificación marcada como leída");
    }
}
