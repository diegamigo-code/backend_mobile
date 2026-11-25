package com.wim100.backend.exception;

import java.util.Map;

public class ValidacionException extends RuntimeException {

    private final Map<String, String> campos;

    public ValidacionException(String campo, String mensaje) {
        super(mensaje);
        this.campos = Map.of(campo, mensaje);
    }

    public ValidacionException(Map<String, String> campos) {
        super("La solicitud contiene datos inválidos");
        this.campos = Map.copyOf(campos);
    }

    public Map<String, String> getCampos() {
        return campos;
    }
}
