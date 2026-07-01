package com.proyecto.WebRopa.service.jpa;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.WebRopa.entity.*;
import com.proyecto.WebRopa.service.*;

@Service
public class PedidoEstadoService implements IPedidoEstadoService {

    private final IPedidosItemsService pedidosItemsService;
    private final IVariantesService variantesService;
    private final IPagosService pagosService;
    private final IBoletasService boletasService;
    private final IRecojoTiendaService recojoTiendaService;
    private final IEnvioDomicilioService envioDomicilioService;
    private final IDevolucionesService devolucionesService;
    private final INotificacionesService notificacionesService;

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager em;

    public PedidoEstadoService(
            IPedidosItemsService pedidosItemsService,
            IVariantesService variantesService,
            IPagosService pagosService,
            IBoletasService boletasService,
            IRecojoTiendaService recojoTiendaService,
            IEnvioDomicilioService envioDomicilioService,
            IDevolucionesService devolucionesService,
            INotificacionesService notificacionesService) {
        this.pedidosItemsService = pedidosItemsService;
        this.variantesService = variantesService;
        this.pagosService = pagosService;
        this.boletasService = boletasService;
        this.recojoTiendaService = recojoTiendaService;
        this.envioDomicilioService = envioDomicilioService;
        this.devolucionesService = devolucionesService;
        this.notificacionesService = notificacionesService;
    }

    // ── → CONFIRMADO ─────────────────────────────────
    @Transactional
    public void aplicarEfectosConfirmado(Pedidos pedido) {
        // Aprobar pago pendiente
        for (Pagos p : pagosService.buscarPorPedidoId(pedido.getId())) {
            if (p.getEstado() == Pagos.Estado.PENDIENTE) {
                p.setEstado(Pagos.Estado.APROBADO);
                pagosService.guardar(p);
            }
        }
        // Generar boleta si no existe
        boolean tieneBoleta = boletasService.buscarTodos().stream()
                .anyMatch(b -> b.getPedido().getId().equals(pedido.getId()));
        if (!tieneBoleta) generarBoleta(pedido);

        // Generar entrega si no existe
        if (pedido.getTipo_entrega() == Pedidos.TipoEntrega.DELIVERY) {
            if (envioDomicilioService.buscarPorPedidoId(pedido.getId()).isEmpty()) {
                generarEnvioDomicilio(pedido);
            }
        } else {
            if (recojoTiendaService.buscarPorPedidoId(pedido.getId()).isEmpty()) {
                generarRecojo(pedido);
            }
        }

        // Registrar movimientos de inventario SALIDA_VENTA si no existen
        String checkJpql = "SELECT COUNT(im) FROM InventarioMovimientos im WHERE im.observacion = :obs";
        long existingMovs = em.createQuery(checkJpql, Long.class)
                .setParameter("obs", "Salida por venta de pedido #" + pedido.getId())
                .getSingleResult();

        if (existingMovs == 0) {
            List<PedidosItems> items = pedidosItemsService.buscarPorPedidoId(pedido.getId());
            for (PedidosItems item : items) {
                InventarioMovimientos mov = new InventarioMovimientos();
                mov.setTipo_movimiento(InventarioMovimientos.TipoMovimiento.SALIDA_VENTA);
                mov.setCantidad(-item.getCantidad());
                mov.setObservacion("Salida por venta de pedido #" + pedido.getId());
                mov.setVariante(item.getVariante());
                mov.setFecha(LocalDateTime.now());
                mov.setEstado(InventarioMovimientos.Estado.ACTIVO);
                em.persist(mov);
            }
        }
    }

    // ── → LISTO_PARA_RECOGER ─────────────────────────
    @Transactional
    public void aplicarEfectosListoParaRecoger(Pedidos pedido) {
        notificacionesService.crear(
                pedido.getUsuario(),
                pedido,
                Notificaciones.Tipo.LISTO_PARA_RECOGER,
                "Tu pedido #" + pedido.getId() + " está listo para recoger en tienda."
        );
    }

    // ── → RECOGIDO ───────────────────────────────────
    @Transactional
    public void aplicarEfectosRecogido(Pedidos pedido) {
        recojoTiendaService.buscarPorPedidoId(pedido.getId()).ifPresent(r -> {
            r.setFecha_recogido(LocalDateTime.now());
            recojoTiendaService.modificar(r);
        });
    }

    // ── → NO_RECOGIDO (desde job A) ──────────────────
    @Transactional
    public void aplicarEfectosNoRecogido(Pedidos pedido) {
        pedido.setFecha_no_recogido(LocalDateTime.now());
        notificacionesService.crear(
                pedido.getUsuario(),
                pedido,
                Notificaciones.Tipo.RECOJO_POR_VENCER,
                "Tu pedido #" + pedido.getId() + " no fue recogido a tiempo. "
                        + "Si no lo recoges pronto, se cancelará y se iniciará tu reembolso."
        );
    }

    // ── → CANCELADO ──────────────────────────────────
    @Transactional
    public void aplicarEfectosCancelado(Pedidos pedido, String descripcion) {
        List<PedidosItems> items = pedidosItemsService.buscarPorPedidoId(pedido.getId());

        // Liberar stock
        for (PedidosItems item : items) {
            Variantes variante = item.getVariante();
            if (variante != null) {
                variante.setStock(variante.getStock() + item.getCantidad());
                variantesService.guardar(variante);
            }
        }

        // Crear solicitudes de devolución si el pago estaba aprobado
        if (pedido.isPago_confirmado()) {
            for (PedidosItems item : items) {
                Devoluciones dev = new Devoluciones();
                dev.setPedido(pedido);
                dev.setPedidoItem(item);
                dev.setMotivo(Devoluciones.Motivo.OTRO);
                dev.setDescripcion(descripcion);
                dev.setCantidad_devuelta(item.getCantidad());
                dev.setMonto_reembolso(item.getPrecio_unitario() * item.getCantidad());
                devolucionesService.guardar(dev);
            }
        }
    }

    // ── Helpers privados ─────────────────────────────
    private void generarBoleta(Pedidos pedido) {
        Boletas boleta = new Boletas();
        boleta.setPedido(pedido);
        String nombre = pedido.getUsuario().getNombre();
        if (nombre == null || nombre.trim().isEmpty()) nombre = "Cliente No Registrado";
        boleta.setNombre_cliente(nombre);
        boleta.setSubtotal(pedido.getSubtotal());
        boleta.setIgv(pedido.getSubtotal() * 0.18);
        boleta.setTotal(pedido.getTotal());
        boleta.setNumero_boleta("BOL-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        boletasService.guardar(boleta);
    }

    private void generarRecojo(Pedidos pedido) {
        RecojoTienda recojo = new RecojoTienda();
        recojo.setPedido(pedido);
        recojo.setCodigo_recojo("REC-" + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        recojoTiendaService.guardar(recojo);
    }

    private void generarEnvioDomicilio(Pedidos pedido) {
        EnvioDomicilio envio = new EnvioDomicilio();
        envio.setPedido(pedido);
        envio.setDireccion(pedido.getDireccion_envio());
        envio.setDistrito(pedido.getDistrito_envio());
        envio.setReferencia(pedido.getReferencia_envio());
        envio.setNombreDestinatario(pedido.getDestinatario_nombre());
        envio.setTelefonoContacto(pedido.getDestinatario_telefono());
        envio.setCostoEnvio(pedido.getCosto_envio());
        envio.setEstado(EnvioDomicilio.Estado.PENDIENTE);
        envio.setCodigo_seguimiento("ENV-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        envioDomicilioService.guardar(envio);
    }
}
