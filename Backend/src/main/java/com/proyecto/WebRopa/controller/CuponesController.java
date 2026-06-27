package com.proyecto.WebRopa.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.WebRopa.entity.Cupones;
import com.proyecto.WebRopa.service.ICuponesService;

@RestController
@RequestMapping("/api")
public class CuponesController {

    private final ICuponesService serviceCupones;
    public CuponesController(ICuponesService serviceCupones) {
        this.serviceCupones = serviceCupones;
    }
    
    @GetMapping("/cupones")
    public List<Cupones> listarTodos() {
        return serviceCupones.buscarTodos();
    }

    @GetMapping("/cupones/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        Optional<Cupones> cupon = serviceCupones.buscarId(id);
        if (!cupon.isPresent()) {
            return ResponseEntity.badRequest().body("El cupón no existe");
        }
        return ResponseEntity.ok(cupon.get());
    }

    @PostMapping("/cupones")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PEDIDOS_GESTIONAR')")
    public ResponseEntity<?> guardar(@RequestBody Cupones cupon) {

        if (cupon.getCodigo() == null || cupon.getCodigo().isEmpty()) {
            return ResponseEntity.badRequest().body("El código es obligatorio");
        }

        if (cupon.getTipo() == null) {
            return ResponseEntity.badRequest().body("El tipo es obligatorio: PORCENTAJE o MONTO_FIJO");
        }

        if (cupon.getValor() == null || cupon.getValor() <= 0) {
            return ResponseEntity.badRequest().body("El valor debe ser mayor a 0");
        }

        // Verificar que el código no exista ya
        boolean codigoExiste = serviceCupones.buscarTodos().stream()
                .anyMatch(c -> c.getCodigo().equalsIgnoreCase(cupon.getCodigo().trim()));
        if (codigoExiste) {
            return ResponseEntity.badRequest().body("El código de cupón ya existe");
        }

        // Validar que no tenga categoría Y producto al mismo tiempo
        if (cupon.getCategoria() != null && cupon.getProducto() != null) {
            return ResponseEntity.badRequest()
                .body("Un cupón no puede aplicar a categoría y producto al mismo tiempo");
        }
        if (cupon.getUsos_maximos() == null || cupon.getUsos_maximos() <= 0) {
            return ResponseEntity.badRequest().body("Debe especificar un límite de usos mayor a 0");
        }

        serviceCupones.guardar(cupon);
        return ResponseEntity.ok(cupon);
    }

    @PutMapping("/cupones")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PEDIDOS_GESTIONAR')")
    public ResponseEntity<?> modificar(@RequestBody Cupones cupon) {
        if (cupon.getId() == null) {
            return ResponseEntity.badRequest().body("Debe especificar el id del cupón");
        }
        Optional<Cupones> existente = serviceCupones.buscarId(cupon.getId());
        if (!existente.isPresent()) {
            return ResponseEntity.badRequest().body("El cupón no existe");
        }

        // Validar que no tenga categoría Y producto al mismo tiempo
        if (cupon.getCategoria() != null && cupon.getProducto() != null) {
            return ResponseEntity.badRequest()
                .body("Un cupón no puede aplicar a categoría y producto al mismo tiempo");
        }

        serviceCupones.modificar(cupon);
        return ResponseEntity.ok(cupon);
    }

    @DeleteMapping("/cupones/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PEDIDOS_GESTIONAR')")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        Optional<Cupones> existente = serviceCupones.buscarId(id);
        if (!existente.isPresent()) {
            return ResponseEntity.badRequest().body("El cupón no existe");
        }
        serviceCupones.eliminar(id);
        return ResponseEntity.ok("Cupón eliminado");
    }

}
