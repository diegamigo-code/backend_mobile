package com.wim100.backend.controller;

import com.wim100.backend.dto.AuthDtos.CrearUsuarioRequest;
import com.wim100.backend.dto.AuthDtos.UsuarioResponse;
import com.wim100.backend.security.UsuarioPrincipal;
import com.wim100.backend.service.AuthService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('DIRECCION')")
public class UsuarioController {

    private final AuthService authService;

    public UsuarioController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return authService.listarUsuarios();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crear(@Valid @RequestBody CrearUsuarioRequest request,
                                 @AuthenticationPrincipal UsuarioPrincipal principal) {
        return authService.crearUsuario(request, principal);
    }
}
