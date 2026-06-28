package com.proyecto.WebRopa.service.jpa;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.WebRopa.entity.ConfiguracionDespacho;
import com.proyecto.WebRopa.entity.Empresas;
import com.proyecto.WebRopa.repository.ConfiguracionDespachoRepository;
import com.proyecto.WebRopa.service.IConfiguracionDespachoService;

@Service
public class ConfiguracionDespachoService implements IConfiguracionDespachoService {

    private final ConfiguracionDespachoRepository repo;

    public ConfiguracionDespachoService(ConfiguracionDespachoRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public ConfiguracionDespacho obtenerOCrearDefault(Long empresaId) {
        return repo.findByEmpresaId(empresaId).orElseGet(() -> {
            ConfiguracionDespacho config = new ConfiguracionDespacho();
            Empresas empresa = new Empresas();
            empresa.setId(empresaId);
            config.setEmpresa(empresa);
            config.setDiasHabilesParaRecojo(5);
            config.setDiasHabilesParaCancelarNoRecogido(7);
            return repo.save(config);
        });
    }

    @Transactional
    public ConfiguracionDespacho actualizar(Long empresaId, int diasRecojo, int diasCancelar) {
        ConfiguracionDespacho config = obtenerOCrearDefault(empresaId);
        config.setDiasHabilesParaRecojo(diasRecojo);
        config.setDiasHabilesParaCancelarNoRecogido(diasCancelar);
        return repo.save(config);
    }
}
