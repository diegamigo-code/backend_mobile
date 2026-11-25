# API de WIM 100

El servicio Spring Boot usa Java 17, Maven, Spring MVC, JPA y MySQL 8.4. El proyecto usa el mismo enfoque de LIF para la imagen multietapa, el volumen de MySQL y la espera de disponibilidad de la base. WIM concentra sus funciones en un servicio y usa sesiones HTTP.

## Ejecución local

Desde esta carpeta, prepara la contraseña de MySQL en `.env` y levanta los dos servicios. La app Android está en el repositorio [desarrollo_mobile](https://github.com/diegamigo-code/desarrollo_mobile).

```sh
cp -n .env.example .env
docker compose up --build -d
docker compose logs -f api
```

La API escucha en el puerto 8080 solo para este PC. Los teléfonos se conectan a través del túnel de Cloudflare (ver más abajo). Para exponerla en la red local, define `WIM_BIND=0.0.0.0` en `.env`. MySQL conserva los datos en el volumen `wim100_data`. Para detener ambos servicios conservando los datos, ejecuta `docker compose down`. Para usar otro puerto del anfitrión, configura `WIM_PORT` en `.env` y ajusta la URL de la app.

La app debug usa `http://10.0.2.2:8080/api` en el emulador Android. Para usar un teléfono por USB, ejecuta `adb reverse tcp:8080 tcp:8080` y compila con `./gradlew :app:installDebug -PwimApiUrl=http://127.0.0.1:8080/api`. Para un teléfono en la misma red Wi-Fi, abre el puerto 8080 en el firewall de Windows y compila con `-PwimApiUrl=http://IP-DEL-PC:8080/api`. Para una API desplegada, indica su URL HTTPS en la propiedad Gradle `wimApiUrl`.

## Acceso desde cualquier teléfono (túnel de Cloudflare)

Ejecuta `publicar_backend.bat` con doble clic y deja la ventana abierta. El script:

1. Inicia Docker Desktop si está cerrado y crea `.env` desde `.env.example` si falta.
2. Levanta MySQL y la API con `docker compose up -d --build`.
3. Descarga `cloudflared` y abre un túnel rápido de Cloudflare hacia `http://127.0.0.1:8080`.
4. Con GitHub CLI guarda la URL pública en la variable `WIM_API_URL` del repositorio
   [desarrollo_mobile](https://github.com/diegamigo-code/desarrollo_mobile) y ejecuta el workflow
   `publicar-api.yml`, que publica `https://diegamigo-code.github.io/desarrollo_mobile/config.json`.
5. Vigila el backend y el túnel cada minuto. Si el túnel se cae, abre uno nuevo y vuelve a publicar la URL.

La app lee `config.json` al iniciar y otra vez si pierde la conexión, así que los teléfonos no necesitan reinstalarla
cuando cambia la URL del túnel. La primera vez el script pide iniciar sesión en GitHub (una sola vez).

## Cuentas de demostración

| Rol | Usuario | Contraseña |
| --- | --- | --- |
| Evaluadora | evaluadora | Rosa100! |
| Segunda evaluadora | evaluadora2 | wim2026 |
| Dirección | direccion | Orbita100! |

El backend crea las cuentas de demostración con hashes BCrypt en una base nueva. Las cuentas son públicas para pruebas con datos ficticios. Cada cliente conserva su cookie de sesión en memoria.

## Operaciones HTTP

| Método | Ruta | Operación |
| --- | --- | --- |
| POST | /api/auth/login | Valida usuario y contraseña, devuelve el perfil y la cookie de sesión |
| POST | /api/auth/logout | Invalida la sesión |
| GET | /api/panel | Candidaturas, criterios, ranking ponderado e historial por candidatura |
| GET | /api/postulantes | Lista de candidaturas |
| GET | /api/postulantes/{id} | Ficha de una candidatura |
| POST | /api/postulantes | Alta |
| PUT | /api/postulantes/{id} | Edición |
| PATCH | /api/postulantes/{id}/estado | Cambio de estado |
| DELETE | /api/postulantes/{id} | Elimina una candidatura sin evaluaciones asociadas |
| POST | /api/postulantes/{id}/evaluaciones | Guarda las cinco notas, su puntaje ponderado y el evento de historial |

Las lecturas requieren sesión. Las escrituras de candidaturas y evaluaciones requieren el rol Evaluadora. El servidor toma la identidad de la sesión y admite una evaluación por evaluadora y candidatura. El panel incluye los resultados de todas las evaluadoras. La app lo actualiza al entrar a las pantallas.

Para comprobar la sesión y consultar el ranking con Compose en marcha:

```sh
sesion=$(mktemp)
curl --fail-with-body -c "$sesion" -H 'Content-Type: application/json' \
  -d '{"usuario":"direccion","password":"Orbita100!"}' http://127.0.0.1:8080/api/auth/login
curl --fail-with-body -b "$sesion" http://127.0.0.1:8080/api/panel
curl --fail-with-body -X POST -b "$sesion" http://127.0.0.1:8080/api/auth/logout
rm "$sesion"
```

Con Maven instalado, `mvn test` ejecuta una prueba de integración del flujo entre evaluadora y dirección con H2. El Dockerfile ejecuta esa prueba al construir la imagen.
