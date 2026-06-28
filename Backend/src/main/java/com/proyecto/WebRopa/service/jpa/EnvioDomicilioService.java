package com.proyecto.WebRopa.service.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.proyecto.WebRopa.entity.EnvioDomicilio;
import com.proyecto.WebRopa.repository.EnvioDomicilioRepository;
import com.proyecto.WebRopa.service.IEnvioDomicilioService;

@Service
public class EnvioDomicilioService implements IEnvioDomicilioService {

    private final EnvioDomicilioRepository repository;

    public EnvioDomicilioService(EnvioDomicilioRepository repository) {
        this.repository = repository;
    }

    public List<EnvioDomicilio> buscarTodos() {
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            return repository.findByEmpresaId(tenantId);
        }
        return repository.findAll();
    }

    public void guardar(EnvioDomicilio entity) {
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null && entity.getEmpresa() == null) {
            com.proyecto.WebRopa.entity.Empresas emp = new com.proyecto.WebRopa.entity.Empresas();
            emp.setId(tenantId);
            entity.setEmpresa(emp);
        }
        repository.save(entity);
    }

    public void modificar(EnvioDomicilio entity) {
        guardar(entity);
    }

    public Optional<EnvioDomicilio> buscarId(Long id) {
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            return repository.findByIdAndEmpresaId(id, tenantId);
        }
        return repository.findById(id);
    }

    public Optional<EnvioDomicilio> buscarPorPedidoId(Long pedidoId) {
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            return repository.findByPedidoIdAndEmpresaId(pedidoId, tenantId);
        }
        return repository.findByPedidoId(pedidoId);
    }

    public void eliminar(Long id) {
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            Optional<EnvioDomicilio> ent = repository.findByIdAndEmpresaId(id, tenantId);
            if (ent.isPresent()) {
                repository.deleteById(id);
            }
        } else {
            repository.deleteById(id);
        }
    }
}
