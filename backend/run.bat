@echo off
set JAVA_HOME=C:\Program Files\BellSoft\LibericaJDK-21-Full
set M2_HOME=C:\tools\maven
set PATH=%JAVA_HOME%\bin;%M2_HOME%\bin;%PATH%
cd /d "%~dp0"
echo Starting backend against real Postgres...
call mvn spring-boot:run
