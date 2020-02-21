export PATH="${HOME}/jdk-11.0.2.jdk/Contents/Home/bin"
export PATH_TO_FX="${HOME}/javafx-sdk-11.0.2/lib"
javac --version
BASIC_ARGUMENTS="--source-path lfx --class-path classes -d classes"
set -e

echo 'compile util'
javac ${BASIC_ARGUMENTS} \
      lfx/util/Area.java lfx/util/Const.java lfx/util/Looper.java lfx/util/Point.java lfx/util/Tuple.java lfx/util/Util.java

echo 'compile util.resource'
javac ${BASIC_ARGUMENTS} --module-path ${PATH_TO_FX} --add-modules javafx.graphics \
      lfx/util/Resource.java

echo 'compile base'
javac ${BASIC_ARGUMENTS} \
      lfx/base/*.java

echo 'compile component'
javac ${BASIC_ARGUMENTS} --module-path ${PATH_TO_FX} --add-modules javafx.graphics \
      lfx/component/*.java

echo 'compile interface'
javac ${BASIC_ARGUMENTS} --module-path ${PATH_TO_FX} --add-modules javafx.graphics \
      lfx/map/Layer.java lfx/map/Environment.java lfx/object/Observable.java

echo 'compile extend'
javac ${BASIC_ARGUMENTS} --module-path ${PATH_TO_FX} --add-modules javafx.graphics \
      lfx/map/Field.java lfx/object/Hero.java lfx/object/Weapon.java lfx/object/Energy.java

echo 'compile tools'
javac ${BASIC_ARGUMENTS} \
      lfx/tool/Parser.java

echo 'compile base'
javac ${BASIC_ARGUMENTS} --module-path ${PATH_TO_FX} --add-modules javafx.graphics \
      lfx/map/BaseMap.java lfx/object/AbstractObject.java

echo 'compile concrete'
javac ${BASIC_ARGUMENTS} --module-path ${PATH_TO_FX} --add-modules javafx.graphics \
      lfx/object/BaseHero.java lfx/object/BaseWeapon.java lfx/object/BaseEnergy.java

echo 'compile some'
javac ${BASIC_ARGUMENTS} --module-path ${PATH_TO_FX} --add-modules javafx.graphics \
      lfx/platform/KeyboardController.java lfx/map/StatusBoard.java

echo 'compile scene'
javac ${BASIC_ARGUMENTS} --module-path ${PATH_TO_FX} --add-modules javafx.graphics,javafx.controls \
      lfx/platform/ConfigScene.java lfx/platform/Arena.java

echo 'compile picking'
javac ${BASIC_ARGUMENTS} --module-path ${PATH_TO_FX} --add-modules javafx.graphics,javafx.controls \
      lfx/platform/PickingScene.java

echo 'compile main'
javac ${BASIC_ARGUMENTS} --module-path ${PATH_TO_FX} --add-modules javafx.graphics,javafx.controls \
      lfx/platform/Main.java

