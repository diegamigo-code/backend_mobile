package com.wim100.backend.service;

import com.wim100.backend.config.AppProperties;
import com.wim100.backend.dto.CandidataDtos.CandidataResponse;
import com.wim100.backend.dto.CandidataDtos.CrearCandidataRequest;
import com.wim100.backend.exception.ConflictoException;
import com.wim100.backend.exception.RecursoNoEncontradoException;
import com.wim100.backend.exception.ValidacionException;
import com.wim100.backend.model.Candidata;
import com.wim100.backend.model.EstadoCandidatura;
import com.wim100.backend.model.RegistroHistorial.Acciones;
import com.wim100.backend.repository.CandidataRepository;
import com.wim100.backend.security.UsuarioPrincipal;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CandidataService {

    private final CandidataRepository candidataRepository;
    private final HistorialService historialService;
    private final List<String> dominiosPermitidos;

    public CandidataService(CandidataRepository candidataRepository, HistorialService historialService,
                            AppProperties props) {
        this.candidataRepository = candidataRepository;
        this.historialService = historialService;
        this.dominiosPermitidos = props.candidatas() != null && props.candidatas().dominiosPermitidos() != null
                ? props.candidatas().dominiosPermitidos().stream().map(d -> d.trim().toLowerCase(Locale.ROOT)).toList()
                : List.of();
    }

    @Transactional(readOnly = true)
    public List<CandidataResponse> listar(String q) {
        List<Candidata> resultado = (q == null || q.isBlank())
                ? candidataRepository.findAllByOrderByIdDesc()
                : candidataRepository.findByNombreContainingIgnoreCaseOrAreaContainingIgnoreCaseOrderByIdDesc(q.trim(), q.trim());
        return resultado.stream().map(CandidataResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public CandidataResponse obtener(Long id) {
        return CandidataResponse.from(buscar(id));
    }

    @Transactional
    public CandidataResponse registrar(CrearCandidataRequest req, UsuarioPrincipal usuario) {
        String correo = req.correo().trim().toLowerCase(Locale.ROOT);
        String dominio = correo.substring(correo.indexOf('@') + 1);
        if (!dominiosPermitidos.isEmpty() && !dominiosPermitidos.contains(dominio)) {
            throw new ValidacionException("correo",
                    "Dominio no permitido (use " + String.join(", ", dominiosPermitidos) + ")");
        }
        String area = req.area().trim();
        if (!Candidata.AREAS.contains(area)) {
            throw new ValidacionException("area", "Área no válida. Opciones: " + String.join(", ", Candidata.AREAS));
        }
        if (candidataRepository.existsByCorreoIgnoreCase(correo)) {
            historialService.registrar(null, usuario.getNombreCompleto(), usuario.getRol().getEtiqueta(),
                    Acciones.REGISTRO_CANDIDATA, "Intento fallido de registro. Correo duplicado: " + correo);
            throw new ConflictoException("El correo ya se encuentra registrado");
        }

        Candidata nueva = candidataRepository.save(new Candidata(
                req.nombre().trim(), correo, req.telefono().trim(), area, req.anosExperiencia(),
                req.resumen().trim(), req.enlacePerfil().trim(), req.uriCv().trim(), usuario.getNombreCompleto()));

        historialService.registrar(nueva.getId(), usuario.getNombreCompleto(), usuario.getRol().getEtiqueta(),
                Acciones.REGISTRO_CANDIDATA, "Candidata registrada exitosamente: " + nueva.getNombre());
        return CandidataResponse.from(nueva);
    }

    @Transactional
    public CandidataResponse cambiarEstado(Long id, EstadoCandidatura nuevoEstado, UsuarioPrincipal usuario) {
        Candidata c = buscar(id);
        EstadoCandidatura anterior = c.getEstado();
        c.setEstado(nuevoEstado);
        historialService.registrar(c.getId(), usuario.getNombreCompleto(), usuario.getRol().getEtiqueta(),
                Acciones.CAMBIO_ESTADO,
                "Estado actualizado de " + anterior.getEtiqueta() + " a: " + nuevoEstado.getEtiqueta());
        return CandidataResponse.from(c);
    }

    public Candidata buscar(Long id) {
        return candidataRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Candidata no encontrada: " + id));
    }
}
