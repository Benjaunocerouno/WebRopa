package com.proyecto.WebRopa.service.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.proyecto.WebRopa.entity.Cupones;
import com.proyecto.WebRopa.repository.CuponesRepository;
import com.proyecto.WebRopa.service.ICuponesService;

@Service
public class CuponesService implements ICuponesService {
    
    private final CuponesRepository repoCupones;
    public CuponesService(CuponesRepository repoCupones) {
        this.repoCupones = repoCupones;
    }

    
    public List<Cupones> buscarTodos() { 
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            return repoCupones.findByEmpresaId(tenantId);
        }
        return repoCupones.findAll(); 
    }
    public void guardar(Cupones entity) { 
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null && entity.getEmpresa() == null) {
            com.proyecto.WebRopa.entity.Empresas emp = new com.proyecto.WebRopa.entity.Empresas();
            emp.setId(tenantId);
            entity.setEmpresa(emp);
        }
        repoCupones.save(entity); 
    }
    public void modificar(Cupones entity) { 
        guardar(entity); 
    }
    public Optional<Cupones> buscarId(Long id) { 
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            return repoCupones.findByIdAndEmpresaId(id, tenantId);
        }
        return repoCupones.findById(id); 
    }
    public void eliminar(Long id) { 
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            Optional<Cupones> ent = repoCupones.findByIdAndEmpresaId(id, tenantId);
            if (ent.isPresent()) {
                repoCupones.deleteById(id);
            }
        } else {
            repoCupones.deleteById(id); 
        }
    }
}

