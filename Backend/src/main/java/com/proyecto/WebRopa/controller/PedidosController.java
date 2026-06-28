package com.proyecto.WebRopa.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyecto.WebRopa.entity.*;
import com.proyecto.WebRopa.service.*;

@RestController
@RequestMapping("/api")
public class PedidosController {

    private final IPedidosService servicePedidos;
    private final IPedidosItemsService servicePedidosItems;
    private final ICarritosService serviceCarritos;
    private final ICarritos_itemsService serviceCarritosItems;
    private final ICuponesService serviceCupones;
    private final ICuponUsosService serviceCuponUsos;
    private final IVariantesService serviceVariantes;
    private final IPagosService servicePagos;
    private final IBoletasService serviceBoletas;
    private final IRecojoTiendaService serviceRecojoTienda;
    private final IDevolucionesService serviceDevolucion;

    public PedidosController(
            IPedidosService servicePedidos,
            IPedidosItemsService servicePedidosItems,
            ICarritosService serviceCarritos,
            ICarritos_itemsService serviceCarritosItems,
            ICuponesService serviceCupones,
            ICuponUsosService serviceCuponUsos,
            IVariantesService serviceVariantes,
            IPagosService servicePagos,
            IBoletasService serviceBoletas,
            IRecojoTiendaService serviceRecojoTienda,
            IDevolucionesService serviceDevolucion) {
        this.servicePedidos = servicePedidos;
        this.servicePedidosItems = servicePedidosItems;
        this.serviceCarritos = serviceCarritos;
        this.serviceCarritosItems = serviceCarritosItems;
        this.serviceCupones = serviceCupones;
        this.serviceCuponUsos = serviceCuponUsos;
        this.serviceVariantes = serviceVariantes;
        this.servicePagos = servicePagos;
        this.serviceBoletas = serviceBoletas;
        this.serviceRecojoTienda = serviceRecojoTienda;
        this.serviceDevolucion = serviceDevolucion;
    }

    // ── Ver todos los pedidos (admin) ────────────────
    @GetMapping("/pedidos")
    public List<Pedidos> listarTodos() {
        return servicePedidos.buscarTodos();
    }

    // ── Ver pedidos de un usuario ────────────────────
    @GetMapping("/pedidos/usuario/{usuarioId}")
    public ResponseEntity<?> listarPorUsuario(@PathVariable Long usuarioId) {
        List<Pedidos> pedidos = servicePedidos.buscarTodos().stream()
                .filter(p -> p.getUsuario() != null && p.getUsuario().getId().equals(usuarioId))
                .collect(Collectors.toList());
        return ResponseEntity.ok(pedidos);
    }

    // ── Ver un pedido específico ─────────────────────
    @GetMapping("/pedidos/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        Optional<Pedidos> pedido = servicePedidos.buscarId(id);
        if (!pedido.isPresent()) {
            return ResponseEntity.badRequest().body("El pedido no existe");
        }
        return ResponseEntity.ok(pedido.get());
    }

