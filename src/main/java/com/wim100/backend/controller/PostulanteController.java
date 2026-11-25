package com.wim100.backend.controller;

import com.wim100.backend.dto.CandidataDtos.CambiarEstadoRequest;
import com.wim100.backend.dto.PostulanteDtos.PostulanteRequest;
import com.wim100.backend.dto.PostulanteDtos.PostulanteResponse;
import com.wim100.backend.security.UsuarioPrincipal;
import com.wim100.backend.service.CandidataService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/postulantes")
public class PostulanteController {

    private final CandidataService candidataService;

    public PostulanteController(CandidataService candidataService) {
        this.candidataService = candidataService;
    }

    @GetMapping
    public List<PostulanteResponse> listar(@RequestParam(required = false) String q) {
        return candidataService.buscarTodas(q).stream().map(PostulanteResponse::from).toList();
    }

    @GetMapping("/{id}")
    public PostulanteResponse obtener(@PathVariable Long id) {
        return PostulanteResponse.from(candidataService.buscar(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('EVALUADORA','DIRECCION')")
    public PostulanteResponse crear(@Valid @RequestBody PostulanteRequest request,
                                    @AuthenticationPrincipal UsuarioPrincipal principal) {
        return PostulanteResponse.from(candidataService.crear(request.aDatos(), principal));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EVALUADORA')")
    public PostulanteResponse actualizar(@PathVariable Long id, @Valid @RequestBody PostulanteRequest request,
                                         @AuthenticationPrincipal UsuarioPrincipal principal) {
        return PostulanteResponse.from(candidataService.actualizar(id, request.aDatos(), principal));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('EVALUADORA','DIRECCION')")
    public PostulanteResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambiarEstadoRequest request,
                                            @AuthenticationPrincipal UsuarioPrincipal principal) {
        return PostulanteResponse.from(candidataService.actualizarEstado(id, request.estado(), principal));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('EVALUADORA')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @AuthenticationPrincipal UsuarioPrincipal principal) {
        candidataService.eliminar(id, principal);
        return ResponseEntity.noContent().build();
    }
}
