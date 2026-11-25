package com.wim100.backend.security;

import com.wim100.backend.config.AppProperties;
import com.wim100.backend.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    public static final String CLAIM_ROL = "rol";
    public static final String CLAIM_NOMBRE = "nombre";

    private final SecretKey key;
    private final Duration expiration;
    private final String issuer;

    public JwtService(AppProperties props) {
        AppProperties.Jwt jwt = props.jwt();
        if (jwt == null || jwt.secret() == null || jwt.secret().isBlank()) {
            throw new IllegalStateException(
                    "Falta app.jwt.secret (variable JWT_SECRET). Genera una con: openssl rand -base64 32");
        }
        byte[] bytes;
        try {
            bytes = Decoders.BASE64.decode(jwt.secret().trim());
        } catch (RuntimeException e) {
            throw new IllegalStateException("JWT_SECRET debe estar codificada en Base64", e);
        }
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET debe tener al menos 256 bits (32 bytes) antes de codificar en Base64");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.expiration = jwt.expiration() != null ? jwt.expiration() : Duration.ofHours(8);
        this.issuer = jwt.issuer() != null && !jwt.issuer().isBlank() ? jwt.issuer() : "wim100-backend";
    }

    public TokenEmitido generarToken(Usuario usuario) {
        Instant ahora = Instant.now();
        Instant expira = ahora.plus(expiration);
        String token = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(issuer)
                .subject(usuario.getUsername())
                .claim(CLAIM_ROL, usuario.getRol().name())
                .claim(CLAIM_NOMBRE, usuario.getNombreCompleto())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(expira))
                .signWith(key)
                .compact();
        return new TokenEmitido(token, expira);
    }

    public Claims validar(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public record TokenEmitido(String token, Instant expiraEn) {
    }
}
