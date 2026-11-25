package cl.duoc.wim100.controller;

import cl.duoc.wim100.dto.LoginRequest;
import cl.duoc.wim100.dto.LoginResponse;
import cl.duoc.wim100.service.AuthService;
import jakarta.validation.Valid;
import jakarta.servlet.http.*;
import cl.duoc.wim100.repository.EventoRepository;
import cl.duoc.wim100.model.Evento;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;

    private final EventoRepository eventos;

    public AuthController(AuthService service, EventoRepository eventos) {
        this.service = service;
        this.eventos = eventos;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req, HttpServletRequest request) {
        LoginResponse cuenta = service.login(req);
        HttpSession anterior = request.getSession(false);
        if (anterior != null) anterior.invalidate();
        request.getSession(true).setAttribute("cuenta", cuenta);
        eventos.save(new Evento(null, cuenta, "Inicio de sesión", "Acceso desde la aplicación"));
        return cuenta;
    }

    @PostMapping("/logout")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void logout(HttpSession session, @SessionAttribute LoginResponse cuenta) {
        session.invalidate();
        eventos.save(new Evento(null, cuenta, "Cierre de sesión", "Sesión cerrada"));
    }
}
