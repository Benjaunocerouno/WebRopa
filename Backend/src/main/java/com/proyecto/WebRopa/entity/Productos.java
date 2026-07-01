package com.proyecto.WebRopa.entity;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "productos")
@SQLDelete(sql = "UPDATE productos SET estado = 'INACTIVO' WHERE id = ?")
public class Productos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String descripcion;
    private Double precio;
    private String imagen_url;

    @Enumerated(EnumType.STRING)
    private Estado estado = Estado.ACTIVO;

    // ENUMS

    public enum Estado {
        ACTIVO, INACTIVO
    }

    // ── Relación con Categorias ──────────────────────
    // Muchos productos pertenecen a una categoría
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id")
    private Categorias categoria;

    // ── Relación con Variantes ───────────────────────
    @OneToMany(mappedBy = "producto", cascade = CascadeType.REMOVE)
    private List<Variantes> variantes;

    // ── Relación con Galería de Imágenes ──────────────────
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // Para evitar bucles infinitos en el JSON
    private List<ImagenesProductos> galeriaImagenes;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "empresa_id")
    private Empresas empresa;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "proveedor_id")
    private Proveedores proveedor;

    // ── Getters y Setters ────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public String getImagen_url() {
        return imagen_url;
    }

    public void setImagen_url(String imagen_url) {
        this.imagen_url = imagen_url;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public Categorias getCategoria() {
        return categoria;
    }

    public void setCategoria(Categorias categoria) {
        this.categoria = categoria;
    }

    public List<Variantes> getVariantes() {
        return variantes;
    }

    public void setVariantes(List<Variantes> variantes) {
        this.variantes = variantes;
    }

    public List<ImagenesProductos> getGaleriaImagenes() {
        return galeriaImagenes;
    }

    public void setGaleriaImagenes(List<ImagenesProductos> galeriaImagenes) {
        this.galeriaImagenes = galeriaImagenes;
    }

    public Empresas getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresas empresa) {
        this.empresa = empresa;
    }

    public Proveedores getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedores proveedor) {
        this.proveedor = proveedor;
    }
}