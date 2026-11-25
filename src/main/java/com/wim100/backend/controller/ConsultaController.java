package com.wim100.backend.controller;

import com.wim100.backend.dto.EvaluacionDtos.CriterioResponse;
import com.wim100.backend.dto.EvaluacionDtos.HistorialResponse;
import com.wim100.backend.dto.EvaluacionDtos.RankingItemResponse;
import com.wim100.backend.model.Candidata;
import com.wim100.backend.model.EstadoCandidatura;
import com.wim100.backend.service.EvaluacionService;
import com.wim100.backend.service.HistorialService;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ConsultaController {

    private final EvaluacionService evaluacionService;
    private final HistorialService historialService;

    public ConsultaController(EvaluacionService evaluacionService, HistorialService historialService) {
        this.evaluacionService = evaluacionService;
        this.historialService = historialService;
    }

    @GetMapping("/criterios")
    public List<CriterioResponse> criterios() {
        return evaluacionService.listarCriterios();
    }

    @GetMapping("/catalogos/areas")
    public List<String> areas() {
        return Candidata.AREAS;
    }

    @GetMapping("/catalogos/estados")
    public List<Map<String, Object>> estados() {
        return Arrays.stream(EstadoCandidatura.values())
                .map(e -> Map.<String, Object>of("valor", e.name(), "etiqueta", e.getEtiqueta(), "resuelta", e.isResuelta()))
                .toList();
    }

    @GetMapping("/ranking")
    @PreAuthorize("hasRole('DIRECCION')")
    public List<RankingItemResponse> ranking() {
        return evaluacionService.ranking();
    }

    @GetMapping("/historial")
    @PreAuthorize("hasRole('DIRECCION')")
    public List<HistorialResponse> historial() {
        return historialService.listarTodo();
    }
}
