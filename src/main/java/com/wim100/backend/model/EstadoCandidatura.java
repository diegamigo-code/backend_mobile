package com.wim100.backend.model;

public enum EstadoCandidatura {
    POSTULADA("Postulada", false),
    EN_EVALUACION("En evaluación", false),
    EVALUADA("Evaluada", false),
    SELECCIONADA("Seleccionada", true),
    NO_SELECCIONADA("No seleccionada", true);

    private final String etiqueta;
    private final boolean resuelta;

    EstadoCandidatura(String etiqueta, boolean resuelta) {
        this.etiqueta = etiqueta;
        this.resuelta = resuelta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public boolean isResuelta() {
        return resuelta;
    }
}
