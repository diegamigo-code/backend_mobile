package cl.duoc.wim100.repository;

import cl.duoc.wim100.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventoRepository extends JpaRepository<Evento, Long> {
    List<Evento> findByCandidataIdIsNotNullOrderByIdDesc();
}
