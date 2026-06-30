package com.proyecto.WebRopa.service.jpa;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import org.springframework.stereotype.Service;
import com.proyecto.WebRopa.dto.ReportesDTOs.*;
import com.proyecto.WebRopa.entity.*;
import com.proyecto.WebRopa.security.TenantContext;
import com.proyecto.WebRopa.service.IReportesService;

@Service
public class ReportesService implements IReportesService {

    @PersistenceContext
    private EntityManager em;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public DashboardReporteDTO getDashboardReporte() {
        Long tenantId = TenantContext.getCurrentTenant();
        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);

        LocalDateTime startOfPrevMonth;
        if (month == 1) {
            startOfPrevMonth = LocalDateTime.of(year - 1, 12, 1, 0, 0, 0);
        } else {
            startOfPrevMonth = LocalDateTime.of(year, month - 1, 1, 0, 0, 0);
        }
        LocalDateTime endOfPrevMonth = startOfPrevMonth.plusMonths(1).minusNanos(1);

        // 1. Ingresos
        double ingresosMes = getIngresos(startOfMonth, endOfMonth, tenantId);
        double ingresosMesAnterior = getIngresos(startOfPrevMonth, endOfPrevMonth, tenantId);

        // 2. Pedidos Completados
        long pedidosCompletados = getPedidosCompletadosCount(startOfMonth, endOfMonth, tenantId);
        long pedidosMesAnterior = getPedidosCompletadosCount(startOfPrevMonth, endOfPrevMonth, tenantId);

        // 3. Ticket Promedio
        double ticketPromedio = pedidosCompletados > 0 ? ingresosMes / pedidosCompletados : 0.0;
        double ticketPromedioMesAnterior = pedidosMesAnterior > 0 ? ingresosMesAnterior / pedidosMesAnterior : 0.0;

        // 4. Devoluciones
        long devCount = getDevolucionesCount(startOfMonth, endOfMonth, tenantId);
        long devCountMesAnterior = getDevolucionesCount(startOfPrevMonth, endOfPrevMonth, tenantId);
        double tasaDevolucion = pedidosCompletados > 0 ? ((double) devCount / pedidosCompletados * 100) : 0.0;
        double tasaDevolucionMesAnterior = pedidosMesAnterior > 0 ? ((double) devCountMesAnterior / pedidosMesAnterior * 100) : 0.0;

        DashboardReporteDTO dto = new DashboardReporteDTO();
        dto.setIngresosMes(Math.round(ingresosMes * 100.0) / 100.0);
        dto.setPedidosCompletados(pedidosCompletados);
        dto.setTicketPromedio(Math.round(ticketPromedio * 100.0) / 100.0);
        dto.setTasaDevolucion(Math.round(tasaDevolucion * 10.0) / 10.0);

        // Deltas
        dto.setIngresosDelta(calculateDelta(ingresosMes, ingresosMesAnterior));
        dto.setPedidosDelta(calculateDelta(pedidosCompletados, pedidosMesAnterior));
        dto.setTicketDelta(calculateDelta(ticketPromedio, ticketPromedioMesAnterior));
        // Devolución delta (menor devolución es positivo, pero mostramos el porcentaje estándar)
        dto.setDevolucionDelta(calculateDelta(tasaDevolucion, tasaDevolucionMesAnterior));

