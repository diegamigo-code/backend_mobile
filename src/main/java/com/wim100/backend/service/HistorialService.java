package com.wim100.backend.service;

import com.wim100.backend.dto.EvaluacionDtos.HistorialResponse;
import com.wim100.backend.model.RegistroHistorial;
import com.wim100.backend.repository.HistorialRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HistorialService {

    private final HistorialRepository historialRepository;

    public HistorialService(HistorialRepository historialRepository) {
        this.historialRepository = historialRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(Long candidataId, String usuarioNombre, String usuarioRol, String accion, String detalle) {
        historialRepository.save(new RegistroHistorial(candidataId, usuarioNombre, usuarioRol, accion, detalle));
    }

    @Transactional(readOnly = true)
    public List<HistorialResponse> listarTodo() {
        return historialRepository.findAllByOrderByFechaDesc().stream().map(HistorialResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<HistorialResponse> listarPorCandidata(Long candidataId) {
        return historialRepository.findByCandidataIdOrderByFechaDesc(candidataId).stream()
                .map(HistorialResponse::from).toList();
    }
}
