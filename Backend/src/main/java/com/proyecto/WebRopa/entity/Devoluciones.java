package com.proyecto.WebRopa.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.*;

@Entity
@Table(name = "devoluciones")
@SQLDelete(sql = "UPDATE devoluciones SET estado = 'CANCELADA' WHERE id = ?")
@SQLRestriction("estado != 'CANCELADA'")
public class Devoluciones {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime fecha = LocalDateTime.now();

    public enum Motivo {
        DEFECTUOSO, TALLA_INCORRECTA, COLOR_INCORRECTO, OTRO
    }

    public enum Estado {
        SOLICITADA, APROBADA, RECHAZADA, REEMBOLSADA, CANCELADA
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Motivo motivo;

    private String descripcion;

    @Column(name = "cantidad_devuelta", nullable = false)
    private Integer cantidaddevuelta;

    @Column(name = "monto_reembolso", nullable = false)
    private Double montoreembolso;

    @Enumerated(EnumType.STRING)
    private Estado estado = Estado.SOLICITADA;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedidos pedido;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pedido_item_id", nullable = false)
    private PedidosItems pedidoItem;

    // ── Getters y Setters ────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public Motivo getMotivo() { return motivo; }
    public void setMotivo(Motivo motivo) { this.motivo = motivo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getCantidad_devuelta() { return cantidaddevuelta; }
    public void setCantidad_devuelta(Integer cantidad_devuelta) { this.cantidaddevuelta = cantidad_devuelta; }

    public Double getMonto_reembolso() { return montoreembolso; }
    public void setMonto_reembolso(Double monto_reembolso) { this.montoreembolso = monto_reembolso; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public Pedidos getPedido() { return pedido; }
    public void setPedido(Pedidos pedido) { this.pedido = pedido; }

    public PedidosItems getPedidoItem() { return pedidoItem; }
    public void setPedidoItem(PedidosItems pedidoItem) { this.pedidoItem = pedidoItem; }
}