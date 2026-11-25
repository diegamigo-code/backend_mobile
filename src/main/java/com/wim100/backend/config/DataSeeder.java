package com.wim100.backend.config;

import com.wim100.backend.model.Candidata;
import com.wim100.backend.model.Criterio;
import com.wim100.backend.model.EstadoCandidatura;
import com.wim100.backend.model.Evaluacion;
import com.wim100.backend.model.RegistroHistorial;
import com.wim100.backend.model.RegistroHistorial.Acciones;
import com.wim100.backend.model.Rol;
import com.wim100.backend.model.Usuario;
import com.wim100.backend.repository.CandidataRepository;
import com.wim100.backend.repository.CriterioRepository;
import com.wim100.backend.repository.EvaluacionRepository;
import com.wim100.backend.repository.HistorialRepository;
import com.wim100.backend.repository.UsuarioRepository;
import com.wim100.backend.service.CalculadoraPuntaje;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final AppProperties props;
    private final UsuarioRepository usuarioRepository;
    private final CriterioRepository criterioRepository;
    private final CandidataRepository candidataRepository;
    private final EvaluacionRepository evaluacionRepository;
    private final HistorialRepository historialRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(AppProperties props, UsuarioRepository usuarioRepository, CriterioRepository criterioRepository,
                      CandidataRepository candidataRepository, EvaluacionRepository evaluacionRepository,
                      HistorialRepository historialRepository, PasswordEncoder passwordEncoder) {
        this.props = props;
        this.usuarioRepository = usuarioRepository;
        this.criterioRepository = criterioRepository;
        this.candidataRepository = candidataRepository;
        this.evaluacionRepository = evaluacionRepository;
        this.historialRepository = historialRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (props.seed() == null || !props.seed().enabled()) {
            return;
        }

        if (usuarioRepository.count() == 0) {
            String pass = props.seed().defaultPassword();
            if (pass == null || pass.length() < 8) {
                throw new IllegalStateException("app.seed.default-password debe tener al menos 8 caracteres");
            }
            String hash = passwordEncoder.encode(pass);
            usuarioRepository.saveAll(List.of(
                    new Usuario("andrea_torres", "Andrea Torres", hash, Rol.EVALUADORA),
                    new Usuario("maria_fernandez", "María Fernández", hash, Rol.EVALUADORA),
                    new Usuario("direccion", "Equipo Dirección", hash, Rol.DIRECCION)
            ));
            log.info("Usuarias de ejemplo creadas: andrea_torres, maria_fernandez (Evaluadora), direccion (Dirección)");
        }

        if (criterioRepository.count() == 0) {
            criterioRepository.saveAll(List.of(
                    new Criterio("Experiencia Técnica y Trayectoria",
                            "Evaluación de los años de experiencia y proyectos previos en áreas tecnológicas.", 25, 1),
                    new Criterio("Conocimientos en Ciberseguridad / TI",
                            "Dominio de herramientas, certificaciones y fundamentos técnicos del área.", 20, 2),
                    new Criterio("Habilidades Blandas y Liderazgo",
                            "Capacidad de trabajo en equipo, comunicación efectiva y liderazgo de proyectos.", 20, 3),
                    new Criterio("Potencial de Crecimiento e Impacto",
                            "Alineación de la candidata con los objetivos y proyección dentro de DYM Cyber.", 20, 4),
                    new Criterio("Calidad de la Postulación y CV",
                            "Claridad, estructura del perfil de LinkedIn, resumen profesional y CV adjunto.", 15, 5)
            ));
        }

        if (candidataRepository.count() == 0) {
            sembrarCandidatasYEvaluaciones();
        }
    }

    private void sembrarCandidatasYEvaluaciones() {
        Candidata camila = candidata("Camila Morales", "camila.morales@example.com", "+56 9 1234 5678", "Forense y respuesta a incidentes", 6,
                "Especialista en respuesta a incidentes y análisis forense digital. Certificada GCIH y CISSP. Lideró equipos de SOC en la industria bancaria.",
                "https://linkedin.com/in/camila-morales-cyber", "cv_camila_morales.pdf", EstadoCandidatura.EN_EVALUACION);
        Candidata valentina = candidata("Valentina Silva", "valentina.silva@example.com", "+56 9 8765 4321", "Seguridad ofensiva (Red Team)", 4,
                "Pentester de aplicaciones web y móviles con certificación OSCP. Ha reportado vulnerabilidades en programas de bug bounty.",
                "https://linkedin.com/in/valentina-silva-dev", "cv_valentina_silva.pdf", EstadoCandidatura.EVALUADA);
        Candidata sofia = candidata("Sofía Araya", "sofia.araya@example.com", "+56 9 5555 4433", "Seguridad defensiva (Blue Team)", 5,
                "Analista de detección y threat hunting. Diseñó casos de uso SIEM y modelos de detección de anomalías con machine learning.",
                "https://linkedin.com/in/sofia-araya-data", "cv_sofia_araya.pdf", EstadoCandidatura.POSTULADA);
        Candidata mariana = candidata("Mariana Fuentes", "mariana.fuentes@example.com", "+56 9 3344 5566", "Ciberseguridad industrial (OT)", 8,
                "Arquitecta de seguridad OT para minería y energía. Experta en IEC 62443, segmentación de redes industriales y SCADA.",
                "https://linkedin.com/in/mariana-fuentes-cloud", "cv_mariana_fuentes.pdf", EstadoCandidatura.SELECCIONADA);
        Candidata isadora = candidata("Isadora Rojas", "isadora.rojas@example.com", "+56 9 7788 9900", "Gobierno, riesgo y cumplimiento", 7,
                "Consultora GRC certificada ISO 27001 Lead Auditor. Lideró la implementación de SGSI y la gestión de riesgos en banca y retail.",
                "https://linkedin.com/in/isadora-rojas-pm", "cv_isadora_rojas.pdf", EstadoCandidatura.POSTULADA);
        Candidata gabriela = candidata("Gabriela Castro", "gabriela.castro@example.com", "+56 9 6677 8899", "Investigación y academia", 3,
                "Investigadora en Criptografía Avanzada. MSc en Ciencias de la Computación con publicaciones en conferencias internacionales.",
                "https://linkedin.com/in/gabriela-castro-research", "cv_gabriela_castro.pdf", EstadoCandidatura.EN_EVALUACION);

        List<Candidata> todas = candidataRepository.saveAll(List.of(camila, valentina, sofia, mariana, isadora, gabriela));

        List<RegistroHistorial> historial = new ArrayList<>();
        for (Candidata c : todas) {
            historial.add(new RegistroHistorial(c.getId(), "Sistema", "Sistema", Acciones.REGISTRO_CANDIDATA,
                    "Candidata registrada inicialmente: " + c.getNombre()));
        }
        historialRepository.saveAll(historial);

        Usuario andrea = usuarioRepository.findByUsername("andrea_torres").orElse(null);
        Usuario maria = usuarioRepository.findByUsername("maria_fernandez").orElse(null);
        if (andrea == null || maria == null) {
            log.warn("No se sembraron evaluaciones: faltan las usuarias evaluadoras de ejemplo");
            return;
        }
        List<Criterio> criterios = criterioRepository.findAllByOrderByOrdenAsc();

        evaluar(camila, andrea, criterios, new int[]{5, 5, 4, 5, 5}, new String[]{
                "Sólida trayectoria en pentesting y respuesta a incidentes.", "Certificaciones vigentes CEH y CISSP.",
                "Buena comunicación y trabajo en equipo.", "Gran potencial de crecimiento en la organización.",
                "CV claro y perfil de LinkedIn actualizado."},
                "Excelente perfil técnico en Ciberseguridad con sólida experiencia en SOC.");
        evaluar(valentina, maria, criterios, new int[]{5, 4, 5, 5, 5}, new String[]{
                "Experiencia sólida en desarrollo nativo Android.", "Buen manejo de seguridad a nivel de software.",
                "Excelentes habilidades comunicativas.", "Proactiva y orientada a resultados.", "Postulación impecable."},
                "Perfil muy sólido en Desarrollo Mobile y Clean Architecture.");
        evaluar(mariana, maria, criterios, new int[]{5, 5, 5, 5, 5}, new String[]{
                "8 años de trayectoria comprobada en Cloud/DevOps.", "Dominio avanzado de TI e infraestructura segura.",
                "Habilidades de liderazgo bien consolidadas.", "Alineación total con los objetivos estratégicos.",
                "CV detallado con evidencias concretas."},
                "Excelente candidata para el rol de Infraestructura Cloud. Cumple todos los requisitos.");
        evaluar(mariana, andrea, criterios, new int[]{5, 5, 5, 4, 4}, new String[]{
                "Trayectoria destacada en automatización e IA aplicada.", "Sólidos conocimientos en herramientas TI.",
                "Gran capacidad resolutiva.", "Alto impacto potencial.", "Presentación profesional estructurada."},
                "Gran capacidad de liderazgo y vasto dominio técnico DevOps.");

        log.info("Datos semilla cargados: {} candidatas, {} criterios, 4 evaluaciones", todas.size(), criterios.size());
    }

    private Candidata candidata(String nombre, String correo, String telefono, String area, int anos, String resumen,
                                String perfil, String cv, EstadoCandidatura estado) {
        Candidata c = new Candidata(nombre, correo, telefono, area, anos, resumen, perfil, cv, "Sistema");
        c.setEstado(estado);
        return c;
    }

    private void evaluar(Candidata c, Usuario evaluadora, List<Criterio> criterios, int[] puntajes, String[] obs,
                         String observacionGeneral) {
        List<int[]> pares = new ArrayList<>();
        for (int i = 0; i < criterios.size(); i++) {
            pares.add(new int[]{puntajes[i], criterios.get(i).getPesoPorcentaje()});
        }
        Evaluacion e = new Evaluacion(c, evaluadora, CalculadoraPuntaje.calcularPuntajePonderado(pares), observacionGeneral);
        for (int i = 0; i < criterios.size(); i++) {
            e.agregarPuntaje(criterios.get(i), puntajes[i], obs[i]);
        }
        evaluacionRepository.save(e);
    }
}
