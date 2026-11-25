package com.wim100.backend.service;

import com.wim100.backend.dto.AuthDtos.CrearUsuarioRequest;
import com.wim100.backend.dto.AuthDtos.LoginRequest;
import com.wim100.backend.dto.AuthDtos.LoginResponse;
import com.wim100.backend.dto.AuthDtos.UsuarioResponse;
import com.wim100.backend.exception.ConflictoException;
import com.wim100.backend.exception.RecursoNoEncontradoException;
import com.wim100.backend.model.RegistroHistorial.Acciones;
import com.wim100.backend.model.Usuario;
import com.wim100.backend.repository.UsuarioRepository;
import com.wim100.backend.security.JwtService;
import com.wim100.backend.security.JwtService.TokenEmitido;
import com.wim100.backend.security.UsuarioPrincipal;
import java.util.List;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final HistorialService historialService;

    public AuthService(AuthenticationManager authenticationManager, UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService, HistorialService historialService) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.historialService = historialService;
    }

    public LoginResponse login(LoginRequest request) {
        String username = request.username().trim().toLowerCase();
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, request.password()));
        } catch (AuthenticationException e) {
            historialService.registrar(null, username, "Desconocido", Acciones.INICIO_SESION_FALLIDO,
                    "Credenciales inválidas para el usuario: " + username);
            throw e;
        }

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        TokenEmitido token = jwtService.generarToken(usuario);

        historialService.registrar(null, usuario.getNombreCompleto(), usuario.getRol().getEtiqueta(),
                Acciones.INICIO_SESION, "Sesión iniciada con rol: " + usuario.getRol().getEtiqueta());

        return new LoginResponse(token.token(), "Bearer", token.expiraEn(), UsuarioResponse.from(usuario));
    }

    public void logout(UsuarioPrincipal principal) {
        historialService.registrar(null, principal.getNombreCompleto(), principal.getRol().getEtiqueta(),
                Acciones.CIERRE_SESION, "Sesión cerrada");
    }

    @Transactional(readOnly = true)
    public UsuarioResponse perfil(UsuarioPrincipal principal) {
        return usuarioRepository.findById(principal.getId())
                .map(UsuarioResponse::from)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioRepository.findAllByOrderByIdAsc().stream().map(UsuarioResponse::from).toList();
    }

    @Transactional
    public UsuarioResponse crearUsuario(CrearUsuarioRequest request, UsuarioPrincipal creador) {
        String username = request.username().trim().toLowerCase();
        if (usuarioRepository.existsByUsername(username)) {
            throw new ConflictoException("El nombre de usuario ya existe");
        }
        Usuario nuevo = usuarioRepository.save(new Usuario(username, request.nombreCompleto().trim(),
                passwordEncoder.encode(request.password()), request.rol()));

        historialService.registrar(null, creador.getNombreCompleto(), creador.getRol().getEtiqueta(),
                Acciones.USUARIO_CREADO, "Usuario creado: " + username + " (" + request.rol().getEtiqueta() + ")");
        return UsuarioResponse.from(nuevo);
    }
}
