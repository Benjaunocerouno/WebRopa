package com.proyecto.WebRopa.service.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.proyecto.WebRopa.entity.Pedidos;
import com.proyecto.WebRopa.repository.PedidosRepository;
import com.proyecto.WebRopa.service.IPedidosService;

@Service
public class PedidosService implements IPedidosService {

    private final PedidosRepository repoPedidos;
    public PedidosService(PedidosRepository repoPedidos) {
        this.repoPedidos = repoPedidos;
    }

    
    public List<Pedidos> buscarTodos() { 
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            return repoPedidos.findByEmpresaId(tenantId);
        }
        return repoPedidos.findAll(); 
    }
    public void guardar(Pedidos entity) { 
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null && entity.getEmpresa() == null) {
            com.proyecto.WebRopa.entity.Empresas emp = new com.proyecto.WebRopa.entity.Empresas();
            emp.setId(tenantId);
            entity.setEmpresa(emp);
        }
        repoPedidos.save(entity); 
    }
    public void modificar(Pedidos entity) { 
        guardar(entity); 
    }
    public Optional<Pedidos> buscarId(Long id) { 
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            return repoPedidos.findByIdAndEmpresaId(id, tenantId);
        }
        return repoPedidos.findById(id); 
    }
    public void eliminar(Long id) { 
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            Optional<Pedidos> ent = repoPedidos.findByIdAndEmpresaId(id, tenantId);
            if (ent.isPresent()) {
                repoPedidos.deleteById(id);
            }
        } else {
            repoPedidos.deleteById(id); 
        }
    }
}

