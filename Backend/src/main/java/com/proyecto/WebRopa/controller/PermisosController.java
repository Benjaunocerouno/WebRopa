package com.proyecto.WebRopa.controller;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import com.proyecto.WebRopa.entity.Permisos;
import com.proyecto.WebRopa.service.IPermisosService;

@RestController
@RequestMapping("/api")
public class PermisosController {
    private final IPermisosService service;
    public PermisosController(IPermisosService service) { this.service = service; }
    @GetMapping("/permisos")
    public List<Permisos> listarTodos() { return service.buscarTodos(); }
    @PostMapping("/permisos")
    public Permisos guardar(@RequestBody Permisos entity) { service.guardar(entity); return entity; }
    @PutMapping("/permisos")
    public Permisos modificar(@RequestBody Permisos entity) { service.modificar(entity); return entity; }
    @DeleteMapping("/permisos/{id}")
    public void eliminar(@PathVariable Long id) { service.eliminar(id); }
}
