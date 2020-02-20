@echo off
set PATH_TO_FX="C:\Program Files\Java\javafx-sdk-11\lib"
javac --version
set DESTINATION="classes"

echo "compile util"
javac --source-path lfx ^
      --class-path %DESTINATION% -d %DESTINATION% lfx/util/*.java

echo "compile component"
javac --source-path lfx --module-path %PATH_TO_FX% --add-modules javafx.graphics ^
      --class-path %DESTINATION% -d %DESTINATION% lfx/component/*.java

echo "compile interface"
javac --source-path lfx --module-path %PATH_TO_FX% --add-modules javafx.graphics ^
      --class-path %DESTINATION% -d %DESTINATION% lfx/map/Layer.java ^
          lfx/map/Environment.java lfx/object/Observable.java

echo "compile extend"
javac --source-path lfx --module-path %PATH_TO_FX% --add-modules javafx.graphics ^
      --class-path %DESTINATION% -d %DESTINATION% lfx/map/Field.java ^
          lfx/object/Hero.java lfx/object/Weapon.java lfx/object/Energy.java

echo "compile tools"
javac --source-path lfx ^
      --class-path %DESTINATION% -d %DESTINATION% lfx/tool/Parser.java

echo "compile base"
javac --source-path lfx --module-path %PATH_TO_FX% --add-modules javafx.graphics ^
      --class-path %DESTINATION% -d %DESTINATION% lfx/map/BaseMap.java lfx/object/AbstractObject.java

echo "compile concrete"
javac --source-path lfx --module-path %PATH_TO_FX% --add-modules javafx.graphics ^
      --class-path %DESTINATION% -d %DESTINATION% ^
          lfx/object/BaseHero.java lfx/object/BaseWeapon.java lfx/object/BaseEnergy.java

echo "compile some"
javac --source-path lfx --module-path %PATH_TO_FX% --add-modules javafx.graphics ^
      --class-path %DESTINATION% -d %DESTINATION% ^
          lfx/platform/KeyboardController.java lfx/map/StatusBoard.java

echo "compile scene"
javac --source-path lfx --module-path %PATH_TO_FX% --add-modules javafx.graphics,javafx.controls ^
      --class-path %DESTINATION% -d %DESTINATION% ^
          lfx/platform/ConfigScene.java lfx/platform/Arena.java

echo "compile picking"
javac --source-path lfx --module-path %PATH_TO_FX% --add-modules javafx.graphics,javafx.controls ^
      --class-path %DESTINATION% -d %DESTINATION% ^
          lfx/platform/PickingScene.java

echo "compile main"
javac --source-path lfx --module-path %PATH_TO_FX% --add-modules javafx.graphics,javafx.controls ^
      --class-path %DESTINATION% -d %DESTINATION% ^
          lfx/platform/Main.java
