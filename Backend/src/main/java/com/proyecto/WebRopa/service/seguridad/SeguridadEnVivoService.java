package com.proyecto.WebRopa.service.seguridad;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class SeguridadEnVivoService {

    public static class SesionInfo {
        public String id;
        public String token;
        public Long usuarioId;
        public String rol;
        public String ip;
        public LocalDateTime ultimaActividad;
        public boolean revocada = false;
        public boolean sospechosa = false;
        public String correo;

        public SesionInfo(String id, String token, Long usuarioId, String rol, String ip, String correo) {
            this.id = id;
            this.token = token;
            this.usuarioId = usuarioId;
            this.rol = rol;
            this.ip = ip;
            this.ultimaActividad = LocalDateTime.now();
            this.correo = correo;
        }
    }

    // Almacén en memoria
    private final Map<String, SesionInfo> sesionesPorToken = new ConcurrentHashMap<>();
    private int sessionCounter = 1000;

    public void registrarLoginExitoso(String token, Long usuarioId, String rol, String ip, String correo) {
        String idSesion = String.valueOf(++sessionCounter);
        SesionInfo sesion = new SesionInfo(idSesion, token, usuarioId, rol, ip, correo);
        sesionesPorToken.put(token, sesion);
    }

    public List<SesionInfo> obtenerSesionesActivas() {
        List<SesionInfo> activas = new ArrayList<>();
        for (SesionInfo s : sesionesPorToken.values()) {
            if (!s.revocada) {
                activas.add(s);
            }
        }
        return activas;
    }

    public void cerrarSesionPorId(String sesionId) {
        for (SesionInfo s : sesionesPorToken.values()) {
            if (s.id.equals(sesionId)) {
                s.revocada = true;
                break;
            }
        }
    }

    public void revocarSesionPorId(String sesionId) {
        for (SesionInfo s : sesionesPorToken.values()) {
            if (s.id.equals(sesionId)) {
                s.revocada = true;
                s.sospechosa = true;
                break;
            }
        }
    }

    public boolean esTokenValido(String token) {
        SesionInfo sesion = sesionesPorToken.get(token);
        // Si no está registrado en el map, lo dejamos pasar por compatibilidad si el servidor se reinició.
        // Pero si SÍ está y fue revocado, entonces bloqueamos.
        if (sesion != null && sesion.revocada) {
            return false;
        }
        return true;
    }

    public void cerrarSesionPorToken(String token) {
        SesionInfo sesion = sesionesPorToken.get(token);
        if (sesion != null) {
            sesion.revocada = true;
        }
    }
}
