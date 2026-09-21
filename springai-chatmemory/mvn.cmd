@echo off
REM Project-local Maven entrypoint: force JDK from .mvn\jdk.home, then use the wrapper.
setlocal
set "JDK_HOME_FILE=%~dp0.mvn\jdk.home"
if exist "%JDK_HOME_FILE%" (
  for /f "usebackq delims=" %%i in ("%JDK_HOME_FILE%") do set "JAVA_HOME=%%i"
)
if not defined JAVA_HOME set "JAVA_HOME=D:\jdk-25.0.2"
set "PATH=%JAVA_HOME%\bin;%PATH%"
call "%~dp0mvnw.cmd" %*
