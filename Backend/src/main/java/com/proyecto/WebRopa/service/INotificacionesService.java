package com.proyecto.WebRopa.service;

import java.util.List;

import com.proyecto.WebRopa.entity.Notificaciones;
import com.proyecto.WebRopa.entity.Pedidos;
import com.proyecto.WebRopa.entity.Usuarios;

public interface INotificacionesService {
    void crear(Usuarios usuario, Pedidos pedido, Notificaciones.Tipo tipo, String mensaje);
    List<Notificaciones> buscarPorUsuarioId(Long usuarioId);
    void marcarComoLeida(Long id);
}
