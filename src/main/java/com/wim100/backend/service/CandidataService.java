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
import com.wim100.backend.model.Rol;
import com.wim100.backend.repository.CandidataRepository;
import com.wim100.backend.repository.EvaluacionRepository;
import com.wim100.backend.security.UsuarioPrincipal;
import java.util.List;
import java.util.Locale;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CandidataService {

    public record DatosCandidata(String nombre, String correo, String telefono, String area, int anosExperiencia,
                                 String resumen, String enlacePerfil, String uriCv) {
    }

    private final CandidataRepository candidataRepository;
    private final EvaluacionRepository evaluacionRepository;
    private final HistorialService historialService;
    private final List<String> dominiosPermitidos;

    public CandidataService(CandidataRepository candidataRepository, EvaluacionRepository evaluacionRepository,
                            HistorialService historialService, AppProperties props) {
        this.candidataRepository = candidataRepository;
        this.evaluacionRepository = evaluacionRepository;
        this.historialService = historialService;
        this.dominiosPermitidos = props.candidatas() != null && props.candidatas().dominiosPermitidos() != null
                ? props.candidatas().dominiosPermitidos().stream().map(d -> d.trim().toLowerCase(Locale.ROOT)).toList()
                : List.of();
    }

    @Transactional(readOnly = true)
    public List<Candidata> buscarTodas(String q) {
        return (q == null || q.isBlank())
                ? candidataRepository.findAllByOrderByIdDesc()
                : candidataRepository.findByNombreContainingIgnoreCaseOrAreaContainingIgnoreCaseOrderByIdDesc(q.trim(), q.trim());
    }

    @Transactional(readOnly = true)
    public List<CandidataResponse> listar(String q) {
        return buscarTodas(q).stream().map(CandidataResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public CandidataResponse obtener(Long id) {
        return CandidataResponse.from(buscar(id));
    }

    @Transactional
    public CandidataResponse registrar(CrearCandidataRequest req, UsuarioPrincipal usuario) {
        return CandidataResponse.from(crear(new DatosCandidata(req.nombre(), req.correo(), req.telefono(), req.area(),
                req.anosExperiencia(), req.resumen(), req.enlacePerfil(), req.uriCv()), usuario));
    }

    @Transactional
    public Candidata crear(DatosCandidata datos, UsuarioPrincipal usuario) {
        DatosCandidata d = normalizarYValidar(datos);
        if (candidataRepository.existsByCorreoIgnoreCase(d.correo())) {
            historialService.registrar(null, usuario.getNombreCompleto(), usuario.getRol().getEtiqueta(),
                    Acciones.REGISTRO_CANDIDATA, "Intento fallido de registro. Correo duplicado: " + d.correo());
            throw new ConflictoException("El correo ya se encuentra registrado");
        }

        Candidata nueva = candidataRepository.save(new Candidata(d.nombre(), d.correo(), d.telefono(), d.area(),
                d.anosExperiencia(), d.resumen(), d.enlacePerfil(), d.uriCv(), usuario.getNombreCompleto()));

        historialService.registrar(nueva.getId(), usuario.getNombreCompleto(), usuario.getRol().getEtiqueta(),
                Acciones.REGISTRO_CANDIDATA, "Candidata registrada exitosamente: " + nueva.getNombre());
        return nueva;
    }

    @Transactional
    public Candidata actualizar(Long id, DatosCandidata datos, UsuarioPrincipal usuario) {
        Candidata c = buscar(id);
        DatosCandidata d = normalizarYValidar(datos);
        if (candidataRepository.existsByCorreoIgnoreCaseAndIdNot(d.correo(), id)) {
            throw new ConflictoException("El correo ya se encuentra registrado");
        }
        c.actualizar(d.nombre(), d.correo(), d.telefono(), d.area(), d.anosExperiencia(), d.resumen(),
                d.enlacePerfil(), d.uriCv());
        historialService.registrar(c.getId(), usuario.getNombreCompleto(), usuario.getRol().getEtiqueta(),
                Acciones.EDICION_CANDIDATA, "Se editaron los datos de " + c.getNombre());
        return c;
    }

    @Transactional
    public void eliminar(Long id, UsuarioPrincipal usuario) {
        Candidata c = buscar(id);
        String nombre = c.getNombre();
        evaluacionRepository.deleteByCandidata_Id(id);
        candidataRepository.delete(c);
        historialService.registrar(null, usuario.getNombreCompleto(), usuario.getRol().getEtiqueta(),
                Acciones.ELIMINACION_CANDIDATA, "Se eliminó a " + nombre + " (ID " + id + ")");
    }

    @Transactional
    public CandidataResponse cambiarEstado(Long id, EstadoCandidatura nuevoEstado, UsuarioPrincipal usuario) {
        return CandidataResponse.from(actualizarEstado(id, nuevoEstado, usuario));
    }

    @Transactional
    public Candidata actualizarEstado(Long id, EstadoCandidatura nuevoEstado, UsuarioPrincipal usuario) {
        if (nuevoEstado.isResuelta() && usuario.getRol() != Rol.DIRECCION) {
            throw new AccessDeniedException("Solo Dirección puede resolver una candidatura");
        }
        Candidata c = buscar(id);
        EstadoCandidatura anterior = c.getEstado();
        if (anterior == nuevoEstado) {
            return c;
        }
        c.setEstado(nuevoEstado);
        historialService.registrar(c.getId(), usuario.getNombreCompleto(), usuario.getRol().getEtiqueta(),
                Acciones.CAMBIO_ESTADO,
                "Estado actualizado de " + anterior.getEtiqueta() + " a: " + nuevoEstado.getEtiqueta());
        return c;
    }

    public Candidata buscar(Long id) {
        return candidataRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Candidata no encontrada: " + id));
    }

    private DatosCandidata normalizarYValidar(DatosCandidata d) {
        String correo = d.correo().trim().toLowerCase(Locale.ROOT);
        String dominio = correo.substring(correo.indexOf('@') + 1);
        if (!dominiosPermitidos.isEmpty() && !dominiosPermitidos.contains(dominio)) {
            throw new ValidacionException("correo",
                    "Dominio no permitido (use " + String.join(", ", dominiosPermitidos) + ")");
        }
        String area = d.area().trim();
        if (!Candidata.AREAS.contains(area)) {
            throw new ValidacionException("area", "Área no válida. Opciones: " + String.join(", ", Candidata.AREAS));
        }
        String cv = d.uriCv() == null ? "" : d.uriCv().trim();
        return new DatosCandidata(d.nombre().trim().replaceAll("\\s+", " "), correo, d.telefono().trim(), area,
                d.anosExperiencia(), d.resumen().trim(), d.enlacePerfil().trim(), cv);
    }
}
