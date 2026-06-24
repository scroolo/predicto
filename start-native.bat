@echo off
title Predicto - Native Postgres mode
chcp 65001 >nul

echo ==========================================
echo  Starting Predicto (native Postgres)
echo  No Docker or Redis required
echo ==========================================
echo.

cd /d "%~dp0"

echo [1/2] Starting backend (Spring Boot + Postgres)...
start "Predicto Backend" cmd /c "title Predicto Backend && "%~dp0backend\run.bat""

echo [2/2] Starting frontend (Vite)...
start "Predicto Frontend" cmd /c "cd /d "%~dp0frontend" && title Predicto Frontend && echo Frontend starting at http://localhost:5173 && call npm run dev & pause"

echo.
echo ==========================================
echo  Started.
echo  Backend  : http://localhost:8080
echo  Frontend : http://localhost:5173
echo.
echo  Close each window to stop the service.
echo ==========================================
pause
