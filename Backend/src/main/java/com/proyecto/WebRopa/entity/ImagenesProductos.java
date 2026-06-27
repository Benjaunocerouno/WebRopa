package com.proyecto.WebRopa.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "imagenes_productos")
public class ImagenesProductos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String url_imagen;

    private int orden;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id", nullable = false)
    private Productos producto;

    // ── Getters y Setters ────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUrl_imagen() { return url_imagen; }
    public void setUrl_imagen(String url_imagen) { this.url_imagen = url_imagen; }
    public int getOrden() { return orden; }
    public void setOrden(int orden) { this.orden = orden; }
    public Productos getProducto() { return producto; }
    public void setProducto(Productos producto) { this.producto = producto; }
}