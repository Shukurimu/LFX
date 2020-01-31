export PATH="${HOME}/jdk-11.0.2.jdk/Contents/Home/bin"
export PATH_TO_FX="${HOME}/javafx-sdk-11.0.2/lib"
javac --version
DESTINATION="classes"

java --module-path ${PATH_TO_FX} --add-modules javafx.graphics,javafx.controls --class-path ${DESTINATION} lfx.platform.Main
