package com.proyecto.WebRopa.controller;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import com.proyecto.WebRopa.entity.Empresas;
import com.proyecto.WebRopa.service.IEmpresasService;

@RestController
@RequestMapping("/api")
public class EmpresasController {
    private final IEmpresasService service;
    public EmpresasController(IEmpresasService service) { this.service = service; }
    @GetMapping("/empresas")
    public List<Empresas> listarTodos() { return service.buscarTodos(); }
    @GetMapping("/empresas/{id}")
    public java.util.Optional<Empresas> buscarPorId(@PathVariable Long id) { return service.buscarId(id); }
    @PostMapping("/empresas")
    public Empresas guardar(@RequestBody Empresas entity) { service.guardar(entity); return entity; }
    @PutMapping("/empresas")
    public Empresas modificar(@RequestBody Empresas entity) { service.modificar(entity); return entity; }
    @DeleteMapping("/empresas/{id}")
    public void eliminar(@PathVariable Long id) { service.eliminar(id); }
}
