package cl.duoc.wim100.repository;

import cl.duoc.wim100.model.Evaluacion;
import org.springframework.data.jpa.repository.*;
import java.util.List;

public interface EvaluacionRepository extends JpaRepository<Evaluacion, Long> {
    boolean existsByPostulanteIdAndEvaluadora(Long id, String evaluadora);

    long countByPostulanteId(Long postulanteId);

    interface Resultado {
        Long getCandidataId();
        String getCandidataNombre();
        String getArea();
        cl.duoc.wim100.model.EstadoPostulante getEstado();
        long getCantidadEvaluaciones();
        double getPromedioPuntaje();
    }

    @Query("""
        select p.id as candidataId, p.nombre as candidataNombre, p.area as area, p.estado as estado,
               count(e.id) as cantidadEvaluaciones, coalesce(avg(e.puntajeTotal), 0.0) as promedioPuntaje
        from Postulante p left join Evaluacion e on e.postulante = p
        group by p.id, p.nombre, p.area, p.estado order by coalesce(avg(e.puntajeTotal), 0.0) desc, p.id
        """)
    List<Resultado> ranking();
}
