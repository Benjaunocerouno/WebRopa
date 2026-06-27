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
@Table(name = "recojo_tienda")
@SQLDelete(sql = "UPDATE recojo_tienda SET estado = 'EXPIRADO' WHERE id = ?")
@SQLRestriction("estado != 'EXPIRADO'")
public class RecojoTienda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String codigo_recojo;

    public enum Estado {
        PENDIENTE, LISTO_PARA_RECOGER, RECOGIDO, EXPIRADO
    }

    @Enumerated(EnumType.STRING)
    private Estado estado = Estado.PENDIENTE;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime fecha_disponible;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime fecha_recogido;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedidos pedido;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "atendido_por", nullable = true)
    private Usuarios atendido_por;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "empresa_id")
    private Empresas empresa;

    // ── Getters y Setters ────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo_recojo() { return codigo_recojo; }
    public void setCodigo_recojo(String codigo_recojo) { this.codigo_recojo = codigo_recojo; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public LocalDateTime getFecha_disponible() { return fecha_disponible; }
    public void setFecha_disponible(LocalDateTime fecha_disponible) { this.fecha_disponible = fecha_disponible; }

    public LocalDateTime getFecha_recogido() { return fecha_recogido; }
    public void setFecha_recogido(LocalDateTime fecha_recogido) { this.fecha_recogido = fecha_recogido; }

    public Pedidos getPedido() { return pedido; }
    public void setPedido(Pedidos pedido) { this.pedido = pedido; }

    public Usuarios getAtendido_por() { return atendido_por; }
    public void setAtendido_por(Usuarios atendido_por) { this.atendido_por = atendido_por; }

    public Empresas getEmpresa() { return empresa; }
    public void setEmpresa(Empresas empresa) { this.empresa = empresa; }
}