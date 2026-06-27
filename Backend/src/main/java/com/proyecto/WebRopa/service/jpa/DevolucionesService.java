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

    public List<Devoluciones> buscarTodos() { return repoDevoluaciones.findAll(); }
    public List<Devoluciones> buscarPorPedidoId(Long pedidoId) { return repoDevoluaciones.findByPedidoId(pedidoId); }
    public List<Devoluciones> buscarPorEstado(Devoluciones.Estado estado) { return repoDevoluaciones.findByEstado(estado); }
    public Optional<Devoluciones> buscarId(Long id) { return repoDevoluaciones.findById(id); }
    public void guardar(Devoluciones devolucion) { repoDevoluaciones.save(devolucion); }
    public void eliminar(Long id) { repoDevoluaciones.deleteById(id); }

    public void aprobarDevolucion(Long id) {
        Optional<Devoluciones> devOpt = repoDevoluaciones.findById(id);
        if (devOpt.isPresent()) {
            Devoluciones dev = devOpt.get();

            // Solo se puede aprobar si está SOLICITADA
            if (dev.getEstado() != Devoluciones.Estado.SOLICITADA) {
                throw new RuntimeException("Solo se pueden aprobar devoluciones en estado SOLICITADA");
            }

            dev.setEstado(Devoluciones.Estado.REEMBOLSADA);
            repoDevoluaciones.save(dev);

            // Buscar el pago del pedido y marcarlo como REEMBOLSADO
            List<Pagos> pagos = repoPagos.findByPedidoId(dev.getPedido().getId());
            if (!pagos.isEmpty()) {
                Pagos pago = pagos.get(0);
                pago.setEstado(Pagos.Estado.REEMBOLSADO);
                repoPagos.save(pago);
            }
        }
    }

    public void rechazarDevolucion(Long id) {
        Optional<Devoluciones> devOpt = repoDevoluaciones.findById(id);
        if (devOpt.isPresent()) {
            Devoluciones dev = devOpt.get();

            // Solo se puede rechazar si está SOLICITADA
            if (dev.getEstado() != Devoluciones.Estado.SOLICITADA) {
                throw new RuntimeException("Solo se pueden rechazar devoluciones en estado SOLICITADA");
            }

            dev.setEstado(Devoluciones.Estado.RECHAZADA);
            repoDevoluaciones.save(dev);
        }
    }
}