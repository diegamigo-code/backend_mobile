package com.wim100.backend.dto;

import com.wim100.backend.model.Candidata;
import com.wim100.backend.model.EstadoCandidatura;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class CandidataDtos {

    private CandidataDtos() {
    }

    public record CrearCandidataRequest(
            @NotBlank(message = "El nombre es obligatorio")
            @Size(min = 3, max = 120, message = "El nombre debe tener al menos 3 caracteres")
            String nombre,

            @NotBlank(message = "El correo es obligatorio")
            @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Formato de correo inválido")
            @Size(max = 150, message = "El correo es demasiado largo")
            String correo,

            @NotBlank(message = "El teléfono es obligatorio")
            @Pattern(regexp = "^(\\+?56)?\\s?9\\s?\\d{4}\\s?\\d{4}$", message = "Formato chileno inválido (+56 9 XXXX XXXX)")
            String telefono,

            @NotBlank(message = "El área es obligatoria")
            String area,

            @NotNull(message = "Debe ingresar un número entero")
            @Min(value = 0, message = "Los años deben estar entre 0 y 50")
            @Max(value = 50, message = "Los años deben estar entre 0 y 50")
            Integer anosExperiencia,

            @NotBlank(message = "El resumen es obligatorio")
            @Size(min = 30, max = 500, message = "El resumen debe tener entre 30 y 500 caracteres")
            String resumen,

            @NotBlank(message = "El perfil de LinkedIn es obligatorio")
            @Pattern(regexp = "^https?://.+", message = "El enlace debe comenzar con http:// o https://")
            @Size(max = 300, message = "El enlace es demasiado largo")
            String enlacePerfil,

            @NotBlank(message = "Debe adjuntar un archivo CV")
            @Size(max = 300, message = "La ruta del CV es demasiado larga")
            String uriCv
    ) {
    }

    public record CambiarEstadoRequest(
            @NotNull(message = "El estado es obligatorio") EstadoCandidatura estado
    ) {
    }

    public record CandidataResponse(
            Long id,
            String nombre,
            String correo,
            String telefono,
            String area,
            int anosExperiencia,
            String resumen,
            String enlacePerfil,
            String uriCv,
            EstadoCandidatura estado,
            String estadoEtiqueta,
            String registradaPor,
            Instant fechaRegistro
    ) {
        public static CandidataResponse from(Candidata c) {
            return new CandidataResponse(c.getId(), c.getNombre(), c.getCorreo(), c.getTelefono(), c.getArea(),
                    c.getAnosExperiencia(), c.getResumen(), c.getEnlacePerfil(), c.getUriCv(), c.getEstado(),
                    c.getEstado().getEtiqueta(), c.getRegistradaPor(), c.getFechaRegistro());
        }
    }
}
