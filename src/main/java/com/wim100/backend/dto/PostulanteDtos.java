package com.wim100.backend.dto;

import com.wim100.backend.model.Candidata;
import com.wim100.backend.service.CandidataService.DatosCandidata;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class PostulanteDtos {

    private PostulanteDtos() {
    }

    public record PostulanteRequest(
            Long id,

            @NotBlank(message = "El nombre es obligatorio")
            @Size(min = 3, max = 120, message = "El nombre debe tener entre 3 y 120 caracteres")
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

            @NotNull(message = "Indica los años de experiencia")
            @Min(value = 0, message = "Los años deben estar entre 0 y 50")
            @Max(value = 50, message = "Los años deben estar entre 0 y 50")
            Integer aniosExperiencia,

            @NotBlank(message = "El resumen es obligatorio")
            @Size(min = 30, max = 500, message = "El resumen debe tener entre 30 y 500 caracteres")
            String resumenExperiencia,

            @NotBlank(message = "El perfil profesional es obligatorio")
            @Pattern(regexp = "^https?://.+", message = "El enlace debe comenzar con http:// o https://")
            @Size(max = 300, message = "El enlace es demasiado largo")
            String linkedinUrl,

            @Size(max = 300, message = "El nombre del CV es demasiado largo")
            String cvNombre,

            String estado,
            Long fechaRegistro,
            String registradaPor
    ) {
        public DatosCandidata aDatos() {
            return new DatosCandidata(nombre, correo, telefono, area, aniosExperiencia, resumenExperiencia,
                    linkedinUrl, cvNombre);
        }
    }

    public record PostulanteResponse(
            Long id,
            String nombre,
            String correo,
            String telefono,
            String area,
            int aniosExperiencia,
            String resumenExperiencia,
            String linkedinUrl,
            String cvNombre,
            String estado,
            long fechaRegistro,
            String registradaPor
    ) {
        public static PostulanteResponse from(Candidata c) {
            String cv = c.getUriCv() == null || c.getUriCv().isBlank() ? null : c.getUriCv();
            return new PostulanteResponse(c.getId(), c.getNombre(), c.getCorreo(), c.getTelefono(), c.getArea(),
                    c.getAnosExperiencia(), c.getResumen(), c.getEnlacePerfil(), cv, c.getEstado().name(),
                    c.getFechaRegistro().toEpochMilli(), c.getRegistradaPor());
        }
    }
}
