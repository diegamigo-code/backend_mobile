package cl.duoc.wim100.repository;

import cl.duoc.wim100.model.Postulante;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface PostulanteRepository extends JpaRepository<Postulante, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Postulante p where p.id = :id")
    Optional<Postulante> bloquearPorId(@Param("id") Long id);

    List<Postulante> findAllByOrderByFechaRegistroDesc();
    boolean existsByCorreoIgnoreCase(String correo);
    boolean existsByCorreoIgnoreCaseAndIdNot(String correo, Long id);
}
