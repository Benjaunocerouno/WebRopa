package com.proyecto.WebRopa.service.jpa;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.proyecto.WebRopa.entity.Permisos;
import com.proyecto.WebRopa.repository.PermisosRepository;
import com.proyecto.WebRopa.service.IPermisosService;

@Service
public class PermisosService implements IPermisosService {
    private final PermisosRepository repo;
    public PermisosService(PermisosRepository repo) { this.repo = repo; }
    public List<Permisos> buscarTodos() { return repo.findAll(); }
    public void guardar(Permisos entity) { repo.save(entity); }
    public void modificar(Permisos entity) { repo.save(entity); }
    public Optional<Permisos> buscarId(Long id) { return repo.findById(id); }
    public void eliminar(Long id) { repo.deleteById(id); }
}