    // ── Crear pedido desde el carrito ────────────────
    @PostMapping("/pedidos")
    public ResponseEntity<?> crearPedido(@RequestBody Pedidos pedido) {

        // 1. Validar que venga el usuario
        if (pedido.getUsuario() == null || pedido.getUsuario().getId() == null) {
            return ResponseEntity.badRequest().body("Debe especificar el usuario");
        }

        // 2. Obtener el carrito del usuario
        Optional<Carritos> carrito = serviceCarritos.buscarPorUsuarioId(pedido.getUsuario().getId());
        if (!carrito.isPresent()) {
            return ResponseEntity.badRequest().body("El usuario no tiene un carrito");
        }

        // 3. Obtener los items del carrito
        List<Carritositems> itemsCarrito = serviceCarritosItems.buscarPorCarritoId(carrito.get().getId());
        if (itemsCarrito.isEmpty()) {
            return ResponseEntity.badRequest().body("El carrito está vacío");
        }

        // 4. Verificar stock de cada variante
        for (Carritositems item : itemsCarrito) {
            Variantes variante = item.getVariante();
            if (variante.getStock() < item.getCantidad()) {
                return ResponseEntity.badRequest()
                    .body("Stock insuficiente para: " + variante.getSku() 
                          + " (disponible: " + variante.getStock() + ")");
            }
        }

        // 5. Calcular subtotal
        double subtotal = 0.0;
        for (Carritositems item : itemsCarrito) {
            subtotal += item.getVariante().getProducto().getPrecio() * item.getCantidad();
        }

        // 6. Aplicar cupón si tiene
        double descuento = 0.0;
        Cupones cuponAplicado = null;

        if (pedido.getCupon() != null && pedido.getCupon().getCodigo() != null) {
            Optional<Cupones> cuponOpt = serviceCupones.buscarTodos().stream()
                    .filter(c -> c.getCodigo().equalsIgnoreCase(pedido.getCupon().getCodigo().trim()))
                    .findFirst();

            // ¿Existe el cupón?
            if (!cuponOpt.isPresent()) {
                return ResponseEntity.badRequest().body("El cupón no existe");
            }

            cuponAplicado = cuponOpt.get();

            // ¿Está activo?
            if (cuponAplicado.getEstado() != Cupones.Estado.ACTIVO) {
                return ResponseEntity.badRequest().body("El cupón no está activo");
            }

            // ¿Tiene usos disponibles?
            if (cuponAplicado.getUsos_maximos() != null && 
                cuponAplicado.getUsos_actuales() >= cuponAplicado.getUsos_maximos()) {
                return ResponseEntity.badRequest().body("El cupón ya no tiene usos disponibles");
            }

            // ¿El usuario ya lo usó?
            Optional<CuponUsos> usoExistente = serviceCuponUsos.buscarPorCuponYUsuario(
                cuponAplicado.getId(), pedido.getUsuario().getId());
            if (usoExistente.isPresent()) {
                return ResponseEntity.badRequest().body("Ya usaste este cupón anteriormente");
            }

            // ¿Cumple el mínimo de compra?
            if (cuponAplicado.getMinimo_compra() != null && 
                subtotal < cuponAplicado.getMinimo_compra()) {
                return ResponseEntity.badRequest()
                    .body("El pedido no cumple el mínimo de S/ " + cuponAplicado.getMinimo_compra());
            }

            // Calcular descuento
            if (cuponAplicado.getTipo() == Cupones.Tipo.PORCENTAJE) {
                descuento = subtotal * (cuponAplicado.getValor() / 100);
            } else {
                descuento = cuponAplicado.getValor();
            }
        }

        // 7. Calcular total
        double total = subtotal - descuento;

        // 8. Crear el pedido
        pedido.setSubtotal(subtotal);
        pedido.setDescuento(descuento);
        pedido.setTotal(total);
        pedido.setEstado(Pedidos.Estado.PENDIENTE);
        pedido.setCupon(cuponAplicado);
        
        // Asignar empresa correspondiente del primer item
        if (!itemsCarrito.isEmpty()) {
            Empresas emp = itemsCarrito.get(0).getVariante().getEmpresa();
            if (emp == null && itemsCarrito.get(0).getVariante().getProducto() != null) {
                emp = itemsCarrito.get(0).getVariante().getProducto().getEmpresa();
            }
            pedido.setEmpresa(emp);
        }
        
        servicePedidos.guardar(pedido);

        // 9. Crear los pedido_items y descontar stock
        for (Carritositems item : itemsCarrito) {
            PedidosItems pedidoItem = new PedidosItems();
            pedidoItem.setPedido(pedido);
            pedidoItem.setVariante(item.getVariante());
            pedidoItem.setCantidad(item.getCantidad());
            // Precio congelado al momento de la compra
            pedidoItem.setPrecio_unitario(item.getVariante().getProducto().getPrecio());
            servicePedidosItems.guardar(pedidoItem);

            // Descontar stock
            Variantes variante = item.getVariante();
            variante.setStock(variante.getStock() - item.getCantidad());
            serviceVariantes.guardar(variante);
        }

        // 10. Registrar uso del cupón si aplicó
        if (cuponAplicado != null) {
            CuponUsos uso = new CuponUsos();
            uso.setCupon(cuponAplicado);
            uso.setUsuario(pedido.getUsuario());
            uso.setPedido(pedido);
            serviceCuponUsos.registrarUso(uso);

            // Incrementar usos del cupón
            cuponAplicado.setUsos_actuales(cuponAplicado.getUsos_actuales() + 1);
            serviceCupones.guardar(cuponAplicado);
        }

        // 11. Vaciar el carrito
        for (Carritositems item : itemsCarrito) {
            serviceCarritosItems.eliminar(item.getId());
        }

        return ResponseEntity.ok(pedido);
    }

