package com.proyecto.WebRopa.controller;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.WebRopa.service.seguridad.SeguridadEnVivoService;
import com.proyecto.WebRopa.service.jpa.AuditoriaService;

@RestController
@RequestMapping("/api/superadmin/seguridad")
public class SuperAdminSeguridadController {

    @Autowired
    private SeguridadEnVivoService seguridadService;

    @Autowired
    private AuditoriaService auditoriaService;

    // 1. Obtener sesiones activas
    @GetMapping("/sesiones")
    public ResponseEntity<List<SeguridadEnVivoService.SesionInfo>> obtenerSesionesActivas() {
        return ResponseEntity.ok(seguridadService.obtenerSesionesActivas());
    }

    // 2. Cerrar sesión por ID
    @PostMapping("/sesiones/{id}/cerrar")
    public ResponseEntity<?> cerrarSesion(@PathVariable String id) {
        seguridadService.cerrarSesionPorId(id);
        auditoriaService.registrar("REVOCAR_SESION", "SESION", "Se forzó el cierre de la sesión ID: " + id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Sesión cerrada correctamente");
        return ResponseEntity.ok(response);
    }

    // 3. Revocar acceso por ID
    @PostMapping("/sesiones/{id}/revocar")
    public ResponseEntity<?> revocarSesion(@PathVariable String id) {
        seguridadService.revocarSesionPorId(id);
        auditoriaService.registrar("REVOCAR_SESION", "SESION", "Se revocó el acceso de la sesión ID: " + id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Acceso revocado correctamente");
        return ResponseEntity.ok(response);
    }
}
