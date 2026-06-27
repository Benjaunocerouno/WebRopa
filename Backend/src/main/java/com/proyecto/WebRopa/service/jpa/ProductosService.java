package com.proyecto.WebRopa.service.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.proyecto.WebRopa.entity.Productos;
import com.proyecto.WebRopa.repository.ProductosRepository;
import com.proyecto.WebRopa.service.IProductosService;

@Service
public class ProductosService implements IProductosService {

    private final ProductosRepository repoProductos;

    public ProductosService(ProductosRepository repoProductos) {
        this.repoProductos = repoProductos;
    }

    public List<Productos> buscarPorEmpresa(Long empresaId) {
        return repoProductos.findByEmpresaId(empresaId);
    }

    public List<Productos> buscarTodos() { 
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            return repoProductos.findByEmpresaId(tenantId);
        }
        return repoProductos.findAll(); 
    }
    public void guardar(Productos producto) { 
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null && producto.getEmpresa() == null) {
            com.proyecto.WebRopa.entity.Empresas emp = new com.proyecto.WebRopa.entity.Empresas();
            emp.setId(tenantId);
            producto.setEmpresa(emp);
        }
        repoProductos.save(producto); 
    }
    public void modificar(Productos producto) { 
        guardar(producto); 
    }
    public Optional<Productos> buscarId(Long id) { 
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            return repoProductos.findByIdAndEmpresaId(id, tenantId);
        }
        return repoProductos.findById(id); 
    }
    public void eliminar(Long id) { 
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            Optional<Productos> prod = repoProductos.findByIdAndEmpresaId(id, tenantId);
            if (prod.isPresent()) {
                repoProductos.deleteById(id);
            }
        } else {
            repoProductos.deleteById(id); 
        }
    }
}