package com.proyecto.WebRopa.service.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SeguridadEnVivoInterceptor implements HandlerInterceptor {

    @Autowired
    private SeguridadEnVivoService seguridadEnVivoService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            
            // Si el token está marcado como revocado o cerrado en el servicio en memoria, bloqueamos
            boolean esValido = true;
            for (SeguridadEnVivoService.SesionInfo s : seguridadEnVivoService.obtenerSesionesActivas()) {
                // obtenerSesionesActivas ya filtra las revocadas
            }
            
            // Mejor: crear un método directo en SeguridadEnVivoService para verificar el token
            if (!seguridadEnVivoService.esTokenValido(token)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Acceso denegado: Sesion terminada por el administrador.");
                return false;
            }
        }
        return true;
    }
}
