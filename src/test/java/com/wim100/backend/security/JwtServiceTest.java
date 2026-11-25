package com.wim100.backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.wim100.backend.config.AppProperties;
import com.wim100.backend.model.Rol;
import com.wim100.backend.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "dGVzdC1zb2xvLXBhcmEtcHJ1ZWJhcy13aW0xMDAtMzItYnl0ZXMtbWluaW1v";

    private static JwtService servicio(String secret, Duration exp) {
        return new JwtService(new AppProperties(new AppProperties.Jwt(secret, exp, "wim100-test"), null, null, null));
    }

    @Test
    void generaYValidaToken() {
        JwtService jwt = servicio(SECRET, Duration.ofMinutes(5));
        Usuario u = new Usuario("andrea_torres", "Andrea Torres", "hash", Rol.EVALUADORA);

        Claims claims = jwt.validar(jwt.generarToken(u).token());

        assertEquals("andrea_torres", claims.getSubject());
        assertEquals("EVALUADORA", claims.get(JwtService.CLAIM_ROL, String.class));
    }

    @Test
    void rechazaTokenFirmadoConOtraClave() {
        Usuario u = new Usuario("x_user", "X", "hash", Rol.DIRECCION);
        String token = servicio(SECRET, Duration.ofMinutes(5)).generarToken(u).token();
        JwtService otra = servicio("b3RyYS1jbGF2ZS1jb21wbGV0YW1lbnRlLWRpc3RpbnRhLTMyLWJ5dGVz", Duration.ofMinutes(5));

        assertThrows(JwtException.class, () -> otra.validar(token));
    }

    @Test
    void rechazaTokenExpirado() {
        JwtService jwt = servicio(SECRET, Duration.ofSeconds(-120));
        String token = jwt.generarToken(new Usuario("x_user", "X", "hash", Rol.DIRECCION)).token();

        assertThrows(ExpiredJwtException.class, () -> jwt.validar(token));
    }

    @Test
    void exigeClaveDeAlMenos256Bits() {
        assertThrows(IllegalStateException.class, () -> servicio("Y29ydGE=", Duration.ofMinutes(5)));
        assertThrows(IllegalStateException.class, () -> servicio("", Duration.ofMinutes(5)));
    }
}
