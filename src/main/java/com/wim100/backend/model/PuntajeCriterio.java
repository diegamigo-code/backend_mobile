package com.wim100.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "puntajes_criterio")
public class PuntajeCriterio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evaluacion_id", nullable = false)
    private Evaluacion evaluacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "criterio_id", nullable = false)
    private Criterio criterio;

    @Column(nullable = false)
    private int puntaje;

    @Column(nullable = false, length = 400)
    private String observacion;

    protected PuntajeCriterio() {
    }

    PuntajeCriterio(Evaluacion evaluacion, Criterio criterio, int puntaje, String observacion) {
        this.evaluacion = evaluacion;
        this.criterio = criterio;
        this.puntaje = puntaje;
        this.observacion = observacion;
    }

    public Long getId() { return id; }
    public Evaluacion getEvaluacion() { return evaluacion; }
    public Criterio getCriterio() { return criterio; }
    public int getPuntaje() { return puntaje; }
    public String getObservacion() { return observacion; }
}
