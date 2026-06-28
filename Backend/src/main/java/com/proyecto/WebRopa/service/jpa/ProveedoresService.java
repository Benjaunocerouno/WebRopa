package com.proyecto.WebRopa.service.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.proyecto.WebRopa.entity.Proveedores;
import com.proyecto.WebRopa.repository.ProveedoresRepository;
import com.proyecto.WebRopa.service.IProveedoresService;
import com.proyecto.WebRopa.repository.EmpresasRepository;

@Service
public class ProveedoresService implements IProveedoresService {

    private final ProveedoresRepository repoProveedores;
    private final EmpresasRepository repoEmpresas;

    public ProveedoresService(ProveedoresRepository repoProveedores, EmpresasRepository repoEmpresas) {
        this.repoProveedores = repoProveedores;
        this.repoEmpresas = repoEmpresas;
    }

    private void validarProveedor(Proveedores proveedor) {
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        Long empresaId = null;

        if (tenantId != null) {
            empresaId = tenantId;
            if (proveedor.getEmpresa() == null) {
                com.proyecto.WebRopa.entity.Empresas emp = new com.proyecto.WebRopa.entity.Empresas();
                emp.setId(tenantId);
                proveedor.setEmpresa(emp);
            }
        } else {
            if (proveedor.getEmpresa() == null || proveedor.getEmpresa().getId() == null) {
                throw new IllegalArgumentException("Aún falta seleccionar o ingresar esa empresa");
            }
            empresaId = proveedor.getEmpresa().getId();
        }

        if (!repoEmpresas.existsById(empresaId)) {
            throw new IllegalArgumentException("La empresa no existe");
        }

        if (proveedor.getRuc() == null || !proveedor.getRuc().matches("\\d{11}")) {
            throw new IllegalArgumentException("El RUC debe ser de 11 dígitos");
        }
        if (proveedor.getRazonSocial() == null || proveedor.getRazonSocial().trim().isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar la razón social");
        }
        if (proveedor.getNombreComercial() == null || proveedor.getNombreComercial().trim().isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar el nombre comercial");
        }
        if (proveedor.getContactoNombre() == null || proveedor.getContactoNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar el nombre del contacto");
        }
        if (proveedor.getTelefono() == null || !proveedor.getTelefono().matches("\\d{9}")) {
            throw new IllegalArgumentException("El teléfono debe tener 9 dígitos");
        }
        if (proveedor.getCorreo() == null || !proveedor.getCorreo().toLowerCase().endsWith(".com")) {
            throw new IllegalArgumentException("El correo debe terminar en .com");
        }
        if (proveedor.getDireccion() == null || proveedor.getDireccion().trim().isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar la dirección");
        }

        Optional<Proveedores> existenteRuc = repoProveedores.findByRucAndEmpresaId(proveedor.getRuc(), empresaId);
        if (existenteRuc.isPresent() && !existenteRuc.get().getId().equals(proveedor.getId())) {
            throw new IllegalArgumentException("Proveedor ya registrado");
        }

        Optional<Proveedores> existenteRazonSocial = repoProveedores.findByRazonSocialAndEmpresaId(proveedor.getRazonSocial(), empresaId);
        if (existenteRazonSocial.isPresent() && !existenteRazonSocial.get().getId().equals(proveedor.getId())) {
            throw new IllegalArgumentException("Ya existe un proveedor registrado con esa razón social");
        }

        Optional<Proveedores> existenteCorreo = repoProveedores.findByCorreoAndEmpresaId(proveedor.getCorreo(), empresaId);
        if (existenteCorreo.isPresent() && !existenteCorreo.get().getId().equals(proveedor.getId())) {
            throw new IllegalArgumentException("Ya existe un proveedor registrado con ese correo");
        }
    }

    @Override
    public List<Proveedores> buscarTodos() {
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            return repoProveedores.findByEmpresaId(tenantId);
        }
        return repoProveedores.findAll();
    }

    @Override
    public void guardar(Proveedores entity) {
        validarProveedor(entity);
        repoProveedores.save(entity);
    }

    @Override
    public void modificar(Proveedores entity) {
        validarProveedor(entity);
        repoProveedores.save(entity);
    }

    @Override
    public Optional<Proveedores> buscarId(Long id) {
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            return repoProveedores.findByIdAndEmpresaId(id, tenantId);
        }
        return repoProveedores.findById(id);
    }

    @Override
    public void eliminar(Long id) {
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            Optional<Proveedores> ent = repoProveedores.findByIdAndEmpresaId(id, tenantId);
            if (ent.isPresent()) {
                repoProveedores.deleteById(id);
            }
        } else {
            repoProveedores.deleteById(id);
        }
    }
}