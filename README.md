# WIM 100 — Backend (Spring Boot + Spring Security + JWT)

API REST para la app Android **WIM 100**. Expone el mismo dominio que hoy vive en Room
(candidatas, criterios ponderados, evaluaciones, ranking y auditoría) y reemplaza el
login "nombre + rol" por autenticación real con contraseña y **JWT**.

| | |
|---|---|
| Java | 21 |
| Spring Boot | 4.1.0 (Spring Security 7, Hibernate 7) |
| JWT | jjwt 0.12.6 — HMAC-SHA256 |
| BD | PostgreSQL 17 (Docker) · H2 en memoria (perfil `dev` y tests) |

## Puesta en marcha

### Opción A — Docker (recomendado)

Requiere Docker Desktop. Ya viene un `.env` con una `JWT_SECRET` y clave de BD generadas al azar
(`.env.example` es la plantilla).

```bash
cd backend
docker compose up -d --build
docker compose logs -f backend      # esperar "Started Wim100BackendApplication"
```

API en `http://localhost:8080` · Healthcheck: `http://localhost:8080/actuator/health`

Para detener: `docker compose down` (agrega `-v` para borrar también los datos).

### Opción B — Sin Docker (H2 en memoria)

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

O en IntelliJ: abrir `backend/pom.xml` como proyecto, ejecutar `Wim100BackendApplication`
con el perfil activo `dev`.

### Tests

```bash
mvn test
```

## Usuarias de ejemplo

Se crean solo si la base está vacía (`SEED_ENABLED=true`). Contraseña: `SEED_DEFAULT_PASSWORD`
(por defecto `Wim100.2026`). **Cámbiala o desactiva la semilla fuera de desarrollo.**

| Usuario | Rol |
|---|---|
| `andrea_torres` | Evaluadora |
| `maria_fernandez` | Evaluadora |
| `direccion` | Dirección |

## Autenticación

1. `POST /api/auth/login` con `{"username","password"}` → devuelve `token`, `expiraEn` y la usuaria.
2. En cada llamada: cabecera `Authorization: Bearer <token>`.
3. El token lleva `sub` (usuario), `rol`, `nombre`, `iss`, `iat`, `exp` y `jti`. Dura `JWT_EXPIRATION` (8 h por defecto).
4. En cada request se recarga la usuaria desde la BD, así que desactivar una cuenta o cambiarle el rol aplica de inmediato.

Errores: `401` sin token / token inválido o expirado, `403` rol sin permiso. Todos los errores usan el mismo JSON:

```json
{ "timestamp": "...", "status": 400, "error": "Bad Request",
  "mensaje": "La solicitud contiene datos inválidos", "path": "/api/candidatas",
  "campos": { "telefono": "Formato chileno inválido (+56 9 XXXX XXXX)" } }
```

## Endpoints

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| POST | `/api/auth/login` | público | Login, entrega JWT |
| POST | `/api/auth/logout` | autenticada | Registra el cierre de sesión en auditoría |
| GET | `/api/auth/me` | autenticada | Perfil de la sesión |
| GET | `/api/candidatas?q=` | autenticada | Lista / búsqueda por nombre o área |
| GET | `/api/candidatas/{id}` | autenticada | Detalle |
| POST | `/api/candidatas` | Evaluadora, Dirección | Registrar candidata |
| PATCH | `/api/candidatas/{id}/estado` | Dirección | Cambiar estado (ej. `SELECCIONADA`) |
| GET | `/api/candidatas/{id}/evaluaciones` | autenticada | Evaluaciones con puntaje por criterio |
| POST | `/api/candidatas/{id}/evaluaciones` | Evaluadora | Registrar evaluación |
| GET | `/api/candidatas/{id}/historial` | Dirección | Auditoría de la candidata |
| GET | `/api/postulantes` · `/{id}` | autenticada | Contrato de la app Android (ver abajo) |
| POST | `/api/postulantes` | Evaluadora, Dirección | Registrar postulante |
| PUT | `/api/postulantes/{id}` | Evaluadora | Editar postulante |
| PATCH | `/api/postulantes/{id}/estado` | Evaluadora (estados intermedios), Dirección (también resolver) | Cambiar estado |
| DELETE | `/api/postulantes/{id}` | Evaluadora | Eliminar postulante y sus evaluaciones |
| GET | `/api/criterios` | autenticada | Matriz de criterios y pesos |
| GET | `/api/catalogos/areas` · `/estados` | autenticada | Listas para formularios |
| GET | `/api/ranking` | Dirección | Ranking consolidado con categoría |
| GET | `/api/historial` | Dirección | Auditoría completa |
| GET/POST | `/api/usuarios` | Dirección | Listar / crear cuentas |

