package com.proyecto.WebRopa.entity;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import jakarta.persistence.*;

@Entity
@Table(name = "inventario_movimientos")
@SQLDelete(sql = "UPDATE inventario_movimientos SET estado = 'ANULADO' WHERE id = ?")
@SQLRestriction("estado != 'ANULADO'")
public class InventarioMovimientos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimiento tipo_movimiento;

    @Column(nullable = false)
    private int cantidad;

    private String observacion;

    @Column(nullable = false, updatable = false)
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime fecha = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private Estado estado = Estado.ACTIVO;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "variante_id", nullable = false)
    private Variantes variante;

    public enum TipoMovimiento { INGRESO_COMPRA, SALIDA_VENTA, INGRESO_DEVOLUCION, AJUSTE_MANUAL }
    public enum Estado { ACTIVO, ANULADO }

    // ── Getters y Setters ────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TipoMovimiento getTipo_movimiento() { return tipo_movimiento; }
    public void setTipo_movimiento(TipoMovimiento tipo_movimiento) { this.tipo_movimiento = tipo_movimiento; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }
    public Variantes getVariante() { return variante; }
    public void setVariante(Variantes variante) { this.variante = variante; }
}