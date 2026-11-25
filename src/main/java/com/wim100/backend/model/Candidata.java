package com.wim100.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "candidatas")
public class Candidata {

    public static final List<String> AREAS = List.of(
            "Ciberseguridad",
            "Desarrollo de Software",
            "Infraestructura y TI",
            "Datos e IA",
            "Gestión de Proyectos",
            "Investigación y I+D"
    );

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, unique = true, length = 150)
    private String correo;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(nullable = false, length = 60)
    private String area;

    @Column(name = "anos_experiencia", nullable = false)
    private int anosExperiencia;

    @Column(nullable = false, length = 500)
    private String resumen;

    @Column(name = "enlace_perfil", nullable = false, length = 300)
    private String enlacePerfil;

    @Column(name = "uri_cv", nullable = false, length = 300)
    private String uriCv;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCandidatura estado = EstadoCandidatura.POSTULADA;

    @Column(name = "registrada_por", nullable = false, length = 120)
    private String registradaPor;

    @Column(name = "fecha_registro", nullable = false)
    private Instant fechaRegistro = Instant.now();

    protected Candidata() {
    }

    public Candidata(String nombre, String correo, String telefono, String area, int anosExperiencia,
                     String resumen, String enlacePerfil, String uriCv, String registradaPor) {
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.area = area;
        this.anosExperiencia = anosExperiencia;
        this.resumen = resumen;
        this.enlacePerfil = enlacePerfil;
        this.uriCv = uriCv;
        this.registradaPor = registradaPor;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public String getTelefono() { return telefono; }
    public String getArea() { return area; }
    public int getAnosExperiencia() { return anosExperiencia; }
    public String getResumen() { return resumen; }
    public String getEnlacePerfil() { return enlacePerfil; }
    public String getUriCv() { return uriCv; }
    public EstadoCandidatura getEstado() { return estado; }
    public String getRegistradaPor() { return registradaPor; }
    public Instant getFechaRegistro() { return fechaRegistro; }

    public void setEstado(EstadoCandidatura estado) { this.estado = estado; }
}
