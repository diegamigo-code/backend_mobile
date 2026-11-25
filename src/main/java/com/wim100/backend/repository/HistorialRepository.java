package com.wim100.backend.repository;

import com.wim100.backend.model.RegistroHistorial;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistorialRepository extends JpaRepository<RegistroHistorial, Long> {

    List<RegistroHistorial> findAllByOrderByFechaDesc();

    List<RegistroHistorial> findByCandidataIdOrderByFechaDesc(Long candidataId);
}
