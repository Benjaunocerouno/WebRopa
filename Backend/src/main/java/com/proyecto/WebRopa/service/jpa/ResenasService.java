package com.proyecto.WebRopa.service.jpa;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.proyecto.WebRopa.entity.Resenas;
import com.proyecto.WebRopa.repository.ResenasRepository;
import com.proyecto.WebRopa.service.IResenasService;

@Service
public class ResenasService implements IResenasService {

    private final ResenasRepository repoResenas;

    public ResenasService(ResenasRepository repoResenas) {
        this.repoResenas = repoResenas;
    }

    public List<Resenas> buscarTodos() { 
        return repoResenas.findAll(); 
    }
    
    public Optional<Resenas> buscarId(Long id) { 
        return repoResenas.findById(id); 
    }
    
    public void eliminar(Long id) { 
        repoResenas.deleteById(id); 
    }

    public void guardar(Resenas resena) {
        // 1. Validar la calificación (1 a 5)
        if (resena.getCalificacion() < 1 || resena.getCalificacion() > 5) {
            throw new RuntimeException("La calificación debe estar entre 1 y 5 estrellas.");
        }

        // 2. Validar que el usuario no haya reseñado ya este producto
        boolean yaExiste = repoResenas.existsByUsuarioIdAndProductoId(
            resena.getUsuario().getId(), 
            resena.getProducto().getId()
        );

        if (yaExiste) {
            throw new RuntimeException("El usuario ya dejó una reseña para este producto.");
        }

        // 3. Forzar que nazca sin aprobar (por seguridad)
        resena.setAprobada(false);
        repoResenas.save(resena);
    }

    public void modificar(Resenas resena) {
        Optional<Resenas> existente = repoResenas.findById(resena.getId());
        
        if (existente.isPresent()) {
            if (resena.getCalificacion() < 1 || resena.getCalificacion() > 5) {
                throw new RuntimeException("La calificación debe estar entre 1 y 5 estrellas.");
            }

            // Solo permitimos modificar el comentario y la calificación.
            // No se puede cambiar de usuario ni de producto.
            Resenas bdResena = existente.get();
            bdResena.setCalificacion(resena.getCalificacion());
            bdResena.setComentario(resena.getComentario());
            
            // Si el cliente edita su reseña, la volvemos a ocultar para que el admin la revise de nuevo
            bdResena.setAprobada(false); 

            repoResenas.save(bdResena);
        }
    }

    public void aprobar(Long id) {
        Optional<Resenas> existente = repoResenas.findById(id);
        if (existente.isPresent()) {
            Resenas resena = existente.get();
            resena.setAprobada(true);
            repoResenas.save(resena);
        }
    }
}