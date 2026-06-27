package com.proyecto.WebRopa.service.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.proyecto.WebRopa.entity.RecojoTienda;
import com.proyecto.WebRopa.repository.RecojoTiendaRepository;
import com.proyecto.WebRopa.service.IRecojoTiendaService;

@Service
public class RecojoTiendaService implements IRecojoTiendaService {

    private final RecojoTiendaRepository repository;

    public RecojoTiendaService(RecojoTiendaRepository repository) {
        this.repository = repository;
    }

    public List<RecojoTienda> buscarTodos() {
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            return repository.findByEmpresaId(tenantId);
        }
        return repository.findAll();
    }

    public void guardar(RecojoTienda entity) {
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null && entity.getEmpresa() == null) {
            com.proyecto.WebRopa.entity.Empresas emp = new com.proyecto.WebRopa.entity.Empresas();
            emp.setId(tenantId);
            entity.setEmpresa(emp);
        }
        repository.save(entity);
    }

    public void modificar(RecojoTienda entity) {
        guardar(entity);
    }

    public Optional<RecojoTienda> buscarId(Long id) {
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            return repository.findByIdAndEmpresaId(id, tenantId);
        }
        return repository.findById(id);
    }

    public Optional<RecojoTienda> buscarPorPedidoId(Long pedidoId) {
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            return repository.findByPedidoIdAndEmpresaId(pedidoId, tenantId);
        }
        return repository.findByPedidoId(pedidoId);
    }

    public void eliminar(Long id) {
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            Optional<RecojoTienda> ent = repository.findByIdAndEmpresaId(id, tenantId);
            if (ent.isPresent()) {
                repository.deleteById(id);
            }
        } else {
            repository.deleteById(id);
        }
    }
}
