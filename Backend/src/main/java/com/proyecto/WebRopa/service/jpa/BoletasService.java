package com.proyecto.WebRopa.service.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.proyecto.WebRopa.entity.Boletas;
import com.proyecto.WebRopa.entity.Pedidos;
import com.proyecto.WebRopa.repository.BoletasRepository;
import com.proyecto.WebRopa.repository.PedidosRepository; // Asegúrate de tener este repositorio
import com.proyecto.WebRopa.service.IBoletasService;

@Service
public class BoletasService implements IBoletasService {

    private final BoletasRepository repoBoletas;
    private final PedidosRepository repoPedidos; // Inyectamos el repositorio de Pedidos

    // Actualizamos el constructor
    public BoletasService(BoletasRepository repoBoletas, PedidosRepository repoPedidos) {
        this.repoBoletas = repoBoletas;
        this.repoPedidos = repoPedidos;
    }

    public List<Boletas> buscarTodos() {
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            return repoBoletas.findByPedidoEmpresaId(tenantId);
        }
        return repoBoletas.findAll();
    }

    public Optional<Boletas> buscarId(Long id) {
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            return repoBoletas.findByIdAndPedidoEmpresaId(id, tenantId);
        }
        return repoBoletas.findById(id);
    }
    public void eliminar(Long id) { repoBoletas.deleteById(id); }

    // ── LA NUEVA LÓGICA DE GUARDADO ─────────────────────────────────
    public void guardar(Boletas boleta) { 
        // 1. Buscamos el pedido real en la base de datos para no confiar en el JSON
        Pedidos pedidoFisico = repoPedidos.findById(boleta.getPedido().getId())
                .orElseThrow(() -> new RuntimeException("El pedido referenciado no existe"));

        // 2. Extraemos el total real que el cliente debe pagar
        double totalReal = pedidoFisico.getTotal();

        // 3. Calculamos matemáticamente el IGV (18%) y el Subtotal
        // Fórmula: Subtotal = Total / 1.18
        double subtotalCalculado = totalReal / 1.18;
        double igvCalculado = totalReal - subtotalCalculado;

        // 4. Redondeamos a 2 decimales y asignamos los valores a la boleta
        boleta.setTotal(Math.round(totalReal * 100.0) / 100.0);
        boleta.setSubtotal(Math.round(subtotalCalculado * 100.0) / 100.0);
        boleta.setIgv(Math.round(igvCalculado * 100.0) / 100.0);

        // 5. Aseguramos que el estado inicial sea siempre ACTIVA por defecto
        boleta.setEstado(Boletas.Estado.ACTIVA);

        // 6. Finalmente, guardamos la boleta
        repoBoletas.save(boleta); 
    }

    public void modificar(Boletas boleta) {
        Optional<Boletas> existente = repoBoletas.findById(boleta.getId());
        if (existente.isPresent()) {
            if (existente.get().getEstado() == Boletas.Estado.ANULADA) {
                throw new RuntimeException("Una boleta anulada no se puede modificar");
            }
            // Solo se permite modificar datos del cliente, jamás los montos
            existente.get().setDni_cliente(boleta.getDni_cliente());
            existente.get().setNombre_cliente(boleta.getNombre_cliente());
            repoBoletas.save(existente.get());
        }
    }
}