@echo off
title Predicto - Starting all services
chcp 65001 >nul

echo ==========================================
echo  Starting Predicto - all services
echo ==========================================
echo.

cd /d "%~dp0"

:: 1. Docker: start Postgres + Redis
echo [1/3] Starting Docker services (Postgres + Redis)...
docker compose up -d 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Docker not available. Use start-no-docker.bat instead.
    pause
    exit /b
)
echo  Docker services running: Postgres :5432, Redis :6379, Adminer :8081
echo.

:: 2. Backend: Spring Boot (full profile = Postgres)
echo [2/3] Starting backend (Spring Boot)...
start "Predicto Backend" cmd /c "title Predicto Backend && "%~dp0run-backend.bat""

:: 3. Frontend: Vite dev server
echo [3/3] Starting frontend (Vite)...
start "Predicto Frontend" cmd /c "cd /d "%~dp0frontend" && title Predicto Frontend && echo Frontend starting at http://localhost:5174 && call npm run dev & pause"

echo.
echo ==========================================
echo  All services started.
echo  Backend  : http://localhost:8080
echo  Frontend : http://localhost:5174
echo  Adminer  : http://localhost:8081
echo.
echo  Close each window to stop the service.
echo ==========================================
pause
