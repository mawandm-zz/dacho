@echo off
set DACHO_HOME=..
set JAVA_PATH=%JAVA_HOME%\jre\bin\java.exe
"%JAVA_PATH%" -Dxml.config=config/dacho.xml -Ddacho.home="%DACHO_HOME%" -Djava.util.logging.config.file="%DACHO_HOME%\config\logging.properties" -server -jar "%DACHO_HOME%\lib\dacho-service-manager-1.0.jar"
