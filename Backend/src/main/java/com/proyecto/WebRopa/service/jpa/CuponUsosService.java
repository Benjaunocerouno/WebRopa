package com.proyecto.WebRopa.service.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import com.proyecto.WebRopa.entity.CuponUsos;
import com.proyecto.WebRopa.entity.Cupones;
import com.proyecto.WebRopa.repository.CuponUsosRepository;
import com.proyecto.WebRopa.repository.CuponesRepository;
import com.proyecto.WebRopa.service.ICuponUsosService;

@Service
public class CuponUsosService implements ICuponUsosService {
    
    private final CuponUsosRepository repoCuponUsos;
    private final CuponesRepository repoCupones;

    public CuponUsosService(CuponUsosRepository repoCuponUsos, CuponesRepository repoCupones) {
        this.repoCuponUsos = repoCuponUsos;
        this.repoCupones = repoCupones;
    }

    public List<CuponUsos> buscarTodos() { return repoCuponUsos.findAll(); }
    public Optional<CuponUsos> buscarPorCuponYUsuario(Long cuponId, Long usuarioId) { return repoCuponUsos.findByCuponIdAndUsuarioId(cuponId, usuarioId); }
    public Optional<CuponUsos> buscarId(Long id) { return repoCuponUsos.findById(id); }
    public void registrarUso(CuponUsos cuponUsos) { repoCuponUsos.save(cuponUsos); }
    public void actualizarNota(Long id, String notaAdmin) {
        Optional<CuponUsos> usoOpt = repoCuponUsos.findById(id);
        if (usoOpt.isPresent()) {
            CuponUsos uso = usoOpt.get();
            uso.setObservacion(notaAdmin);
            repoCuponUsos.save(uso);
        }
    }
    
    @Transactional
    public void eliminar(Long id) { 
        Optional<CuponUsos> usoOpt = repoCuponUsos.findById(id);
        if (usoOpt.isPresent()) {
            Cupones cupon = usoOpt.get().getCupon();
            if (cupon != null && cupon.getUsos_actuales() > 0) {
                cupon.setUsos_actuales(cupon.getUsos_actuales() - 1);
                repoCupones.save(cupon);
            }
            repoCuponUsos.deleteById(id);
        }
    }
}
