package com.proyecto.WebRopa.service.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.proyecto.WebRopa.entity.Carritositems;
import com.proyecto.WebRopa.repository.CarritositemsRepository;
import com.proyecto.WebRopa.service.ICarritos_itemsService;

import jakarta.transaction.Transactional;

@Service
public class CarritositemsService implements ICarritos_itemsService{
    
    private final CarritositemsRepository repoCarritositems;
    public CarritositemsService(CarritositemsRepository repoCarritositems) {
        this.repoCarritositems = repoCarritositems;
    }

    public List<Carritositems> buscarTodos() { return repoCarritositems.findAll(); }
    public List<Carritositems> buscarPorCarritoId(Long carritoId) {
    return repoCarritositems.findByCarritoId(carritoId);}
   @Transactional
    public void guardar(Carritositems carritositem) { repoCarritositems.save(carritositem); }
    public Optional<Carritositems> buscarPorCarritoYVariante(Long carritoId, Long varianteId) {
    return repoCarritositems.findByCarritoIdAndVarianteId(carritoId, varianteId);}
    public void modificar(Carritositems carritositem) { repoCarritositems.save(carritositem); }
    public Optional<Carritositems> buscarId(Long id) { return repoCarritositems.findById(id); }
    public void eliminar(Long id) { repoCarritositems.deleteById(id); }
    
}
