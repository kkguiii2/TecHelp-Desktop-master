@echo off
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

REM Ajusta o caminho do JavaFX para usar as dependências do Maven
set JAVAFX_PATH=target\dependency

java --module-path "%JAVAFX_PATH%" ^
     --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base ^
     --add-opens javafx.controls/com.sun.javafx.scene.control.behavior=ALL-UNNAMED ^
     --add-opens javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED ^
     --add-opens javafx.base/com.sun.javafx.binding=ALL-UNNAMED ^
     --add-opens javafx.base/com.sun.javafx.event=ALL-UNNAMED ^
     --add-opens javafx.graphics/com.sun.javafx.stage=ALL-UNNAMED ^
     -cp "target\classes;target\dependency\*" ^
     com.techelp.Launcher 