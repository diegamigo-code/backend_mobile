package cl.duoc.wim100.service;

import cl.duoc.wim100.dto.PostulanteRequest;
import cl.duoc.wim100.exception.CorreoDuplicadoException;
import cl.duoc.wim100.exception.RecursoNoEncontradoException;
import cl.duoc.wim100.model.EstadoPostulante;
import cl.duoc.wim100.model.Postulante;
import cl.duoc.wim100.repository.PostulanteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import cl.duoc.wim100.dto.LoginResponse;
import cl.duoc.wim100.model.Evento;
import cl.duoc.wim100.repository.EventoRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostulanteService {

    private final PostulanteRepository repository;

    private final EventoRepository eventos;

    public PostulanteService(PostulanteRepository repository, EventoRepository eventos) {
        this.repository = repository;
        this.eventos = eventos;
    }

    public List<Postulante> listar() {
        return repository.findAllByOrderByFechaRegistroDesc();
    }

    public Postulante obtener(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el postulante " + id));
    }

    @Transactional
    public Postulante crear(LoginResponse cuenta, PostulanteRequest req) {
        // Regla de negocio: no se registra dos veces el mismo correo
        if (repository.existsByCorreoIgnoreCase(req.correo().trim())) {
            throw new CorreoDuplicadoException("Ya existe una candidatura con este correo");
        }
        Postulante p = new Postulante();
        copiarDatos(req, p);
        p.setEstado(EstadoPostulante.POSTULADA);
        p.setFechaRegistro(System.currentTimeMillis());
        p.setRegistradaPor(cuenta.usuario());
        repository.save(p);
        eventos.save(new Evento(p.getId(), cuenta, "Registro de candidatura", p.getNombre()));
        return p;
    }

    @Transactional
    public Postulante actualizar(LoginResponse cuenta, Long id, PostulanteRequest req) {
        Postulante p = repository.bloquearPorId(id).orElseThrow(() -> new RecursoNoEncontradoException("La candidata ya no existe"));
        if (repository.existsByCorreoIgnoreCaseAndIdNot(req.correo().trim(), id)) {
            throw new CorreoDuplicadoException("Ya existe una candidatura con este correo");
        }
        copiarDatos(req, p); // no cambia estado, fecha ni quién la registró
        eventos.save(new Evento(id, cuenta, "Edición de candidatura", p.getNombre()));
        return p;
    }

    @Transactional
    public Postulante cambiarEstado(LoginResponse cuenta, Long id, EstadoPostulante estado) {
        Postulante p = repository.bloquearPorId(id).orElseThrow(() -> new RecursoNoEncontradoException("La candidata ya no existe"));
        p.setEstado(estado);
        eventos.save(new Evento(id, cuenta, "Cambio de estado", estado.name()));
        return p;
    }

    public void eliminar(Long id) {
        repository.delete(obtener(id)); // lanza 404 si no existe
    }

    private void copiarDatos(PostulanteRequest r, Postulante p) {
        p.setNombre(r.nombre().trim().replaceAll("\\s+", " "));
        p.setCorreo(r.correo().trim().toLowerCase());
        p.setTelefono(r.telefono().trim());
        p.setArea(r.area().trim());
        p.setAniosExperiencia(r.aniosExperiencia());
        p.setResumenExperiencia(r.resumenExperiencia().trim());
        p.setLinkedinUrl(r.linkedinUrl().trim());
        p.setCvNombre(r.cvNombre());
    }
}
