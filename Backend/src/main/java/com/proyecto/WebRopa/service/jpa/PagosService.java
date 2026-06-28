package com.proyecto.WebRopa.service.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.proyecto.WebRopa.entity.Pagos;
import com.proyecto.WebRopa.repository.PagosRepository;
import com.proyecto.WebRopa.service.IPagosService;

@Service
public class PagosService implements IPagosService {

    private final PagosRepository repoPagos;
    public PagosService(PagosRepository repoPagos) {
        this.repoPagos = repoPagos;
    }

    public List<Pagos> buscarPorPedidoId(Long pedidoId) {return repoPagos.findByPedidoId(pedidoId);}

    public List<Pagos> buscarTodos() {
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            return repoPagos.findByPedidoEmpresaId(tenantId);
        }
        return repoPagos.findAll();
    }

    public Optional<Pagos> buscarId(Long id) {
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            return repoPagos.findByIdAndPedidoEmpresaId(id, tenantId);
        }
        return repoPagos.findById(id);
    }

    public void guardar(Pagos pago) {repoPagos.save(pago);}

    public void cambiarEstado(Long id, Pagos.Estado nuevoEstado) {
        Optional<Pagos> pago = repoPagos.findById(id);
        if (pago.isPresent()) {
            pago.get().setEstado(nuevoEstado);
            repoPagos.save(pago.get());
        }
    }

    public Pagos modificar(Pagos pagoUpdate) {
        Optional<Pagos> existenteOpt = repoPagos.findById(pagoUpdate.getId());
        if (!existenteOpt.isPresent()) {
            throw new RuntimeException("El pago no existe");
        }
        
        Pagos existente = existenteOpt.get();
        
        if (pagoUpdate.getEstado() != null) {
            existente.setEstado(pagoUpdate.getEstado());
        }
        if (pagoUpdate.getReferencia_externa() != null) {
            existente.setReferencia_externa(pagoUpdate.getReferencia_externa());
        }
        
        return repoPagos.save(existente);
    }
}