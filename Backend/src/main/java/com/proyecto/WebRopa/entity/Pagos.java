package com.proyecto.WebRopa.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.SQLDelete;

import com.fasterxml.jackson.annotation.JsonFormat;

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
@Table(name = "pagos")
@SQLDelete(sql = "UPDATE pagos SET estado = 'REEMBOLSADO' WHERE id = ?")
public class Pagos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(nullable = false)
    private Double monto;

    // ── Enums correctamente declarados ──────────────
    public enum Metodo {
        TARJETA, YAPE, PLIN, TRANSFERENCIA
    }

    public enum Estado {
        PENDIENTE, APROBADO, RECHAZADO, REEMBOLSADO
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Metodo metodo;

    @Enumerated(EnumType.STRING)
    private Estado estado = Estado.PENDIENTE;
    // ─────────────────────────────────────────────────

    private String referencia_externa;
    private String proveedor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedidos pedido;

    // ── Getters y Setters ────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }

    public Metodo getMetodo() { return metodo; }
    public void setMetodo(Metodo metodo) { this.metodo = metodo; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public String getReferencia_externa() { return referencia_externa; }
    public void setReferencia_externa(String referencia_externa) { this.referencia_externa = referencia_externa; }

    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }

    public Pedidos getPedido() { return pedido; }
    public void setPedido(Pedidos pedido) { this.pedido = pedido; }
}