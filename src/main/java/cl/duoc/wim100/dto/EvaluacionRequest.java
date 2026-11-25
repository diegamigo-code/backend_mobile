package cl.duoc.wim100.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record EvaluacionRequest(@NotBlank @Size(min=20, max=400) String observacionGeneral,
        @NotNull @Size(min=5, max=5) List<@NotNull @Valid Nota> notas) {
    public record Nota(@Min(1) @Max(5) int criterioId, @Min(1) @Max(5) int puntaje,
                       @NotNull String observacion) {}
}
