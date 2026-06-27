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
@Table(name = "boletas")
@SQLDelete(sql = "UPDATE boletas SET estado = 'ANULADA' WHERE id = ?")
@SQLRestriction("estado != 'ANULADA'")
public class Boletas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String numero_boleta;

    @Column(nullable = false, updatable = false)
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime fecha_emision = LocalDateTime.now();

    @Column(nullable = false)
    private String nombre_cliente;

    private String dni_cliente;

    @Column(nullable = false)
    private double subtotal;

    @Column(nullable = false)
    private double igv;

    @Column(nullable = false)
    private double total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado = Estado.ACTIVA;

    public enum Estado {
        ACTIVA, ANULADA
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedidos pedido;

    // ── Getters y Setters ────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumero_boleta() { return numero_boleta; }
    public void setNumero_boleta(String numero_boleta) { this.numero_boleta = numero_boleta; }

    public LocalDateTime getFecha_emision() { return fecha_emision; }
    public void setFecha_emision(LocalDateTime fecha_emision) { this.fecha_emision = fecha_emision; }

    public String getNombre_cliente() { return nombre_cliente; }
    public void setNombre_cliente(String nombre_cliente) { this.nombre_cliente = nombre_cliente; }

    public String getDni_cliente() { return dni_cliente; }
    public void setDni_cliente(String dni_cliente) { this.dni_cliente = dni_cliente; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getIgv() { return igv; }
    public void setIgv(double igv) { this.igv = igv; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public Pedidos getPedido() { return pedido; }
    public void setPedido(Pedidos pedido) { this.pedido = pedido; }
}