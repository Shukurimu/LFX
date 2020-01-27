@echo off
javac --version
set DESTINATION="classes"
set PATH_TO_FX="C:\Program Files\Java\javafx-sdk-11\lib"

REM javac --source-path lfx ^
      REM --class-path %DESTINATION% -d %DESTINATION% lfx/util/*.java

REM javac --source-path lfx --module-path %PATH_TO_FX% --add-modules javafx.graphics ^
      REM --class-path %DESTINATION% -d %DESTINATION% lfx/component/*.java

REM javac --source-path lfx --module-path %PATH_TO_FX% --add-modules javafx.graphics ^
      REM --class-path %DESTINATION% -d %DESTINATION% lfx/map/Layer.java ^
          REM lfx/map/Environment.java lfx/object/Observable.java

REM javac --source-path lfx --module-path %PATH_TO_FX% --add-modules javafx.graphics ^
      REM --class-path %DESTINATION% -d %DESTINATION% lfx/map/BaseMap.java ^
          REM lfx/object/Hero.java lfx/object/Weapon.java lfx/object/Energy.java

REM javac --source-path lfx --module-path %PATH_TO_FX% --add-modules javafx.graphics ^
      REM --class-path %DESTINATION% -d %DESTINATION% lfx/object/AbstractObject.java

REM javac --source-path lfx --module-path %PATH_TO_FX% --add-modules javafx.graphics ^
      REM --class-path %DESTINATION% -d %DESTINATION% ^
          REM lfx/object/BaseHero.java lfx/object/BaseWeapon.java lfx/object/BaseEnergy.java

REM javac --source-path lfx --module-path %PATH_TO_FX% --add-modules javafx.graphics ^
      REM --class-path %DESTINATION% -d %DESTINATION% ^
          REM lfx/platform/KeyboardController.java lfx/platform/StatusBoard.java

javac --source-path lfx --module-path %PATH_TO_FX% --add-modules javafx.graphics,javafx.controls ^
      --class-path %DESTINATION% -d %DESTINATION% ^
          lfx/platform/ConfigScene.java
