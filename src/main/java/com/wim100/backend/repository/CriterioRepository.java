package com.wim100.backend.repository;

import com.wim100.backend.model.Criterio;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CriterioRepository extends JpaRepository<Criterio, Long> {

    List<Criterio> findAllByOrderByOrdenAsc();
}
