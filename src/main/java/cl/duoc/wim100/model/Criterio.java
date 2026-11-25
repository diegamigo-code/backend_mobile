package cl.duoc.wim100.model;

import java.util.List;

public record Criterio(int id, String nombre, String descripcion, int pesoPorcentaje, int orden) {
    public static final List<Criterio> MATRIZ = List.of(
        new Criterio(1, "Experiencia Técnica y Trayectoria", "Experiencia y proyectos previos en áreas tecnológicas.", 25, 1),
        new Criterio(2, "Conocimientos en Ciberseguridad / TI", "Dominio de herramientas, metodologías y certificaciones.", 20, 2),
        new Criterio(3, "Habilidades Blandas y Liderazgo", "Comunicación, trabajo en equipo y liderazgo.", 20, 3),
        new Criterio(4, "Potencial de Crecimiento e Impacto", "Proyección y aporte a la organización.", 20, 4),
        new Criterio(5, "Calidad de la Postulación y CV", "Claridad y estructura de la postulación.", 15, 5)
    );
}