        // 5. Tendencia Ventas (Últimos 7 días)
        List<VentaDiariaDTO> tendencia = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            LocalDateTime startOfDay = day.atStartOfDay();
            LocalDateTime endOfDay = day.atTime(LocalTime.MAX);
            double dailyRevenue = getIngresos(startOfDay, endOfDay, tenantId);
            tendencia.add(new VentaDiariaDTO(day.format(DATE_FORMATTER), Math.round(dailyRevenue * 100.0) / 100.0));
        }
        dto.setTendenciaVentas(tendencia);

        // 6. Top 5 Categorías
        dto.setTopCategorias(getTopCategorias(startOfMonth, endOfMonth, tenantId));

        return dto;
    }

    private double getIngresos(LocalDateTime start, LocalDateTime end, Long tenantId) {
        String jpql = "SELECT COALESCE(SUM(b.total), 0.0) FROM Boletas b JOIN b.pedido p " +
                      "WHERE b.fecha_emision >= :start AND b.fecha_emision <= :end";
        if (tenantId != null) {
            jpql += " AND p.empresa.id = :tenantId";
        }
        TypedQuery<Double> query = em.createQuery(jpql, Double.class)
                                     .setParameter("start", start)
                                     .setParameter("end", end);
        if (tenantId != null) {
            query.setParameter("tenantId", tenantId);
        }
        return query.getSingleResult();
    }

    private long getPedidosCompletadosCount(LocalDateTime start, LocalDateTime end, Long tenantId) {
        String jpql = "SELECT COUNT(p) FROM Pedidos p " +
                      "WHERE p.pago_confirmado = true AND p.fecha_creacion >= :start AND p.fecha_creacion <= :end";
        if (tenantId != null) {
            jpql += " AND p.empresa.id = :tenantId";
        }
        TypedQuery<Long> query = em.createQuery(jpql, Long.class)
                                   .setParameter("start", start)
                                   .setParameter("end", end);
        if (tenantId != null) {
            query.setParameter("tenantId", tenantId);
        }
        return query.getSingleResult();
    }

    private long getDevolucionesCount(LocalDateTime start, LocalDateTime end, Long tenantId) {
        String jpql = "SELECT COUNT(d) FROM Devoluciones d JOIN d.pedido p " +
                      "WHERE d.fecha >= :start AND d.fecha <= :end";
        if (tenantId != null) {
            jpql += " AND p.empresa.id = :tenantId";
        }
        TypedQuery<Long> query = em.createQuery(jpql, Long.class)
                                   .setParameter("start", start)
                                   .setParameter("end", end);
        if (tenantId != null) {
            query.setParameter("tenantId", tenantId);
        }
        return query.getSingleResult();
    }

    private String calculateDelta(double current, double previous) {
        if (previous == 0.0) {
            return current > 0.0 ? "▲ +100% vs mes anterior" : "0% vs mes anterior";
        }
        double diff = current - previous;
        double pct = (diff / previous) * 100.0;
        if (pct >= 0) {
            return String.format("▲ +%.1f%% vs mes anterior", pct);
        } else {
            return String.format("▼ %.1f%% vs mes anterior", pct);
        }
    }

    private List<CategoriaTopDTO> getTopCategorias(LocalDateTime start, LocalDateTime end, Long tenantId) {
        String totJpql = "SELECT COALESCE(SUM(pi.cantidad), 0L) FROM PedidosItems pi " +
                         "WHERE pi.pedido.pago_confirmado = true AND pi.pedido.fecha_creacion >= :start AND pi.pedido.fecha_creacion <= :end";
        if (tenantId != null) {
            totJpql += " AND pi.pedido.empresa.id = :tenantId";
        }
        TypedQuery<Long> qTot = em.createQuery(totJpql, Long.class)
                                  .setParameter("start", start)
                                  .setParameter("end", end);
        if (tenantId != null) {
            qTot.setParameter("tenantId", tenantId);
        }
        long totalQty = qTot.getSingleResult();

        String jpql = "SELECT pi.variante.producto.categoria.nombre, SUM(pi.cantidad) " +
                      "FROM PedidosItems pi " +
                      "WHERE pi.pedido.pago_confirmado = true AND pi.pedido.fecha_creacion >= :start AND pi.pedido.fecha_creacion <= :end";
        if (tenantId != null) {
            jpql += " AND pi.pedido.empresa.id = :tenantId";
        }
        jpql += " GROUP BY pi.variante.producto.categoria.nombre ORDER BY SUM(pi.cantidad) DESC";

        TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class)
                                       .setParameter("start", start)
                                       .setParameter("end", end);
        if (tenantId != null) {
            query.setParameter("tenantId", tenantId);
        }
        List<Object[]> results = query.setMaxResults(5).getResultList();

        List<CategoriaTopDTO> list = new ArrayList<>();
        for (Object[] res : results) {
            String name = (String) res[0];
            if (name == null) name = "Sin categoría";
            int qty = ((Number) res[1]).intValue();
            double pct = totalQty > 0 ? ((double) qty / totalQty * 100.0) : 0.0;
            list.add(new CategoriaTopDTO(name, Math.round(pct * 10.0) / 10.0, qty));
        }
        return list;
    }

    @Override
    public List<BoletaFinancieraDTO> getVentasFinancieras(LocalDate fechaInicio, LocalDate fechaFin, String estadoPedido) {
        Long tenantId = TenantContext.getCurrentTenant();

        String jpql = "SELECT b FROM Boletas b JOIN b.pedido p WHERE 1=1";
        if (tenantId != null) {
            jpql += " AND p.empresa.id = :tenantId";
        }
        if (fechaInicio != null) {
            jpql += " AND b.fecha_emision >= :start";
        }
        if (fechaFin != null) {
            jpql += " AND b.fecha_emision <= :end";
        }
        if (estadoPedido != null && !estadoPedido.equalsIgnoreCase("Todos")) {
            jpql += " AND p.estado = :estado";
        }
        jpql += " ORDER BY b.fecha_emision DESC";

        TypedQuery<Boletas> query = em.createQuery(jpql, Boletas.class);
        if (fechaInicio != null) {
            query.setParameter("start", fechaInicio.atStartOfDay());
        }
        if (fechaFin != null) {
            query.setParameter("end", fechaFin.atTime(LocalTime.MAX));
        }
        if (estadoPedido != null && !estadoPedido.equalsIgnoreCase("Todos")) {
            try {
                query.setParameter("estado", Pedidos.Estado.valueOf(estadoPedido.toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Ignore or format correctly
            }
        }
        if (tenantId != null) {
            query.setParameter("tenantId", tenantId);
        }

        List<Boletas> boletasList = query.getResultList();
        List<BoletaFinancieraDTO> response = new ArrayList<>();

        for (Boletas b : boletasList) {
            // Obtener el método de pago del pedido
            String metodoPago = "No registrado";
            String pagoJpql = "SELECT pa.metodo FROM Pagos pa WHERE pa.pedido.id = :pedidoId ORDER BY pa.fecha DESC";
            TypedQuery<Pagos.Metodo> qPago = em.createQuery(pagoJpql, Pagos.Metodo.class)
                                                .setParameter("pedidoId", b.getPedido().getId())
                                                .setMaxResults(1);
            List<Pagos.Metodo> pagos = qPago.getResultList();
            if (!pagos.isEmpty()) {
                metodoPago = pagos.get(0).toString();
            }

            response.add(new BoletaFinancieraDTO(
                b.getFecha_emision().format(DATE_TIME_FORMATTER),
                b.getNumero_boleta(),
                b.getNombre_cliente(),
                b.getSubtotal(),
                b.getIgv(),
                b.getTotal(),
                metodoPago
            ));
        }
        return response;
    }

    @Override
    public List<ProductoTopDTO> getTopProductos() {
        Long tenantId = TenantContext.getCurrentTenant();

        String jpql = "SELECT pi.variante.producto.nombre, pi.variante.talla, pi.variante.color, " +
                      "SUM(pi.cantidad), SUM(pi.cantidad * pi.precio_unitario) " +
                      "FROM PedidosItems pi " +
                      "WHERE pi.pedido.pago_confirmado = true";
        if (tenantId != null) {
            jpql += " AND pi.pedido.empresa.id = :tenantId";
        }
        jpql += " GROUP BY pi.variante.producto.nombre, pi.variante.talla, pi.variante.color " +
                "ORDER BY SUM(pi.cantidad) DESC";

        TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);
        if (tenantId != null) {
            query.setParameter("tenantId", tenantId);
        }

        List<Object[]> results = query.setMaxResults(20).getResultList();
        List<ProductoTopDTO> list = new ArrayList<>();
        int ranking = 1;
        for (Object[] res : results) {
            String name = (String) res[0];
            String talla = (String) res[1];
            String color = (String) res[2];
            int unidades = ((Number) res[3]).intValue();
            double ingresos = ((Number) res[4]).doubleValue();

            String variante = talla + " / " + color;
            list.add(new ProductoTopDTO(
                ranking++,
                name,
                variante,
                unidades,
                Math.round(ingresos * 100.0) / 100.0
            ));
        }
        return list;
    }

    @Override
    public InventarioReporteDTO getInventarioReporte() {
        Long tenantId = TenantContext.getCurrentTenant();
        InventarioReporteDTO report = new InventarioReporteDTO();

        // 1. Alertas Stock
        String stockJpql = "SELECT v FROM Variantes v WHERE v.stock <= v.stock_critico AND v.estado = 'ACTIVO'";
        if (tenantId != null) {
            stockJpql += " AND v.empresa.id = :tenantId";
        }
        stockJpql += " ORDER BY v.stock ASC";
        TypedQuery<Variantes> qStock = em.createQuery(stockJpql, Variantes.class);
        if (tenantId != null) {
            qStock.setParameter("tenantId", tenantId);
        }
        List<Variantes> stockList = qStock.getResultList();
        List<StockCriticoDTO> stockDtos = new ArrayList<>();
        for (Variantes v : stockList) {
            String prodNombre = v.getProducto().getNombre() + " (" + v.getTalla() + " / " + v.getColor() + ")";
            stockDtos.add(new StockCriticoDTO(prodNombre, v.getId(), v.getStock(), v.getStock_critico()));
        }
        report.setAlertasStock(stockDtos);

        // 2. Distribución de movimientos
        String movDistJpql = "SELECT im.tipo_movimiento, COUNT(im) FROM InventarioMovimientos im JOIN im.variante v ";
        if (tenantId != null) {
            movDistJpql += "WHERE v.empresa.id = :tenantId ";
        }
        movDistJpql += "GROUP BY im.tipo_movimiento";
        TypedQuery<Object[]> qMovDist = em.createQuery(movDistJpql, Object[].class);
        if (tenantId != null) {
            qMovDist.setParameter("tenantId", tenantId);
        }
        List<Object[]> distResults = qMovDist.getResultList();
        Map<String, Integer> distMap = new HashMap<>();
        for (Object[] res : distResults) {
            distMap.put(res[0].toString(), ((Number) res[1]).intValue());
        }
        report.setDistribucionMovimientos(distMap);

        // 3. Últimos movimientos
        String movsJpql = "SELECT im FROM InventarioMovimientos im JOIN im.variante v ";
        if (tenantId != null) {
            movsJpql += "WHERE v.empresa.id = :tenantId ";
        }
        movsJpql += "ORDER BY im.fecha DESC";
        TypedQuery<InventarioMovimientos> qMovs = em.createQuery(movsJpql, InventarioMovimientos.class);
        if (tenantId != null) {
            qMovs.setParameter("tenantId", tenantId);
        }
        List<InventarioMovimientos> movsList = qMovs.setMaxResults(50).getResultList();
        List<MovimientoDetalleDTO> movDtos = new ArrayList<>();
        for (InventarioMovimientos im : movsList) {
            String prodVar = im.getVariante().getProducto().getNombre() + " (" + im.getVariante().getTalla() + " / " + im.getVariante().getColor() + ")";
            movDtos.add(new MovimientoDetalleDTO(
                im.getFecha().format(DATE_TIME_FORMATTER),
                im.getTipo_movimiento().toString(),
                prodVar,
                im.getCantidad(),
                im.getObservacion()
            ));
        }
        report.setUltimosMovimientos(movDtos);

        return report;
    }

    @Override
    public FidelizacionReporteDTO getFidelizacionReporte() {
        Long tenantId = TenantContext.getCurrentTenant();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = LocalDateTime.of(now.getYear(), now.getMonthValue(), 1, 0, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);

        FidelizacionReporteDTO report = new FidelizacionReporteDTO();

        // 1. Cupones usados en el mes
        String cuJpql = "SELECT COUNT(cu) FROM CuponUsos cu JOIN cu.pedido p " +
                        "WHERE cu.fecha >= :start AND cu.fecha <= :end";
        if (tenantId != null) {
            cuJpql += " AND p.empresa.id = :tenantId";
        }
        TypedQuery<Long> qCu = em.createQuery(cuJpql, Long.class)
                                 .setParameter("start", startOfMonth)
                                 .setParameter("end", endOfMonth);
        if (tenantId != null) {
            qCu.setParameter("tenantId", tenantId);
        }
        report.setCuponesUsadosMes(qCu.getSingleResult());

        // 2. Ahorro de clientes (descuentos en el mes)
        String ahJpql = "SELECT COALESCE(SUM(p.descuento), 0.0) FROM Pedidos p " +
                        "WHERE p.pago_confirmado = true AND p.fecha_creacion >= :start AND p.fecha_creacion <= :end";
        if (tenantId != null) {
            ahJpql += " AND p.empresa.id = :tenantId";
        }
        TypedQuery<Double> qAh = em.createQuery(ahJpql, Double.class)
                                 .setParameter("start", startOfMonth)
                                 .setParameter("end", endOfMonth);
        if (tenantId != null) {
            qAh.setParameter("tenantId", tenantId);
        }
        report.setAhorroClientes(Math.round(qAh.getSingleResult() * 100.0) / 100.0);

        // 3. Efectividad de cupones (todos los tiempos)
        String efJpql = "SELECT p.cupon.codigo, p.cupon.tipo, p.cupon.valor, COUNT(p), SUM(p.descuento), p.cupon.estado " +
                        "FROM Pedidos p WHERE p.cupon IS NOT NULL AND p.pago_confirmado = true";
        if (tenantId != null) {
            efJpql += " AND p.empresa.id = :tenantId";
        }
        efJpql += " GROUP BY p.cupon.codigo, p.cupon.tipo, p.cupon.valor, p.cupon.estado";

        TypedQuery<Object[]> qEf = em.createQuery(efJpql, Object[].class);
        if (tenantId != null) {
            qEf.setParameter("tenantId", tenantId);
        }
        List<Object[]> efResults = qEf.getResultList();
        List<CuponEfectividadDTO> efDtos = new ArrayList<>();
        for (Object[] res : efResults) {
            String codigo = (String) res[0];
            String tipo = res[1].toString();
            double valor = ((Number) res[2]).doubleValue();
            int count = ((Number) res[3]).intValue();
            double totalDesc = ((Number) res[4]).doubleValue();
            String estado = res[5].toString();

            String descStr = tipo.equals("PORCENTAJE") ? valor + "%" : "S/ " + valor;
            efDtos.add(new CuponEfectividadDTO(
                codigo,
                descStr,
                count,
                Math.round(totalDesc * 100.0) / 100.0,
                estado
            ));
        }
        report.setEfectividadCupones(efDtos);

        return report;
    }

    @Override
    public DevolucionesReporteDTO getDevolucionesReporte() {
        Long tenantId = TenantContext.getCurrentTenant();
        DevolucionesReporteDTO report = new DevolucionesReporteDTO();

        // 1. Motivos frecuentes
        String motJpql = "SELECT d.motivo, COUNT(d) FROM Devoluciones d JOIN d.pedido p ";
        if (tenantId != null) {
            motJpql += "WHERE p.empresa.id = :tenantId ";
        }
        motJpql += "GROUP BY d.motivo";
        TypedQuery<Object[]> qMot = em.createQuery(motJpql, Object[].class);
        if (tenantId != null) {
            qMot.setParameter("tenantId", tenantId);
        }
        List<Object[]> motResults = qMot.getResultList();
        Map<String, Integer> motMap = new HashMap<>();
        for (Object[] res : motResults) {
            motMap.put(res[0].toString(), ((Number) res[1]).intValue());
        }
        report.setMotivosFrecuentes(motMap);

        // 2. Últimas solicitudes de devolución
        String listJpql = "SELECT d FROM Devoluciones d JOIN d.pedido p ";
        if (tenantId != null) {
            listJpql += "WHERE p.empresa.id = :tenantId ";
        }
        listJpql += "ORDER BY d.fecha DESC";
        TypedQuery<Devoluciones> qList = em.createQuery(listJpql, Devoluciones.class);
        if (tenantId != null) {
            qList.setParameter("tenantId", tenantId);
        }
        List<Devoluciones> devList = qList.setMaxResults(50).getResultList();
        List<DevolucionDetalleDTO> devDtos = new ArrayList<>();
        for (Devoluciones d : devList) {
            String prodNombre = d.getPedidoItem().getVariante().getProducto().getNombre() + 
                                " (" + d.getPedidoItem().getVariante().getTalla() + " / " + d.getPedidoItem().getVariante().getColor() + ")";
            devDtos.add(new DevolucionDetalleDTO(
                d.getId(),
                d.getFecha().format(DATE_TIME_FORMATTER),
                d.getMotivo().toString(),
                d.getEstado().toString(),
                d.getCantidad_devuelta(),
                d.getMonto_reembolso(),
                prodNombre
            ));
        }
        report.setUltimasSolicitudes(devDtos);

        return report;
    }
}
