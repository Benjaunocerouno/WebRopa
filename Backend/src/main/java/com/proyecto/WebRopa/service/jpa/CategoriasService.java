package com.proyecto.WebRopa.service.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.proyecto.WebRopa.entity.Categorias;
import com.proyecto.WebRopa.repository.CategoriasRepository;
import com.proyecto.WebRopa.service.ICategoriasService;

@Service
public class CategoriasService implements ICategoriasService {

    
    private final CategoriasRepository repoCategorias;
    public CategoriasService(CategoriasRepository repoCategorias) {
        this.repoCategorias = repoCategorias;
    }

    
    public List<Categorias> buscarTodos() { 
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            return repoCategorias.findByEmpresaId(tenantId);
        }
        return repoCategorias.findAll(); 
    }
    public void guardar(Categorias entity) { 
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null && entity.getEmpresa() == null) {
            com.proyecto.WebRopa.entity.Empresas emp = new com.proyecto.WebRopa.entity.Empresas();
            emp.setId(tenantId);
            entity.setEmpresa(emp);
        }
        repoCategorias.save(entity); 
    }
    public void modificar(Categorias entity) { 
        guardar(entity); 
    }
    public Optional<Categorias> buscarId(Long id) { 
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            return repoCategorias.findByIdAndEmpresaId(id, tenantId);
        }
        return repoCategorias.findById(id); 
    }
    public void eliminar(Long id) { 
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            Optional<Categorias> ent = repoCategorias.findByIdAndEmpresaId(id, tenantId);
            if (ent.isPresent()) {
                repoCategorias.deleteById(id);
            }
        } else {
            repoCategorias.deleteById(id); 
        }
    }
}

