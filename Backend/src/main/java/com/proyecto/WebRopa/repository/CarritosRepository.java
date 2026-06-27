package com.proyecto.WebRopa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.proyecto.WebRopa.entity.Carritos;
import java.util.Optional;

public interface CarritosRepository extends JpaRepository<Carritos, Long> {

    // Busca el carrito de un usuario específico
    // NOTA: Gracias a tu @SQLRestriction en la entidad, esto automáticamente 
    // solo devolverá el carrito si su estado es 'ACTIVO'.
    Optional<Carritos> findByUsuarioId(Long usuarioId);

    // Método personalizado para reactivar el carrito mediante SQL nativo
    @Modifying
    @Query(value = "UPDATE carritos SET estado = 'ACTIVO' WHERE id = :id", nativeQuery = true)
    int reactivarCarrito(@Param("id") Long id);
}