    // ── Modificar pedido (admin) ─────────────────────
    @PutMapping("/pedidos")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PEDIDOS_GESTIONAR') or hasAuthority('ADMIN')")
    public ResponseEntity<?> modificarPedido(@RequestBody Pedidos pedidoUpdate) {
        if (pedidoUpdate.getId() == null) {
            return ResponseEntity.badRequest().body("Debe especificar el id del pedido");
        }

        Optional<Pedidos> existenteOpt = servicePedidos.buscarId(pedidoUpdate.getId());
        if (!existenteOpt.isPresent()) {
            return ResponseEntity.badRequest().body("El pedido no existe");
        }

        Pedidos existente = existenteOpt.get();

        if (pedidoUpdate.getNotas() != null) {
            existente.setNotas(pedidoUpdate.getNotas().trim());
        }

        if (pedidoUpdate.getEstado() != null && !pedidoUpdate.getEstado().equals(existente.getEstado())) {
            Pedidos.Estado actual = existente.getEstado();
            Pedidos.Estado nuevo  = pedidoUpdate.getEstado();

            if (!esTransicionValida(actual, nuevo)) {
                return ResponseEntity.badRequest()
                    .body("Transición de estado inválida: " + actual + " → " + nuevo);
            }

            // Cancelar siempre es permitido; cualquier otro avance requiere pago confirmado
            if (nuevo != Pedidos.Estado.CANCELADO && !existente.isPago_confirmado()) {
                return ResponseEntity.badRequest()
                    .body("No se puede avanzar el estado: el pago del pedido no ha sido confirmado");
            }

            existente.setEstado(nuevo);

            if (nuevo == Pedidos.Estado.CONFIRMADO) {
                // Aprobar pago pendiente si existe
                for (Pagos p : servicePagos.buscarPorPedidoId(existente.getId())) {
                    if (p.getEstado() == Pagos.Estado.PENDIENTE) {
                        p.setEstado(Pagos.Estado.APROBADO);
                        servicePagos.guardar(p);
                    }
                }
                // Generar boleta si no existe
                boolean tieneBoleta = serviceBoletas.buscarTodos().stream()
                    .anyMatch(b -> b.getPedido().getId().equals(existente.getId()));
                if (!tieneBoleta) generarBoleta(existente);
                // Generar recojo si no existe
                if (serviceRecojoTienda.buscarPorPedidoId(existente.getId()).isEmpty()) {
                    generarRecojo(existente);
                }

            } else if (nuevo == Pedidos.Estado.RECOGIDO) {
                // Registrar fecha y hora exacta del recojo en la tabla recojo_tienda
                serviceRecojoTienda.buscarPorPedidoId(existente.getId()).ifPresent(r -> {
                    r.setFecha_recogido(LocalDateTime.now());
                    serviceRecojoTienda.modificar(r);
                });

            } else if (nuevo == Pedidos.Estado.CANCELADO) {
                // Liberar stock de todas las variantes del pedido
                List<PedidosItems> items = servicePedidosItems.buscarPorPedidoId(existente.getId());
                for (PedidosItems item : items) {
                    Variantes variante = item.getVariante();
                    if (variante != null) {
                        variante.setStock(variante.getStock() + item.getCantidad());
                        serviceVariantes.guardar(variante);
                    }
                }
                // Si el pago ya estaba aprobado, crear solicitud de devolución por cada item
                if (existente.isPago_confirmado()) {
                    for (PedidosItems item : items) {
                        Devoluciones dev = new Devoluciones();
                        dev.setPedido(existente);
                        dev.setPedidoItem(item);
                        dev.setMotivo(Devoluciones.Motivo.OTRO);
                        dev.setDescripcion("Pedido cancelado por administrador");
                        dev.setCantidad_devuelta(item.getCantidad());
                        dev.setMonto_reembolso(item.getPrecio_unitario() * item.getCantidad());
                        serviceDevolucion.guardar(dev);
                    }
                }
            }
        }

        servicePedidos.modificar(existente);
        return ResponseEntity.ok(existente);
    }

