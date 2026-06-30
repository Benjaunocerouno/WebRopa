package com.proyecto.WebRopa.service;

import java.time.LocalDate;
import java.util.List;
import com.proyecto.WebRopa.dto.ReportesDTOs.*;

public interface IReportesService {
    DashboardReporteDTO getDashboardReporte();
    List<BoletaFinancieraDTO> getVentasFinancieras(LocalDate fechaInicio, LocalDate fechaFin, String estadoPedido);
    List<ProductoTopDTO> getTopProductos();
    InventarioReporteDTO getInventarioReporte();
    FidelizacionReporteDTO getFidelizacionReporte();
    DevolucionesReporteDTO getDevolucionesReporte();
}
