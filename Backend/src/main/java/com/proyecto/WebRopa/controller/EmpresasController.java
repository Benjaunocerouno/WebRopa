package com.proyecto.WebRopa.controller;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import com.proyecto.WebRopa.entity.Empresas;
import com.proyecto.WebRopa.service.IEmpresasService;
import com.proyecto.WebRopa.service.jpa.AuditoriaService;

@RestController
@RequestMapping("/api")
public class EmpresasController {
    private final IEmpresasService service;
    private final AuditoriaService auditoriaService;

    public EmpresasController(IEmpresasService service, AuditoriaService auditoriaService) {
        this.service = service;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping("/empresas")
    public List<Empresas> listarTodos() { return service.buscarTodos(); }

    @GetMapping("/empresas/{id}")
    public java.util.Optional<Empresas> buscarPorId(@PathVariable Long id) { return service.buscarId(id); }

    @PostMapping("/empresas")
    public Empresas guardar(@RequestBody Empresas entity) {
        service.guardar(entity);
        auditoriaService.registrar("CREAR_EMPRESA", "EMPRESA", "Se registró la empresa: " + entity.getRazonSocial() + " con RUC " + entity.getRuc());
        return entity;
    }

    @PutMapping("/empresas")
    public Empresas modificar(@RequestBody Empresas entity) {
        service.modificar(entity);
        String accion = entity.getEstado() == Empresas.Estado.ACTIVO ? "REACTIVAR_EMPRESA" : "SUSPENDER_EMPRESA";
        auditoriaService.registrar(accion, "EMPRESA", "Se cambió el estado de la empresa " + entity.getRazonSocial() + " (ID: " + entity.getId() + ") a " + entity.getEstado());
        return entity;
    }

    @DeleteMapping("/empresas/{id}")
    public void eliminar(@PathVariable Long id) {
        java.util.Optional<Empresas> empOpt = service.buscarId(id);
        service.eliminar(id);
        empOpt.ifPresent(entity -> 
            auditoriaService.registrar("ELIMINAR_EMPRESA", "EMPRESA", "Se eliminó la empresa " + entity.getRazonSocial() + " (ID: " + entity.getId() + ")")
        );
    }
}
