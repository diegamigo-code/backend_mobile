package com.wim100.backend.service;

import com.wim100.backend.dto.EvaluacionDtos.CrearEvaluacionRequest;
import com.wim100.backend.dto.EvaluacionDtos.CriterioResponse;
import com.wim100.backend.dto.EvaluacionDtos.EvaluacionResponse;
import com.wim100.backend.dto.EvaluacionDtos.PuntajeRequest;
import com.wim100.backend.dto.EvaluacionDtos.RankingItemResponse;
import com.wim100.backend.exception.ConflictoException;
import com.wim100.backend.exception.RecursoNoEncontradoException;
import com.wim100.backend.exception.ValidacionException;
import com.wim100.backend.model.Candidata;
import com.wim100.backend.model.Criterio;
import com.wim100.backend.model.EstadoCandidatura;
import com.wim100.backend.model.Evaluacion;
import com.wim100.backend.model.RegistroHistorial.Acciones;
import com.wim100.backend.model.Usuario;
import com.wim100.backend.repository.CandidataRepository;
import com.wim100.backend.repository.CriterioRepository;
import com.wim100.backend.repository.EvaluacionRepository;
import com.wim100.backend.repository.UsuarioRepository;
import com.wim100.backend.security.UsuarioPrincipal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EvaluacionService {

    private final EvaluacionRepository evaluacionRepository;
    private final CriterioRepository criterioRepository;
    private final CandidataRepository candidataRepository;
    private final UsuarioRepository usuarioRepository;
    private final CandidataService candidataService;
    private final HistorialService historialService;

    public EvaluacionService(EvaluacionRepository evaluacionRepository, CriterioRepository criterioRepository,
                             CandidataRepository candidataRepository, UsuarioRepository usuarioRepository,
                             CandidataService candidataService, HistorialService historialService) {
        this.evaluacionRepository = evaluacionRepository;
        this.criterioRepository = criterioRepository;
        this.candidataRepository = candidataRepository;
        this.usuarioRepository = usuarioRepository;
        this.candidataService = candidataService;
        this.historialService = historialService;
    }

    @Transactional(readOnly = true)
    public List<CriterioResponse> listarCriterios() {
        return criterioRepository.findAllByOrderByOrdenAsc().stream().map(CriterioResponse::from).toList();
    }

    @Transactional
    public EvaluacionResponse registrar(Long candidataId, CrearEvaluacionRequest req, UsuarioPrincipal principal) {
        Candidata candidata = candidataService.buscar(candidataId);
        Usuario evaluadora = usuarioRepository.findById(principal.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        if (candidata.getEstado().isResuelta()) {
            throw new ConflictoException("La candidatura ya fue resuelta (" + candidata.getEstado().getEtiqueta()
                    + ") y no admite nuevas evaluaciones");
        }

        if (evaluacionRepository.existsByCandidata_IdAndEvaluadora_Id(candidataId, evaluadora.getId())) {
            historialService.registrar(candidataId, evaluadora.getNombreCompleto(), evaluadora.getRol().getEtiqueta(),
                    Acciones.INTENTO_DUPLICADO, "La evaluadora " + evaluadora.getNombreCompleto()
                            + " intentó evaluar nuevamente a la candidata ID " + candidataId);
            throw new ConflictoException("Ya has registrado una evaluación para esta candidata");
        }

        List<Criterio> criterios = criterioRepository.findAllByOrderByOrdenAsc();
        Map<Long, Criterio> criteriosPorId = criterios.stream()
                .collect(Collectors.toMap(Criterio::getId, Function.identity()));
        validarPuntajes(req.puntajes(), criteriosPorId);

        List<int[]> paresPuntajePeso = new ArrayList<>();
        for (PuntajeRequest p : req.puntajes()) {
            paresPuntajePeso.add(new int[]{p.puntaje(), criteriosPorId.get(p.criterioId()).getPesoPorcentaje()});
        }
        double puntajeTotal = CalculadoraPuntaje.calcularPuntajePonderado(paresPuntajePeso);

        Evaluacion evaluacion = new Evaluacion(candidata, evaluadora, puntajeTotal, req.observacionGeneral().trim());
        req.puntajes().stream()
                .sorted(Comparator.comparingInt(p -> criteriosPorId.get(p.criterioId()).getOrden()))
                .forEach(p -> evaluacion.agregarPuntaje(criteriosPorId.get(p.criterioId()), p.puntaje(),
                        p.observacion() == null ? "" : p.observacion().trim()));
        evaluacionRepository.save(evaluacion);

        historialService.registrar(candidataId, evaluadora.getNombreCompleto(), evaluadora.getRol().getEtiqueta(),
                Acciones.EVALUACION_CREADA,
                "Evaluación registrada con nota " + String.format(Locale.ROOT, "%.1f", puntajeTotal) + "/100");

        if (candidata.getEstado() == EstadoCandidatura.POSTULADA) {
            candidata.setEstado(EstadoCandidatura.EN_EVALUACION);
        }

        return EvaluacionResponse.from(evaluacion, CalculadoraPuntaje.obtenerCategoria(puntajeTotal));
    }

    @Transactional(readOnly = true)
    public List<EvaluacionResponse> listarPorCandidata(Long candidataId) {
        candidataService.buscar(candidataId);
        return evaluacionRepository.findByCandidata_IdOrderByFechaCreacionAsc(candidataId).stream()
                .map(e -> EvaluacionResponse.from(e, CalculadoraPuntaje.obtenerCategoria(e.getPuntajeTotal())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RankingItemResponse> ranking() {
        Map<Long, List<Evaluacion>> porCandidata = evaluacionRepository.findAllBy().stream()
                .collect(Collectors.groupingBy(e -> e.getCandidata().getId()));

        record Fila(Candidata c, int cantidad, double promedio) {
        }

        List<Fila> filas = candidataRepository.findAll().stream()
                .map(c -> {
                    List<Evaluacion> evs = porCandidata.getOrDefault(c.getId(), List.of());
                    double promedio = evs.stream().mapToDouble(Evaluacion::getPuntajeTotal).average().orElse(0.0);
                    return new Fila(c, evs.size(), CalculadoraPuntaje.redondear(promedio));
                })
                .sorted(Comparator.comparingDouble(Fila::promedio).reversed()
                        .thenComparing(f -> f.c().getNombre()))
                .toList();

        List<RankingItemResponse> resultado = new ArrayList<>();
        int posicion = 1;
        for (Fila f : filas) {
            resultado.add(new RankingItemResponse(posicion++, f.c().getId(), f.c().getNombre(), f.c().getArea(),
                    f.c().getEstado(), f.c().getEstado().getEtiqueta(), f.cantidad(), f.promedio(),
                    CalculadoraPuntaje.obtenerCategoria(f.promedio()),
                    f.cantidad() >= Evaluacion.MINIMO_PARA_CONSOLIDAR));
        }
        return resultado;
    }

    private void validarPuntajes(List<PuntajeRequest> puntajes, Map<Long, Criterio> criteriosPorId) {
        Map<String, String> errores = new LinkedHashMap<>();
        Set<Long> vistos = new HashSet<>();

        for (int i = 0; i < puntajes.size(); i++) {
            PuntajeRequest p = puntajes.get(i);
            String campo = "puntajes[" + i + "]";
            if (!criteriosPorId.containsKey(p.criterioId())) {
                errores.put(campo + ".criterioId", "Criterio inexistente: " + p.criterioId());
                continue;
            }
            if (!vistos.add(p.criterioId())) {
                errores.put(campo + ".criterioId", "Criterio repetido: " + p.criterioId());
            }
            String obs = p.observacion() == null ? "" : p.observacion().trim();
            if (p.puntaje() <= 2 && obs.length() < 10) {
                errores.put(campo + ".observacion",
                        "Para puntajes de 1 o 2 es obligatoria una justificación (mínimo 10 caracteres)");
            }
        }

        Set<Long> faltantes = new HashSet<>(criteriosPorId.keySet());
        faltantes.removeAll(vistos);
        if (!faltantes.isEmpty()) {
            errores.put("puntajes", "Faltan criterios por calificar: " + faltantes.stream().sorted().toList());
        }

        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }
    }
}
