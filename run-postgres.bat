@echo off
cd /d "%~dp0backend"
set JAVA_HOME=C:\Program Files\BellSoft\LibericaJDK-21-Full
set PATH=%JAVA_HOME%\bin;C:\tools\maven\bin;%PATH%
echo Backend starting against real Postgres...
mvn spring-boot:run
pause
