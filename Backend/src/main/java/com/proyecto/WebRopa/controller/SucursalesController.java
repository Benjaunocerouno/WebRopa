package com.proyecto.WebRopa.controller;

import java.util.List;
import java.util.Optional;
import org.springframework.web.bind.annotation.*;
import com.proyecto.WebRopa.entity.Sucursales;
import com.proyecto.WebRopa.service.ISucursalesService;

@RestController
@RequestMapping("/api")
public class SucursalesController {
    private final ISucursalesService service;
    
    public SucursalesController(ISucursalesService service) { this.service = service; }
    
    @GetMapping("/sucursales")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('ADMIN') or hasAuthority('PERSONAL_GESTIONAR')")
    public List<Sucursales> listarTodos() { return service.buscarTodos(); }
    
    @GetMapping("/sucursales/empresa/{empresaId}")
    public List<Sucursales> listarPorEmpresa(@PathVariable Long empresaId) {
        return service.buscarPorEmpresa(empresaId);
    }
    
    @GetMapping("/sucursales/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('ADMIN') or hasAuthority('PERSONAL_GESTIONAR')")
    public Optional<Sucursales> buscarPorId(@PathVariable Long id) { return service.buscarId(id); }
    
    @PostMapping("/sucursales")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('ADMIN') or hasAuthority('PERSONAL_GESTIONAR')")
    public Sucursales guardar(@RequestBody Sucursales entity) { service.guardar(entity); return entity; }
    
    @PutMapping("/sucursales")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('ADMIN') or hasAuthority('PERSONAL_GESTIONAR')")
    public Sucursales modificar(@RequestBody Sucursales entity) { service.modificar(entity); return entity; }
    
    @DeleteMapping("/sucursales/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('ADMIN') or hasAuthority('PERSONAL_GESTIONAR')")
    public void eliminar(@PathVariable Long id) { service.eliminar(id); }
}
