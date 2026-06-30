package com.proyecto.WebRopa.service.jpa;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.proyecto.WebRopa.entity.Sucursales;
import com.proyecto.WebRopa.repository.SucursalesRepository;
import com.proyecto.WebRopa.service.ISucursalesService;

@Service
public class SucursalesService implements ISucursalesService {
    private final SucursalesRepository repo;
    
    public SucursalesService(SucursalesRepository repo) { this.repo = repo; }
    
    public List<Sucursales> buscarTodos() {
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            return repo.findByEmpresaId(tenantId);
        }
        return repo.findAll();
    }
    
    public List<Sucursales> buscarPorEmpresa(Long empresaId) {
        return repo.findByEmpresaId(empresaId);
    }
    
    public void guardar(Sucursales entity) {
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null && entity.getEmpresa() == null) {
            com.proyecto.WebRopa.entity.Empresas emp = new com.proyecto.WebRopa.entity.Empresas();
            emp.setId(tenantId);
            entity.setEmpresa(emp);
        }
        repo.save(entity);
    }
    
    public void modificar(Sucursales entity) {
        guardar(entity);
    }
    
    public Optional<Sucursales> buscarId(Long id) {
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            return repo.findByIdAndEmpresaId(id, tenantId);
        }
        return repo.findById(id);
    }
    
    public void eliminar(Long id) {
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            Optional<Sucursales> ent = repo.findByIdAndEmpresaId(id, tenantId);
            if (ent.isPresent()) {
                repo.deleteById(id);
            }
        } else {
            repo.deleteById(id);
        }
    }
}
