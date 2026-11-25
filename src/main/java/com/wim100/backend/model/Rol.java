package com.wim100.backend.model;

public enum Rol {
    EVALUADORA("Evaluadora"),
    DIRECCION("Dirección");

    private final String etiqueta;

    Rol(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String authority() {
        return "ROLE_" + name();
    }
}
