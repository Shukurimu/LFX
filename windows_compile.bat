@echo off
set PATH_TO_FX="C:\Program Files\Java\javafx-sdk-11\lib"
javac --version
set DESTINATION="classes"

echo "compile_util"
javac --source-path lfx ^
      --class-path %DESTINATION% -d %DESTINATION% lfx/util/*.java

echo "compile_component"
javac --source-path lfx --module-path %PATH_TO_FX% --add-modules javafx.graphics ^
      --class-path %DESTINATION% -d %DESTINATION% lfx/component/*.java

echo "compile_interface"
javac --source-path lfx --module-path %PATH_TO_FX% --add-modules javafx.graphics ^
      --class-path %DESTINATION% -d %DESTINATION% lfx/map/Layer.java ^
          lfx/map/Environment.java lfx/object/Observable.java

echo "compile_extend"
javac --source-path lfx --module-path %PATH_TO_FX% --add-modules javafx.graphics ^
      --class-path %DESTINATION% -d %DESTINATION% lfx/map/Field.java ^
          lfx/object/Hero.java lfx/object/Weapon.java lfx/object/Energy.java

echo "compile_tools"
javac --source-path lfx ^
      --class-path %DESTINATION% -d %DESTINATION% lfx/tool/RawTxtParser.java

echo "compile_base"
javac --source-path lfx --module-path %PATH_TO_FX% --add-modules javafx.graphics ^
      --class-path %DESTINATION% -d %DESTINATION% lfx/map/BaseMap.java lfx/object/AbstractObject.java

echo "compile_concrete"
javac --source-path lfx --module-path %PATH_TO_FX% --add-modules javafx.graphics ^
      --class-path %DESTINATION% -d %DESTINATION% ^
          lfx/object/BaseHero.java lfx/object/BaseWeapon.java lfx/object/BaseEnergy.java

echo "compile_some"
javac --source-path lfx --module-path %PATH_TO_FX% --add-modules javafx.graphics ^
      --class-path %DESTINATION% -d %DESTINATION% ^
          lfx/platform/KeyboardController.java lfx/map/StatusBoard.java

echo "compile_scene"
javac --source-path lfx --module-path %PATH_TO_FX% --add-modules javafx.graphics,javafx.controls ^
      --class-path %DESTINATION% -d %DESTINATION% ^
          lfx/platform/ConfigScene.java lfx/platform/Arena.java

echo "compile_picking"
javac --source-path lfx --module-path %PATH_TO_FX% --add-modules javafx.graphics,javafx.controls ^
      --class-path %DESTINATION% -d %DESTINATION% ^
          lfx/platform/PickingScene.java

echo "compile_main"
javac --source-path lfx --module-path %PATH_TO_FX% --add-modules javafx.graphics,javafx.controls ^
      --class-path %DESTINATION% -d %DESTINATION% ^
          lfx/platform/Main.java
