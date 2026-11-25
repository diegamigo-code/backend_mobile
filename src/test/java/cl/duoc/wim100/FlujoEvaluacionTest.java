package cl.duoc.wim100;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.stream.IntStream;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:wim;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class FlujoEvaluacionTest {
    @Autowired MockMvc api;
    @Autowired ObjectMapper json;

    private MockHttpSession login(String usuario, String clave) throws Exception {
        var respuesta = api.perform(post("/api/auth/login").contentType(APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("usuario", usuario, "password", clave))))
                .andExpect(status().isOk()).andReturn();
        return (MockHttpSession) respuesta.getRequest().getSession(false);
    }

    @Test
    void evaluacionVisibleDesdeOtraSesion() throws Exception {
        api.perform(get("/api/panel")).andExpect(status().isUnauthorized());
        MockHttpSession evaluadora = login("evaluadora", "Rosa100!");
        MockHttpSession direccion = login("direccion", "Orbita100!");
        String postulante = """
                {"nombre":"Ejemplo WIM","correo":"prueba@test.cl","telefono":"+56 9 1234 5678",
                 "area":"Ciberseguridad","aniosExperiencia":5,"resumenExperiencia":"Experiencia en proyectos de seguridad informática.",
                 "linkedinUrl":"https://example.com/perfil","cvNombre":"cv.pdf","registradaPor":"Evaluadora"}
                """;
        String respuesta = api.perform(post("/api/postulantes").session(evaluadora)
                .contentType(APPLICATION_JSON).content(postulante))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id = json.readTree(respuesta).get("id").asLong();
        var notas = IntStream.rangeClosed(1, 5)
                .mapToObj(i -> Map.of("criterioId", i, "puntaje", i == 1 ? 5 : 3, "observacion", ""))
                .toList();
        String ruta = "/api/postulantes/" + id + "/evaluaciones";
        String evaluacion = json.writeValueAsString(Map.of("observacionGeneral", "Cumple con las competencias evaluadas.", "notas", notas));
        api.perform(post(ruta).session(direccion).contentType(APPLICATION_JSON).content(evaluacion))
                .andExpect(status().isForbidden());
        api.perform(post(ruta).session(evaluadora).contentType(APPLICATION_JSON).content(evaluacion))
                .andExpect(status().isCreated());
        api.perform(post(ruta).session(evaluadora).contentType(APPLICATION_JSON).content(evaluacion))
                .andExpect(status().isConflict());
        api.perform(get("/api/panel").session(direccion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ranking[0].candidataId").value((int) id))
                .andExpect(jsonPath("$.ranking[0].cantidadEvaluaciones").value(1))
                .andExpect(jsonPath("$.ranking[0].promedioPuntaje").value(70.0))
                .andExpect(jsonPath("$.ranking[0].estado").value("EN_EVALUACION"))
                .andExpect(jsonPath("$.historial[0].accion").value("Evaluación registrada"));
        MockHttpSession segunda = login("evaluadora2", "wim2026");
        api.perform(post(ruta).session(segunda).contentType(APPLICATION_JSON).content(evaluacion))
                .andExpect(status().isCreated());
        api.perform(get("/api/panel").session(direccion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ranking[0].cantidadEvaluaciones").value(2))
                .andExpect(jsonPath("$.ranking[0].estado").value("EVALUADA"));
        api.perform(post("/api/auth/logout").session(direccion)).andExpect(status().isNoContent());
    }
}
