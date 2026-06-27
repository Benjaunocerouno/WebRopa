package com.proyecto.WebRopa.service.jpa;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.proyecto.WebRopa.entity.ImagenesProductos;
import com.proyecto.WebRopa.repository.ImagenesProductosRepository;
import com.proyecto.WebRopa.service.IImagenesProductosService;

@Service
public class ImagenesProductosService implements IImagenesProductosService {

    private final ImagenesProductosRepository repoImagenes;

    public ImagenesProductosService(ImagenesProductosRepository repoImagenes) {
        this.repoImagenes = repoImagenes;
    }

    public List<ImagenesProductos> buscarTodos() { return repoImagenes.findAll(); }
    public Optional<ImagenesProductos> buscarId(Long id) { return repoImagenes.findById(id); }
    public void guardar(ImagenesProductos imagen) { repoImagenes.save(imagen); }
    public void eliminar(Long id) { repoImagenes.deleteById(id); }

    public void modificar(ImagenesProductos imagen) {
        Optional<ImagenesProductos> existente = repoImagenes.findById(imagen.getId());
        if (existente.isPresent()) {
            existente.get().setUrl_imagen(imagen.getUrl_imagen());
            existente.get().setOrden(imagen.getOrden());
            repoImagenes.save(existente.get());
        } else {
            throw new RuntimeException("La imagen no existe");
        }
    }
}