    // ── Máquina de estados: transiciones válidas ─────
    private static boolean esTransicionValida(Pedidos.Estado actual, Pedidos.Estado nuevo) {
        return switch (actual) {
            case PENDIENTE          -> nuevo == Pedidos.Estado.CONFIRMADO        || nuevo == Pedidos.Estado.CANCELADO;
            case CONFIRMADO         -> nuevo == Pedidos.Estado.EN_PREPARACION    || nuevo == Pedidos.Estado.CANCELADO;
            case EN_PREPARACION     -> nuevo == Pedidos.Estado.LISTO_PARA_RECOGER || nuevo == Pedidos.Estado.CANCELADO;
            case LISTO_PARA_RECOGER -> nuevo == Pedidos.Estado.RECOGIDO          || nuevo == Pedidos.Estado.NO_RECOGIDO;
            case NO_RECOGIDO        -> nuevo == Pedidos.Estado.LISTO_PARA_RECOGER || nuevo == Pedidos.Estado.CANCELADO;
            case RECOGIDO, CANCELADO -> false;
        };
    }

    // ── Helper Methods ───────────────────────────────
    private Boletas generarBoleta(Pedidos pedido) {
        Boletas boleta = new Boletas();
        boleta.setPedido(pedido);
        
        String nombreUsuario = pedido.getUsuario().getNombre();
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
            nombreUsuario = "Cliente No Registrado";
        }
        boleta.setNombre_cliente(nombreUsuario);
        
        boleta.setSubtotal(pedido.getSubtotal());
        boleta.setIgv(pedido.getSubtotal() * 0.18);
        boleta.setTotal(pedido.getTotal());
        boleta.setNumero_boleta("BOL-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        serviceBoletas.guardar(boleta);
        return boleta;
    }

    private RecojoTienda generarRecojo(Pedidos pedido) {
        RecojoTienda recojo = new RecojoTienda();
        recojo.setPedido(pedido);
        recojo.setCodigo_recojo("REC-" + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        serviceRecojoTienda.guardar(recojo);
        return recojo;
    }

    // ── Eliminar pedido (Borrado lógico y liberar stock) ─────────────
    @DeleteMapping("/pedidos/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        Optional<Pedidos> pedido = servicePedidos.buscarId(id);
        if (!pedido.isPresent()) {
            return ResponseEntity.badRequest().body("El pedido no existe");
        }
        
        Pedidos p = pedido.get();
        if (p.getEstado() == Pedidos.Estado.PENDIENTE) {
            p.setEstado(Pedidos.Estado.CANCELADO);
            servicePedidos.guardar(p);
            
            // Liberar Stock de Variantes
            List<PedidosItems> items = servicePedidosItems.buscarPorPedidoId(p.getId());
            for (PedidosItems item : items) {
                Variantes variante = item.getVariante();
                if (variante != null) {
                    variante.setStock(variante.getStock() + item.getCantidad());
                    serviceVariantes.guardar(variante);
                }
            }
            return ResponseEntity.ok("Pedido cancelado y stock liberado");
        } else {
            // Borrado normal (solo admin)
            servicePedidos.eliminar(id);
            return ResponseEntity.ok("Pedido eliminado");
        }
    }
}