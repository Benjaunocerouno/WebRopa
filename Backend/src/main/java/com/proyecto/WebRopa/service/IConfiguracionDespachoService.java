package com.proyecto.WebRopa.service;

import com.proyecto.WebRopa.entity.ConfiguracionDespacho;

public interface IConfiguracionDespachoService {
    ConfiguracionDespacho obtenerOCrearDefault(Long empresaId);
    ConfiguracionDespacho actualizar(Long empresaId, int diasRecojo, int diasCancelar);
}
