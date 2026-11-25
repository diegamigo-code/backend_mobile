package com.wim100.backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class CalculadoraPuntaje {

    private CalculadoraPuntaje() {
    }

    public static double calcularPuntajePonderado(List<int[]> puntajesConPeso) {
        if (puntajesConPeso == null || puntajesConPeso.isEmpty()) {
            return 0.0;
        }
        int pesoTotal = 0;
        long sumaPonderada = 0;
        for (int[] par : puntajesConPeso) {
            sumaPonderada += (long) par[0] * par[1];
            pesoTotal += par[1];
        }
        if (pesoTotal == 0) {
            return 0.0;
        }
        return redondear((double) sumaPonderada / (pesoTotal * 5.0) * 100.0);
    }

    public static String obtenerCategoria(double puntaje) {
        if (puntaje >= 85.0) return "Sobresaliente";
        if (puntaje >= 70.0) return "Sólida";
        if (puntaje >= 50.0) return "En desarrollo";
        return "Insuficiente";
    }

    public static double redondear(double valor) {
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
