@echo off
setlocal EnableExtensions
cd /d "%~dp0"
title WIM 100 - VIGILANTE - NO CERRAR
set "REPO=diegamigo-code/desarrollo_mobile"
set "WORKFLOW=publicar-api.yml"
set "PUERTO=8080"
set "SALUD=/api/panel"
set "TITULO_TUNEL=Tunel Cloudflare WIM 100 - NO CERRAR"
echo ============================================
echo   WIM 100 - backend + Cloudflare
echo   publicacion automatica de la URL en GitHub
echo ============================================
echo.

docker info >nul 2>&1
if not errorlevel 1 goto docker_ok
echo [1/6] Iniciando Docker Desktop...
if exist "%ProgramFiles%\Docker\Docker\Docker Desktop.exe" start "" "%ProgramFiles%\Docker\Docker\Docker Desktop.exe"
set /a TRIES=0
:wait_docker
timeout /t 5 /nobreak >nul
docker info >nul 2>&1
if not errorlevel 1 goto docker_ok
set /a TRIES+=1
if %TRIES% lss 36 goto wait_docker
echo [ERROR] Docker no respondio en 3 minutos. Abre Docker Desktop y vuelve a ejecutar este archivo.
pause
exit /b 1
:docker_ok
echo [1/6] Docker listo.

if exist .env findstr /b /c:"WIM_DB_PASSWORD=" .env >nul 2>&1 && goto env_ok
copy /y .env.example .env >nul
echo [2/6] Archivo .env creado desde .env.example.
goto env_done
:env_ok
echo [2/6] Archivo .env listo.
:env_done

echo [3/6] Levantando MySQL y la API. La primera vez tarda varios minutos...
docker compose up -d --build
if errorlevel 1 goto compose_fail
call :esperar_local 120
if errorlevel 1 goto local_fail
echo [3/6] Backend respondiendo en http://127.0.0.1:%PUERTO%
goto backend_ok
:compose_fail
echo [ERROR] docker compose no pudo levantar los servicios. Revisa el mensaje anterior.
pause
exit /b 1
:local_fail
echo [ERROR] La API no respondio. Revisa los logs con: docker compose logs api
pause
exit /b 1
:backend_ok

set "CF=%LOCALAPPDATA%\cloudflared\cloudflared.exe"
if exist "%CF%" goto cf_ok
echo [4/6] Descargando cloudflared...
if not exist "%LOCALAPPDATA%\cloudflared" mkdir "%LOCALAPPDATA%\cloudflared"
curl.exe -L -s -o "%CF%" https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-windows-amd64.exe
if exist "%CF%" goto cf_ok
echo [ERROR] No se pudo descargar cloudflared.
pause
exit /b 1
:cf_ok
echo [4/6] cloudflared listo.

set "GH="
for %%G in ("%ProgramFiles%\GitHub CLI\gh.exe" "%LOCALAPPDATA%\Programs\GitHub CLI\gh.exe") do if not defined GH if exist %%G set "GH=%%~G"
if not defined GH for /f "delims=" %%G in ('where gh 2^>nul') do if not defined GH set "GH=%%G"
if defined GH goto gh_have
echo [5/6] Instalando GitHub CLI...
winget install -e --id GitHub.cli --accept-package-agreements --accept-source-agreements
if exist "%ProgramFiles%\GitHub CLI\gh.exe" set "GH=%ProgramFiles%\GitHub CLI\gh.exe"
if defined GH goto gh_have
echo [AVISO] No se pudo instalar GitHub CLI. Tendras que actualizar la variable a mano.
goto gh_done
:gh_have
"%GH%" auth status -h github.com >nul 2>&1
if not errorlevel 1 goto gh_logged
echo.
echo [5/6] Inicia sesion en GitHub. Esto se hace UNA sola vez.
echo       Presiona Enter, copia el codigo que aparece y pegalo en el navegador.
"%GH%" auth login -h github.com -p https -w
"%GH%" auth status -h github.com >nul 2>&1
if errorlevel 1 (
  echo [AVISO] No se completo el inicio de sesion. Tendras que actualizar la variable a mano.
  set "GH="
  goto gh_done
)
:gh_logged
echo [5/6] GitHub CLI con sesion iniciada.
"%GH%" api -X POST "repos/%REPO%/pages" -f build_type=workflow >nul 2>&1
"%GH%" api -X PUT "repos/%REPO%/pages" -f build_type=workflow >nul 2>&1
:gh_done

