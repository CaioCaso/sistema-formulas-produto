@echo off
setlocal

set "SQLITE_JAR=lib\sqlite-jdbc-3.51.0.0.jar"

echo Compilando o projeto...
javac -cp ".;%SQLITE_JAR%" ApiServer.java FormulaTree.java Database.java FormulaService.java ItemFormula.java TestMain.java
if errorlevel 1 (
    echo.
    echo Falha na compilacao. Verifique se o JDK esta instalado e se javac funciona no terminal.
    pause
    exit /b 1
)

echo Iniciando o servidor em http://localhost:8050 ...
java -cp ".;%SQLITE_JAR%" ApiServer

endlocal
