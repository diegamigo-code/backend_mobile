package com.wim100.backend.repository;

import com.wim100.backend.model.Evaluacion;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluacionRepository extends JpaRepository<Evaluacion, Long> {

    boolean existsByCandidata_IdAndEvaluadora_Id(Long candidataId, Long evaluadoraId);

    @EntityGraph(attributePaths = {"evaluadora", "puntajes", "puntajes.criterio"})
    List<Evaluacion> findByCandidata_IdOrderByFechaCreacionAsc(Long candidataId);

    @EntityGraph(attributePaths = {"candidata"})
    List<Evaluacion> findAllBy();
}
