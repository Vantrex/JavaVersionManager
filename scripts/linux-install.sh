#!/bin/bash
echo 'Installing JVM for Linux..'
DIRECTORY="$HOME/.jvm"
if [ ! -e "$DIRECTORY" ]; then
    mkdir "$DIRECTORY"
fi
if [ ! -e "$DIRECTORY/bin" ]; then
    mkdir "$DIRECTORY/bin"
fi
cd ".."
echo 'Building jar..'
#./gradlew shadowJar -q &> /dev/ null
./gradlew shadowJar
echo 'Built jar!'
echo 'Moving jar into home dir'
mv "build/libs/JDKSwitcher-1.0-SNAPSHOT-all.jar" "$DIRECTORY/bin/jvm.jar"
echo 'Cleanup..'
rm -r build
echo 'Creating start file'
cd "$DIRECTORY" || exit
# shellcheck disable=SC2016
echo '#!/bin/sh
java -jar '"$DIRECTORY"'/bin/jvm.jar $@
if [ "$#" -ge 1 ]; then
      source ~/.profile
fi' > 'jvm'
chmod 777 jvm
echo 'Modifying .bashrc..'
# shellcheck disable=SC2016
if ! grep -q '# This loads jvm' "$HOME/.bashrc"; then
    echo '
export JVM_DIR="$HOME/.jvm"
[ -s "$JVM_DIR/jvm" ] && \. "$JVM_DIR/jvm" # This loads jvm' >> "$HOME/.bashrc"
fi
echo 'Modified .bashrc!'
source "$HOME/.bashrc"
echo 'Installed JVM for Linux! Use "jvm" to run.'