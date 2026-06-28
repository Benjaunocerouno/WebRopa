package com.proyecto.WebRopa.service;

import com.proyecto.WebRopa.entity.Pedidos;

public interface IPedidoEstadoService {
    void aplicarEfectosConfirmado(Pedidos pedido);
    void aplicarEfectosListoParaRecoger(Pedidos pedido);
    void aplicarEfectosRecogido(Pedidos pedido);
    void aplicarEfectosNoRecogido(Pedidos pedido);
    void aplicarEfectosCancelado(Pedidos pedido, String descripcion);
}
