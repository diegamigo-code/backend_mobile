package com.wim100.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "historial", indexes = @Index(name = "idx_historial_candidata", columnList = "candidata_id"))
public class RegistroHistorial {

    public static final class Acciones {
        public static final String REGISTRO_CANDIDATA = "Registro de candidatura";
        public static final String EVALUACION_CREADA = "Evaluación registrada";
        public static final String INTENTO_DUPLICADO = "Intento de evaluación duplicada";
        public static final String CAMBIO_ESTADO = "Cambio de estado";
        public static final String INICIO_SESION = "Inicio de sesión";
        public static final String INICIO_SESION_FALLIDO = "Inicio de sesión fallido";
        public static final String CIERRE_SESION = "Cierre de sesión";
        public static final String USUARIO_CREADO = "Usuario creado";

        private Acciones() {
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "candidata_id")
    private Long candidataId;

    @Column(name = "usuario_nombre", nullable = false, length = 120)
    private String usuarioNombre;

    @Column(name = "usuario_rol", nullable = false, length = 30)
    private String usuarioRol;

    @Column(nullable = false, length = 60)
    private String accion;

    @Column(nullable = false, length = 500)
    private String detalle;

    @Column(nullable = false)
    private Instant fecha = Instant.now();

    protected RegistroHistorial() {
    }

    public RegistroHistorial(Long candidataId, String usuarioNombre, String usuarioRol, String accion, String detalle) {
        this.candidataId = candidataId;
        this.usuarioNombre = usuarioNombre;
        this.usuarioRol = usuarioRol;
        this.accion = accion;
        this.detalle = detalle.length() > 500 ? detalle.substring(0, 500) : detalle;
    }

    public Long getId() { return id; }
    public Long getCandidataId() { return candidataId; }
    public String getUsuarioNombre() { return usuarioNombre; }
    public String getUsuarioRol() { return usuarioRol; }
    public String getAccion() { return accion; }
    public String getDetalle() { return detalle; }
    public Instant getFecha() { return fecha; }
}
