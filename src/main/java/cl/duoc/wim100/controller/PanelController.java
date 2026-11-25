package cl.duoc.wim100.controller;

import cl.duoc.wim100.dto.*;
import cl.duoc.wim100.service.PanelService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api")
public class PanelController {
    private final PanelService panel;

    public PanelController(PanelService panel) {
        this.panel = panel;
    }

    @GetMapping("/panel")
    public PanelService.Panel leer() { return panel.leer(); }

    @PostMapping("/postulantes/{id}/evaluaciones")
    @ResponseStatus(HttpStatus.CREATED)
    public long evaluar(@PathVariable Long id, @SessionAttribute LoginResponse cuenta,
            @Valid @RequestBody EvaluacionRequest req) {
        return panel.evaluar(id, cuenta, req);
    }
}
