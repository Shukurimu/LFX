@echo off
set PATH_TO_FX="C:\Program Files\Java\javafx-sdk-11\lib"
javac --version
set DESTINATION="classes"

javac --source-path lfx ^
      --class-path %DESTINATION% -d %DESTINATION% lfx/util/*.java

javac --source-path lfx --module-path %PATH_TO_FX% --add-modules javafx.graphics ^
      --class-path %DESTINATION% -d %DESTINATION% lfx/component/*.java

javac --source-path lfx --module-path %PATH_TO_FX% --add-modules javafx.graphics ^
      --class-path %DESTINATION% -d %DESTINATION% lfx/map/Layer.java ^
          lfx/map/Environment.java lfx/object/Observable.java

javac --source-path lfx --module-path %PATH_TO_FX% --add-modules javafx.graphics ^
      --class-path %DESTINATION% -d %DESTINATION% lfx/map/Field.java ^
          lfx/object/Hero.java lfx/object/Weapon.java lfx/object/Energy.java

javac --source-path lfx --module-path %PATH_TO_FX% --add-modules javafx.graphics ^
      --class-path %DESTINATION% -d %DESTINATION% lfx/map/BaseMap.java lfx/object/AbstractObject.java

javac --source-path lfx --module-path %PATH_TO_FX% --add-modules javafx.graphics ^
      --class-path %DESTINATION% -d %DESTINATION% ^
          lfx/object/BaseHero.java lfx/object/BaseWeapon.java lfx/object/BaseEnergy.java

javac --source-path lfx --module-path %PATH_TO_FX% --add-modules javafx.graphics ^
      --class-path %DESTINATION% -d %DESTINATION% ^
          lfx/platform/KeyboardController.java lfx/map/StatusBoard.java

javac --source-path lfx --module-path %PATH_TO_FX% --add-modules javafx.graphics,javafx.controls ^
      --class-path %DESTINATION% -d %DESTINATION% ^
          lfx/platform/ConfigScene.java lfx/platform/Arena.java

javac --source-path lfx --module-path %PATH_TO_FX% --add-modules javafx.graphics,javafx.controls ^
      --class-path %DESTINATION% -d %DESTINATION% ^
          lfx/platform/PickingScene.java

javac --source-path lfx --module-path %PATH_TO_FX% --add-modules javafx.graphics,javafx.controls ^
      --class-path %DESTINATION% -d %DESTINATION% ^
          lfx/platform/Main.java
