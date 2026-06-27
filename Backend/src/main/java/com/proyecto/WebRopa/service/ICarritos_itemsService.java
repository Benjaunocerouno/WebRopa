package com.proyecto.WebRopa.service;

import java.util.List;
import java.util.Optional;
import com.proyecto.WebRopa.entity.Carritositems;


public interface ICarritos_itemsService {
    List<Carritositems> buscarTodos();
    List<Carritositems> buscarPorCarritoId(Long carritoId);
    void guardar(Carritositems carrito_item);
    Optional<Carritositems> buscarPorCarritoYVariante(Long carritoId, Long varianteId);
    void modificar(Carritositems carrito_item);
    Optional<Carritositems> buscarId(Long id);
    void eliminar(Long id);
}
