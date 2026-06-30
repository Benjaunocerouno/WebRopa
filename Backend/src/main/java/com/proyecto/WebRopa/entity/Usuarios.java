package com.proyecto.WebRopa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Entity
@Table(name = "usuarios")
@SQLDelete(sql = "UPDATE usuarios SET estado = 'INACTIVO' WHERE id = ?")
@SQLRestriction("estado != 'INACTIVO'")
@JsonPropertyOrder({"id", "nombre", "correo", "telefono", "rol"})
public class Usuarios {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String correo;

    private String nombre;

    private String telefono;

    private String estado = "ACTIVO";

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // Permite recibirla en el POST, pero no la devuelve en el GET
    private String password;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id", nullable = false)
    private Roles rol;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "empresa_id")
    private Empresas empresa;

    @Column(name = "talla_uniforme", length = 10)
    private String tallaUniforme;

    @Column(name = "descuento_empleado")
    private Double descuentoEmpleado;

    @Column(length = 100)
    private String especialidad;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sucursal_id")
    private Sucursales sucursal;

    // ─── Getters y Setters ──────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getPassword() { return password; }

    // Al setear el password, lo encripta automáticamente con BCrypt
    public void setPassword(String password) {
        if (password != null && (password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$"))) {
            this.password = password;
        } else {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            this.password = encoder.encode(password);
        }
    }

    public Roles getRol() { return rol; }
    public void setRol(Roles rol) { this.rol = rol; }

    public Empresas getEmpresa() { return empresa; }
    public void setEmpresa(Empresas empresa) { this.empresa = empresa; }

    public String getTallaUniforme() { return tallaUniforme; }
    public void setTallaUniforme(String tallaUniforme) { this.tallaUniforme = tallaUniforme; }

    public Double getDescuentoEmpleado() { return descuentoEmpleado; }
    public void setDescuentoEmpleado(Double descuentoEmpleado) { this.descuentoEmpleado = descuentoEmpleado; }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public Sucursales getSucursal() { return sucursal; }
    public void setSucursal(Sucursales sucursal) { this.sucursal = sucursal; }
}