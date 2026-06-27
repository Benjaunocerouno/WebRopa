param($entityName, $repoName, $path)
$content = Get-Content -Raw $path

$replacement = "
    public List<$entityName> buscarTodos() { 
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            return $repoName.findByEmpresaId(tenantId);
        }
        return $repoName.findAll(); 
    }
    public void guardar($entityName entity) { 
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null && entity.getEmpresa() == null) {
            com.proyecto.WebRopa.entity.Empresas emp = new com.proyecto.WebRopa.entity.Empresas();
            emp.setId(tenantId);
            entity.setEmpresa(emp);
        }
        $repoName.save(entity); 
    }
    public void modificar($entityName entity) { 
        guardar(entity); 
    }
    public Optional<$entityName> buscarId(Long id) { 
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            return $repoName.findByIdAndEmpresaId(id, tenantId);
        }
        return $repoName.findById(id); 
    }
    public void eliminar(Long id) { 
        Long tenantId = com.proyecto.WebRopa.security.TenantContext.getCurrentTenant();
        if (tenantId != null) {
            Optional<$entityName> ent = $repoName.findByIdAndEmpresaId(id, tenantId);
            if (ent.isPresent()) {
                $repoName.deleteById(id);
            }
        } else {
            $repoName.deleteById(id); 
        }
    }
}
"

$content = $content -replace '(?s)public List<[^>]+> buscarTodos\(\).*', $replacement
Set-Content -Path $path -Value $content
