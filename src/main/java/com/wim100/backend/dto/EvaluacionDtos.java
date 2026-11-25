package com.wim100.backend.dto;

import com.wim100.backend.model.Criterio;
import com.wim100.backend.model.EstadoCandidatura;
import com.wim100.backend.model.Evaluacion;
import com.wim100.backend.model.PuntajeCriterio;
import com.wim100.backend.model.RegistroHistorial;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

public final class EvaluacionDtos {

    private EvaluacionDtos() {
    }

    public record CriterioResponse(Long id, String nombre, String descripcion, int pesoPorcentaje, int orden) {
        public static CriterioResponse from(Criterio c) {
            return new CriterioResponse(c.getId(), c.getNombre(), c.getDescripcion(), c.getPesoPorcentaje(), c.getOrden());
        }
    }

    public record PuntajeRequest(
            @NotNull(message = "El criterio es obligatorio") Long criterioId,
            @NotNull(message = "El puntaje es obligatorio")
            @Min(value = 1, message = "El puntaje debe estar entre 1 y 5")
            @Max(value = 5, message = "El puntaje debe estar entre 1 y 5")
            Integer puntaje,
            @Size(max = 400, message = "La observación no puede superar 400 caracteres")
            String observacion
    ) {
    }

    public record CrearEvaluacionRequest(
            @NotEmpty(message = "Debe calificar todos los criterios")
            List<@Valid PuntajeRequest> puntajes,

            @NotBlank(message = "La observación general es obligatoria")
            @Size(min = 20, max = 400, message = "La observación general debe tener entre 20 y 400 caracteres")
            String observacionGeneral
    ) {
    }

    public record PuntajeResponse(Long criterioId, String criterioNombre, int pesoPorcentaje, int puntaje,
                                  String observacion) {
        public static PuntajeResponse from(PuntajeCriterio p) {
            return new PuntajeResponse(p.getCriterio().getId(), p.getCriterio().getNombre(),
                    p.getCriterio().getPesoPorcentaje(), p.getPuntaje(), p.getObservacion());
        }
    }

    public record EvaluacionResponse(
            Long id,
            Long candidataId,
            String evaluadoraUsername,
            String evaluadoraNombre,
            double puntajeTotal,
            String categoria,
            String observacionGeneral,
            Instant fechaCreacion,
            List<PuntajeResponse> puntajes
    ) {
        public static EvaluacionResponse from(Evaluacion e, String categoria) {
            return new EvaluacionResponse(e.getId(), e.getCandidata().getId(), e.getEvaluadora().getUsername(),
                    e.getEvaluadora().getNombreCompleto(), e.getPuntajeTotal(), categoria,
                    e.getObservacionGeneral(), e.getFechaCreacion(),
                    e.getPuntajes().stream().map(PuntajeResponse::from).toList());
        }
    }

    public record RankingItemResponse(
            int posicion,
            Long candidataId,
            String candidataNombre,
            String area,
            EstadoCandidatura estado,
            String estadoEtiqueta,
            int cantidadEvaluaciones,
            double promedioPuntaje,
            String categoria,
            boolean consolidado
    ) {
    }

    public record HistorialResponse(Long id, Long candidataId, String usuarioNombre, String usuarioRol,
                                    String accion, String detalle, Instant fecha) {
        public static HistorialResponse from(RegistroHistorial r) {
            return new HistorialResponse(r.getId(), r.getCandidataId(), r.getUsuarioNombre(), r.getUsuarioRol(),
                    r.getAccion(), r.getDetalle(), r.getFecha());
        }
    }
}