:start_tunnel
echo.
echo [6/6] [%date% %time:~0,8%] Abriendo tunel de Cloudflare...
taskkill /fi "WINDOWTITLE eq %TITULO_TUNEL%*" /t /f >nul 2>&1
if exist tunnel.log del tunnel.log
start "%TITULO_TUNEL%" /min cmd /c ""%CF%" tunnel --url http://127.0.0.1:%PUERTO% --logfile tunnel.log"
set "URL="
set /a TRIES=0
:wait_url
timeout /t 2 /nobreak >nul
for /f "delims=" %%u in ('powershell -NoProfile -Command "if(Test-Path tunnel.log){(Select-String -Path tunnel.log -Pattern 'https://[a-z0-9-]+\.trycloudflare\.com' | Select-Object -First 1).Matches.Value}"') do set "URL=%%u"
if defined URL goto got_url
set /a TRIES+=1
if %TRIES% lss 45 goto wait_url
echo No se obtuvo direccion. Puede que no haya internet. Reintentando...
goto start_tunnel

:got_url
echo %URL%> tunnel_url.txt
echo Direccion nueva: %URL%
echo Esperando que responda...
set /a TRIES=0
:wait_public
set "CODE="
for /f %%c in ('curl.exe -s -o nul -m 10 -w "%%{http_code}" "%URL%%SALUD%"') do set "CODE=%%c"
if "%CODE%"=="401" goto publish
if "%CODE%"=="200" goto publish
set /a TRIES+=1
if %TRIES% geq 20 goto start_tunnel
timeout /t 3 /nobreak >nul
goto wait_public

:publish
echo Backend publico OK.
if not defined GH goto manual
echo Actualizando la variable en GitHub y publicando config.json...
"%GH%" variable set WIM_API_URL -R %REPO% -b "%URL%/api" || goto gh_fail
"%GH%" workflow run %WORKFLOW% -R %REPO% --ref main || goto gh_fail
echo.
echo ============================================
echo  LISTO. GitHub esta publicando la URL nueva.
echo  En 1 o 2 minutos la app la tomara desde:
echo  https://diegamigo-code.github.io/desarrollo_mobile/config.json
echo  API publica: %URL%/api
echo ============================================
goto monitor

:gh_fail
echo [AVISO] No se pudo actualizar GitHub automaticamente.
:manual
echo.
echo  Actualiza a mano en GitHub (%REPO%, Settings, Variables):
echo   WIM_API_URL = %URL%/api
echo  y ejecuta el workflow "Publicar URL de la API".

:monitor
echo.
echo Vigilando la conexion cada minuto. NO CIERRES esta ventana.
set /a FAILS=0
:mon_loop
timeout /t 60 /nobreak >nul
call :esperar_local 1
if errorlevel 1 (
  echo [%time:~0,8%] El backend no responde. Reiniciandolo...
  docker compose up -d
  call :esperar_local 60
)
set "CODE="
for /f %%c in ('curl.exe -s -o nul -m 15 -w "%%{http_code}" "%URL%%SALUD%"') do set "CODE=%%c"
if "%CODE%"=="401" (
  set /a FAILS=0
  goto mon_loop
)
if "%CODE%"=="200" (
  set /a FAILS=0
  goto mon_loop
)
set /a FAILS+=1
echo [%time:~0,8%] El tunel no responde, intento %FAILS% de 3.
if %FAILS% lss 3 goto mon_loop
echo El tunel se perdio. Creando uno nuevo y volviendo a publicar...
goto start_tunnel

:esperar_local
set /a ESPERA=0
:esperar_local_loop
set "LCODE="
for /f %%c in ('curl.exe -s -o nul -m 5 -w "%%{http_code}" "http://127.0.0.1:%PUERTO%%SALUD%"') do set "LCODE=%%c"
if "%LCODE%"=="401" exit /b 0
if "%LCODE%"=="200" exit /b 0
set /a ESPERA+=1
if %ESPERA% geq %~1 exit /b 1
timeout /t 5 /nobreak >nul
goto esperar_local_loop
