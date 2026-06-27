package com.proyecto.WebRopa.service.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.proyecto.WebRopa.entity.Carritos;
import com.proyecto.WebRopa.repository.CarritosRepository;
import com.proyecto.WebRopa.service.ICarritosService;

import jakarta.transaction.Transactional;

@Service
public class CarritosService implements ICarritosService {

    
    private final CarritosRepository repoCarritos;
    public CarritosService(CarritosRepository repoCarritos) {
        this.repoCarritos = repoCarritos;
    }

    public List<Carritos> buscarTodos() { 
        return repoCarritos.findAll(); 
    }

    public void guardar(Carritos carrito) {
        repoCarritos.save(carrito);
    }

    public Optional<Carritos> buscarPorUsuarioId(Long usuarioId) {
        return repoCarritos.findByUsuarioId(usuarioId);
    }

    public Optional<Carritos> buscarId(Long id) {
        return repoCarritos.findById(id);
    }

    @Override
    @Transactional
    public void reactivarCarrito(Long id) {
        int filasModificadas = repoCarritos.reactivarCarrito(id);
        if (filasModificadas == 0) {
            throw new RuntimeException("No se pudo reactivar el carrito con id: " + id);
        }
    }

    public void eliminar(Long id) {
        repoCarritos.deleteById(id);
    }
}