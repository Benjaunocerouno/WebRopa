package com.proyecto.WebRopa.service.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.proyecto.WebRopa.entity.Devoluciones;
import com.proyecto.WebRopa.entity.Pagos;
import com.proyecto.WebRopa.repository.DevolucionesRepository;
import com.proyecto.WebRopa.repository.PagosRepository;
import com.proyecto.WebRopa.service.IDevolucionesService;

@Service
public class DevolucionesService implements IDevolucionesService {

    private final DevolucionesRepository repoDevoluaciones;
    private final PagosRepository repoPagos;

    public DevolucionesService(
            DevolucionesRepository repoDevoluaciones,
            PagosRepository repoPagos) {
        this.repoDevoluaciones = repoDevoluaciones;
        this.repoPagos = repoPagos;
    }

    public List<Devoluciones> buscarTodos() {
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            return repoDevoluaciones.findByPedidoEmpresaId(tenantId);
        }
        return repoDevoluaciones.findAll();
    }

    public List<Devoluciones> buscarPorPedidoId(Long pedidoId) {
        return repoDevoluaciones.findByPedidoId(pedidoId);
    }

    public List<Devoluciones> buscarPorEstado(Devoluciones.Estado estado) {
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            return repoDevoluaciones.findByEstadoAndPedidoEmpresaId(estado, tenantId);
        }
        return repoDevoluaciones.findByEstado(estado);
    }

    public Optional<Devoluciones> buscarId(Long id) {
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            return repoDevoluaciones.findByIdAndPedidoEmpresaId(id, tenantId);
        }
        return repoDevoluaciones.findById(id);
    }
    public void guardar(Devoluciones devolucion) { repoDevoluaciones.save(devolucion); }
    public void eliminar(Long id) { repoDevoluaciones.deleteById(id); }

    // SOLICITADA → APROBADA (admin revisa y aprueba; el reembolso físico aún no ocurre)
    public void aprobarDevolucion(Long id) {
        Optional<Devoluciones> devOpt = repoDevoluaciones.findById(id);
        if (devOpt.isPresent()) {
            Devoluciones dev = devOpt.get();
            if (dev.getEstado() != Devoluciones.Estado.SOLICITADA) {
                throw new RuntimeException("Solo se pueden aprobar devoluciones en estado SOLICITADA");
            }
            dev.setEstado(Devoluciones.Estado.APROBADA);
            repoDevoluaciones.save(dev);
        }
    }

    // SOLICITADA → RECHAZADA
    public void rechazarDevolucion(Long id) {
        Optional<Devoluciones> devOpt = repoDevoluaciones.findById(id);
        if (devOpt.isPresent()) {
            Devoluciones dev = devOpt.get();
            if (dev.getEstado() != Devoluciones.Estado.SOLICITADA) {
                throw new RuntimeException("Solo se pueden rechazar devoluciones en estado SOLICITADA");
            }
            dev.setEstado(Devoluciones.Estado.RECHAZADA);
            repoDevoluaciones.save(dev);
        }
    }

    // APROBADA → REEMBOLSADA (admin confirma que el dinero fue devuelto manualmente)
    public void reembolsarDevolucion(Long id) {
        Optional<Devoluciones> devOpt = repoDevoluaciones.findById(id);
        if (devOpt.isPresent()) {
            Devoluciones dev = devOpt.get();
            if (dev.getEstado() != Devoluciones.Estado.APROBADA) {
                throw new RuntimeException("Solo se pueden reembolsar devoluciones en estado APROBADA");
            }
            dev.setEstado(Devoluciones.Estado.REEMBOLSADA);
            repoDevoluaciones.save(dev);
            marcarPagoReembolsado(dev.getPedido().getId());
        }
    }

    // Procesa todas las devoluciones SOLICITADA o APROBADA de un pedido cancelado de una vez
    public void reembolsarTodasPorPedido(Long pedidoId) {
        List<Devoluciones> devs = repoDevoluaciones.findByPedidoId(pedidoId);
        if (devs.isEmpty()) {
            throw new RuntimeException("No hay devoluciones registradas para ese pedido");
        }
        for (Devoluciones dev : devs) {
            if (dev.getEstado() == Devoluciones.Estado.SOLICITADA
                    || dev.getEstado() == Devoluciones.Estado.APROBADA) {
                dev.setEstado(Devoluciones.Estado.REEMBOLSADA);
                repoDevoluaciones.save(dev);
            }
        }
        marcarPagoReembolsado(pedidoId);
    }

    private void marcarPagoReembolsado(Long pedidoId) {
        List<Pagos> pagos = repoPagos.findByPedidoId(pedidoId);
        if (!pagos.isEmpty()) {
            Pagos pago = pagos.get(0);
            pago.setEstado(Pagos.Estado.REEMBOLSADO);
            repoPagos.save(pago);
        }
    }
}