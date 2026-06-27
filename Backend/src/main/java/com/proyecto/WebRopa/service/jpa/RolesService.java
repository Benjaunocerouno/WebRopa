package com.proyecto.WebRopa.service.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.proyecto.WebRopa.entity.Roles;
import com.proyecto.WebRopa.repository.RolesRepository;
import com.proyecto.WebRopa.service.IRolesServices;

@Service
public class RolesService implements IRolesServices {
    
    private final RolesRepository repoRoles;
    
    public RolesService(RolesRepository repoRoles) {
        this.repoRoles = repoRoles;
    }

    public List<Roles> buscarTodos() { return repoRoles.findAll(); }
    public Optional<Roles> buscarId(Long id) { return repoRoles.findById(id); }
    public Optional<Roles> buscarPorNombre(String nombre) { return repoRoles.findByNombre(nombre); }
    public void guardar(Roles rol) { repoRoles.save(rol); }
    public void modificar(Roles rol) { repoRoles.save(rol); }
    public void eliminar(Long id) { repoRoles.deleteById(id); }
}
