package com.proyecto.WebRopa.dto;

import java.util.List;
import java.util.Map;

public class ReportesDTOs {

    // 1. DASHBOARD GENERAL
    public static class DashboardReporteDTO {
        private double ingresosMes;
        private long pedidosCompletados;
        private double ticketPromedio;
        private double tasaDevolucion;
        
        private String ingresosDelta;
        private String pedidosDelta;
        private String ticketDelta;
        private String devolucionDelta;

        private List<VentaDiariaDTO> tendenciaVentas;
        private List<CategoriaTopDTO> topCategorias;

        // Getters y Setters
        public double getIngresosMes() { return ingresosMes; }
        public void setIngresosMes(double ingresosMes) { this.ingresosMes = ingresosMes; }
        public long getPedidosCompletados() { return pedidosCompletados; }
        public void setPedidosCompletados(long pedidosCompletados) { this.pedidosCompletados = pedidosCompletados; }
        public double getTicketPromedio() { return ticketPromedio; }
        public void setTicketPromedio(double ticketPromedio) { this.ticketPromedio = ticketPromedio; }
        public double getTasaDevolucion() { return tasaDevolucion; }
        public void setTasaDevolucion(double tasaDevolucion) { this.tasaDevolucion = tasaDevolucion; }
        public String getIngresosDelta() { return ingresosDelta; }
        public void setIngresosDelta(String ingresosDelta) { this.ingresosDelta = ingresosDelta; }
        public String getPedidosDelta() { return pedidosDelta; }
        public void setPedidosDelta(String pedidosDelta) { this.pedidosDelta = pedidosDelta; }
        public String getTicketDelta() { return ticketDelta; }
        public void setTicketDelta(String ticketDelta) { this.ticketDelta = ticketDelta; }
        public String getDevolucionDelta() { return devolucionDelta; }
        public void setDevolucionDelta(String devolucionDelta) { this.devolucionDelta = devolucionDelta; }
        public List<VentaDiariaDTO> getTendenciaVentas() { return tendenciaVentas; }
        public void setTendenciaVentas(List<VentaDiariaDTO> tendenciaVentas) { this.tendenciaVentas = tendenciaVentas; }
        public List<CategoriaTopDTO> getTopCategorias() { return topCategorias; }
        public void setTopCategorias(List<CategoriaTopDTO> topCategorias) { this.topCategorias = topCategorias; }
    }

    public static class VentaDiariaDTO {
        private String fecha;
        private double total;

        public VentaDiariaDTO(String fecha, double total) {
            this.fecha = fecha;
            this.total = total;
        }

        public String getFecha() { return fecha; }
        public double getTotal() { return total; }
    }

    public static class CategoriaTopDTO {
        private String categoria;
        private double porcentaje;
        private int cantidad;

        public CategoriaTopDTO(String categoria, double porcentaje, int cantidad) {
            this.categoria = categoria;
            this.porcentaje = porcentaje;
            this.cantidad = cantidad;
        }

        public String getCategoria() { return categoria; }
        public double getPorcentaje() { return porcentaje; }
        public int getCantidad() { return cantidad; }
    }

    // 2. REPORTE DE VENTAS FINANCIERO
    public static class BoletaFinancieraDTO {
        private String fecha;
        private String numeroBoleta;
        private String cliente;
        private double subtotal;
        private double igv;
        private double total;
        private String metodoPago;

        public BoletaFinancieraDTO(String fecha, String numeroBoleta, String cliente, double subtotal, double igv, double total, String metodoPago) {
            this.fecha = fecha;
            this.numeroBoleta = numeroBoleta;
            this.cliente = cliente;
            this.subtotal = subtotal;
            this.igv = igv;
            this.total = total;
            this.metodoPago = metodoPago;
        }

        public String getFecha() { return fecha; }
        public String getNumeroBoleta() { return numeroBoleta; }
        public String getCliente() { return cliente; }
        public double getSubtotal() { return subtotal; }
        public double getIgv() { return igv; }
        public double getTotal() { return total; }
        public String getMetodoPago() { return metodoPago; }
    }

    // 3. TOP PRODUCTOS
    public static class ProductoTopDTO {
        private int ranking;
        private String nombre;
        private String variante; // e.g. "M / Blanco"
        private int unidadesVendidas;
        private double ingresoGenerado;

        public ProductoTopDTO(int ranking, String nombre, String variante, int unidadesVendidas, double ingresoGenerado) {
            this.ranking = ranking;
            this.nombre = nombre;
            this.variante = variante;
            this.unidadesVendidas = unidadesVendidas;
            this.ingresoGenerado = ingresoGenerado;
        }

        public int getRanking() { return ranking; }
        public String getNombre() { return nombre; }
        public String getVariante() { return variante; }
        public int getUnidadesVendidas() { return unidadesVendidas; }
        public double getIngresoGenerado() { return ingresoGenerado; }
    }

    // 4. INVENTARIO
    public static class InventarioReporteDTO {
        private List<StockCriticoDTO> alertasStock;
        private Map<String, Integer> distribucionMovimientos;
        private List<MovimientoDetalleDTO> ultimosMovimientos;

