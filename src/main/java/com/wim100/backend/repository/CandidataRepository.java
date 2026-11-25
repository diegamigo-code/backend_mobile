package com.wim100.backend.repository;

import com.wim100.backend.model.Candidata;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidataRepository extends JpaRepository<Candidata, Long> {

    List<Candidata> findAllByOrderByIdDesc();

    List<Candidata> findByNombreContainingIgnoreCaseOrAreaContainingIgnoreCaseOrderByIdDesc(String nombre, String area);

    boolean existsByCorreoIgnoreCase(String correo);
}
