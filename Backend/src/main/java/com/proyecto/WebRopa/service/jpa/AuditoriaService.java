package com.proyecto.WebRopa.service.jpa;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import com.proyecto.WebRopa.entity.AuditoriaLog;
import com.proyecto.WebRopa.entity.Usuarios;
import com.proyecto.WebRopa.repository.AuditoriaLogRepository;
import com.proyecto.WebRopa.service.IUsuariosService;
import java.time.LocalDateTime;
import java.util.Optional;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class AuditoriaService {

    @Autowired
    private AuditoriaLogRepository repo;

    @Autowired
    private IUsuariosService serviceUsuarios;

    @Autowired
    private HttpServletRequest request;

    public void registrar(String accion, String entidad, String detalle) {
        String correo = "sistema";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null && !auth.getName().equals("anonymousUser")) {
            correo = auth.getName();
        }

        AuditoriaLog log = new AuditoriaLog();
        log.setFechaHora(LocalDateTime.now());
        log.setAutorCorreo(correo);
        log.setAccion(accion);
        log.setEntidad(entidad);
        log.setDetalle(detalle);

        if ("sistema".equalsIgnoreCase(correo)) {
            log.setAutorNombre("Sistema");
            log.setAutorRol("SISTEMA");
        } else {
            Optional<Usuarios> uOpt = serviceUsuarios.buscarPorCorreo(correo);
            if (uOpt.isPresent()) {
                log.setAutorNombre(uOpt.get().getNombre());
                log.setAutorRol(uOpt.get().getRol() != null ? uOpt.get().getRol().getNombre() : "SIN_ROL");
            } else {
                log.setAutorNombre(correo);
                log.setAutorRol("ADMIN");
            }
        }

        String ip = "0.0.0.0";
        if (request != null) {
            ip = request.getRemoteAddr();
            // Si el cliente está detrás de un proxy (e.g. Nginx, Cloudflare), usar la cabecera correspondiente
            String xfHeader = request.getHeader("X-Forwarded-For");
            if (xfHeader != null && !xfHeader.isEmpty()) {
                ip = xfHeader.split(",")[0];
            }
        }
        log.setIpOrigen(ip);

        repo.save(log);
    }
}
