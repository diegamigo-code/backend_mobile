package com.wim100.backend.controller;

import com.wim100.backend.dto.AuthDtos.LoginRequest;
import com.wim100.backend.dto.AuthDtos.LoginResponse;
import com.wim100.backend.dto.AuthDtos.UsuarioResponse;
import com.wim100.backend.security.UsuarioPrincipal;
import com.wim100.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UsuarioPrincipal principal) {
        authService.logout(principal);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public UsuarioResponse me(@AuthenticationPrincipal UsuarioPrincipal principal) {
        return authService.perfil(principal);
    }
}
