package com.proyecto.WebRopa.entity;

import java.sql.Date;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "cupones")
@SQLDelete(sql = "UPDATE cupones SET estado = 'INACTIVO' WHERE id = ?")
@SQLRestriction("estado != 'INACTIVO'")
public class Cupones {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    public enum Tipo {
        PORCENTAJE, MONTO_FIJO
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tipo tipo;
    // ─────────────────────────────────────────────────

    private Double valor;
    private Double minimo_compra;
    private Integer usos_maximos;
    private Integer usos_actuales = 0;
    
    @JsonFormat(pattern = "dd/MM/yyyy")
    private Date fecha_inicio;
    
    @JsonFormat(pattern = "dd/MM/yyyy")
    private Date fecha_fin;

    // nullable = true porque el cupón puede ser general (sin categoría ni producto)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id", nullable = true)
    private Categorias categoria;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id", nullable = true)
    private Productos producto;

    @Enumerated(EnumType.STRING)
    private Estado estado = Estado.ACTIVO;
    
    // ENUMS

    public enum Estado {
        ACTIVO, INACTIVO
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "empresa_id")
    private Empresas empresa;

    // ── Getters y Setters ────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo.toUpperCase(); }

    public Tipo getTipo() { return tipo; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }

    public Double getValor() { return valor; }
    public void setValor(Double valor) { this.valor = valor; }

    public Double getMinimo_compra() { return minimo_compra; }
    public void setMinimo_compra(Double minimo_compra) { this.minimo_compra = minimo_compra; }

    public Integer getUsos_maximos() { return usos_maximos; }
    public void setUsos_maximos(Integer usos_maximos) { this.usos_maximos = usos_maximos; }

    public Integer getUsos_actuales() { return usos_actuales; }
    public void setUsos_actuales(Integer usos_actuales) { this.usos_actuales = usos_actuales; }

    public Date getFecha_inicio() { return fecha_inicio; }
    public void setFecha_inicio(Date fecha_inicio) { this.fecha_inicio = fecha_inicio; }

    public Date getFecha_fin() { return fecha_fin; }
    public void setFecha_fin(Date fecha_fin) { this.fecha_fin = fecha_fin; }

    public Categorias getCategoria() { return categoria; }
    public void setCategoria(Categorias categoria) { this.categoria = categoria; }

    public Productos getProducto() { return producto; }
    public void setProducto(Productos producto) { this.producto = producto; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public Empresas getEmpresa() { return empresa; }
    public void setEmpresa(Empresas empresa) { this.empresa = empresa; }
}