package cl.duoc.wim100.service;

import cl.duoc.wim100.dto.*;
import cl.duoc.wim100.model.*;
import cl.duoc.wim100.repository.*;
import cl.duoc.wim100.exception.RecursoNoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Service
public class PanelService {
    private final PostulanteRepository postulantes;
    private final EvaluacionRepository evaluaciones;
    private final EventoRepository eventos;

    public PanelService(PostulanteRepository postulantes, EvaluacionRepository evaluaciones, EventoRepository eventos) {
        this.postulantes = postulantes;
        this.evaluaciones = evaluaciones;
        this.eventos = eventos;
    }

    public record Panel(List<Postulante> postulantes, List<Criterio> criterios,
            List<EvaluacionRepository.Resultado> ranking, List<Evento> historial) {}

    @Transactional(readOnly = true)
    public Panel leer() {
        return new Panel(postulantes.findAllByOrderByFechaRegistroDesc(), Criterio.MATRIZ,
                evaluaciones.ranking(), eventos.findByCandidataIdIsNotNullOrderByIdDesc());
    }

    @Transactional
    public long evaluar(Long id, LoginResponse cuenta, EvaluacionRequest req) {
        Postulante candidata = postulantes.bloquearPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("La candidata ya no existe"));
        if (evaluaciones.existsByPostulanteIdAndEvaluadora(id, cuenta.usuario())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya evaluaste a esta candidata");
        }

        Set<Integer> criterios = new HashSet<>();
        List<Evaluacion.Nota> notas = new ArrayList<>();
        if (req.observacionGeneral().trim().length() < 20) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La observación general requiere 20 caracteres");
        }
        int sumaPonderada = 0;
        for (EvaluacionRequest.Nota nota : req.notas()) {
            if (!criterios.add(nota.criterioId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hay criterios repetidos");
            }
            String observacion = nota.observacion().trim();
            if (nota.puntaje() <= 2 && observacion.length() < 10) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Justifica las notas bajas con al menos 10 caracteres");
            }
            Criterio criterio = Criterio.MATRIZ.get(nota.criterioId() - 1);
            sumaPonderada += nota.puntaje() * criterio.pesoPorcentaje();
            notas.add(new Evaluacion.Nota(criterio.id(), nota.puntaje(), observacion));
        }
        double total = sumaPonderada / 5.0;
        Evaluacion evaluacion = evaluaciones.save(new Evaluacion(candidata, cuenta.usuario(),
                req.observacionGeneral().trim(), total, notas));
        if (candidata.getEstado() == EstadoPostulante.POSTULADA) {
            candidata.setEstado(EstadoPostulante.EN_EVALUACION);
        }
        eventos.save(new Evento(id, cuenta, "Evaluación registrada", "Puntaje ponderado: " + total + "/100"));
        return evaluacion.getId();
    }
}
