package com.proyecto.WebRopa.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyecto.WebRopa.entity.*;
import com.proyecto.WebRopa.service.*;

@RestController
@RequestMapping("/api")
public class PagosController {

    private final IPagosService servicePagos;
    private final IPedidosService servicePedidos;
    private final IBoletasService serviceBoletas;
    private final IRecojoTiendaService serviceRecojoTienda;

    public PagosController(
            IPagosService servicePagos,
            IPedidosService servicePedidos,
            IBoletasService serviceBoletas,
            IRecojoTiendaService serviceRecojoTienda) {
        this.servicePagos = servicePagos;
        this.servicePedidos = servicePedidos;
        this.serviceBoletas = serviceBoletas;
        this.serviceRecojoTienda = serviceRecojoTienda;
    }

    // ── Ver todos los pagos (admin) ──────────────────
    @GetMapping("/pagos")
    public List<Pagos> listarTodos() {
        return servicePagos.buscarTodos();
    }

    // ── Ver pagos de un pedido ───────────────────────
    @GetMapping("/pagos/pedido/{pedidoId}")
    public ResponseEntity<?> buscarPorPedido(@PathVariable Long pedidoId) {
        List<Pagos> pagos = servicePagos.buscarPorPedidoId(pedidoId);
        if (pagos.isEmpty()) {
            return ResponseEntity.badRequest().body("No hay pagos para ese pedido");
        }
        return ResponseEntity.ok(pagos);
    }

    // ── Registrar pago ───────────────────────────────
    @org.springframework.transaction.annotation.Transactional
    @PostMapping("/pagos")
    public ResponseEntity<?> registrarPago(@RequestBody Pagos pago) {

        if (pago.getPedido() == null || pago.getPedido().getId() == null) {
            return ResponseEntity.badRequest().body("Debe especificar el pedido");
        }
        if (pago.getMetodo() == null) {
            return ResponseEntity.badRequest()
                .body("Debe especificar el método: TARJETA, YAPE, PLIN o TRANSFERENCIA");
        }

        Optional<Pedidos> pedidoOpt = servicePedidos.buscarId(pago.getPedido().getId());
        if (!pedidoOpt.isPresent()) {
            return ResponseEntity.badRequest().body("El pedido no existe");
        }

        Pedidos pedido = pedidoOpt.get();

        if (pedido.isPago_confirmado()) {
            return ResponseEntity.badRequest().body("Este pedido ya fue pagado");
        }
        if (pedido.getEstado() == Pedidos.Estado.CANCELADO) {
            return ResponseEntity.badRequest().body("No se puede pagar un pedido cancelado");
        }

        pago.setPedido(pedido);
        pago.setMonto(pedido.getTotal());
        
        boolean isTarjeta = (pago.getMetodo() == Pagos.Metodo.TARJETA);
        pago.setEstado(isTarjeta ? Pagos.Estado.APROBADO : Pagos.Estado.PENDIENTE);
        servicePagos.guardar(pago);

        Map<String, Object> response = new HashMap<>();
        Map<String, Object> pagoData = new HashMap<>();
        pagoData.put("monto", pago.getMonto());
        pagoData.put("metodo", pago.getMetodo().toString());
        pagoData.put("estado", pago.getEstado().toString());
        response.put("pago", pagoData);

        if (isTarjeta) {
            pedido.setPago_confirmado(true);
            pedido.setEstado(Pedidos.Estado.CONFIRMADO);
            servicePedidos.guardar(pedido);

            Boletas boleta = generarBoleta(pedido);
            RecojoTienda recojo = generarRecojo(pedido);
            
            Map<String, Object> boletaData = new HashMap<>();
            boletaData.put("numero_boleta", boleta.getNumero_boleta());
            boletaData.put("nombre_cliente", boleta.getNombre_cliente());
            boletaData.put("total", boleta.getTotal());
            response.put("boleta", boletaData);
            
            Map<String, Object> recojoData = new HashMap<>();
            recojoData.put("codigo_recojo", recojo.getCodigo_recojo());
            response.put("recojo", recojoData);
        }

        return ResponseEntity.ok(response);
    }

    // ── Modificar pago (admin/cajero) ────────────────
    @org.springframework.transaction.annotation.Transactional
    @PutMapping("/pagos")
    public ResponseEntity<?> modificarPago(@RequestBody Pagos pagoUpdate) {
        if (pagoUpdate.getId() == null) {
            return ResponseEntity.badRequest().body("Debe especificar el id del pago");
        }

        try {
            Optional<Pagos> pagoExistenteOpt = servicePagos.buscarId(pagoUpdate.getId());
            if (!pagoExistenteOpt.isPresent()) {
                return ResponseEntity.badRequest().body("El pago no existe");
            }
            
            Pagos pagoExistente = pagoExistenteOpt.get();
            boolean eraPendiente = (pagoExistente.getEstado() == Pagos.Estado.PENDIENTE);

            Pagos pagoModificado = servicePagos.modificar(pagoUpdate);

            if (eraPendiente && pagoModificado.getEstado() == Pagos.Estado.APROBADO) {
                Pedidos pedido = pagoModificado.getPedido();
                if (!pedido.isPago_confirmado()) {
                    pedido.setPago_confirmado(true);
                    pedido.setEstado(Pedidos.Estado.CONFIRMADO);
                    servicePedidos.guardar(pedido);

                    generarBoleta(pedido);
                    generarRecojo(pedido);
                }
            }

            return ResponseEntity.ok(pagoModificado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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
        boleta.setNumero_boleta("BOL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        serviceBoletas.guardar(boleta);
        return boleta;
    }

    private RecojoTienda generarRecojo(Pedidos pedido) {
        RecojoTienda recojo = new RecojoTienda();
        recojo.setPedido(pedido);
        recojo.setEstado(RecojoTienda.Estado.PENDIENTE);
        recojo.setCodigo_recojo("REC-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        serviceRecojoTienda.guardar(recojo);
        return recojo;
    }
}