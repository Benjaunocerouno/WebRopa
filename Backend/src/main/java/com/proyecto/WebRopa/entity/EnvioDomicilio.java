package com.proyecto.WebRopa.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

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

@Entity
@Table(name = "envio_domicilio")
@SQLDelete(sql = "UPDATE envio_domicilio SET estado = 'DEVUELTO' WHERE id = ?")
@SQLRestriction("estado != 'DEVUELTO'")
public class EnvioDomicilio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String direccion;

    @Column(nullable = false)
    private String distrito;

    private String referencia;

    @Column(nullable = false)
    private String nombreDestinatario;

    @Column(nullable = false)
    private String telefonoContacto;

    @Column(nullable = false)
    private double costoEnvio;

    @Column(nullable = false, unique = true)
    private String codigo_seguimiento;

    public enum Estado {
        PENDIENTE, PREPARADO, EN_CAMINO, ENTREGADO, DEVUELTO
    }

    @Enumerated(EnumType.STRING)
    private Estado estado = Estado.PENDIENTE;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime fecha_envio;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime fecha_entrega;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedidos pedido;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "empresa_id")
    private Empresas empresa;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "despachado_por")
    private Usuarios despachado_por;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getDistrito() { return distrito; }
    public void setDistrito(String distrito) { this.distrito = distrito; }

    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }

    public String getNombreDestinatario() { return nombreDestinatario; }
    public void setNombreDestinatario(String nombreDestinatario) { this.nombreDestinatario = nombreDestinatario; }

    public String getTelefonoContacto() { return telefonoContacto; }
    public void setTelefonoContacto(String telefonoContacto) { this.telefonoContacto = telefonoContacto; }

    public double getCostoEnvio() { return costoEnvio; }
    public void setCostoEnvio(double costoEnvio) { this.costoEnvio = costoEnvio; }

    public String getCodigo_seguimiento() { return codigo_seguimiento; }
    public void setCodigo_seguimiento(String codigo_seguimiento) { this.codigo_seguimiento = codigo_seguimiento; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public LocalDateTime getFecha_envio() { return fecha_envio; }
    public void setFecha_envio(LocalDateTime fecha_envio) { this.fecha_envio = fecha_envio; }

    public LocalDateTime getFecha_entrega() { return fecha_entrega; }
    public void setFecha_entrega(LocalDateTime fecha_entrega) { this.fecha_entrega = fecha_entrega; }

    public Pedidos getPedido() { return pedido; }
    public void setPedido(Pedidos pedido) { this.pedido = pedido; }

    public Empresas getEmpresa() { return empresa; }
    public void setEmpresa(Empresas empresa) { this.empresa = empresa; }

    public Usuarios getDespachado_por() { return despachado_por; }
    public void setDespachado_por(Usuarios despachado_por) { this.despachado_por = despachado_por; }
}
