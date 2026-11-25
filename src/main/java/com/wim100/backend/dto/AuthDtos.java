package com.wim100.backend.dto;

import com.wim100.backend.model.Rol;
import com.wim100.backend.model.Usuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(
            @NotBlank(message = "El nombre de usuario es obligatorio") String username,
            @NotBlank(message = "La contraseña es obligatoria") String password
    ) {
    }

    public record LoginResponse(
            String token,
            String tipo,
            Instant expiraEn,
            UsuarioResponse usuario
    ) {
    }

    public record UsuarioResponse(
            Long id,
            String username,
            String nombreCompleto,
            Rol rol,
            String rolEtiqueta,
            boolean activo
    ) {
        public static UsuarioResponse from(Usuario u) {
            return new UsuarioResponse(u.getId(), u.getUsername(), u.getNombreCompleto(), u.getRol(),
                    u.getRol().getEtiqueta(), u.isActivo());
        }
    }

    public record CrearUsuarioRequest(
            @NotBlank(message = "El nombre de usuario es obligatorio")
            @Pattern(regexp = "^[a-z0-9._-]{3,50}$",
                    message = "Use 3 a 50 caracteres en minúscula: letras, números, punto, guion o guion bajo")
            String username,

            @NotBlank(message = "El nombre completo es obligatorio")
            @Size(min = 3, max = 120, message = "El nombre debe tener entre 3 y 120 caracteres")
            String nombreCompleto,

            @NotBlank(message = "La contraseña es obligatoria")
            @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
            String password,

            @NotNull(message = "El rol es obligatorio")
            Rol rol
    ) {
    }
}