        public List<StockCriticoDTO> getAlertasStock() { return alertasStock; }
        public void setAlertasStock(List<StockCriticoDTO> alertasStock) { this.alertasStock = alertasStock; }
        public Map<String, Integer> getDistribucionMovimientos() { return distribucionMovimientos; }
        public void setDistribucionMovimientos(Map<String, Integer> distribucionMovimientos) { this.distribucionMovimientos = distribucionMovimientos; }
        public List<MovimientoDetalleDTO> getUltimosMovimientos() { return ultimosMovimientos; }
        public void setUltimosMovimientos(List<MovimientoDetalleDTO> ultimosMovimientos) { this.ultimosMovimientos = ultimosMovimientos; }
    }

    public static class StockCriticoDTO {
        private String nombre;
        private Long varianteId;
        private int stock;
        private int stockCritico;

        public StockCriticoDTO(String nombre, Long varianteId, int stock, int stockCritico) {
            this.nombre = nombre;
            this.varianteId = varianteId;
            this.stock = stock;
            this.stockCritico = stockCritico;
        }

        public String getNombre() { return nombre; }
        public Long getVarianteId() { return varianteId; }
        public int getStock() { return stock; }
        public int getStockCritico() { return stockCritico; }
    }

    public static class MovimientoDetalleDTO {
        private String fecha;
        private String tipoMovimiento;
        private String productoVariante;
        private int cantidad;
        private String observacion;

        public MovimientoDetalleDTO(String fecha, String tipoMovimiento, String productoVariante, int cantidad, String observacion) {
            this.fecha = fecha;
            this.tipoMovimiento = tipoMovimiento;
            this.productoVariante = productoVariante;
            this.cantidad = cantidad;
            this.observacion = observacion;
        }

        public String getFecha() { return fecha; }
        public String getTipoMovimiento() { return tipoMovimiento; }
        public String getProductoVariante() { return productoVariante; }
        public int getCantidad() { return cantidad; }
        public String getObservacion() { return observacion; }
    }

    // 5. FIDELIZACIÓN (CLIENTES / CUPONES)
    public static class FidelizacionReporteDTO {
        private long cuponesUsadosMes;
        private double ahorroClientes;
        private List<CuponEfectividadDTO> efectividadCupones;

        public long getCuponesUsadosMes() { return cuponesUsadosMes; }
        public void setCuponesUsadosMes(long cuponesUsadosMes) { this.cuponesUsadosMes = cuponesUsadosMes; }
        public double getAhorroClientes() { return ahorroClientes; }
        public void setAhorroClientes(double ahorroClientes) { this.ahorroClientes = ahorroClientes; }
        public List<CuponEfectividadDTO> getEfectividadCupones() { return efectividadCupones; }
        public void setEfectividadCupones(List<CuponEfectividadDTO> efectividadCupones) { this.efectividadCupones = efectividadCupones; }
    }

    public static class CuponEfectividadDTO {
        private String codigo;
        private String descuento;
        private int vecesUsado;
        private double totalDescontado;
        private String estado;

        public CuponEfectividadDTO(String codigo, String descuento, int vecesUsado, double totalDescontado, String estado) {
            this.codigo = codigo;
            this.descuento = descuento;
            this.vecesUsado = vecesUsado;
            this.totalDescontado = totalDescontado;
            this.estado = estado;
        }

        public String getCodigo() { return codigo; }
        public String getDescuento() { return descuento; }
        public int getVecesUsado() { return vecesUsado; }
        public double getTotalDescontado() { return totalDescontado; }
        public String getEstado() { return estado; }
    }

    // 6. DEVOLUCIONES
    public static class DevolucionesReporteDTO {
        private Map<String, Integer> motivosFrecuentes;
        private List<DevolucionDetalleDTO> ultimasSolicitudes;

        public Map<String, Integer> getMotivosFrecuentes() { return motivosFrecuentes; }
        public void setMotivosFrecuentes(Map<String, Integer> motivosFrecuentes) { this.motivosFrecuentes = motivosFrecuentes; }
        public List<DevolucionDetalleDTO> getUltimasSolicitudes() { return ultimasSolicitudes; }
        public void setUltimasSolicitudes(List<DevolucionDetalleDTO> ultimasSolicitudes) { this.ultimasSolicitudes = ultimasSolicitudes; }
    }

    public static class DevolucionDetalleDTO {
        private Long id;
        private String fecha;
        private String motivo;
        private String estado;
        private int cantidad;
        private double monto;
        private String productoNombre;

        public DevolucionDetalleDTO(Long id, String fecha, String motivo, String estado, int cantidad, double monto, String productoNombre) {
            this.id = id;
            this.fecha = fecha;
            this.motivo = motivo;
            this.estado = estado;
            this.cantidad = cantidad;
            this.monto = monto;
            this.productoNombre = productoNombre;
        }

        public Long getId() { return id; }
        public String getFecha() { return fecha; }
        public String getMotivo() { return motivo; }
        public String getEstado() { return estado; }
        public int getCantidad() { return cantidad; }
        public double getMonto() { return monto; }
        public String getProductoNombre() { return productoNombre; }
    }
}
