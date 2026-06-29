package com.proyecto.WebRopa.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.*;

@Entity
@Table(name = "pedidos")
@SQLDelete(sql = "UPDATE pedidos SET estado = 'CANCELADO' WHERE id = ?")
@SQLRestriction("estado != 'CANCELADO'")
public class Pedidos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime fecha_creacion = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado = Estado.PENDIENTE;

    @Column(nullable = false)
    private double subtotal;

    private double descuento = 0.0;

    @Column(nullable = false)
    private double total;

    private boolean pago_confirmado = false;

    private String notas;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuarios usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cupon_id")
    private Cupones cupon;

    // ENUM
    public enum Estado {
        PENDIENTE,
        CONFIRMADO,
        EN_PREPARACION,
        LISTO_PARA_RECOGER,
        RECOGIDO,
        EN_CAMINO,
        ENTREGADO,
        NO_RECOGIDO,
        CANCELADO
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "empresa_id")
    private Empresas empresa;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime fecha_no_recogido;

    // GETTERS Y SETTERS

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFecha_creacion() {
        return fecha_creacion;
    }

    public void setFecha_creacion(LocalDateTime fecha_creacion) {
        this.fecha_creacion = fecha_creacion;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getDescuento() {
        return descuento;
    }

    public void setDescuento(double descuento) {
        this.descuento = descuento;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public boolean isPago_confirmado() {
        return pago_confirmado;
    }

    public void setPago_confirmado(boolean pago_confirmado) {
        this.pago_confirmado = pago_confirmado;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public Usuarios getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuarios usuario) {
        this.usuario = usuario;
    }

    public Cupones getCupon() {
        return cupon;
    }

    public void setCupon(Cupones cupon) {
        this.cupon = cupon;
    }

    public Empresas getEmpresa() { return empresa; }
    public void setEmpresa(Empresas empresa) { this.empresa = empresa; }

    public LocalDateTime getFecha_no_recogido() {
        return fecha_no_recogido;
    }

    public void setFecha_no_recogido(LocalDateTime fecha_no_recogido) {
        this.fecha_no_recogido = fecha_no_recogido;
    }
}