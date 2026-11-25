package com.wim100.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "criterios")
public class Criterio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 300)
    private String descripcion;

    @Column(name = "peso_porcentaje", nullable = false)
    private int pesoPorcentaje;

    @Column(nullable = false)
    private int orden;

    protected Criterio() {
    }

    public Criterio(String nombre, String descripcion, int pesoPorcentaje, int orden) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.pesoPorcentaje = pesoPorcentaje;
        this.orden = orden;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public int getPesoPorcentaje() { return pesoPorcentaje; }
    public int getOrden() { return orden; }
}
