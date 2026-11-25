package com.wim100.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class RestSecurityHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        Object detalle = request.getAttribute(JwtAuthenticationFilter.ATTR_JWT_ERROR);
        String mensaje = detalle != null ? detalle.toString() : "Se requiere autenticación";
        response.setHeader("WWW-Authenticate", "Bearer");
        escribir(response, request, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", mensaje);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        escribir(response, request, HttpServletResponse.SC_FORBIDDEN, "Forbidden",
                "No tienes permisos para realizar esta acción");
    }

    private void escribir(HttpServletResponse response, HttpServletRequest request, int status, String error,
                          String mensaje) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String json = "{\"timestamp\":\"" + Instant.now() + "\","
                + "\"status\":" + status + ","
                + "\"error\":\"" + error + "\","
                + "\"mensaje\":\"" + escapar(mensaje) + "\","
                + "\"path\":\"" + escapar(request.getRequestURI()) + "\","
                + "\"campos\":{}}";
        response.getWriter().write(json);
    }

    private static String escapar(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
