export PATH="${HOME}/jdk-11.0.2.jdk/Contents/Home/bin"
export PATH_TO_FX="${HOME}/javafx-sdk-11.0.2/lib"
javac --version
DESTINATION="classes"

function compile_util() {
    echo "${FUNCNAME}"
    javac --source-path lfx \
          --class-path ${DESTINATION} -d ${DESTINATION} lfx/util/*.java
}

function compile_component() {
    echo "${FUNCNAME}"
    javac --source-path lfx --module-path ${PATH_TO_FX} --add-modules javafx.graphics \
          --class-path ${DESTINATION} -d ${DESTINATION} lfx/component/*.java
}

function compile_interface() {
    echo "${FUNCNAME}"
    javac --source-path lfx --module-path ${PATH_TO_FX} --add-modules javafx.graphics \
          --class-path ${DESTINATION} -d ${DESTINATION} lfx/map/Layer.java \
              lfx/map/Environment.java lfx/object/Observable.java
}

function compile_extend() {
    echo "${FUNCNAME}"
    javac --source-path lfx --module-path ${PATH_TO_FX} --add-modules javafx.graphics \
          --class-path ${DESTINATION} -d ${DESTINATION} lfx/map/Field.java \
              lfx/object/Hero.java lfx/object/Weapon.java lfx/object/Energy.java
}

function compile_tools() {
    echo "${FUNCNAME}"
    javac --source-path lfx \
          --class-path ${DESTINATION} -d ${DESTINATION} lfx/tool/RawTxtParser.java
}

function compile_base() {
    echo "${FUNCNAME}"
    javac --source-path lfx --module-path ${PATH_TO_FX} --add-modules javafx.graphics \
          --class-path ${DESTINATION} -d ${DESTINATION} lfx/map/BaseMap.java lfx/object/AbstractObject.java
}

function compile_concrete() {
    echo "${FUNCNAME}"
    javac --source-path lfx --module-path ${PATH_TO_FX} --add-modules javafx.graphics \
          --class-path ${DESTINATION} -d ${DESTINATION} \
              lfx/object/BaseHero.java lfx/object/BaseWeapon.java lfx/object/BaseEnergy.java
}

function compile_some() {
    echo "${FUNCNAME}"
    javac --source-path lfx --module-path ${PATH_TO_FX} --add-modules javafx.graphics \
          --class-path ${DESTINATION} -d ${DESTINATION} \
              lfx/platform/KeyboardController.java lfx/map/StatusBoard.java
}

function compile_scene() {
    echo "${FUNCNAME}"
    javac --source-path lfx --module-path ${PATH_TO_FX} --add-modules javafx.graphics,javafx.controls \
          --class-path ${DESTINATION} -d ${DESTINATION} \
              lfx/platform/ConfigScene.java lfx/platform/Arena.java
}

function compile_picking() {
    echo "${FUNCNAME}"
    javac --source-path lfx --module-path ${PATH_TO_FX} --add-modules javafx.graphics,javafx.controls \
          --class-path ${DESTINATION} -d ${DESTINATION} lfx/platform/PickingScene.java
}

function compile_main() {
    echo "${FUNCNAME}"
    javac --source-path lfx --module-path ${PATH_TO_FX} --add-modules javafx.graphics,javafx.controls \
          --class-path ${DESTINATION} -d ${DESTINATION} lfx/platform/Main.java
}

compile_util
compile_component
compile_interface
compile_extend
compile_tools
compile_base
compile_concrete
compile_some
compile_scene
compile_picking
compile_main
