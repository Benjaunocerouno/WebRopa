package com.proyecto.WebRopa.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

import com.proyecto.WebRopa.entity.CuponUsos;
import com.proyecto.WebRopa.service.ICuponUsosService;

@RestController
@RequestMapping("/api")
public class CuponUsosController {

    private final ICuponUsosService serviceCuponUsos;
    public CuponUsosController(ICuponUsosService serviceCuponUsos) {
        this.serviceCuponUsos = serviceCuponUsos;
    }

    // Ver todos los usos (para auditoría del admin)
    @GetMapping("/cuponusos")
    public List<CuponUsos> listarTodos() {
        return serviceCuponUsos.buscarTodos();
    }

    // Verificar si un usuario ya usó un cupón específico
    @GetMapping("/cuponusos/cupon/{cuponId}/usuario/{usuarioId}")
    public ResponseEntity<?> verificarUso(
            @PathVariable Long cuponId,
            @PathVariable Long usuarioId) {

        Optional<CuponUsos> uso = serviceCuponUsos.buscarPorCuponYUsuario(cuponId, usuarioId);

        if (uso.isPresent()) {
            return ResponseEntity.ok("El usuario ya usó este cupón el " + uso.get().getFecha());
        }

        return ResponseEntity.ok("El usuario no ha usado este cupón");
    }

    // Modificar observación (nota) del uso de un cupón
    @PutMapping("/cuponusos")
    public ResponseEntity<?> actualizarNota(@RequestBody CuponUsos cuponUsoUpdate) {
        if (cuponUsoUpdate.getId() == null) {
            return ResponseEntity.badRequest().body("Debe especificar el id del uso de cupón");
        }

        Optional<CuponUsos> existente = serviceCuponUsos.buscarId(cuponUsoUpdate.getId());
        if (!existente.isPresent()) {
            return ResponseEntity.badRequest().body("El registro de uso de cupón no existe");
        }
        
        serviceCuponUsos.actualizarNota(cuponUsoUpdate.getId(), cuponUsoUpdate.getObservacion());
        return ResponseEntity.ok("Observación actualizada correctamente");
    }

    // Eliminar uso (revertir)
    @DeleteMapping("/cuponusos/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        Optional<CuponUsos> existente = serviceCuponUsos.buscarId(id);
        if (!existente.isPresent()) {
            return ResponseEntity.badRequest().body("El uso de cupón no existe");
        }
        
        serviceCuponUsos.eliminar(id);
        return ResponseEntity.ok("Uso de cupón revertido correctamente");
    }

}