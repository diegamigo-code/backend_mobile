package cl.duoc.wim100.dto;

import jakarta.validation.constraints.*;

public record PostulanteRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 3, max = 60, message = "El nombre debe tener entre 3 y 60 caracteres")
        String nombre,

        @NotBlank(message = "El correo es obligatorio")
        @Size(max = 120)
        @Email(message = "Formato de correo inválido")
        @Pattern(regexp = "(?i)^[^@]+@(ejemplo\\.cl|example\\.com|test\\.cl)$", message = "Dominio de correo no permitido")
        String correo,

        @NotBlank(message = "El teléfono es obligatorio")
        @Size(max = 20)
        @Pattern(regexp = "^(\\+?56)?\\s?9\\s?\\d{4}\\s?\\d{4}$", message = "Formato chileno inválido")
        String telefono,

        @NotBlank(message = "El área es obligatoria")
        @Size(max = 60)
        String area,

        @NotNull(message = "Indica los años de experiencia")
        @Min(value = 0, message = "No puede ser negativo")
        @Max(value = 50, message = "Máximo 50 años")
        Integer aniosExperiencia,

        @NotBlank(message = "El resumen es obligatorio")
        @Size(min = 30, max = 500, message = "El resumen debe tener entre 30 y 500 caracteres")
        String resumenExperiencia,

        @NotBlank(message = "El enlace al perfil es obligatorio")
        @Size(max = 200)
        @Pattern(regexp = "^https?://.+", message = "Debe comenzar con http:// o https://")
        String linkedinUrl,

        @NotBlank(message = "Indica el CV de la candidata")
        @Size(max = 255)
        String cvNombre
) {}
