package com.wim100.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class CalculadoraPuntajeTest {

    @Test
    void puntajeMaximoEs100() {
        double r = CalculadoraPuntaje.calcularPuntajePonderado(List.of(
                new int[]{5, 25}, new int[]{5, 20}, new int[]{5, 20}, new int[]{5, 20}, new int[]{5, 15}));
        assertEquals(100.0, r);
    }

    @Test
    void ponderaSegunPeso() {
        double r = CalculadoraPuntaje.calcularPuntajePonderado(List.of(
                new int[]{4, 25}, new int[]{3, 20}, new int[]{5, 20}, new int[]{2, 20}, new int[]{4, 15}));
        assertEquals(72.0, r);
        assertEquals("Sólida", CalculadoraPuntaje.obtenerCategoria(r));
    }

    @Test
    void listaVaciaEsCero() {
        assertEquals(0.0, CalculadoraPuntaje.calcularPuntajePonderado(List.of()));
        assertEquals("Insuficiente", CalculadoraPuntaje.obtenerCategoria(0.0));
    }
}
