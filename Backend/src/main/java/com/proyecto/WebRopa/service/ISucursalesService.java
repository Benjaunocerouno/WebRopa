package com.proyecto.WebRopa.service;
import java.util.List;
import java.util.Optional;
import com.proyecto.WebRopa.entity.Sucursales;

public interface ISucursalesService {
    List<Sucursales> buscarTodos();
    List<Sucursales> buscarPorEmpresa(Long empresaId);
    void guardar(Sucursales sucursal);
    void modificar(Sucursales sucursal);
    Optional<Sucursales> buscarId(Long id);
    void eliminar(Long id);
}
