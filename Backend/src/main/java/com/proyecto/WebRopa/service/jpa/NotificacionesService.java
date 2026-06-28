package com.proyecto.WebRopa.service.jpa;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.WebRopa.entity.Notificaciones;
import com.proyecto.WebRopa.entity.Pedidos;
import com.proyecto.WebRopa.entity.Usuarios;
import com.proyecto.WebRopa.repository.NotificacionesRepository;
import com.proyecto.WebRopa.service.INotificacionesService;

@Service
public class NotificacionesService implements INotificacionesService {

    private final NotificacionesRepository repo;

    public NotificacionesService(NotificacionesRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public void crear(Usuarios usuario, Pedidos pedido, Notificaciones.Tipo tipo, String mensaje) {
        // Dedup: no crear si ya existe una notificación del mismo tipo para este pedido
        if (pedido != null && repo.existsByPedidoIdAndTipo(pedido.getId(), tipo)) {
            return;
        }
        Notificaciones n = new Notificaciones();
        n.setUsuario(usuario);
        n.setPedido(pedido);
        n.setTipo(tipo);
        n.setMensaje(mensaje);
        repo.save(n);
    }

    public List<Notificaciones> buscarPorUsuarioId(Long usuarioId) {
        return repo.findByUsuarioIdOrderByFechaDesc(usuarioId);
    }

    @Transactional
    public void marcarComoLeida(Long id) {
        repo.findById(id).ifPresent(n -> {
            n.setLeida(true);
            repo.save(n);
        });
    }
}
