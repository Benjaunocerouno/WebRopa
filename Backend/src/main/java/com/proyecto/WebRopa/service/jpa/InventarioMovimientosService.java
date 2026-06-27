package com.proyecto.WebRopa.service.jpa;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.proyecto.WebRopa.entity.InventarioMovimientos;
import com.proyecto.WebRopa.entity.Variantes;
import com.proyecto.WebRopa.repository.InventarioMovimientosRepository;
import com.proyecto.WebRopa.service.IInventarioMovimientosService;
import com.proyecto.WebRopa.service.IVariantesService;

@Service
public class InventarioMovimientosService implements IInventarioMovimientosService {

    private final InventarioMovimientosRepository repoMovimientos;
    private final IVariantesService serviceVariantes;

    public InventarioMovimientosService(InventarioMovimientosRepository repoMovimientos, IVariantesService serviceVariantes) {
        this.repoMovimientos = repoMovimientos;
        this.serviceVariantes = serviceVariantes;
    }

    public List<InventarioMovimientos> buscarTodos() { return repoMovimientos.findAll(); }
    public Optional<InventarioMovimientos> buscarId(Long id) { return repoMovimientos.findById(id); }
    
    @Transactional
    public void guardar(InventarioMovimientos movimiento) { 
        Optional<Variantes> varOpt = serviceVariantes.buscarId(movimiento.getVariante().getId());
        if (!varOpt.isPresent()) {
            throw new RuntimeException("La variante especificada no existe");
        }
        Variantes variante = varOpt.get();

        // Actualizar stock de la variante (soporta positivos para entradas y negativos para salidas)
        int nuevoStock = variante.getStock() + movimiento.getCantidad();
        if (nuevoStock < 0) {
            throw new RuntimeException("Stock insuficiente para realizar este movimiento de salida");
        }
        variante.setStock(nuevoStock);
        serviceVariantes.guardar(variante);

        movimiento.setEstado(InventarioMovimientos.Estado.ACTIVO);
        repoMovimientos.save(movimiento); 
    }
    
    @Transactional
    public void eliminar(Long id) { 
        Optional<InventarioMovimientos> movOpt = repoMovimientos.findById(id);
        if (movOpt.isPresent()) {
            InventarioMovimientos movimiento = movOpt.get();
            Optional<Variantes> varOpt = serviceVariantes.buscarId(movimiento.getVariante().getId());
            if (varOpt.isPresent()) {
                Variantes variante = varOpt.get();
                // Revertir el stock en la variante
                variante.setStock(variante.getStock() - movimiento.getCantidad());
                serviceVariantes.guardar(variante);
            }
        }
        repoMovimientos.deleteById(id); 
    }

    public void modificar(InventarioMovimientos movimiento) {
        Optional<InventarioMovimientos> existente = repoMovimientos.findById(movimiento.getId());
        if (existente.isPresent()) {
            // Regla de negocio estricta: Solo se permite modificar la observación de un registro contable.
            existente.get().setObservacion(movimiento.getObservacion());
            repoMovimientos.save(existente.get());
        } else {
            throw new RuntimeException("El movimiento no existe");
        }
    }
}