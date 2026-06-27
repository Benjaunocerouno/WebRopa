package com.proyecto.WebRopa.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import com.proyecto.WebRopa.entity.Carritositems;


public interface CarritositemsRepository extends JpaRepository<Carritositems, Long> {

    @Override
    @EntityGraph(attributePaths = {"carrito", "carrito.usuario", "variante", "variante.producto"})
    Optional<Carritositems> findById(Long id);

    // Agregar en el Repository:
    Optional<Carritositems> findByCarritoIdAndVarianteId(Long carritoId, Long varianteId);
    List<Carritositems> findByCarritoId(Long carritoId);
}