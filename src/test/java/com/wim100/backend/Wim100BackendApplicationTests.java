package com.wim100.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.wim100.backend.dto.AuthDtos.LoginRequest;
import com.wim100.backend.dto.AuthDtos.LoginResponse;
import com.wim100.backend.model.Rol;
import com.wim100.backend.service.AuthService;
import com.wim100.backend.service.EvaluacionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class Wim100BackendApplicationTests {

    @Autowired
    AuthService authService;

    @Autowired
    EvaluacionService evaluacionService;

    @Test
    void loginConCredencialesCorrectasEntregaToken() {
        LoginResponse r = authService.login(new LoginRequest("direccion", "Test.12345"));
        assertFalse(r.token().isBlank());
        assertEquals(Rol.DIRECCION, r.usuario().rol());
    }

    @Test
    void loginConClaveIncorrectaFalla() {
        assertThrows(BadCredentialsException.class,
                () -> authService.login(new LoginRequest("direccion", "incorrecta")));
    }

    @Test
    void semillaCargaCincoCriteriosYRanking() {
        assertEquals(5, evaluacionService.listarCriterios().size());
        assertEquals(6, evaluacionService.ranking().size());
    }
}