Ejemplos listos para ejecutar en `api.http`.

## Contrato con la app Android

`/api/postulantes` usa los nombres de campos de la app (`PostulanteDto` en Kotlin):

```json
{ "id": 1, "nombre": "Camila Morales", "correo": "camila.morales@example.com",
  "telefono": "+56 9 1234 5678", "area": "Forense y respuesta a incidentes",
  "aniosExperiencia": 6, "resumenExperiencia": "...", "linkedinUrl": "https://...",
  "cvNombre": "cv_camila_morales.pdf", "estado": "EN_EVALUACION",
  "fechaRegistro": 1764072000000, "registradaPor": "Sistema" }
```

`fechaRegistro` va en milisegundos. `id`, `estado`, `fechaRegistro` y `registradaPor` se ignoran al crear o editar:
los define el servidor (la autora se toma del token).

Áreas válidas: Seguridad ofensiva (Red Team), Seguridad defensiva (Blue Team), Gobierno, riesgo y cumplimiento,
Ciberseguridad industrial (OT), Forense y respuesta a incidentes, Investigación y academia.

## Reglas de negocio (las mismas de la app)

- Validaciones de `CandidataValidator`: nombre ≥ 3, correo con dominio permitido (`DOMINIOS_PERMITIDOS`),
  teléfono chileno, área del catálogo, experiencia 0–50, resumen 30–500, enlace http(s), CV obligatorio.
- Correo de candidata único (el intento duplicado queda en auditoría).
- Una evaluación por evaluadora y candidata (el intento duplicado queda en auditoría → `409`).
- Deben calificarse **todos** los criterios, 1 a 5; puntaje 1–2 exige justificación ≥ 10 caracteres;
  observación general 20–400.
- Nota = Σ(puntaje × peso) / (Σpeso × 5) × 100. Categorías: Sobresaliente ≥ 85, Sólida ≥ 70, En desarrollo ≥ 50, Insuficiente.
- Primera evaluación pasa la candidata de *Postulada* a *En evaluación*.
- **Nuevo:** una candidatura resuelta (Seleccionada / No seleccionada) no admite más evaluaciones.
- **Nuevo:** la evaluadora se toma del token, no del cuerpo (no se puede evaluar a nombre de otra).

## Variables de entorno

| Variable | Por defecto | |
|---|---|---|
| `DB_URL` / `DB_USER` / `DB_PASSWORD` | `jdbc:postgresql://localhost:5432/wim100` / `wim100` / `wim100` | |
| `JWT_SECRET` | — (obligatoria) | Base64, ≥ 32 bytes |
| `JWT_EXPIRATION` | `8h` | |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000,http://localhost:5173` | Para clientes web |
| `SEED_ENABLED` / `SEED_DEFAULT_PASSWORD` | `true` / `Wim100.2026` | |
| `DOMINIOS_PERMITIDOS` | `ejemplo.cl,example.com,test.cl` | |
| `JPA_DDL_AUTO` | `update` | En producción: `validate` + Flyway |

## Conectar la app Android

- Desde el **emulador**, el backend del PC es `http://10.0.2.2:8080`.
- Desde un **teléfono físico**, usa la IP del PC en la red local (ej. `http://192.168.1.20:8080`).
- Para HTTP sin TLS en desarrollo hay que permitir cleartext (`android:usesCleartextTraffic="true"` o un
  `network_security_config`) y agregar el permiso `INTERNET` al `AndroidManifest.xml`.
- Guardar el token (por ejemplo en DataStore) y enviarlo con un interceptor de OkHttp/Retrofit.

## Pendiente / siguientes pasos sugeridos

- Refresh tokens y lista de revocación (hoy el logout solo audita; el token vale hasta que expira).
- Migraciones con Flyway en lugar de `ddl-auto: update`.
- Documentación OpenAPI/Swagger (springdoc).
- Rate-limit en `/api/auth/login`.
