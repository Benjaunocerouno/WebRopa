package com.proyecto.WebRopa.service.jpa;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.proyecto.WebRopa.entity.Empresas;
import com.proyecto.WebRopa.repository.EmpresasRepository;
import com.proyecto.WebRopa.service.IEmpresasService;

@Service
public class EmpresasService implements IEmpresasService {
    private final EmpresasRepository repo;
    public EmpresasService(EmpresasRepository repo) { this.repo = repo; }
    public List<Empresas> buscarTodos() { return repo.findAll(); }
    public void guardar(Empresas entity) { repo.save(entity); }
    public void modificar(Empresas entity) { repo.save(entity); }
    public Optional<Empresas> buscarId(Long id) { return repo.findById(id); }
    public void eliminar(Long id) { repo.deleteById(id); }
}
