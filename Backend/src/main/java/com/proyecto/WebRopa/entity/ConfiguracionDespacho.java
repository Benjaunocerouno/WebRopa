package com.proyecto.WebRopa.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "configuracion_despacho")
public class ConfiguracionDespacho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false, unique = true)
    private Empresas empresa;

    @Column(name = "dias_habiles_para_recojo", nullable = false)
    private int diasHabilesParaRecojo = 5;

    @Column(name = "dias_habiles_para_cancelar_no_recogido", nullable = false)
    private int diasHabilesParaCancelarNoRecogido = 7;

    // ── Getters y Setters ────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Empresas getEmpresa() { return empresa; }
    public void setEmpresa(Empresas empresa) { this.empresa = empresa; }

    public int getDiasHabilesParaRecojo() { return diasHabilesParaRecojo; }
    public void setDiasHabilesParaRecojo(int dias) { this.diasHabilesParaRecojo = dias; }

    public int getDiasHabilesParaCancelarNoRecogido() { return diasHabilesParaCancelarNoRecogido; }
    public void setDiasHabilesParaCancelarNoRecogido(int dias) { this.diasHabilesParaCancelarNoRecogido = dias; }
}
