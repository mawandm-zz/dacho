@echo off
set DACHO_HOME=..
set JAVA_PATH=%JAVA_HOME%\jre\bin\java.exe
%JAVA_PATH% -Dxml.config=config/dacho.xml -Ddacho.home="%DACHO_HOME%" -server -jar "%DACHO_HOME%\dacho-service-manager-1.0.jar"
