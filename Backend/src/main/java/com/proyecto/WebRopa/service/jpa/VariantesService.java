package com.proyecto.WebRopa.service.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.proyecto.WebRopa.entity.Variantes;
import com.proyecto.WebRopa.repository.VariantesRepository;
import com.proyecto.WebRopa.service.IVariantesService;

@Service
public class VariantesService implements IVariantesService {
    
    private final VariantesRepository repoVariantes;
    public VariantesService(VariantesRepository repoVariantes) {
        this.repoVariantes = repoVariantes;
    }

    
    public List<Variantes> buscarTodos() { 
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            return repoVariantes.findByEmpresaId(tenantId);
        }
        return repoVariantes.findAll(); 
    }
    public void guardar(Variantes entity) { 
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null && entity.getEmpresa() == null) {
            com.proyecto.WebRopa.entity.Empresas emp = new com.proyecto.WebRopa.entity.Empresas();
            emp.setId(tenantId);
            entity.setEmpresa(emp);
        }
        repoVariantes.save(entity); 
    }
    public void modificar(Variantes entity) { 
        guardar(entity); 
    }
    public Optional<Variantes> buscarId(Long id) { 
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            return repoVariantes.findByIdAndEmpresaId(id, tenantId);
        }
        return repoVariantes.findById(id); 
    }
    public void eliminar(Long id) { 
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            Optional<Variantes> ent = repoVariantes.findByIdAndEmpresaId(id, tenantId);
            if (ent.isPresent()) {
                repoVariantes.deleteById(id);
            }
        } else {
            repoVariantes.deleteById(id); 
        }
    }
}

