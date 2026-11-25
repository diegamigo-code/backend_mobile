package com.wim100.backend.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "evaluaciones",
        uniqueConstraints = @UniqueConstraint(name = "uk_evaluacion_candidata_evaluadora",
                columnNames = {"candidata_id", "evaluadora_id"})
)
public class Evaluacion {

    public static final int MINIMO_PARA_CONSOLIDAR = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidata_id", nullable = false)
    private Candidata candidata;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evaluadora_id", nullable = false)
    private Usuario evaluadora;

    @Column(name = "puntaje_total", nullable = false)
    private double puntajeTotal;

    @Column(name = "observacion_general", nullable = false, length = 400)
    private String observacionGeneral;

    @Column(name = "fecha_creacion", nullable = false)
    private Instant fechaCreacion = Instant.now();

    @OneToMany(mappedBy = "evaluacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<PuntajeCriterio> puntajes = new ArrayList<>();

    protected Evaluacion() {
    }

    public Evaluacion(Candidata candidata, Usuario evaluadora, double puntajeTotal, String observacionGeneral) {
        this.candidata = candidata;
        this.evaluadora = evaluadora;
        this.puntajeTotal = puntajeTotal;
        this.observacionGeneral = observacionGeneral;
    }

    public void agregarPuntaje(Criterio criterio, int puntaje, String observacion) {
        puntajes.add(new PuntajeCriterio(this, criterio, puntaje, observacion));
    }

    public Long getId() { return id; }
    public Candidata getCandidata() { return candidata; }
    public Usuario getEvaluadora() { return evaluadora; }
    public double getPuntajeTotal() { return puntajeTotal; }
    public String getObservacionGeneral() { return observacionGeneral; }
    public Instant getFechaCreacion() { return fechaCreacion; }
    public List<PuntajeCriterio> getPuntajes() { return puntajes; }
}
