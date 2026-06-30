package com.proyecto.WebRopa.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.proyecto.WebRopa.dto.ReportesDTOs.*;
import com.proyecto.WebRopa.service.IReportesService;

@RestController
@RequestMapping("/api/reportes")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAuthority('REPORTES_VER') or hasAuthority('ADMIN') or hasAuthority('SUPERADMIN')")
public class ReportesController {

    private final IReportesService reportesService;

    public ReportesController(IReportesService reportesService) {
        this.reportesService = reportesService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardReporteDTO> getDashboard() {
        return ResponseEntity.ok(reportesService.getDashboardReporte());
    }

    @GetMapping("/ventas")
    public ResponseEntity<?> getVentas(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(required = false, defaultValue = "Todos") String estadoPedido) {
        
        LocalDate start = null;
        LocalDate end = null;
        try {
            if (fechaInicio != null && !fechaInicio.isEmpty()) {
                start = LocalDate.parse(fechaInicio);
            }
            if (fechaFin != null && !fechaFin.isEmpty()) {
                end = LocalDate.parse(fechaFin);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Formato de fecha inválido. Use yyyy-MM-dd");
        }
        
        List<BoletaFinancieraDTO> ventas = reportesService.getVentasFinancieras(start, end, estadoPedido);
        return ResponseEntity.ok(ventas);
    }

    @GetMapping("/productos")
    public ResponseEntity<List<ProductoTopDTO>> getTopProductos() {
        return ResponseEntity.ok(reportesService.getTopProductos());
    }

    @GetMapping("/inventario")
    public ResponseEntity<InventarioReporteDTO> getInventarioReporte() {
        return ResponseEntity.ok(reportesService.getInventarioReporte());
    }

    @GetMapping("/clientes")
    public ResponseEntity<FidelizacionReporteDTO> getFidelizacionReporte() {
        return ResponseEntity.ok(reportesService.getFidelizacionReporte());
    }

    @GetMapping("/devoluciones")
    public ResponseEntity<DevolucionesReporteDTO> getDevolucionesReporte() {
        return ResponseEntity.ok(reportesService.getDevolucionesReporte());
    }
}
