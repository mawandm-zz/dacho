@echo off
sc create Dacho binPath= "%cd%\dacho.exe" DisplayName= "Dacho Service Manager" start= auto
if %ERRORLEVEL% == 0 (
	sc description Dacho "Manages services written in the Java Programming Language"
)