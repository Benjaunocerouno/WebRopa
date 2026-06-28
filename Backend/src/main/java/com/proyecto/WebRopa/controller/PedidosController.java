package com.proyecto.WebRopa.controller;

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
    private final IPedidoEstadoService pedidoEstadoService;

    public PedidosController(
            IPedidosService servicePedidos,
            IPedidosItemsService servicePedidosItems,
            ICarritosService serviceCarritos,
            ICarritos_itemsService serviceCarritosItems,
            ICuponesService serviceCupones,
            ICuponUsosService serviceCuponUsos,
            IVariantesService serviceVariantes,
            IPedidoEstadoService pedidoEstadoService) {
        this.servicePedidos = servicePedidos;
        this.servicePedidosItems = servicePedidosItems;
        this.serviceCarritos = serviceCarritos;
        this.serviceCarritosItems = serviceCarritosItems;
        this.serviceCupones = serviceCupones;
        this.serviceCuponUsos = serviceCuponUsos;
        this.serviceVariantes = serviceVariantes;
        this.pedidoEstadoService = pedidoEstadoService;
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

        if (pedido.getUsuario() == null || pedido.getUsuario().getId() == null) {
            return ResponseEntity.badRequest().body("Debe especificar el usuario");
        }

        Optional<Carritos> carrito = serviceCarritos.buscarPorUsuarioId(pedido.getUsuario().getId());
        if (!carrito.isPresent()) {
            return ResponseEntity.badRequest().body("El usuario no tiene un carrito");
        }

        List<Carritositems> itemsCarrito = serviceCarritosItems.buscarPorCarritoId(carrito.get().getId());
        if (itemsCarrito.isEmpty()) {
            return ResponseEntity.badRequest().body("El carrito está vacío");
        }

        for (Carritositems item : itemsCarrito) {
            Variantes variante = item.getVariante();
            if (variante.getStock() < item.getCantidad()) {
                return ResponseEntity.badRequest()
                    .body("Stock insuficiente para: " + variante.getSku()
                          + " (disponible: " + variante.getStock() + ")");
            }
        }

        double subtotal = 0.0;
        for (Carritositems item : itemsCarrito) {
            subtotal += item.getVariante().getProducto().getPrecio() * item.getCantidad();
        }

        double descuento = 0.0;
        Cupones cuponAplicado = null;

        if (pedido.getCupon() != null && pedido.getCupon().getCodigo() != null) {
            Optional<Cupones> cuponOpt = serviceCupones.buscarTodos().stream()
                    .filter(c -> c.getCodigo().equalsIgnoreCase(pedido.getCupon().getCodigo().trim()))
                    .findFirst();

            if (!cuponOpt.isPresent()) {
                return ResponseEntity.badRequest().body("El cupón no existe");
            }

            cuponAplicado = cuponOpt.get();

            if (cuponAplicado.getEstado() != Cupones.Estado.ACTIVO) {
                return ResponseEntity.badRequest().body("El cupón no está activo");
            }

            if (cuponAplicado.getUsos_maximos() != null &&
                cuponAplicado.getUsos_actuales() >= cuponAplicado.getUsos_maximos()) {
                return ResponseEntity.badRequest().body("El cupón ya no tiene usos disponibles");
            }

            Optional<CuponUsos> usoExistente = serviceCuponUsos.buscarPorCuponYUsuario(
                cuponAplicado.getId(), pedido.getUsuario().getId());
            if (usoExistente.isPresent()) {
                return ResponseEntity.badRequest().body("Ya usaste este cupón anteriormente");
            }

            if (cuponAplicado.getMinimo_compra() != null &&
                subtotal < cuponAplicado.getMinimo_compra()) {
                return ResponseEntity.badRequest()
                    .body("El pedido no cumple el mínimo de S/ " + cuponAplicado.getMinimo_compra());
            }

            if (cuponAplicado.getTipo() == Cupones.Tipo.PORCENTAJE) {
                descuento = subtotal * (cuponAplicado.getValor() / 100);
            } else {
                descuento = cuponAplicado.getValor();
            }
        }

        Double costoEnvio = pedido.getCosto_envio();
        double total = subtotal - descuento + (costoEnvio != null ? costoEnvio : 0.0);

        pedido.setSubtotal(subtotal);
        pedido.setDescuento(descuento);
        pedido.setTotal(total);
        pedido.setEstado(Pedidos.Estado.PENDIENTE);
        pedido.setCupon(cuponAplicado);

        if (!itemsCarrito.isEmpty()) {
            Empresas emp = itemsCarrito.get(0).getVariante().getEmpresa();
            if (emp == null && itemsCarrito.get(0).getVariante().getProducto() != null) {
                emp = itemsCarrito.get(0).getVariante().getProducto().getEmpresa();
            }
            pedido.setEmpresa(emp);
        }

        servicePedidos.guardar(pedido);

        for (Carritositems item : itemsCarrito) {
            PedidosItems pedidoItem = new PedidosItems();
            pedidoItem.setPedido(pedido);
            pedidoItem.setVariante(item.getVariante());
            pedidoItem.setCantidad(item.getCantidad());
            pedidoItem.setPrecio_unitario(item.getVariante().getProducto().getPrecio());
            servicePedidosItems.guardar(pedidoItem);

            Variantes variante = item.getVariante();
            variante.setStock(variante.getStock() - item.getCantidad());
            serviceVariantes.guardar(variante);
        }

        if (cuponAplicado != null) {
            CuponUsos uso = new CuponUsos();
            uso.setCupon(cuponAplicado);
            uso.setUsuario(pedido.getUsuario());
            uso.setPedido(pedido);
            serviceCuponUsos.registrarUso(uso);

            cuponAplicado.setUsos_actuales(cuponAplicado.getUsos_actuales() + 1);
            serviceCupones.guardar(cuponAplicado);
        }

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

            if (nuevo != Pedidos.Estado.CANCELADO && !existente.isPago_confirmado()) {
                return ResponseEntity.badRequest()
                    .body("No se puede avanzar el estado: el pago del pedido no ha sido confirmado");
            }

            existente.setEstado(nuevo);

            switch (nuevo) {
                case CONFIRMADO         -> pedidoEstadoService.aplicarEfectosConfirmado(existente);
                case LISTO_PARA_RECOGER -> pedidoEstadoService.aplicarEfectosListoParaRecoger(existente);
                case RECOGIDO           -> pedidoEstadoService.aplicarEfectosRecogido(existente);
                case CANCELADO          -> pedidoEstadoService.aplicarEfectosCancelado(existente, "Pedido cancelado por administrador");
                default -> { }
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
            case EN_PREPARACION     -> nuevo == Pedidos.Estado.LISTO_PARA_RECOGER || nuevo == Pedidos.Estado.EN_CAMINO || nuevo == Pedidos.Estado.CANCELADO;
            case LISTO_PARA_RECOGER -> nuevo == Pedidos.Estado.RECOGIDO          || nuevo == Pedidos.Estado.NO_RECOGIDO || nuevo == Pedidos.Estado.CANCELADO;
            case NO_RECOGIDO        -> nuevo == Pedidos.Estado.LISTO_PARA_RECOGER || nuevo == Pedidos.Estado.CANCELADO;
            case EN_CAMINO          -> nuevo == Pedidos.Estado.ENTREGADO         || nuevo == Pedidos.Estado.CANCELADO;
            case RECOGIDO, ENTREGADO, CANCELADO -> false;
        };
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
            servicePedidos.eliminar(id);
            return ResponseEntity.ok("Pedido eliminado");
        }
    }
}
