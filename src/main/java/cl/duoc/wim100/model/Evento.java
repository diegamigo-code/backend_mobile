package cl.duoc.wim100.model;

import cl.duoc.wim100.dto.LoginResponse;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Evento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long candidataId;
    private String usuarioNombre;
    private String usuarioRol;
    private String accion;
    private String detalle;
    private long fecha;

    public Evento(Long candidataId, LoginResponse cuenta, String accion, String detalle) {
        this.candidataId = candidataId;
        this.usuarioNombre = cuenta.usuario();
        this.usuarioRol = cuenta.rol().name();
        this.accion = accion;
        this.detalle = detalle;
        this.fecha = System.currentTimeMillis();
    }
}
