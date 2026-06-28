package com.proyecto.WebRopa.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyecto.WebRopa.entity.ConfiguracionDespacho;
import com.proyecto.WebRopa.service.IConfiguracionDespachoService;

@RestController
@RequestMapping("/api")
public class ConfiguracionDespachoController {

    private final IConfiguracionDespachoService service;

    public ConfiguracionDespachoController(IConfiguracionDespachoService service) {
        this.service = service;
    }

    // ── Ver configuración de una empresa (crea default si no existe) ──
    @GetMapping("/configuracion-despacho/{empresaId}")
    public ResponseEntity<?> obtener(@PathVariable Long empresaId) {
        ConfiguracionDespacho config = service.obtenerOCrearDefault(empresaId);
        return ResponseEntity.ok(config);
    }

    // ── Actualizar plazos de una empresa ──────────────
    @PutMapping("/configuracion-despacho/{empresaId}")
    public ResponseEntity<?> actualizar(
            @PathVariable Long empresaId,
            @RequestBody Map<String, Integer> body) {

        Integer diasRecojo    = body.get("dias_habiles_para_recojo");
        Integer diasCancelar  = body.get("dias_habiles_para_cancelar_no_recogido");

        if (diasRecojo == null || diasCancelar == null) {
            return ResponseEntity.badRequest()
                .body("Debe enviar dias_habiles_para_recojo y dias_habiles_para_cancelar_no_recogido");
        }
        if (diasRecojo < 1 || diasCancelar < 1) {
            return ResponseEntity.badRequest()
                .body("Los plazos deben ser al menos 1 día hábil");
        }

        ConfiguracionDespacho config = service.actualizar(empresaId, diasRecojo, diasCancelar);
        return ResponseEntity.ok(config);
    }
}
