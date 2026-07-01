package com.proyecto.WebRopa.controller;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.proyecto.WebRopa.entity.AuditoriaLog;
import com.proyecto.WebRopa.repository.AuditoriaLogRepository;

@RestController
@RequestMapping("/api/superadmin/auditoria")
public class SuperAdminAuditoriaController {

    @Autowired
    private AuditoriaLogRepository repo;

    @GetMapping
    public List<AuditoriaLog> listarLogs(
            @RequestParam(value = "desde", required = false) String desde,
            @RequestParam(value = "hasta", required = false) String hasta,
            @RequestParam(value = "usuario", required = false) String usuario) {
        List<AuditoriaLog> todos = repo.findAll();

        return todos.stream()
            .filter(log -> {
                if (usuario != null && !usuario.isEmpty() && !usuario.equalsIgnoreCase("todos")) {
                    return log.getAutorCorreo().equalsIgnoreCase(usuario.trim());
                }
                return true;
            })
            .filter(log -> {
                if (desde != null && !desde.isEmpty()) {
                    try {
                        java.time.LocalDate start = java.time.LocalDate.parse(desde);
                        return log.getFechaHora().toLocalDate().isAfter(start) || log.getFechaHora().toLocalDate().isEqual(start);
                    } catch (Exception e) {
                        return true;
                    }
                }
                return true;
            })
            .filter(log -> {
                if (hasta != null && !hasta.isEmpty()) {
                    try {
                        java.time.LocalDate end = java.time.LocalDate.parse(hasta);
                        return log.getFechaHora().toLocalDate().isBefore(end) || log.getFechaHora().toLocalDate().isEqual(end);
                    } catch (Exception e) {
                        return true;
                    }
                }
                return true;
            })
            .sorted((a, b) -> b.getFechaHora().compareTo(a.getFechaHora())) // Más reciente primero
            .collect(Collectors.toList());
    }
}
