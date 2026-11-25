package cl.duoc.wim100.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"postulante_id", "evaluadora"}))
@Getter
@NoArgsConstructor
public class Evaluacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "postulante_id")
    private Postulante postulante;
    @Column(nullable = false)
    private String evaluadora;
    @Column(nullable = false, length = 400)
    private String observacionGeneral;
    private double puntajeTotal;
    @ElementCollection
    @CollectionTable(name = "notas_evaluacion", joinColumns = @JoinColumn(name = "evaluacion_id"))
    private List<Nota> notas;

    public Evaluacion(Postulante postulante, String evaluadora, String observacion, double total, List<Nota> notas) {
        this.postulante = postulante;
        this.evaluadora = evaluadora;
        this.observacionGeneral = observacion;
        this.puntajeTotal = total;
        this.notas = notas;
    }

    @Embeddable
    @Getter
    @NoArgsConstructor
    public static class Nota {
        private int criterioId;
        private int puntaje;
        @Lob private String observacion;
        public Nota(int criterioId, int puntaje, String observacion) {
            this.criterioId = criterioId;
            this.puntaje = puntaje;
            this.observacion = observacion;
        }
    }
}
