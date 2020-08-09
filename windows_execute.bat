@echo off
set PATH_TO_FX="C:\Program Files\Java\javafx-sdk-11\lib"
REM javac --version
set DESTINATION="classes"

java --module-path %PATH_TO_FX% --add-modules javafx.graphics,javafx.controls --class-path %DESTINATION% lfx.platform.ConfigScene
REM java --class-path %DESTINATION% lfx.tool.Parser rawdata/template.txt
