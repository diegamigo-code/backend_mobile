package com.wim100.backend.controller;

import com.wim100.backend.dto.CandidataDtos.CambiarEstadoRequest;
import com.wim100.backend.dto.CandidataDtos.CandidataResponse;
import com.wim100.backend.dto.CandidataDtos.CrearCandidataRequest;
import com.wim100.backend.dto.EvaluacionDtos.CrearEvaluacionRequest;
import com.wim100.backend.dto.EvaluacionDtos.EvaluacionResponse;
import com.wim100.backend.dto.EvaluacionDtos.HistorialResponse;
import com.wim100.backend.security.UsuarioPrincipal;
import com.wim100.backend.service.CandidataService;
import com.wim100.backend.service.EvaluacionService;
import com.wim100.backend.service.HistorialService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/candidatas")
public class CandidataController {

    private final CandidataService candidataService;
    private final EvaluacionService evaluacionService;
    private final HistorialService historialService;

    public CandidataController(CandidataService candidataService, EvaluacionService evaluacionService,
                               HistorialService historialService) {
        this.candidataService = candidataService;
        this.evaluacionService = evaluacionService;
        this.historialService = historialService;
    }

    @GetMapping
    public List<CandidataResponse> listar(@RequestParam(required = false) String q) {
        return candidataService.listar(q);
    }

    @GetMapping("/{id}")
    public CandidataResponse obtener(@PathVariable Long id) {
        return candidataService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('EVALUADORA','DIRECCION')")
    public CandidataResponse registrar(@Valid @RequestBody CrearCandidataRequest request,
                                       @AuthenticationPrincipal UsuarioPrincipal principal) {
        return candidataService.registrar(request, principal);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('DIRECCION')")
    public CandidataResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambiarEstadoRequest request,
                                           @AuthenticationPrincipal UsuarioPrincipal principal) {
        return candidataService.cambiarEstado(id, request.estado(), principal);
    }

    @GetMapping("/{id}/evaluaciones")
    public List<EvaluacionResponse> evaluaciones(@PathVariable Long id) {
        return evaluacionService.listarPorCandidata(id);
    }

    @PostMapping("/{id}/evaluaciones")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('EVALUADORA')")
    public EvaluacionResponse evaluar(@PathVariable Long id, @Valid @RequestBody CrearEvaluacionRequest request,
                                      @AuthenticationPrincipal UsuarioPrincipal principal) {
        return evaluacionService.registrar(id, request, principal);
    }

    @GetMapping("/{id}/historial")
    @PreAuthorize("hasRole('DIRECCION')")
    public List<HistorialResponse> historial(@PathVariable Long id) {
        candidataService.buscar(id);
        return historialService.listarPorCandidata(id);
    }
}
