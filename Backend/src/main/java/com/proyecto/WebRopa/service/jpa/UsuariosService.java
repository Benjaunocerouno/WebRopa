package com.proyecto.WebRopa.service.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.proyecto.WebRopa.entity.Usuarios;
import com.proyecto.WebRopa.repository.UsuariosRepository;
import com.proyecto.WebRopa.service.IUsuariosService;

@Service
public class UsuariosService implements IUsuariosService {

    private final UsuariosRepository repoUsuarios;

    public UsuariosService(UsuariosRepository repoUsuarios) {
        this.repoUsuarios = repoUsuarios;
    }

    
    public List<Usuarios> buscarTodos() { 
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            return repoUsuarios.findByEmpresaId(tenantId);
        }
        return repoUsuarios.findAll(); 
    }
    public void guardar(Usuarios entity) { 
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null && entity.getEmpresa() == null) {
            com.proyecto.WebRopa.entity.Empresas emp = new com.proyecto.WebRopa.entity.Empresas();
            emp.setId(tenantId);
            entity.setEmpresa(emp);
        }
        repoUsuarios.save(entity); 
    }
    public void modificar(Usuarios entity) { 
        guardar(entity); 
    }
    public Optional<Usuarios> buscarId(Long id) { 
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            return repoUsuarios.findByIdAndEmpresaId(id, tenantId);
        }
        return repoUsuarios.findById(id); 
    }
    public void eliminar(Long id) { 
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            Optional<Usuarios> ent = repoUsuarios.findByIdAndEmpresaId(id, tenantId);
            if (ent.isPresent()) {
                repoUsuarios.deleteById(id);
            }
        } else {
            repoUsuarios.deleteById(id); 
        }
    }
    public Optional<Usuarios> buscarPorCorreo(String correo) {
        return repoUsuarios.findByCorreo(correo);
    }
}

